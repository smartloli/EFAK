package org.kafka.eagle.dto.cluster;

import lombok.Data;
import java.util.List;

/**
 * <p>
 * Kafka 集群健康状态信息 DTO，用于监控集群的整体健康状况。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/13 18:48:12
 * @version 5.0.0
 */
@Data
public class ClusterHealth {
    private boolean healthy;
    private List<String> underReplicatedPartitions;
    private List<String> offlinePartitions;
    private List<String> unavailableBrokers;
}