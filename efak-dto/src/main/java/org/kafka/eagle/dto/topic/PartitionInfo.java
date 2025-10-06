package org.kafka.eagle.dto.topic;

import lombok.Data;
import java.util.List;

/**
 * <p>
 * Kafka Topic 分区信息类，用于存储 Topic 分区的详细信息。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/6/22 02:05:05
 * @version 5.0.0
 */
@Data
public class PartitionInfo {
    /**
     * 分区号
     */
    private int partition;
    
    /**
     * Leader 节点
     */
    private int leader;
    
    /**
     * 副本节点列表
     */
    private List<Integer> replicas;
    
    /**
     * 同步副本节点列表
     */
    private List<Integer> inSyncReplicas;
    
    /**
     * 日志大小
     */
    private long logSize;
    
    /**
     * 日志起始偏移量
     */
    private long logStartOffset;
    
    /**
     * 日志结束偏移量
     */
    private long logEndOffset;
    
    /**
     * 消息数量
     */
    private long messageCount;
}