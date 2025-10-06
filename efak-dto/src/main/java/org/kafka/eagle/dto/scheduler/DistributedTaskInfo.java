package org.kafka.eagle.dto.scheduler;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 分布式任务信息类，用于存储分布式任务的配置和状态信息。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/13 21:11:05
 * @version 5.0.0
 */
@Data
public class DistributedTaskInfo {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 执行频率（秒）
     */
    private Integer executionInterval;

    /**
     * 任务状态：RUNNING, STOPPED, ERROR
     */
    private String status;

    /**
     * 总Topic数量
     */
    private Integer totalTopicCount;

    /**
     * 当前节点分配的Topic数量
     */
    private Integer assignedTopicCount;

    /**
     * 当前节点ID
     */
    private String currentNodeId;

    /**
     * 所有节点信息
     */
    private List<NodeInfo> nodes;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 上次执行时间
     */
    private LocalDateTime lastExecuteTime;

    /**
     * 下次执行时间
     */
    private LocalDateTime nextExecuteTime;

    /**
     * 节点信息内部类
     */
    @Data
    public static class NodeInfo {
        private String nodeId;
        private String nodeIp;
        private Integer port;
        private String status; // 在线，离线
        private LocalDateTime lastHeartbeat;
        private Integer assignedTopicCount;
    }
}