package org.kafka.eagle.dto.topic;

import lombok.Data;

/**
 * <p>
 * Topic 统计数据传输对象类，用于存储 Topic 的统计信息。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/9/26 23:15:05
 * @version 5.0.0
 */
@Data
public class TopicStatisticsDTO {
    /**
     * 当前总容量（字节）
     */
    private Long totalCapacity;

    /**
     * 当前总记录数
     */
    private Long totalRecordCount;

    /**
     * 当前平均读取速度（字节/秒）
     */
    private Double avgReadSpeed;

    /**
     * 当前平均写入速度（字节/秒）
     */
    private Double avgWriteSpeed;

    /**
     * 容量趋势
     */
    private TrendDTO capacityTrend;

    /**
     * 记录数趋势
     */
    private TrendDTO recordCountTrend;

    /**
     * 读取速度趋势
     */
    private TrendDTO readSpeedTrend;

    /**
     * 写入速度趋势
     */
    private TrendDTO writeSpeedTrend;

    /**
     * 趋势数据内部类
     */
    @Data
    public static class TrendDTO {
        /**
         * 趋势值（正数表示增长，负数表示下降）
         */
        private Double value;

        /**
         * 趋势百分比
         */
        private Double percentage;

        public TrendDTO(Double value, Double percentage) {
            this.value = value;
            this.percentage = percentage;
        }
    }
}