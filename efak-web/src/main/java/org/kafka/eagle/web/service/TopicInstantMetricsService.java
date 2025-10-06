package org.kafka.eagle.web.service;

import org.kafka.eagle.dto.topic.TopicInstantMetrics;

import java.util.List;

/**
 * <p>
 * Topic实时指标服务接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/20 22:02:01
 * @version 5.0.0
 */
public interface TopicInstantMetricsService {

    /**
     * Batch update topics metrics
     * 批量更新多个Topic的指标（新记录写入或根据唯一键更新）
     *
     * @param metrics list of topic metrics
     * @return success or not
     */
    boolean batchUpdateTopicsMetrics(List<TopicInstantMetrics> metrics);

    /**
     * Get top N topics by specific metric
     * 根据集群ID、指标类型、TOPN过滤条件查询数据
     *
     * @param clusterId cluster ID
     * @param metricType metric type (byte_out, capacity, logsize, byte_in)
     * @param topN top N topics
     * @return list of top topics
     */
    List<TopicInstantMetrics> getTopTopicsByMetric(String clusterId, String metricType, int topN);
}