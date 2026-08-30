# 更新任务执行历史字段映射修复

## 问题描述

在任务调度系统中，当更新任务执行历史记录时，出现以下异常：

```
### SQL: UPDATE ke_task_execution_history SET end_time = ?, duration = ?, execution_status = ?, result_message = ?, error_message = ?, updated_time = NOW() WHERE id = ?
### Cause: java.sql.SQLIntegrityConstraintViolationException: Column 'execution_status' cannot be null
; Column 'execution_status' cannot be null ===
```

## 问题分析

### 1. 问题根源

在`TaskExecutionHistoryMapper.java`的`updateTaskExecutionHistory`方法中，SQL语句使用的参数名与代码中传递的字段名不匹配：

```java
// 错误的SQL映射
@Update("UPDATE ke_task_execution_history SET end_time = #{endTime}, " +
        "duration = #{duration}, execution_status = #{status}, " +  // 错误：使用 #{status}
        "result_message = #{result}, error_message = #{errorMessage}, " +  // 错误：使用 #{result}
        "updated_time = NOW() WHERE id = #{id}")
```

但代码中传递的字段名是：
- `executionStatus` 而不是 `status`
- `resultMessage` 而不是 `result`

### 2. 字段映射问题

| 数据库字段 | 错误映射 | 正确映射 | 说明 |
|-----------|----------|----------|------|
| `execution_status` | `#{status}` | `#{executionStatus}` | 执行状态 |
| `result_message` | `#{result}` | `#{resultMessage}` | 执行结果消息 |

### 3. 影响范围

这个问题影响了所有调用`updateTaskExecutionHistory`的地方：

1. `TaskSchedulerServiceImpl.recordTaskExecutionEnd`
2. `EnhancedDistributedTaskScheduler.recordTaskExecutionEnd`
3. `DistributedTaskSchedulerServiceImpl.recordTaskExecutionEnd`

## 修复方案

### 1. 修复TaskExecutionHistoryMapper.java

```java
/**
 * 更新任务执行历史
 */
@Update("UPDATE ke_task_execution_history SET end_time = #{endTime}, " +
        "duration = #{duration}, execution_status = #{executionStatus}, " +  // 修复：使用正确的字段名
        "result_message = #{resultMessage}, error_message = #{errorMessage}, " +  // 修复：使用正确的字段名
        "updated_time = NOW() WHERE id = #{id}")
int updateTaskExecutionHistory(Map<String, Object> history);
```

### 2. 字段映射对照表

| 数据库字段 | MyBatis映射字段 | 状态值 | 说明 |
|-----------|----------------|--------|------|
| `end_time` | `endTime` | - | 结束时间 |
| `duration` | `duration` | - | 执行时长(毫秒) |
| `execution_status` | `executionStatus` | `SUCCESS`/`FAILED` | 执行状态 |
| `result_message` | `resultMessage` | - | 执行结果消息 |
| `error_message` | `errorMessage` | - | 错误信息 |
| `id` | `id` | - | 记录ID |

### 3. 调用代码验证

所有调用`updateTaskExecutionHistory`的代码都正确使用了字段名：

#### TaskSchedulerServiceImpl.java
```java
public void recordTaskExecutionEnd(Long executionId, boolean success, String result, String errorMessage) {
    LocalDateTime endTime = LocalDateTime.now();
    Map<String, Object> history = new HashMap<>();
    history.put("id", executionId);
    history.put("endTime", endTime);
    history.put("executionStatus", success ? "SUCCESS" : "FAILED");  // 正确
    history.put("resultMessage", result);  // 正确
    history.put("errorMessage", errorMessage);  // 正确

    taskExecutionHistoryMapper.updateTaskExecutionHistory(history);
}
```

#### EnhancedDistributedTaskScheduler.java
```java
private void recordTaskExecutionEnd(Long executionId, TaskExecutionResult result) {
    if (executionId == null) {
        return;
    }

    try {
        Map<String, Object> history = new HashMap<>();
        history.put("id", executionId);
        history.put("endTime", result.getEndTime());
        history.put("duration", result.getDuration());
        history.put("executionStatus", result.isSuccess() ? "SUCCESS" : "FAILED");  // 正确
        history.put("resultMessage", result.getResult());  // 正确
        history.put("errorMessage", result.getErrorMessage());  // 正确

        taskExecutionHistoryMapper.updateTaskExecutionHistory(history);
    } catch (Exception e) {
        log.error("记录任务执行结束失败", e);
    }
}
```

#### DistributedTaskSchedulerServiceImpl.java
```java
private void recordTaskExecutionEnd(Long executionId, TaskExecutionResult result) {
    Map<String, Object> history = new HashMap<>();
    history.put("id", executionId);
    history.put("endTime", LocalDateTime.now());
    history.put("executionStatus",
            result.isSuccess() ? TaskConfig.EXECUTION_STATUS_SUCCESS : TaskConfig.EXECUTION_STATUS_FAILED);  // 正确
    history.put("resultMessage", result.getResult());  // 正确
    history.put("errorMessage", result.getErrorMessage());  // 正确
    history.put("duration", result.getDuration());

    taskExecutionHistoryMapper.updateTaskExecutionHistory(history);
}
```

## 修复效果

### 1. 字段映射正确

- **executionStatus**: 正确映射到数据库的`execution_status`字段
- **resultMessage**: 正确映射到数据库的`result_message`字段
- **errorMessage**: 正确映射到数据库的`error_message`字段

### 2. 数据完整性

- **NOT NULL约束**: 确保`execution_status`字段不会为空
- **状态值正确**: 使用预定义的状态常量
- **字段完整性**: 所有必需字段都有正确的值

### 3. 错误处理

- **异常消除**: 不再出现`execution_status`为空异常
- **数据一致性**: 确保更新操作的正确性
- **日志记录**: 保持现有的异常处理和日志记录

## 技术细节

### 1. MyBatis参数映射

```java
// 修复前（错误）
execution_status = #{status}
result_message = #{result}

// 修复后（正确）
execution_status = #{executionStatus}
result_message = #{resultMessage}
```

### 2. 字段命名规范

- **驼峰命名**: Java代码中使用驼峰命名法
- **下划线命名**: 数据库字段使用下划线命名法
- **MyBatis映射**: 自动处理驼峰到下划线的转换

### 3. 状态值管理

```java
// 使用TaskConfig中定义的状态常量
TaskConfig.EXECUTION_STATUS_SUCCESS  // "SUCCESS"
TaskConfig.EXECUTION_STATUS_FAILED   // "FAILED"
```

## 测试验证

### 1. 功能测试

1. **任务执行测试**: 执行任务并验证历史记录更新
2. **状态更新测试**: 验证执行状态正确更新
3. **结果记录测试**: 验证执行结果和错误信息正确记录

### 2. 数据验证

1. **数据库查询**: 检查`ke_task_execution_history`表中的记录
2. **字段完整性**: 确认所有字段都有正确的值
3. **状态值验证**: 确认状态值都是有效的

### 3. 异常测试

1. **空值测试**: 确保不会出现字段为空的情况
2. **异常恢复**: 测试异常情况下的恢复机制
3. **边界值测试**: 测试极值情况

## 相关文件

### 1. 修复的文件

- `efak-web/src/main/java/org/kafka/eagle/web/mapper/TaskExecutionHistoryMapper.java`
  - 修复`updateTaskExecutionHistory`方法的SQL映射

### 2. 验证的文件

- `efak-web/src/main/java/org/kafka/eagle/web/service/impl/TaskSchedulerServiceImpl.java`
  - 验证`recordTaskExecutionEnd`方法的字段名
- `efak-web/src/main/java/org/kafka/eagle/web/scheduler/EnhancedDistributedTaskScheduler.java`
  - 验证`recordTaskExecutionEnd`方法的字段名
- `efak-web/src/main/java/org/kafka/eagle/web/service/impl/DistributedTaskSchedulerServiceImpl.java`
  - 验证`recordTaskExecutionEnd`方法的字段名

### 3. 相关配置

- `efak-web/src/main/resources/sql/efak_task_execution_history.sql`
  - 数据库表结构定义
- `efak-web/src/main/java/org/kafka/eagle/web/util/TaskConfig.java`
  - 状态常量定义

## 总结

通过修复`TaskExecutionHistoryMapper.java`中的字段映射问题，解决了更新任务执行历史时`execution_status`字段为空的问题：

1. **字段映射正确**: 使用正确的MyBatis参数映射
2. **命名规范统一**: 保持Java代码和数据库字段的命名一致性
3. **数据完整性**: 确保所有必需字段都有正确的值
4. **状态管理**: 使用预定义的状态常量

这个修复确保了任务执行历史记录的正确更新，提升了系统的数据一致性和稳定性。 