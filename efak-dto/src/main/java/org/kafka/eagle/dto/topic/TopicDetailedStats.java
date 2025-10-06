package org.kafka.eagle.dto.topic;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Kafka Topic 详细统计信息类，用于存储 Topic 的详细统计数据。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/6/22 01:05:05
 * @version 5.0.0
 */
@Data
public class TopicDetailedStats {

    /**
     * Topic名称
     */
    private String topicName;

    /**
     * 分区数量
     */
    private int partitionCount;

    /**
     * 副本因子
     */
    private short replicationFactor;

    /**
     * 有效时间（毫秒）
     */
    private String retentionMs;

    /**
     * Broker分布详情（百分比）
     */
    private int brokerSpread;

    /**
     * Broker是否倾斜（百分比）
     */
    private int brokerSkewed;

    /**
     * Leader是否倾斜（百分比）
     */
    private int leaderSkewed;

    /**
     * 图标
     */
    private String icon;

    /**
     * Topic配置信息
     */
    private Map<String, String> configs;

    /**
     * 分区详细信息列表
     */
    private List<PartitionDetailedInfo> partitions;

    /**
     * 统计时间
     */
    private LocalDateTime statsTime;

    /**
     * 集群ID
     */
    private String clusterId;

    /**
     * 分区详细信息
     */
    @Data
    public static class PartitionDetailedInfo {
        /**
         * 分区ID
         */
        private int partitionId;

        /**
         * Leader Broker ID
         */
        private int leaderId;

        /**
         * 副本Broker ID列表
         */
        private List<Integer> replicas;

        /**
         * 同步副本Broker ID列表
         */
        private List<Integer> isr;

        /**
         * 起始偏移量
         */
        private long startOffset;

        /**
         * 结束偏移量
         */
        private long endOffset;

        /**
         * 分区状态
         */
        private String status;
    }
}