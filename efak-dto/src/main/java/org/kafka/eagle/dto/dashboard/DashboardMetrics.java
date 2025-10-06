package org.kafka.eagle.dto.dashboard;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * <p>
 * 仪表板指标 DTO，用于存储仪表板的各种性能指标信息。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/9/13 09:06:23
 * @version 5.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetrics {

    /**
     * 吞吐量指标
     */
    private MetricValue throughput;

    /**
     * 健康状态指标
     */
    private MetricValue health;

    /**
     * 延迟指标
     */
    private MetricValue latency;

    /**
     * 存储指标
     */
    private MetricValue storage;

    /**
     * 指标值内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricValue {
        /**
         * 指标值
         */
        private String value;
        
        /**
         * 变化量
         */
        private String change;
        
        /**
         * 趋势 (positive, negative, neutral)
         */
        private String trend;
    }
}