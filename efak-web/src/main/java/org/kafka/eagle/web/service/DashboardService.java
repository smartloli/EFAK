package org.kafka.eagle.web.service;

import org.kafka.eagle.dto.dashboard.DashboardStats;
import org.kafka.eagle.dto.dashboard.MetricsTableData;
import org.kafka.eagle.dto.dashboard.TopicAnalysisData;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Dashboard service interface
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/27 03:02:51
 * @version 5.0.0
 */
public interface DashboardService {

    /**
     * Get dashboard statistics by cluster ID
     *
     * @param clusterId cluster ID
     * @return dashboard statistics
     */
    DashboardStats getDashboardStats(String clusterId);

    /**
     * Get performance trend data by cluster ID and time period
     *
     * @param clusterId cluster ID
     * @param period time period (1h, 24h, 3d, 7d)
     * @return performance trend data
     */
    Object getPerformanceTrend(String clusterId, String period);

    /**
     * Get topic analysis data by cluster ID
     * 获取主题状态分析数据
     *
     * @param clusterId cluster ID
     * @return topic analysis data
     */
    TopicAnalysisData getTopicAnalysis(String clusterId);

    /**
     * Get metrics table data by cluster ID and dimension
     * 获取性能指标表格数据
     *
     * @param clusterId cluster ID
     * @param dimension dimension type (messages, size, read, write)
     * @return metrics table data list
     */
    List<MetricsTableData> getMetricsTableData(String clusterId, String dimension);

    /**
     * Debug method to get metric types info for troubleshooting
     * 调试方法：获取指标类型信息用于故障排除
     *
     * @param clusterId cluster ID
     * @return debug information about available metrics
     */
    List<Map<String, Object>> getMetricTypesDebugInfo(String clusterId);
}