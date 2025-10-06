package org.kafka.eagle.dto.topic;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * <p>
 * Topic 实时指标数据传输对象类，对应 ke_topic_instant_metrics 表的数据结构。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/9/20 01:25:05
 * @version 5.0.0
 */
@Data
public class TopicInstantMetrics {

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
     * 指标类型: byte_out, capacity, logsize, byte_in
     */
    private String metricType;

    /**
     * 指标数值
     */
    private String metricValue;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdated;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}