package org.kafka.eagle.web.service;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.ai.DingTalkAlertSender;
import org.kafka.eagle.ai.FeishuAlertSender;
import org.kafka.eagle.ai.WeChatWorkAlertSender;
import org.kafka.eagle.core.api.KafkaClusterFetcher;
import org.kafka.eagle.core.api.KafkaSchemaFactory;
import org.kafka.eagle.core.api.KafkaStoragePlugin;
import org.kafka.eagle.core.constant.JmxMetricsConst;
import org.kafka.eagle.core.constant.MBeanMetricsConst;
import org.kafka.eagle.dto.alert.AlertChannel;
import org.kafka.eagle.dto.alert.AlertInfo;
import org.kafka.eagle.dto.alert.AlertTypeConfig;
import org.kafka.eagle.dto.broker.BrokerDetailedInfo;
import org.kafka.eagle.dto.broker.BrokerInfo;
import org.kafka.eagle.dto.broker.BrokerMetrics;
import org.kafka.eagle.dto.cluster.KafkaClientInfo;
import org.kafka.eagle.dto.cluster.KafkaClusterInfo;
import org.kafka.eagle.dto.consumer.ConsumerGroupDetailInfo;
import org.kafka.eagle.dto.consumer.ConsumerGroupTopicInfo;
import org.kafka.eagle.dto.jmx.JMXInitializeInfo;
import org.kafka.eagle.dto.performance.PerformanceMonitor;
import org.kafka.eagle.dto.scheduler.TaskExecutionResult;
import org.kafka.eagle.dto.scheduler.TaskScheduler;
import org.kafka.eagle.dto.topic.TopicDetailedStats;
import org.kafka.eagle.dto.topic.TopicInfo;
import org.kafka.eagle.dto.topic.TopicInstantMetrics;
import org.kafka.eagle.dto.topic.TopicMetrics;
import org.kafka.eagle.web.mapper.*;
import org.kafka.eagle.web.scheduler.DistributedTaskCoordinator;
import org.kafka.eagle.web.util.KafkaClientUtils;
import org.kafka.eagle.web.util.TaskConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: Task executor manager for EFAK web interface
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/30 01:19:56
 * @version 5.0.0
 */
@Slf4j
@Service
public class TaskExecutorManager {

    @Autowired
    private BrokerMapper brokerMapper;

    @Autowired
    private ClusterMapper clusterMapper;

    @Autowired
    private DistributedTaskCoordinator taskCoordinator;

    @Autowired
    private BrokerMetricsService brokerMetricsService;

    @Autowired
    private TopicInfoMapper topicInfoMapper;

    @Autowired
    private TopicMetricsMapper topicMetricsMapper;

    @Autowired
    private TopicInstantMetricsMapper topicInstantMetricsMapper;

    @Autowired
    private ConsumerGroupTopicService consumerGroupTopicService;

    @Autowired
    private PerformanceMonitorService performanceMonitorService;

    @Autowired
    private AlertMapper alertMapper;

    @Autowired
    private ConsumerGroupTopicMapper consumerGroupTopicMapper;

    @Autowired
    private DataCleanupService dataCleanupService;

    @Value("${efak.data-retention-days:30}")
    private int dataRetentionDays;

    /**
     * 执行任务
     */
    public TaskExecutionResult executeTask(TaskScheduler task) {
        TaskExecutionResult result = new TaskExecutionResult();

        try {
            // 根据任务类型执行不同的任务
            switch (task.getTaskType()) {
                case TaskConfig.TASK_TYPE_TOPIC_MONITOR:
                    result = executeTopicMonitorTask(task);
                    break;
                case TaskConfig.TASK_TYPE_CONSUMER_MONITOR:
                    result = executeConsumerMonitorTask(task);
                    break;
                case TaskConfig.TASK_TYPE_CLUSTER_MONITOR:
                    result = executeClusterMonitorTask(task);
                    break;
                case TaskConfig.TASK_TYPE_ALERT_MONITOR:
                    result = executeAlertMonitorTask(task);
                    break;
                case TaskConfig.TASK_TYPE_DATA_CLEANUP:
                    result = executeDataCleanupTask(task);
                    break;
                case TaskConfig.TASK_TYPE_PERFORMANCE_STATS:
                    result = executePerformanceStatsTask(task);
                    break;
                default:
                    result.setSuccess(false);
                    result.setErrorMessage("不支持的任务类型：" + task.getTaskType());
                    break;
            }
        } catch (Exception e) {
            log.error("任务执行异常: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage("任务执行异常：" + e.getMessage());
        }

        return result;
    }

    /**
     * 执行主题监控任务 - 使用真实数据（支持分片）
     */
    private TaskExecutionResult executeTopicMonitorTask(TaskScheduler task) {
        TaskExecutionResult result = new TaskExecutionResult();

        try {

            // 1. 从数据库ke_cluster表获取所有集群信息
            List<KafkaClusterInfo> clusters = getAllClusters();
            if (clusters == null || clusters.isEmpty()) {
                log.info("数据库中没有集群信息，跳过主题监控任务");
                result.setSuccess(true);
                result.setResult("数据库中没有集群信息，跳过主题监控任务");
                return result;
            }

            // 2. 获取所有主题名称（合并所有集群的主题）
            List<String> allTopicNames = new ArrayList<>();
            Map<String, String> topicToClusterMap = new HashMap<>(); // 主题名称到集群ID的映射

            for (KafkaClusterInfo cluster : clusters) {
                try {
                    // 从数据库ke_broker_info表获取当前集群的Broker信息
                    List<BrokerInfo> brokers = brokerMapper.getBrokersByClusterId(cluster.getClusterId());
                    if (brokers.isEmpty()) {
                        log.warn("集群 {} 没有broker信息，跳过", cluster.getClusterId());
                        continue;
                    }

                    // 构建KafkaClientInfo，使用KafkaClientUtils工具类
                    KafkaClientInfo kafkaClientInfo = KafkaClientUtils.buildKafkaClientInfo(cluster, brokers);

                    // 使用KafkaSchemaFactory获取主题名称
                    KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());
                    Set<String> clusterTopics = ksf.listTopicNames(kafkaClientInfo);

                    // 将主题添加到总列表，并记录所属集群
                    for (String topicName : clusterTopics) {
                        String uniqueTopicKey = cluster.getClusterId() + ":" + topicName;
                        allTopicNames.add(uniqueTopicKey);
                        topicToClusterMap.put(uniqueTopicKey, cluster.getClusterId());
                    }

                } catch (Exception e) {
                    log.error("获取集群 {} 的主题信息失败: {}", cluster.getClusterId(), e.getMessage(), e);
                }
            }

            if (allTopicNames.isEmpty()) {
                log.info("所有集群都没有主题信息，跳过主题监控任务");
                result.setSuccess(true);
                result.setResult("所有集群都没有主题信息，跳过主题监控任务");
                return result;
            }

            // 3. 使用分布式任务协调器进行分片
            List<String> assignedTopicNames = taskCoordinator.shardTopics(allTopicNames);

            if (assignedTopicNames.isEmpty()) {
                // 关键逻辑：即使当前节点未分配到任务，也上报空分片结果，便于统计/汇总口径一致
                Map<String, Object> shardResult = new HashMap<>();
                shardResult.put("nodeId", taskCoordinator.getCurrentNodeId());
                shardResult.put("assignedTopicCount", 0);
                shardResult.put("partitionCount", 0L);
                shardResult.put("processedTopicNames", Collections.emptyList());
                shardResult.put("savedToDatabase", 0);
                taskCoordinator.saveShardResult("topic_monitor", shardResult);

                result.setSuccess(true);
                result.setResult("当前节点没有分配到主题，任务完成");
                return result;
            }

            // 4. 获取分配给当前节点的主题详细统计信息
            List<TopicDetailedStats> topicStats = new ArrayList<>();
            int totalPartitions = 0;

            // 按集群分组处理主题，实现批量获取
            Map<String, List<String>> clusterTopicsMap = new HashMap<>();
            for (String uniqueTopicKey : assignedTopicNames) {
                String clusterId = topicToClusterMap.get(uniqueTopicKey);
                String topicName = uniqueTopicKey.substring(uniqueTopicKey.indexOf(":") + 1);

                clusterTopicsMap.computeIfAbsent(clusterId, k -> new ArrayList<>()).add(topicName);
            }

            // 按集群批量获取主题元数据
            for (Map.Entry<String, List<String>> entry : clusterTopicsMap.entrySet()) {
                String clusterId = entry.getKey();
                List<String> clusterTopicNames = entry.getValue();

                try {
                    // 获取集群信息
                    KafkaClusterInfo cluster = clusters.stream()
                            .filter(c -> c.getClusterId().equals(clusterId))
                            .findFirst()
                            .orElse(null);

                    if (cluster == null) {
                        log.warn("找不到集群 {} 的信息，跳过该集群的主题", clusterId);
                        continue;
                    }

                    // 获取集群的broker信息
                    List<BrokerInfo> brokers = brokerMapper.getBrokersByClusterId(clusterId);
                    if (brokers.isEmpty()) {
                        log.warn("集群 {} 没有broker信息，跳过该集群的主题", clusterId);
                        continue;
                    }

                    // 构建KafkaClientInfo，使用KafkaClientUtils工具类
                    KafkaClientInfo kafkaClientInfo = KafkaClientUtils.buildKafkaClientInfo(cluster, brokers);

                    // 使用批量方法获取该集群所有主题的元数据，传递broker信息用于计算
                    KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());
                    Set<String> topicsSet = new HashSet<>(clusterTopicNames);
                    Map<String, TopicDetailedStats> topicMetadataMap = ksf.getTopicMetaData(kafkaClientInfo, topicsSet, brokers);

                    // 处理每个主题的元数据
                    for (String topicName : clusterTopicNames) {
                        TopicDetailedStats topicMetadata = topicMetadataMap.get(topicName);

                        if (topicMetadata == null) {
                            log.warn("未能获取主题 {} 的元数据，跳过", topicName);
                            continue;
                        }

                        // 设置额外属性
                        topicMetadata.setClusterId(clusterId);

                        // 收集额外的主题指标数据
                        try {
                            TopicMetrics topicMetrics = collectTopicMetrics(kafkaClientInfo, brokers, topicName, ksf);
                            if (topicMetrics != null) {
                                // 保存主题指标到数据库
                                saveTopicMetrics(topicMetrics);
                            }
                            List<TopicInstantMetrics> topicInstantMetrics = collectTopicInstantMetrics(kafkaClientInfo, brokers, topicName, ksf);
                            if (topicInstantMetrics.size() > 0) {
                                // 保存主题即时指标
                                topicInstantMetricsMapper.batchUpsertMetrics(topicInstantMetrics);
                            }

                        } catch (Exception e) {
                            log.error("收集主题 {} 指标数据失败: {}", topicName, e.getMessage(), e);
                        }

                        topicStats.add(topicMetadata);
                        totalPartitions += topicMetadata.getPartitionCount();

                    }

                } catch (Exception e) {
                    log.error("批量获取集群 {} 的主题详细信息失败: {}", clusterId, e.getMessage(), e);
                }
            }

            // 5. 将统计信息保存到数据库
            int savedCount = saveTopicStatsToDatabase(topicStats);

            // 6. 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("totalTopicCount", allTopicNames.size());
            data.put("assignedTopicCount", assignedTopicNames.size());
            data.put("partitionCount", totalPartitions);
            data.put("savedToDatabase", savedCount);
            data.put("clusterCount", clusters.size());

            // 保存分片结果到Redis
            Map<String, Object> shardResult = new HashMap<>();
            shardResult.put("nodeId", taskCoordinator.getCurrentNodeId());
            shardResult.put("assignedTopicCount", assignedTopicNames.size());
            shardResult.put("partitionCount", totalPartitions);
            shardResult.put("processedTopicNames", assignedTopicNames);
            shardResult.put("savedToDatabase", savedCount);
            taskCoordinator.saveShardResult("topic_monitor", shardResult);

            result.setSuccess(true);
            result.setResult(String.format("主题监控分片完成，总共%d个主题，当前节点处理%d个主题，%d个分区，保存%d个主题到数据库",
                    allTopicNames.size(), assignedTopicNames.size(), totalPartitions, savedCount));
            result.setData(data);

        } catch (Exception e) {
            log.error("主题监控任务执行失败: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage("主题监控任务执行失败：" + e.getMessage());
        }

        // 完成任务执行并计算持续时间
        result.complete();
        return result;
    }

    /**
     * 从数据库获取所有集群信息
     */
    private List<KafkaClusterInfo> getAllClusters() {
        try {
            // 使用ClusterMapper查询所有集群，不分页获取
            return clusterMapper.queryClusters(null, null, null, "updatedAt", "DESC", 0, 1000);
        } catch (Exception e) {
            log.error("获取集群信息失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 将主题统计信息保存到数据库
     */
    private int saveTopicStatsToDatabase(List<TopicDetailedStats> topicStats) {
        int savedCount = 0;

        try {
            for (TopicDetailedStats stats : topicStats) {
                TopicInfo topicInfo = convertToTopicInfo(stats);
                topicInfo.setCreateBy("system");
                topicInfo.setUpdateBy("system");

                try {
                    // 使用基于topic_name和cluster_id的插入/更新方法
                    int result = topicInfoMapper.insertOrUpdateByTopicAndCluster(topicInfo);
                    if (result > 0) {
                        savedCount++;
                    }
                } catch (Exception e) {
                    log.error("保存主题统计信息失败: {} (集群: {}), 错误: {}",
                            stats.getTopicName(), stats.getClusterId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("批量保存主题统计信息时发生异常", e);
        }

        return savedCount;
    }

    /**
     * 将TopicDetailedStats转换为TopicInfo
     */
    private TopicInfo convertToTopicInfo(TopicDetailedStats stats) {
        TopicInfo topicInfo = new TopicInfo();

        topicInfo.setTopicName(stats.getTopicName());
        topicInfo.setPartitions(stats.getPartitionCount());
        topicInfo.setReplicas(stats.getReplicationFactor());
        topicInfo.setClusterId(stats.getClusterId()); // 设置集群ID

        // 转换broker分布信息
        topicInfo.setBrokerSpread(String.valueOf(stats.getBrokerSpread()));

        // 转换broker倾斜信息
        topicInfo.setBrokerSkewed(String.valueOf(stats.getBrokerSkewed()));

        // 转换leader倾斜信息
        topicInfo.setLeaderSkewed(String.valueOf(stats.getLeaderSkewed()));

        // 转换保留时间
        if (stats.getRetentionMs() != null) {
            topicInfo.setRetentionTime(String.valueOf(stats.getRetentionMs()));
        }

        return topicInfo;
    }

    /**
     * 执行消费者监控任务 - 使用真实数据（支持分片）
     */
    private TaskExecutionResult executeConsumerMonitorTask(TaskScheduler task) {
        TaskExecutionResult result = new TaskExecutionResult();

        try {

            // 1. 从数据库ke_cluster表获取所有集群信息
            List<KafkaClusterInfo> clusters = getAllClusters();
            if (clusters == null || clusters.isEmpty()) {
                log.info("数据库中没有集群信息，跳过消费者监控任务");
                result.setSuccess(true);
                result.setResult("数据库中没有集群信息，跳过消费者监控任务");
                return result;
            }

            // 2. 获取所有消费者组ID（合并所有集群的消费者组）
            List<String> allConsumerGroupIds = new ArrayList<>();
            Map<String, String> consumerGroupToClusterMap = new HashMap<>(); // 消费者组ID到集群ID的映射

            for (KafkaClusterInfo cluster : clusters) {
                try {
                    // 从数据库ke_broker_info表获取当前集群的Broker信息
                    List<BrokerInfo> brokers = brokerMapper.getBrokersByClusterId(cluster.getClusterId());
                    if (brokers.isEmpty()) {
                        log.warn("集群 {} 没有broker信息，跳过", cluster.getClusterId());
                        continue;
                    }

                    // 构建KafkaClientInfo，使用KafkaClientUtils工具类
                    KafkaClientInfo kafkaClientInfo = KafkaClientUtils.buildKafkaClientInfo(cluster, brokers);

                    // 使用KafkaSchemaFactory获取消费者组集合
                    KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());
                    Set<String> groupIds = ksf.getConsumerGroupIds(kafkaClientInfo);

                    // 将消费者组添加到总列表，并记录所属集群
                    for (String groupId : groupIds) {
                        String uniqueGroupKey = cluster.getClusterId() + ":" + groupId;
                        allConsumerGroupIds.add(uniqueGroupKey);
                        consumerGroupToClusterMap.put(uniqueGroupKey, cluster.getClusterId());
                    }

                } catch (Exception e) {
                    log.error("获取集群 {} 的消费者组信息失败: {}", cluster.getClusterId(), e.getMessage(), e);
                }
            }

            if (allConsumerGroupIds.isEmpty()) {
                log.info("所有集群都没有消费者组信息，跳过消费者监控任务");
                result.setSuccess(true);
                result.setResult("所有集群都没有消费者组信息，跳过消费者监控任务");
                return result;
            }

            // 3. 使用分布式任务协调器进行分片
            List<String> assignedConsumerGroupIds = taskCoordinator.shardConsumerGroups(allConsumerGroupIds);

            if (assignedConsumerGroupIds.isEmpty()) {
                // 关键逻辑：即使当前节点未分配到任务，也上报空分片结果，便于统计/汇总口径一致
                Map<String, Object> shardResult = new HashMap<>();
                shardResult.put("nodeId", taskCoordinator.getCurrentNodeId());
                shardResult.put("assignedConsumerGroupCount", 0);
                shardResult.put("activeConsumers", 0);
                shardResult.put("lagConsumers", 0);
                shardResult.put("totalLag", 0L);
                shardResult.put("processedConsumerGroupIds", Collections.emptyList());
                taskCoordinator.saveShardResult("consumer_monitor", shardResult);

                result.setSuccess(true);
                result.setResult("当前节点没有分配到消费者组，任务完成");
                return result;
            }

            // 4. 获取分配给当前节点的消费者组详细信息并收集数据
            List<ConsumerGroupTopicInfo> allConsumerGroupTopicInfos = new ArrayList<>();
            int activeConsumers = 0;
            int lagConsumers = 0;
            long totalLag = 0;

            // 按集群分组处理消费者组，实现批量获取
            Map<String, List<String>> clusterConsumerGroupsMap = new HashMap<>();
            for (String uniqueGroupKey : assignedConsumerGroupIds) {
                String clusterId = consumerGroupToClusterMap.get(uniqueGroupKey);
                String groupId = uniqueGroupKey.substring(uniqueGroupKey.indexOf(":") + 1);

                clusterConsumerGroupsMap.computeIfAbsent(clusterId, k -> new ArrayList<>()).add(groupId);
            }

            // 按集群批量获取消费者组数据
            for (Map.Entry<String, List<String>> entry : clusterConsumerGroupsMap.entrySet()) {
                String clusterId = entry.getKey();
                List<String> clusterGroupIds = entry.getValue();

                try {
                    // 获取集群信息
                    KafkaClusterInfo cluster = clusters.stream()
                            .filter(c -> c.getClusterId().equals(clusterId))
                            .findFirst()
                            .orElse(null);

                    if (cluster == null) {
                        log.warn("找不到集群 {} 的信息，跳过该集群的消费者组", clusterId);
                        continue;
                    }

                    // 获取集群的broker信息
                    List<BrokerInfo> brokers = brokerMapper.getBrokersByClusterId(clusterId);
                    if (brokers.isEmpty()) {
                        log.warn("集群 {} 没有broker信息，跳过该集群的消费者组", clusterId);
                        continue;
                    }

                    // 构建KafkaClientInfo，使用KafkaClientUtils工具类
                    KafkaClientInfo kafkaClientInfo = KafkaClientUtils.buildKafkaClientInfo(cluster, brokers);

                    // 使用批量方法获取该集群所有消费者组的主题信息
                    KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());
                    Set<String> groupIdsSet = new HashSet<>(clusterGroupIds);

                    // 获取消费者组详细信息（用于统计活跃消费者）
                    List<ConsumerGroupDetailInfo> consumerGroupDetails = ksf.getConsumerGroups(kafkaClientInfo, groupIdsSet);

                    // 获取消费者组主题信息（用于数据库存储）
                    List<ConsumerGroupTopicInfo> consumerGroupTopicInfos = ksf.getKafkaConsumerGroupTopic(kafkaClientInfo, groupIdsSet);

                    // 统计活跃消费者和延迟信息
                    for (ConsumerGroupDetailInfo group : consumerGroupDetails) {
                        if ("STABLE".equals(group.getState())) {
                            activeConsumers++;
                        }
                    }

                    // 处理每个消费者组主题的数据，设置集群ID
                    for (ConsumerGroupTopicInfo cgti : consumerGroupTopicInfos) {
                        cgti.setClusterId(clusterId);
                        allConsumerGroupTopicInfos.add(cgti);

                        // 计算延迟统计
                        if (cgti.getLags() != null && cgti.getLags() > 0) {
                            lagConsumers++;
                            totalLag += cgti.getLags();
                        }
                    }

                } catch (Exception e) {
                    log.error("批量获取集群 {} 的消费者组详细信息失败: {}", clusterId, e.getMessage(), e);
                }
            }

            // 5. 将消费者组主题信息保存到数据库
            int savedCount = saveConsumerGroupTopicInfosToDatabase(allConsumerGroupTopicInfos);

            // 6. 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("totalConsumerGroupCount", allConsumerGroupIds.size()); // 总消费者组数量
            data.put("assignedConsumerGroupCount", assignedConsumerGroupIds.size()); // 当前节点处理的消费者组数量
            data.put("assignedConsumerGroupIds", assignedConsumerGroupIds); // 当前节点处理的消费者组列表
            data.put("activeConsumers", activeConsumers);
            data.put("lagConsumers", lagConsumers);
            data.put("totalLag", totalLag);
            data.put("avgLag", lagConsumers > 0 ? totalLag / lagConsumers : 0);
            data.put("savedToDatabase", savedCount);
            data.put("clusterCount", clusters.size());
            data.put("nodeId", taskCoordinator.getCurrentNodeId()); // 当前节点ID

            // 保存分片结果到Redis
            Map<String, Object> shardResult = new HashMap<>();
            shardResult.put("nodeId", taskCoordinator.getCurrentNodeId());
            shardResult.put("assignedConsumerGroupCount", assignedConsumerGroupIds.size());
            shardResult.put("activeConsumers", activeConsumers);
            shardResult.put("lagConsumers", lagConsumers);
            shardResult.put("totalLag", totalLag);
            shardResult.put("savedToDatabase", savedCount);
            shardResult.put("processedConsumerGroupIds", assignedConsumerGroupIds);
            taskCoordinator.saveShardResult("consumer_monitor", shardResult);

            result.setSuccess(true);
            result.setResult(String.format("消费者监控分片完成，总共%d个消费者组，当前节点处理%d个消费者组，%d个活跃，%d个有延迟，总延迟%d，保存%d条记录到数据库",
                    allConsumerGroupIds.size(), assignedConsumerGroupIds.size(), activeConsumers, lagConsumers,
                    totalLag, savedCount));
            result.setData(data);

        } catch (Exception e) {
            log.error("消费者监控任务执行失败: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage("消费者监控任务执行失败：" + e.getMessage());
        }

        // 完成任务执行并计算持续时间
        result.complete();
        return result;
    }

    /**
     * 执行集群监控任务 - 使用真实数据并更新broker信息到数据库（支持分片）
     */
    private TaskExecutionResult executeClusterMonitorTask(TaskScheduler task) {
        TaskExecutionResult result = new TaskExecutionResult();

        try {

            // 从数据库获取broker信息，用于构建host、port、jmx_port映射
            List<BrokerInfo> dbBrokers = brokerMapper.queryAllBrokers();
            if (dbBrokers == null || dbBrokers.isEmpty()) {
                log.info("数据库中没有broker信息，跳过集群监控任务");
                result.setSuccess(true);
                result.setResult("数据库中没有broker信息，跳过集群监控任务");
                return result;
            }
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

            // 获取所有broker ID列表
            List<Integer> allBrokerIds = dbBrokers.stream()
                    .map(BrokerInfo::getBrokerId)
                    .collect(Collectors.toList());

            // 使用分布式任务协调器进行分片
            List<Integer> assignedBrokerIds = taskCoordinator.shardBrokers(allBrokerIds);

            if (assignedBrokerIds.isEmpty()) {
                // 关键逻辑：即使当前节点未分配到任务，也上报空分片结果，便于统计/汇总口径一致
                Map<String, Object> shardResult = new HashMap<>();
                shardResult.put("nodeId", taskCoordinator.getCurrentNodeId());
                shardResult.put("assignedBrokerCount", 0);
                shardResult.put("onlineBrokers", 0);
                shardResult.put("offlineBrokers", 0);
                shardResult.put("updatedBrokers", 0);
                shardResult.put("savedMetrics", 0);
                shardResult.put("processedBrokerIds", Collections.emptyList());
                taskCoordinator.saveShardResult("cluster_monitor", shardResult);

                result.setSuccess(true);
                result.setResult("当前节点没有分配到broker，任务完成");
                return result;
            }

            // 过滤出分配给当前节点的broker信息
            List<BrokerInfo> assignedBrokers = dbBrokers.stream()
                    .filter(broker -> assignedBrokerIds.contains(broker.getBrokerId()))
                    .collect(Collectors.toList());

            // 获取分配给当前节点的Broker详细信息
            List<BrokerDetailedInfo> brokerInfos = fetchBrokerDetailedInfos(assignedBrokers);

            // 计算统计信息
            int onlineBrokers = 0;
            int offlineBrokers = 0;

            // 收集启动时间和版本号信息
            Map<String, Object> brokerDetails = new HashMap<>();
            String clusterVersion = null;
            String earliestStartTime = null;

            // 更新broker信息到数据库
            int updatedBrokers = 0;
            int createdBrokers = 0;
            int savedMetrics = 0;

            for (BrokerDetailedInfo broker : brokerInfos) {
                if ("ONLINE".equals(broker.getStatus())) {
                    onlineBrokers++;
                } else {
                    offlineBrokers++;
                }

                // 收集每个Broker的详细信息
                Map<String, Object> brokerInfo = new HashMap<>();
                brokerInfo.put("brokerId", broker.getBrokerId());
                brokerInfo.put("host", broker.getHost());
                brokerInfo.put("port", broker.getPort());
                brokerInfo.put("status", broker.getStatus());
                brokerInfo.put("version", broker.getVersion());
                brokerInfo.put("startTime", broker.getStartTime());
                brokerInfo.put("cpuUsagePercent", broker.getCpuUsagePercent());
                brokerInfo.put("memoryUsagePercent", broker.getMemoryUsagePercent());

                brokerDetails.put("broker_" + broker.getBrokerId(), brokerInfo);

                // 记录版本号（使用第一个非空版本）
                if (clusterVersion == null && broker.getVersion() != null) {
                    clusterVersion = broker.getVersion();
                }

                // 记录最早的启动时间
                if (broker.getStartTime() != null) {
                    if (earliestStartTime == null
                            || broker.getStartTime().isBefore(java.time.LocalDateTime.parse(earliestStartTime))) {
                        earliestStartTime = broker.getStartTime().toString();
                    }
                }

                // 更新broker信息到数据库
                try {
                    // 从assignedBrokers中找到对应的broker信息获取clusterId
                    String clusterId = assignedBrokers.stream()
                            .filter(b -> b.getBrokerId().equals(broker.getBrokerId()))
                            .map(BrokerInfo::getClusterId)
                            .findFirst()
                            .orElse("default"); // 默认集群ID

                    updateBrokerInfoInDatabase(broker, clusterId);
                    updatedBrokers++;
                } catch (Exception e) {
                    log.error("更新broker {} 信息到数据库失败: {}", broker.getBrokerId(), e.getMessage(), e);
                    // 不抛出异常，继续处理其他broker
                }

                // 保存broker性能指标数据到ke_broker_metrics表
                try {
                    BrokerMetrics metrics = new BrokerMetrics();
                    metrics.setBrokerId(broker.getBrokerId());
                    metrics.setHostIp(broker.getHost());
                    metrics.setPort(broker.getPort());

                    // 从assignedBrokers中找到对应的broker信息获取clusterId
                    String clusterId = assignedBrokers.stream()
                            .filter(b -> b.getBrokerId().equals(broker.getBrokerId()))
                            .map(BrokerInfo::getClusterId)
                            .findFirst()
                            .orElse("default"); // 默认集群ID
                    metrics.setClusterId(clusterId);

                    // 转换CPU和内存使用率为BigDecimal
                    if (broker.getCpuUsagePercent() != 0.0) {
                        metrics.setCpuUsage(BigDecimal.valueOf(broker.getCpuUsagePercent()));
                    }
                    if (broker.getMemoryUsagePercent() != 0.0) {
                        metrics.setMemoryUsage(BigDecimal.valueOf(broker.getMemoryUsagePercent()));
                    }

                    LocalDateTime now = LocalDateTime.now();
                    metrics.setCollectTime(now);
                    metrics.setCreateTime(now);

                    boolean saved = brokerMetricsService.saveBrokerMetrics(metrics);
                    if (saved) {
                        savedMetrics++;
                    } else {
                        log.warn("保存broker {} 性能指标数据失败", broker.getBrokerId());
                    }
                } catch (Exception e) {
                    log.error("保存broker {} 性能指标数据失败: {}", broker.getBrokerId(), e.getMessage(), e);
                    // 不抛出异常，继续处理其他broker
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("totalBrokerCount", allBrokerIds.size()); // 总broker数量
            data.put("assignedBrokerCount", brokerInfos.size()); // 当前节点处理的broker数量
            data.put("assignedBrokerIds", assignedBrokerIds); // 当前节点处理的broker ID列表
            data.put("onlineBrokers", onlineBrokers);
            data.put("offlineBrokers", offlineBrokers);
            data.put("clusterVersion", clusterVersion);
            data.put("earliestStartTime", earliestStartTime);
            data.put("brokerDetails", brokerDetails);
            data.put("updatedBrokers", updatedBrokers);
            data.put("createdBrokers", createdBrokers);
            data.put("savedMetrics", savedMetrics);
            data.put("nodeId", taskCoordinator.getCurrentNodeId()); // 当前节点ID

            // 保存分片任务结果到Redis，供其他节点汇总使用
            Map<String, Object> shardResult = new HashMap<>();
            shardResult.put("nodeId", taskCoordinator.getCurrentNodeId());
            shardResult.put("assignedBrokerCount", brokerInfos.size());
            shardResult.put("onlineBrokers", onlineBrokers);
            shardResult.put("offlineBrokers", offlineBrokers);
            shardResult.put("updatedBrokers", updatedBrokers);
            shardResult.put("savedMetrics", savedMetrics);
            shardResult.put("processedBrokerIds", assignedBrokerIds);
            taskCoordinator.saveShardResult("cluster_monitor", shardResult);

            result.setSuccess(true);
            result.setResult(
                    String.format("集群监控分片完成，总共%d个Broker，当前节点处理%d个Broker（%d个在线，%d个离线），版本：%s，更新了%d个broker信息，保存了%d条性能指标",
                            allBrokerIds.size(), brokerInfos.size(), onlineBrokers, offlineBrokers,
                            clusterVersion != null ? clusterVersion : "未知",
                            updatedBrokers, savedMetrics));
            result.setData(data);

            log.info(
                    "集群监控任务分片执行完成: 总共{}个Broker, 当前节点处理{}个Broker, {}个在线, 版本: {}, 最早启动时间: {}, 更新了{}个broker信息, 保存了{}条性能指标",
                    allBrokerIds.size(), brokerInfos.size(), onlineBrokers, clusterVersion, earliestStartTime,
                    updatedBrokers, savedMetrics);

        } catch (Exception e) {
            log.error("集群监控任务执行失败: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage("集群监控任务执行失败：" + e.getMessage());
        }

        // 完成任务执行并计算持续时间
        result.complete();
        return result;
    }

    /**
     * 执行告警监控任务
     */
    private TaskExecutionResult executeAlertMonitorTask(TaskScheduler task) {
        TaskExecutionResult result = new TaskExecutionResult();

        try {

            // 1. 从数据库获取所有集群信息
            List<KafkaClusterInfo> clusters = getAllClusters();
            if (clusters == null || clusters.isEmpty()) {
                log.info("数据库中没有集群信息，跳过告警监控任务");
                result.setSuccess(true);
                result.setResult("数据库中没有集群信息，跳过告警监控任务");
                return result;
            }

            // 2. 获取所有已启用的告警类型配置
            List<AlertTypeConfig> allAlertConfigs = new ArrayList<>();
            for (KafkaClusterInfo cluster : clusters) {
                List<AlertTypeConfig> clusterConfigs = alertMapper.getAlertTypeConfigs(cluster.getClusterId());
                if (clusterConfigs != null) {
                    // 筛选启用的配置
                    List<AlertTypeConfig> enabledConfigs = clusterConfigs.stream()
                            .filter(config -> Boolean.TRUE.equals(config.getEnabled()))
                            .collect(Collectors.toList());
                    allAlertConfigs.addAll(enabledConfigs);
                }
            }

            if (allAlertConfigs.isEmpty()) {
                log.info("没有已启用的告警配置，跳过告警监控任务");
                result.setSuccess(true);
                result.setResult("没有已启用的告警配置，任务完成");
                return result;
            }

            // 3. 使用分布式任务协调器对告警配置进行分片
            List<Long> allConfigIds = allAlertConfigs.stream()
                    .map(AlertTypeConfig::getId)
                    .collect(Collectors.toList());

            List<Long> assignedConfigIds = taskCoordinator.shardAlertConfigs(allConfigIds);

            if (assignedConfigIds.isEmpty()) {
                // 关键逻辑：即使当前节点未分配到任务，也上报空分片结果，便于统计/汇总口径一致
                Map<String, Object> shardResult = new HashMap<>();
                shardResult.put("nodeId", taskCoordinator.getCurrentNodeId());
                shardResult.put("assignedConfigCount", 0);
                shardResult.put("evaluatedCount", 0);
                shardResult.put("triggeredCount", 0);
                shardResult.put("sentCount", 0);
                shardResult.put("processedConfigIds", Collections.emptyList());
                taskCoordinator.saveShardResult("alert_monitor", shardResult);

                result.setSuccess(true);
                result.setResult("当前节点没有分配到告警配置，任务完成");
                return result;
            }

            // 4. 过滤出分配给当前节点的告警配置
            List<AlertTypeConfig> assignedConfigs = allAlertConfigs.stream()
                    .filter(config -> assignedConfigIds.contains(config.getId()))
                    .collect(Collectors.toList());

            // 5. 执行告警评估和发送
            int evaluatedCount = 0;
            int triggeredCount = 0;
            int sentCount = 0;

            for (AlertTypeConfig config : assignedConfigs) {
                evaluatedCount++;
                try {
                    boolean triggered = evaluateAndProcessAlert(config);
                    if (triggered) {
                        triggeredCount++;
                        sentCount++;
                    }
                } catch (Exception e) {
                    log.error("处理告警配置 {} 失败: {}", config.getId(), e.getMessage(), e);
                }
            }

            // 6. 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("totalConfigCount", allAlertConfigs.size());
            data.put("assignedConfigCount", assignedConfigs.size());
            data.put("assignedConfigIds", assignedConfigIds);
            data.put("evaluatedCount", evaluatedCount);
            data.put("triggeredCount", triggeredCount);
            data.put("sentCount", sentCount);
            data.put("clusterCount", clusters.size());
            data.put("nodeId", taskCoordinator.getCurrentNodeId());

            // 保存分片结果到Redis
            Map<String, Object> shardResult = new HashMap<>();
            shardResult.put("nodeId", taskCoordinator.getCurrentNodeId());
            shardResult.put("assignedConfigCount", assignedConfigs.size());
            shardResult.put("evaluatedCount", evaluatedCount);
            shardResult.put("triggeredCount", triggeredCount);
            shardResult.put("sentCount", sentCount);
            shardResult.put("processedConfigIds", assignedConfigIds);
            taskCoordinator.saveShardResult("alert_monitor", shardResult);

            result.setSuccess(true);
            result.setResult(String.format("告警监控分片完成，总共%d个配置，当前节点处理%d个配置，评估%d个，触发%d个，发送%d个",
                    allAlertConfigs.size(), assignedConfigs.size(), evaluatedCount, triggeredCount, sentCount));
            result.setData(data);

        } catch (Exception e) {
            log.error("告警监控任务执行失败: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage("告警监控任务执行失败：" + e.getMessage());
        }

        // 完成任务执行并计算持续时间
        result.complete();
        return result;
    }

    /**
     * 评估并处理告警
     */
    private boolean evaluateAndProcessAlert(AlertTypeConfig config) {
        String type = config.getType();
        String clusterId = config.getClusterId();

        try {
            boolean triggered = false;

            switch (type) {
                case "consumer-lag":
                    triggered = evaluateConsumerLagAlert(config);
                    break;
                case "broker-availability":
                    triggered = evaluateBrokerAvailabilityAlert(config);
                    break;
                case "topic-capacity":
                    triggered = evaluateTopicCapacityAlert(config);
                    break;
                case "topic-throughput":
                    triggered = evaluateTopicThroughputAlert(config);
                    break;
                default:
                    log.warn("不支持的告警类型: {}", type);
                    return false;
            }

            return triggered;

        } catch (Exception e) {
            log.error("评估告警配置 {} (type={}, cluster={}) 失败: {}",
                    config.getId(), type, clusterId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 评估消费者延迟告警
     */
    private boolean evaluateConsumerLagAlert(AlertTypeConfig config) {
        try {
            // 解析target字段（格式为my-group-1,ke28）
            String target = config.getTarget();
            if (target == null || target.trim().isEmpty() || "all".equalsIgnoreCase(target.trim())) {
                return false;
            }

            String[] parts = target.split(",");
            if (parts.length < 2) {
                log.warn("消费者延迟告警配置 {} 的target格式不正确: {}", config.getId(), target);
                return false;
            }

            String groupId = parts[0].trim();
            String topicName = parts[1].trim();
            BigDecimal threshold = config.getThreshold();

            if (threshold == null || threshold.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }

            // 查询最近1小时以内的最新记录
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            List<ConsumerGroupTopicInfo> recentRecords = consumerGroupTopicMapper.selectConsumerGroupTopicPage(
                    new org.kafka.eagle.dto.consumer.ConsumerGroupTopicQueryRequest() {{
                        setClusterId(config.getClusterId());
                        setGroupId(groupId);
                        setTopicName(topicName);
                        setStartTime(oneHourAgo);
                        setEndTime(LocalDateTime.now());
                    }}, 0, 1);

            if (recentRecords == null || recentRecords.isEmpty()) {
                return false;
            }

            ConsumerGroupTopicInfo latestRecord = recentRecords.get(0);
            Long lags = latestRecord.getLags();

            if (lags == null || lags <= 0) {
                return false;
            }

            // 判断是否超过阈值
            if (lags > threshold.longValue()) {
                return processTriggeredAlert(config, "消费者ID: " + groupId + ",主题: " + topicName, lags + " " + config.getUnit(), threshold.longValue());
            }

            return false;

        } catch (Exception e) {
            log.error("评估消费者延迟告警失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 评估Broker可用性告警
     */
    private boolean evaluateBrokerAvailabilityAlert(AlertTypeConfig config) {
        try {
            String target = config.getTarget();
            if (target == null || target.trim().isEmpty() || "all".equalsIgnoreCase(target.trim())) {
                return false;
            }

            String ipAddress = target.trim();
            BigDecimal threshold = config.getThreshold();

            if (threshold == null || threshold.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }

            // 查询ke_broker_info表获取memory_usage
            BrokerInfo brokerInfo = brokerMapper.getBrokerByHostIp(ipAddress);
            if (brokerInfo == null) {
                return false;
            }

            BigDecimal memoryUsage = brokerInfo.getMemoryUsage();
            if (memoryUsage == null) {
                return false;
            }

            // 判断是否超过阈值
            if (memoryUsage.compareTo(threshold) > 0) {
                return processTriggeredAlert(config, "服务器" + ipAddress, memoryUsage + "%", threshold.longValue());
            }

            return false;

        } catch (Exception e) {
            log.error("评估Broker可用性告警失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 评估主题容量告警
     */
    private boolean evaluateTopicCapacityAlert(AlertTypeConfig config) {
        try {
            String target = config.getTarget();
            if (target == null || target.trim().isEmpty() || "all".equalsIgnoreCase(target.trim())) {
                return false;
            }

            String topicName = target.trim();
            BigDecimal threshold = config.getThreshold();
            String unit = config.getUnit();

            if (threshold == null || threshold.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }

            // 将阈值转换为字节
            long thresholdBytes = convertToBytes(threshold, unit);

            // 查询ke_topic_instant_metrics表获取容量
            List<TopicInstantMetrics> metrics = topicInstantMetricsMapper.selectByTopicAndClusterAndMetricType(
                    config.getClusterId(), topicName, MBeanMetricsConst.Topic.CAPACITY.key());

            if (metrics == null || metrics.isEmpty()) {
                return false;
            }

            TopicInstantMetrics latestMetric = metrics.get(0);
            String metricValueStr = latestMetric.getMetricValue();

            if (metricValueStr == null || metricValueStr.trim().isEmpty()) {
                return false;
            }

            long currentCapacity;
            try {
                currentCapacity = Long.parseLong(metricValueStr);
            } catch (NumberFormatException e) {
                return false;
            }

            // 判断是否超过阈值
            if (currentCapacity > thresholdBytes) {
                return processTriggeredAlert(config, "主题:" + topicName,
                        formatBytes(currentCapacity), thresholdBytes);
            }

            return false;

        } catch (Exception e) {
            log.error("评估主题容量告警失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 评估主题写入速度告警
     */
    private boolean evaluateTopicThroughputAlert(AlertTypeConfig config) {
        try {
            String target = config.getTarget();
            if (target == null || target.trim().isEmpty() || "all".equalsIgnoreCase(target.trim())) {
                return false;
            }

            String topicName = target.trim();
            BigDecimal threshold = config.getThreshold();

            if (threshold == null || threshold.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }

            // 查询ke_topic_instant_metrics表获取写入速度
            List<TopicInstantMetrics> metrics = topicInstantMetricsMapper.selectByTopicAndClusterAndMetricType(
                    config.getClusterId(), topicName, MBeanMetricsConst.Topic.BYTE_IN.key());

            if (metrics == null || metrics.isEmpty()) {
                return false;
            }

            TopicInstantMetrics latestMetric = metrics.get(0);
            String metricValueStr = latestMetric.getMetricValue();

            if (metricValueStr == null || metricValueStr.trim().isEmpty()) {
                return false;
            }

            BigDecimal currentThroughput;
            try {
                currentThroughput = new BigDecimal(metricValueStr);
            } catch (NumberFormatException e) {
                return false;
            }

            // 判断是否超过阈值
            if (currentThroughput.compareTo(threshold) > 0) {
                return processTriggeredAlert(config, "主题" + topicName,
                        currentThroughput + " " + config.getUnit(), threshold.longValue());
            }

            return false;

        } catch (Exception e) {
            log.error("评估主题写入速度告警失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 处理触发的告警
     */
    private boolean processTriggeredAlert(AlertTypeConfig config, String alertSource, String currentValue, long thresholdValue) {
        try {
            // 1. 检查是否已存在告警记录
            AlertInfo existingAlert = alertMapper.getLatestAlertByTaskId(config.getId(), config.getClusterId());

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime updatedAt = existingAlert.getUpdatedAt();
            // 30分钟以内被手动确认、忽略的告警，不再处理，超过30分钟告警未被处理，则重新生成新的告警记录
            boolean isWithin30Minutes = Duration.between(updatedAt, now).toMinutes() < 30;

            Set<Integer> validStatus = Set.of(1, 2, 3);

            if (existingAlert != null && existingAlert.getStatus() != null && existingAlert.getStatus() == 0) {
                // 如果存在未处理的告警，更新告警记录
                existingAlert.setDescription(String.format("%s 告警阈值超过%d %s，当前值：%s",
                        alertSource, thresholdValue, config.getUnit() != null ? config.getUnit() : "",
                        currentValue));
                existingAlert.setUpdatedAt(now);

                // 计算持续时间：updated_at - created_at
                String duration = calculateAlertDuration(existingAlert.getCreatedAt(), now);
                existingAlert.setDuration(duration);

                alertMapper.updateAlertStatus(config.getId(), config.getClusterId(), 0);

            } else if (existingAlert != null && existingAlert.getStatus() != null && isWithin30Minutes && validStatus.contains(existingAlert.getStatus())) {
                // 如果存在已确认的告警，不需要更新
                return false;

            } else {
                // 创建新的告警记录
                AlertInfo newAlert = new AlertInfo();
                newAlert.setAlertTaskId(config.getId());
                newAlert.setClusterId(config.getClusterId());
                newAlert.setTitle(config.getName());
                newAlert.setDescription(String.format("%s 告警阈值超过%d %s，当前值：%s",
                        alertSource, thresholdValue, config.getUnit() != null ? config.getUnit() : "",
                        currentValue));
                newAlert.setChannelId(config.getChannelId());
                newAlert.setDuration("0s"); // 新创建的告警持续时间为0s
                newAlert.setStatus(AlertInfo.STATUS_UNPROCESSED);
                newAlert.setCreatedAt(now);
                // 新创建时 updatedAt 设置为与 createdAt 相同
                newAlert.setUpdatedAt(now);

                int result = alertMapper.createAlert(newAlert);
                if (result > 0) {
                } else {
                    log.warn("创建告警记录失败: configId={}, clusterId={}", config.getId(), config.getClusterId());
                    return false;
                }
            }

            // 2. 发送告警通知
            sendAlertNotification(config, alertSource, currentValue, thresholdValue);

            return true;

        } catch (Exception e) {
            log.error("处理触发的告警失败: configId={}, error={}", config.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送告警通知
     */
    private void sendAlertNotification(AlertTypeConfig config, String alertSource, String currentValue, long thresholdValue) {
        if (config.getChannelId() == null) {
            return;
        }

        try {
            // 获取渠道信息
            AlertChannel channel = alertMapper.getAlertChannelById(config.getChannelId());
            if (channel == null || !Boolean.TRUE.equals(channel.getEnabled())) {
                return;
            }

            String title = config.getName();
            String threshold = thresholdValue + " " + (config.getUnit() != null ? config.getUnit() : "");
            String alertType = alertSource;
            String webhook = channel.getApiUrl();

            // 根据渠道类型发送通知
            switch (channel.getType()) {
                case "dingtalk":
                    sendDingTalkAlert(webhook, title, threshold, alertType);
                    break;
                case "wechat":
                    sendWeChatAlert(webhook, title, threshold, alertType);
                    break;
                case "feishu":
                    sendFeishuAlert(webhook, title, threshold, alertType);
                    break;
                default:
                    log.warn("不支持的渠道类型: {}", channel.getType());
                    break;
            }

        } catch (Exception e) {
            log.error("发送告警通知失败: configId={}, error={}", config.getId(), e.getMessage(), e);
        }
    }

    /**
     * 发送钉钉告警
     */
    private void sendDingTalkAlert(String webhook, String title, String threshold, String alertType) {
        CompletableFuture.runAsync(() -> {
            DingTalkAlertSender sender = new DingTalkAlertSender(webhook);

            try {
                String markdownContent = "# EFAK - " + title + "\n<br/>" +
                        "告警阈值超过" + threshold + "\n<br/>" +
                        "**时间**: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n<br/>" +
                        "**告警类型**: " + alertType + " \n<br/>" +
                        "请相关人员关注并处理！";
                String markdownResult = sender.sendMarkdownAlert("系统告警", markdownContent, false, "");
            } catch (IOException e) {
                log.error("发送钉钉告警失败: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * 发送微信告警
     */
    private void sendWeChatAlert(String webhook, String title, String threshold, String alertType) {
        CompletableFuture.runAsync(() -> {
            WeChatWorkAlertSender sender = new WeChatWorkAlertSender(webhook);

            try {
                String markdownContent = "# EFAK - " + title + "\n" +
                        "告警阈值超过" + threshold + "\n" +
                        "**时间**: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n" +
                        "**告警类型**: " + alertType + " \n" +
                        "请相关人员关注并处理！";
                String markdownResult = sender.sendMarkdownAlert(markdownContent);
            } catch (IOException e) {
                log.error("发送企业微信告警失败: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * 发送飞书告警
     */
    private void sendFeishuAlert(String webhook, String title, String threshold, String alertType) {
        CompletableFuture.runAsync(() -> {
            FeishuAlertSender sender = new FeishuAlertSender(webhook);

            try {
                String postContent = "# EFAK - " + title + "\n" +
                        "告警阈值超过" + threshold + "\n" +
                        "- **时间**: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n" +
                        "**告警类型**: " + alertType + " \n" +
                        "请相关人员关注并处理！";
                String postResult = sender.sendPostAlert("系统告警", postContent, "");
            } catch (IOException e) {
                log.error("发送飞书告警失败: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * 将阈值转换为字节
     */
    private long convertToBytes(BigDecimal value, String unit) {
        if (value == null) return 0L;

        if (unit == null || unit.trim().isEmpty()) {
            unit = "MB"; // 默认单位
        }

        unit = unit.trim().toUpperCase();
        switch (unit) {
            case "B":
            case "BYTES":
                return value.longValue();
            case "KB":
                return value.multiply(BigDecimal.valueOf(1024L)).longValue();
            case "MB":
                return value.multiply(BigDecimal.valueOf(1024L * 1024L)).longValue();
            case "GB":
                return value.multiply(BigDecimal.valueOf(1024L * 1024L * 1024L)).longValue();
            case "TB":
                return value.multiply(BigDecimal.valueOf(1024L * 1024L * 1024L * 1024L)).longValue();
            default:
                return value.multiply(BigDecimal.valueOf(1024L * 1024L)).longValue(); // 默认当作MB
        }
    }

    /**
     * 格式化字节数为可读格式
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        if (bytes < 1024L * 1024 * 1024 * 1024) return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        return String.format("%.2f TB", bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0));
    }

    /**
     * 执行数据清理任务（支持分片）
     */
    private TaskExecutionResult executeDataCleanupTask(TaskScheduler task) {
        TaskExecutionResult result = new TaskExecutionResult();

        try {

            // 定义需要清理的表名列表
            List<String> tableNames = Arrays.asList(
                    "ke_alerts",
                    "ke_broker_metrics",
                    "ke_chat_message",
                    "ke_performance_monitor",
                    "ke_task_execution_history",
                    "ke_topics_metrics"
            );

            // 使用分布式任务协调器对表进行分片
            List<String> assignedTables = taskCoordinator.shardTables(tableNames);

            if (assignedTables.isEmpty()) {
                // 关键逻辑：即使当前节点未分配到任务，也上报空分片结果，便于统计/汇总口径一致
                Map<String, Object> shardResult = new HashMap<>();
                shardResult.put("nodeId", taskCoordinator.getCurrentNodeId());
                shardResult.put("assignedTableCount", 0);
                shardResult.put("cleanedRecords", 0);
                shardResult.put("freedSpace", 0L);
                shardResult.put("cleanupResults", Collections.emptyMap());
                shardResult.put("processedTables", Collections.emptyList());
                taskCoordinator.saveShardResult("data_cleanup", shardResult);

                result.setSuccess(true);
                result.setResult("当前节点没有分配到清理表，任务完成");
                return result;
            }

            // 执行分配给当前节点的表的数据清理
            int totalCleanedRecords = 0;
            long totalFreedSpace = 0;
            Map<String, Integer> cleanupResults = new HashMap<>();

            for (String tableName : assignedTables) {
                try {
                    int cleanedRecords = cleanupTableData(tableName, dataRetentionDays);
                    cleanupResults.put(tableName, cleanedRecords);
                    totalCleanedRecords += cleanedRecords;

                    // 估算释放的存储空间 (每条记录约100字节)
                    long freedSpace = cleanedRecords * 100L;
                    totalFreedSpace += freedSpace;

                } catch (Exception e) {
                    log.error("清理表 {} 数据失败: {}", tableName, e.getMessage(), e);
                    cleanupResults.put(tableName, 0);
                }
            }

            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("totalTableCount", tableNames.size());
            data.put("assignedTableCount", assignedTables.size());
            data.put("assignedTables", assignedTables);
            data.put("cleanedRecords", totalCleanedRecords);
            data.put("retentionDays", dataRetentionDays);
            data.put("freedSpace", totalFreedSpace);
            data.put("cleanupTime", LocalDateTime.now());
            data.put("cleanupResults", cleanupResults);
            data.put("nodeId", taskCoordinator.getCurrentNodeId());

            // 保存分片结果到Redis
            Map<String, Object> shardResult = new HashMap<>();
            shardResult.put("nodeId", taskCoordinator.getCurrentNodeId());
            shardResult.put("assignedTableCount", assignedTables.size());
            shardResult.put("cleanedRecords", totalCleanedRecords);
            shardResult.put("freedSpace", totalFreedSpace);
            shardResult.put("cleanupResults", cleanupResults);
            shardResult.put("processedTables", assignedTables);
            taskCoordinator.saveShardResult("data_cleanup", shardResult);

            result.setSuccess(true);
            result.setResult(String.format("数据清理分片完成，总共%d个表，当前节点处理%d个表，清理%d条过期记录，释放约%d字节存储空间",
                    tableNames.size(), assignedTables.size(), totalCleanedRecords, totalFreedSpace));
            result.setData(data);

        } catch (Exception e) {
            log.error("数据清理任务执行失败: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage("数据清理任务执行失败：" + e.getMessage());
        }

        // 完成任务执行并计算持续时间
        result.complete();
        return result;
    }

    /**
     * 执行性能统计任务 - 使用真实数据（支持分片）
     */
    private TaskExecutionResult executePerformanceStatsTask(TaskScheduler task) {
        TaskExecutionResult result = new TaskExecutionResult();

        try {

            // 1. 从数据库获取所有broker信息
            List<BrokerInfo> dbBrokers = brokerMapper.queryAllBrokers();
            if (dbBrokers == null || dbBrokers.isEmpty()) {
                result.setSuccess(true);
                result.setResult("数据库中没有broker信息，跳过性能统计任务");
                return result;
            }

            // 2. 获取所有broker ID列表
            List<Integer> allBrokerIds = dbBrokers.stream()
                    .map(BrokerInfo::getBrokerId)
                    .collect(Collectors.toList());

            // 3. 使用分布式任务协调器进行分片
            List<Integer> assignedBrokerIds = taskCoordinator.shardBrokers(allBrokerIds);

            if (assignedBrokerIds.isEmpty()) {
                // 关键逻辑：即使当前节点未分配到任务，也上报空分片结果，便于统计/汇总口径一致
                Map<String, Object> shardResult = new HashMap<>();
                shardResult.put("nodeId", taskCoordinator.getCurrentNodeId());
                shardResult.put("assignedBrokerCount", 0);
                shardResult.put("successCount", 0);
                shardResult.put("failureCount", 0);
                shardResult.put("savedCount", 0);
                shardResult.put("processedBrokerIds", Collections.emptyList());
                taskCoordinator.saveShardResult("performance_stats", shardResult);

                result.setSuccess(true);
                result.setResult("当前节点没有分配到broker，任务完成");
                return result;
            }

            // 4. 过滤出分配给当前节点的broker信息
            List<BrokerInfo> assignedBrokers = dbBrokers.stream()
                    .filter(broker -> assignedBrokerIds.contains(broker.getBrokerId()))
                    .collect(Collectors.toList());

            // 5. 收集分配给当前节点的broker的性能指标数据
            List<PerformanceMonitor> performanceMonitors = new ArrayList<>();
            int successCount = 0;
            int failureCount = 0;
            LocalDateTime collectTime = LocalDateTime.now();

            for (BrokerInfo broker : assignedBrokers) {
                try {
                    // 检查broker是否有JMX端口配置
                    if (broker.getJmxPort() == null || broker.getJmxPort() <= 0) {
                        log.warn("Broker {} 没有配置JMX端口，跳过性能指标收集", broker.getBrokerId());
                        failureCount++;
                        continue;
                    }

                    String brokerHost = broker.getHostIp();
                    int jmxPort = broker.getJmxPort();

                    // 收集JMX性能指标
                    BigDecimal messageIn = getBrokerJmxMetric(brokerHost, jmxPort, JmxMetricsConst.Server.MESSAGES_IN_PER_SEC.key(), MBeanMetricsConst.Common.ONE_MINUTE_RATE.key());
                    BigDecimal byteIn = getBrokerJmxMetric(brokerHost, jmxPort, JmxMetricsConst.Server.BYTES_IN_PER_SEC.key(), MBeanMetricsConst.Common.ONE_MINUTE_RATE.key());
                    BigDecimal byteOut = getBrokerJmxMetric(brokerHost, jmxPort, JmxMetricsConst.Server.BYTES_OUT_PER_SEC.key(), MBeanMetricsConst.Common.ONE_MINUTE_RATE.key());
                    BigDecimal timeMsProduce = getBrokerJmxMetric(brokerHost, jmxPort, JmxMetricsConst.Server.REQUEST_TIME_MS_PRODUCE.key(), MBeanMetricsConst.Common.MEAN.key());
                    BigDecimal timeMsConsumer = getBrokerJmxMetric(brokerHost, jmxPort, JmxMetricsConst.Server.REQUEST_TIME_MS_OFFSET_FETCH.key(), MBeanMetricsConst.Common.MEAN.key());

                    // 获取CPU和内存使用率
                    BrokerInfo brokerDetails = fetchBrokerResourceUsage(broker);

                    // 创建PerformanceMonitor对象
                    PerformanceMonitor performanceMonitor = new PerformanceMonitor();
                    performanceMonitor.setClusterId(broker.getClusterId());
                    performanceMonitor.setKafkaHost(brokerHost + ":" + broker.getPort());

                    // 直接使用BigDecimal类型（保留精度）
                    performanceMonitor.setMessageIn(messageIn);
                    performanceMonitor.setByteIn(byteIn);
                    performanceMonitor.setByteOut(byteOut);
                    performanceMonitor.setTimeMsProduce(timeMsProduce);
                    performanceMonitor.setTimeMsConsumer(timeMsConsumer);

                    // 设置CPU和内存使用率
                    if (brokerDetails.getMemoryUsage() != null) {
                        performanceMonitor.setMemoryUsage(brokerDetails.getMemoryUsage());
                    } else {
                        performanceMonitor.setMemoryUsage(BigDecimal.ZERO);
                    }

                    if (brokerDetails.getCpuUsage() != null) {
                        performanceMonitor.setCpuUsage(brokerDetails.getCpuUsage());
                    } else {
                        performanceMonitor.setCpuUsage(BigDecimal.ZERO);
                    }

                    performanceMonitor.setCollectTime(collectTime);
                    performanceMonitor.setCollectDate(collectTime.toLocalDate());

                    performanceMonitors.add(performanceMonitor);
                    successCount++;

                } catch (Exception e) {
                    log.error("收集broker {} 性能指标失败: {}", broker.getBrokerId(), e.getMessage(), e);
                    failureCount++;
                }
            }

            // 6. 批量保存性能监控数据到数据库
            int savedCount = 0;
            if (!performanceMonitors.isEmpty()) {
                try {
                    savedCount = performanceMonitorService.batchInsertPerformanceMonitors(performanceMonitors);
                } catch (Exception e) {
                    log.error("保存性能监控数据到数据库失败: {}", e.getMessage(), e);
                }
            }

            // 7. 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("totalBrokerCount", allBrokerIds.size()); // 总broker数量
            data.put("assignedBrokerCount", assignedBrokers.size()); // 当前节点处理的broker数量
            data.put("assignedBrokerIds", assignedBrokerIds); // 当前节点处理的broker ID列表
            data.put("successCount", successCount); // 成功收集的broker数量
            data.put("failureCount", failureCount); // 失败的broker数量
            data.put("savedCount", savedCount); // 保存到数据库的记录数
            data.put("collectTime", collectTime);
            data.put("nodeId", taskCoordinator.getCurrentNodeId()); // 当前节点ID

            // 保存分片结果到Redis
            Map<String, Object> shardResult = new HashMap<>();
            shardResult.put("nodeId", taskCoordinator.getCurrentNodeId());
            shardResult.put("assignedBrokerCount", assignedBrokers.size());
            shardResult.put("successCount", successCount);
            shardResult.put("failureCount", failureCount);
            shardResult.put("savedCount", savedCount);
            shardResult.put("processedBrokerIds", assignedBrokerIds);
            taskCoordinator.saveShardResult("performance_stats", shardResult);

            result.setSuccess(true);
            result.setResult(String.format("性能统计分片完成，总共%d个broker，当前节点处理%d个broker，成功%d个，失败%d个，保存%d条记录到数据库",
                    allBrokerIds.size(), assignedBrokers.size(), successCount, failureCount, savedCount));
            result.setData(data);

        } catch (Exception e) {
            log.error("性能统计任务执行失败: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage("性能统计任务执行失败：" + e.getMessage());
        }

        // 完成任务执行并计算持续时间
        result.complete();
        return result;
    }

    /**
     * 获取broker的资源使用率（CPU和内存）
     */
    private BrokerInfo fetchBrokerResourceUsage(BrokerInfo brokerInfo) {
        BrokerInfo result = new BrokerInfo();
        result.setCpuUsage(BigDecimal.ZERO);
        result.setMemoryUsage(BigDecimal.ZERO);

        if (brokerInfo.getJmxPort() == null || brokerInfo.getJmxPort() <= 0) {
            return result;
        }

        try {
            // 使用KafkaClusterFetcher获取broker资源使用率
            JMXInitializeInfo initializeInfo = new JMXInitializeInfo();
            initializeInfo.setBrokerId(String.valueOf(brokerInfo.getBrokerId()));
            initializeInfo.setHost(brokerInfo.getHostIp());
            initializeInfo.setPort(brokerInfo.getJmxPort());
            initializeInfo.setUri("service:jmx:rmi:///jndi/rmi://%s/jmxrmi");

            BrokerInfo jmxBrokerInfo = KafkaClusterFetcher.fetchBrokerDetails(initializeInfo);

            if (jmxBrokerInfo != null) {
                result.setCpuUsage(jmxBrokerInfo.getCpuUsage());
                result.setMemoryUsage(jmxBrokerInfo.getMemoryUsage());

            }

        } catch (Exception e) {
        }

        return result;
    }

    /**
     * 更新broker信息到数据库
     * 注意：host、port、jmx_port字段不会被覆盖，保持原有值
     */
    private void updateBrokerInfoInDatabase(BrokerDetailedInfo broker, String clusterId) {
        try {
            // 检查broker是否已存在 - 使用新的方法确保唯一性
            BrokerInfo existingBroker = brokerMapper.getBrokerByClusterIdAndBrokerId(clusterId, broker.getBrokerId());

            if (existingBroker != null) {
                // 更新现有broker的动态信息，不更新host、port、jmx_port字段
                int updateResult = brokerMapper.updateBrokerDynamicInfo(
                        clusterId,
                        broker.getBrokerId(),
                        broker.getStatus().toLowerCase(),
                        java.math.BigDecimal.valueOf(broker.getCpuUsagePercent()),
                        java.math.BigDecimal.valueOf(broker.getMemoryUsagePercent()),
                        broker.getStartTime(),
                        broker.getVersion());

                if (updateResult > 0) {
                } else {
                    log.warn("更新broker {} 动态信息到数据库失败，影响行数为0", broker.getBrokerId());
                }
            } else {
                // 创建新的broker信息
                BrokerInfo newBroker = new BrokerInfo();
                newBroker.setClusterId(clusterId);
                newBroker.setBrokerId(broker.getBrokerId());
                newBroker.setHostIp(broker.getHost());
                newBroker.setPort(broker.getPort());
                // 注意：新创建的broker不设置jmx_port，需要用户手动配置
                newBroker.setStatus(broker.getStatus().toLowerCase());
                newBroker.setCpuUsage(java.math.BigDecimal.valueOf(broker.getCpuUsagePercent()));
                newBroker.setMemoryUsage(java.math.BigDecimal.valueOf(broker.getMemoryUsagePercent()));
                newBroker.setStartupTime(broker.getStartTime());
                newBroker.setVersion(broker.getVersion());
                newBroker.setCreatedBy("system");

                int createResult = brokerMapper.createBroker(newBroker);
                if (createResult > 0) {
                } else {
                    log.warn("创建broker {} 信息到数据库失败，影响行数为0", broker.getBrokerId());
                }
            }
        } catch (Exception e) {
            log.error("更新broker {} 信息到数据库失败: {}", broker.getBrokerId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取Broker详细信息（使用真实JMX数据）
     */
    private List<BrokerDetailedInfo> fetchBrokerDetailedInfos(List<BrokerInfo> brokerInfos) {
        List<BrokerDetailedInfo> detailedInfos = new ArrayList<>();

        if (brokerInfos == null || brokerInfos.isEmpty()) {
            return detailedInfos;
        }

        for (BrokerInfo brokerInfo : brokerInfos) {
            try {
                BrokerDetailedInfo detailedInfo = fetchSingleBrokerInfo(brokerInfo);
                if (detailedInfo != null) {
                    detailedInfos.add(detailedInfo);
                }
            } catch (Exception e) {
                log.error("获取broker {} 详细信息失败: {}", brokerInfo.getBrokerId(), e.getMessage(), e);

                // 创建一个默认的BrokerDetailedInfo，标记为离线状态
                BrokerDetailedInfo offlineBroker = new BrokerDetailedInfo();
                offlineBroker.setBrokerId(brokerInfo.getBrokerId());
                offlineBroker.setHost(brokerInfo.getHostIp());
                offlineBroker.setPort(brokerInfo.getPort());
                offlineBroker.setJmxPort(brokerInfo.getJmxPort() != null ? brokerInfo.getJmxPort() : 0);
                offlineBroker.setStatus("OFFLINE");
                offlineBroker.setVersion("Unknown");
                offlineBroker.setCpuUsagePercent(0.0);
                offlineBroker.setMemoryUsagePercent(0.0);
                offlineBroker.setLastUpdateTime(LocalDateTime.now());

                detailedInfos.add(offlineBroker);
            }
        }

        return detailedInfos;
    }

    /**
     * 获取单个Broker的详细信息
     */
    private BrokerDetailedInfo fetchSingleBrokerInfo(BrokerInfo brokerInfo) {
        if (brokerInfo.getJmxPort() == null || brokerInfo.getJmxPort() <= 0) {
            log.warn("Broker {} 没有配置JMX端口，跳过JMX数据获取", brokerInfo.getBrokerId());

            // 返回基本信息，标记为UNKNOWN状态
            BrokerDetailedInfo detailedInfo = new BrokerDetailedInfo();
            detailedInfo.setBrokerId(brokerInfo.getBrokerId());
            detailedInfo.setHost(brokerInfo.getHostIp());
            detailedInfo.setPort(brokerInfo.getPort());
            detailedInfo.setJmxPort(0);
            detailedInfo.setStatus("UNKNOWN");
            detailedInfo.setVersion("Unknown");
            detailedInfo.setCpuUsagePercent(0.0);
            detailedInfo.setMemoryUsagePercent(0.0);
            detailedInfo.setLastUpdateTime(LocalDateTime.now());

            return detailedInfo;
        }

        try {
            // 使用KafkaClusterFetcher获取Broker详细信息
            JMXInitializeInfo initializeInfo = new JMXInitializeInfo();
            initializeInfo.setBrokerId(String.valueOf(brokerInfo.getBrokerId()));
            initializeInfo.setHost(brokerInfo.getHostIp());
            initializeInfo.setPort(brokerInfo.getJmxPort());

            // 使用标准JMX连接URI
            initializeInfo.setUri("service:jmx:rmi:///jndi/rmi://%s/jmxrmi");

            BrokerInfo jmxBrokerInfo = KafkaClusterFetcher.fetchBrokerDetails(initializeInfo);

            // 转换为BrokerDetailedInfo
            BrokerDetailedInfo detailedInfo = new BrokerDetailedInfo();
            detailedInfo.setBrokerId(brokerInfo.getBrokerId());
            detailedInfo.setHost(brokerInfo.getHostIp());
            detailedInfo.setPort(brokerInfo.getPort());
            detailedInfo.setJmxPort(brokerInfo.getJmxPort());

            if (jmxBrokerInfo.getVersion() != null && !jmxBrokerInfo.getVersion().trim().isEmpty()) {
                detailedInfo.setVersion(jmxBrokerInfo.getVersion());
                detailedInfo.setStatus("ONLINE");
            } else {
                detailedInfo.setVersion("Unknown");
                detailedInfo.setStatus("OFFLINE");
            }

            if (jmxBrokerInfo.getStartupTime() != null) {
                detailedInfo.setStartTime(jmxBrokerInfo.getStartupTime());

                // 计算运行时长
                LocalDateTime now = LocalDateTime.now();
                long uptimeSeconds = java.time.Duration.between(jmxBrokerInfo.getStartupTime(), now).getSeconds();
                detailedInfo.setUptimeSeconds(uptimeSeconds);
            }

            // 设置CPU和内存使用率
            if (jmxBrokerInfo.getCpuUsage() != null) {
                detailedInfo.setCpuUsagePercent(jmxBrokerInfo.getCpuUsage().doubleValue());
            }

            if (jmxBrokerInfo.getMemoryUsage() != null) {
                detailedInfo.setMemoryUsagePercent(jmxBrokerInfo.getMemoryUsage().doubleValue());
            }

            detailedInfo.setLastUpdateTime(LocalDateTime.now());

            return detailedInfo;

        } catch (Exception e) {
            log.error("通过JMX获取broker {} 详细信息失败: {}", brokerInfo.getBrokerId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 收集主题指标数据
     */
    private TopicMetrics collectTopicMetrics(KafkaClientInfo kafkaClientInfo, List<BrokerInfo> brokers, String topicName, KafkaSchemaFactory ksf) {
        TopicMetrics metrics = new TopicMetrics();
        metrics.setTopicName(topicName);
        metrics.setClusterId(kafkaClientInfo.getClusterId());
        LocalDateTime collectTime = LocalDateTime.now();
        metrics.setCollectTime(collectTime);
        metrics.setCreateTime(collectTime);

        try {
            // 1. 获取topic容量（使用已实现的方法）
            Long capacity = ksf.getTopicRecordCapacityNum(kafkaClientInfo, brokers, topicName);
            metrics.setCapacity(capacity != null ? capacity : 0L);

            // 2. 获取topic消息记录数
            Long recordCount = ksf.getTotalTopicLogSize(kafkaClientInfo, topicName);
            metrics.setRecordCount(recordCount != null ? recordCount : 0L);

            // 3. 计算增量 - 获取数据库中该Topic的最新记录
            Long recordCountDiff = 0L;
            Long capacityDiff = 0L;

            try {
                TopicMetrics latestMetrics = topicMetricsMapper.selectLatestTopicMetricsByClusterAndTopic(
                        kafkaClientInfo.getClusterId(), topicName);

                if (latestMetrics != null) {
                    // 计算增量：当前值 - 数据库最新值
                    recordCountDiff = metrics.getRecordCount() - latestMetrics.getRecordCount();
                    capacityDiff = metrics.getCapacity() - latestMetrics.getCapacity();

                }
            } catch (Exception e) {
                log.warn("获取主题 {} 历史指标数据失败，使用当前值作为增量: {}", topicName, e.getMessage());
            }

            // 设置增量值
            metrics.setRecordCountDiff(recordCountDiff);
            metrics.setCapacityDiff(capacityDiff);

            // 4. 获取写入和读取速度
            BigDecimal writeSpeed = getTopicJmxMetric(brokers, topicName, JmxMetricsConst.Server.BYTES_IN_PER_SEC_TOPIC.key());
            BigDecimal readSpeed = getTopicJmxMetric(brokers, topicName, JmxMetricsConst.Server.BYTES_OUT_PER_SEC_TOPIC.key());

            metrics.setWriteSpeed(writeSpeed != null ? writeSpeed : BigDecimal.ZERO);
            metrics.setReadSpeed(readSpeed != null ? readSpeed : BigDecimal.ZERO);

            return metrics;

        } catch (Exception e) {
            log.error("收集主题 {} 指标数据时发生异常: {}", topicName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取当前 topic 实时指标
     */
    private List<TopicInstantMetrics> collectTopicInstantMetrics(KafkaClientInfo kafkaClientInfo, List<BrokerInfo> brokers, String topicName, KafkaSchemaFactory ksf) {
        List<TopicInstantMetrics> metricsList = new ArrayList<>();
        LocalDateTime collectTime = LocalDateTime.now();

        try {
            // 1. 获取topic容量
            Long capacity = ksf.getTopicRecordCapacityNum(kafkaClientInfo, brokers, topicName);
            TopicInstantMetrics capacityMetrics = new TopicInstantMetrics();
            capacityMetrics.setTopicName(topicName);
            capacityMetrics.setClusterId(kafkaClientInfo.getClusterId());
            capacityMetrics.setMetricType(MBeanMetricsConst.Topic.CAPACITY.key());
            capacityMetrics.setMetricValue(String.valueOf(capacity != null ? capacity : 0L));
            capacityMetrics.setLastUpdated(collectTime);
            capacityMetrics.setCreateTime(collectTime);
            metricsList.add(capacityMetrics);

            // 2. 获取topic消息记录数
            Long recordCount = ksf.getTotalActualTopicLogSize(kafkaClientInfo, topicName);
            TopicInstantMetrics logsizeMetrics = new TopicInstantMetrics();
            logsizeMetrics.setTopicName(topicName);
            logsizeMetrics.setClusterId(kafkaClientInfo.getClusterId());
            logsizeMetrics.setMetricType(MBeanMetricsConst.Topic.LOG_SIZE.key());
            logsizeMetrics.setMetricValue(String.valueOf(recordCount != null ? recordCount : 0L));
            logsizeMetrics.setLastUpdated(collectTime);
            logsizeMetrics.setCreateTime(collectTime);
            metricsList.add(logsizeMetrics);

            // 3. 获取写入速度
            BigDecimal writeSpeed = getTopicJmxMetric(brokers, topicName, JmxMetricsConst.Server.BYTES_IN_PER_SEC_TOPIC.key());
            TopicInstantMetrics byteInMetrics = new TopicInstantMetrics();
            byteInMetrics.setTopicName(topicName);
            byteInMetrics.setClusterId(kafkaClientInfo.getClusterId());
            byteInMetrics.setMetricType(MBeanMetricsConst.Topic.BYTE_IN.key());
            byteInMetrics.setMetricValue(String.valueOf(writeSpeed != null ? writeSpeed : BigDecimal.ZERO));
            byteInMetrics.setLastUpdated(collectTime);
            byteInMetrics.setCreateTime(collectTime);
            metricsList.add(byteInMetrics);

            // 4. 获取读取速度
            BigDecimal readSpeed = getTopicJmxMetric(brokers, topicName, JmxMetricsConst.Server.BYTES_OUT_PER_SEC_TOPIC.key());
            TopicInstantMetrics byteOutMetrics = new TopicInstantMetrics();
            byteOutMetrics.setTopicName(topicName);
            byteOutMetrics.setClusterId(kafkaClientInfo.getClusterId());
            byteOutMetrics.setMetricType(MBeanMetricsConst.Topic.BYTE_OUT.key());
            byteOutMetrics.setMetricValue(String.valueOf(readSpeed != null ? readSpeed : BigDecimal.ZERO));
            byteOutMetrics.setLastUpdated(collectTime);
            byteOutMetrics.setCreateTime(collectTime);
            metricsList.add(byteOutMetrics);

        } catch (Exception e) {
            log.error("收集主题 {} 即时指标数据时发生异常: {}", topicName, e.getMessage(), e);
        }
        return metricsList;
    }

    /**
     * 通过JMX获取主题指标
     */
    private BigDecimal getTopicJmxMetric(List<BrokerInfo> brokers, String topicName, String jmxObjectNameTemplate) {
        if (brokers == null || brokers.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 使用第一个有JMX端口的broker获取指标
        for (BrokerInfo broker : brokers) {
            if (broker.getJmxPort() == null || broker.getJmxPort() <= 0) {
                continue;
            }

            try {
                JMXInitializeInfo initializeInfo = new JMXInitializeInfo();
                initializeInfo.setBrokerId(String.valueOf(broker.getBrokerId()));
                initializeInfo.setHost(broker.getHostIp());
                initializeInfo.setPort(broker.getJmxPort());
                initializeInfo.setUri("service:jmx:rmi:///jndi/rmi://%s/jmxrmi");

                String objectName = String.format(jmxObjectNameTemplate, topicName);
                initializeInfo.setObjectName(objectName);

                // 使用KafkaClusterFetcher执行JMX操作获取OneMinuteRate
                final BigDecimal[] result = {BigDecimal.ZERO};
                executeJmxOperation(initializeInfo, mbeanConnection -> {
                    try {
                        Object value = mbeanConnection.getAttribute(
                                new javax.management.ObjectName(objectName),
                                MBeanMetricsConst.Common.ONE_MINUTE_RATE.key()
                        );
                        if (value != null) {
                            // 保留2位小数
                            result[0] = new BigDecimal(value.toString()).setScale(2, java.math.RoundingMode.HALF_UP);
                        }
                    } catch (Exception e) {
                        // 当 topic 没有流量读取或者写入的时候，JMX 指标中是不会显示该 topic 名称的
                    }
                });

                return result[0];

            } catch (Exception e) {
            }
        }

        return BigDecimal.ZERO;
    }

    /**
     * 获取Broker 指标
     */
    private BigDecimal getBrokerJmxMetric(String brokerHost, int jmxPort, String objectName, String attribute) {

        // 使用第一个有JMX端口的broker获取指标

        try {
            JMXInitializeInfo initializeInfo = new JMXInitializeInfo();
            initializeInfo.setHost(brokerHost);
            initializeInfo.setPort(jmxPort);
            initializeInfo.setUri("service:jmx:rmi:///jndi/rmi://%s/jmxrmi");

            initializeInfo.setObjectName(objectName);

            // 使用KafkaClusterFetcher执行JMX操作获取OneMinuteRate
            final BigDecimal[] result = {BigDecimal.ZERO};
            executeJmxOperation(initializeInfo, mbeanConnection -> {
                try {
                    Object value = mbeanConnection.getAttribute(
                            new javax.management.ObjectName(objectName),
                            attribute
                    );
                    if (value != null) {
                        // 保留2位小数
                        result[0] = new BigDecimal(value.toString()).setScale(2, java.math.RoundingMode.HALF_UP);
                    }
                } catch (Exception e) {
                }
            });

            return result[0];

        } catch (Exception e) {
        }

        return BigDecimal.ZERO;
    }

    /**
     * 执行JMX操作的辅助方法
     */
    private void executeJmxOperation(JMXInitializeInfo initializeInfo, JMXOperation operation) {
        javax.management.remote.JMXConnector connector = null;
        try {
            javax.management.remote.JMXServiceURL jmxUrl = new javax.management.remote.JMXServiceURL(
                    String.format(initializeInfo.getUri(), initializeInfo.getHost() + ":" + initializeInfo.getPort()));
            initializeInfo.setUrl(jmxUrl);

            // 创建连接
            connector = javax.management.remote.JMXConnectorFactory.connect(jmxUrl);
            operation.execute(connector.getMBeanServerConnection());
        } catch (Exception e) {
        } finally {
            if (connector != null) {
                try {
                    connector.close();
                } catch (java.io.IOException e) {
                    log.error("Failed to close JMX connector: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 保存主题指标到数据库
     */
    private void saveTopicMetrics(TopicMetrics metrics) {
        try {
            int result = topicMetricsMapper.insert(metrics);
            if (result <= 0) {
                log.warn("保存主题指标数据失败，影响行数为0: {}", metrics.getTopicName());
            }
        } catch (Exception e) {
            log.error("保存主题指标数据到数据库失败: {}", metrics.getTopicName(), e);
        }
    }

    /**
     * JMX操作接口
     */
    @FunctionalInterface
    private interface JMXOperation {
        void execute(javax.management.MBeanServerConnection mbeanConnection) throws Exception;
    }

    /**
     * 将消费者组主题信息保存到数据库
     */
    private int saveConsumerGroupTopicInfosToDatabase(List<ConsumerGroupTopicInfo> consumerGroupTopicInfos) {
        int savedCount = 0;

        if (consumerGroupTopicInfos == null || consumerGroupTopicInfos.isEmpty()) {
            return savedCount;
        }

        try {
            // 准备批量插入的数据
            List<org.kafka.eagle.dto.consumer.ConsumerGroupTopicInsertRequest> insertRequests = new ArrayList<>();

            for (ConsumerGroupTopicInfo cgti : consumerGroupTopicInfos) {
                try {
                    org.kafka.eagle.dto.consumer.ConsumerGroupTopicInsertRequest insertRequest =
                            new org.kafka.eagle.dto.consumer.ConsumerGroupTopicInsertRequest();

                    insertRequest.setClusterId(cgti.getClusterId());
                    insertRequest.setGroupId(cgti.getGroupId());
                    insertRequest.setTopicName(cgti.getTopicName());
                    insertRequest.setState(cgti.getState());
                    insertRequest.setLogsize(cgti.getLogsize());
                    insertRequest.setOffsets(cgti.getOffsets());
                    insertRequest.setLags(cgti.getLags());
                    insertRequest.setCollectTime(cgti.getCollectTime());
                    insertRequest.setCollectDate(cgti.getCollectDate());

                    insertRequests.add(insertRequest);

                } catch (Exception e) {
                    log.error("转换消费者组主题信息失败: {} (集群: {}, 组: {}, 主题: {}), 错误: {}",
                            cgti, cgti.getClusterId(), cgti.getGroupId(), cgti.getTopicName(), e.getMessage(), e);
                }
            }

            if (!insertRequests.isEmpty()) {
                // 使用注入的 ConsumerGroupTopicService 进行批量插入
                boolean success = consumerGroupTopicService.batchInsertConsumerGroupTopic(insertRequests);
                if (success) {
                    savedCount = insertRequests.size();
                } else {
                    log.error("批量保存消费者组主题信息到数据库失败");
                }
            }

        } catch (Exception e) {
            log.error("批量保存消费者组主题信息时发生异常", e);
        }

        return savedCount;
    }

    /**
     * 清理指定表的过期数据
     */
    private int cleanupTableData(String tableName, int retentionDays) {
        try {

            // 根据表名确定时间字段名称
            String timeColumn = getTimeColumnForTable(tableName);
            if (timeColumn == null) {
                log.warn("表 {} 没有配置时间字段，跳过清理", tableName);
                return 0;
            }

            // 使用 DataCleanupService 执行数据清理
            int deletedCount = dataCleanupService.cleanupExpiredData(tableName, timeColumn, retentionDays);

            return deletedCount;

        } catch (Exception e) {
            log.error("清理表 {} 数据失败: {}", tableName, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取表对应的时间字段名称
     */
    private String getTimeColumnForTable(String tableName) {
        switch (tableName) {
            case "ke_alerts":
                return "created_at";
            case "ke_broker_metrics":
                return "create_time";
            case "ke_chat_message":
                return "create_time";
            case "ke_performance_monitor":
                return "collect_time";
            case "ke_task_execution_history":
                return "created_time";
            case "ke_topics_metrics":
                return "create_time";
            default:
                log.warn("未知的表名: {}", tableName);
                return null;
        }
    }

    /**
     * 计算告警持续时间
     *
     * @param createdAt 告警创建时间
     * @param updatedAt 告警更新时间
     * @return 格式化的持续时间字符串 (如: "2h 30m 15s" 或 "45m 30s" 或 "30s")
     */
    private String calculateAlertDuration(LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (createdAt == null || updatedAt == null) {
            return "0s";
        }

        java.time.Duration duration = java.time.Duration.between(createdAt, updatedAt);
        long totalSeconds = duration.getSeconds();

        if (totalSeconds < 0) {
            return "0s";
        }

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder durationStr = new StringBuilder();

        if (hours > 0) {
            durationStr.append(hours).append("h ");
        }
        if (minutes > 0) {
            durationStr.append(minutes).append("m ");
        }
        if (seconds > 0 || durationStr.length() == 0) {
            durationStr.append(seconds).append("s");
        }

        return durationStr.toString().trim();
    }

}
