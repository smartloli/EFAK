package org.kafka.eagle.web.controller;

import org.kafka.eagle.dto.alert.*;
import org.kafka.eagle.web.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 告警管理控制器
 * 提供告警查询、状态更新、渠道管理、类型配置等功能接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/30 01:38:38
 * @version 5.0.0
 */
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    /**
     * 查询告警列表
     */
    @PostMapping("/query")
    public ResponseEntity<AlertPageResponse> queryAlerts(@RequestBody AlertQueryRequest request, @RequestParam String cid) {
        request.setClusterId(cid);
        AlertPageResponse response = alertService.queryAlerts(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID查询告警详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<AlertInfo> getAlertById(@PathVariable Long id) {
        AlertInfo alert = alertService.getAlertById(id);
        if (alert == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(alert);
    }

    /**
     * 更新告警状态
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateAlertStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam String cid) {
        boolean success = alertService.updateAlertStatusById(id, cid, status);
        if (success) {
            return ResponseEntity.ok("状态更新成功");
        } else {
            return ResponseEntity.badRequest().body("状态更新失败");
        }
    }

    /**
     * 查询告警渠道列表
     */
    @GetMapping("/channels")
    public ResponseEntity<List<AlertChannel>> getAlertChannels(@RequestParam String cid) {
        List<AlertChannel> channels = alertService.getAlertChannels(cid);
        return ResponseEntity.ok(channels);
    }

    /**
     * 根据ID查询告警渠道
     */
    @GetMapping("/channels/{id}")
    public ResponseEntity<AlertChannel> getAlertChannelById(@PathVariable Long id) {
        AlertChannel channel = alertService.getAlertChannelById(id);
        if (channel == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(channel);
    }

    /**
     * 创建告警渠道
     */
    @PostMapping("/channels")
    public ResponseEntity<String> createAlertChannel(@RequestBody AlertChannel channel, @RequestParam String cid) {
        channel.setClusterId(cid);
        boolean success = alertService.createAlertChannel(channel);
        if (success) {
            return ResponseEntity.ok("渠道创建成功");
        } else {
            return ResponseEntity.badRequest().body("渠道创建失败");
        }
    }

    /**
     * 更新告警渠道
     */
    @PutMapping("/channels/{id}")
    public ResponseEntity<String> updateAlertChannel(
            @PathVariable Long id,
            @RequestBody AlertChannel channel,
            @RequestParam String cid) {
        channel.setId(id);
        channel.setClusterId(cid);
        boolean success = alertService.updateAlertChannel(channel);
        if (success) {
            return ResponseEntity.ok("渠道更新成功");
        } else {
            return ResponseEntity.badRequest().body("渠道更新失败");
        }
    }

    /**
     * 删除告警渠道
     */
    @DeleteMapping("/channels/{id}")
    public ResponseEntity<String> deleteAlertChannel(@PathVariable Long id) {
        boolean success = alertService.deleteAlertChannel(id);
        if (success) {
            return ResponseEntity.ok("渠道删除成功");
        } else {
            return ResponseEntity.badRequest().body("渠道删除失败");
        }
    }

    /**
     * 查询告警类型配置列表
     */
    @GetMapping("/type-configs")
    public ResponseEntity<List<AlertTypeConfig>> getAlertTypeConfigs(@RequestParam String cid) {
        List<AlertTypeConfig> configs = alertService.getAlertTypeConfigs(cid);
        return ResponseEntity.ok(configs);
    }

    /**
     * 分页查询告警类型配置列表
     */
    @GetMapping("/type-configs/page")
    public ResponseEntity<AlertPageResponse> getAlertTypeConfigsPage(
            @RequestParam String cid,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "5") Integer pageSize) {
        AlertPageResponse response = alertService.getAlertTypeConfigsPage(cid, page, pageSize);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID查询告警类型配置
     */
    @GetMapping("/type-configs/{id}")
    public ResponseEntity<AlertTypeConfig> getAlertTypeConfigById(@PathVariable Long id) {
        AlertTypeConfig config = alertService.getAlertTypeConfigById(id);
        if (config == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(config);
    }

    /**
     * 创建告警类型配置
     */
    @PostMapping("/type-configs")
    public ResponseEntity<String> createAlertTypeConfig(@RequestBody AlertTypeConfig config, @RequestParam String cid) {
        config.setClusterId(cid);
        boolean success = alertService.createAlertTypeConfig(config);
        if (success) {
            return ResponseEntity.ok("配置创建成功");
        } else {
            return ResponseEntity.badRequest().body("配置创建失败");
        }
    }

    /**
     * 更新告警类型配置
     */
    @PutMapping("/type-configs/{id}")
    public ResponseEntity<String> updateAlertTypeConfig(@PathVariable Long id, @RequestBody AlertTypeConfig config, @RequestParam String cid) {
        config.setId(id);
        config.setClusterId(cid);
        boolean success = alertService.updateAlertTypeConfig(config);
        if (success) {
            return ResponseEntity.ok("配置更新成功");
        } else {
            return ResponseEntity.badRequest().body("配置更新失败");
        }
    }

    /**
     * 删除告警类型配置
     */
    @DeleteMapping("/type-configs/{id}")
    public ResponseEntity<String> deleteAlertTypeConfig(@PathVariable Long id) {
        boolean success = alertService.deleteAlertTypeConfig(id);
        if (success) {
            return ResponseEntity.ok("配置删除成功");
        } else {
            return ResponseEntity.badRequest().body("配置删除失败");
        }
    }

    /**
     * 检查监控目标是否存在
     */
    @GetMapping("/check-target")
    public ResponseEntity<Map<String, Object>> checkTargetExists(
            @RequestParam String cid,
            @RequestParam String target) {
        boolean exists = alertService.checkTargetExists(cid, target);
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("exists", exists);
        response.put("target", target);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据类型和目标查询告警类型配置
     */
    @GetMapping("/type-configs/by-type-target")
    public ResponseEntity<List<AlertTypeConfig>> getAlertTypeConfigsByTypeAndTarget(
            @RequestParam String cid,
            @RequestParam String type,
            @RequestParam String target) {
        List<AlertTypeConfig> configs = alertService.getAlertTypeConfigsByTypeAndTarget(cid, type, target);
        return ResponseEntity.ok(configs);
    }

    /**
     * 执行数据均衡操作
     */
    @PostMapping("/{id}/rebalance")
    public ResponseEntity<String> performRebalance(
            @PathVariable Long id,
            @RequestParam String cid) {
        boolean success = alertService.performRebalanceById(id, cid);
        if (success) {
            return ResponseEntity.ok("数据均衡操作完成");
        } else {
            return ResponseEntity.badRequest().body("数据均衡操作失败");
        }
    }

    /**
     * 获取Broker列表（用于告警配置）
     */
    @GetMapping("/brokers")
    public ResponseEntity<List<Map<String, Object>>> getBrokers(@RequestParam String cid) {
        List<Map<String, Object>> brokers = alertService.getBrokersByClusterId(cid);
        return ResponseEntity.ok(brokers);
    }

    /**
     * 获取消费者组列表（用于告警配置）
     */
    @GetMapping("/consumer-groups")
    public ResponseEntity<List<Map<String, Object>>> getConsumerGroups(@RequestParam String cid) {
        List<Map<String, Object>> groups = alertService.getConsumerGroupsByClusterId(cid);
        return ResponseEntity.ok(groups);
    }

    /**
     * 根据消费者组获取Topic列表（用于告警配置）
     */
    @GetMapping("/consumer-topics")
    public ResponseEntity<List<Map<String, Object>>> getConsumerTopics(
            @RequestParam String cid,
            @RequestParam String groupId) {
        List<Map<String, Object>> topics = alertService.getConsumerTopicsByGroupId(cid, groupId);
        return ResponseEntity.ok(topics);
    }

    /**
     * 获取告警通知（用于通知中心）
     */
    @GetMapping("/notifications")
    public ResponseEntity<List<AlertInfo>> getAlertNotifications(
            @RequestParam String cid,
            @RequestParam(defaultValue = "5") Integer limit) {

        // 创建查询请求，查询告警通知
        AlertQueryRequest request = new AlertQueryRequest();
        request.setClusterId(cid);
        request.setPage(1);
        request.setPageSize(limit);
        request.setSortField("updatedAt");  // 按更新时间排序
        request.setSortOrder("desc");

        // 查询告警通知
        AlertPageResponse response = alertService.queryAlertsForNotifications(request);
        return ResponseEntity.ok(response.getAlerts());
    }

    /**
     * 根据指标类型获取Topic列表（用于告警配置）
     */
    @GetMapping("/topics-by-metric")
    public ResponseEntity<List<Map<String, Object>>> getTopicsByMetric(
            @RequestParam String cid,
            @RequestParam String metricType) {
        List<Map<String, Object>> topics = alertService.getTopicsByMetricType(cid, metricType);
        return ResponseEntity.ok(topics);
    }
}