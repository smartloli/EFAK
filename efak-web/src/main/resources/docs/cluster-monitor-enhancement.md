# 集群健康检查任务增强功能

## 功能概述

在分布式定时任务执行"集群健康检查任务"类型时，系统现在能够获取broker节点相关信息（brokerId、host、port、内存使用率、CPU使用率、启动时间、版本号）并更新到已有的`ke_broker_info`表中，同时确保host、port、jmx_port数据不被覆盖。

## 主要修改

### 1. BrokerMapper增强

在`BrokerMapper.java`中添加了新的更新方法：

```java
/**
 * 更新Broker动态信息（不更新host、port、jmx_port字段）
 */
@Update("UPDATE ke_broker_info SET " +
        "  status = #{status}, " +
        "  cpu_usage = #{cpuUsage}, " +
        "  memory_usage = #{memoryUsage}, " +
        "  startup_time = #{startupTime}, " +
        "  version = #{version}, " +
        "  updated_at = NOW() " +
        "WHERE broker_id = #{brokerId}")
int updateBrokerDynamicInfo(@Param("brokerId") Integer brokerId,
        @Param("status") String status,
        @Param("cpuUsage") java.math.BigDecimal cpuUsage,
        @Param("memoryUsage") java.math.BigDecimal memoryUsage,
        @Param("startupTime") java.time.LocalDateTime startupTime,
        @Param("version") String version);
```

### 2. TaskExecutorManager优化

在`TaskExecutorManager.java`中修改了集群监控任务的实现：

#### 2.1 从数据库获取broker配置信息

```java
// 从数据库获取broker信息，用于构建host、port、jmx_port映射
List<BrokerInfo> dbBrokers = brokerMapper.queryAllBrokers();
Map<Integer, String> brokerHosts = new HashMap<>();
Map<Integer, Integer> brokerPorts = new HashMap<>();
Map<Integer, Integer> brokerJmxPorts = new HashMap<>();

for (BrokerInfo dbBroker : dbBrokers) {
    brokerHosts.put(dbBroker.getBrokerId(), dbBroker.getHostIp());
    brokerPorts.put(dbBroker.getBrokerId(), dbBroker.getPort());
    if (dbBroker.getJmxPort() != null) {
        brokerJmxPorts.put(dbBroker.getBrokerId(), dbBroker.getJmxPort());
    }
}

log.info("从数据库获取到 {} 个broker配置信息", dbBrokers.size());
```

#### 2.2 更新broker信息时保留关键字段

```java
/**
 * 更新broker信息到数据库
 * 注意：host、port、jmx_port字段不会被覆盖，保持原有值
 */
private void updateBrokerInfoInDatabase(BrokerDetailedInfo broker) {
    try {
        // 检查broker是否已存在
        BrokerInfo existingBroker = brokerMapper.getBrokerByBrokerId(broker.getBrokerId());

        if (existingBroker != null) {
            // 更新现有broker的动态信息，不更新host、port、jmx_port字段
            brokerMapper.updateBrokerDynamicInfo(
                broker.getBrokerId(),
                broker.getStatus().toLowerCase(),
                java.math.BigDecimal.valueOf(broker.getCpuUsagePercent()),
                java.math.BigDecimal.valueOf(broker.getMemoryUsagePercent()),
                broker.getStartTime(),
                broker.getVersion());
            log.debug("更新broker {} 动态信息到数据库（保留host、port、jmx_port原有值）", broker.getBrokerId());
        } else {
            // 创建新的broker信息
            BrokerInfo newBroker = new BrokerInfo();
            newBroker.setBrokerId(broker.getBrokerId());
            newBroker.setHostIp(broker.getHost());
            newBroker.setPort(broker.getPort());
            // 注意：新创建的broker不设置jmx_port，需要用户手动配置
            newBroker.setStatus(broker.getStatus().toLowerCase());
            newBroker.setCpuUsage(java.math.BigDecimal.valueOf(broker.getCpuUsagePercent()));
            newBroker.setMemoryUsage(java.math.BigDecimal.valueOf(broker.getMemoryUsagePercent()));
            newBroker.setStartupTime(broker.getStartTime());
            newBroker.setVersion(broker.getVersion());
            newBroker.setCreatedBy("system");

            brokerMapper.createBroker(newBroker);
            log.debug("创建broker {} 信息到数据库", broker.getBrokerId());
        }
    } catch (Exception e) {
        log.error("更新broker {} 信息到数据库失败: {}", broker.getBrokerId(), e.getMessage(), e);
        throw e;
    }
}
```

## 功能特点

### 1. 数据保护
- **保留关键字段**: host、port、jmx_port字段不会被覆盖
- **只更新动态信息**: 只更新状态、CPU、内存、启动时间、版本号等动态信息
- **配置分离**: 静态配置（host、port、jmx_port）与动态监控数据分离

### 2. 智能更新
- **增量更新**: 只更新发生变化的字段
- **错误处理**: 完善的异常处理和日志记录
- **事务安全**: 使用数据库事务确保数据一致性

### 3. 监控指标
- **CPU使用率**: 实时监控broker的CPU使用情况
- **内存使用率**: 监控broker的内存使用情况
- **启动时间**: 记录broker的启动时间
- **版本信息**: 获取broker的Kafka版本号
- **在线状态**: 监控broker的在线/离线状态

## 工作流程

### 1. 任务执行流程
```
集群健康检查任务启动
    ↓
从数据库获取broker配置信息
    ↓
构建host、port、jmx_port映射
    ↓
调用KafkaServiceProxy获取broker详细信息
    ↓
遍历每个broker
    ↓
检查broker是否已存在于数据库
    ↓
如果存在：更新动态信息（保留host、port、jmx_port）
    ↓
如果不存在：创建新的broker记录
    ↓
记录执行结果和统计信息
```

### 2. 数据更新流程
```
获取broker详细信息
    ↓
检查数据库中的现有记录
    ↓
如果记录存在
    ↓
使用updateBrokerDynamicInfo更新
    ↓
只更新：status、cpu_usage、memory_usage、startup_time、version
    ↓
保留：host_ip、port、jmx_port
    ↓
如果记录不存在
    ↓
创建新记录（包含host、port，但不设置jmx_port）
```

## 数据库表结构

### ke_broker_info表字段说明

| 字段名 | 类型 | 说明 | 更新策略 |
|--------|------|------|----------|
| broker_id | int | Broker ID | 不更新 |
| host_ip | varchar | 主机IP | 不更新 |
| port | int | 端口 | 不更新 |
| jmx_port | int | JMX端口 | 不更新 |
| status | varchar | 状态 | 更新 |
| cpu_usage | decimal | CPU使用率 | 更新 |
| memory_usage | decimal | 内存使用率 | 更新 |
| startup_time | timestamp | 启动时间 | 更新 |
| version | varchar | 版本号 | 更新 |

## 配置要求

### 1. 数据库配置
确保`ke_broker_info`表存在并包含必要的字段。

### 2. Broker配置
- 在`ke_broker_info`表中配置broker的基本信息（host、port、jmx_port）
- 确保JMX端口配置正确，以便获取性能指标

### 3. 任务调度配置
确保集群健康检查任务已启用并配置了正确的执行频率。

## 监控和日志

### 1. 关键日志
- `从数据库获取到 {} 个broker配置信息`
- `更新broker {} 动态信息到数据库（保留host、port、jmx_port原有值）`
- `创建broker {} 信息到数据库`
- `集群监控任务执行完成: {}个Broker, {}个在线, 版本: {}, 最早启动时间: {}, 更新了{}个broker信息`

### 2. 监控指标
- 总broker数量
- 在线broker数量
- 离线broker数量
- 更新的broker数量
- 创建的broker数量
- 集群版本信息
- 最早启动时间

## 使用示例

### 1. 手动触发集群健康检查
```java
@Autowired
private TaskExecutorManager taskExecutorManager;

// 创建集群监控任务
TaskScheduler task = new TaskScheduler();
task.setTaskType("cluster_monitor");
task.setTaskName("集群健康检查任务");

// 执行任务
TaskExecutionResult result = taskExecutorManager.executeTask(task);
```

### 2. 查看broker信息
```sql
-- 查看所有broker信息
SELECT broker_id, host_ip, port, jmx_port, status, cpu_usage, memory_usage, version 
FROM ke_broker_info;

-- 查看在线broker
SELECT broker_id, host_ip, port, status, cpu_usage, memory_usage 
FROM ke_broker_info 
WHERE status = 'online';
```

## 注意事项

### 1. 数据保护
- host、port、jmx_port字段不会被覆盖
- 新创建的broker不会自动设置jmx_port，需要手动配置
- 确保数据库中的broker配置信息正确

### 2. 性能考虑
- 任务执行频率不宜过高，建议每10-30分钟执行一次
- 大量broker时，考虑分批处理
- 监控数据库连接池使用情况

### 3. 错误处理
- 单个broker更新失败不会影响其他broker
- 详细的错误日志记录便于问题排查
- 支持重试机制

## 故障排除

### 1. 无法获取broker信息
- 检查Kafka集群连接状态
- 确认broker配置信息正确
- 查看应用日志中的错误信息

### 2. JMX指标获取失败
- 检查broker的JMX端口配置
- 确认网络连通性
- 检查JMX访问权限

### 3. 数据库更新失败
- 检查数据库连接状态
- 确认表结构正确
- 查看SQL执行日志

## 总结

通过这次增强，集群健康检查任务现在能够：

1. **智能更新**: 只更新动态监控数据，保留静态配置信息
2. **数据保护**: 确保host、port、jmx_port等关键字段不被覆盖
3. **实时监控**: 获取broker的实时性能指标和状态信息
4. **完善日志**: 提供详细的操作日志和错误信息
5. **高可用性**: 支持异常处理和错误恢复

这确保了broker配置信息的稳定性，同时提供了实时的监控数据更新功能。 