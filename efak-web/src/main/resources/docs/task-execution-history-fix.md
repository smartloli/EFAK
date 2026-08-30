# 任务执行历史记录修复

## 问题描述

遇到以下错误：
```
org.springframework.dao.DataIntegrityViolationException: 
### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Column 'updated_time' cannot be null
### The error may exist in org/kafka/eagle/web/mapper/TaskExecutionHistoryMapper.java (best guess)
### The error may involve org.kafka.eagle.web.mapper.TaskExecutionHistoryMapper.insertTaskExecutionHistory-Inline
### The error occurred while setting parameters
### SQL: INSERT INTO ke_task_execution_history (task_id, task_name, task_type, execution_status, start_time, end_time, duration, result_message, error_message, executor_node, trigger_type, trigger_user, input_params, output_result, created_time, updated_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
### Cause: java.sql.SQLIntegrityConstraintViolationException: Column 'updated_time' cannot be null
; Column 'updated_time' cannot be null
```

## 问题原因

1. **数据库表结构**: `ke_task_execution_history`表中的`updated_time`字段定义为：
   ```sql
   `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
   ```

2. **代码问题**: 在插入任务执行历史记录时，代码试图手动设置`updated_time`字段，但传递了`null`值，导致数据库约束违反。

3. **Mapper配置**: `TaskExecutionHistoryMapper.insertTaskExecutionHistory`方法在SQL中包含了`updated_time`字段，但代码中没有正确设置该值。

## 解决方案

### 1. 修改Mapper SQL语句

**修改前**:
```sql
INSERT INTO ke_task_execution_history (
    task_id, task_name, task_type, execution_status, 
    start_time, end_time, duration, result_message, error_message, 
    executor_node, trigger_type, trigger_user, input_params, output_result, 
    created_time, updated_time
) VALUES (
    #{taskId}, #{taskName}, #{taskType}, #{executionStatus}, 
    #{startTime}, #{endTime}, #{duration}, #{resultMessage}, #{errorMessage}, 
    #{executorNode}, #{triggerType}, #{triggerUser}, #{inputParams}, #{outputResult}, 
    #{createdTime}, #{updatedTime}
)
```

**修改后**:
```sql
INSERT INTO ke_task_execution_history (
    task_id, task_name, task_type, execution_status, 
    start_time, end_time, duration, result_message, error_message, 
    executor_node, trigger_type, trigger_user, input_params, output_result, 
    created_time
) VALUES (
    #{taskId}, #{taskName}, #{taskType}, #{executionStatus}, 
    #{startTime}, #{endTime}, #{duration}, #{resultMessage}, #{errorMessage}, 
    #{executorNode}, #{triggerType}, #{triggerUser}, #{inputParams}, #{outputResult}, 
    #{createdTime}
)
```

### 2. 移除代码中的updated_time设置

**修改的文件**:
- `EnhancedDistributedTaskScheduler.java`
- `DistributedTaskSchedulerServiceImpl.java`
- `TaskSchedulerServiceImpl.java`

**修改内容**:
```java
// 移除以下代码行
history.put("updatedTime", LocalDateTime.now());
```

### 3. 让数据库自动处理updated_time

利用数据库的`DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`特性：
- 插入时：数据库自动设置`updated_time`为当前时间
- 更新时：数据库自动更新`updated_time`为当前时间

## 修复详情

### 1. TaskExecutionHistoryMapper.java
```java
@Insert("INSERT INTO ke_task_execution_history (task_id, task_name, task_type, execution_status, " +
        "start_time, end_time, duration, result_message, error_message, executor_node, " +
        "trigger_type, trigger_user, input_params, output_result, created_time) " +
        "VALUES (#{taskId}, #{taskName}, #{taskType}, #{executionStatus}, " +
        "#{startTime}, #{endTime}, #{duration}, #{resultMessage}, #{errorMessage}, " +
        "#{executorNode}, #{triggerType}, #{triggerUser}, #{inputParams}, #{outputResult}, " +
        "#{createdTime})")
@Options(useGeneratedKeys = true, keyProperty = "id")
int insertTaskExecutionHistory(Map<String, Object> history);
```

### 2. EnhancedDistributedTaskScheduler.java
```java
private Long recordTaskExecutionStart(TaskScheduler task) {
    try {
        Map<String, Object> history = new HashMap<>();
        history.put("taskId", task.getId());
        history.put("taskName", task.getTaskName());
        history.put("taskType", task.getTaskType());
        history.put("clusterName", task.getClusterName());
        history.put("executionStatus", "RUNNING");
        history.put("startTime", LocalDateTime.now());
        history.put("executorNode", taskCoordinator.getCurrentNodeId());
        history.put("triggerType", "SCHEDULED");
        history.put("createdTime", LocalDateTime.now());
        // 移除: history.put("updatedTime", LocalDateTime.now());

        taskExecutionHistoryMapper.insertTaskExecutionHistory(history);
        return (Long) history.get("id");
    } catch (Exception e) {
        log.error("记录任务执行开始失败", e);
        return null;
    }
}
```

### 3. DistributedTaskSchedulerServiceImpl.java
```java
private Long recordTaskExecutionStart(TaskScheduler task) {
    Map<String, Object> history = new HashMap<>();
    history.put("taskId", task.getId());
    history.put("taskName", task.getTaskName());
    history.put("taskType", task.getTaskType());
    history.put("clusterName", task.getClusterName());
    history.put("startTime", LocalDateTime.now());
    history.put("executionStatus", TaskConfig.EXECUTION_STATUS_RUNNING);
    history.put("triggerType", TaskConfig.TRIGGER_TYPE_SCHEDULED);
    history.put("executorNode", getCurrentNodeInfo().get("nodeId"));
    history.put("createdTime", LocalDateTime.now());
    // 移除: history.put("updatedTime", LocalDateTime.now());

    taskExecutionHistoryMapper.insertTaskExecutionHistory(history);
    return (Long) history.get("id");
}
```

### 4. TaskSchedulerServiceImpl.java
```java
public Long recordTaskExecutionStart(Long taskId, String taskName, String taskType, String clusterName) {
    Map<String, Object> history = new HashMap<>();
    history.put("taskId", taskId);
    history.put("taskName", taskName);
    history.put("taskType", taskType);
    history.put("clusterName", clusterName);
    history.put("startTime", LocalDateTime.now());
    history.put("status", "running");
    history.put("createdTime", LocalDateTime.now());
    // 移除: history.put("updatedTime", LocalDateTime.now());

    taskExecutionHistoryMapper.insertTaskExecutionHistory(history);
    return (Long) history.get("id");
}
```

## 测试验证

### 1. 创建测试控制器
创建了`TaskExecutionTestController`用于测试：
- 测试插入任务执行历史
- 测试更新任务执行历史
- 查询任务执行历史
- 测试数据库连接

### 2. 测试端点
```bash
# 测试插入
POST /api/test/task-execution/test-insert

# 测试更新
POST /api/test/task-execution/test-update/{executionId}

# 查询历史
GET /api/test/task-execution/history/{taskId}

# 测试连接
GET /api/test/task-execution/test-connection
```

## 数据库表结构

### ke_task_execution_history表
```sql
CREATE TABLE IF NOT EXISTS `ke_task_execution_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` bigint(20) NOT NULL COMMENT '任务ID',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `task_type` varchar(50) NOT NULL COMMENT '任务类型',
  `execution_status` varchar(20) NOT NULL COMMENT '执行状态',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration` bigint(20) DEFAULT NULL COMMENT '执行时长(毫秒)',
  `result_message` text COMMENT '执行结果消息',
  `error_message` text COMMENT '错误信息',
  `executor_node` varchar(100) DEFAULT NULL COMMENT '执行节点',
  `trigger_type` varchar(20) DEFAULT 'SCHEDULED' COMMENT '触发类型',
  `trigger_user` varchar(50) DEFAULT NULL COMMENT '触发用户',
  `input_params` text COMMENT '输入参数(JSON格式)',
  `output_result` text COMMENT '输出结果(JSON格式)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_execution_status` (`execution_status`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_task_type` (`task_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务执行历史表';
```

## 优势

### 1. 简化代码
- 移除了手动设置`updated_time`的代码
- 减少了代码复杂度和维护成本

### 2. 数据库一致性
- 利用数据库的自动时间戳功能
- 确保时间戳的一致性和准确性

### 3. 性能优化
- 减少了数据传输量
- 利用数据库内置功能提高性能

### 4. 错误预防
- 避免了手动设置时间戳可能出现的错误
- 确保数据完整性

## 注意事项

### 1. 数据库兼容性
- 确保数据库支持`DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`
- MySQL 5.6+支持此功能

### 2. 时区设置
- 确保数据库和应用使用相同的时区设置
- 建议使用UTC时区避免时区问题

### 3. 测试验证
- 在修复后进行全面测试
- 验证插入和更新操作是否正常

## 总结

通过以下步骤成功修复了任务执行历史记录的`updated_time`字段问题：

1. **修改Mapper SQL**: 移除`updated_time`字段的手动设置
2. **移除代码设置**: 删除所有手动设置`updated_time`的代码
3. **利用数据库特性**: 让数据库自动处理时间戳
4. **创建测试工具**: 提供测试端点验证修复效果

修复后，任务执行历史记录功能应该能够正常工作，不再出现`updated_time`字段为空的错误。 