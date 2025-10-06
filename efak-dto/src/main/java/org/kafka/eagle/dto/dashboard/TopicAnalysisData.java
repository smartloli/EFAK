package org.kafka.eagle.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * <p>
 * 主题状态分析数据传输对象 DTO，用于存储主题分析的各种统计数据。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/9/13 11:05:07
 * @version 5.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicAnalysisData {

    /**
     * 数据类型，固定为 "activity"
     */
    private String type;

    /**
     * 图表标签列表
     */
    private List<String> labels;

    /**
     * 图表数值列表
     */
    private List<Integer> values;

    /**
     * 主题统计信息
     */
    private TopicStats stats;

    /**
     * 容量分布数据
     */
    private CapacityDistribution capacityDistribution;

    /**
     * 主题统计信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicStats {
        /**
         * 主题总数
         */
        private Integer totalTopics;

        /**
         * 活跃主题数
         */
        private Integer activeTopics;

        /**
         * 空闲主题数
         */
        private Integer idleTopics;

        /**
         * 活跃百分比
         */
        private Double activePercentage;

        /**
         * 总容量
         */
        private Long totalCapacity;

        /**
         * 格式化的总容量
         */
        private String totalCapacityFormatted;
    }

    /**
     * 容量分布内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapacityDistribution {
        /**
         * 标签列表
         */
        private List<String> labels;

        /**
         * 数值列表
         */
        private List<Integer> values;

        /**
         * 颜色列表
         */
        private List<String> colors;
    }
}