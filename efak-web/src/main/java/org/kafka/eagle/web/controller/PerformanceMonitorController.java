package org.kafka.eagle.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.performance.PerformanceMonitor;
import org.kafka.eagle.dto.performance.PerformanceQueryRequest;
import org.kafka.eagle.dto.performance.PerformancePageResponse;
import org.kafka.eagle.web.service.PerformanceMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 性能监控控制器
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/24 22:31:07
 * @version 5.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/performance/monitors")
public class PerformanceMonitorController {

    @Autowired
    private PerformanceMonitorService performanceMonitorService;

    /**
     * Insert performance monitor data
     */
    @PostMapping("/insert")
    public ResponseEntity<Map<String, Object>> insertPerformanceMonitor(@RequestBody PerformanceMonitor performanceMonitor) {
        Map<String, Object> response = new HashMap<>();
        try {
            int result = performanceMonitorService.insertPerformanceMonitor(performanceMonitor);
            response.put("success", true);
            response.put("message", "Performance monitor data inserted successfully");
            response.put("affectedRows", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to insert performance monitor data", e);
            response.put("success", false);
            response.put("message", "Failed to insert performance monitor data: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Batch insert performance monitor data
     */
    @PostMapping("/batch-insert")
    public ResponseEntity<Map<String, Object>> batchInsertPerformanceMonitors(@RequestBody List<PerformanceMonitor> performanceMonitorList) {
        Map<String, Object> response = new HashMap<>();
        try {
            int result = performanceMonitorService.batchInsertPerformanceMonitors(performanceMonitorList);
            response.put("success", true);
            response.put("message", "Performance monitor data batch inserted successfully");
            response.put("affectedRows", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to batch insert performance monitor data", e);
            response.put("success", false);
            response.put("message", "Failed to batch insert performance monitor data: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Query performance monitor data with pagination
     */
    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> queryPerformanceMonitors(@RequestBody PerformanceQueryRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            PerformancePageResponse pageResponse = performanceMonitorService.queryPerformanceMonitors(request);
            response.put("success", true);
            response.put("data", pageResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to query performance monitors", e);
            response.put("success", false);
            response.put("message", "Failed to query performance monitors: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Get latest performance monitor for each cluster
     */
    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> getLatestPerformanceMonitors() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<PerformanceMonitor> monitors = performanceMonitorService.getLatestPerformanceMonitors();
            response.put("success", true);
            response.put("data", monitors);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get latest performance monitors", e);
            response.put("success", false);
            response.put("message", "Failed to get latest performance monitors: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Get performance statistics
     */
    @GetMapping("/statistics/{clusterId}")
    public ResponseEntity<Map<String, Object>> getPerformanceStatistics(
            @PathVariable String clusterId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> statistics = performanceMonitorService.getPerformanceStatistics(clusterId, startDate, endDate);
            response.put("success", true);
            response.put("data", statistics);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get performance statistics", e);
            response.put("success", false);
            response.put("message", "Failed to get performance statistics: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Get performance trend data
     */
    @GetMapping("/trend/{clusterId}")
    public ResponseEntity<Map<String, Object>> getPerformanceTrendData(
            @PathVariable String clusterId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String timeRange) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> trendData;

            // 如果提供了timeRange，则根据timeRange计算起始和结束时间
            if (timeRange != null && !timeRange.isEmpty()) {
                trendData = performanceMonitorService.getPerformanceTrendDataByTimeRange(clusterId, timeRange);
            } else {
                // 使用传统的startDate和endDate参数
                trendData = performanceMonitorService.getPerformanceTrendData(clusterId, startDate, endDate);
            }

            response.put("success", true);
            response.put("data", trendData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get performance trend data", e);
            response.put("success", false);
            response.put("message", "Failed to get performance trend data: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Get real-time performance data for specific cluster
     */
    @GetMapping("/realtime/{clusterId}")
    public ResponseEntity<Map<String, Object>> getRealtimePerformance(@PathVariable String clusterId) {
        Map<String, Object> response = new HashMap<>();
        try {
            PerformanceMonitor monitor = performanceMonitorService.getLatestPerformanceByCluster(clusterId);
            response.put("success", true);
            response.put("data", monitor);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get realtime performance data", e);
            response.put("success", false);
            response.put("message", "Failed to get realtime performance data: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Cleanup old performance monitor data
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupOldData(@RequestParam(defaultValue = "30") int retentionDays) {
        Map<String, Object> response = new HashMap<>();
        try {
            int deletedCount = performanceMonitorService.cleanupOldData(retentionDays);
            response.put("success", true);
            response.put("message", "Old performance monitor data cleaned up successfully");
            response.put("deletedCount", deletedCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to cleanup old performance monitor data", e);
            response.put("success", false);
            response.put("message", "Failed to cleanup old data: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Get performance data by Kafka host
     */
    @GetMapping("/host/{kafkaHost}")
    public ResponseEntity<Map<String, Object>> getPerformanceByKafkaHost(
            @PathVariable String kafkaHost,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<PerformanceMonitor> monitors = performanceMonitorService.getPerformanceByKafkaHost(kafkaHost, startDate, endDate);
            response.put("success", true);
            response.put("data", monitors);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get performance data by Kafka host", e);
            response.put("success", false);
            response.put("message", "Failed to get performance data by Kafka host: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Get high resource usage alerts
     */
    @GetMapping("/alerts")
    public ResponseEntity<Map<String, Object>> getHighResourceUsageAlerts(
            @RequestParam(defaultValue = "80.0") java.math.BigDecimal memoryThreshold,
            @RequestParam(defaultValue = "80.0") java.math.BigDecimal cpuThreshold) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<PerformanceMonitor> alerts = performanceMonitorService.getHighResourceUsageAlerts(memoryThreshold, cpuThreshold);
            response.put("success", true);
            response.put("data", alerts);
            response.put("memoryThreshold", memoryThreshold);
            response.put("cpuThreshold", cpuThreshold);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get high resource usage alerts", e);
            response.put("success", false);
            response.put("message", "Failed to get high resource usage alerts: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Get resource usage trend by host
     */
    @GetMapping("/resource-trend/{kafkaHost}")
    public ResponseEntity<Map<String, Object>> getResourceUsageTrendByHost(
            @PathVariable String kafkaHost,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> trendData = performanceMonitorService.getResourceUsageTrendByHost(kafkaHost, startDate, endDate);
            response.put("success", true);
            response.put("data", trendData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get resource usage trend data", e);
            response.put("success", false);
            response.put("message", "Failed to get resource usage trend data: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Get performance dashboard data (with resource usage)
     */
    @GetMapping("/dashboard/{clusterId}")
    public ResponseEntity<Map<String, Object>> getPerformanceDashboard(@PathVariable String clusterId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 获取最新性能数据
            PerformanceMonitor latestData = performanceMonitorService.getLatestPerformanceByCluster(clusterId);

            // 获取高资源使用率的告警
            List<PerformanceMonitor> highUsageAlerts = performanceMonitorService.getHighResourceUsageAlerts(
                java.math.BigDecimal.valueOf(80.0), java.math.BigDecimal.valueOf(80.0));

            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.put("latestPerformance", latestData);
            dashboardData.put("highUsageAlerts", highUsageAlerts);
            dashboardData.put("alertCount", highUsageAlerts.size());

            response.put("success", true);
            response.put("data", dashboardData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get performance dashboard data", e);
            response.put("success", false);
            response.put("message", "Failed to get performance dashboard data: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Get latest performance monitor data for specific cluster within last 10 minutes
     */
    @GetMapping("/latest/{clusterId}")
    public ResponseEntity<Map<String, Object>> getLatestPerformanceByCluster(@PathVariable String clusterId) {
        Map<String, Object> response = new HashMap<>();
        try {
            PerformanceMonitor monitor = performanceMonitorService.getLatestPerformanceByClusterWithin10Minutes(clusterId);
            response.put("success", true);
            response.put("data", monitor);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get latest performance data for cluster: {}", clusterId, e);
            response.put("success", false);
            response.put("message", "Failed to get latest performance data: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}