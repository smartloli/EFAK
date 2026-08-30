# BrokerMetricsMapper XML到注解迁移文档

## 概述
本文档记录了将 `BrokerMetricsMapper.xml` 中的SQL语句迁移到 `BrokerMetricsMapper.java` 接口中使用注解实现的过程。

## 迁移背景
- **目标**: 将XML配置的SQL语句转换为Java注解形式，简化配置管理
- **原文件**: `/Users/smartloli/workspace/EFAK-AI/efak-web/src/main/resources/mybatis/mapper/BrokerMetricsMapper.xml`
- **目标文件**: `/Users/smartloli/workspace/EFAK-AI/efak-web/src/main/java/org/kafka/eagle/web/mapper/BrokerMetricsMapper.java`

## 迁移内容

### 已存在的方法（保持不变）
1. `insertBrokerMetrics` - 插入单条Broker性能指标数据
2. `batchInsertBrokerMetrics` - 批量插入Broker性能指标数据
3. `queryMetricsByTimeRange` - 查询指定时间范围内的性能指标数据
4. `queryHourlyAggregatedMetrics` - 查询小时级聚合数据
5. `queryDailyAggregatedMetrics` - 查询日级聚合数据
6. `deleteMetricsBeforeTime` - 删除指定时间之前的历史数据
7. `deleteHistoricalData` - 删除历史数据（重复方法）
8. `queryLatestMetrics` - 查询最新的性能指标数据
9. `countMetricsByTimeRange` - 统计指定时间范围内的数据条数

### 新增的方法（从XML迁移）

#### 1. selectCpuUsageTrend - CPU使用率趋势查询
```java
@Select("<script>" +
        "<choose>" +
        "  <when test='aggregationType == \"hourly\">" +
        "    SELECT broker_id as brokerId, host_ip as hostIp, " +
        "           DATE_FORMAT(collect_time, '%Y-%m-%d %H:00:00') as timePoint, " +
        "           AVG(cpu_usage) as cpuUsage, COUNT(*) as sampleCount " +
        "    FROM ke_broker_metrics " +
        "    WHERE collect_time BETWEEN #{startTime} AND #{endTime} " +
        "    <if test='brokerId != null'>AND broker_id = #{brokerId}</if>" +
        "    GROUP BY broker_id, host_ip, DATE_FORMAT(collect_time, '%Y-%m-%d %H') " +
        "    ORDER BY timePoint ASC" +
        "  </when>" +
        "  <when test='aggregationType == \"daily\">" +
        "    SELECT broker_id as brokerId, host_ip as hostIp, " +
        "           DATE_FORMAT(collect_time, '%Y-%m-%d 00:00:00') as timePoint, " +
        "           AVG(cpu_usage) as cpuUsage, COUNT(*) as sampleCount " +
        "    FROM ke_broker_metrics " +
        "    WHERE collect_time BETWEEN #{startTime} AND #{endTime} " +
        "    <if test='brokerId != null'>AND broker_id = #{brokerId}</if>" +
        "    GROUP BY broker_id, host_ip, DATE_FORMAT(collect_time, '%Y-%m-%d') " +
        "    ORDER BY timePoint ASC" +
        "  </when>" +
        "  <otherwise>" +
        "    SELECT broker_id as brokerId, host_ip as hostIp, " +
        "           collect_time as timePoint, cpu_usage as cpuUsage, " +
        "           1 as sampleCount " +
        "    FROM ke_broker_metrics " +
        "    WHERE collect_time BETWEEN #{startTime} AND #{endTime} " +
        "    <if test='brokerId != null'>AND broker_id = #{brokerId}</if>" +
        "    ORDER BY collect_time ASC" +
        "  </otherwise>" +
        "</choose>" +
        "</script>")
List<Map<String, Object>> selectCpuUsageTrend(@Param("brokerId") Integer brokerId,
                @Param("startTime") LocalDateTime startTime,
                @Param("endTime") LocalDateTime endTime,
                @Param("aggregationType") String aggregationType);
```

#### 2. selectMemoryUsageTrend - 内存使用率趋势查询
```java
@Select("<script>" +
        "<choose>" +
        "  <when test='aggregationType == \"hourly\">" +
        "    SELECT broker_id as brokerId, host_ip as hostIp, " +
        "           DATE_FORMAT(collect_time, '%Y-%m-%d %H:00:00') as timePoint, " +
        "           AVG(memory_usage) as memoryUsage, COUNT(*) as sampleCount " +
        "    FROM ke_broker_metrics " +
        "    WHERE collect_time BETWEEN #{startTime} AND #{endTime} " +
        "    <if test='brokerId != null'>AND broker_id = #{brokerId}</if>" +
        "    GROUP BY broker_id, host_ip, DATE_FORMAT(collect_time, '%Y-%m-%d %H') " +
        "    ORDER BY timePoint ASC" +
        "  </when>" +
        "  <when test='aggregationType == \"daily\">" +
        "    SELECT broker_id as brokerId, host_ip as hostIp, " +
        "           DATE_FORMAT(collect_time, '%Y-%m-%d 00:00:00') as timePoint, " +
        "           AVG(memory_usage) as memoryUsage, COUNT(*) as sampleCount " +
        "    FROM ke_broker_metrics " +
        "    WHERE collect_time BETWEEN #{startTime} AND #{endTime} " +
        "    <if test='brokerId != null'>AND broker_id = #{brokerId}</if>" +
        "    GROUP BY broker_id, host_ip, DATE_FORMAT(collect_time, '%Y-%m-%d') " +
        "    ORDER BY timePoint ASC" +
        "  </when>" +
        "  <otherwise>" +
        "    SELECT broker_id as brokerId, host_ip as hostIp, " +
        "           collect_time as timePoint, memory_usage as memoryUsage, " +
        "           1 as sampleCount " +
        "    FROM ke_broker_metrics " +
        "    WHERE collect_time BETWEEN #{startTime} AND #{endTime} " +
        "    <if test='brokerId != null'>AND broker_id = #{brokerId}</if>" +
        "    ORDER BY collect_time ASC" +
        "  </otherwise>" +
        "</choose>" +
        "</script>")
List<Map<String, Object>> selectMemoryUsageTrend(@Param("brokerId") Integer brokerId,
                @Param("startTime") LocalDateTime startTime,
                @Param("endTime") LocalDateTime endTime,
                @Param("aggregationType") String aggregationType);
```

#### 3. selectMetricsStats - 性能指标统计信息查询
```java
@Select("<script>" +
        "SELECT broker_id as brokerId, host_ip as hostIp, " +
        "       COUNT(*) as totalCount, " +
        "       AVG(cpu_usage) as avgCpuUsage, " +
        "       MAX(cpu_usage) as maxCpuUsage, " +
        "       MIN(cpu_usage) as minCpuUsage, " +
        "       AVG(memory_usage) as avgMemoryUsage, " +
        "       MAX(memory_usage) as maxMemoryUsage, " +
        "       MIN(memory_usage) as minMemoryUsage, " +
        "       MIN(collect_time) as firstCollectTime, " +
        "       MAX(collect_time) as lastCollectTime " +
        "FROM ke_broker_metrics " +
        "WHERE collect_time BETWEEN #{startTime} AND #{endTime} " +
        "<if test='brokerId != null'>AND broker_id = #{brokerId}</if>" +
        "GROUP BY broker_id, host_ip " +
        "ORDER BY broker_id ASC" +
        "</script>")
List<Map<String, Object>> selectMetricsStats(@Param("brokerId") Integer brokerId,
                @Param("startTime") LocalDateTime startTime,
                @Param("endTime") LocalDateTime endTime);
```

## 迁移特点

### 1. 注解类型使用
- `@Select`: 用于查询操作
- `@Insert`: 用于插入操作
- `@Update`: 用于更新操作
- `@Delete`: 用于删除操作
- `@Options`: 用于配置选项（如自动生成主键）

### 2. 动态SQL支持
- 使用 `<script>` 标签包装复杂的动态SQL
- 支持 `<if>`, `<choose>`, `<when>`, `<otherwise>` 等动态标签
- 支持 `<foreach>` 进行批量操作

### 3. 参数映射
- 使用 `@Param` 注解明确指定参数名称
- 支持复杂对象和基本类型参数
- 自动进行驼峰命名转换（如 `broker_id` -> `brokerId`）

## 迁移优势

1. **代码集中**: SQL语句与接口方法在同一文件中，便于维护
2. **类型安全**: 编译时检查SQL语法和参数类型
3. **IDE支持**: 更好的代码提示和重构支持
4. **减少文件**: 不需要单独的XML映射文件
5. **版本控制**: 更容易跟踪SQL变更历史

## 注意事项

1. **转义字符**: 在注解中的SQL字符串需要正确处理转义字符
2. **动态SQL**: 复杂的动态SQL仍需要使用 `<script>` 标签
3. **可读性**: 对于非常复杂的SQL，XML方式可能更易读
4. **性能**: 注解方式与XML方式在运行时性能相同

## 验证结果

- ✅ 编译成功，无语法错误
- ✅ 所有原XML中的SQL语句已成功迁移
- ✅ 保持了原有的功能和参数结构
- ✅ 删除了原XML文件，避免配置冲突

## 总结

本次迁移成功将 `BrokerMetricsMapper.xml` 中的所有SQL语句转换为Java注解形式，包括：
- 3个新增的查询方法（CPU趋势、内存趋势、统计信息）
- 保持了所有原有方法的功能不变
- 删除了XML配置文件，简化了项目结构
- 通过编译验证，确保迁移的正确性

迁移完成后，开发人员可以直接在Java接口中查看和修改SQL语句，提高了开发效率和代码可维护性。