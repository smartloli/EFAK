# EFAK 分布式任务调度系统

## 概述

本系统实现了基于Redis的分布式任务调度功能，解决了单节点执行定时任务时CPU和内存压力过大的问题。通过多节点部署和任务分片，可以有效分散系统负载，提高整体性能和可靠性。

## 核心特性

### 1. 服务注册与发现
- 自动服务注册到Redis注册中心
- 实时心跳监控
- 自动离线服务清理
- 动态服务发现

### 2. 任务分片
- **集群监控任务分片**: 将broker节点按节点数量均匀分配
- **主题监控任务分片**: 将主题按节点数量均匀分配
- **消费者监控任务分片**: 将消费者组按节点数量均匀分配
- 支持动态分片调整

### 3. 分布式协调
- 基于Redis的分布式锁
- 任务执行结果汇总
- 节点状态同步
- 故障转移支持

### 4. 监控与管理
- REST API监控接口
- 实时任务执行状态
- 分片结果查询
- 系统统计信息

## 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   EFAK Node 1   │    │   EFAK Node 2   │    │   EFAK Node N   │
│                 │    │                 │    │                 │
│ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │
│ │Task Executor│ │    │ │Task Executor│ │    │ │Task Executor│ │
│ └─────────────┘ │    │ └─────────────┘ │    │ └─────────────┘ │
│ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │
│ │Coordinator  │ │    │ │Coordinator  │ │    │ │Coordinator  │ │
│ └─────────────┘ │    │ └─────────────┘ │    │ └─────────────┘ │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │  Redis Cluster  │
                    │                 │
                    │ ┌─────────────┐ │
                    │ │Service Reg. │ │
                    │ └─────────────┘ │
                    │ ┌─────────────┐ │
                    │ │Shard Results│ │
                    │ └─────────────┘ │
                    │ ┌─────────────┐ │
                    │ │Distributed  │ │
                    │ │Lock         │ │
                    │ └─────────────┘ │
                    └─────────────────┘
```

## 核心组件

### 1. DistributedTaskCoordinator
分布式任务协调器，负责：
- 服务注册与心跳管理
- 任务分片逻辑
- 分布式锁管理
- 分片结果存储

### 2. UnifiedDistributedScheduler
统一分布式调度器，负责：
- 定时任务调度
- 任务执行协调
- 结果汇总触发
- 任务状态管理

### 3. TaskExecutorManager
任务执行管理器，负责：
- 具体任务执行
- 分片任务处理
- 执行结果记录

### 4. ShardResultAggregationService
分片结果汇总服务，负责：
- 收集各节点执行结果
- 数据汇总计算
- 统计信息生成

## 配置说明

### application.yml 配置

```yaml
efak:
  distributed:
    task:
      # 是否启用分布式任务调度
      enabled: true
      
      # 节点心跳间隔（秒）
      heartbeat-interval: 30
      
      # 离线节点清理间隔（秒）
      cleanup-interval: 60
      
      # 节点离线超时时间（秒）
      offline-timeout: 120
      
      # 分片结果等待时间（秒）
      shard-result-wait-time: 30
      
      # 分片结果过期时间（分钟）
      shard-result-expire-minutes: 10
      
      # 集群监控任务配置
      cluster-monitor:
        sharding-enabled: true
        min-shard-size: 1
        max-shard-size: 50
      
      # 主题监控任务配置
      topic-monitor:
        sharding-enabled: true
        min-shard-size: 1
        max-shard-size: 100
      
      # 消费者监控任务配置
      consumer-monitor:
        sharding-enabled: true
        min-shard-size: 1
        max-shard-size: 100

# Redis配置
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
    timeout: 3000ms
```

## 部署指南

### 1. 环境准备
- Java 8+
- Redis 5.0+
- Spring Boot 2.x+

### 2. 多节点部署

#### 节点1部署
```bash
# 启动第一个节点
java -jar efak-web.jar --server.port=8080
```

#### 节点2部署
```bash
# 启动第二个节点
java -jar efak-web.jar --server.port=8081
```

#### 节点N部署
```bash
# 启动第N个节点
java -jar efak-web.jar --server.port=808N
```

### 3. 验证部署

检查在线服务：
```bash
curl http://localhost:8080/api/distributed-task/services/online
```

查看分片信息：
```bash
curl http://localhost:8080/api/distributed-task/shards/brokers
```

## API 接口

### 1. 服务管理

#### 获取在线服务列表
```http
GET /api/distributed-task/services/online
```

#### 获取服务详细信息
```http
GET /api/distributed-task/services/{serviceId}
```

#### 清理离线服务
```http
POST /api/distributed-task/cleanup/offline-services
```

### 2. 分片管理

#### 获取broker分片信息
```http
GET /api/distributed-task/shards/brokers
```

#### 获取主题分片信息
```http
GET /api/distributed-task/shards/topics
```

#### 获取消费者组分片信息
```http
GET /api/distributed-task/shards/consumer-groups
```

### 3. 结果管理

#### 获取分片任务结果
```http
GET /api/distributed-task/results/{taskType}
```

#### 汇总分片任务结果
```http
POST /api/distributed-task/aggregate/{taskType}?waitTimeSeconds=30
```

#### 清理分片任务结果
```http
DELETE /api/distributed-task/results/{taskType}
```

### 4. 统计信息

#### 获取分布式任务统计
```http
GET /api/distributed-task/stats
```

## 工作流程

### 1. 服务启动流程
```
1. 服务启动
2. 生成唯一节点ID
3. 注册到Redis注册中心
4. 启动心跳定时器
5. 启动任务调度器
6. 开始接收任务分片
```

### 2. 任务执行流程
```
1. 定时任务触发
2. 获取在线服务列表
3. 计算任务分片
4. 执行分配的任务片段
5. 保存执行结果到Redis
6. 等待其他节点完成
7. 汇总所有节点结果
8. 清理分片数据
```

### 3. 故障处理流程
```
1. 心跳超时检测
2. 标记节点离线
3. 重新计算分片
4. 故障转移执行
5. 更新任务分配
```

## 性能优化

### 1. 分片策略优化
- 根据数据量动态调整分片大小
- 考虑节点性能差异进行权重分配
- 避免热点数据集中在单个节点

### 2. Redis优化
- 使用Redis集群提高可用性
- 合理设置过期时间避免内存泄漏
- 使用Pipeline批量操作提高性能

### 3. 网络优化
- 减少不必要的网络调用
- 使用连接池复用连接
- 合理设置超时时间

## 监控指标

### 1. 系统指标
- 在线节点数量
- 任务执行成功率
- 平均执行时间
- 分片均衡度

### 2. 性能指标
- CPU使用率
- 内存使用率
- 网络IO
- Redis连接数

### 3. 业务指标
- Broker监控覆盖率
- Topic监控覆盖率
- Consumer监控覆盖率
- 数据更新及时性

## 故障排查

### 1. 常见问题

#### 节点无法注册
- 检查Redis连接配置
- 确认网络连通性
- 查看防火墙设置

#### 任务分片不均匀
- 检查节点心跳状态
- 确认分片算法配置
- 查看节点负载情况

#### 结果汇总失败
- 检查等待时间配置
- 确认所有节点正常运行
- 查看Redis存储状态

### 2. 日志分析

#### 关键日志位置
```
org.kafka.eagle.web.scheduler.DistributedTaskCoordinator
org.kafka.eagle.web.scheduler.UnifiedDistributedScheduler
org.kafka.eagle.web.service.TaskExecutorManager
org.kafka.eagle.web.service.ShardResultAggregationService
```

#### 日志级别设置
```yaml
logging:
  level:
    org.kafka.eagle.web.scheduler: DEBUG
    org.kafka.eagle.web.service: DEBUG
```

## 扩展开发

### 1. 添加新的任务类型

1. 在TaskExecutorManager中添加新的执行方法
2. 在DistributedTaskCoordinator中添加分片逻辑
3. 在ShardResultAggregationService中添加汇总逻辑
4. 更新配置类支持新任务类型

### 2. 自定义分片策略

继承并实现自定义分片接口：
```java
public interface ShardingStrategy<T> {
    List<T> shard(List<T> items, List<String> nodes, String currentNode);
}
```

### 3. 扩展监控功能

添加自定义监控指标：
```java
@Component
public class CustomMetricsCollector {
    // 实现自定义指标收集逻辑
}
```

## 最佳实践

1. **合理配置分片大小**: 避免分片过小导致开销过大，或分片过大导致负载不均
2. **监控节点健康状态**: 及时发现和处理故障节点
3. **定期清理过期数据**: 避免Redis内存泄漏
4. **使用连接池**: 提高Redis连接复用率
5. **设置合理的超时时间**: 平衡系统响应性和稳定性
6. **备份重要配置**: 确保系统可快速恢复
7. **渐进式部署**: 先小规模验证再全面推广

## 版本历史

- v1.0.0: 初始版本，支持基本的分布式任务调度
- v1.1.0: 添加任务分片功能
- v1.2.0: 增加结果汇总和监控API
- v1.3.0: 优化性能和稳定性

## 技术支持

如有问题或建议，请联系开发团队或提交Issue。