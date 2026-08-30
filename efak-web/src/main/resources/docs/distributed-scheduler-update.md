# DistributedSchedulerService 更新功能

## 功能概述

`DistributedSchedulerService`已更新为从`ke_task_scheduler`表中读取任务类型，并自动清理无效的任务类型。现在它支持多种任务类型的分布式执行，而不仅仅是Topic容量统计。

## 主要更新内容

### 1. 数据库驱动的任务管理
- **从数据库读取任务**: 自动从`ke_task_scheduler`表读取启用的任务
- **任务类型过滤**: 只执行支持的任务类型
- **无效任务清理**: 自动禁用不支持的任务类型

### 2. 支持的任务类型
```java
private static final List<String> SUPPORTED_TASK_TYPES = List.of(
    "topic_monitor",      // Topic监控任务
    "consumer_monitor",   // 消费者监控任务
    "cluster_monitor",    // 集群监控任务
    "alert_cleanup",      // 告警清理任务
    "data_cleanup",       // 数据清理任务
    "performance_stats"   // 性能统计任务
);
```

### 3. 任务执行流程
```
系统启动
    ↓
清理无效任务类型
    ↓
初始化分布式协调器
    ↓
定时扫描数据库中的启用任务
    ↓
根据任务类型执行相应逻辑
    ↓
更新任务执行结果
```

## 核心方法

### 1. 清理无效任务类型
```java
private void cleanupInvalidTaskTypes() {
    // 获取所有任务
    List<TaskScheduler> allTasks = taskSchedulerMapper.selectAllTasks();
    
    // 检查每个任务类型是否有效
    for (TaskScheduler task : allTasks) {
        if (!SUPPORTED_TASK_TYPES.contains(task.getTaskType())) {
            // 将无效任务状态设置为disabled
            taskSchedulerMapper.updateTaskStatus(task.getId(), "disabled", updateTime);
        }
    }
}
```

### 2. 从数据库读取启用任务
```java
private List<TaskScheduler> getEnabledTasksFromDatabase() {
    // 获取所有启用的任务
    List<TaskScheduler> enabledTasks = taskSchedulerMapper.selectEnabledTasks();
    
    // 过滤出支持的任务类型
    List<TaskScheduler> supportedTasks = new ArrayList<>();
    for (TaskScheduler task : enabledTasks) {
        if (SUPPORTED_TASK_TYPES.contains(task.getTaskType())) {
            supportedTasks.add(task);
        }
    }
    
    return supportedTasks;
}
```

### 3. 执行单个任务
```java
private void executeTask(TaskScheduler task) {
    // 根据任务类型执行不同的逻辑
    switch (task.getTaskType()) {
        case "topic_monitor":
            executeTopicMonitorTask(task);
            break;
        case "consumer_monitor":
            executeConsumerMonitorTask(task);
            break;
        case "cluster_monitor":
            executeClusterMonitorTask(task);
            break;
        case "alert_cleanup":
            executeAlertCleanupTask(task);
            break;
        case "data_cleanup":
            executeDataCleanupTask(task);
            break;
        case "performance_stats":
            executePerformanceStatsTask(task);
            break;
        default:
            log.warn("不支持的任务类型: {}", task.getTaskType());
            break;
    }
    
    // 更新任务执行结果
    updateTaskExecutionResult(task, true, "执行成功", null);
}
```

## 任务类型实现

### 1. Topic监控任务
```java
private void executeTopicMonitorTask(TaskScheduler task) {
    // 从efak-core中获取所有Topic列表
    List<String> allTopics = getAllTopicsFromCore();
    
    // 分配Topic给当前节点
    List<String> assignedTopics = taskCoordinator.assignTopicsToCurrentNode(allTopics);
    
    // 异步执行统计任务
    CompletableFuture<List<TopicCapacityStats>> future = calculateTopicCapacityStatsAsync(assignedTopics);
    
    // 等待任务完成并处理结果
    List<TopicCapacityStats> statsList = future.get();
    
    // 保存统计结果到Redis
    saveStatsToRedis(statsList);
}
```

### 2. 其他任务类型
- **消费者监控任务**: 监控消费者组状态、消费延迟等
- **集群监控任务**: 监控Kafka集群健康状态、Broker状态等
- **告警清理任务**: 清理过期的告警记录
- **数据清理任务**: 清理过期的监控数据
- **性能统计任务**: 收集和统计系统性能指标

## 数据库表结构

### ke_task_scheduler表
```sql
CREATE TABLE `ke_task_scheduler` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_name` varchar(100) NOT NULL,
  `task_type` varchar(50) NOT NULL,
  `cron_expression` varchar(100) NOT NULL,
  `description` text,
  `status` varchar(20) NOT NULL DEFAULT 'enabled',
  `last_execute_time` varchar(30) DEFAULT NULL,
  `next_execute_time` varchar(30) DEFAULT NULL,
  `execute_count` int(11) NOT NULL DEFAULT '0',
  `success_count` int(11) NOT NULL DEFAULT '0',
  `fail_count` int(11) NOT NULL DEFAULT '0',
  `last_execute_result` text,
  `error_message` text,
  `created_by` varchar(50) NOT NULL,
  `create_time` datetime NOT NULL,
  `updated_by` varchar(50) DEFAULT NULL,
  `update_time` datetime NOT NULL,
  `config` text,
  `timeout` int(11) DEFAULT '300',
  `node_id` varchar(100) DEFAULT NULL,
  `cluster_name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
);
```

## 配置说明

### 1. 调度器配置
```properties
# 分布式调度器启用状态
efak.scheduler.distributed.enabled=true

# 执行间隔（秒）
efak.scheduler.distributed.interval=60

# Topic容量统计间隔（毫秒）
efak.scheduler.topic-capacity.interval=60000
```

### 2. Kafka配置
```properties
# Kafka服务器地址
kafka.bootstrap.servers=localhost:9092

# JMX URI
efak.jmx.uri=127.0.0.1:9988
```

## 监控和日志

### 1. 关键日志
- 任务类型清理: `任务类型清理完成: 总任务数={}, 无效任务数={}, 已清理任务数={}`
- 任务执行: `开始执行任务: ID={}, 名称={}, 类型={}, Cron表达式={}`
- 任务结果更新: `任务执行结果更新成功: 任务ID={}, 成功={}`

### 2. 监控指标
- 总任务数
- 有效任务数
- 无效任务数
- 任务执行成功率
- 各类型任务执行次数

## 使用示例

### 1. 添加新任务
```sql
INSERT INTO ke_task_scheduler (
    task_name, task_type, cron_expression, description, status, 
    created_by, create_time, update_time
) VALUES (
    'Topic监控任务', 'topic_monitor', '0 */5 * * * ?', 
    '监控Kafka主题状态，检查分区数量、副本数量等指标', 'enabled',
    'admin', NOW(), NOW()
);
```

### 2. 手动触发任务执行
```java
@Autowired
private DistributedSchedulerService schedulerService;

// 手动触发分布式任务调度
schedulerService.triggerTaskExecution();
```

### 3. 更新任务执行频率
```java
// 更新执行频率为30秒
schedulerService.updateExecutionInterval(30);
```

## 注意事项

1. **任务类型验证**: 系统会自动验证任务类型是否在支持列表中
2. **无效任务处理**: 不支持的任务类型会被自动设置为disabled状态
3. **分布式协调**: 通过Redis实现多节点间的任务协调
4. **错误处理**: 每个任务的执行都有完善的错误处理和日志记录
5. **性能考虑**: 异步执行避免阻塞主线程

## 故障排除

### 1. 任务不执行
- 检查任务状态是否为'enabled'
- 确认任务类型是否在支持列表中
- 查看日志中的任务执行情况

### 2. 无效任务类型
- 查看日志中的任务类型清理信息
- 确认任务类型拼写是否正确
- 检查是否在SUPPORTED_TASK_TYPES列表中

### 3. 分布式协调问题
- 检查Redis连接是否正常
- 确认分布式锁是否正常工作
- 查看节点注册和心跳状态 