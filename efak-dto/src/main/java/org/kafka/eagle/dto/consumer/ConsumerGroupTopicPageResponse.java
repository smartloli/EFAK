package org.kafka.eagle.dto.consumer;

import lombok.Data;
import java.util.List;

/**
 * <p>
 * 消费者组主题分页响应 DTO，用于返回消费者组主题信息的分页查询结果。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/9/13 11:05:07
 * @version 5.0.0
 */
@Data
public class ConsumerGroupTopicPageResponse {

    /**
     * 数据列表
     */
    private List<ConsumerGroupTopicInfo> data;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 是否有上一页
     */
    private Boolean hasPrev;

    public ConsumerGroupTopicPageResponse() {
    }

    public ConsumerGroupTopicPageResponse(List<ConsumerGroupTopicInfo> data, Long total, Integer page, Integer pageSize) {
        this.data = data;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
        this.hasNext = page < totalPages;
        this.hasPrev = page > 1;
    }
}