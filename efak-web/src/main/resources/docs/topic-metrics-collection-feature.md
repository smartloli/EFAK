# Topic指标采集功能开发文档

## 功能概述

本次开发实现了Kafka Topic指标采集功能，支持在分布式环境下采集Topic的详细指标数据并保存到数据库中，为后续的监控和分析提供数据支持。

## 开发内容

### 1. 数据结构设计

#### TopicMetrics 数据传输对象
- **位置**: `efak-dto/src/main/java/org/kafka/eagle/dto/topic/TopicMetrics.java`
- **功能**: 定义Topic指标的数据结构
- **字段**:
  - `id`: 主键ID
  - `topicName`: Topic名称
  - `recordCount`: 记录数
  - `capacity`: 容量(字节)
  - `writeSpeed`: 写入速度(消息/秒)
  - `readSpeed`: 读取速度(消息/秒)
  - `collectTime`: 采集时间
  - `createTime`: 创建时间

### 2. 数据库设计

#### ke_topics_metrics 表
- **位置**: `efak-web/src/main/resources/sql/ke_topics_metrics.sql`
- **功能**: 存储Topic指标采集明细数据
- **特性**:
  - 支持按月分区以提高查询性能
  - 创建了多个索引优化查询效率
  - 使用InnoDB引擎，支持事务

### 3. 数据访问层

#### TopicMetricsMapper 接口
- **位置**: `efak-web/src/main/java/org/kafka/eagle/web/mapper/TopicMetricsMapper.java`
- **功能**: 使用MyBatis注解实现数据库操作
- **方法**:
  - `insert`: 插入单条记录
  - `batchInsert`: 批量插入记录
  - `selectTopicMetrics`: 分页查询指标
  - `countTopicMetrics`: 统计记录数
  - `selectLatestTopicMetrics`: 查询最新指标
  - `deleteOldMetrics`: 删除历史数据
  - `getTopicMetricsStatistics`: 获取统计信息

### 4. 业务逻辑层

#### TopicMetricsService 接口和实现
- **接口位置**: `efak-web/src/main/java/org/kafka/eagle/web/service/TopicMetricsService.java`
- **实现位置**: `efak-web/src/main/java/org/kafka/eagle/web/service/impl/TopicMetricsServiceImpl.java`
- **核心功能**:
  - Topic指标的CRUD操作
  - 批量数据处理
  - 指标数据采集和转换
  - 历史数据清理

### 5. 任务执行器扩展

#### TaskExecutorManager 扩展
- **位置**: `efak-web/src/main/java/org/kafka/eagle/web/service/TaskExecutorManager.java`
- **扩展内容**:
  - 在`executeTopicMonitorTask`方法中集成Topic指标采集功能
  - 支持分片环境下的指标采集
  - 添加了`collectAndSaveTopicMetrics`方法

## 技术特性

### 1. 分布式支持
- 支持在分布式环境下进行Topic分片采集
- 每个节点只处理分配给它的Topic，避免重复采集
- 通过DistributedTaskCoordinator进行任务协调

### 2. 性能优化
- 使用批量插入提高数据库写入性能
- 数据库表支持分区，提高查询效率
- 创建了合适的索引优化常用查询

### 3. 异常处理
- 完善的异常处理机制
- 单个Topic采集失败不影响其他Topic
- 详细的日志记录便于问题排查

### 4. 事务支持
- 使用Spring事务管理确保数据一致性
- 批量操作支持回滚机制

## 测试结果

### 1. 编译测试
- ✅ Maven编译成功，无编译错误
- ✅ 所有相关类文件正确生成

### 2. 数据库测试
- ✅ ke_topics_metrics表创建成功
- ✅ 表结构符合设计要求
- ✅ 索引创建正确

### 3. 代码结构测试
- ✅ TopicMetrics.java 创建成功
- ✅ TopicMetricsMapper.java 创建成功
- ✅ TopicMetricsService.java 创建成功
- ✅ TopicMetricsServiceImpl.java 创建成功
- ✅ TaskExecutorManager.java 扩展成功

## 使用说明

### 1. 自动采集
Topic指标采集功能已集成到现有的定时任务系统中，会在执行Topic监控任务时自动采集指标数据。

### 2. 手动调用
可以通过TopicMetricsService的`collectAndSaveTopicMetrics()`方法手动触发指标采集。

### 3. 数据查询
可以通过TopicMetricsService提供的各种查询方法获取指标数据：
- 分页查询指定时间范围的指标
- 获取特定Topic的最新指标
- 获取指标统计信息

### 4. 数据清理
系统提供了`deleteOldMetrics`方法用于清理历史数据，建议定期执行以控制数据量。

## 后续优化建议

1. **实时指标计算**: 可以考虑添加实时的写入/读取速度计算
2. **指标聚合**: 添加按小时、天、月的指标聚合功能
3. **告警集成**: 基于指标数据实现自动告警功能
4. **可视化支持**: 为前端提供图表展示所需的API接口
5. **性能监控**: 添加采集任务本身的性能监控

## 总结

Topic指标采集功能已成功开发完成，包括完整的数据结构设计、数据库表创建、业务逻辑实现和任务集成。功能支持分布式环境，具有良好的性能和可扩展性，为EFAK系统的监控能力提供了重要支撑。

---

**开发时间**: 2025年1月27日  
**开发者**: smartloli  
**版本**: 1.0.0