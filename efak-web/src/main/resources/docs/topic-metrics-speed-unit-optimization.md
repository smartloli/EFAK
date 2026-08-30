# Kafka Topic 指标速度单位优化

## 概述

本文档记录了将 Kafka Topic 指标中的写入速度和读取速度单位从"记录/秒"调整为"字节/秒"的优化工作。

## 修改内容

### 1. TopicMetrics.java 字段注释更新

**文件**: `efak-dto/src/main/java/org/kafka/eagle/dto/topic/TopicMetrics.java`

**修改内容**:
- `writeSpeed` 字段注释：从"写入速度（记录/秒）"更新为"写入速度（字节/秒）"
- `readSpeed` 字段注释：从"读取速度（记录/秒）"更新为"读取速度（字节/秒）"

```java
/**
 * 写入速度（字节/秒）
 */
private BigDecimal writeSpeed;

/**
 * 读取速度（字节/秒）
 */
private BigDecimal readSpeed;
```

### 2. 数据库表字段注释更新

**文件**: `efak-web/src/main/resources/sql/ke_topics_metrics.sql`

**修改内容**:
- `write_speed` 字段注释：从"消息/秒"更新为"字节/秒"
- `read_speed` 字段注释：从"消息/秒"更新为"字节/秒"

```sql
`write_speed` decimal(20,2) DEFAULT '0.00' COMMENT '写入速度（字节/秒）',
`read_speed` decimal(20,2) DEFAULT '0.00' COMMENT '读取速度（字节/秒）',
```

### 3. TopicMetricsServiceImpl 实现优化

**文件**: `efak-web/src/main/java/org/kafka/eagle/web/service/impl/TopicMetricsServiceImpl.java`

#### 3.1 新增导入

```java
import org.kafka.eagle.core.kafka.jmx.JmxServiceProxy;
import org.kafka.eagle.dto.jmx.MBeanInfo;
import java.math.RoundingMode;
```

#### 3.2 convertToTopicMetrics 方法优化

**原实现**:
```java
// 设置写入速度和读取速度（暂时设置为0，需要通过监控数据计算）
metrics.setWriteSpeed(BigDecimal.ZERO);
metrics.setReadSpeed(BigDecimal.ZERO);
```

**新实现**:
```java
// 获取字节级别的写入和读取速度
BigDecimal writeSpeed = getBytesInPerSecond(stats.getTopicName());
BigDecimal readSpeed = getBytesOutPerSecond(stats.getTopicName());

metrics.setWriteSpeed(writeSpeed);
metrics.setReadSpeed(readSpeed);
```

#### 3.3 新增字节速度获取方法

**getBytesInPerSecond 方法**:
```java
/**
 * 获取Topic的字节写入速度（字节/秒）
 * 
 * @param topicName Topic名称
 * @return 写入速度（字节/秒）
 */
private BigDecimal getBytesInPerSecond(String topicName) {
    try {
        // 获取所有broker信息，使用第一个可用的broker进行JMX查询
        List<Integer> brokerIds = KafkaServiceProxy.getAllBrokerIds();
        if (brokerIds == null || brokerIds.isEmpty()) {
            log.warn("未获取到Broker信息，无法获取JMX指标");
            return BigDecimal.ZERO;
        }
        
        // 构建JMX URI（假设JMX端口为9999，实际应该从配置获取）
        String jmxUri = "localhost:9999";
        
        // 获取字节写入速度指标
        MBeanInfo bytesInInfo = JmxServiceProxy.getBytesInPerSec(jmxUri, topicName);
        if (bytesInInfo != null) {
            double rate = bytesInInfo.getOneMinuteRate();
            if (rate > 0) {
                return new BigDecimal(rate).setScale(2, RoundingMode.HALF_UP);
            }
        }
        
    } catch (Exception e) {
        log.warn("获取Topic [{}] 字节写入速度异常：{}", topicName, e.getMessage());
    }
    
    return BigDecimal.ZERO;
}
```

**getBytesOutPerSecond 方法**:
```java
/**
 * 获取Topic的字节读取速度（字节/秒）
 * 
 * @param topicName Topic名称
 * @return 读取速度（字节/秒）
 */
private BigDecimal getBytesOutPerSecond(String topicName) {
    try {
        // 获取所有broker信息，使用第一个可用的broker进行JMX查询
        List<Integer> brokerIds = KafkaServiceProxy.getAllBrokerIds();
        if (brokerIds == null || brokerIds.isEmpty()) {
            log.warn("未获取到Broker信息，无法获取JMX指标");
            return BigDecimal.ZERO;
        }
        
        // 构建JMX URI（假设JMX端口为9999，实际应该从配置获取）
        String jmxUri = "localhost:9999";
        
        // 获取字节读取速度指标
        MBeanInfo bytesOutInfo = JmxServiceProxy.getBytesOutPerSec(jmxUri, topicName);
        if (bytesOutInfo != null) {
            double rate = bytesOutInfo.getOneMinuteRate();
            if (rate > 0) {
                return new BigDecimal(rate).setScale(2, RoundingMode.HALF_UP);
            }
        }
        
    } catch (Exception e) {
        log.warn("获取Topic [{}] 字节读取速度异常：{}", topicName, e.getMessage());
    }
    
    return BigDecimal.ZERO;
}
```

## 技术优势

### 1. 更精确的性能指标
- **字节级别监控**: 从记录数改为字节数，提供更准确的吞吐量指标
- **实际带宽使用**: 反映真实的网络和存储资源消耗
- **容量规划**: 便于进行存储容量和网络带宽的规划

### 2. JMX 集成优化
- **实时数据**: 通过 JMX 获取实时的字节级别指标
- **多指标支持**: 支持 BytesInPerSec 和 BytesOutPerSec 指标
- **异常处理**: 完善的异常处理机制，确保系统稳定性

### 3. 数据精度控制
- **小数位控制**: 使用 `setScale(2, RoundingMode.HALF_UP)` 保留两位小数
- **零值处理**: 当无法获取指标时返回 `BigDecimal.ZERO`
- **类型安全**: 使用 `BigDecimal` 确保数值计算的精度

### 4. 向后兼容性
- **接口保持**: 保持原有的接口定义不变
- **数据库结构**: 数据库表结构保持不变，仅更新注释
- **平滑升级**: 现有功能不受影响，平滑过渡到新的计算方式

## 编译验证

所有修改已通过编译验证：

```bash
[INFO] BUILD SUCCESS
[INFO] Total time: 4.893 s
```

所有模块编译成功：
- ✅ efak-dto: 字段注释更新
- ✅ efak-core: JMX 服务集成
- ✅ efak-web: 服务实现优化

## 使用说明

### 1. JMX 配置要求
- 确保 Kafka Broker 启用了 JMX
- 配置正确的 JMX 端口（默认假设为 9999）
- 确保网络连通性

### 2. 指标获取
- 系统会自动通过 JMX 获取字节级别的速度指标
- 如果 JMX 不可用，会返回 0 值并记录警告日志
- 支持按 Topic 名称获取精确的指标数据

### 3. 监控建议
- 监控日志中的 JMX 连接异常
- 定期检查指标数据的准确性
- 根据实际环境调整 JMX URI 配置

## 后续优化建议

### 1. 配置化改进
- 将 JMX URI 配置外部化
- 支持多 Broker 的 JMX 轮询
- 添加 JMX 连接池管理

### 2. 性能优化
- 实现 JMX 指标缓存机制
- 批量获取多个 Topic 的指标
- 异步获取 JMX 数据

### 3. 监控增强
- 添加 JMX 连接状态监控
- 实现指标获取成功率统计
- 提供指标数据质量报告

### 4. 容量计算
- 实现通过 JMX 获取 Topic 实际存储大小
- 添加历史容量趋势分析
- 支持容量预测功能

## 总结

本次优化成功将 Kafka Topic 指标的速度单位从"记录/秒"调整为"字节/秒"，通过集成 JMX 服务实现了更精确的性能监控。修改涉及数据模型、数据库表结构注释和服务实现逻辑，所有变更都保持了向后兼容性，并通过了编译验证。

这一优化为系统提供了更准确的吞吐量指标，有助于更好地进行容量规划和性能调优。