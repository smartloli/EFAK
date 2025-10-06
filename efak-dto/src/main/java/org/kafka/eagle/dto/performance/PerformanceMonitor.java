package org.kafka.eagle.dto.performance;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * <p>
 * Kafka 性能监控指标信息类，用于存储 Kafka 集群的各种性能监控数据。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/9/24 02:05:07
 * @version 5.0.0
 */
@Data
public class PerformanceMonitor {
    /**
     * 自增ID
     */
    private Long id;

    /**
     * 集群ID
     */
    private String clusterId;

    /**
     * Kafka节点HOST
     */
    private String kafkaHost;

    /**
     * 消息每秒写入记录数
     */
    private BigDecimal messageIn;

    /**
     * 每秒写入字节数
     */
    private BigDecimal byteIn;

    /**
     * 每秒读取字节数
     */
    private BigDecimal byteOut;

    /**
     * 写入耗时(毫秒)
     */
    private BigDecimal timeMsProduce;

    /**
     * 消费耗时(毫秒)
     */
    private BigDecimal timeMsConsumer;

    /**
     * 内存使用率(%)
     */
    private BigDecimal memoryUsage;

    /**
     * CPU使用率(%)
     */
    private BigDecimal cpuUsage;

    /**
     * 采集时间
     */
    private LocalDateTime collectTime;

    /**
     * 日期(格式：2025-09-24)
     */
    private LocalDate collectDate;
}