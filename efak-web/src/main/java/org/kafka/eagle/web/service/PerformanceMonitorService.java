package org.kafka.eagle.web.service;

import org.kafka.eagle.dto.performance.PerformanceMonitor;
import org.kafka.eagle.dto.performance.PerformanceQueryRequest;
import org.kafka.eagle.dto.performance.PerformancePageResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 性能监控服务接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/24 22:31:17
 * @version 5.0.0
 */
public interface PerformanceMonitorService {

    /**
     * Insert performance monitor data
     *
     * @param performanceMonitor performance monitor information
     * @return affected rows
     */
    int insertPerformanceMonitor(PerformanceMonitor performanceMonitor);

    /**
     * Batch insert performance monitor data
     *
     * @param performanceMonitorList list of performance monitors
     * @return affected rows
     */
    int batchInsertPerformanceMonitors(List<PerformanceMonitor> performanceMonitorList);

    /**
     * Query performance monitor data with pagination
     *
     * @param request query request
     * @return performance monitor page response
     */
    PerformancePageResponse queryPerformanceMonitors(PerformanceQueryRequest request);

    /**
     * Get latest performance monitor for each cluster
     *
     * @return latest performance monitor list
     */
    List<PerformanceMonitor> getLatestPerformanceMonitors();

    /**
     * Get performance statistics
     *
     * @param clusterId cluster id
     * @param startDate start date (yyyy-MM-dd)
     * @param endDate end date (yyyy-MM-dd)
     * @return statistics map
     */
    Map<String, Object> getPerformanceStatistics(String clusterId, String startDate, String endDate);

    /**
     * Get performance trend data for chart display
     *
     * @param clusterId cluster ID
     * @param startDate start date (yyyy-MM-dd)
     * @param endDate end date (yyyy-MM-dd)
     * @return trend data
     */
    Map<String, Object> getPerformanceTrendData(String clusterId, String startDate, String endDate);

    /**
     * Get performance trend data by time range for chart display
     *
     * @param clusterId cluster ID
     * @param timeRange time range (1h, 24h, 7d, 30d)
     * @return trend data
     */
    Map<String, Object> getPerformanceTrendDataByTimeRange(String clusterId, String timeRange);

    /**
     * Get real-time performance data for monitoring dashboard
     *
     * @param clusterId cluster ID
     * @return real-time performance data
     */
    PerformanceMonitor getLatestPerformanceByCluster(String clusterId);

    /**
     * Clean up old performance monitor data
     *
     * @param retentionDays retention days (older data will be deleted)
     * @return deleted records count
     */
    int cleanupOldData(int retentionDays);

    /**
     * Get performance data by Kafka host
     *
     * @param kafkaHost Kafka host
     * @param startDate start date (yyyy-MM-dd)
     * @param endDate end date (yyyy-MM-dd)
     * @return performance monitor list
     */
    List<PerformanceMonitor> getPerformanceByKafkaHost(String kafkaHost, String startDate, String endDate);

    /**
     * Get high resource usage alerts
     *
     * @param memoryThreshold memory usage threshold (percentage)
     * @param cpuThreshold CPU usage threshold (percentage)
     * @return performance monitors with high resource usage
     */
    List<PerformanceMonitor> getHighResourceUsageAlerts(BigDecimal memoryThreshold, BigDecimal cpuThreshold);

    /**
     * Get resource usage trend by host
     *
     * @param kafkaHost kafka host
     * @param startDate start date (yyyy-MM-dd)
     * @param endDate end date (yyyy-MM-dd)
     * @return resource usage trend data
     */
    Map<String, Object> getResourceUsageTrendByHost(String kafkaHost, String startDate, String endDate);

    /**
     * Get latest performance monitor data for specific cluster within last 10 minutes
     *
     * @param clusterId cluster ID
     * @return latest performance monitor within 10 minutes or null if no data
     */
    PerformanceMonitor getLatestPerformanceByClusterWithin10Minutes(String clusterId);
}