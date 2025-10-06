package org.kafka.eagle.web.service;

import org.kafka.eagle.dto.broker.BrokerMetrics;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Broker性能指标服务接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/14 01:06:49
 * @version 5.0.0
 */
public interface BrokerMetricsService {

        /**
         * 时间维度枚举
         */
        enum TimeRange {
                FIVE_MINUTES("5m", 5, "MINUTE", "raw"),
                FIFTEEN_MINUTES("15m", 15, "MINUTE", "raw"),
                THIRTY_MINUTES("30m", 30, "MINUTE", "raw"),
                ONE_HOUR("1h", 1, "HOUR", "raw"),
                SIX_HOURS("6h", 6, "HOUR", "hourly"),
                TWENTY_FOUR_HOURS("24h", 24, "HOUR", "hourly"),
                SEVEN_DAYS("7d", 7, "DAY", "daily"),
                THIRTY_DAYS("30d", 30, "DAY", "daily");

                private final String code;
                private final int amount;
                private final String unit;
                private final String aggregationType;

                TimeRange(String code, int amount, String unit, String aggregationType) {
                        this.code = code;
                        this.amount = amount;
                        this.unit = unit;
                        this.aggregationType = aggregationType;
                }

                public String getCode() {
                        return code;
                }

                public int getAmount() {
                        return amount;
                }

                public String getUnit() {
                        return unit;
                }

                public String getAggregationType() {
                        return aggregationType;
                }

                public static TimeRange fromCode(String code) {
                        for (TimeRange range : values()) {
                                if (range.code.equals(code)) {
                                        return range;
                                }
                        }
                        return ONE_HOUR; // 默认返回1小时
                }

                public LocalDateTime calculateStartTime(LocalDateTime endTime) {
                        switch (unit) {
                                case "MINUTE":
                                        return endTime.minusMinutes(amount);
                                case "HOUR":
                                        return endTime.minusHours(amount);
                                case "DAY":
                                        return endTime.minusDays(amount);
                                default:
                                        return endTime.minusHours(1);
                        }
                }
        }

        /**
         * 保存Broker性能指标数据
         */
        boolean saveBrokerMetrics(BrokerMetrics metrics);

        /**
         * 批量保存Broker性能指标数据
         */
        boolean batchSaveBrokerMetrics(List<BrokerMetrics> metricsList);

        /**
         * 获取统一的趋势数据（支持集群过滤和多种时间维度）
         *
         * @param metricType 指标类型: "cpu" 或 "memory"
         * @param timeRange  时间范围代码
         * @param clusterId  集群ID，为null时查询所有集群
         * @return 趋势数据，格式化为前端图表所需的格式
         */
        Map<String, Object> getTrendData(String metricType, String timeRange, String clusterId);

        /**
         * 查询指定时间范围内的Broker性能指标数据
         */
        List<BrokerMetrics> queryMetricsByTimeRange(Integer brokerId, LocalDateTime startTime, LocalDateTime endTime);


        /**
         * 查询最新的Broker性能指标数据
         */
        List<BrokerMetrics> queryLatestMetrics(Integer brokerId, Integer limit);

        /**
         * 清理历史数据
         *
         * @param beforeTime 清理此时间之前的数据
         * @return 清理的记录数
         */
        int cleanupHistoryData(LocalDateTime beforeTime);

        /**
         * 清理历史数据
         *
         * @param retentionDays 保留天数
         * @return 清理的记录数
         */
        int cleanupHistoricalData(int retentionDays);


        /**
         * 获取Broker性能指标统计信息
         *
         * @param brokerId  Broker ID
         * @param startTime 开始时间
         * @param endTime   结束时间
         * @return 统计信息
         */
        Map<String, Object> getBrokerMetricsStats(Integer brokerId, LocalDateTime startTime, LocalDateTime endTime);


        /**
         * 查询趋势数据（支持集群过滤，统一接口）
         *
         * @param metricType      指标类型: "cpu" 或 "memory"
         * @param clusterId       集群ID，为null时查询所有集群
         * @param startTime       开始时间
         * @param endTime         结束时间
         * @param aggregationType 聚合类型：raw(原始数据)、hourly(按小时)、daily(按天)
         * @return 趋势数据
         */
        List<Map<String, Object>> queryTrendDataByCluster(String metricType, String clusterId,
                        LocalDateTime startTime, LocalDateTime endTime, String aggregationType);
}