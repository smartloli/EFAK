package org.kafka.eagle.dto.topic;

import lombok.Data;
import java.util.List;

/**
 * <p>
 * Topic 分页响应 DTO 类，用于封装 Topic 分页查询的响应数据。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/15 23:15:02
 * @version 5.0.0
 */
@Data
public class TopicPageResponse {
    /**
     * 数据列表
     */
    private List<TopicInfo> data;
    
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
    
    public TopicPageResponse() {}
    
    public TopicPageResponse(List<TopicInfo> data, Long total, Integer page, Integer pageSize) {
        this.data = data;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
        this.hasNext = page < totalPages;
        this.hasPrev = page > 1;
    }
}