package org.kafka.eagle.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.web.scheduler.DistributedTaskCoordinator;
import org.kafka.eagle.web.service.ShardResultAggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * <p>
 * DistributedTask 控制器
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/08/03 01:02:22
 * @version 5.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/distributed-task")
public class DistributedTaskController {

    @Autowired
    private DistributedTaskCoordinator taskCoordinator;

    @Autowired
    private ShardResultAggregationService shardResultAggregationService;

    /**
     * 获取在线服务列表
     */
    @GetMapping("/services/online")
    public Map<String, Object> getOnlineServices() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<String> onlineServices = taskCoordinator.getOnlineServices();
            result.put("success", true);
            result.put("data", onlineServices);
            result.put("count", onlineServices.size());
            result.put("timestamp", new Date());
        } catch (Exception e) {
            log.error("获取在线服务列表失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 获取分布式服务节点详细信息
     */
    @GetMapping("/services/details")
    public Map<String, Object> getServiceDetails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(defaultValue = "") String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> allServiceDetails = taskCoordinator.getServiceDetails();
            
            // 关键词过滤
            List<Map<String, Object>> filteredServices = allServiceDetails;
            if (keyword != null && !keyword.trim().isEmpty()) {
                filteredServices = allServiceDetails.stream()
                    .filter(service -> {
                        String nodeId = (String) service.get("nodeId");
                        String ipAddress = (String) service.get("ipAddress");
                        return (nodeId != null && nodeId.toLowerCase().contains(keyword.toLowerCase())) ||
                               (ipAddress != null && ipAddress.toLowerCase().contains(keyword.toLowerCase()));
                    })
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // 分页处理
            int total = filteredServices.size();
            int startIndex = page * pageSize;
            int endIndex = Math.min(startIndex + pageSize, total);
            
            List<Map<String, Object>> pagedServices = new ArrayList<>();
            if (startIndex < total) {
                pagedServices = filteredServices.subList(startIndex, endIndex);
            }
            
            result.put("success", true);
            result.put("data", pagedServices);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) total / pageSize));
            result.put("timestamp", new Date());
        } catch (Exception e) {
            log.error("获取分布式服务节点详细信息失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 获取服务详细信息
     */
    @GetMapping("/services/{serviceId}")
    public Map<String, Object> getServiceInfo(@PathVariable String serviceId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<String> onlineServices = taskCoordinator.getOnlineServices();
            if (onlineServices.contains(serviceId)) {
                Map<String, Object> serviceInfo = new HashMap<>();
                serviceInfo.put("serviceId", serviceId);
                serviceInfo.put("status", "online");
                serviceInfo.put("lastHeartbeat", new Date());
                result.put("success", true);
                result.put("data", serviceInfo);
            } else {
                result.put("success", false);
                result.put("error", "服务不存在或已离线");
            }
        } catch (Exception e) {
            log.error("获取服务信息失败: {}", serviceId, e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 获取broker分片信息
     */
    @GetMapping("/shards/brokers")
    public Map<String, Object> getBrokerShards() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 模拟broker ID列表
            List<Integer> brokerIds = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            List<Integer> assignedBrokers = taskCoordinator.shardBrokers(brokerIds);
            
            Map<String, Object> shardInfo = new HashMap<>();
            shardInfo.put("assignedBrokers", assignedBrokers);
            shardInfo.put("assignedCount", assignedBrokers.size());
            
            result.put("success", true);
            result.put("data", shardInfo);
            result.put("totalBrokers", brokerIds.size());
            result.put("onlineServices", taskCoordinator.getOnlineServices().size());
            result.put("timestamp", new Date());
        } catch (Exception e) {
            log.error("获取broker分片信息失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 获取主题分片信息
     */
    @GetMapping("/shards/topics")
    public Map<String, Object> getTopicShards() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 模拟主题名称列表
            List<String> topicNames = Arrays.asList("topic1", "topic2", "topic3", "topic4", "topic5");
            List<String> assignedTopics = taskCoordinator.shardTopics(topicNames);
            
            Map<String, Object> shardInfo = new HashMap<>();
            shardInfo.put("assignedTopics", assignedTopics);
            shardInfo.put("assignedCount", assignedTopics.size());
            
            result.put("success", true);
            result.put("data", shardInfo);
            result.put("totalTopics", topicNames.size());
            result.put("onlineServices", taskCoordinator.getOnlineServices().size());
            result.put("timestamp", new Date());
        } catch (Exception e) {
            log.error("获取主题分片信息失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 获取消费者组分片信息
     */
    @GetMapping("/shards/consumer-groups")
    public Map<String, Object> getConsumerGroupShards() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 模拟消费者组ID列表
            List<String> consumerGroupIds = Arrays.asList("group1", "group2", "group3", "group4", "group5");
            List<String> assignedConsumerGroups = taskCoordinator.shardConsumerGroups(consumerGroupIds);
            
            Map<String, Object> shardInfo = new HashMap<>();
            shardInfo.put("assignedConsumerGroups", assignedConsumerGroups);
            shardInfo.put("assignedCount", assignedConsumerGroups.size());
            
            result.put("success", true);
            result.put("data", shardInfo);
            result.put("totalConsumerGroups", consumerGroupIds.size());
            result.put("onlineServices", taskCoordinator.getOnlineServices().size());
            result.put("timestamp", new Date());
        } catch (Exception e) {
            log.error("获取消费者组分片信息失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 获取分片任务结果
     */
    @GetMapping("/results/{taskType}")
    public Map<String, Object> getShardResults(@PathVariable String taskType) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> shardResults = taskCoordinator.getAllShardResults(taskType);
            result.put("success", true);
            result.put("data", shardResults);
            result.put("nodeCount", shardResults.size());
            result.put("taskType", taskType);
            result.put("timestamp", new Date());
        } catch (Exception e) {
            log.error("获取分片任务结果失败: {}", taskType, e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 汇总分片任务结果
     */
    @PostMapping("/aggregate/{taskType}")
    public Map<String, Object> aggregateShardResults(
            @PathVariable String taskType,
            @RequestParam(defaultValue = "30") int waitTimeSeconds) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> aggregatedResult = null;
            
            switch (taskType) {
                case "cluster_monitor":
                    aggregatedResult = shardResultAggregationService.aggregateClusterMonitorResults(waitTimeSeconds);
                    break;
                case "topic_monitor":
                    aggregatedResult = shardResultAggregationService.aggregateTopicMonitorResults(waitTimeSeconds);
                    break;
                case "consumer_monitor":
                    aggregatedResult = shardResultAggregationService.aggregateConsumerMonitorResults(waitTimeSeconds);
                    break;
                default:
                    result.put("success", false);
                    result.put("error", "不支持的任务类型: " + taskType);
                    return result;
            }
            
            result.put("success", true);
            result.put("data", aggregatedResult);
            result.put("taskType", taskType);
            result.put("waitTimeSeconds", waitTimeSeconds);
        } catch (Exception e) {
            log.error("汇总分片任务结果失败: {}", taskType, e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 清理分片任务结果
     */
    @DeleteMapping("/results/{taskType}")
    public Map<String, Object> clearShardResults(@PathVariable String taskType) {
        Map<String, Object> result = new HashMap<>();
        try {
            taskCoordinator.clearShardResults(taskType);
            result.put("success", true);
            result.put("message", "分片任务结果清理完成");
            result.put("taskType", taskType);
            result.put("timestamp", new Date());
        } catch (Exception e) {
            log.error("清理分片任务结果失败: {}", taskType, e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 手动触发离线服务清理
     */
    @PostMapping("/cleanup/offline-services")
    public Map<String, Object> cleanupOfflineServices() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<String> beforeCleanup = taskCoordinator.getOnlineServices();
            taskCoordinator.cleanupOfflineServices();
            List<String> afterCleanup = taskCoordinator.getOnlineServices();
            
            result.put("success", true);
            result.put("message", "离线服务清理完成");
            result.put("beforeCount", beforeCleanup.size());
            result.put("afterCount", afterCleanup.size());
            result.put("cleanedCount", beforeCleanup.size() - afterCleanup.size());
            result.put("timestamp", new Date());
        } catch (Exception e) {
            log.error("清理离线服务失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 获取分布式任务统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> getDistributedTaskStats() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<String> onlineServices = taskCoordinator.getOnlineServices();
            
            // 获取各种分片结果统计
            Map<String, Object> clusterResults = taskCoordinator.getAllShardResults("cluster_monitor");
            Map<String, Object> topicResults = taskCoordinator.getAllShardResults("topic_monitor");
            Map<String, Object> consumerResults = taskCoordinator.getAllShardResults("consumer_monitor");
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("onlineServiceCount", onlineServices.size());
            stats.put("onlineServices", onlineServices);
            stats.put("clusterMonitorShardCount", clusterResults.size());
            stats.put("topicMonitorShardCount", topicResults.size());
            stats.put("consumerMonitorShardCount", consumerResults.size());
            stats.put("totalShardCount", clusterResults.size() + topicResults.size() + consumerResults.size());
            
            result.put("success", true);
            result.put("data", stats);
            result.put("timestamp", new Date());
        } catch (Exception e) {
            log.error("获取分布式任务统计信息失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}