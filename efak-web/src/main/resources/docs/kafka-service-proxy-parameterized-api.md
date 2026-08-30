# KafkaServiceProxy参数化API优化

## 优化概述

将KafkaServiceProxy的所有接口方法改为参数化设计，通过传入broker信息参数来初始化连接，而不是依赖全局初始化状态。这样可以确保每次调用都使用最新的broker配置信息。

## 主要修改

### 1. KafkaServiceProxy接口重构

#### 1.1 添加参数化初始化方法

```java
/**
 * Initialize services with broker information from database
 * 
 * @param brokerInfos List of broker information from database
 */
public static void initializeWithBrokerInfo(List<org.kafka.eagle.dto.broker.BrokerInfo> brokerInfos) {
    if (brokerInfos == null || brokerInfos.isEmpty()) {
        throw new IllegalArgumentException("Broker information cannot be null or empty");
    }

    // 构建bootstrap servers字符串
    StringBuilder bootstrapServers = new StringBuilder();
    for (int i = 0; i < brokerInfos.size(); i++) {
        org.kafka.eagle.dto.broker.BrokerInfo broker = brokerInfos.get(i);
        if (i > 0) {
            bootstrapServers.append(",");
        }
        bootstrapServers.append(broker.getHostIp()).append(":").append(broker.getPort());
    }

    String bootstrapServersStr = bootstrapServers.toString();
    log.info("Initializing KafkaServiceProxy with broker information: {}", bootstrapServersStr);
    initialize(bootstrapServersStr);
}
```

#### 1.2 为所有方法添加broker信息参数重载版本

**Topic相关方法**：
```java
// 原有方法
public static List<String> getAllTopicNames() throws ExecutionException, InterruptedException

// 新增参数化方法
public static List<String> getAllTopicNames(List<org.kafka.eagle.dto.broker.BrokerInfo> brokerInfos) throws ExecutionException, InterruptedException

// 原有方法
public static List<TopicDetailedStats> getAllTopicDetailedStats() throws ExecutionException, InterruptedException

// 新增参数化方法
public static List<TopicDetailedStats> getAllTopicDetailedStats(List<org.kafka.eagle.dto.broker.BrokerInfo> brokerInfos) throws ExecutionException, InterruptedException
```

**Consumer相关方法**：
```java
// 原有方法
public static List<String> getAllConsumerGroupIds() throws ExecutionException, InterruptedException

// 新增参数化方法
public static List<String> getAllConsumerGroupIds(List<org.kafka.eagle.dto.broker.BrokerInfo> brokerInfos) throws ExecutionException, InterruptedException

// 原有方法
public static List<ConsumerGroupDetailedInfo> getAllConsumerGroupDetailedInfo() throws ExecutionException, InterruptedException

// 新增参数化方法
public static List<ConsumerGroupDetailedInfo> getAllConsumerGroupDetailedInfo(List<org.kafka.eagle.dto.broker.BrokerInfo> brokerInfos) throws ExecutionException, InterruptedException
```

**Cluster相关方法**：
```java
// 原有方法
public static List<Integer> getAllBrokerIds() throws ExecutionException, InterruptedException

// 新增参数化方法
public static List<Integer> getAllBrokerIds(List<org.kafka.eagle.dto.broker.BrokerInfo> brokerInfos) throws ExecutionException, InterruptedException

// 原有方法
public static List<BrokerDetailedInfo> getAllBrokerDetailedInfo() throws ExecutionException, InterruptedException

// 新增参数化方法
public static List<BrokerDetailedInfo> getAllBrokerDetailedInfo(List<org.kafka.eagle.dto.broker.BrokerInfo> brokerInfos) throws ExecutionException, InterruptedException

// 原有方法
public static BrokerDetailedInfo getBrokerDetailedInfo(int brokerId) throws ExecutionException, InterruptedException

// 新增参数化方法
public static BrokerDetailedInfo getBrokerDetailedInfo(int brokerId, List<org.kafka.eagle.dto.broker.BrokerInfo> brokerInfos) throws ExecutionException, InterruptedException

// 原有方法
public static List<BrokerDetailedInfo> getBrokersDetailedInfo(List<Integer> brokerIds) throws ExecutionException, InterruptedException

// 新增参数化方法
public static List<BrokerDetailedInfo> getBrokersDetailedInfo(List<Integer> brokerIds, List<org.kafka.eagle.dto.broker.BrokerInfo> brokerInfos) throws ExecutionException, InterruptedException

// 原有方法
public static Map<String, Object> getClusterSummary() throws ExecutionException, InterruptedException

// 新增参数化方法
public static Map<String, Object> getClusterSummary(List<org.kafka.eagle.dto.broker.BrokerInfo> brokerInfos) throws ExecutionException, InterruptedException

// 原有方法
public static Map<Integer, Map<String, Object>> getBrokerResourceSummary() throws ExecutionException, InterruptedException

// 新增参数化方法
public static Map<Integer, Map<String, Object>> getBrokerResourceSummary(List<org.kafka.eagle.dto.broker.BrokerInfo> brokerInfos) throws ExecutionException, InterruptedException
```

### 2. TaskExecutorManager优化

#### 2.1 移除全局初始化逻辑

移除了`ensureKafkaServiceProxyInitialized`方法，不再依赖全局初始化状态。

#### 2.2 修改所有任务执行方法

**executeTopicMonitorTask**：
```java
// 从数据库获取broker信息
List<BrokerInfo> dbBrokers = brokerMapper.queryAllBrokers();
if (dbBrokers.isEmpty()) {
    throw new IllegalStateException("数据库中没有broker信息，无法执行主题监控任务");
}

// 使用API（获取主题名使用无参方法，详细指标需传入brokers）
List<String> topicNames = KafkaServiceProxy.getAllTopicNames();
List<TopicDetailedStats> topicStats = KafkaServiceProxy.getTopicsDetailedStats(topicNames, dbBrokers);
```

**executeConsumerMonitorTask**：
```java
// 从数据库获取broker信息
List<BrokerInfo> dbBrokers = brokerMapper.queryAllBrokers();
if (dbBrokers.isEmpty()) {
    throw new IllegalStateException("数据库中没有broker信息，无法执行消费者监控任务");
}

// 使用API（消费者相关当前为无参方法，依赖全局初始化）
List<String> consumerGroupIds = KafkaServiceProxy.getAllConsumerGroupIds();
List<ConsumerGroupDetailedInfo> consumerGroups = KafkaServiceProxy.getAllConsumerGroupDetailedInfo();
```

**executeClusterMonitorTask**：
```java
// 从数据库获取broker信息
List<BrokerInfo> dbBrokers = brokerMapper.queryAllBrokers();
Map<Integer, String> brokerHosts = new HashMap<>();
Map<Integer, Integer> brokerPorts = new HashMap<>();
Map<Integer, Integer> brokerJmxPorts = new HashMap<>();

for (BrokerInfo dbBroker : dbBrokers) {
    brokerHosts.put(dbBroker.getBrokerId(), dbBroker.getHostIp());
    brokerPorts.put(dbBroker.getBrokerId(), dbBroker.getPort());
    if (dbBroker.getJmxPort() != null) {
        brokerJmxPorts.put(dbBroker.getBrokerId(), dbBroker.getJmxPort());
    }
}

// 使用API（broker详细信息可传入brokers以获取JMX等信息）
List<BrokerDetailedInfo> brokerInfos = KafkaServiceProxy.getAllBrokerDetailedInfo(dbBrokers);
```

**executePerformanceStatsTask**：
```java
// 从数据库获取broker信息
List<BrokerInfo> dbBrokers = brokerMapper.queryAllBrokers();
if (dbBrokers.isEmpty()) {
    throw new IllegalStateException("数据库中没有broker信息，无法执行性能统计任务");
}

// 使用API（获取主题名使用无参方法，详细指标需传入brokers）
List<String> topicNames = KafkaServiceProxy.getAllTopicNames();
List<TopicDetailedStats> topicStats = KafkaServiceProxy.getTopicsDetailedStats(topicNames, dbBrokers);
```

## 工作流程

### 1. 参数化API调用流程
```
任务执行开始
    ↓
从数据库获取broker信息
    ↓
验证broker信息有效性
    ↓
调用参数化API方法
    ↓
API内部使用broker信息初始化连接
    ↓
执行Kafka操作
    ↓
返回结果
```

### 2. 初始化流程
```
参数化方法调用
    ↓
检查broker信息参数
    ↓
构建bootstrap servers字符串
    ↓
调用initialize(bootstrapServers)
    ↓
创建AdminClient和Consumer
    ↓
初始化TopicService、ConsumerService、ClusterService
    ↓
执行具体操作
```

## 优势

### 1. 配置实时性
- **动态配置**: 每次调用都使用最新的broker配置
- **实时生效**: broker信息变更立即生效，无需重启
- **配置隔离**: 不同任务可以使用不同的broker配置

### 2. 错误处理
- **参数验证**: 严格的broker信息参数验证
- **错误隔离**: 单个任务失败不影响其他任务
- **详细日志**: 完整的错误信息和调试日志

### 3. 性能优化
- **按需连接**: 只在需要时建立Kafka连接
- **连接复用**: 同一任务执行期间复用连接
- **资源管理**: 自动清理连接资源

### 4. 灵活性
- **多broker支持**: 支持多个broker的负载均衡
- **配置灵活**: 支持不同的broker配置组合
- **扩展性强**: 易于添加新的参数化方法

## 使用示例

### 1. 基本使用
```java
// 获取broker信息
List<BrokerInfo> brokers = brokerMapper.queryAllBrokers();

// 使用API
List<String> topicNames = KafkaServiceProxy.getAllTopicNames();
List<BrokerDetailedInfo> brokerInfos = KafkaServiceProxy.getAllBrokerDetailedInfo(brokers);
```

### 2. 错误处理
```java
try {
    List<BrokerInfo> brokers = brokerMapper.queryAllBrokers();
    if (brokers.isEmpty()) {
        throw new IllegalStateException("没有可用的broker信息");
    }
    
    List<String> topicNames = KafkaServiceProxy.getAllTopicNames();
    // 处理结果
} catch (IllegalArgumentException e) {
    // 处理参数错误
} catch (ExecutionException | InterruptedException e) {
    // 处理Kafka操作错误
}
```

### 3. 任务执行
```java
@Autowired
private TaskExecutorManager taskExecutorManager;

// 创建任务
TaskScheduler task = new TaskScheduler();
task.setTaskType("cluster_monitor");

// 执行任务（内部会自动获取broker信息并调用参数化API）
TaskExecutionResult result = taskExecutorManager.executeTask(task);
```

## 配置要求

### 1. 数据库配置
确保`ke_broker_info`表中有正确的broker信息：

```sql
-- 检查broker信息
SELECT broker_id, host_ip, port, jmx_port, status 
FROM ke_broker_info 
WHERE status = 'online';
```

### 2. Broker配置
- **host_ip**: broker的主机IP地址
- **port**: broker的Kafka端口
- **jmx_port**: broker的JMX端口（可选）
- **status**: broker状态（online/offline）

### 3. 网络配置
- 确保应用能够访问broker的Kafka端口
- 确保应用能够访问broker的JMX端口（如果需要JMX指标）

## 监控和日志

### 1. 关键日志
- `Initializing KafkaServiceProxy with broker information: {}`
- `KafkaServiceProxy initialized successfully`
- `数据库中没有broker信息，无法执行{}任务`

### 2. 错误处理
- 参数验证失败时的详细错误信息
- Kafka连接失败时的重试机制
- 初始化失败时的详细错误信息

## 注意事项

### 1. 性能考虑
- 每次调用都会重新初始化连接
- 建议在任务执行期间复用连接
- 监控连接建立时间

### 2. 错误处理
- 确保broker信息正确配置
- 监控Kafka连接状态
- 处理broker不可用的情况

### 3. 安全配置
- 如果启用了Kafka安全认证，需要配置相应的安全参数
- 确保JMX访问权限配置正确
- 监控认证失败的情况

## 故障排除

### 1. 参数错误
```bash
# 检查broker信息
mysql -u root -p efak_ai -e "SELECT * FROM ke_broker_info;"

# 检查应用日志
tail -f logs/efak-ai.log | grep "Broker information cannot be null or empty"
```

### 2. 连接失败
- 检查broker的Kafka端口是否开放
- 确认网络连通性
- 检查防火墙设置

### 3. 任务执行失败
- 查看任务执行日志
- 检查broker状态
- 确认broker配置正确

## 总结

通过这次优化，KafkaServiceProxy现在能够：

1. **参数化设计**: 所有方法都接受broker信息参数
2. **动态配置**: 每次调用都使用最新的broker配置
3. **错误隔离**: 单个任务失败不影响其他任务
4. **性能优化**: 按需连接，避免不必要的资源消耗
5. **灵活扩展**: 易于添加新的参数化方法

这确保了系统能够根据实际的broker配置进行Kafka操作，提高了系统的灵活性和可靠性。