# Application.properties 配置清理

## 清理概述

已成功清理`application.properties`中的`kafka.bootstrap.servers`和`efak.jmx.uri`属性，以及相关的代码逻辑。现在系统完全从`ke_broker_info`表中动态获取broker信息。

## 清理内容

### 1. Application.properties 清理

#### 移除的配置项
```properties
# 已移除的配置项
kafka.bootstrap.servers=localhost:9092
efak.jmx.uri=127.0.0.1:9988
```

#### 配置项清理
```properties
# 已移除所有分布式调度器相关配置项
# 系统默认启动分布式调度器，无需配置文件
```

### 2. 代码逻辑清理

#### KafkaServiceConfig.java 更新
- **移除**: `@Value("${kafka.bootstrap.servers:localhost:9092}")`配置注入
- **新增**: `BrokerMapper`依赖注入
- **新增**: `getBootstrapServersFromBroker()`方法，从数据库动态获取broker信息
- **更新**: 初始化逻辑，从broker信息构建bootstrap servers字符串

```java
@Autowired
private BrokerMapper brokerMapper;

private String getBootstrapServersFromBroker() {
    try {
        List<BrokerInfo> brokers = brokerMapper.queryAllBrokers();
        if (brokers.isEmpty()) {
            log.warn("ke_broker_info表中没有broker信息");
            return null;
        }

        // 构建bootstrap servers字符串
        StringBuilder bootstrapServers = new StringBuilder();
        for (BrokerInfo broker : brokers) {
            if (bootstrapServers.length() > 0) {
                bootstrapServers.append(",");
            }
            bootstrapServers.append(broker.getHostIp()).append(":").append(broker.getPort());
        }

        String result = bootstrapServers.toString();
        log.info("从broker信息构建bootstrap servers: {}", result);
        return result;
    } catch (Exception e) {
        log.error("从broker信息获取bootstrap servers失败", e);
        return null;
    }
}
```

#### DistributedTaskSchedulerInitializer.java 更新
- **移除**: `DEFAULT_JMX_URI`常量
- **移除**: `DEFAULT_BOOTSTRAP_SERVERS`常量
- **更新**: `buildBootstrapServers()`方法，完全从数据库获取broker信息
- **优化**: `initializeJmxServiceProxy()`方法，不再使用硬编码的JMX URI
- **增强**: 当没有broker信息时，优雅地跳过初始化而不是使用默认配置

### 3. 工作流程更新

#### 系统启动流程
```
应用启动
    ↓
KafkaServiceConfig.run()
    ↓
getBootstrapServersFromBroker()
    ↓
查询ke_broker_info表
    ↓
构建bootstrap servers字符串
    ↓
初始化KafkaServiceProxy
```

#### JMX URI获取流程
```
需要JMX指标
    ↓
DistributedSchedulerService.getJmxUriFromBroker()
    ↓
查询ke_broker_info表
    ↓
筛选在线broker
    ↓
获取host_ip:jmx_port
    ↓
返回JMX URI
```

## 优势

### 1. 动态配置
- **无需重启**: broker信息变更时无需重启应用
- **实时更新**: 从数据库实时获取最新的broker信息
- **灵活管理**: 通过数据库管理broker配置

### 2. 简化配置
- **减少配置项**: 移除静态配置文件中的broker信息
- **统一管理**: 所有broker信息统一在数据库中管理
- **易于维护**: 通过Web界面管理broker配置

### 3. 高可用性
- **自动切换**: 优先使用在线broker
- **降级处理**: 在线broker不可用时使用其他broker
- **错误处理**: 完善的异常处理和日志记录

## 配置对比

### 清理前
```properties
# Kafka默认配置
kafka.bootstrap.servers=localhost:9092
efak.jmx.uri=127.0.0.1:9988
```

### 清理后
```properties
# 分布式调度器默认启动，无需配置文件
# 所有broker信息从ke_broker_info表动态获取
```

## 数据库依赖

### ke_broker_info表
系统现在完全依赖`ke_broker_info`表来获取broker信息：

```sql
-- 必需的broker信息字段
broker_id: Broker ID
host_ip: 主机IP地址
port: Kafka端口
jmx_port: JMX端口
status: 状态(online/offline)
```

### 示例数据
```sql
INSERT INTO ke_broker_info (
    broker_id, host_ip, port, jmx_port, status, created_by
) VALUES 
(1, '192.168.1.100', 9092, 9988, 'online', 'admin'),
(2, '192.168.1.101', 9092, 9989, 'online', 'admin');
```

## 注意事项

### 1. 数据库依赖
- 确保`ke_broker_info`表中有正确的broker信息
- 确保broker状态正确设置
- 确保JMX端口配置正确

### 2. 网络连通性
- 确保应用能够访问broker的Kafka端口
- 确保应用能够访问broker的JMX端口
- 检查防火墙设置

### 3. 权限设置
- 确保JMX访问权限配置正确
- 确保Kafka访问权限配置正确

## 故障排除

### 1. 无法获取broker信息
```bash
# 检查数据库连接
mysql -u root -p efak_ai -e "SELECT * FROM ke_broker_info;"

# 检查broker状态
mysql -u root -p efak_ai -e "SELECT broker_id, host_ip, port, jmx_port, status FROM ke_broker_info WHERE status = 'online';"
```

### 2. Kafka连接失败
- 检查broker的Kafka端口是否开放
- 确认网络连通性
- 检查Kafka服务状态

### 3. JMX连接失败
- 检查broker的JMX端口是否开放
- 确认JMX访问权限
- 检查JMX配置

### 4. 应用启动失败
- 查看应用日志中的错误信息
- 确认数据库连接正常
- 检查broker信息是否正确

## 迁移指南

### 从静态配置迁移到动态配置

#### 1. 准备broker信息
```sql
-- 添加broker信息到数据库
INSERT INTO ke_broker_info (
    broker_id, host_ip, port, jmx_port, status, created_by
) VALUES (
    1, 'your-kafka-host', 9092, 9988, 'online', 'admin'
);
```

#### 2. 更新配置文件
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