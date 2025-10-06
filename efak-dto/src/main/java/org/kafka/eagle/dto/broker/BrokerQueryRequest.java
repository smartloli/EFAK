package org.kafka.eagle.dto.broker;

import lombok.Data;

/**
 * <p>
 * Broker 查询请求 DTO，用于封装 Broker 列表查询的条件参数。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/13 10:00:02
 * @version 5.0.0
 */
@Data
public class BrokerQueryRequest {
    private String search;
    private String status;
    // 新增：按集群过滤
    private String clusterId;
    private String sortField = "createdAt";
    private String sortOrder = "DESC";
    private Integer page = 1;
    private Integer pageSize = 10;
}