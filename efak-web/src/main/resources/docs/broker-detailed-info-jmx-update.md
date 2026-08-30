# 变更说明：BrokerDetailedInfo 增加 jmxPort 字段并移除磁盘/分区相关字段

本次改动根据需求完成以下事项：

- 在 Broker 详情对象中新增字段 jmxPort（int）。
- 删除以下字段及其相关代码：
  - diskUsagePercent、diskUsed、diskTotal
  - partitionCount、leaderPartitionCount、replicaPartitionCount、offlinePartitionCount
  - partitions、BrokerPartitionInfo
- 同步调整服务层对已删除字段的引用，确保编译通过。

## 修改的核心文件

- efak-dto
  - org.kafka.eagle.dto.broker.BrokerDetailedInfo：新增 jmxPort，移除磁盘与分区相关字段及 getter/setter。
- efak-core
  - org.kafka.eagle.core.kafka.impl.ClusterServiceImpl：
    - 构建 BrokerDetailedInfo 时设置 jmxPort 的默认值与外部传入值。
    - 移除 getBrokerResourceSummary 中对 diskUsage 的引用，避免调用已删除字段。
    - 清理与磁盘/分区字段相关的初始化逻辑与赋值（如有）。

## 影响范围与兼容性

- Broker 资源汇总接口不再返回 diskUsage 字段；前端或调用方若依赖该字段，需要同步调整。
- 其他 Topic 相关类和页面中出现的 partitionCount/partitions 属于 Topic 层面对象，并非 BrokerDetailedInfo 的字段，不受本次改动影响（保持原状）。

## 构建验证

- 已执行：source ~/.bash_profile && mvn clean compile -DskipTests
- 结果：全模块编译成功（efak-dto、efak-core、efak-web 等）。

## 后续建议

- 如需在 UI 中展示磁盘使用率，请从独立的数据源或指标体系获取后再行设计字段，不建议复用已移除的 BrokerDetailedInfo 字段。
- 与调度/采集链路有关的 JMX 端口已通过 jmxPort 字段贯通，确保后续 JMX 采集逻辑以该字段为准。