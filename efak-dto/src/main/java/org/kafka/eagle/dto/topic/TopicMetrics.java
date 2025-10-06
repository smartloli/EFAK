package org.kafka.eagle.dto.topic;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * Kafka Topic 指标信息类，用于监控 Topic 的各项指标数据。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/27 02:05:05
 * @version 5.0.0
 */
@Data
public class TopicMetrics {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 集群ID
     */
    private String clusterId;
    
    /**
     * Topic名称
     */
    private String topicName;
    
    /**
     * 记录数
     */
    private Long recordCount;
    
    /**
     * 容量（字节）
     */
    private Long capacity;
    
    /**
     * 写入速度（字节/秒）
     */
    private BigDecimal writeSpeed;
    
    /**
     * 读取速度（字节/秒）
     */
    private BigDecimal readSpeed;

    /**
     * 记录数增量
     */
    private Long recordCountDiff;

    /**
     * 容量增量（字节）
     */
    private Long capacityDiff;

    /**
     * 采集时间
     */
    private LocalDateTime collectTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}