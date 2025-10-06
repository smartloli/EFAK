package org.kafka.eagle.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.dashboard.DashboardResponse;
import org.kafka.eagle.dto.dashboard.DashboardStats;
import org.kafka.eagle.dto.dashboard.MetricsTableData;
import org.kafka.eagle.dto.dashboard.TopicAnalysisData;
import org.kafka.eagle.web.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Dashboard Controller
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/27 03:04:10
 * @version 5.0.0
 */
@RestController
@RequestMapping("/dashboard/api")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Get hero section statistics
     *
     * @param cid cluster ID
     * @return dashboard statistics
     */
    @GetMapping("/hero-stats")
    public ResponseEntity<DashboardResponse<DashboardStats>> getHeroStats(@RequestParam("cid") String cid) {
        try {
            DashboardStats stats = dashboardService.getDashboardStats(cid);
            return ResponseEntity.ok(DashboardResponse.success(stats));
        } catch (Exception e) {
            log.error("Failed to get hero stats for cluster: {}", cid, e);
            return ResponseEntity.ok(DashboardResponse.error("获取统计信息失败: " + e.getMessage()));
        }
    }

    /**
     * Get performance trend data (for performance chart)
     *
     * @param cid cluster ID
     * @param period time period (1h, 24h, 3d, 7d)
     * @return performance trend data
     */
    @GetMapping("/performance-trend")
    public ResponseEntity<DashboardResponse<Object>> getPerformanceTrend(
            @RequestParam("cid") String cid,
            @RequestParam("period") String period) {
        try {
            Object performanceData = dashboardService.getPerformanceTrend(cid, period);
            return ResponseEntity.ok(DashboardResponse.success(performanceData));
        } catch (Exception e) {
            log.error("Failed to get performance trend data for cluster: {}, period: {}", cid, period, e);
            return ResponseEntity.ok(DashboardResponse.error("获取性能趋势数据失败: " + e.getMessage()));
        }
    }

    /**
     * Get topics analysis data (for charts)
     *
     * @param cid cluster ID
     * @return topics analysis data
     */
    @GetMapping("/topics")
    public ResponseEntity<DashboardResponse<TopicAnalysisData>> getTopicsAnalysis(@RequestParam("cid") String cid) {
        try {
            TopicAnalysisData analysisData = dashboardService.getTopicAnalysis(cid);
            return ResponseEntity.ok(DashboardResponse.success(analysisData));
        } catch (Exception e) {
            log.error("Failed to get topics analysis data for cluster: {}", cid, e);
            return ResponseEntity.ok(DashboardResponse.error("获取主题分析数据失败: " + e.getMessage()));
        }
    }

    /**
     * Get metrics table data (for performance metrics table)
     *
     * @param dimension table dimension (messages, size, read, write)
     * @param cid cluster ID
     * @return metrics table data
     */
    @GetMapping("/metrics-table")
    public ResponseEntity<DashboardResponse<List<MetricsTableData>>> getMetricsTable(
            @RequestParam("dimension") String dimension,
            @RequestParam("cid") String cid) {
        try {
            List<MetricsTableData> tableData = dashboardService.getMetricsTableData(cid, dimension);
            return ResponseEntity.ok(DashboardResponse.success(tableData));
        } catch (Exception e) {
            log.error("Failed to get metrics table data for cluster: {}, dimension: {}", cid, dimension, e);
            return ResponseEntity.ok(DashboardResponse.error("获取表格数据失败: " + e.getMessage()));
        }
    }

    /**
     * Debug endpoint to check what metric types exist in the database
     *
     * @param cid cluster ID
     * @return debug information about available metrics
     */
    @GetMapping("/debug/metrics-info")
    public ResponseEntity<DashboardResponse<List<Map<String, Object>>>> getMetricsDebugInfo(
            @RequestParam("cid") String cid) {
        try {
            List<Map<String, Object>> debugInfo = dashboardService.getMetricTypesDebugInfo(cid);
            return ResponseEntity.ok(DashboardResponse.success(debugInfo));
        } catch (Exception e) {
            log.error("Failed to get metrics debug info for cluster: {}", cid, e);
            return ResponseEntity.ok(DashboardResponse.error("获取调试信息失败: " + e.getMessage()));
        }
    }
}