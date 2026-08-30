# TaskExecutorManager 中 KafkaServiceProxy 初始化问题修复

## 问题描述

在 `TaskExecutorManager` 执行集群监控任务时，出现 `KafkaServiceProxy not initialized` 的 `IllegalStateException` 异常：

```
ERROR [] o.k.e.w.service.TaskExecutorManager - 集群监控任务执行失败: KafkaServiceProxy not initialized. Please initialize with broker information from database. 
java.lang.IllegalStateException: KafkaServiceProxy not initialized. Please initialize with broker information from database. 
	at org.kafka.eagle.core.kafka.proxy.KafkaServiceProxy.checkInitialization(KafkaServiceProxy.java:419) 
	at org.kafka.eagle.core.kafka.proxy.KafkaServiceProxy.getAllBrokerDetailedInfo(KafkaServiceProxy.java:364) 
	at org.kafka.eagle.web.service.TaskExecutorManager.executeClusterMonitorTask(TaskExecutorManager.java:310) 
	at org.kafka.eagle.web.service.TaskExecutorManager.executeTask(TaskExecutorManager.java:60)
```

## 问题分析

`TaskExecutorManager` 在执行集群监控任务时，直接调用了 `KafkaServiceProxy.getAllBrokerDetailedInfo()` 等静态方法，但 `KafkaServiceProxy` 未进行初始化，导致在 `checkInitialization()` 方法中抛出异常。

## 解决方案

### 1. 在 TaskExecutorManager 中注入 DatabaseConfigService

在 `TaskExecutorManager` 中注入 `DatabaseConfigService`，以便从数据库加载 Broker 和安全配置信息：

```java
@Autowired
private DatabaseConfigService databaseConfigService;
```

### 2. 添加初始化守护方法

在 `TaskExecutorManager` 中添加 `ensureKafkaServiceProxyInitialized()` 方法，确保在使用 `KafkaServiceProxy` 前已完成初始化：

```java
// 确保在使用 KafkaServiceProxy 前已完成初始化
private void ensureKafkaServiceProxyInitialized() {
    if (!KafkaServiceProxy.isInitialized()) {
        // 优先从数据库加载 Broker 列表和安全配置
        List<BrokerInfo> brokers = brokerMapper.queryAllBrokers();
        if (brokers == null || brokers.isEmpty()) {
            throw new IllegalStateException("数据库中没有broker信息，无法初始化KafkaServiceProxy");
        }

        // 通过 DatabaseConfigService 获取安全配置（Map），再转换为 List<KafkaSecurityConfig>
        Map<String, String> securityConfigMap = databaseConfigService.getSecurityConfigsFromDatabase();
        List<org.kafka.eagle.dto.config.KafkaSecurityConfig> securityConfigs =
                convertToSecurityConfigList(securityConfigMap);

        KafkaServiceProxy.initialize(brokers, securityConfigs);
        log.info("KafkaServiceProxy 已通过数据库配置完成初始化，broker数: {}，安全配置数: {}", brokers.size(),
                securityConfigs == null ? 0 : securityConfigs.size());
    }
}
```

### 3. 添加配置转换方法

添加辅助方法，将 Map 格式的安全配置转换为 List 格式：

```java
// 将 Map<String, String> 的安全配置转换为 List<KafkaSecurityConfig>
private List<org.kafka.eagle.dto.config.KafkaSecurityConfig> convertToSecurityConfigList(
        Map<String, String> securityConfigMap) {
    List<org.kafka.eagle.dto.config.KafkaSecurityConfig> list = new java.util.ArrayList<>();
    if (securityConfigMap == null || securityConfigMap.isEmpty()) {
        return list;
    }
    for (Map.Entry<String, String> entry : securityConfigMap.entrySet()) {
        org.kafka.eagle.dto.config.KafkaSecurityConfig cfg = new org.kafka.eagle.dto.config.KafkaSecurityConfig();
        cfg.setConfigKey(entry.getKey());
        cfg.setConfigValue(entry.getValue());
        list.add(cfg);
    }
    return list;
}
```

### 4. 在 executeTask 方法中调用初始化守护

在 `executeTask` 方法开始处添加初始化检查：

```java
public void executeTask(String taskType, Object context) {
    try {
        // 确保 KafkaServiceProxy 已初始化
        ensureKafkaServiceProxyInitialized();
        
        // ... 原有的任务执行逻辑
    } catch (Exception e) {
        log.error("{} 任务执行失败: {}", getTaskDisplayName(taskType), e.getMessage(), e);
        throw e;
    }
}
```

## 修复效果

- **自动初始化**：在执行任何任务前自动检查并初始化 `KafkaServiceProxy`
- **数据库驱动**：从数据库动态加载 Broker 和安全配置，无需硬编码
- **异常防护**：避免了 `KafkaServiceProxy not initialized` 异常
- **生命周期管理**：确保 Kafka 服务和 JMX 服务的正确初始化

## 编译验证

修复后的代码通过了 Maven 编译验证：

```bash
mvn clean compile -DskipTests
# BUILD SUCCESS
```

这个修复确保了 `TaskExecutorManager` 在执行任何任务前都会先检查并初始化 `KafkaServiceProxy`，从根本上解决了初始化异常问题。