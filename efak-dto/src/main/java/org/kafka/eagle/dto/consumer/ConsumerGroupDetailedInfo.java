package org.kafka.eagle.dto.consumer;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * Kafka 消费者组详细信息 DTO，包含消费者组的完整信息和消费状态。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/13 09:09:05
 * @version 5.0.0
 */
@Data
public class ConsumerGroupDetailedInfo {

    /**
     * 消费者组ID
     */
    private String groupId;

    /**
     * 消费者组状态
     */
    private String state;

    /**
     * 消费者成员数量
     */
    private int memberCount;

    /**
     * 消费者成员信息
     */
    private List<ConsumerMemberInfo> members;

    /**
     * Topic消费详情
     */
    private List<TopicConsumptionInfo> topicConsumptions;

    /**
     * 总积压数
     */
    private long totalLag;

    /**
     * 总已消费记录数
     */
    private long totalConsumed;

    /**
     * 总记录数
     */
    private long totalRecords;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;

    /**
     * 消费者成员信息内部类
     */
    @Data
    public static class ConsumerMemberInfo {
        /**
         * 成员ID
         */
        private String memberId;

        /**
         * 客户端ID
         */
        private String clientId;

        /**
         * 客户端主机
         */
        private String clientHost;

        /**
         * 分配的分区
         */
        private List<String> assignedPartitions;

        /**
         * 最后心跳时间
         */
        private LocalDateTime lastHeartbeat;
    }

    /**
     * Topic消费信息内部类
     */
    @Data
    public static class TopicConsumptionInfo {
        /**
         * Topic名称
         */
        private String topicName;

        /**
         * 分区消费信息
         */
        private List<PartitionConsumptionInfo> partitionConsumptions;

        /**
         * 总积压数
         */
        private long totalLag;

        /**
         * 已消费记录数
         */
        private long consumedRecords;

        /**
         * 总记录数
         */
        private long totalRecords;
    }

    /**
     * 分区消费信息内部类
     */
    @Data
    public static class PartitionConsumptionInfo {
        /**
         * 分区ID
         */
        private int partitionId;

        /**
         * 当前偏移量
         */
        private long currentOffset;

        /**
         * 已提交偏移量
         */
        private long committedOffset;

        /**
         * 日志结束偏移量
         */
        private long logEndOffset;

        /**
         * 积压数
         */
        private long lag;

        /**
         * 消费者ID
         */
        private String consumerId;

        /**
         * 最后消费时间
         */
        private LocalDateTime lastConsumeTime;
    }
}