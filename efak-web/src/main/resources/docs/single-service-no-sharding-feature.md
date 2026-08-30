# 单服务节点跳过分片功能实现

## 功能概述

当Redis注册中心检测到在线服务数量只有1个时，定时调度任务不启动分片任务，直接处理所有任务项。这样可以避免单节点环境下的不必要分片开销，提高执行效率。

## 实现原理

### 修改位置

文件：`/Users/smartloli/workspace/EFAK-AI/efak-web/src/main/java/org/kafka/eagle/web/scheduler/DistributedTaskCoordinator.java`

### 修改内容

在三个分片方法中添加了单服务检测逻辑：

1. **shardBrokers()** - Broker分片方法
2. **shardTopics()** - 主题分片方法  
3. **shardConsumerGroups()** - 消费者组分片方法

### 核心逻辑

```java
// 当只有1个在线服务时，不启动分片任务，直接返回所有任务项
if (onlineServices.size() == 1) {
    log.info("检测到只有1个在线服务，跳过分片逻辑，当前节点处理所有任务项");
    return allItems; // 返回所有任务项而不是分片结果
}
```

## 功能特点

### 1. 自动检测
- 系统自动检测Redis注册中心的在线服务数量
- 无需手动配置，智能判断是否需要分片

### 2. 性能优化
- 单节点环境下避免不必要的分片计算
- 减少Redis查询和分片算法开销
- 提高任务执行效率

### 3. 日志记录
- 详细记录跳过分片的原因和处理的任务数量
- 便于监控和调试

### 4. 向后兼容
- 多节点环境下仍然正常进行分片
- 不影响现有的分布式任务调度逻辑

## 适用场景

### 1. 开发环境
- 单机开发测试环境
- 本地调试场景

### 2. 小规模部署
- 单节点生产环境
- 资源受限的部署场景

### 3. 故障恢复
- 集群中只剩一个节点时的故障恢复
- 临时单节点运行场景

## 验证方法

### 1. 检查在线服务数量

```bash
# 查看Redis中的在线服务
redis-cli hgetall "efak:services:registry"

# 查看心跳信息
redis-cli keys "efak:services:heartbeat:*"
```

### 2. 查看API接口

```bash
# 获取在线服务列表
curl http://localhost:8080/api/distributed-task/services/online

# 查看分片统计信息
curl http://localhost:8080/api/distributed-task/stats
```

### 3. 监控日志输出

查找以下关键日志信息：

```
检测到只有1个在线服务，跳过分片逻辑，当前节点处理所有broker: [1]
检测到只有1个在线服务，跳过分片逻辑，当前节点处理所有主题: 5 个
检测到只有1个在线服务，跳过分片逻辑，当前节点处理所有消费者组: 3 个
```

### 4. 数据库验证

```sql
-- 检查任务执行历史
SELECT task_type, start_time, end_time, result_message 
FROM ke_task_execution_history 
WHERE task_type IN ('cluster_monitor', 'topic_monitor', 'consumer_monitor')
ORDER BY start_time DESC 
LIMIT 10;

-- 检查broker信息更新
SELECT broker_id, cpu_usage, memory_usage, updated_at,
       TIMESTAMPDIFF(MINUTE, updated_at, NOW()) as minutes_ago 
FROM ke_broker_info 
ORDER BY updated_at DESC;
```

## 测试步骤

### 1. 准备单节点环境

1. 确保只有一个EFAK服务实例在运行
2. 检查Redis中只有一个服务注册记录
3. 确认心跳正常

### 2. 触发任务执行

1. 等待定时任务自动触发
2. 或手动触发任务执行
3. 观察日志输出

### 3. 验证结果

1. 检查日志中是否出现"跳过分片逻辑"的信息
2. 确认所有任务项都被当前节点处理
3. 验证数据库中的数据是否正常更新

### 4. 多节点测试

1. 启动第二个服务实例
2. 确认系统恢复正常分片模式
3. 验证分片逻辑正常工作

## 注意事项

### 1. 性能考虑
- 单节点处理所有任务可能增加负载
- 需要确保单节点有足够的资源处理全部任务

### 2. 监控建议
- 监控单节点的CPU和内存使用情况
- 关注任务执行时间是否在合理范围内

### 3. 扩展性
- 当业务量增长时，及时考虑扩展为多节点
- 监控任务执行效率，适时调整部署策略

## 配置参数

当前实现不需要额外配置参数，系统会自动检测并应用此功能。如果需要强制启用或禁用此功能，可以考虑在`DistributedTaskConfig`中添加相关配置项：

```yaml
efak:
  distributed:
    task:
      # 是否在单节点时跳过分片（默认true）
      skip-sharding-on-single-node: true
```

## 版本信息

- **实现版本**: v1.4.0
- **实现日期**: 2025-08-02
- **修改文件**: DistributedTaskCoordinator.java
- **影响范围**: 分布式任务调度模块

## 相关文档

- [分布式任务调度文档](distributed-task-README.md)
- [集群监控问题分析](cluster-monitoring-issue-analysis.md)
- [分布式调度器更新](distributed-scheduler-update.md)