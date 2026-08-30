# Topic Record Capacity 功能说明

## 概述

本文档介绍了新增的 `getTopicRecordCapacityNum` 方法，该方法用于获取 Kafka 主题的记录容量大小（以字节为单位）。

## 功能特性

### 主要方法

#### `getTopicRecordCapacityNum(String topicName, List<BrokerInfo> brokers)`

**功能**: 计算指定主题在所有分区中的总容量大小

**参数**:
- `topicName`: 主题名称
- `brokers`: Broker 信息列表

**返回值**: 
- 成功时返回主题总容量（字节）
- 失败时返回 -1

**实现原理**:
1. 获取主题的分区元数据和领导者信息
2. 遍历每个分区，获取其领导者 Broker 的 JMX URI
3. 通过 JMX 连接获取每个分区的大小
4. 累加所有分区大小得到总容量

### 辅助方法

#### `getTopicMetaData(String topicName, List<BrokerInfo> brokers)`

**功能**: 获取主题元数据，包括分区-领导者映射关系

**返回值**: Map<分区号, 领导者Broker ID>

#### `getBrokerJmxRmiOfLeaderId(int leaderId, List<BrokerInfo> brokers)`

**功能**: 根据领导者 Broker ID 获取对应的 JMX URI

**返回值**: JMX URI (host:port) 或 null

#### `getPartitionSize(String jmxUri, String topicName, int partition)`

**功能**: 从 JMX 指标获取指定分区的大小

**返回值**: 分区大小（字节）或 -1

## 使用示例

### 基本使用

```java
// 初始化 JMX 服务
JmxServiceProxy.initialize();

// 准备 Broker 信息
List<BrokerInfo> brokers = getBrokerList(); // 从数据库或配置获取

// 获取主题容量
String topicName = "my-topic";
long capacity = JmxServiceProxy.getTopicRecordCapacityNum(topicName, brokers);

if (capacity >= 0) {
    System.out.println("主题容量: " + capacity + " 字节");
    System.out.println("主题容量: " + (capacity / 1024 / 1024) + " MB");
} else {
    System.out.println("获取主题容量失败");
}
```

### 批量获取多个主题容量

```java
String[] topics = {"topic1", "topic2", "topic3"};
long totalCapacity = 0;

for (String topic : topics) {
    long capacity = JmxServiceProxy.getTopicRecordCapacityNum(topic, brokers);
    if (capacity >= 0) {
        totalCapacity += capacity;
        System.out.println(topic + ": " + capacity + " 字节");
    }
}

System.out.println("总容量: " + totalCapacity + " 字节");
```

## JMX 指标说明

### 使用的 JMX 指标

- **ObjectName**: `kafka.log:type=Log,name=Size,topic=<topicName>,partition=<partitionId>`
- **Attribute**: `Value`
- **含义**: 指定主题分区的日志大小（字节）

### JMX 常量

在 `JmxConstants.KafkaLog` 枚举中新增了 `SIZE` 常量：

```java
SIZE("kafka.log:type=Log,name=Size,topic=%s,partition=%d")
```

## 错误处理

### 常见错误情况

1. **主题名称为空**: 返回 -1
2. **Broker 列表为空**: 返回 -1
3. **无法获取主题元数据**: 返回 -1
4. **JMX 连接失败**: 跳过该分区，继续处理其他分区
5. **分区大小获取失败**: 跳过该分区，记录警告日志

### 日志级别

- **INFO**: 成功获取容量信息
- **WARN**: 部分分区获取失败或配置问题
- **ERROR**: 严重错误，如连接失败或异常
- **DEBUG**: 详细的分区大小信息

## 性能考虑

### 优化建议

1. **连接复用**: 对同一 Broker 的多个分区查询可以复用 JMX 连接
2. **并行处理**: 可以并行获取不同 Broker 上的分区大小
3. **缓存机制**: 对于频繁查询的主题，可以实现缓存机制
4. **超时设置**: JMX 连接使用了 30 秒超时，避免长时间阻塞

### 注意事项

1. **JMX 端口配置**: 确保所有 Broker 都正确配置了 JMX 端口
2. **网络连通性**: 确保应用服务器能够访问 Broker 的 JMX 端口
3. **权限设置**: 确保有足够权限访问 JMX 指标
4. **Broker 状态**: 建议优先使用在线状态的 Broker

## 测试示例

参考 `TopicCapacityExample.java` 文件中的完整测试示例，包括：

- 单个主题容量获取
- 多个主题批量获取
- 错误处理演示
- 性能测试

## 依赖关系

### 必需的类

- `JmxServiceProxy`: 主要服务类
- `JmxConstants`: JMX 常量定义
- `JmxUtils`: JMX 工具类
- `BrokerInfo`: Broker 信息 DTO
- `TopicDetailedStats`: 主题详细统计信息
- `KafkaServiceProxy`: Kafka 服务代理

### Maven 依赖

确保项目中包含以下依赖：

```xml
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
</dependency>
```

## 版本信息

- **版本**: 1.0
- **作者**: Mr.SmartLoli
- **创建日期**: 2025/6/22
- **最后更新**: 2025/6/22