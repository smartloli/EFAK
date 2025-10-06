package org.kafka.eagle.dto.alert;

import lombok.Data;

/**
 * <p>
 * 告警查询请求 DTO，用于封装告警列表查询的条件参数。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/27 10:00:12
 * @version 5.0.0
 */
@Data
public class AlertQueryRequest {
    private String clusterId;
    private String search;
    private Integer status;
    private String type;
    private String channel;
    private String timeRange;
    private Integer page = 1;
    private Integer pageSize = 5;
    private String sortField = "createdAt";
    private String sortOrder = "desc";
}