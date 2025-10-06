package org.kafka.eagle.dto.topic;

import lombok.Data;

/**
 * <p>
 * Topic 查询请求 DTO 类，用于封装 Topic 查询的请求参数。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/9/15 00:02:05
 * @version 5.0.0
 */
@Data
public class TopicQueryRequest {
    /**
     * 搜索关键词
     */
    private String search;
    
    /**
     * Broker分布状态筛选
     */
    private String brokerSpread;
    
    /**
     * Broker倾斜状态筛选
     */
    private String brokerSkewed;
    
    /**
     * Leader倾斜状态筛选
     */
    private String leaderSkewed;
    
    /**
     * 排序字段
     */
    private String sortField = "createTime";
    
    /**
     * 排序方向
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
     * 集群ID
     */
    private String clusterId;
}