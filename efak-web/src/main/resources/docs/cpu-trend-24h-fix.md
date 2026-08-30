# CPU趋势接口24h时间范围修复报告

## 问题描述

用户报告 `/api/brokers/metrics/cpu-trend?timeRange=24h` 接口返回数据为空，尽管数据库表 `ke_broker_metrics` 中有记录。

## 问题分析

通过代码分析发现问题根源在于 `BrokerMetricsController.java` 中的 `calculateStartTime` 方法缺少对 `"24h"` 时间范围的处理。

### 原始代码问题

```java
private LocalDateTime calculateStartTime(LocalDateTime endTime, String timeRange) {
    switch (timeRange) {
        case "1h":
            return endTime.minusHours(1);
        case "6h":
            return endTime.minusHours(6);
        case "12h":
            return endTime.minusHours(12);
        case "1d":
            return endTime.minusDays(1);
        case "3d":
            return endTime.minusDays(3);
        case "7d":
            return endTime.minusDays(7);
        default:
            return endTime.minusHours(1); // 问题：24h会走到这里，只查询1小时数据
    }
}
```

### 问题影响

当用户请求 `timeRange=24h` 时：
1. `calculateStartTime` 方法无法匹配 `"24h"` case
2. 执行 `default` 分支，返回 `endTime.minusHours(1)`
3. 实际只查询了最近1小时的数据，而不是24小时
4. 如果最近1小时内没有数据，接口返回空结果

## 修复方案

在 `calculateStartTime` 方法中添加对 `"24h"` 时间范围的支持：

```java
private LocalDateTime calculateStartTime(LocalDateTime endTime, String timeRange) {
    switch (timeRange) {
        case "1h":
            return endTime.minusHours(1);
        case "6h":
            return endTime.minusHours(6);
        case "12h":
            return endTime.minusHours(12);
        case "24h":  // 新增：支持24小时时间范围
            return endTime.minusHours(24);
        case "1d":
            return endTime.minusDays(1);
        case "3d":
            return endTime.minusDays(3);
        case "7d":
            return endTime.minusDays(7);
        default:
            return endTime.minusHours(1);
    }
}
```

## 修复验证

### 测试代码

创建了测试程序验证修复效果：

```java
public class Test24hFix {
    public static void main(String[] args) {
        LocalDateTime endTime = LocalDateTime.now();
        String timeRange = "24h";
        
        LocalDateTime startTime = calculateStartTime(endTime, timeRange);
        
        System.out.println("End Time: " + endTime);
        System.out.println("Time Range: " + timeRange);
        System.out.println("Start Time: " + startTime);
        System.out.println("Time Difference (hours): " + 
            java.time.Duration.between(startTime, endTime).toHours());
    }
}
```

### 测试结果

```
End Time: 2025-08-03T20:12:53.921809
Time Range: 24h
Start Time: 2025-08-02T20:12:53.921809
Time Difference (hours): 24
```

✅ **验证成功**：24h时间范围现在能正确解析为24小时前的时间。

## 影响范围

### 受影响的接口

1. **CPU趋势接口**：`/api/brokers/metrics/cpu-trend?timeRange=24h`
2. **内存趋势接口**：`/api/brokers/metrics/memory-trend?timeRange=24h`

### 修复后的行为

- `timeRange=24h` 现在会正确查询最近24小时的数据
- 与 `timeRange=1d` 功能等效（都是24小时）
- 保持与其他时间范围的一致性

## 相关文件

- **修复文件**：`/Users/smartloli/workspace/EFAK-AI/efak-web/src/main/java/org/kafka/eagle/web/controller/BrokerMetricsController.java`
- **修复行数**：第178-195行的 `calculateStartTime` 方法

## 注意事项

1. **数据可用性**：修复后接口能正确查询24小时数据，但仍需确保数据库中有相应时间范围的数据
2. **性能考虑**：24小时数据量较大，建议在生产环境中监控查询性能
3. **一致性**：`24h` 和 `1d` 现在功能相同，都查询24小时数据

## 预防措施

1. **单元测试**：建议为 `calculateStartTime` 方法添加单元测试，覆盖所有时间范围
2. **文档更新**：更新API文档，明确支持的时间范围参数
3. **代码审查**：在添加新的时间范围时，确保在所有相关方法中都有对应的处理逻辑

## 总结

此次修复解决了 `/api/brokers/metrics/cpu-trend?timeRange=24h` 接口返回空数据的问题。问题根源是时间范围解析逻辑缺少对 `"24h"` 的支持，导致实际只查询1小时数据。通过在 `calculateStartTime` 方法中添加 `case "24h"` 分支，现在能正确处理24小时时间范围的查询请求。

修复已通过测试验证，确保24小时时间范围能正确解析为24小时前的时间点。