# 任务执行使用真实数据修复

## 问题描述

在任务调度系统中，`TaskExecutorManager`中的任务执行方法都使用了模拟数据，包括：

- `Thread.sleep()` 模拟执行时间
- 硬编码的统计数据
- 随机生成的数值
- 模拟的监控结果

这导致任务执行历史记录中显示的是虚假数据，而不是真实的Kafka集群状态。

## 修复方案

### 1. 修改TaskExecutorManager.java

将所有的模拟数据替换为真实的Kafka数据，使用`KafkaServiceProxy`获取真实的集群信息。

#### 1.1 主题监控任务修复

**修复前（模拟数据）：**
```java
// 模拟主题监控逻辑
Thread.sleep(2000); // 模拟执行时间

Map<String, Object> data = new HashMap<>();
data.put("topicCount", 45);
data.put("partitionCount", 180);
data.put("messageCount", 1250000);
data.put("consumerGroups", 12);

result.setResult("主题监控完成，共监控45个主题，180个分区，消息总数125万条，消费者组12个");
```

**修复后（真实数据）：**
```java
// 从数据库获取Broker信息
List<org.kafka.eagle.dto.broker.BrokerInfo> dbBrokers = brokerMapper.queryAllBrokers();

// 获取所有主题名称（参数化API）
List<String> topicNames = KafkaServiceProxy.getAllTopicNames();

// 获取主题详细统计信息（仅针对当前集群）
List<TopicDetailedStats> topicStats = KafkaServiceProxy.getTopicsDetailedStats(topicNames, dbBrokers);

// 计算总统计信息
long totalPartitions = 0;
long totalRecords = 0;
long totalSize = 0;

for (TopicDetailedStats stats : topicStats) {
    totalPartitions += stats.getPartitionCount();
    totalRecords += stats.getTotalRecords();
    totalSize += stats.getTotalSize();
}

Map<String, Object> data = new HashMap<>();
data.put("topicCount", topicNames.size());
data.put("partitionCount", totalPartitions);
data.put("messageCount", totalRecords);
data.put("totalSize", totalSize);
data.put("avgPartitionCount", topicStats.isEmpty() ? 0 : totalPartitions / topicStats.size());
data.put("avgMessageCount", topicStats.isEmpty() ? 0 : totalRecords / topicStats.size());
data.put("avgTopicSize", topicStats.isEmpty() ? 0 : totalSize / topicStats.size());

result.setResult(String.format("主题监控完成，共监控%d个主题，%d个分区，消息总数%d条，总大小%d字节", 
    topicNames.size(), totalPartitions, totalRecords, totalSize));
```

#### 1.2 消费者监控任务修复

**修复前（模拟数据）：**
```java
// 模拟消费者监控逻辑
Thread.sleep(1500); // 模拟执行时间

Map<String, Object> data = new HashMap<>();
data.put("consumerGroupCount", 12);
data.put("activeConsumers", 8);
data.put("lagConsumers", 2);
data.put("totalLag", 15000);

result.setResult("消费者监控完成，共监控12个消费者组，8个活跃消费者，2个存在延迟，总延迟15000条消息");
```

**修复后（真实数据）：**
```java
// 从数据库获取Broker信息
List<org.kafka.eagle.dto.broker.BrokerInfo> dbBrokers = brokerMapper.queryAllBrokers();

// 获取所有消费者组ID（参数化API）
List<String> consumerGroupIds = KafkaServiceProxy.getAllConsumerGroupIds();

// 获取消费者组详细信息（参数化API）
List<ConsumerGroupDetailedInfo> consumerGroups = KafkaServiceProxy.getAllConsumerGroupDetailedInfo();

// 计算统计信息
int activeConsumers = 0;
int lagConsumers = 0;
long totalLag = 0;

for (ConsumerGroupDetailedInfo group : consumerGroups) {
    if ("STABLE".equals(group.getState())) {
        activeConsumers++;
    }
    // 计算延迟
    Long groupLag = group.getTotalLag();
    if (groupLag != null && groupLag > 0) {
        lagConsumers++;
        totalLag += groupLag;
    }
}

Map<String, Object> data = new HashMap<>();
data.put("consumerGroupCount", consumerGroupIds.size());
data.put("activeConsumers", activeConsumers);
data.put("lagConsumers", lagConsumers);
data.put("totalLag", totalLag);
data.put("avgLag", consumerGroups.isEmpty() ? 0 : totalLag / consumerGroups.size());

result.setResult(String.format("消费者监控完成，共监控%d个消费者组，%d个活跃消费者，%d个存在延迟，总延迟%d条消息", 
    consumerGroupIds.size(), activeConsumers, lagConsumers, totalLag));
```

#### 1.3 集群监控任务修复

**修复前（模拟数据）：**
```java
// 模拟集群监控逻辑
Thread.sleep(1000); // 模拟执行时间

Map<String, Object> data = new HashMap<>();
data.put("brokerCount", 3);
data.put("onlineBrokers", 3);
data.put("offlineBrokers", 0);
data.put("totalPartitions", 180);
data.put("underReplicatedPartitions", 0);

result.setResult("集群监控完成，3个Broker全部在线，180个分区，无复制不足的分区");
```

**修复后（真实数据）：**
```java
// 从数据库获取Broker信息
List<org.kafka.eagle.dto.broker.BrokerInfo> dbBrokers = brokerMapper.queryAllBrokers();

// 获取所有Broker ID（参数化API）
List<Integer> brokerIds = KafkaServiceProxy.getAllBrokerIds();

// 获取Broker详细信息（参数化API）
List<BrokerDetailedInfo> brokerInfos = KafkaServiceProxy.getAllBrokerDetailedInfo(dbBrokers);

// 获取集群摘要信息（参数化API）
Map<String, Object> clusterSummary = KafkaServiceProxy.getClusterSummary();

// 计算统计信息
int onlineBrokers = 0;
int offlineBrokers = 0;
long totalPartitions = 0;
long underReplicatedPartitions = 0;

for (BrokerDetailedInfo broker : brokerInfos) {
    if ("ONLINE".equals(broker.getStatus())) {
        onlineBrokers++;
    } else {
        offlineBrokers++;
    }
}

// 从集群摘要中获取分区信息
if (clusterSummary != null) {
    totalPartitions = ((Number) clusterSummary.getOrDefault("totalPartitions", 0)).longValue();
    underReplicatedPartitions = ((Number) clusterSummary.getOrDefault("underReplicatedPartitions", 0)).longValue();
}

Map<String, Object> data = new HashMap<>();
data.put("brokerCount", brokerIds.size());
data.put("onlineBrokers", onlineBrokers);
data.put("offlineBrokers", offlineBrokers);
data.put("totalPartitions", totalPartitions);
data.put("underReplicatedPartitions", underReplicatedPartitions);
data.put("clusterSummary", clusterSummary);

result.setResult(String.format("集群监控完成，%d个Broker，%d个在线，%d个离线，%d个分区，%d个复制不足的分区", 
    brokerIds.size(), onlineBrokers, offlineBrokers, totalPartitions, underReplicatedPartitions));
```

#### 1.4 性能统计任务修复

**修复前（模拟数据）：**
```java
// 模拟性能统计逻辑
Thread.sleep(2500); // 模拟执行时间

Map<String, Object> data = new HashMap<>();
data.put("avgThroughput", 12500);
data.put("avgLatency", 15);
data.put("peakThroughput", 18000);
data.put("totalMessages", 2500000);

result.setResult("性能统计完成，平均吞吐量12500 msg/s，平均延迟15ms，峰值吞吐量18000 msg/s");
```

**修复后（真实数据）：**
```java
// 获取JMX性能指标
String jmxUri = getJmxUriFromTask(task);
Map<String, Object> performanceData = new HashMap<>();

if (jmxUri != null && !jmxUri.trim().isEmpty()) {
    try {
        // 获取JMX性能指标
        performanceData = collectJmxPerformanceMetrics(jmxUri);
    } catch (Exception e) {
        log.warn("获取JMX性能指标失败: {}", e.getMessage());
    }
}

// 获取主题统计信息作为性能指标的一部分（参数化API）
List<org.kafka.eagle.dto.broker.BrokerInfo> dbBrokers = brokerMapper.queryAllBrokers();
List<String> topicNames = KafkaServiceProxy.getAllTopicNames();
List<TopicDetailedStats> topicStats = KafkaServiceProxy.getTopicsDetailedStats(topicNames, dbBrokers);
long totalMessages = 0;
for (TopicDetailedStats stats : topicStats) {
    totalMessages += stats.getTotalRecords();
}

Map<String, Object> data = new HashMap<>();
data.put("totalMessages", totalMessages);
data.put("topicCount", topicStats.size());
data.put("jmxMetrics", performanceData);
data.put("collectTime", java.time.LocalDateTime.now());

result.setResult(String.format("性能统计完成，总消息数%d条，主题数%d个", totalMessages, topicStats.size()));
```

### 2. 新增依赖和导入

添加了必要的导入语句：

```java
import org.kafka.eagle.core.kafka.proxy.KafkaServiceProxy;
import org.kafka.eagle.core.kafka.jmx.JmxServiceProxy;
import org.kafka.eagle.dto.topic.TopicDetailedStats;
import org.kafka.eagle.dto.consumer.ConsumerGroupDetailedInfo;
import org.kafka.eagle.dto.broker.BrokerDetailedInfo;
import org.kafka.eagle.dto.broker.BrokerInfo; // 新增
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.concurrent.ExecutionException;
```

### 3. 增强日志记录

为每个任务执行方法添加了详细的日志记录：

```java
@Slf4j
@Service
public class TaskExecutorManager {
    
    // 在每个方法中添加日志
    log.info("开始执行主题监控任务");
    log.info("主题监控任务执行完成: {}个主题, {}个分区, {}条消息", 
        topicNames.size(), totalPartitions, totalRecords);
    log.error("主题监控任务执行失败: {}", e.getMessage(), e);
}
```

### 4. 错误处理优化

改进了异常处理，提供更详细的错误信息：

```java
} catch (Exception e) {
    log.error("任务执行异常: {}", e.getMessage(), e);
    result.setSuccess(false);
    result.setErrorMessage("任务执行异常：" + e.getMessage());
}
```

## 修复效果

### 1. 真实数据获取

- **主题监控**: 获取真实的主题数量、分区数、消息数、总大小
- **消费者监控**: 获取真实的消费者组数量、活跃消费者、延迟情况
- **集群监控**: 获取真实的Broker数量、在线状态、分区信息
- **性能统计**: 获取真实的JMX指标和主题统计信息

### 2. 数据准确性

- **实时性**: 每次任务执行都获取最新的集群状态
- **完整性**: 包含所有相关的统计指标
- **一致性**: 数据来源统一，避免不一致的问题

### 3. 可扩展性

- **模块化**: 每个任务类型独立实现
- **可配置**: 支持从任务配置中获取参数
- **可扩展**: 易于添加新的任务类型和指标

### 4. 监控和调试

- **详细日志**: 记录任务执行的每个步骤
- **错误追踪**: 提供详细的错误信息和堆栈跟踪
- **性能监控**: 记录任务执行时间和资源使用情况

## 技术细节

### 1. KafkaServiceProxy使用

```java
// 获取主题信息（参数化API）
List<org.kafka.eagle.dto.broker.BrokerInfo> dbBrokers = brokerMapper.queryAllBrokers();
List<String> topicNames = KafkaServiceProxy.getAllTopicNames();
List<TopicDetailedStats> topicStats = KafkaServiceProxy.getTopicsDetailedStats(topicNames, dbBrokers);

// 获取消费者组信息（参数化API）
List<String> consumerGroupIds = KafkaServiceProxy.getAllConsumerGroupIds();
List<ConsumerGroupDetailedInfo> consumerGroups = KafkaServiceProxy.getAllConsumerGroupDetailedInfo();

// 获取集群信息（参数化API）
List<Integer> brokerIds = KafkaServiceProxy.getAllBrokerIds();
List<BrokerDetailedInfo> brokerInfos = KafkaServiceProxy.getAllBrokerDetailedInfo(dbBrokers);
Map<String, Object> clusterSummary = KafkaServiceProxy.getClusterSummary();
```

### 2. 数据类型处理

```java
// 处理可能为null的Long类型
Long groupLag = group.getTotalLag();
if (groupLag != null && groupLag > 0) {
    lagConsumers++;
    totalLag += groupLag;
}

// 处理Number类型的转换
totalPartitions = ((Number) clusterSummary.getOrDefault("totalPartitions", 0)).longValue();
```

### 3. 异常处理

```java
try {
    // 获取JMX性能指标
    performanceData = collectJmxPerformanceMetrics(jmxUri);
} catch (Exception e) {
    log.warn("获取JMX性能指标失败: {}", e.getMessage());
}
```

## 相关文件

### 1. 修改的文件

- `efak-web/src/main/java/org/kafka/eagle/web/service/TaskExecutorManager.java`
  - 将所有模拟数据替换为真实数据
  - 添加详细的日志记录
  - 改进错误处理

### 2. 依赖的服务

- `efak-core/src/main/java/org/kafka/eagle/core/kafka/proxy/KafkaServiceProxy.java`
  - 提供Kafka集群数据访问接口
- `efak-core/src/main/java/org/kafka/eagle/core/kafka/jmx/JmxServiceProxy.java`
  - 提供JMX性能指标访问接口

### 3. 相关的DTO类

- `efak-dto/src/main/java/org/kafka/eagle/dto/topic/TopicDetailedStats.java`
- `efak-dto/src/main/java/org/kafka/eagle/dto/consumer/ConsumerGroupDetailedInfo.java`
- `efak-dto/src/main/java/org/kafka/eagle/dto/broker/BrokerDetailedInfo.java`

## 总结

通过这次修复，任务调度系统现在能够：

1. **获取真实数据**: 从Kafka集群获取实时的主题、消费者、Broker信息
2. **提供准确统计**: 计算真实的监控指标和性能数据
3. **支持实时监控**: 每次任务执行都反映最新的集群状态
4. **增强可观测性**: 通过详细日志记录任务执行过程
5. **改进错误处理**: 提供更好的异常处理和错误信息

这确保了任务执行历史记录中显示的是真实的Kafka集群状态，而不是模拟数据，提高了系统的可信度和实用性。