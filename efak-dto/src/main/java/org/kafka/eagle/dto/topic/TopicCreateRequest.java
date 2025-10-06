package org.kafka.eagle.dto.topic;

import lombok.Data;

/**
 * <p>
 * Topic 创建请求 DTO 类，用于封装创建 Topic 时的请求参数。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/15 01:09:05
 * @version 5.0.0
 */
@Data
public class TopicCreateRequest {
    /**
     * Topic名称
     */
    private String topicName;

    /**
     * 分区数
     */
    private Integer partitions;

    /**
     * 副本数
     */
    private Short replicas;

    /**
     * 保留时间
     */
    private String retentionTime;

    /**
     * 图标类型（可选，默认database）
     */
    private String icon;

    /**
     * 集群ID
     */
    private String clusterId;

    /**
     * 创建人
     */
    private String createBy;
}