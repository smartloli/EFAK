package org.kafka.eagle.dto.broker;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * <p>
 * Kafka Broker 详细信息 DTO，包含 Broker 的完整配置和运行状态信息。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/19 18:48
 * @version 5.0.0
 */
@Data
public class BrokerDetailedInfo {

    /**
     * Broker ID
     */
    private int brokerId;

    /**
     * Broker主机地址
     */
    private String host;

    /**
     * Broker端口
     */
    private int port;

    /**
     * JMX端口
     */
    private int jmxPort;

    /**
     * Broker版本号
     */
    private String version;

    /**
     * 启动时间
     */
    private LocalDateTime startTime;

    /**
     * 运行时长（秒）
     */
    private long uptimeSeconds;

    /**
     * 内存使用百分比
     */
    private double memoryUsagePercent;

    /**
     * 已使用内存（字节）
     */
    private long memoryUsed;

    /**
     * 总内存（字节）
     */
    private long memoryTotal;

    /**
     * CPU使用百分比
     */
    private double cpuUsagePercent;

    /**
     * Broker状态
     */
    private String status;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;
}