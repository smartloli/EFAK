package org.kafka.eagle.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.broker.BrokerInfo;
import org.kafka.eagle.dto.cluster.KafkaClientInfo;
import org.kafka.eagle.dto.cluster.KafkaClusterInfo;
import org.kafka.eagle.dto.consumer.ConsumerGroupPageResponse;
import org.kafka.eagle.web.service.BrokerService;
import org.kafka.eagle.web.service.ClusterService;
import org.kafka.eagle.web.service.ConsumerGroupTopicService;
import org.kafka.eagle.web.util.KafkaClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Consumer组管理控制器
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/08/24 14:03:44
 * @version 5.0.0
 */
@Slf4j
@Controller
@RequestMapping("/consumers")
public class ConsumerController {

    @Autowired
    private ConsumerGroupTopicService consumerGroupTopicService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private BrokerService brokerService;

    /**
     * 获取消费者组统计信息
     *
     * @param clusterId 集群ID
     * @return 统计信息响应
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getConsumerStats(@RequestParam("cid") String clusterId) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (clusterId == null || clusterId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "集群ID参数不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 获取统计数据
            Map<String, Object> stats = consumerGroupTopicService.getConsumerStats(clusterId);

            response.put("success", true);
            response.put("data", stats);
            response.put("message", "获取统计信息成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取空闲消费者组趋势数据
     *
     * @param clusterId 集群ID
     * @param timeRange 时间范围，默认1d（最近24小时）
     * @return 趋势数据响应
     */
    @GetMapping("/api/idle-groups-trend")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getIdleGroupsTrend(@RequestParam("cid") String clusterId,
                                                                  @RequestParam(value = "timeRange", defaultValue = "1d") String timeRange) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (clusterId == null || clusterId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "集群ID参数不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 获取趋势数据
            List<Map<String, Object>> trendData = consumerGroupTopicService.getIdleGroupsTrend(clusterId, timeRange);

            response.put("success", true);
            response.put("data", trendData);
            response.put("message", "获取趋势数据成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取趋势数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取消费者组列表（分页）
     *
     * @param clusterId 集群ID
     * @param search    搜索关键字（消费者组ID或主题名称）
     * @param page      页码
     * @param pageSize  每页大小
     * @return 消费者组分页数据
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getConsumerGroupsList(@RequestParam("cid") String clusterId,
                                                                     @RequestParam(value = "search", defaultValue = "") String search,
                                                                     @RequestParam(value = "page", defaultValue = "1") int page,
                                                                     @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (clusterId == null || clusterId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "集群ID参数不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            if (page < 1) {
                page = 1;
            }
            if (pageSize < 1 || pageSize > 100) {
                pageSize = 10;
            }

            // 获取消费者组列表
            ConsumerGroupPageResponse pageResponse = consumerGroupTopicService.getConsumerGroupsList(clusterId, search, page, pageSize);

            response.put("success", true);
            response.put("data", pageResponse);
            response.put("message", "获取消费者组列表成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取消费者组列表失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取消费者组详细信息
     *
     * @param clusterId 集群ID
     * @param groupId   消费者组ID
     * @param topic     主题名称
     * @return 消费者组详细信息
     */
    @GetMapping("/api/detail")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getConsumerGroupDetail(@RequestParam("cid") String clusterId,
                                                                      @RequestParam("groupId") String groupId,
                                                                      @RequestParam("topic") String topic) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (clusterId == null || clusterId.trim().isEmpty() ||
                    groupId == null || groupId.trim().isEmpty() ||
                    topic == null || topic.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "必要参数不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 获取消费者组详细信息
            Map<String, Object> detailInfo = consumerGroupTopicService.getConsumerGroupDetail(clusterId, groupId, topic);

            response.put("success", true);
            response.put("data", detailInfo);
            response.put("message", "获取消费者组详细信息成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取消费者组详细信息失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取消费者组速度数据
     *
     * @param clusterId 集群ID
     * @param groupId   消费者组ID
     * @param topic     主题名称
     * @return 速度数据
     */
    @GetMapping("/api/speed")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getConsumerGroupSpeed(@RequestParam("cid") String clusterId,
                                                                     @RequestParam("groupId") String groupId,
                                                                     @RequestParam("topic") String topic) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (clusterId == null || clusterId.trim().isEmpty() ||
                    groupId == null || groupId.trim().isEmpty() ||
                    topic == null || topic.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "必要参数不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 获取消费者组速度数据
            Map<String, Object> speedData = consumerGroupTopicService.getConsumerGroupSpeed(clusterId, groupId, topic);

            response.put("success", true);
            response.put("data", speedData);
            response.put("message", "获取消费者组速度数据成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取消费者组速度数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取消费者组延迟趋势数据
     *
     * @param clusterId 集群ID
     * @param groupId   消费者组ID
     * @param topic     主题名称
     * @param timeRange 时间范围
     * @return 延迟趋势数据
     */
    @GetMapping("/api/lag-trend")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getConsumerGroupLagTrend(@RequestParam("cid") String clusterId,
                                                                        @RequestParam("groupId") String groupId,
                                                                        @RequestParam("topic") String topic,
                                                                        @RequestParam(value = "timeRange", defaultValue = "1d") String timeRange) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (clusterId == null || clusterId.trim().isEmpty() ||
                    groupId == null || groupId.trim().isEmpty() ||
                    topic == null || topic.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "必要参数不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 获取消费者组延迟趋势数据
            List<Map<String, Object>> lagTrendData = consumerGroupTopicService.getConsumerGroupLagTrend(clusterId, groupId, topic, timeRange);

            response.put("success", true);
            response.put("data", lagTrendData);
            response.put("message", "获取消费者组延迟趋势数据成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取消费者组延迟趋势数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 重置消费者组偏移量
     *
     * @param requestBody 请求体，包含消费者组ID、主题名称、重置类型和其他参数
     * @return 重置结果响应
     */
    @PostMapping("/api/reset-offset")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resetConsumerGroupOffset(@RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 从请求体中获取参数
            String groupId = (String) requestBody.get("groupId");
            String clusterId = (String) requestBody.get("cid");
            String topic = (String) requestBody.get("topic");
            String resetType = (String) requestBody.get("resetType");

            // 验证必要参数
            if (groupId == null || groupId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "消费者组ID不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            if (clusterId == null || clusterId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "集群ID不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            if (resetType == null || resetType.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "重置类型不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 获取重置值（可选）
            Long resetValue = null;
            if (requestBody.containsKey("offsetValue")) {
                Object offsetValueObj = requestBody.get("offsetValue");
                if (offsetValueObj instanceof Number) {
                    resetValue = ((Number) offsetValueObj).longValue();
                }
            }
            if (requestBody.containsKey("timestamp")) {
                Object timestampObj = requestBody.get("timestamp");
                if (timestampObj instanceof Number) {
                    resetValue = ((Number) timestampObj).longValue();
                }
            }

            // 1. 根据集群ID查询集群信息
            KafkaClusterInfo cluster = clusterService.findByClusterId(clusterId);
            if (cluster == null) {
                response.put("success", false);
                response.put("message", "未找到集群信息，集群ID: " + clusterId);
                return ResponseEntity.badRequest().body(response);
            }

            // 2. 根据集群ID查询broker信息
            List<BrokerInfo> brokerInfos = brokerService.getBrokersByClusterId(clusterId);
            if (brokerInfos == null || brokerInfos.isEmpty()) {
                response.put("success", false);
                response.put("message", "未找到集群的broker信息，集群ID: " + clusterId);
                return ResponseEntity.badRequest().body(response);
            }

            // 3. 构建KafkaClientInfo，使用KafkaClientUtils工具类
            KafkaClientInfo kafkaClientInfo = KafkaClientUtils.buildKafkaClientInfo(cluster, brokerInfos);

            // 调用服务层重置偏移量，使用构建好的KafkaClientInfo
            boolean success = consumerGroupTopicService.resetConsumerGroupOffsetWithClientInfo(
                    kafkaClientInfo, groupId, topic, resetType, resetValue);

            if (success) {
                response.put("success", true);
                response.put("message", "消费者组偏移量重置成功");
            } else {
                response.put("success", false);
                response.put("message", "消费者组偏移量重置失败");
                log.warn("消费者组 {} 偏移量重置失败", groupId);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            String groupId = (String) requestBody.get("groupId");
            String topic = (String) requestBody.get("topic");
            log.error("重置消费者组 {} , topic:{} 偏移量失败: {}", groupId, topic, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "重置失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

}