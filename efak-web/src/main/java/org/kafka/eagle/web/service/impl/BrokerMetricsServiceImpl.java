package org.kafka.eagle.web.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.broker.BrokerMetrics;
import org.kafka.eagle.web.mapper.BrokerMetricsMapper;
import org.kafka.eagle.web.service.BrokerMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * Broker性能指标服务实现类
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/14 01:08:44
 * @version 5.0.0
 */
@Slf4j
@Service
public class BrokerMetricsServiceImpl implements BrokerMetricsService {

    @Autowired
    private BrokerMetricsMapper brokerMetricsMapper;

    @Override
    @Transactional
    public boolean saveBrokerMetrics(BrokerMetrics metrics) {
        try {
            if (metrics == null) {
                log.warn("BrokerMetrics对象为空，无法保存");
                return false;
            }

            // 设置采集时间为当前时间（如果未设置）
            if (metrics.getCollectTime() == null) {
                metrics.setCollectTime(LocalDateTime.now());
            }

            int result = brokerMetricsMapper.insertBrokerMetrics(metrics);
            if (result > 0) {
                return true;
            } else {
                log.warn("保存Broker {} 性能指标数据失败，影响行数为0", metrics.getBrokerId());
                return false;
            }
        } catch (Exception e) {
            log.error("保存Broker性能指标数据异常", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean batchSaveBrokerMetrics(List<BrokerMetrics> metricsList) {
        try {
            if (metricsList == null || metricsList.isEmpty()) {
                log.warn("BrokerMetrics列表为空，无法批量保存");
                return false;
            }

            // 设置采集时间为当前时间（如果未设置）
            LocalDateTime now = LocalDateTime.now();
            for (BrokerMetrics metrics : metricsList) {
                if (metrics.getCollectTime() == null) {
                    metrics.setCollectTime(now);
                }
            }

            int result = brokerMetricsMapper.batchInsertBrokerMetrics(metricsList);
            if (result > 0) {
                return true;
            } else {
                log.warn("批量保存Broker性能指标数据失败，影响行数为0");
                return false;
            }
        } catch (Exception e) {
            log.error("批量保存Broker性能指标数据异常", e);
            return false;
        }
    }

    @Override
    public List<BrokerMetrics> queryMetricsByTimeRange(Integer brokerId, LocalDateTime startTime,
            LocalDateTime endTime) {
        try {
            return brokerMetricsMapper.queryMetricsByTimeRange(brokerId, startTime, endTime);
        } catch (Exception e) {
            log.error("查询Broker性能指标数据异常", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<BrokerMetrics> queryLatestMetrics(Integer brokerId, Integer limit) {
        try {
            if (limit == null || limit <= 0) {
                limit = 100; // 默认查询最近100条记录
            }
            return brokerMetricsMapper.queryLatestMetrics(brokerId, limit);
        } catch (Exception e) {
            log.error("查询最新Broker性能指标数据异常", e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public int cleanupHistoryData(LocalDateTime beforeTime) {
        try {
            int deletedCount = brokerMetricsMapper.deleteMetricsBeforeTime(beforeTime);
            return deletedCount;
        } catch (Exception e) {
            log.error("清理历史数据异常", e);
            return 0;
        }
    }

    @Override
    public int cleanupHistoricalData(int retentionDays) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
        return brokerMetricsMapper.deleteHistoricalData(cutoffTime);
    }

    @Override
    public Map<String, Object> getBrokerMetricsStats(Integer brokerId, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> stats = new HashMap<>();

        try {
            List<BrokerMetrics> metrics = queryMetricsByTimeRange(brokerId, startTime, endTime);

            if (metrics.isEmpty()) {
                stats.put("dataCount", 0);
                stats.put("avgCpuUsage", 0.0);
                stats.put("maxCpuUsage", 0.0);
                stats.put("minCpuUsage", 0.0);
                stats.put("avgMemoryUsage", 0.0);
                stats.put("maxMemoryUsage", 0.0);
                stats.put("minMemoryUsage", 0.0);
                return stats;
            }

            // 计算统计信息
            double avgCpu = metrics.stream()
                    .mapToDouble(m -> m.getCpuUsage().doubleValue())
                    .average()
                    .orElse(0.0);

            double maxCpu = metrics.stream()
                    .mapToDouble(m -> m.getCpuUsage().doubleValue())
                    .max()
                    .orElse(0.0);

            double minCpu = metrics.stream()
                    .mapToDouble(m -> m.getCpuUsage().doubleValue())
                    .min()
                    .orElse(0.0);

            double avgMemory = metrics.stream()
                    .mapToDouble(m -> m.getMemoryUsage().doubleValue())
                    .average()
                    .orElse(0.0);

            double maxMemory = metrics.stream()
                    .mapToDouble(m -> m.getMemoryUsage().doubleValue())
                    .max()
                    .orElse(0.0);

            double minMemory = metrics.stream()
                    .mapToDouble(m -> m.getMemoryUsage().doubleValue())
                    .min()
                    .orElse(0.0);

            stats.put("dataCount", metrics.size());
            stats.put("avgCpuUsage", Math.round(avgCpu * 100.0) / 100.0);
            stats.put("maxCpuUsage", Math.round(maxCpu * 100.0) / 100.0);
            stats.put("minCpuUsage", Math.round(minCpu * 100.0) / 100.0);
            stats.put("avgMemoryUsage", Math.round(avgMemory * 100.0) / 100.0);
            stats.put("maxMemoryUsage", Math.round(maxMemory * 100.0) / 100.0);
            stats.put("minMemoryUsage", Math.round(minMemory * 100.0) / 100.0);

        } catch (Exception e) {
            log.error("获取Broker性能指标统计信息异常", e);
        }

        return stats;
    }

    @Override
    public Map<String, Object> getTrendData(String metricType, String timeRange, String clusterId) {
        try {
            BrokerMetricsService.TimeRange range = BrokerMetricsService.TimeRange.fromCode(timeRange);
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = range.calculateStartTime(endTime);

            List<Map<String, Object>> trendData = queryTrendDataByCluster(
                    metricType, clusterId, startTime, endTime, range.getAggregationType());

            return formatChartData(trendData, metricType);
        } catch (Exception e) {
            log.error("获取{}趋势数据失败", metricType, e);
            throw new RuntimeException("获取" + metricType + "趋势数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> queryTrendDataByCluster(String metricType, String clusterId,
            LocalDateTime startTime, LocalDateTime endTime, String aggregationType) {
        try {
            return brokerMetricsMapper.queryTrendDataUnified(metricType, clusterId, startTime, endTime, aggregationType);
        } catch (Exception e) {
            log.error("查询{}趋势数据失败", metricType, e);
            throw new RuntimeException("查询" + metricType + "趋势数据失败: " + e.getMessage(), e);
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

        // 第一遍遍历：收集所有时间点和broker信息
        for (Map<String, Object> data : trendData) {
            Object timeLabelObj = data.get("collectTime");
            if (timeLabelObj == null) {
                log.warn("数据中缺少collectTime字段，跳过该条记录: {}", data);
                continue;
            }
            String timeLabel = formatTimeLabel(timeLabelObj.toString());

            // 安全获取hostIp
            Object hostIpObj = data.get("hostIp");
            if (hostIpObj == null) {
                log.warn("数据中缺少hostIp字段，跳过该条记录: {}", data);
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

            // 收集hostIp
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
}