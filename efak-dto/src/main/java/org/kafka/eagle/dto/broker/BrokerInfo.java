package org.kafka.eagle.dto.broker;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * Kafka Broker 信息 DTO，用于管理 Broker 的基本配置和状态信息。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/13 10:00:00
 * @version 5.0.0
 */
@Data
public class BrokerInfo {
    private Long id;
    private String clusterId;
    private Integer brokerId;
    private String hostIp;
    private Integer port;
    private Integer jmxPort;
    private String status;
    private BigDecimal cpuUsage;
    private BigDecimal memoryUsage;
    private LocalDateTime startupTime;
    private String version;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 前端兼容性：提供 Kafka 端口（镜像端口）
    private Integer kafkaPort;
}