package org.kafka.eagle.dto.cluster;

import lombok.Data;

/**
 * <p>
 * 集群查询请求 DTO，用于封装集群列表查询的条件参数。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/8/26 00:09:41
 * @version 5.0.0
 */
@Data
public class ClusterQueryRequest {
    private int page = 1;
    private int pageSize = 10;
    private String search;
    private String clusterType;
    private String auth; // 是 / 否
    private String sortField = "updatedAt";
    private String sortOrder = "DESC"; // 升序 / 降序
}