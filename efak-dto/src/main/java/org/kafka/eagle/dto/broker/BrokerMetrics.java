package org.kafka.eagle.dto.broker;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * Broker 性能指标历史数据 DTO，用于记录 Broker 的性能监控数据。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/8/3 10:00:09
 * @version 5.0.0
 */
@Data
public class BrokerMetrics {
    private Long id;
    private String clusterId;
    private Integer brokerId;
    private String hostIp;
    private Integer port;
    private BigDecimal cpuUsage;
    private BigDecimal memoryUsage;
    private LocalDateTime collectTime;
    private LocalDateTime createTime;
}