package org.kafka.eagle.dto.cluster;

import lombok.Data;
import java.util.Map;

/**
 * <p>
 * Kafka 集群资源指标和统计信息 DTO，用于提供集群的性能监控数据。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/13 18:48:22
 * @version 5.0.0
 */
@Data
public class ClusterMetrics {
    private int totalBrokers;
    private int totalTopics;
    private int totalPartitions;
    private long totalMessages;
    private long totalBytes;
    private double avgMessageRate;
    private double avgByteRate;
    private Map<String, Double> brokerMessageRates;
    private Map<String, Double> brokerByteRates;
    private Map<String, Long> topicMessageCounts;
    private Map<String, Long> topicByteCounts;
    private long underReplicatedPartitions;
    private long offlinePartitions;
    private double networkRequestRate;
    private double networkErrorRate;
}