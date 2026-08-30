# DistributedSchedulerService Broker集成

## 更新概述

`DistributedSchedulerService`已经更新，现在从`ke_broker_info`表中获取Kafka broker信息，而不是从配置文件读取。同时移除了无效任务类型清理功能。

## 主要变化

### 1. 移除配置文件依赖
- **移除jmxUri配置**: 不再从`application.properties`中读取`efak.jmx.uri`
- **移除bootstrapServers配置**: 不再从`application.properties`中读取`kafka.bootstrap.servers`
- **动态获取broker信息**: 从`ke_broker_info`表中动态获取broker地址和JMX端口

### 2. 添加BrokerMapper依赖
```java
@Autowired
private BrokerMapper brokerMapper;
```

### 3. 新增getJmxUriFromBroker方法
```java
/**
 * 从broker信息中获取JMX URI
 */
private String getJmxUriFromBroker() {
    try {
        List<BrokerInfo> brokers = brokerMapper.queryAllBrokers();
        if (brokers.isEmpty()) {
            log.warn("ke_broker_info表中没有broker信息");
            return null;
        }

        // 获取第一个在线的broker
        BrokerInfo onlineBroker = brokers.stream()
                .filter(broker -> "online".equals(broker.getStatus()))
                .findFirst()
                .orElse(brokers.get(0)); // 如果没有在线的，使用第一个

        if (onlineBroker.getJmxPort() != null && onlineBroker.getJmxPort() > 0) {
            String jmxUri = onlineBroker.getHostIp() + ":" + onlineBroker.getJmxPort();
            log.debug("从broker获取JMX URI: {}", jmxUri);
            return jmxUri;
        } else {
            log.warn("Broker {} 没有配置JMX端口", onlineBroker.getBrokerId());
            return null;
        }
    } catch (Exception e) {
        log.error("从broker信息获取JMX URI失败", e);
        return null;
    }
}
```

### 4. 更新JMX指标获取逻辑
```java
// 从JMX获取Topic性能指标
Map<String, Object> jmxMetrics = null;
try {
    String jmxUri = getJmxUriFromBroker();
    if (jmxUri != null && !jmxUri.trim().isEmpty()) {
        jmxMetrics = JmxServiceProxy.getTopicMetrics(jmxUri, topicName);
    }
} catch (Exception e) {
    log.warn("获取JMX指标失败: {}", e.getMessage());
}
```

### 5. 移除cleanupInvalidTaskTypes功能
- 删除了`cleanupInvalidTaskTypes()`方法
- 移除了初始化时对无效任务类型的清理
- 简化了系统启动流程

## 数据库表结构

### ke_broker_info表
```sql
CREATE TABLE IF NOT EXISTS `ke_broker_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `broker_id` int(11) NOT NULL COMMENT 'Broker ID',
  `host_ip` varchar(50) NOT NULL COMMENT '主机IP',
  `port` int(11) NOT NULL DEFAULT 9092 COMMENT '端口',
  `jmx_port` int(11) DEFAULT NULL COMMENT 'JMX端口',
  `status` varchar(20) NOT NULL DEFAULT 'offline' COMMENT '状态(online/offline)',
  `cpu_usage` decimal(5,2) DEFAULT NULL COMMENT 'CPU使用率(%)',
  `memory_usage` decimal(5,2) DEFAULT NULL COMMENT '内存使用率(%)',
  `startup_time` timestamp NULL DEFAULT NULL COMMENT '启动时间',
  `version` varchar(50) DEFAULT NULL COMMENT '版本号',
  `kafka_home` varchar(500) DEFAULT NULL COMMENT 'Kafka安装目录',
  `startup_script` varchar(500) DEFAULT NULL COMMENT '启动脚本路径',
  `config_file` varchar(500) DEFAULT NULL COMMENT '配置文件路径',
  `ssh_username` varchar(100) DEFAULT NULL COMMENT 'SSH用户名',
  `ssh_port` int(11) DEFAULT 22 COMMENT 'SSH端口',
  `ssh_password` varchar(255) DEFAULT NULL COMMENT 'SSH密码(加密存储)',
  `private_key_path` varchar(500) DEFAULT NULL COMMENT '私钥路径',
  `remark` text COMMENT '备注信息',
  `created_by` varchar(100) DEFAULT NULL COMMENT '创建人',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_broker_host_port` (`broker_id`, `host_ip`, `port`),
  KEY `idx_status` (`status`),
  KEY `idx_host_ip` (`host_ip`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Kafka Broker信息表';
```

## 配置更新

### 移除的配置项
```properties
# 以下配置项已移除
# efak.jmx.uri=127.0.0.1:9988
# kafka.bootstrap.servers=localhost:9092
```

### 保留的配置项
```properties
# 分布式调度器配置
efak.scheduler.distributed.enabled=true
efak.scheduler.distributed.interval=60
efak.scheduler.distributed.interval=60000
```

## 工作流程

### 1. 系统启动
```java
@PostConstruct
public void init() {
    schedulerEnabled = true;
    
    if (schedulerEnabled) {
        try {
            // 初始化任务协调器
            taskCoordinator.initializeNode();
            log.info("分布式定时任务服务初始化完成");
        } catch (Exception e) {
            log.error("分布式定时任务服务初始化失败", e);
        }
    }
}
```

### 2. JMX URI获取流程
```
需要JMX指标
    ↓
调用getJmxUriFromBroker()
    ↓
查询ke_broker_info表
    ↓
筛选在线broker
    ↓
获取host_ip:jmx_port
    ↓
返回JMX URI
```

### 3. 任务执行流程
```
定时执行任务
    ↓
从ke_task_scheduler读取任务
    ↓
执行任务逻辑
    ↓
需要JMX指标时
    ↓
从ke_broker_info获取broker信息
    ↓
构建JMX URI
    ↓
获取JMX指标
```

## 优势

### 1. 动态配置
- **无需重启**: broker信息变更时无需重启应用
- **实时更新**: 从数据库实时获取最新的broker信息
- **灵活管理**: 通过数据库管理broker配置

### 2. 高可用性
- **自动切换**: 优先使用在线broker
- **降级处理**: 在线broker不可用时使用其他broker
- **错误处理**: 完善的异常处理和日志记录

### 3. 简化配置
- **减少配置项**: 移除静态配置文件中的broker信息
- **统一管理**: 所有broker信息统一在数据库中管理
- **易于维护**: 通过Web界面管理broker配置

## 使用示例

### 1. 添加Broker信息
```sql
INSERT INTO ke_broker_info (
    broker_id, host_ip, port, jmx_port, status, 
    created_by
) VALUES (
    1, '192.168.1.100', 9092, 9988, 'online',
    'admin'
);
```

### 2. 更新Broker状态
```sql
UPDATE ke_broker_info 
SET status = 'online', 
    cpu_usage = 25.5, 
    memory_usage = 45.2,
    startup_time = NOW()
WHERE broker_id = 1;
```

### 3. 查询Broker信息
```sql
SELECT broker_id, host_ip, port, jmx_port, status 
FROM ke_broker_info 
WHERE status = 'online';
```

## 注意事项

1. **Broker配置**: 确保`ke_broker_info`表中有正确的broker信息
2. **JMX端口**: 确保broker配置了正确的JMX端口
3. **网络连通性**: 确保应用能够访问broker的JMX端口
4. **权限设置**: 确保JMX访问权限配置正确

## 故障排除

### 1. JMX连接失败
- 检查broker的JMX端口配置
- 确认网络连通性
- 检查JMX访问权限

### 2. 无法获取broker信息
- 检查`ke_broker_info`表是否有数据
- 确认broker状态是否正确
- 检查数据库连接

### 3. 任务执行失败
- 查看日志中的错误信息
- 确认broker信息是否正确
- 检查任务配置是否有效 