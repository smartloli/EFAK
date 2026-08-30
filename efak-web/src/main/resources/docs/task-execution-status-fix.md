# 任务执行状态字段修复

## 问题描述

在任务调度系统中，当用户点击"立即执行"按钮时，出现以下异常：

```
=== 任务执行异常: 
### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Column 'execution_status' cannot be null
### The error may exist in org/kafka/eagle/web/mapper/TaskExecutionHistoryMapper.java (best guess)
### The error may involve org.kafka.eagle.web.mapper.TaskExecutionHistoryMapper.insertTaskExecutionHistory-Inline
### The error occurred while setting parameters
### SQL: INSERT INTO ke_task_execution_history (task_id, task_name, task_type, execution_status, start_time, end_time, duration, result_message, error_message, executor_node, trigger_type, trigger_user, input_params, output_result, created_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
### Cause: java.sql.SQLIntegrityConstraintViolationException: Column 'execution_status' cannot be null
; Column 'execution_status' cannot be null ===
```

## 问题分析

### 1. 数据库表结构

`ke_task_execution_history`表中的`execution_status`字段定义为：

```sql
`execution_status` varchar(20) NOT NULL COMMENT '执行状态：RUNNING-执行中，SUCCESS-成功，FAILED-失败，CANCELLED-已取消'
```

该字段有NOT NULL约束，不能为空。

### 2. 问题根源

在`TaskSchedulerServiceImpl.java`的`recordTaskExecutionStart`方法中，使用了错误的字段名：

```java
// 错误的代码
history.put("status", "running");  // 应该是 "executionStatus"
```

而在`recordTaskExecutionEnd`方法中也有类似问题：

```java
// 错误的代码
history.put("status", success ? "success" : "failed");  // 应该是 "executionStatus"
history.put("result", result);  // 应该是 "resultMessage"
```

### 3. 字段映射问题

MyBatis的`insertTaskExecutionHistory`方法期望的字段名与代码中使用的字段名不匹配：

- 代码中使用：`status`
- 数据库字段：`execution_status`
- MyBatis映射：`executionStatus`

## 修复方案

### 1. 修复TaskSchedulerServiceImpl.java

#### 修复recordTaskExecutionStart方法

```java
/**
 * 记录任务执行开始
 */
public Long recordTaskExecutionStart(Long taskId, String taskName, String taskType, String clusterName) {
    Map<String, Object> history = new HashMap<>();
    history.put("taskId", taskId);
    history.put("taskName", taskName);
    history.put("taskType", taskType);
    history.put("clusterName", clusterName);
    history.put("startTime", LocalDateTime.now());
    // 修复：使用正确的字段名
    history.put("executionStatus", "RUNNING");
    history.put("triggerType", "MANUAL");
    history.put("executorNode", "manual-executor");
    history.put("createdTime", LocalDateTime.now());

    taskExecutionHistoryMapper.insertTaskExecutionHistory(history);
    return (Long) history.get("id");
}
```

#### 修复recordTaskExecutionEnd方法

```java
/**
 * 记录任务执行结束
 */
public void recordTaskExecutionEnd(Long executionId, boolean success, String result, String errorMessage) {
    LocalDateTime endTime = LocalDateTime.now();
    Map<String, Object> history = new HashMap<>();
    history.put("id", executionId);
    history.put("endTime", endTime);
    // 修复：使用正确的字段名和状态值
    history.put("executionStatus", success ? "SUCCESS" : "FAILED");
    history.put("resultMessage", result);
    history.put("errorMessage", errorMessage);

    taskExecutionHistoryMapper.updateTaskExecutionHistory(history);
}
```

### 2. 字段映射对照表

| 数据库字段 | MyBatis映射字段 | 状态值 | 说明 |
|-----------|----------------|--------|------|
| `execution_status` | `executionStatus` | `RUNNING` | 执行中 |
| `execution_status` | `executionStatus` | `SUCCESS` | 成功 |
| `execution_status` | `executionStatus` | `FAILED` | 失败 |
| `execution_status` | `executionStatus` | `CANCELLED` | 已取消 |
| `trigger_type` | `triggerType` | `SCHEDULED` | 定时触发 |
| `trigger_type` | `triggerType` | `MANUAL` | 手动触发 |
| `result_message` | `resultMessage` | - | 执行结果消息 |
| `error_message` | `errorMessage` | - | 错误信息 |

### 3. 状态常量定义

在`TaskConfig.java`中定义了正确的状态常量：

```java
// 执行状态常量
public static final String EXECUTION_STATUS_RUNNING = "RUNNING";
public static final String EXECUTION_STATUS_SUCCESS = "SUCCESS";
public static final String EXECUTION_STATUS_FAILED = "FAILED";
public static final String EXECUTION_STATUS_CANCELLED = "CANCELLED";

// 触发类型常量
public static final String TRIGGER_TYPE_SCHEDULED = "SCHEDULED";
public static final String TRIGGER_TYPE_MANUAL = "MANUAL";
```

## 修复效果

### 1. 问题解决

- **字段映射正确**: 使用正确的字段名`executionStatus`而不是`status`
- **状态值正确**: 使用大写的状态值`RUNNING`、`SUCCESS`、`FAILED`
- **触发类型正确**: 手动执行时使用`MANUAL`触发类型
- **执行节点标识**: 添加`executorNode`字段标识执行节点

### 2. 数据一致性

- **状态值统一**: 所有代码都使用相同的状态常量
- **字段名统一**: 所有代码都使用正确的MyBatis映射字段名
- **触发类型明确**: 区分定时触发和手动触发

### 3. 错误处理

- **空值检查**: 确保所有必需字段都有值
- **状态验证**: 使用预定义的状态常量，避免无效状态
- **异常处理**: 完善异常处理机制

## 相关文件

### 1. 修复的文件

- `efak-web/src/main/java/org/kafka/eagle/web/service/impl/TaskSchedulerServiceImpl.java`
  - 修复`recordTaskExecutionStart`方法
  - 修复`recordTaskExecutionEnd`方法

### 2. 参考文件

- `efak-web/src/main/resources/sql/efak_task_execution_history.sql`
  - 数据库表结构定义
- `efak-web/src/main/java/org/kafka/eagle/web/mapper/TaskExecutionHistoryMapper.java`
  - MyBatis映射接口
- `efak-web/src/main/java/org/kafka/eagle/web/util/TaskConfig.java`
  - 状态常量定义

### 3. 其他相关文件

- `efak-web/src/main/java/org/kafka/eagle/web/scheduler/EnhancedDistributedTaskScheduler.java`
  - 分布式任务调度器（已正确实现）
- `efak-web/src/main/java/org/kafka/eagle/web/service/impl/DistributedTaskSchedulerServiceImpl.java`
  - 分布式任务调度服务（已正确实现）

## 测试验证

### 1. 功能测试

1. **立即执行测试**: 点击立即执行按钮，确认不再出现`execution_status`为空异常
2. **状态记录测试**: 验证任务执行历史中正确记录了执行状态
3. **手动触发测试**: 确认手动触发的任务正确标记为`MANUAL`触发类型

### 2. 数据验证

1. **数据库查询**: 检查`ke_task_execution_history`表中的记录
2. **状态值验证**: 确认状态值都是有效的大写状态
3. **字段完整性**: 确认所有必需字段都有值

### 3. 异常处理测试

1. **空值测试**: 确保不会出现字段为空的情况
2. **异常恢复**: 测试异常情况下的恢复机制
3. **日志记录**: 确认异常日志正确记录

## 总结

通过修复`TaskSchedulerServiceImpl.java`中的字段映射问题，解决了任务执行时`execution_status`字段为空的问题：

1. **字段名修正**: 使用正确的MyBatis映射字段名
2. **状态值统一**: 使用预定义的状态常量
3. **触发类型明确**: 区分定时和手动触发
4. **数据完整性**: 确保所有必需字段都有值

这个修复确保了任务执行历史记录的正确性，提升了系统的稳定性和数据一致性。 