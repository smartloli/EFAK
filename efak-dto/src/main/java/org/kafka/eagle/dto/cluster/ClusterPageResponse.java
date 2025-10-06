package org.kafka.eagle.dto.cluster;

import lombok.Data;
import java.util.List;

/**
 * <p>
 * 集群分页响应 DTO，用于返回集群列表的分页查询结果。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/13 10:00:09
 * @version 5.0.0
 */
@Data
public class ClusterPageResponse {
    private List<KafkaClusterInfo> clusters;
    private long total;
    private int page;
    private int pageSize;
}