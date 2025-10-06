package org.kafka.eagle.dto.scheduler;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * <p>
 * Topic 容量统计信息类，用于存储 Topic 的容量统计数据。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/8/27 02:05:05
 * @version 5.0.0
 */
@Data
public class TopicCapacityStats {

    /**
     * Topic名称
     */
    private String topicName;

    /**
     * 分区数量
     */
    private Integer partitionCount;

    /**
     * 副本数量
     */
    private Integer replicationFactor;

    /**
     * 总容量（字节）
     */
    private Long totalSize;

    /**
     * 单个分区平均容量（字节）
     */
    private Long avgPartitionSize;

    /**
     * 最大分区容量（字节）
     */
    private Long maxPartitionSize;

    /**
     * 最小分区容量（字节）
     */
    private Long minPartitionSize;

    /**
     * 统计时间
     */
    private LocalDateTime statsTime;

    /**
     * 执行节点ID
     */
    private String nodeId;
}