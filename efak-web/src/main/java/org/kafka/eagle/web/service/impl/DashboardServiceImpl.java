package org.kafka.eagle.web.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.core.util.NetUtils;
import org.kafka.eagle.dto.broker.BrokerInfo;
import org.kafka.eagle.dto.dashboard.DashboardStats;
import org.kafka.eagle.dto.dashboard.MetricsTableData;
import org.kafka.eagle.dto.dashboard.TopicAnalysisData;
import org.kafka.eagle.web.mapper.*;
import org.kafka.eagle.web.service.DashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.math.BigDecimal;

/**
 * <p>
 * Dashboard service implementation
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/27 03:09:21
 * @version 5.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final BrokerMapper brokerMapper;
    private final TopicMapper topicMapper;
    private final ConsumerGroupTopicMapper consumerGroupTopicMapper;
    private final PerformanceMonitorMapper performanceMonitorMapper;
    private final TopicInstantMetricsMapper topicInstantMetricsMapper;

    @Override
    public DashboardStats getDashboardStats(String clusterId) {
        try {
            // 1. Calculate online nodes (from ke_broker_info based on cluster_id and NetUtils.telnet)
            Integer onlineNodes = getOnlineNodesCount(clusterId);

            // 2. Get total topics count (from ke_topic_info based on cluster_id)
            Integer totalTopics = getTotalTopicsCount(clusterId);

            // 3. Get total partitions count (sum of partitions from ke_topic_info based on cluster_id)
            Integer totalPartitions = getTotalPartitionsCount(clusterId);

            // 4. Get consumer groups count (distinct group_id from ke_consumer_group_topic based on cluster_id and today's date)
            Integer consumerGroups = getConsumerGroupsCount(clusterId);

            return DashboardStats.builder()
                    .clusterId(clusterId)
                    .onlineNodes(onlineNodes)
                    .totalTopics(totalTopics)
                    .totalPartitions(totalPartitions)
                    .consumerGroups(consumerGroups)
                    .build();

        } catch (Exception e) {
            log.error("Failed to get dashboard stats for cluster: {}", clusterId, e);
            // Return default values in case of error
            return DashboardStats.builder()
                    .clusterId(clusterId)
                    .onlineNodes(0)
                    .totalTopics(0)
                    .totalPartitions(0)
                    .consumerGroups(0)
                    .build();
        }
    }

    /**
     * Calculate online nodes count using NetUtils.telnet
     */
    private Integer getOnlineNodesCount(String clusterId) {
        List<BrokerInfo> brokers = brokerMapper.getBrokersByClusterId(clusterId);
        int onlineCount = 0;

        for (BrokerInfo broker : brokers) {
            if (NetUtils.telnet(broker.getHostIp(), broker.getPort())) {
                onlineCount++;
            }
        }

        return onlineCount;
    }

    /**
     * Get total topics count from ke_topic_info
     */
    private Integer getTotalTopicsCount(String clusterId) {
        // Using a direct query to avoid complex TopicQueryRequest setup
        // This would need a new mapper method - let me add it to the interface
        return topicMapper.countTopicsByClusterId(clusterId).intValue();
    }

    /**
     * Get total partitions count by summing all partitions from ke_topic_info
     */
    private Integer getTotalPartitionsCount(String clusterId) {
        // This would need a new mapper method to sum partitions
        return topicMapper.sumPartitionsByClusterId(clusterId);
    }

    /**
     * Get consumer groups count from ke_consumer_group_topic for today
     */
    private Integer getConsumerGroupsCount(String clusterId) {
        String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return consumerGroupTopicMapper.countDistinctGroupsByClusterIdAndDate(clusterId, todayDate).intValue();
    }

    @Override
    public Object getPerformanceTrend(String clusterId, String period) {
        try {
            // Calculate time range based on period
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = calculateStartTime(endTime, period);

            // Get performance trend data from database
            List<Map<String, Object>> trendData = performanceMonitorMapper.getPerformanceTrendDataByTime(
                    clusterId, startTime, endTime);

            // Transform data for chart display
            Map<String, Object> result = new HashMap<>();
            List<String> labels = new ArrayList<>();
            List<Double> writeLatency = new ArrayList<>();
            List<Double> readLatency = new ArrayList<>();

            DateTimeFormatter formatter = getTimeFormatter(period);

            for (Map<String, Object> data : trendData) {
                String timeStr = (String) data.get("collectTime");
                LocalDateTime time = LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                labels.add(time.format(formatter));

                // Convert null values to 0.0
                Double writeTime = data.get("avgTimeMsProduce") != null ?
                    ((Number) data.get("avgTimeMsProduce")).doubleValue() : 0.0;
                Double readTime = data.get("avgTimeMsConsumer") != null ?
                    ((Number) data.get("avgTimeMsConsumer")).doubleValue() : 0.0;

                writeLatency.add(writeTime);
                readLatency.add(readTime);
            }

            result.put("labels", labels);
            result.put("writeLatency", writeLatency);
            result.put("readLatency", readLatency);

            return result;

        } catch (Exception e) {
            log.error("Failed to get performance trend for cluster: {}, period: {}", clusterId, period, e);
            // Return empty data in case of error
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("labels", new ArrayList<>());
            errorResult.put("writeLatency", new ArrayList<>());
            errorResult.put("readLatency", new ArrayList<>());
            return errorResult;
        }
    }

    /**
     * Calculate start time based on period
     */
    private LocalDateTime calculateStartTime(LocalDateTime endTime, String period) {
        switch (period) {
            case "1h":
                return endTime.minusHours(1);
            case "24h":
                return endTime.minusHours(24);
            case "3d":
                return endTime.minusDays(3);
            case "7d":
                return endTime.minusDays(7);
            default:
                return endTime.minusHours(1);
        }
    }

    /**
     * Get time formatter based on period
     */
    private DateTimeFormatter getTimeFormatter(String period) {
        switch (period) {
            case "1h":
                return DateTimeFormatter.ofPattern("HH:mm");
            case "24h":
                return DateTimeFormatter.ofPattern("HH:mm");
            case "3d":
            case "7d":
                return DateTimeFormatter.ofPattern("MM/dd HH:mm");
            default:
                return DateTimeFormatter.ofPattern("HH:mm");
        }
    }

    @Override
    public TopicAnalysisData getTopicAnalysis(String clusterId) {
        try {
            // 1. 查询总主题数
            Long totalTopics = topicMapper.countTopicsByClusterId(clusterId);

            // 2. 查询活跃和空闲主题统计
            Map<String, Object> activityStats = topicInstantMetricsMapper.getTopicActivityStats(clusterId);
            Integer activeCount = extractInteger(activityStats, "activeCount");
            Integer idleCount = extractInteger(activityStats, "idleCount");

            // 3. 计算活跃占比
            double activePercentage = totalTopics > 0 ?
                (activeCount.doubleValue() / totalTopics.doubleValue()) * 100 : 0.0;

            // 4. 查询总容量
            Long totalCapacity = topicInstantMetricsMapper.getTotalCapacityByCluster(clusterId);

            // 5. 查询容量分布
            Map<String, Object> capacityDist = topicInstantMetricsMapper.getCapacityDistribution(clusterId);

            // 6. 构建主题状态图表数据
            List<String> labels = List.of("活跃主题", "空闲主题");
            List<Integer> values = List.of(activeCount, idleCount);

            // 7. 构建容量分布图表数据
            List<String> capacityLabels = List.of("0-100MB", "100MB-1GB", "1GB-10GB", "10GB+");
            List<Integer> capacityValues = List.of(
                extractInteger(capacityDist, "range0to100MB"),
                extractInteger(capacityDist, "range100MBto1GB"),
                extractInteger(capacityDist, "range1GBto10GB"),
                extractInteger(capacityDist, "range10GBPlus")
            );
            List<String> capacityColors = List.of("#10b981", "#3b82f6", "#f59e0b", "#ef4444");

            // 8. 构建返回数据
            return TopicAnalysisData.builder()
                .type("activity")
                .labels(labels)
                .values(values)
                .stats(TopicAnalysisData.TopicStats.builder()
                    .totalTopics(totalTopics.intValue())
                    .activeTopics(activeCount)
                    .idleTopics(idleCount)
                    .activePercentage(Double.parseDouble(String.format("%.1f", activePercentage)))
                    .totalCapacity(totalCapacity)
                    .totalCapacityFormatted(formatBytes(totalCapacity))
                    .build())
                .capacityDistribution(TopicAnalysisData.CapacityDistribution.builder()
                    .labels(capacityLabels)
                    .values(capacityValues)
                    .colors(capacityColors)
                    .build())
                .build();

        } catch (Exception e) {
            log.error("Failed to get topic analysis for cluster: {}", clusterId, e);
            // 返回默认数据
            return TopicAnalysisData.builder()
                .type("activity")
                .labels(List.of("活跃主题", "空闲主题"))
                .values(List.of(0, 0))
                .stats(TopicAnalysisData.TopicStats.builder()
                    .totalTopics(0)
                    .activeTopics(0)
                    .idleTopics(0)
                    .activePercentage(0.0)
                    .totalCapacity(0L)
                    .totalCapacityFormatted("0B")
                    .build())
                .capacityDistribution(TopicAnalysisData.CapacityDistribution.builder()
                    .labels(List.of("0-100MB", "100MB-1GB", "1GB-10GB", "10GB+"))
                    .values(List.of(0, 0, 0, 0))
                    .colors(List.of("#10b981", "#3b82f6", "#f59e0b", "#ef4444"))
                    .build())
                .build();
        }
    }

    /**
     * 从Map中安全提取Integer值
     */
    private Integer extractInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }

    /**
     * 格式化字节数为可读字符串
     */
    private String formatBytes(Long bytes) {
        if (bytes == null || bytes == 0) return "0B";

        String[] units = {"B", "KB", "MB", "GB", "TB", "PB"};
        int unitIndex = 0;
        double size = bytes.doubleValue();

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        if (unitIndex == 0) {
            return String.format("%.0f%s", size, units[unitIndex]);
        } else {
            return String.format("%.1f%s", size, units[unitIndex]);
        }
    }

    @Override
    public List<MetricsTableData> getMetricsTableData(String clusterId, String dimension) {
        try {
            String metricType = mapDimensionToMetricType(dimension);
            log.info("Getting metrics table data for cluster: {}, dimension: {}, metricType: {}", clusterId, dimension, metricType);

            List<Map<String, Object>> debugInfo = getMetricTypesDebugInfo(clusterId);
            log.debug("Available metric types for cluster {}: {}", clusterId, debugInfo);

            List<Map<String, Object>> rawData = topicInstantMetricsMapper.getMetricsTableDataWithSkew(clusterId, metricType, 10);
            log.info("Retrieved {} rows from database for metrics table", rawData.size());

            List<MetricsTableData> result = new ArrayList<>();

            for (int i = 0; i < rawData.size(); i++) {
                Map<String, Object> row = rawData.get(i);

                String topicName = (String) row.get("topicName");
                String metricValue = (String) row.get("metricValue");
                Integer partitions = (Integer) row.get("partitions");
                Integer replicas = (Integer) row.get("replicas");
                String status = (String) row.get("status");
                String brokerSkewed = (String) row.get("brokerSkewed");

                // Parse metric value - handle potential null or invalid values
                Long rawValue;
                try {
                    if (metricValue != null && !metricValue.trim().isEmpty() && !metricValue.equals("0")) {
                        // Try to parse as double first, then convert to long for bytes
                        double doubleValue = Double.parseDouble(metricValue.trim());
                        rawValue = Math.round(doubleValue);
                    } else {
                        rawValue = 0L;
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid metric value '{}' for topic '{}', using 0. Error: {}", metricValue, topicName, e.getMessage());
                    rawValue = 0L;
                }

                String formattedValue = formatMetricValue(rawValue, dimension);

                // Process broker skewed data
                String formattedSkew = formatBrokerSkewed(brokerSkewed);
                String skewLevel = getSkewLevel(brokerSkewed);

                MetricsTableData tableData = MetricsTableData.builder()
                    .topic(topicName)
                    .description(null) // Remove description to clean up UI
                    .value(formattedValue)
                    .rawValue(rawValue)
                    .brokerSkewed(formattedSkew)
                    .skewLevel(skewLevel)
                    .partitions(partitions != null ? partitions : 0)
                    .replicas(replicas != null ? replicas : 0)
                    .status(status != null ? status : "idle")
                    .rank(i + 1)
                    .build();

                result.add(tableData);
            }

            log.info("Successfully processed {} metrics table entries for dimension {}", result.size(), dimension);
            return result;

        } catch (Exception e) {
            log.error("Failed to get metrics table data for cluster: {}, dimension: {}", clusterId, dimension, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getMetricTypesDebugInfo(String clusterId) {
        try {
            return topicInstantMetricsMapper.getMetricTypesDebugInfo(clusterId);
        } catch (Exception e) {
            log.error("Failed to get metrics debug info for cluster: {}", clusterId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Map UI dimension to database metric type
     */
    private String mapDimensionToMetricType(String dimension) {
        switch (dimension) {
            case "messages":
                return "log_size";
            case "size":
                return "capacity";
            case "read":
                return "byte_out";
            case "write":
                return "byte_in";
            default:
                return "log_size";
        }
    }

    /**
     * Format metric value according to dimension
     */
    private String formatMetricValue(Long value, String dimension) {
        if (value == null || value == 0) {
            return getZeroValue(dimension);
        }

        switch (dimension) {
            case "messages":
                return formatNumber(value.doubleValue(), "条");
            case "size":
                return formatBytes(value);
            case "read":
            case "write":
                return formatByteRate(value); // Use new byte rate formatting
            default:
                return value.toString();
        }
    }

    /**
     * Get zero value string for different dimensions
     */
    private String getZeroValue(String dimension) {
        switch (dimension) {
            case "messages":
                return "0条";
            case "size":
                return "0B";
            case "read":
            case "write":
                return "0.00B/s"; // Updated to match 2 decimal places format
            default:
                return "0";
        }
    }

    /**
     * Format byte rate with automatic unit conversion (B/s, KB/s, MB/s, GB/s)
     * 字节速率格式化，自动适配单位换算，保留2位小数
     */
    private String formatByteRate(Long bytesPerSecond) {
        if (bytesPerSecond == null || bytesPerSecond == 0) return "0.00B/s";

        String[] units = {"B/s", "KB/s", "MB/s", "GB/s", "TB/s", "PB/s"};
        int unitIndex = 0;
        double rate = bytesPerSecond.doubleValue();

        while (rate >= 1024 && unitIndex < units.length - 1) {
            rate /= 1024;
            unitIndex++;
        }

        // Always keep 2 decimal places for all units including B/s
        return String.format("%.2f%s", rate, units[unitIndex]);
    }

    /**
     * Format broker skewed value with status text
     * 格式化数据倾斜值，包含状态文字
     */
    private String formatBrokerSkewed(String brokerSkewed) {
        if (brokerSkewed == null || brokerSkewed.trim().isEmpty()) {
            return "正常(0%)";
        }

        try {
            double skewValue = Double.parseDouble(brokerSkewed);
            String statusText;
            if (skewValue >= 0 && skewValue <= 60) {
                statusText = "正常";
            } else if (skewValue > 60 && skewValue <= 80) {
                statusText = "告警";
            } else {
                statusText = "异常";
            }
            return String.format("%s(%.1f%%)", statusText, skewValue);
        } catch (NumberFormatException e) {
            return "正常(0%)";
        }
    }

    /**
     * Determine skew level based on broker_skewed value
     * 根据数据倾斜值确定倾斜级别
     * 0-60: normal (green), 60-80: warning (yellow), 80+: error (red)
     */
    private String getSkewLevel(String brokerSkewed) {
        if (brokerSkewed == null || brokerSkewed.trim().isEmpty()) {
            return "normal";
        }

        try {
            double skewValue = Double.parseDouble(brokerSkewed);
            if (skewValue >= 0 && skewValue <= 60) {
                return "normal";
            } else if (skewValue > 60 && skewValue <= 80) {
                return "warning";
            } else {
                return "error";
            }
        } catch (NumberFormatException e) {
            return "normal";
        }
    }

    /**
     * Format number with units (copied from existing formatNumber method in dashboard.js)
     */
    private String formatNumber(Double num, String unit) {
        if (num == null || num.isNaN()) return "0" + unit;

        if (num >= 1000000000) {
            return String.format("%.1f十亿%s", num / 1000000000, unit);
        } else if (num >= 100000000) {
            return String.format("%.1f亿%s", num / 100000000, unit);
        } else if (num >= 10000000) {
            return String.format("%.1f千万%s", num / 10000000, unit);
        } else if (num >= 1000000) {
            return String.format("%.1f百万%s", num / 1000000, unit);
        } else if (num >= 10000) {
            return String.format("%.1f万%s", num / 10000, unit);
        } else if (num >= 1000) {
            return String.format("%.1f千%s", num / 1000, unit);
        } else if (num >= 1) {
            return String.format("%d%s", Math.round(num), unit);
        } else {
            return String.format("%.2f%s", num, unit);
        }
    }

}