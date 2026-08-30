# formatChartData函数字段映射修复报告

## 问题描述

在`BrokerMetricsController.java`的`formatChartData`函数中，字段解析逻辑与数据库实际返回的记录格式不匹配，导致数据解析失败。

## 数据库记录格式

实际数据库返回的记录格式为：
```
{collectTime=2025-08-03T18:11:09, brokerId=1, hostIp=127.0.0.1, port=9092, value=0.10}
```

## 问题分析

原代码中的字段映射存在以下问题：
1. 使用`broker_id`字段获取Broker ID，但实际字段名为`brokerId`
2. 使用`metricType + "_usage"`（如`cpu_usage`、`memory_usage`）获取指标值，但实际字段名为`value`

## 修复方案

### 修改前的代码
```java
// 安全获取broker_id
Object brokerIdObj = data.get("broker_id");
if (brokerIdObj == null) {
    log.warn("数据中缺少broker_id字段，跳过该条记录: {}", data);
    continue;
}

// 安全获取指标值
Object valueObj = data.get(metricType + "_usage");
if (valueObj == null) {
    log.warn("数据中缺少{}字段，跳过该条记录: {}", metricType + "_usage", data);
    continue;
}
```

### 修改后的代码
```java
// 安全获取brokerId
Object brokerIdObj = data.get("brokerId");
if (brokerIdObj == null) {
    log.warn("数据中缺少brokerId字段，跳过该条记录: {}", data);
    continue;
}

// 安全获取指标值
Object valueObj = data.get("value");
if (valueObj == null) {
    log.warn("数据中缺少value字段，跳过该条记录: {}", data);
    continue;
}
```

## 修复内容

1. **Broker ID字段映射**：将`broker_id`改为`brokerId`
2. **指标值字段映射**：将`metricType + "_usage"`改为`value`
3. **日志信息更新**：相应更新了错误日志中的字段名称

## 影响范围

- **文件**：`BrokerMetricsController.java`
- **方法**：`formatChartData`
- **接口**：
  - `/api/brokers/metrics/cpu-trend`
  - `/api/brokers/metrics/memory-trend`

## 验证结果

- ✅ 代码编译成功
- ✅ 字段映射与数据库记录格式匹配
- ✅ 日志信息准确反映实际字段名

## 注意事项

1. 确保数据库查询返回的数据格式与修复后的字段映射一致
2. 如果数据库表结构发生变化，需要相应调整字段映射
3. 建议在Service层统一数据格式，避免Controller层直接处理数据库字段映射

## 预防措施

1. 在DTO类中明确定义字段映射关系
2. 使用常量定义字段名，避免硬编码
3. 添加单元测试验证数据解析逻辑
4. 建立数据格式文档，确保前后端字段映射一致性

---

**修复时间**：2025-08-03  
**修复人员**：AI Assistant  
**状态**：已完成