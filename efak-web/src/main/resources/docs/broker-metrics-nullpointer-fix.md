# BrokerMetricsController NullPointerException 修复文档

## 问题描述

在 `BrokerMetricsController` 的 `formatChartData` 方法中出现 `NullPointerException` 异常：

```
java.lang.NullPointerException: Cannot invoke "Object.toString()" because the return value of "java.util.Map.get(Object)" is null
    at org.kafka.eagle.web.controller.BrokerMetricsController.formatChartData(BrokerMetricsController.java:207)
```

## 问题原因

在 `formatChartData` 方法中，代码直接调用 `Map.get().toString()` 方法，但没有检查 `Map.get()` 返回的值是否为 `null`。当数据库查询结果中某些字段为空时，就会抛出 `NullPointerException`。

具体问题出现在以下几行代码：

```java
String timeLabel = data.get("time_label").toString();           // 第207行
String brokerId = data.get("broker_id").toString();             // 第208行  
Double value = Double.parseDouble(data.get(metricType + "_usage").toString()); // 第209行
```

## 修复方案

### 1. 添加空值检查

对每个从 Map 中获取的值都进行空值检查，避免直接调用 `toString()` 方法：

```java
// 安全获取time_label
Object timeLabelObj = data.get("time_label");
if (timeLabelObj == null) {
    log.warn("数据中缺少time_label字段，跳过该条记录: {}", data);
    continue;
}
String timeLabel = timeLabelObj.toString();
```

### 2. 添加数据类型转换异常处理

对数值类型的转换添加异常处理：

```java
Double value;
try {
    value = Double.parseDouble(valueObj.toString());
} catch (NumberFormatException e) {
    log.warn("无法解析{}字段的值: {}，跳过该条记录", metricType + "_usage", valueObj);
    continue;
}
```

### 3. 增强日志记录

添加详细的警告日志，帮助定位数据问题：

- 记录缺少字段的具体信息
- 记录数据转换失败的详细信息
- 跳过有问题的记录而不是让整个方法失败

## 修复后的代码

```java
private Map<String, Object> formatChartData(List<Map<String, Object>> trendData, String metricType) {
    Map<String, Object> chartData = new HashMap<>();
    List<String> labels = new ArrayList<>();
    Map<String, List<Double>> brokerDataMap = new HashMap<>();

    // 处理数据
    for (Map<String, Object> data : trendData) {
        // 安全获取time_label
        Object timeLabelObj = data.get("time_label");
        if (timeLabelObj == null) {
            log.warn("数据中缺少time_label字段，跳过该条记录: {}", data);
            continue;
        }
        String timeLabel = timeLabelObj.toString();
        
        // 安全获取broker_id
        Object brokerIdObj = data.get("broker_id");
        if (brokerIdObj == null) {
            log.warn("数据中缺少broker_id字段，跳过该条记录: {}", data);
            continue;
        }
        String brokerId = brokerIdObj.toString();
        
        // 安全获取指标值
        Object valueObj = data.get(metricType + "_usage");
        if (valueObj == null) {
            log.warn("数据中缺少{}字段，跳过该条记录: {}", metricType + "_usage", data);
            continue;
        }
        
        Double value;
        try {
            value = Double.parseDouble(valueObj.toString());
        } catch (NumberFormatException e) {
            log.warn("无法解析{}字段的值: {}，跳过该条记录", metricType + "_usage", valueObj);
            continue;
        }

        if (!labels.contains(timeLabel)) {
            labels.add(timeLabel);
        }

        brokerDataMap.computeIfAbsent("Broker " + brokerId, k -> new ArrayList<>()).add(value);
    }
    
    // ... 其余代码保持不变
}
```

## 影响范围

此修复影响以下API接口：

1. `/api/brokers/metrics/cpu-trend` - CPU趋势数据接口
2. `/api/brokers/metrics/memory-trend` - 内存趋势数据接口

## 测试建议

1. **正常数据测试**：确保正常的监控数据能够正确处理
2. **空值数据测试**：模拟数据库中存在空值的情况
3. **异常数据测试**：模拟数据类型转换失败的情况
4. **边界条件测试**：测试空列表、单条记录等边界情况

## 预防措施

1. **代码审查**：在类似的数据处理代码中都应该添加空值检查
2. **数据验证**：在数据入库时就应该进行数据完整性验证
3. **单元测试**：为数据处理方法编写完整的单元测试，覆盖各种异常情况

## 修复时间

- 修复日期：2025-08-03
- 修复版本：5.0.0
- 修复人员：AI Assistant