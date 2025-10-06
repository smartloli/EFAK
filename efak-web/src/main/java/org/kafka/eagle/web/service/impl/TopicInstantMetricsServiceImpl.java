package org.kafka.eagle.web.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.topic.TopicInstantMetrics;
import org.kafka.eagle.web.mapper.TopicInstantMetricsMapper;
import org.kafka.eagle.web.service.TopicInstantMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Topic实时指标服务接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/20 22:02:32
 * @version 5.0.0
 */
@Slf4j
@Service
public class TopicInstantMetricsServiceImpl implements TopicInstantMetricsService {

    @Autowired
    private TopicInstantMetricsMapper topicInstantMetricsMapper;

    @Override
    @Transactional
    public boolean batchUpdateTopicsMetrics(List<TopicInstantMetrics> metrics) {
        try {
            if (metrics == null || metrics.isEmpty()) {
                return false;
            }

            // 验证数据完整性
            for (TopicInstantMetrics metric : metrics) {
                if (metric.getClusterId() == null || metric.getClusterId().trim().isEmpty() ||
                    metric.getTopicName() == null || metric.getTopicName().trim().isEmpty() ||
                    metric.getMetricType() == null || metric.getMetricType().trim().isEmpty() ||
                    metric.getMetricValue() == null || metric.getMetricValue().trim().isEmpty()) {
                    throw new IllegalArgumentException("Metric data is incomplete: " + metric);
                }
            }

            return topicInstantMetricsMapper.batchUpsertMetrics(metrics) > 0;
        } catch (Exception e) {
            log.error("批量更新主题指标失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<TopicInstantMetrics> getTopTopicsByMetric(String clusterId, String metricType, int topN) {
        try {
            if (clusterId == null || clusterId.trim().isEmpty() ||
                metricType == null || metricType.trim().isEmpty() ||
                topN <= 0) {
                return new ArrayList<>();
            }

            return topicInstantMetricsMapper.getTopMetricsByType(clusterId, metricType, topN);
        } catch (Exception e) {
            log.error("获取Top指标主题失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}