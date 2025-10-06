package org.kafka.eagle.dto.consumer;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * <p>
 * 消费者组主题延迟收集信息 DTO，用于存储消费者组对特定主题的消费延迟信息。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/9/13 09:05:23
 * @version 5.0.0
 */
@Data
public class ConsumerGroupTopicInfo {

    /**
     * 自增主键ID
     */
    private Long id;

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
     * 日志大小
     */
    private Long logsize;

    /**
     * 偏移量
     */
    private Long offsets;

    /**
     * 延迟数
     */
    private Long lags;

    /**
     * 收集时间
     */
    private LocalDateTime collectTime;

    /**
     * 收集日期
     */
    private LocalDate collectDate;
}