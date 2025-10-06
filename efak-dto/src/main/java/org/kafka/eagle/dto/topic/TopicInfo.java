package org.kafka.eagle.dto.topic;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * <p>
 * Kafka Topic 详细信息类，用于存储 Topic 的详细配置和状态信息。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/6/22 00:05:05
 * @version 5.0.0
 */
@Data
public class TopicInfo {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * Topic名称
     */
    private String topicName;

    /**
     * 集群ID
     */
    private String clusterId;

    /**
     * 分区数
     */
    private Integer partitions;
    
    /**
     * 副本数
     */
    private Short replicas;
    
    /**
     * Broker分布状态
     */
    private String brokerSpread;
    
    /**
     * Broker倾斜状态
     */
    private String brokerSkewed;
    
    /**
     * Leader倾斜状态
     */
    private String leaderSkewed;
    
    /**
     * 保留时间
     */
    private String retentionTime;

    /**
     * 图标类型（默认database）
     */
    private String icon;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 创建人
     */
    private String createBy;
    
    /**
     * 更新人
     */
    private String updateBy;
    
    // 兼容原有字段
    private Map<String, String> configs;
    private Map<Integer, PartitionInfo> partitionInfo;
}