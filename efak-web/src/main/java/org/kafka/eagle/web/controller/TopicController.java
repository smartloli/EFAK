package org.kafka.eagle.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.topic.*;
import org.kafka.eagle.web.service.TopicMetricsService;
import org.kafka.eagle.web.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * <p>
 * Topic管理控制器
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/08/24 16:06:17
 * @version 5.0.0
 */
@Slf4j
@Controller
@RequestMapping("/topic")
public class TopicController {

    @Autowired
    private TopicService topicService;

    @Autowired
    private TopicMetricsService topicMetricsService;

    /**
     * 分页查询Topic列表
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTopicList(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "brokerSpread", required = false) String brokerSpread,
            @RequestParam(value = "brokerSkewed", required = false) String brokerSkewed,
            @RequestParam(value = "leaderSkewed", required = false) String leaderSkewed,
            @RequestParam(value = "sortField", required = false, defaultValue = "create_time") String sortField,
            @RequestParam(value = "sortOrder", required = false, defaultValue = "DESC") String sortOrder,
            @RequestParam(value = "clusterId", required = false) String clusterId,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {

        try {
            TopicQueryRequest request = new TopicQueryRequest();
            request.setSearch(search);
            request.setBrokerSpread(brokerSpread);
            request.setBrokerSkewed(brokerSkewed);
            request.setLeaderSkewed(leaderSkewed);
            // 映射前端字段名到数据库字段名
            String dbSortField = mapSortFieldToDbColumn(sortField);
            request.setSortField(dbSortField);
            request.setSortOrder(sortOrder);
            request.setPage(page);
            request.setPageSize(pageSize);
            request.setClusterId(clusterId);

            TopicPageResponse response = topicService.getTopicPage(request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response.getData());
            result.put("total", response.getTotal());
            result.put("page", response.getPage());
            result.put("pageSize", response.getPageSize());
            result.put("totalPages", response.getTotalPages());
            result.put("hasNext", response.getHasNext());
            result.put("hasPrev", response.getHasPrev());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("查询Topic列表失败：{}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 创建Topic
     */
    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createTopic(Authentication authentication, @RequestBody TopicCreateRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 参数校验
            if (request == null || request.getTopicName() == null || request.getTopicName().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Topic名称不能为空");
                return ResponseEntity.ok(result);
            }

            if (request.getPartitions() == null || request.getPartitions() <= 0) {
                result.put("success", false);
                result.put("message", "分区数必须大于0");
                return ResponseEntity.ok(result);
            }

            if (request.getReplicas() == null || request.getReplicas() <= 0) {
                result.put("success", false);
                result.put("message", "副本数必须大于0");
                return ResponseEntity.ok(result);
            }

            if (request.getClusterId() == null) {
                result.put("success", false);
                result.put("message", "集群ID不能为空");
                return ResponseEntity.ok(result);
            }

            if (request.getCreateBy() == null) {
                request.setCreateBy(authentication.getName());
            }

            // 设置集群ID
            if (request.getClusterId() == null || request.getClusterId().trim().isEmpty()) {
                log.warn("集群ID为空，使用默认配置");
            }

            // 调用业务服务创建Topic（会先调用Kafka操作，再写入数据库）
            boolean success = topicService.createTopic(request);

            if (success) {
                result.put("success", true);
                result.put("message", "Topic创建成功");
            } else {
                result.put("success", false);
                result.put("message", "Topic创建失败");
                log.warn("Topic创建失败: {}", request.getTopicName());
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("创建Topic失败：{}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "创建失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 根据主题名称和集群ID获取Topic基本信息（用于主题详情页面）
     */
    @GetMapping("/api/info/{topicName}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTopicBasicInfo(
            @PathVariable String topicName,
            @RequestParam(value = "clusterId", required = false) String clusterId) {
        Map<String, Object> result = new HashMap<>();

        try {
            TopicInfo topicInfo = topicService.getTopicByNameAndCluster(topicName, clusterId);

            if (topicInfo != null) {
                result.put("success", true);
                // 只返回基本信息，不包含创建时间
                Map<String, Object> data = new HashMap<>();
                data.put("topicName", topicInfo.getTopicName());
                data.put("partitionCount", topicInfo.getPartitions());
                data.put("replicationFactor", topicInfo.getReplicas());
                data.put("clusterId", topicInfo.getClusterId());

                // 获取真实的统计数据
                Map<String, Object> detailedStats = topicService.getTopicDetailedStats(topicName, clusterId);
                data.put("totalRecords", detailedStats.get("totalRecords"));
                data.put("totalSize", detailedStats.get("totalSize"));
                data.put("writeSpeed", detailedStats.get("writeSpeed"));
                data.put("readSpeed", detailedStats.get("readSpeed"));

                result.put("data", data);
            } else {
                result.put("success", false);
                result.put("message", "主题不存在");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("获取主题基本信息失败：{}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 根据ID获取Topic详情（保持原有接口兼容性）
     */
    @GetMapping("/api/info/id/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTopicInfoById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            TopicInfo topicInfo = topicService.getTopicById(id);

            if (topicInfo != null) {
                result.put("success", true);
                result.put("data", topicInfo);
            } else {
                result.put("success", false);
                result.put("message", "Topic不存在");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("获取Topic详情失败：{}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 映射前端字段名到数据库字段名
     */
    private String mapSortFieldToDbColumn(String sortField) {
        if (sortField == null) return "create_time";
        switch (sortField) {
            case "topic_name":
            case "topicName":
                return "topic_name";
            case "partitions":
            case "partitionCount":
                return "partitions";
            case "replicas":
            case "replicationFactor":
                return "replicas";
            case "broker_spread":
            case "brokerSpread":
                return "broker_spread";
            case "broker_skewed":
            case "brokerSkewed":
                return "broker_skewed";
            case "leader_skewed":
            case "leaderSkewed":
                return "leader_skewed";
            case "retention_time":
            case "retentionTime":
                return "retention_time";
            case "create_time":
            case "createTime":
                return "create_time";
            case "update_time":
            case "updateTime":
                return "update_time";
            default:
                return "create_time";
        }
    }

    /**
     * 获取Topic分区信息（支持分页）
     */
    @GetMapping("/api/partitions/{topicName}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTopicPartitions(
            @PathVariable String topicName,
            @RequestParam(value = "clusterId", required = false) String clusterId,
            @RequestParam(value = "start", required = false, defaultValue = "0") Integer start,
            @RequestParam(value = "length", required = false, defaultValue = "10") Integer length) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 构建分页参数
            Map<String, Object> params = new HashMap<>();
            params.put("start", start);
            params.put("length", length);

            // 调用TopicService获取分区分页数据
            Map<String, Object> pageResult = topicService.getTopicPartitionPage(topicName, clusterId, params);

            result.put("success", true);
            result.put("data", pageResult.get("records"));
            result.put("total", pageResult.get("total"));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取Topic分区信息失败：{}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 获取Topic消息流量数据
     */
    @GetMapping("/api/flow/{topicName}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTopicMessageFlow(
            @PathVariable String topicName,
            @RequestParam(value = "clusterId", required = false) String clusterId,
            @RequestParam(value = "timeRange", required = false, defaultValue = "1d") String timeRange) {
        Map<String, Object> result = new HashMap<>();
        try {

            // 调用服务层获取消息流量趋势数据
            List<Map<String, Object>> flowData = topicMetricsService.getTopicMessageFlowTrend(clusterId, topicName, timeRange);

            result.put("success", true);
            result.put("data", flowData);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取Topic消息流量失败：{}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 获取Topic消费者组信息
     */
    @GetMapping("/api/consumer-groups/{topicName}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTopicConsumerGroups(
            @PathVariable String topicName,
            @RequestParam(value = "clusterId", required = false) String clusterId,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "5") Integer pageSize) {

        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> consumerGroupsData = topicService.getTopicConsumerGroups(topicName, clusterId, page, pageSize);
            result.put("success", true);
            result.putAll(consumerGroupsData);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("获取主题 {} 的消费者组信息失败：{}", topicName, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
            result.put("data", new ArrayList<>());
            result.put("total", 0L);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", 0);
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 获取Topic配置信息
     */
    @GetMapping("/api/config/{topicName}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTopicConfig(
            @PathVariable String topicName,
            @RequestParam(value = "clusterId", required = false) String clusterId) {
        Map<String, Object> result = new HashMap<>();
        try {

            // 调用服务层获取Topic配置
            Map<String, String> topicConfig = topicService.getTopicConfig(topicName, clusterId);

            result.put("success", true);
            result.put("data", topicConfig);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取Topic配置信息失败：{}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 获取Topic分区消息
     */
    @GetMapping("/api/messages/{topicName}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTopicMessages(
            @PathVariable String topicName,
            @RequestParam(value = "clusterId", required = false) String clusterId,
            @RequestParam(value = "partition", required = false) Integer partition,
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        Map<String, Object> result = new HashMap<>();
        try {

            // 调用TopicService获取分区消息
            List<Map<String, Object>> messages = topicService.getTopicPartitionMessages(topicName, clusterId, partition, limit);

            result.put("success", true);
            result.put("data", messages);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取Topic消息失败：{}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 删除Topic
     */
    @DeleteMapping("/api/delete/{topicName}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteTopic(
            @PathVariable String topicName,
            @RequestParam(value = "clusterId", required = false) String clusterId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = topicService.deleteTopic(topicName, clusterId);
            if (success) {
                result.put("success", true);
                result.put("message", "删除成功");
            } else {
                result.put("success", false);
                result.put("message", "删除失败");
                log.warn("Topic删除失败: {}", topicName);
            }
        } catch (Exception e) {
            log.error("删除Topic失败：{}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 扩缩容Topic
     */
    @PostMapping("/api/scale/{topicName}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> scaleTopic(
            Authentication authentication,
            @PathVariable String topicName,
            @RequestParam Integer newPartitions,
            @RequestParam(value = "clusterId", required = false) String clusterId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = topicService.scaleTopic(topicName, newPartitions, clusterId, authentication.getName());
            if (success) {
                result.put("success", true);
                result.put("message", "扩缩容成功");
            } else {
                result.put("success", false);
                result.put("message", "扩缩容失败");
                log.warn("Topic扩容失败: {}", topicName);
            }
        } catch (Exception e) {
            log.error("扩缩容Topic失败：{}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "扩缩容失败：" + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 批量设置Topic保留时间
     */
    @PutMapping("/api/retention")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateTopicRetention(Authentication authentication, @RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String topicName = (String) request.get("topicName");
            String clusterId = (String) request.get("clusterId");
            Object retentionObj = request.get("retentionMs");

            Long retentionMs = null;
            if (retentionObj instanceof Number) {
                retentionMs = ((Number) retentionObj).longValue();
            } else if (retentionObj instanceof String) {
                try {
                    retentionMs = Long.parseLong((String) retentionObj);
                } catch (NumberFormatException ignored) {
                }
            }

            boolean success = false;
            if (topicName != null && retentionMs != null && retentionMs > 0) {
                success = topicService.setTopicRetention(topicName, retentionMs, clusterId, authentication.getName());
            }

            if (success) {
                result.put("success", true);
                result.put("message", "更新成功");
            } else {
                result.put("success", false);
                result.put("message", "更新失败");
                log.warn("Topic保留时间更新失败: {}", topicName);
            }
        } catch (Exception e) {
            log.error("批量设置Topic保留时间失败：{}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 发送测试数据到Topic
     */
    @PostMapping("/api/test-data/{topicName}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendTestData(
            @PathVariable String topicName,
            @RequestParam(required = false, defaultValue = "json") String dataType,
            @RequestParam(required = false, defaultValue = "10") Integer messageCount) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = topicService.sendTestData(topicName, dataType, messageCount);
            if (success) {
                result.put("success", true);
                result.put("message", "发送成功");
            } else {
                result.put("success", false);
                result.put("message", "发送失败");
            }
        } catch (Exception e) {
            log.error("发送测试数据失败：{}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "发送失败：" + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 获取所有主题名称列表 - 用于下拉选择
     */
    @GetMapping("/api/names")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTopicNames() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<String> topicNames = topicService.getAllTopicNames();
            result.put("success", true);
            result.put("data", topicNames);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取主题名称列表失败：{}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
            result.put("data", new ArrayList<>());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 获取Topic统计数据 - 包含容量、记录数、读写速度等统计信息和趋势对比
     */
    @GetMapping("/api/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTopicStatistics(@RequestParam(value = "clusterId") String clusterId) {
        Map<String, Object> result = new HashMap<>();
        try {
            TopicStatisticsDTO statistics = topicMetricsService.getTopicStatistics(clusterId);
            result.put("success", true);
            result.put("data", statistics);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取Topic统计数据失败：{}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 获取Topic趋势数据 - 用于图表展示
     */
    @GetMapping("/api/trend")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTopicTrend(
            @RequestParam(value = "dimension", required = false, defaultValue = "capacity") String dimension,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "topics", required = false) String topics,
            @RequestParam(value = "clusterId", required = false) String clusterId) {

        Map<String, Object> result = new HashMap<>();
        try {
            // 解析主题列表
            List<String> topicList = new ArrayList<>();
            if (topics != null && !topics.trim().isEmpty()) {
                topicList = Arrays.asList(topics.split(","));
            }

            // 调用服务层获取趋势数据
            Map<String, Object> trendData = topicMetricsService.getTopicTrendData(
                    dimension, startDate, endDate, topicList, clusterId);

            result.put("success", true);
            result.put("data", trendData);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("获取Topic趋势数据失败：{}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
}