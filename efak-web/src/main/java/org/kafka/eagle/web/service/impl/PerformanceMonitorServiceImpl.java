package org.kafka.eagle.web.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.performance.PerformanceMonitor;
import org.kafka.eagle.dto.performance.PerformanceQueryRequest;
import org.kafka.eagle.dto.performance.PerformancePageResponse;
import org.kafka.eagle.web.mapper.PerformanceMonitorMapper;
import org.kafka.eagle.web.service.PerformanceMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * <p>
 * 性能监控服务接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/24 23:36:27
 * @version 5.0.0
 */
@Slf4j
@Service
public class PerformanceMonitorServiceImpl implements PerformanceMonitorService {

    @Autowired
    private PerformanceMonitorMapper performanceMonitorMapper;

    @Override
    public int insertPerformanceMonitor(PerformanceMonitor performanceMonitor) {
        try {
            // 自动设置采集时间和日期如果未设置
            if (performanceMonitor.getCollectTime() == null) {
                performanceMonitor.setCollectTime(java.time.LocalDateTime.now());
            }
            if (performanceMonitor.getCollectDate() == null) {
                performanceMonitor.setCollectDate(LocalDate.now());
            }

            int result = performanceMonitorMapper.insert(performanceMonitor);
            log.info("Successfully inserted performance monitor data for cluster: {}", performanceMonitor.getClusterId());
            return result;
        } catch (Exception e) {
            log.error("Failed to insert performance monitor data for cluster: {}", performanceMonitor.getClusterId(), e);
            throw e;
        }
    }

    @Override
    public int batchInsertPerformanceMonitors(List<PerformanceMonitor> performanceMonitorList) {
        if (performanceMonitorList == null || performanceMonitorList.isEmpty()) {
            return 0;
        }

        try {
            // 自动设置采集时间和日期
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            LocalDate today = LocalDate.now();

            performanceMonitorList.forEach(monitor -> {
                if (monitor.getCollectTime() == null) {
                    monitor.setCollectTime(now);
                }
                if (monitor.getCollectDate() == null) {
                    monitor.setCollectDate(today);
                }
            });

            int result = performanceMonitorMapper.batchInsert(performanceMonitorList);
            log.info("Successfully batch inserted {} performance monitor records", result);
            return result;
        } catch (Exception e) {
            log.error("Failed to batch insert performance monitor data", e);
            throw e;
        }
    }

    @Override
    public PerformancePageResponse queryPerformanceMonitors(PerformanceQueryRequest request) {
        try {
            Map<String, Object> params = buildQueryParams(request);

            // 查询总数
            int total = performanceMonitorMapper.countPerformanceMonitors(params);

            // 查询数据
            List<PerformanceMonitor> records = performanceMonitorMapper.selectPerformanceMonitors(params);

            // 构建响应
            PerformancePageResponse response = new PerformancePageResponse();
            response.setTotal((long) total);
            response.setRecords(records);
            response.setPageNum(request.getPageNum());
            response.setPageSize(request.getPageSize());
            response.setTotalPages((int) Math.ceil((double) total / request.getPageSize()));

            return response;
        } catch (Exception e) {
            log.error("Failed to query performance monitors", e);
            throw e;
        }
    }

    @Override
    public List<PerformanceMonitor> getLatestPerformanceMonitors() {
        try {
            return performanceMonitorMapper.selectLatestPerformanceMonitors();
        } catch (Exception e) {
            log.error("Failed to get latest performance monitors", e);
            throw e;
        }
    }

    @Override
    public Map<String, Object> getPerformanceStatistics(String clusterId, String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);

            Map<String, Object> statistics = performanceMonitorMapper.getPerformanceStatistics(clusterId, start, end);

            if (statistics == null) {
                statistics = new HashMap<>();
            }

            log.info("Retrieved performance statistics for cluster {} from {} to {}", clusterId, startDate, endDate);
            return statistics;
        } catch (Exception e) {
            log.error("Failed to get performance statistics for cluster: {}", clusterId, e);
            throw e;
        }
    }

    @Override
    public Map<String, Object> getPerformanceTrendData(String clusterId, String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);

            List<Map<String, Object>> trendData = performanceMonitorMapper.getPerformanceTrendData(clusterId, start, end);

            Map<String, Object> result = new HashMap<>();
            result.put("trendData", trendData);
            result.put("clusterId", clusterId);
            result.put("startDate", startDate);
            result.put("endDate", endDate);

            log.info("Retrieved performance trend data for cluster {} from {} to {}", clusterId, startDate, endDate);
            return result;
        } catch (Exception e) {
            log.error("Failed to get performance trend data for cluster: {}", clusterId, e);
            throw e;
        }
    }

    @Override
    public PerformanceMonitor getLatestPerformanceByCluster(String clusterId) {
        try {
            return performanceMonitorMapper.selectLatestByClusterId(clusterId);
        } catch (Exception e) {
            log.error("Failed to get latest performance data for cluster: {}", clusterId, e);
            throw e;
        }
    }

    @Override
    public int cleanupOldData(int retentionDays) {
        try {
            LocalDate cutoffDate = LocalDate.now().minusDays(retentionDays);
            int deletedCount = performanceMonitorMapper.deleteOldMonitors(cutoffDate);
            log.info("Cleaned up {} old performance monitor records older than {}", deletedCount, cutoffDate);
            return deletedCount;
        } catch (Exception e) {
            log.error("Failed to cleanup old performance monitor data", e);
            throw e;
        }
    }

    /**
     * Build query parameters map
     */
    private Map<String, Object> buildQueryParams(PerformanceQueryRequest request) {
        Map<String, Object> params = new HashMap<>();

        if (request.getClusterId() != null && !request.getClusterId().trim().isEmpty()) {
            params.put("clusterId", request.getClusterId());
        }

        if (request.getKafkaHost() != null && !request.getKafkaHost().trim().isEmpty()) {
            params.put("kafkaHost", request.getKafkaHost());
        }

        if (request.getMemoryUsageThreshold() != null) {
            params.put("memoryUsageThreshold", request.getMemoryUsageThreshold());
        }

        if (request.getCpuUsageThreshold() != null) {
            params.put("cpuUsageThreshold", request.getCpuUsageThreshold());
        }

        if (request.getMessageInThreshold() != null) {
            params.put("messageInThreshold", request.getMessageInThreshold());
        }

        if (request.getByteInThreshold() != null) {
            params.put("byteInThreshold", request.getByteInThreshold());
        }

        if (request.getByteOutThreshold() != null) {
            params.put("byteOutThreshold", request.getByteOutThreshold());
        }

        if (request.getTimeMsProduceThreshold() != null) {
            params.put("timeMsProduceThreshold", request.getTimeMsProduceThreshold());
        }

        if (request.getStartDate() != null) {
            params.put("startDate", request.getStartDate());
        }

        if (request.getEndDate() != null) {
            params.put("endDate", request.getEndDate());
        }

        // 分页参数
        if (request.getPageNum() != null && request.getPageSize() != null) {
            int offset = (request.getPageNum() - 1) * request.getPageSize();
            params.put("offset", offset);
            params.put("limit", request.getPageSize());
        }

        return params;
    }

    @Override
    public List<PerformanceMonitor> getPerformanceByKafkaHost(String kafkaHost, String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);

            return performanceMonitorMapper.selectByKafkaHost(kafkaHost, start, end);
        } catch (Exception e) {
            log.error("Failed to get performance data by Kafka host: {}", kafkaHost, e);
            throw e;
        }
    }

    @Override
    public List<PerformanceMonitor> getHighResourceUsageAlerts(java.math.BigDecimal memoryThreshold, java.math.BigDecimal cpuThreshold) {
        try {
            return performanceMonitorMapper.selectHighResourceUsage(memoryThreshold, cpuThreshold);
        } catch (Exception e) {
            log.error("Failed to get high resource usage alerts", e);
            throw e;
        }
    }

    @Override
    public Map<String, Object> getResourceUsageTrendByHost(String kafkaHost, String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);

            List<Map<String, Object>> trendData = performanceMonitorMapper.getResourceUsageTrendByHost(kafkaHost, start, end);

            Map<String, Object> result = new HashMap<>();
            result.put("trendData", trendData);
            result.put("kafkaHost", kafkaHost);
            result.put("startDate", startDate);
            result.put("endDate", endDate);

            log.info("Retrieved resource usage trend data for host {} from {} to {}", kafkaHost, startDate, endDate);
            return result;
        } catch (Exception e) {
            log.error("Failed to get resource usage trend data for host: {}", kafkaHost, e);
            throw e;
        }
    }

    @Override
    public PerformanceMonitor getLatestPerformanceByClusterWithin10Minutes(String clusterId) {
        try {
            return performanceMonitorMapper.selectLatestPerformanceByClusterWithin10Minutes(clusterId);
        } catch (Exception e) {
            log.error("Failed to get latest performance data for cluster: {}", clusterId, e);
            throw e;
        }
    }

    @Override
    public Map<String, Object> getPerformanceTrendDataByTimeRange(String clusterId, String timeRange) {
        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime;

            // 根据时间范围计算开始时间
            switch (timeRange.toLowerCase()) {
                case "1h":
                    startTime = endTime.minusHours(1);
                    break;
                case "6h":
                    startTime = endTime.minusHours(6);
                    break;
                case "24h":
                    startTime = endTime.minusDays(1);
                    break;
                case "3d":
                    startTime = endTime.minusDays(3);
                    break;
                case "7d":
                    startTime = endTime.minusDays(7);
                    break;
                default:
                    startTime = endTime.minusDays(1); // 默认24小时
            }

            // 使用新的基于时间的趋势数据查询方法
            List<Map<String, Object>> trendData = performanceMonitorMapper.getPerformanceTrendDataByTime(clusterId, startTime, endTime);

            // 处理趋势数据，计算集群级别的吞吐量汇总
            List<Map<String, Object>> processedData = processTrendDataForCluster(trendData, timeRange);

            Map<String, Object> result = new HashMap<>();
            result.put("trendData", processedData);
            result.put("clusterId", clusterId);
            result.put("timeRange", timeRange);
            result.put("startTime", startTime);
            result.put("endTime", endTime);

            log.info("Retrieved performance trend data by time range for cluster {} with range {}", clusterId, timeRange);
            return result;
        } catch (Exception e) {
            log.error("Failed to get performance trend data by time range for cluster: {}", clusterId, e);
            throw e;
        }
    }

    /**
     * 处理趋势数据，用于集群级别的数据汇总和图表展示
     */
    private List<Map<String, Object>> processTrendDataForCluster(List<Map<String, Object>> rawData, String timeRange) {
        if (rawData == null || rawData.isEmpty()) {
            return new ArrayList<>();
        }

        // 根据时间范围确定聚合方式
        Map<String, Map<String, Object>> aggregatedData = new LinkedHashMap<>();

        for (Map<String, Object> data : rawData) {
            String collectTimeStr = (String) data.get("collectTime");

            // 根据时间范围进行不同粒度的时间聚合
            String timeKey = getAggregationTimeKey(collectTimeStr, timeRange);

            // 如果该时间段还没有数据，初始化
            aggregatedData.computeIfAbsent(timeKey, k -> {
                Map<String, Object> newData = new HashMap<>();
                newData.put("time", k);
                newData.put("messageInSum", 0.0);
                newData.put("byteInSum", 0.0);
                newData.put("byteOutSum", 0.0);
                newData.put("timeMsProduceAvg", 0.0);
                newData.put("timeMsConsumerAvg", 0.0);
                newData.put("count", 0);
                return newData;
            });

            // 累加数据
            Map<String, Object> existing = aggregatedData.get(timeKey);
            existing.put("messageInSum", (Double) existing.get("messageInSum") + getDoubleValue(data.get("avgMessageIn")));
            existing.put("byteInSum", (Double) existing.get("byteInSum") + getDoubleValue(data.get("avgByteIn")));
            existing.put("byteOutSum", (Double) existing.get("byteOutSum") + getDoubleValue(data.get("avgByteOut")));
            existing.put("timeMsProduceAvg", (Double) existing.get("timeMsProduceAvg") + getDoubleValue(data.get("avgTimeMsProduce")));
            existing.put("timeMsConsumerAvg", (Double) existing.get("timeMsConsumerAvg") + getDoubleValue(data.get("avgTimeMsConsumer")));
            existing.put("count", (Integer) existing.get("count") + 1);
        }

        // 计算平均值并构建最终结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> data : aggregatedData.values()) {
            int count = (Integer) data.get("count");
            if (count > 0) {
                Map<String, Object> processedItem = new HashMap<>();
                processedItem.put("time", data.get("time"));
                processedItem.put("messageIn", roundToTwoDecimals((Double) data.get("messageInSum"))); // 集群总吞吐量
                processedItem.put("byteIn", roundToTwoDecimals((Double) data.get("byteInSum")));
                processedItem.put("byteOut", roundToTwoDecimals((Double) data.get("byteOutSum")));
                processedItem.put("timeMsProduce", roundToTwoDecimals((Double) data.get("timeMsProduceAvg") / count));
                processedItem.put("timeMsConsumer", roundToTwoDecimals((Double) data.get("timeMsConsumerAvg") / count));
                result.add(processedItem);
            }
        }

        return result;
    }

    /**
     * 根据时间范围获取聚合的时间键
     */
    private String getAggregationTimeKey(String collectTimeStr, String timeRange) {
        try {
            LocalDateTime collectTime = LocalDateTime.parse(collectTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            switch (timeRange.toLowerCase()) {
                case "1h":
                    // 1小时：按分钟聚合
                    return collectTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                case "6h":
                    // 6小时：按10分钟聚合
                    int minute = collectTime.getMinute();
                    int roundedMinute = (minute / 10) * 10;
                    return collectTime.withMinute(roundedMinute).format(DateTimeFormatter.ofPattern("HH:mm"));
                case "24h":
                    // 24小时：按小时聚合
                    return collectTime.format(DateTimeFormatter.ofPattern("MM-dd HH:00"));
                case "3d":
                case "7d":
                    // 3天和7天：按小时聚合
                    return collectTime.format(DateTimeFormatter.ofPattern("MM-dd HH:00"));
                default:
                    return collectTime.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
            }
        } catch (Exception e) {
            log.warn("Failed to parse collect time: {}, using original string", collectTimeStr);
            return collectTimeStr;
        }
    }

    /**
     * 安全地获取Double值
     */
    private Double getDoubleValue(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * 将Double值四舍五入到2位小数
     */
    private Double roundToTwoDecimals(Double value) {
        if (value == null) {
            return 0.0;
        }
        return Math.round(value * 100.0) / 100.0;
    }
}