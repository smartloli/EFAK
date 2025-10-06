package org.kafka.eagle.dto.broker;

import lombok.Data;

/**
 * <p>
 * Broker 统计信息 DTO，用于提供 Broker 集群的统计数据。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/13 10:00:03
 * @version 5.0.0
 */
@Data
public class BrokerStats {
    private Integer totalCount;
    private Integer onlineCount;
    private Integer offlineCount;
    private Double avgCpuUsage;
    private Double avgMemoryUsage;
}