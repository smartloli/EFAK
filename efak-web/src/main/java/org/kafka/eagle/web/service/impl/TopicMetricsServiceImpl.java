package org.kafka.eagle.web.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.topic.TopicStatisticsDTO;
import org.kafka.eagle.web.mapper.TopicMetricsMapper;
import org.kafka.eagle.web.mapper.TopicInstantMetricsMapper;
import org.kafka.eagle.web.service.TopicMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 * Topic指标服务接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/21 23:56:57
 * @version 5.0.0
 */
@Slf4j
@Service
public class TopicMetricsServiceImpl implements TopicMetricsService {

    @Autowired
    private TopicMetricsMapper topicMetricsMapper;

    @Autowired
    private TopicInstantMetricsMapper topicInstantMetricsMapper;

    @Override
    public TopicStatisticsDTO getTopicStatistics(String clusterId) {
        TopicStatisticsDTO statistics = new TopicStatisticsDTO();

        try {
            // 从ke_topic_instant_metrics表获取当前统计数据（所有集群）
            Map<String, Object> currentStats = topicInstantMetricsMapper.getInstantMetricsStatistics(clusterId);

            if (currentStats != null) {
                // 设置当前值 - 直接从ke_topic_instant_metrics表获取
                statistics.setTotalCapacity(getLongValue(currentStats, "totalCapacity"));
                statistics.setTotalRecordCount(getLongValue(currentStats, "totalRecordCount"));
                statistics.setAvgReadSpeed(getDoubleValue(currentStats, "avgReadSpeed"));
                statistics.setAvgWriteSpeed(getDoubleValue(currentStats, "avgWriteSpeed"));

                // 由于ke_topic_instant_metrics表是即时数据，没有历史对比，设置默认趋势为0
                statistics.setCapacityTrend(new TopicStatisticsDTO.TrendDTO(0.0, 0.0));
                statistics.setRecordCountTrend(new TopicStatisticsDTO.TrendDTO(0.0, 0.0));
                statistics.setReadSpeedTrend(new TopicStatisticsDTO.TrendDTO(0.0, 0.0));
                statistics.setWriteSpeedTrend(new TopicStatisticsDTO.TrendDTO(0.0, 0.0));
            } else {
                log.warn("未找到ke_topic_instant_metrics表中的统计数据");
                // 设置默认值
                setDefaultValues(statistics);
            }

        } catch (Exception e) {
            log.error("从ke_topic_instant_metrics表获取统计数据失败", e);
            // 设置默认值
            setDefaultValues(statistics);
        }

        return statistics;
    }

    @Override
    public Map<String, Object> getTopicTrendData(String dimension, String startDate, String endDate, List<String> topics, String clusterId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 参数验证和默认值设置
            if (startDate == null || endDate == null) {
                // 默认查询最近7天
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                endDate = sdf.format(cal.getTime());
                cal.add(Calendar.DAY_OF_MONTH, -6);
                startDate = sdf.format(cal.getTime());
            }

            if (dimension == null) {
                dimension = "capacity";
            }

            // 查询趋势数据（增量数据）
            List<Map<String, Object>> trendDataList = topicMetricsMapper.getTrendData(
                dimension, startDate, endDate, topics, clusterId);

            // 构建时间标签列表
            List<String> labels = generateDateLabels(startDate, endDate);

            // 构建数据集
            List<Map<String, Object>> datasets = new ArrayList<>();

            if (topics == null || topics.isEmpty()) {
                // 查询所有主题的聚合数据
                Map<String, Object> aggregateDataset = buildAggregateDataset(trendDataList, labels, dimension);
                datasets.add(aggregateDataset);
            } else {
                // 查询指定主题的数据
                datasets = buildTopicDatasets(trendDataList, labels, dimension, topics);
            }

            result.put("labels", labels);
            result.put("datasets", datasets);

        } catch (Exception e) {
            log.error("获取趋势数据失败", e);
            // 返回空数据
            result.put("labels", new ArrayList<>());
            result.put("datasets", new ArrayList<>());
        }

        return result;
    }

    /**
     * 生成日期标签列表
     */
    private List<String> generateDateLabels(String startDate, String endDate) {
        List<String> labels = new ArrayList<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat labelFormat = new SimpleDateFormat("MM-dd");

            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            Calendar cal = Calendar.getInstance();
            cal.setTime(start);

            while (!cal.getTime().after(end)) {
                labels.add(labelFormat.format(cal.getTime()));
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        } catch (Exception e) {
            log.error("生成日期标签失败", e);
        }
        return labels;
    }

    /**
     * 构建聚合数据集（所有主题合计）
     */
    private Map<String, Object> buildAggregateDataset(List<Map<String, Object>> trendDataList,
                                                     List<String> labels, String dimension) {
        Map<String, Object> dataset = new HashMap<>();
        List<Double> data = new ArrayList<>();

        // 按日期聚合增量数据
        Map<String, Double> dateValueMap = new HashMap<>();
        for (Map<String, Object> record : trendDataList) {
            String dateStr = record.get("date").toString();
            // 使用增量字段进行累加计算
            String diffField = dimension.equals("capacity") ? "totalCapacity" : "totalRecordCount";
            Double diffValue = getDoubleValue(record, diffField);
            dateValueMap.put(dateStr, dateValueMap.getOrDefault(dateStr, 0.0) + diffValue);
        }

        // 按标签顺序填充数据
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat labelFormat = new SimpleDateFormat("MM-dd");

        for (String label : labels) {
            try {
                // 将MM-dd格式的标签转换为完整的yyyy-MM-dd格式
                Date labelDate = labelFormat.parse(label);
                Calendar cal = Calendar.getInstance();
                cal.setTime(labelDate);
                cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR)); // 使用当前年份

                String dateKey = sdf.format(cal.getTime());
                Double dayValue = dateValueMap.getOrDefault(dateKey, 0.0);
                data.add(dayValue);
            } catch (Exception e) {
                log.warn("处理日期标签失败: {}", label, e);
                data.add(0.0);
            }
        }

        dataset.put("label", dimension.equals("capacity") ? "总容量增量" : "总记录数增量");
        dataset.put("data", data);
        dataset.put("borderColor", "#3b82f6");
        dataset.put("backgroundColor", "rgba(59, 130, 246, 0.1)");

        return dataset;
    }

    /**
     * 构建主题数据集
     */
    private List<Map<String, Object>> buildTopicDatasets(List<Map<String, Object>> trendDataList,
                                                        List<String> labels, String dimension, List<String> topics) {
        List<Map<String, Object>> datasets = new ArrayList<>();
        String[] colors = {"#3b82f6", "#ef4444", "#10b981", "#f59e0b", "#8b5cf6"};

        for (int i = 0; i < topics.size(); i++) {
            String topicName = topics.get(i);
            Map<String, Object> dataset = new HashMap<>();
            List<Double> data = new ArrayList<>();

            // 过滤当前主题的增量数据
            Map<String, Double> dateValueMap = new HashMap<>();
            for (Map<String, Object> record : trendDataList) {
                if (topicName.equals(record.get("topicName"))) {
                    String dateStr = record.get("date").toString();
                    // 使用增量字段
                    String diffField = dimension.equals("capacity") ? "totalCapacity" : "totalRecordCount";
                    Double diffValue = getDoubleValue(record, diffField);
                    dateValueMap.put(dateStr, diffValue);
                }
            }

            // 按标签顺序填充数据
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat labelFormat = new SimpleDateFormat("MM-dd");

            for (String label : labels) {
                try {
                    // 将MM-dd格式的标签转换为完整的yyyy-MM-dd格式
                    Date labelDate = labelFormat.parse(label);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(labelDate);
                    cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR)); // 使用当前年份

                    String dateKey = sdf.format(cal.getTime());
                    Double dayValue = dateValueMap.getOrDefault(dateKey, 0.0);
                    data.add(dayValue);
                } catch (Exception e) {
                    log.warn("处理主题 {} 日期标签失败: {}", topicName, label, e);
                    data.add(0.0);
                }
            }

            dataset.put("label", topicName + (dimension.equals("capacity") ? " (容量增量)" : " (记录数增量)"));
            dataset.put("data", data);
            dataset.put("borderColor", colors[i % colors.length]);
            dataset.put("backgroundColor", colors[i % colors.length] + "20");

            datasets.add(dataset);
        }

        return datasets;
    }

    /**
     * 设置默认值
     */
    private void setDefaultValues(TopicStatisticsDTO statistics) {
        statistics.setTotalCapacity(0L);
        statistics.setTotalRecordCount(0L);
        statistics.setAvgReadSpeed(0.0);
        statistics.setAvgWriteSpeed(0.0);
        statistics.setCapacityTrend(new TopicStatisticsDTO.TrendDTO(0.0, 0.0));
        statistics.setRecordCountTrend(new TopicStatisticsDTO.TrendDTO(0.0, 0.0));
        statistics.setReadSpeedTrend(new TopicStatisticsDTO.TrendDTO(0.0, 0.0));
        statistics.setWriteSpeedTrend(new TopicStatisticsDTO.TrendDTO(0.0, 0.0));
    }

    /**
     * 计算趋势
     */
    private TopicStatisticsDTO.TrendDTO calculateTrend(Number current, Number previous) {
        if (current == null || previous == null) {
            return new TopicStatisticsDTO.TrendDTO(0.0, 0.0);
        }

        double currentValue = current.doubleValue();
        double previousValue = previous.doubleValue();

        if (previousValue == 0) {
            // 如果前一天的值为0，直接返回当前值作为增长
            return new TopicStatisticsDTO.TrendDTO(currentValue, currentValue > 0 ? 100.0 : 0.0);
        }

        double difference = currentValue - previousValue;
        double percentage = (difference / previousValue) * 100;

        return new TopicStatisticsDTO.TrendDTO(difference, percentage);
    }

    /**
     * 从Map中安全获取Long值
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0L;
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).longValue();
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            log.warn("无法将值转换为Long: {} = {}", key, value);
            return 0L;
        }
    }

    /**
     * 从Map中安全获取Double值
     */
    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0.0;
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).doubleValue();
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            log.warn("无法将值转换为Double: {} = {}", key, value);
            return 0.0;
        }
    }

    @Override
    public List<Map<String, Object>> getTopicMessageFlowTrend(String clusterId, String topicName, String timeRange) {
        try {
            // 参数验证
            if (topicName == null || topicName.trim().isEmpty()) {
                log.warn("主题名称为空，返回空数据");
                return new ArrayList<>();
            }

            // 计算时间范围的开始和结束时间戳
            Long[] timeRangeMillis = calculateTimeRange(timeRange);
            Long startTime = timeRangeMillis[0];
            Long endTime = timeRangeMillis[1];

            // 查询数据库
            List<Map<String, Object>> flowData = topicMetricsMapper.getTopicMessageFlowTrend(
                    clusterId, topicName, startTime, endTime);

            return flowData != null ? flowData : new ArrayList<>();

        } catch (Exception e) {
            log.error("获取消息流量趋势数据失败：clusterId={}, topicName={}, timeRange={}",
                      clusterId, topicName, timeRange, e);
            return new ArrayList<>();
        }
    }

    /**
     * 计算时间范围的开始和结束时间戳（毫秒）
     */
    private Long[] calculateTimeRange(String timeRange) {
        long currentTime = System.currentTimeMillis();
        long startTime;

        switch (timeRange) {
            case "1h":
                startTime = currentTime - (60 * 60 * 1000L); // 1小时前
                break;
            case "6h":
                startTime = currentTime - (6 * 60 * 60 * 1000L); // 6小时前
                break;
            case "1d":
                startTime = currentTime - (24 * 60 * 60 * 1000L); // 1天前
                break;
            case "3d":
                startTime = currentTime - (3 * 24 * 60 * 60 * 1000L); // 3天前
                break;
            case "7d":
                startTime = currentTime - (7 * 24 * 60 * 60 * 1000L); // 7天前
                break;
            default:
                // 默认1天
                startTime = currentTime - (24 * 60 * 60 * 1000L);
                break;
        }

        return new Long[]{startTime, currentTime};
    }
}