package org.kafka.eagle.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 仪表板性能指标表格数据 DTO，用于存储仪表板性能指标表格的数据。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/9/13 11:05:07
 * @version 5.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricsTableData {

    /**
     * 主题名称
     */
    private String topic;

    /**
     * 主题描述
     */
    private String description;

    /**
     * 主要指标值 (格式化字符串)
     */
    private String value;

    /**
     * 原始指标值 (用于计算)
     */
    private Long rawValue;

    /**
     * Broker 倾斜值 (数据倾斜百分比)
     */
    private String brokerSkewed;

    /**
     * Broker 倾斜级别 (normal/warning/error)
     */
    private String skewLevel;

    /**
     * 分区数
     */
    private Integer partitions;

    /**
     * 副本数
     */
    private Integer replicas;

    /**
     * 状态
     */
    private String status;

    /**
     * 排名
     */
    private Integer rank;
}