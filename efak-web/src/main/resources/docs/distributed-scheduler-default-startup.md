# 分布式调度器默认启动配置

## 概述

分布式调度器现在默认启动，无需在配置文件中进行任何配置。系统完全依赖`ke_broker_info`表来获取broker信息，实现了真正的零配置启动。

## 主要变化

### 1. 配置文件清理

#### 移除的配置项
```properties
# 已移除的配置项
kafka.bootstrap.servers=localhost:9092
efak.jmx.uri=127.0.0.1:9988
efak.scheduler.distributed.enabled=true
efak.scheduler.distributed.interval=60
```

#### 当前配置
```properties
# 分布式调度器默认启动，无需配置文件
# 所有broker信息从ke_broker_info表动态获取
```

### 2. 代码逻辑更新

#### DistributedTaskSchedulerInitializer.java 更新
- **移除**: `DEFAULT_BOOTSTRAP_SERVERS`常量
- **移除**: `DEFAULT_JMX_URI`常量
- **更新**: `buildBootstrapServers()`方法，完全从数据库获取broker信息
- **增强**: 当没有broker信息时，优雅地跳过初始化而不是使用默认配置

```java
/**
 * 构建bootstrap servers字符串
 */
private String buildBootstrapServers(List<BrokerInfo> brokers) {
    if (brokers.isEmpty()) {
        log.warn("没有可用的broker信息，无法构建bootstrap servers");
        return null;
    }

    StringBuilder bootstrapServers = new StringBuilder();
    for (int i = 0; i < brokers.size(); i++) {
        BrokerInfo broker = brokers.get(i);
        if (i > 0) {
            bootstrapServers.append(",");
        }
        bootstrapServers.append(broker.getHostIp()).append(":").append(broker.getPort());
    }

    return bootstrapServers.toString();
}
```

#### 启动流程优化
```java
@Override
public void run(String... args) throws Exception {
    log.info("=== 开始初始化分布式任务调度 ===");

    try {
        // 1. 检查broker信息
        List<BrokerInfo> brokers = checkBrokerInfo();
        if (brokers.isEmpty()) {
            log.warn("表ke_broker_info记录为空，分布式调度任务停止执行");
            return;
        }

        // 2. 检查Kafka安全配置
        boolean securityEnabled = checkKafkaSecurityConfig();
        log.info("Kafka安全认证状态: {}", securityEnabled ? "已启用" : "未启用");

        // 3. 构建bootstrap servers字符串
        String bootstrapServers = buildBootstrapServers(brokers);
        if (bootstrapServers == null || bootstrapServers.trim().isEmpty()) {
            log.warn("无法构建bootstrap servers，跳过KafkaServiceProxy初始化");
            return;
        }
        log.info("构建的bootstrap servers: {}", bootstrapServers);

        // 4. 初始化KafkaServiceProxy
        initializeKafkaServiceProxy(bootstrapServers, securityEnabled);

        // 5. 初始化JmxServiceProxy
        initializeJmxServiceProxy();

        // 6. 启动分布式任务调度
        startDistributedTaskScheduler();

        log.info("=== 分布式任务调度初始化完成 ===");

    } catch (Exception e) {
        log.error("分布式任务调度初始化失败", e);
        throw e;
    }
}
```

## 优势

### 1. 零配置启动
- **无需配置**: 系统启动时无需任何配置文件设置
- **自动检测**: 自动从数据库检测broker信息
- **智能降级**: 当没有broker信息时，优雅地跳过初始化

### 2. 完全动态化
- **数据库驱动**: 所有配置信息从数据库动态获取
- **实时更新**: broker信息变更时无需重启应用
- **灵活管理**: 通过Web界面管理所有配置

### 3. 高可用性
- **自动切换**: 优先使用在线broker
- **错误处理**: 完善的异常处理和日志记录
- **优雅降级**: 当broker不可用时，系统仍能正常启动

## 工作流程

### 系统启动流程
```
应用启动
    ↓
DistributedTaskSchedulerInitializer.run()
    ↓
checkBrokerInfo() - 查询ke_broker_info表
    ↓
checkKafkaSecurityConfig() - 查询安全配置
    ↓
buildBootstrapServers() - 构建broker列表
    ↓
initializeKafkaServiceProxy() - 初始化Kafka服务
    ↓
initializeJmxServiceProxy() - 初始化JMX服务
    ↓
startDistributedTaskScheduler() - 启动调度器
```

### 无broker信息时的处理
```
应用启动
    ↓
checkBrokerInfo() - 查询ke_broker_info表
    ↓
表为空或查询失败
    ↓
记录警告日志
    ↓
优雅退出，不影响应用启动
```

## 配置对比

### 清理前
```properties
# Kafka默认配置
kafka.bootstrap.servers=localhost:9092
efak.jmx.uri=127.0.0.1:9988

# 分布式调度器配置
efak.scheduler.distributed.enabled=true
efak.scheduler.distributed.interval=60
```

### 清理后
```properties
# 分布式调度器默认启动，无需配置文件
# 所有broker信息从ke_broker_info表动态获取
```

## 数据库依赖

### ke_broker_info表结构
```sql
CREATE TABLE ke_broker_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    broker_id INT NOT NULL,
    host_ip VARCHAR(255) NOT NULL,
    port INT NOT NULL,
    jmx_port INT,
    status VARCHAR(50) DEFAULT 'online',
    created_by VARCHAR(255),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 必需的broker信息
- `broker_id`: Broker ID
- `host_ip`: 主机IP地址
- `port`: Kafka端口
- `jmx_port`: JMX端口（可选）
- `status`: 状态（online/offline）

## 注意事项

### 1. 数据库准备
- 确保`ke_broker_info`表存在
- 确保表中有正确的broker信息
- 确保broker状态正确设置

### 2. 网络连通性
- 确保应用能够访问broker的Kafka端口
- 确保应用能够访问broker的JMX端口
- 检查防火墙设置

### 3. 权限设置
- 确保JMX访问权限配置正确
- 确保Kafka访问权限配置正确

## 故障排除

### 1. 应用启动失败
```bash
# 检查应用日志
tail -f logs/efak-ai.log

# 检查数据库连接
mysql -u root -p efak_ai -e "SELECT * FROM ke_broker_info;"
```

### 2. 无法获取broker信息
```sql
-- 检查broker信息
SELECT broker_id, host_ip, port, jmx_port, status 
FROM ke_broker_info 
WHERE status = 'online';
```

### 3. Kafka连接失败
- 检查broker的Kafka端口是否开放
- 确认网络连通性
- 检查Kafka服务状态

### 4. JMX连接失败
- 检查broker的JMX端口是否开放
- 确认JMX访问权限
- 检查JMX配置

## 迁移指南

### 从配置驱动迁移到数据库驱动

#### 1. 准备broker信息
```sql
-- 添加broker信息到数据库
INSERT INTO ke_broker_info (
    broker_id, host_ip, port, jmx_port, status, created_by
) VALUES 
(1, '192.168.1.100', 9092, 9988, 'online', 'admin'),
(2, '192.168.1.101', 9092, 9989, 'online', 'admin');
```

#### 2. 清理配置文件
```properties
# 移除所有静态配置
# kafka.bootstrap.servers=localhost:9092
# efak.jmx.uri=127.0.0.1:9988
# efak.scheduler.distributed.enabled=true
# efak.scheduler.distributed.interval=60

# 分布式调度器默认启动，无需配置
```

#### 3. 重启应用
```bash
# 重启应用以应用新配置
./mvnw spring-boot:run
```

#### 4. 验证配置
- 检查应用日志确认broker信息正确加载
- 验证Kafka连接正常
- 验证JMX指标获取正常
- 验证分布式调度器正常启动

## 最佳实践

### 1. Broker管理
- 定期检查broker状态
- 及时更新离线broker状态
- 确保JMX端口配置正确

### 2. 监控告警
- 监控broker连接状态
- 监控JMX指标获取状态
- 设置适当的告警阈值

### 3. 日志管理
- 定期检查应用日志
- 关注broker连接相关的警告和错误
- 及时处理异常情况 