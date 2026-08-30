package org.kafka.eagle.dto.scheduler;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * One collector round. Used to detect missing shards after a node crash or rebalance.
 */
@Data
public class CollectRound {
    private Long id;
    private String roundId;
    private String taskType;
    private String nodeId;
    private String clusterId;
    private Integer assignedCount;
    private Integer successCount;
    private Integer skippedCount;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
