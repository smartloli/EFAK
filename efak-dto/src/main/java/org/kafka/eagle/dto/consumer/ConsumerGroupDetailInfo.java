package org.kafka.eagle.dto.consumer;

import lombok.Data;

/**
 * <p>
 * 消费者组详细信息 DTO，用于存储 Kafka 消费者组的详细信息。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/9/13 09:05:23
 * @version 5.0.0
 */
@Data
public class ConsumerGroupDetailInfo implements Cloneable {

    /**
     * 集群ID
     */
    private String clusterId;

    /**
     * 消费者组ID
     */
    private String groupId;

    /**
     * 主题名称
     */
    private String topicName;

    /**
     * 消费者组状态
     */
    private String state;

    /**
     * 协调器
     */
    private String coordinator;

    /**
     * 拥有者
     */
    private String owner;

    /**
     * 状态（运行中/停止）
     */
    private String status;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}