package org.kafka.eagle.web.controller;

import org.kafka.eagle.dto.topic.TopicInstantMetrics;
import org.kafka.eagle.web.service.TopicInstantMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * Topic实时指标控制器
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/20 22:01:49
 * @version 5.0.0
 */
@RestController
@RequestMapping("/api/topic/instant-metrics")
public class TopicInstantMetricsController {

    @Autowired
    private TopicInstantMetricsService topicInstantMetricsService;

    /**
     * Get top N topics by specific metric
     * GET /api/topic/instant-metrics/cluster/{clusterId}/top
     */
    @GetMapping("/cluster/{clusterId}/top")
    public ResponseEntity<List<TopicInstantMetrics>> getTopTopicsByMetric(
            @PathVariable String clusterId,
            @RequestParam String metricType,
            @RequestParam(defaultValue = "10") int topN) {
        try {
            List<TopicInstantMetrics> topTopics = topicInstantMetricsService.getTopTopicsByMetric(clusterId, metricType, topN);
            return ResponseEntity.ok(topTopics);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }
}