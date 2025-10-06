package org.kafka.eagle.web.service;

import org.kafka.eagle.dto.topic.TopicStatisticsDTO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Topic指标服务接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/21 23:56:57
 * @version 5.0.0
 */
public interface TopicMetricsService {

    /**
     * Get topic statistics with trend comparison
     *
     * @return topic statistics DTO
     */
    TopicStatisticsDTO getTopicStatistics(String clusterId);

    /**
     * Get topic trend data for chart display
     *
     * @param dimension 数据维度 (capacity/messages)
     * @param startDate 开始日期 (yyyy-MM-dd)
     * @param endDate 结束日期 (yyyy-MM-dd)
     * @param topics 主题列表
     * @param clusterId 集群ID
     * @return 趋势数据
     */
    Map<String, Object> getTopicTrendData(String dimension, String startDate, String endDate, List<String> topics, String clusterId);

    /**
     * Get topic message flow trend data for chart display
     * 获取主题消息流量趋势数据（用于生产消息流量趋势图表）
     *
     * @param clusterId 集群ID
     * @param topicName 主题名称
     * @param timeRange 时间范围 (1h/6h/1d/3d/7d)
     * @return 消息流量趋势数据列表
     */
    List<Map<String, Object>> getTopicMessageFlowTrend(String clusterId, String topicName, String timeRange);
}