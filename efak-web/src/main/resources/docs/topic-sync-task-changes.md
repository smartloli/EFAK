# 主题监控与同步改造说明

## 变更概览
- 新增批量获取主题详情的代理方法：在 <mcfile name="KafkaServiceProxy.java" path="/Users/smartloli/workspace/EFAK-AI/efak-core/src/main/java/org/kafka/eagle/core/kafka/proxy/KafkaServiceProxy.java"></mcfile> 中新增 <mcsymbol name="getTopicsDetailedStats" filename="KafkaServiceProxy.java" path="/Users/smartloli/workspace/EFAK-AI/efak-core/src/main/java/org/kafka/eagle/core/kafka/proxy/KafkaServiceProxy.java" startline="1" type="function"></mcsymbol>，用于按主题名集合批量拉取详情。
- 改造任务执行器：在 <mcfile name="TaskExecutorManager.java" path="/Users/smartloli/workspace/EFAK-AI/efak-web/src/main/java/org/kafka/eagle/web/service/TaskExecutorManager.java"></mcfile> 的 <mcsymbol name="executeTopicMonitorTask" filename="TaskExecutorManager.java" path="/Users/smartloli/workspace/EFAK-AI/efak-web/src/main/java/org/kafka/eagle/web/service/TaskExecutorManager.java" startline="73" type="function"></mcsymbol> 中，
  - 通过分布式协调器对主题进行分片；
  - 拉取数据库中的 Broker 列表，调用新的批量接口获取当前分片内主题的 TopicDetailedStats；
  - 聚合总分区、总消息数、总大小，并将结果以分片粒度写入 Redis；
  - 新增 <mcsymbol name="saveTopicStatsToDatabase" filename="TaskExecutorManager.java" path="/Users/smartloli/workspace/EFAK-AI/efak-web/src/main/java/org/kafka/eagle/web/service/TaskExecutorManager.java" startline="132" type="function"></mcsymbol> 与 <mcsymbol name="convertToTopicInfo" filename="TaskExecutorManager.java" path="/Users/smartloli/workspace/EFAK-AI/efak-web/src/main/java/org/kafka/eagle/web/service/TaskExecutorManager.java" startline="160" type="function"></mcsymbol>，将真实统计数据同步至 ke_topic_info 表。
- 清理同步接口：在 <mcfile name="TopicController.java" path="/Users/smartloli/workspace/EFAK-AI/efak-web/src/main/java/org/kafka/eagle/web/controller/TopicController.java"></mcfile> 中移除了 `/api/sync` 接口，并修复了保留时间设置 API 的签名与实现，补回演示所需的辅助方法，保证编译通过。

## 设计要点
- 通过 KafkaServiceProxy 统一出口，复用 TopicServiceImpl 的批量实现，减少重复逻辑。
- 任务执行器按分片执行，避免单点处理全部主题造成的压力；并将处理结果回写数据库，保证页面查询直连 DB 的一致性与可见性。
- Mapper 采用注解 SQL，不新增 XML 文件，遵循现有项目规范。

## 影响范围
- 仅涉及服务层与任务执行层，对外 REST API 除移除 `/api/sync` 外保持不变。
- ke_topic_info 表将被周期性更新，数据来源改为任务分片真实采集。

## 验证
- 执行命令：`source ~/.bash_profile && mvn clean compile -DskipTests` 已构建成功。

## 后续建议
- 可为 TaskExecutorManager 的数据库写入增加批处理/事务以提升效率。
- 根据生产集群体量，评估分片大小与任务调度频率，避免对 Kafka 造成过大查询压力。
