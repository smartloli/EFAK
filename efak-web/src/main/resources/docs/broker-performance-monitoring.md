# Broker性能监控功能实现文档

## 功能概述

本文档描述了EFAK系统中新增的Broker性能监控功能，包括CPU和内存使用率的历史数据存储、趋势图表展示以及数据清理机制。

## 功能特性

### 1. 性能数据收集
- 在现有的集群监控任务中集成broker性能数据收集
- 自动收集每个broker的CPU和内存使用率
- 数据存储到`ke_broker_metrics`表中
- 支持批量数据保存

### 2. 历史数据存储
- 创建专门的`ke_broker_metrics`表存储历史性能数据
- 包含broker ID、主机IP、端口、CPU使用率、内存使用率、采集时间等字段
- 支持高效的时间范围查询和聚合统计

### 3. 趋势图表展示
- 在集群管理页面新增CPU和内存使用率趋势图表
- 支持多个时间范围选择（1小时、6小时、12小时、1天、3天、7天）
- 使用Chart.js实现交互式图表
- 支持多broker数据对比显示

### 4. 数据清理机制
- 集成到现有的数据清理任务中
- 默认保留30天的历史数据
- 自动清理过期数据，释放存储空间

## 技术实现

### 数据库设计

#### ke_broker_metrics表结构
```sql
CREATE TABLE ke_broker_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    broker_id INT NOT NULL,
    host_ip VARCHAR(50) NOT NULL,
    port INT NOT NULL,
    cpu_usage DECIMAL(5,2),
    memory_usage DECIMAL(5,2),
    collect_time DATETIME NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_broker_collect_time (broker_id, collect_time),
    INDEX idx_collect_time (collect_time)
);
```

### 后端实现

#### 1. 数据模型
- `BrokerMetrics.java`: 性能指标数据传输对象
- 包含broker基本信息和性能指标数据

#### 2. 数据访问层
- `BrokerMetricsMapper.java`: MyBatis映射接口
- 提供数据的增删改查操作
- 支持时间范围查询和聚合统计
- 使用注解方式实现SQL操作

#### 3. 服务层
- `BrokerMetricsService.java`: 服务接口
- `BrokerMetricsServiceImpl.java`: 服务实现
- 提供性能数据的业务逻辑处理
- 支持单条和批量数据保存
- 提供趋势数据查询功能

#### 4. 控制器层
- `BrokerMetricsController.java`: REST API控制器
- 提供`/api/brokers/metrics/cpu`和`/api/brokers/metrics/memory`接口
- 支持时间范围参数
- 返回格式化的图表数据

#### 5. 任务集成
- `TaskExecutorManager.java`: 任务执行管理器
- 在`executeClusterMonitorTask`方法中集成性能数据收集
- 在`executeDataCleanupTask`方法中集成数据清理逻辑

### 前端实现

#### 1. 页面结构
- `cluster.html`: 集群管理页面
- 新增CPU和内存使用率趋势图表容器
- 包含时间范围选择和刷新按钮

#### 2. 样式设计
- 添加图表控件的CSS样式
- 保持与现有页面风格一致
- 响应式设计支持

#### 3. JavaScript功能
- `cluster.js`: 集群管理页面脚本
- 新增broker性能图表相关功能
- 使用Chart.js库实现图表渲染
- 支持异步数据加载和图表更新
- 提供用户交互功能（时间范围选择、手动刷新）

## API接口

### 1. 获取CPU使用率趋势
```
GET /api/brokers/metrics/cpu?timeRange={timeRange}
```

**参数：**
- `timeRange`: 时间范围（1h, 6h, 12h, 1d, 3d, 7d），默认1h

**响应：**
```json
{
  "success": true,
  "data": {
    "labels": ["时间标签1", "时间标签2", ...],
    "datasets": [
      {
        "label": "Broker 1",
        "data": [10.5, 15.2, 12.8, ...]
      },
      {
        "label": "Broker 2",
        "data": [8.3, 11.7, 9.5, ...]
      }
    ]
  },
  "message": "获取CPU趋势数据成功"
}
```

### 2. 获取内存使用率趋势
```
GET /api/brokers/metrics/memory?timeRange={timeRange}
```

**参数和响应格式与CPU接口相同**

## 部署说明

### 1. 数据库更新
- 确保`ke_broker_metrics`表已创建
- 检查索引是否正确建立

### 2. 配置检查
- 确认集群监控任务正常运行
- 验证数据清理任务配置

### 3. 功能验证
- 检查性能数据是否正常收集
- 验证前端图表是否正常显示
- 测试API接口响应

## 监控和维护

### 1. 数据监控
- 定期检查`ke_broker_metrics`表的数据量
- 监控数据收集的完整性
- 关注数据清理任务的执行情况

### 2. 性能优化
- 根据数据量调整索引策略
- 优化查询性能
- 考虑数据分区策略（如果数据量很大）

### 3. 故障排查
- 检查日志中的错误信息
- 验证数据库连接和权限
- 确认前端资源加载正常

## 扩展建议

### 1. 功能扩展
- 添加更多性能指标（磁盘I/O、网络流量等）
- 支持性能告警功能
- 提供性能报告导出

### 2. 技术优化
- 考虑使用时序数据库存储性能数据
- 实现数据压缩和归档
- 添加缓存机制提升查询性能

### 3. 用户体验
- 添加更多图表类型选择
- 支持自定义时间范围
- 提供数据下载功能

## 总结

本次实现的broker性能监控功能为EFAK系统提供了完整的性能数据收集、存储、展示和管理能力。通过集成到现有的任务调度系统中，确保了功能的稳定性和可维护性。前端图表提供了直观的数据可视化，帮助用户更好地监控和分析Kafka集群的性能状况。