# Cron表达式更新功能

## 功能概述

本功能实现了当`ke_task_scheduler`表中的任务类型Cron表达式被修改时，自动更新对应的任务执行频率，同时更新上次执行和下次执行时间。

## 核心组件

### 1. CronExpressionUpdateService
- **位置**: `org.kafka.eagle.web.service.CronExpressionUpdateService`
- **功能**: 处理Cron表达式更新的核心服务
- **主要方法**:
  - `handleCronExpressionUpdate()`: 处理Cron表达式更新
  - `recalculateNextExecuteTime()`: 重新计算下次执行时间
  - `notifyDistributedScheduler()`: 通知分布式调度器

### 2. EnhancedDistributedTaskScheduler
- **位置**: `org.kafka.eagle.web.scheduler.EnhancedDistributedTaskScheduler`
- **功能**: 增强的分布式任务调度器，支持Cron表达式变化监听
- **新增功能**:
  - `startCronExpressionChangeListener()`: 启动Cron表达式变化监听器
  - `checkCronExpressionUpdates()`: 检查Cron表达式更新
  - `handleCronExpressionUpdate()`: 处理Cron表达式更新

### 3. TaskSchedulerMapper
- **位置**: `org.kafka.eagle.web.mapper.TaskSchedulerMapper`
- **新增方法**: `updateNextExecuteTime()`: 更新任务的下次执行时间

## 工作流程

### 1. Cron表达式更新流程
```
用户修改Cron表达式 
    ↓
TaskSchedulerServiceImpl.updateTaskScheduler()
    ↓
检查Cron表达式是否被修改
    ↓
CronExpressionUpdateService.handleCronExpressionUpdate()
    ↓
验证新的Cron表达式
    ↓
重新计算下次执行时间
    ↓
更新数据库中的next_execute_time
    ↓
通知分布式调度器
    ↓
发送Redis消息通知
```

### 2. 分布式调度器监听流程
```
EnhancedDistributedTaskScheduler启动
    ↓
startCronExpressionChangeListener()
    ↓
每30秒检查Redis中的更新通知
    ↓
发现Cron表达式更新通知
    ↓
handleCronExpressionUpdate()
    ↓
重新计算下次执行时间
    ↓
更新任务执行计划
```

## 支持的Cron表达式格式

### 基本格式
```
秒 分 时 日 月 周
```

### 支持的表达式类型
1. **固定时间**: `0 30 9 * * ?` (每天上午9:30)
2. **间隔执行**: `0 */5 * * * ?` (每5分钟执行一次)
3. **小时间隔**: `0 0 */2 * * ?` (每2小时执行一次)
4. **通配符**: `0 0 12 * * ?` (每天中午12点)

## API接口

### 1. 更新任务Cron表达式
```
POST /api/scheduler/{id}
Content-Type: application/json

{
  "cronExpression": "0 */10 * * * ?",
  "description": "更新后的描述",
  "timeout": 300
}
```

### 2. 测试Cron表达式更新
```
POST /api/cron-test/update-cron?taskId=1&newCronExpression=0 */15 * * * ?
```

### 3. 验证Cron表达式
```
POST /api/cron-test/validate-cron?cronExpression=0 */5 * * * ?
```

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

### 1. Redis配置
确保Redis服务可用，用于分布式调度器之间的通信：
```properties
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.database=0
```

### 2. 调度器配置
```properties
# 任务扫描间隔（毫秒）
efak.scheduler.scan.interval=60000

# Cron表达式更新检查间隔（秒）
efak.scheduler.cron.check.interval=30
```

## 使用示例

### 1. 修改任务执行频率
```bash
# 将任务1的执行频率从每5分钟改为每10分钟
curl -X PUT http://localhost:8080/api/scheduler/1 \
  -H "Content-Type: application/json" \
  -d '{
    "cronExpression": "0 */10 * * * ?",
    "description": "更新为每10分钟执行一次"
  }'
```

### 2. 测试Cron表达式更新
```bash
# 测试将任务1的Cron表达式更新为每15分钟执行一次
curl -X POST "http://localhost:8080/api/cron-test/update-cron?taskId=1&newCronExpression=0 */15 * * * ?"
```

## 监控和日志

### 1. 关键日志
- Cron表达式更新: `任务 {} 的Cron表达式已更新: {} -> {}`
- 下次执行时间计算: `任务 {} 的下次执行时间已重新计算为: {}`
- 分布式通知: `已发送Cron表达式更新通知到分布式调度器`

### 2. 监控指标
- Cron表达式更新次数
- 下次执行时间计算成功率
- 分布式调度器通知成功率

## 注意事项

1. **Cron表达式验证**: 所有Cron表达式都会进行格式验证
2. **分布式一致性**: 通过Redis消息确保多节点间的配置同步
3. **错误处理**: 更新失败时会记录详细错误日志
4. **性能考虑**: Cron表达式更新检查间隔为30秒，避免过于频繁的检查

## 故障排除

### 1. Cron表达式更新失败
- 检查Cron表达式格式是否正确
- 查看日志中的详细错误信息
- 确认数据库连接正常

### 2. 分布式调度器未收到更新通知
- 检查Redis服务是否正常
- 确认Redis配置正确
- 查看调度器日志中的通知处理情况

### 3. 下次执行时间计算错误
- 检查Cron表达式解析逻辑
- 确认时间计算算法正确
- 查看具体的Cron表达式格式 