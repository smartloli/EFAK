package org.kafka.eagle.dto.consumer;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * <p>
 * 消费者组主题插入请求 DTO，用于插入消费者组主题信息的请求参数。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/9/13 11:05:07
 * @version 5.0.0
 */
@Data
public class ConsumerGroupTopicInsertRequest {

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