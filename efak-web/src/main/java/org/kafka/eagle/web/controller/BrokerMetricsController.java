package org.kafka.eagle.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.broker.BrokerMetrics;
import org.kafka.eagle.web.service.BrokerMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Broker性能指标控制器
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/14 00:54:04
 * @version 5.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/brokers/metrics")
@CrossOrigin(origins = "*")
public class BrokerMetricsController {

    @Autowired
    private BrokerMetricsService brokerMetricsService;

    /**
     * 统一的趋势数据接口（支持CPU和内存）
     */
    @GetMapping("/trend")
    public ResponseEntity<Map<String, Object>> getTrendData(
            @RequestParam String metricType,
            @RequestParam(defaultValue = "1h") String timeRange,
            @RequestParam(required = false) String clusterId) {
        try {
            // 验证指标类型
            if (!"cpu".equalsIgnoreCase(metricType) && !"memory".equalsIgnoreCase(metricType)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "不支持的指标类型: " + metricType + "，支持的类型: cpu, memory");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Map<String, Object> trendData = brokerMetricsService.getTrendData(metricType, timeRange, clusterId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", trendData);
            response.put("message", "获取" + metricType.toUpperCase() + "趋势数据成功");
            response.put("metricType", metricType);
            response.put("timeRange", timeRange);
            response.put("clusterId", clusterId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取{}趋势数据失败", metricType, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取" + metricType.toUpperCase() + "趋势数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 获取Broker CPU使用率趋势数据（用于前端图表）
     * @deprecated 使用 /trend?metricType=cpu 替代
     */
    @Deprecated
    @GetMapping("/cpu-trend")
    public ResponseEntity<Map<String, Object>> getBrokerCpuTrend(
            @RequestParam(defaultValue = "1h") String timeRange,
            @RequestParam(required = false) String clusterId) {
        try {
            Map<String, Object> cpuTrendData = brokerMetricsService.getTrendData("cpu", timeRange, clusterId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", cpuTrendData);
            response.put("message", "获取CPU趋势数据成功");
            response.put("timeRange", timeRange);
            response.put("clusterId", clusterId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取CPU趋势数据失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取CPU趋势数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 获取Broker 内存使用率趋势数据（用于前端图表）
     * @deprecated 使用 /trend?metricType=memory 替代
     */
    @Deprecated
    @GetMapping("/memory-trend")
    public ResponseEntity<Map<String, Object>> getBrokerMemoryTrend(
            @RequestParam(defaultValue = "1h") String timeRange,
            @RequestParam(required = false) String clusterId) {
        try {
            Map<String, Object> memoryTrendData = brokerMetricsService.getTrendData("memory", timeRange, clusterId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", memoryTrendData);
            response.put("message", "获取内存趋势数据成功");
            response.put("timeRange", timeRange);
            response.put("clusterId", clusterId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取内存趋势数据失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取内存趋势数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 查询指定时间范围内的性能指标数据
     */
    @GetMapping("/range")
    public ResponseEntity<Map<String, Object>> getMetricsByTimeRange(
            @RequestParam(required = false) Integer brokerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        try {
            List<BrokerMetrics> metrics = brokerMetricsService.queryMetricsByTimeRange(
                    brokerId, startTime, endTime);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", metrics);
            response.put("total", metrics.size());
            response.put("message", "查询性能指标数据成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询性能指标数据失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "查询性能指标数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 查询最新的性能指标数据
     */
    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> getLatestMetrics(
            @RequestParam(required = false) Integer brokerId,
            @RequestParam(defaultValue = "100") Integer limit) {

        try {
            List<BrokerMetrics> metrics = brokerMetricsService.queryLatestMetrics(brokerId, limit);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", metrics);
            response.put("total", metrics.size());
            response.put("message", "查询最新性能指标数据成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询最新性能指标数据失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "查询最新性能指标数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 获取性能指标统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getMetricsStats(
            @RequestParam(required = false) Integer brokerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        try {
            Map<String, Object> stats = brokerMetricsService.getBrokerMetricsStats(
                    brokerId, startTime, endTime);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("message", "获取性能指标统计信息成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取性能指标统计信息失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取性能指标统计信息失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 根据时间范围计算开始时间
     */
    private LocalDateTime calculateStartTime(LocalDateTime endTime, String timeRange) {
        switch (timeRange) {
            case "5m":
                return endTime.minusMinutes(5);
            case "15m":
                return endTime.minusMinutes(15);
            case "30m":
                return endTime.minusMinutes(30);
            case "1h":
                return endTime.minusHours(1);
            case "6h":
                return endTime.minusHours(6);
            case "12h":
                return endTime.minusHours(12);
            case "24h":
                return endTime.minusHours(24);
            case "1d":
                return endTime.minusDays(1);
            case "3d":
                return endTime.minusDays(3);
            case "7d":
                return endTime.minusDays(7);
            default:
                return endTime.minusHours(1);
        }
    }

    /**
     * 格式化图表数据
     */
    private Map<String, Object> formatChartData(List<Map<String, Object>> trendData, String metricType) {
        Map<String, Object> chartData = new HashMap<>();
        List<String> labels = new ArrayList<>();
        Map<String, Map<String, Double>> timeSeriesData = new HashMap<>();
        Set<String> hostIps = new HashSet<>();

        // 第一遍遍历：收集所有时间点和broker ID
        for (Map<String, Object> data : trendData) {
            Object timeLabelObj = data.get("collectTime");
            if (timeLabelObj == null) {
                log.warn("数据中缺少collectTime字段，跳过该条记录: {}", data);
                continue;
            }
            String timeLabel = formatTimeLabel(timeLabelObj.toString());
            
            // 安全获取brokerId
            Object hostIpObj = data.get("hostIp");
            if (hostIpObj == null) {
                log.warn("数据中缺少hostIpObj字段，跳过该条记录: {}", data);
                continue;
            }
            String hostIp = hostIpObj.toString();
            
            // 安全获取指标值
            Object valueObj = data.get("value");
            if (valueObj == null) {
                log.warn("数据中缺少value字段，跳过该条记录: {}", data);
                continue;
            }
            
            Double value;
            try {
                value = Double.parseDouble(valueObj.toString());
            } catch (NumberFormatException e) {
                log.warn("无法解析value字段的值: {}，跳过该条记录", valueObj);
                continue;
            }

            // 收集时间标签
            if (!labels.contains(timeLabel)) {
                labels.add(timeLabel);
            }
            
            // 收集broker ID
            hostIps.add(hostIp);
            
            // 存储时间序列数据
            timeSeriesData.computeIfAbsent(timeLabel, k -> new HashMap<>()).put(hostIp, value);
        }

        // 排序时间标签
        labels.sort(String::compareTo);
        
        // 构建datasets
        List<Map<String, Object>> datasets = new ArrayList<>();
        for (String hostIp : hostIps) {
            Map<String, Object> dataset = new HashMap<>();
            dataset.put("label", hostIp);
            
            List<Double> dataPoints = new ArrayList<>();
            for (String timeLabel : labels) {
                Map<String, Double> timeData = timeSeriesData.get(timeLabel);
                Double value = (timeData != null) ? timeData.get(hostIp) : null;
                dataPoints.add(value); // Chart.js可以处理null值
            }
            
            dataset.put("data", dataPoints);
            datasets.add(dataset);
        }

        chartData.put("labels", labels);
        chartData.put("datasets", datasets);

        return chartData;
    }
    
    /**
     * 格式化时间标签
     */
    private String formatTimeLabel(String timeStr) {
        try {
            // 如果是ISO格式时间，转换为更友好的显示格式
            if (timeStr.contains("T")) {
                LocalDateTime dateTime = LocalDateTime.parse(timeStr);
                return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm"));
            }
            return timeStr;
        } catch (Exception e) {
            log.warn("时间格式化失败，使用原始值: {}", timeStr);
            return timeStr;
        }
    }

    /**
     * 手动保存性能指标数据（用于测试）
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveMetrics(@RequestBody BrokerMetrics metrics) {
        try {
            boolean success = brokerMetricsService.saveBrokerMetrics(metrics);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "保存性能指标数据成功" : "保存性能指标数据失败");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("保存性能指标数据失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "保存性能指标数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 批量保存性能指标数据（用于测试）
     */
    @PostMapping("/batch-save")
    public ResponseEntity<Map<String, Object>> batchSaveMetrics(@RequestBody List<BrokerMetrics> metricsList) {
        try {
            boolean success = brokerMetricsService.batchSaveBrokerMetrics(metricsList);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "批量保存性能指标数据成功" : "批量保存性能指标数据失败");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("批量保存性能指标数据失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "批量保存性能指标数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 清理历史数据
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupHistoryData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeTime) {

        try {
            int deletedCount = brokerMetricsService.cleanupHistoryData(beforeTime);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("deletedCount", deletedCount);
            response.put("message", "清理历史数据成功，删除 " + deletedCount + " 条记录");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("清理历史数据失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "清理历史数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}