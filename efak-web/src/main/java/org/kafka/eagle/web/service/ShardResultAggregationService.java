package org.kafka.eagle.web.service;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.web.scheduler.DistributedTaskCoordinator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * ShardResultAggregation 服务接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/30 01:16:29
 * @version 5.0.0
 */
@Slf4j
@Service
public class ShardResultAggregationService {

    @Autowired
    private DistributedTaskCoordinator taskCoordinator;

    /**
     * 汇总集群监控任务的分片结果
     * @param waitTimeSeconds 等待其他节点完成的时间（秒）
     * @return 汇总结果
     */
    public Map<String, Object> aggregateClusterMonitorResults(int waitTimeSeconds) {
        return aggregateShardResults("cluster_monitor", waitTimeSeconds);
    }

    /**
     * 汇总主题监控任务的分片结果
     * @param waitTimeSeconds 等待其他节点完成的时间（秒）
     * @return 汇总结果
     */
    public Map<String, Object> aggregateTopicMonitorResults(int waitTimeSeconds) {
        return aggregateShardResults("topic_monitor", waitTimeSeconds);
    }

    /**
     * 汇总消费者监控任务的分片结果
     * @param waitTimeSeconds 等待其他节点完成的时间（秒）
     * @return 汇总结果
     */
    public Map<String, Object> aggregateConsumerMonitorResults(int waitTimeSeconds) {
        return aggregateShardResults("consumer_monitor", waitTimeSeconds);
    }

    /**
     * 汇总告警监控任务的分片结果
     * @param waitTimeSeconds 等待其他节点完成的时间（秒）
     * @return 汇总结果
     */
    public Map<String, Object> aggregateAlertMonitorResults(int waitTimeSeconds) {
        return aggregateShardResults("alert_monitor", waitTimeSeconds);
    }

    /**
     * 汇总数据清理任务的分片结果
     * @param waitTimeSeconds 等待其他节点完成的时间（秒）
     * @return 汇总结果
     */
    public Map<String, Object> aggregateDataCleanupResults(int waitTimeSeconds) {
        return aggregateShardResults("data_cleanup", waitTimeSeconds);
    }

    /**
     * 汇总性能统计任务的分片结果
     * @param waitTimeSeconds 等待其他节点完成的时间（秒）
     * @return 汇总结果
     */
    public Map<String, Object> aggregatePerformanceStatsResults(int waitTimeSeconds) {
        return aggregateShardResults("performance_stats", waitTimeSeconds);
    }

    /**
     * 汇总分片任务结果
     * @param taskType 任务类型
     * @param waitTimeSeconds 等待其他节点完成的时间（秒）
     * @return 汇总结果
     */
    private Map<String, Object> aggregateShardResults(String taskType, int waitTimeSeconds) {
        try {
            
            // 等待其他节点完成任务
            if (waitTimeSeconds > 0) {
                try {
                    TimeUnit.SECONDS.sleep(waitTimeSeconds);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("等待被中断", e);
                }
            }
            
            // 获取所有节点的分片结果
            Map<String, Object> allShardResults = taskCoordinator.getAllShardResults(taskType);
            
            if (allShardResults.isEmpty()) {
                log.warn("没有找到{}任务的分片结果", taskType);
                return createEmptyAggregationResult(taskType);
            }
            
            // 根据任务类型进行不同的汇总逻辑
            switch (taskType) {
                case "cluster_monitor":
                    return aggregateClusterMonitorShardResults(allShardResults);
                case "topic_monitor":
                    return aggregateTopicMonitorShardResults(allShardResults);
                case "consumer_monitor":
                    return aggregateConsumerMonitorShardResults(allShardResults);
                case "alert_monitor":
                    return aggregateAlertMonitorShardResults(allShardResults);
                case "data_cleanup":
                    return aggregateDataCleanupShardResults(allShardResults);
                case "performance_stats":
                    return aggregatePerformanceStatsShardResults(allShardResults);
                default:
                    log.warn("不支持的任务类型: {}", taskType);
                    return createEmptyAggregationResult(taskType);
            }
            
        } catch (Exception e) {
            log.error("汇总{}任务分片结果失败", taskType, e);
            return createErrorAggregationResult(taskType, e.getMessage());
        }
    }

    /**
     * 汇总集群监控分片结果
     */
    private Map<String, Object> aggregateClusterMonitorShardResults(Map<String, Object> allShardResults) {
        Map<String, Object> aggregatedResult = new HashMap<>();
        
        int totalOnlineBrokers = 0;
        int totalOfflineBrokers = 0;
        int totalUpdatedBrokers = 0;
        Set<Integer> allProcessedBrokerIds = new HashSet<>();
        List<String> participatingNodes = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : allShardResults.entrySet()) {
            String nodeId = entry.getKey();
            participatingNodes.add(nodeId);
            
            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> shardResult = (Map<String, Object>) entry.getValue();
                
                // 累加各项指标
                totalOnlineBrokers += getIntValue(shardResult, "onlineBrokers");
                totalOfflineBrokers += getIntValue(shardResult, "offlineBrokers");
                totalUpdatedBrokers += getIntValue(shardResult, "updatedBrokers");
                
                // 收集处理的broker ID
                Object processedBrokerIds = shardResult.get("processedBrokerIds");
                if (processedBrokerIds instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Integer> brokerIds = (List<Integer>) processedBrokerIds;
                    allProcessedBrokerIds.addAll(brokerIds);
                }
            }
        }
        
        aggregatedResult.put("taskType", "cluster_monitor");
        aggregatedResult.put("participatingNodes", participatingNodes);
        aggregatedResult.put("nodeCount", participatingNodes.size());
        aggregatedResult.put("totalProcessedBrokers", allProcessedBrokerIds.size());
        aggregatedResult.put("totalOnlineBrokers", totalOnlineBrokers);
        aggregatedResult.put("totalOfflineBrokers", totalOfflineBrokers);
        aggregatedResult.put("totalUpdatedBrokers", totalUpdatedBrokers);
        aggregatedResult.put("processedBrokerIds", new ArrayList<>(allProcessedBrokerIds));
        aggregatedResult.put("aggregationTime", new Date());

        return aggregatedResult;
    }

    /**
     * 汇总主题监控分片结果
     */
    private Map<String, Object> aggregateTopicMonitorShardResults(Map<String, Object> allShardResults) {
        Map<String, Object> aggregatedResult = new HashMap<>();
        
        long totalPartitionCount = 0;
        Set<String> allProcessedTopicNames = new HashSet<>();
        List<String> participatingNodes = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : allShardResults.entrySet()) {
            String nodeId = entry.getKey();
            participatingNodes.add(nodeId);
            
            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> shardResult = (Map<String, Object>) entry.getValue();
                
                // 累加各项指标
                totalPartitionCount += getLongValue(shardResult, "partitionCount");
                
                // 收集处理的主题名称
                Object processedTopicNames = shardResult.get("processedTopicNames");
                if (processedTopicNames instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> topicNames = (List<String>) processedTopicNames;
                    allProcessedTopicNames.addAll(topicNames);
                }
            }
        }
        
        aggregatedResult.put("taskType", "topic_monitor");
        aggregatedResult.put("participatingNodes", participatingNodes);
        aggregatedResult.put("nodeCount", participatingNodes.size());
        aggregatedResult.put("totalProcessedTopics", allProcessedTopicNames.size());
        aggregatedResult.put("totalPartitionCount", totalPartitionCount);
        aggregatedResult.put("avgPartitionPerTopic", allProcessedTopicNames.isEmpty() ? 0 : totalPartitionCount / allProcessedTopicNames.size());
        aggregatedResult.put("processedTopicNames", new ArrayList<>(allProcessedTopicNames));
        aggregatedResult.put("aggregationTime", new Date());

        return aggregatedResult;
    }

    /**
     * 汇总消费者监控分片结果
     */
    private Map<String, Object> aggregateConsumerMonitorShardResults(Map<String, Object> allShardResults) {
        Map<String, Object> aggregatedResult = new HashMap<>();
        
        int totalActiveConsumers = 0;
        int totalLagConsumers = 0;
        long totalLag = 0;
        Set<String> allProcessedConsumerGroupIds = new HashSet<>();
        List<String> participatingNodes = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : allShardResults.entrySet()) {
            String nodeId = entry.getKey();
            participatingNodes.add(nodeId);
            
            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> shardResult = (Map<String, Object>) entry.getValue();
                
                // 累加各项指标
                totalActiveConsumers += getIntValue(shardResult, "activeConsumers");
                totalLagConsumers += getIntValue(shardResult, "lagConsumers");
                totalLag += getLongValue(shardResult, "totalLag");
                
                // 收集处理的消费者组ID
                Object processedConsumerGroupIds = shardResult.get("processedConsumerGroupIds");
                if (processedConsumerGroupIds instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> consumerGroupIds = (List<String>) processedConsumerGroupIds;
                    allProcessedConsumerGroupIds.addAll(consumerGroupIds);
                }
            }
        }
        
        aggregatedResult.put("taskType", "consumer_monitor");
        aggregatedResult.put("participatingNodes", participatingNodes);
        aggregatedResult.put("nodeCount", participatingNodes.size());
        aggregatedResult.put("totalProcessedConsumerGroups", allProcessedConsumerGroupIds.size());
        aggregatedResult.put("totalActiveConsumers", totalActiveConsumers);
        aggregatedResult.put("totalLagConsumers", totalLagConsumers);
        aggregatedResult.put("totalLag", totalLag);
        aggregatedResult.put("avgLag", totalLagConsumers > 0 ? totalLag / totalLagConsumers : 0);
        aggregatedResult.put("processedConsumerGroupIds", new ArrayList<>(allProcessedConsumerGroupIds));
        aggregatedResult.put("aggregationTime", new Date());

        return aggregatedResult;
    }

    /**
     * 创建空的汇总结果
     */
    private Map<String, Object> createEmptyAggregationResult(String taskType) {
        Map<String, Object> result = new HashMap<>();
        result.put("taskType", taskType);
        result.put("participatingNodes", Collections.emptyList());
        result.put("nodeCount", 0);
        result.put("aggregationTime", new Date());
        result.put("status", "empty");
        return result;
    }

    /**
     * 创建错误的汇总结果
     */
    private Map<String, Object> createErrorAggregationResult(String taskType, String errorMessage) {
        Map<String, Object> result = new HashMap<>();
        result.put("taskType", taskType);
        result.put("participatingNodes", Collections.emptyList());
        result.put("nodeCount", 0);
        result.put("aggregationTime", new Date());
        result.put("status", "error");
        result.put("errorMessage", errorMessage);
        return result;
    }

    /**
     * 安全获取整数值
     */
    private int getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    /**
     * 安全获取长整数值
     */
    private long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }

    /**
     * 汇总告警监控分片结果
     */
    private Map<String, Object> aggregateAlertMonitorShardResults(Map<String, Object> allShardResults) {
        Map<String, Object> aggregatedResult = new HashMap<>();

        int totalEvaluatedCount = 0;
        int totalTriggeredCount = 0;
        int totalSentCount = 0;
        Set<Long> allProcessedConfigIds = new HashSet<>();
        List<String> participatingNodes = new ArrayList<>();

        for (Map.Entry<String, Object> entry : allShardResults.entrySet()) {
            String nodeId = entry.getKey();
            participatingNodes.add(nodeId);

            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> shardResult = (Map<String, Object>) entry.getValue();

                // 累加各项指标
                totalEvaluatedCount += getIntValue(shardResult, "evaluatedCount");
                totalTriggeredCount += getIntValue(shardResult, "triggeredCount");
                totalSentCount += getIntValue(shardResult, "sentCount");

                // 收集处理的配置ID
                Object processedConfigIds = shardResult.get("processedConfigIds");
                if (processedConfigIds instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Long> configIds = (List<Long>) processedConfigIds;
                    allProcessedConfigIds.addAll(configIds);
                }
            }
        }

        aggregatedResult.put("taskType", "alert_monitor");
        aggregatedResult.put("participatingNodes", participatingNodes);
        aggregatedResult.put("nodeCount", participatingNodes.size());
        aggregatedResult.put("totalProcessedConfigs", allProcessedConfigIds.size());
        aggregatedResult.put("totalEvaluatedCount", totalEvaluatedCount);
        aggregatedResult.put("totalTriggeredCount", totalTriggeredCount);
        aggregatedResult.put("totalSentCount", totalSentCount);
        aggregatedResult.put("triggerRate", totalEvaluatedCount > 0 ? (double) totalTriggeredCount / totalEvaluatedCount : 0.0);
        aggregatedResult.put("processedConfigIds", new ArrayList<>(allProcessedConfigIds));
        aggregatedResult.put("aggregationTime", new Date());

        return aggregatedResult;
    }

    /**
     * 汇总数据清理分片结果
     */
    private Map<String, Object> aggregateDataCleanupShardResults(Map<String, Object> allShardResults) {
        Map<String, Object> aggregatedResult = new HashMap<>();

        int totalCleanedRecords = 0;
        long totalFreedSpace = 0;
        Set<String> allProcessedTables = new HashSet<>();
        List<String> participatingNodes = new ArrayList<>();
        Map<String, Integer> aggregatedCleanupResults = new HashMap<>();

        for (Map.Entry<String, Object> entry : allShardResults.entrySet()) {
            String nodeId = entry.getKey();
            participatingNodes.add(nodeId);

            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> shardResult = (Map<String, Object>) entry.getValue();

                // 累加各项指标
                totalCleanedRecords += getIntValue(shardResult, "cleanedRecords");
                totalFreedSpace += getLongValue(shardResult, "freedSpace");

                // 收集处理的表名
                Object processedTables = shardResult.get("processedTables");
                if (processedTables instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> tableNames = (List<String>) processedTables;
                    allProcessedTables.addAll(tableNames);
                }

                // 汇总清理结果
                Object cleanupResults = shardResult.get("cleanupResults");
                if (cleanupResults instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Integer> tableCleanupResults = (Map<String, Integer>) cleanupResults;
                    for (Map.Entry<String, Integer> tableEntry : tableCleanupResults.entrySet()) {
                        aggregatedCleanupResults.merge(tableEntry.getKey(), tableEntry.getValue(), Integer::sum);
                    }
                }
            }
        }

        aggregatedResult.put("taskType", "data_cleanup");
        aggregatedResult.put("participatingNodes", participatingNodes);
        aggregatedResult.put("nodeCount", participatingNodes.size());
        aggregatedResult.put("totalProcessedTables", allProcessedTables.size());
        aggregatedResult.put("totalCleanedRecords", totalCleanedRecords);
        aggregatedResult.put("totalFreedSpace", totalFreedSpace);
        aggregatedResult.put("processedTables", new ArrayList<>(allProcessedTables));
        aggregatedResult.put("aggregatedCleanupResults", aggregatedCleanupResults);
        aggregatedResult.put("aggregationTime", new Date());

        return aggregatedResult;
    }

    /**
     * 汇总性能统计分片结果
     */
    private Map<String, Object> aggregatePerformanceStatsShardResults(Map<String, Object> allShardResults) {
        Map<String, Object> aggregatedResult = new HashMap<>();

        int totalSuccessCount = 0;
        int totalFailureCount = 0;
        int totalSavedCount = 0;
        Set<Integer> allProcessedBrokerIds = new HashSet<>();
        List<String> participatingNodes = new ArrayList<>();

        for (Map.Entry<String, Object> entry : allShardResults.entrySet()) {
            String nodeId = entry.getKey();
            participatingNodes.add(nodeId);

            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> shardResult = (Map<String, Object>) entry.getValue();

                // 累加各项指标
                totalSuccessCount += getIntValue(shardResult, "successCount");
                totalFailureCount += getIntValue(shardResult, "failureCount");
                totalSavedCount += getIntValue(shardResult, "savedCount");

                // 收集处理的broker ID
                Object processedBrokerIds = shardResult.get("processedBrokerIds");
                if (processedBrokerIds instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Integer> brokerIds = (List<Integer>) processedBrokerIds;
                    allProcessedBrokerIds.addAll(brokerIds);
                }
            }
        }

        aggregatedResult.put("taskType", "performance_stats");
        aggregatedResult.put("participatingNodes", participatingNodes);
        aggregatedResult.put("nodeCount", participatingNodes.size());
        aggregatedResult.put("totalProcessedBrokers", allProcessedBrokerIds.size());
        aggregatedResult.put("totalSuccessCount", totalSuccessCount);
        aggregatedResult.put("totalFailureCount", totalFailureCount);
        aggregatedResult.put("totalSavedCount", totalSavedCount);
        aggregatedResult.put("successRate", (totalSuccessCount + totalFailureCount) > 0 ?
            (double) totalSuccessCount / (totalSuccessCount + totalFailureCount) : 0.0);
        aggregatedResult.put("processedBrokerIds", new ArrayList<>(allProcessedBrokerIds));
        aggregatedResult.put("aggregationTime", new Date());

        return aggregatedResult;
    }
}