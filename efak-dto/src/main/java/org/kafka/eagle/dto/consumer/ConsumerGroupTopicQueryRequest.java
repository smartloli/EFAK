package org.kafka.eagle.dto.consumer;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * <p>
 * 消费者组主题查询请求 DTO，用于查询消费者组主题信息的请求参数。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/9/13 09:05:23
 * @version 5.0.0
 */
@Data
public class ConsumerGroupTopicQueryRequest {

    /**
     * 集群ID
     */
    private String clusterId;

    /**
     * 消费者组ID (可选)
     */
    private String groupId;

    /**
     * 主题名称 (可选)
     */
    private String topicName;

    /**
     * 消费者组状态 (可选)
     */
    private String state;

    /**
     * 收集日期 (可选)
     */
    private LocalDate collectDate;

    /**
     * 开始时间 (可选)
     */
    private LocalDateTime startTime;

    /**
     * 结束时间 (可选)
     */
    private LocalDateTime endTime;

    /**
     * 排序字段
     */
    private String sortField = "collect_time";

    /**
     * 排序方式
     */
    private String sortOrder = "DESC";

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 搜索关键字
     */
    private String search;
}