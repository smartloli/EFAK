package org.kafka.eagle.dto.dashboard;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * <p>
 * 仪表板统计信息 DTO，用于存储仪表板的各种统计数据。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/9/13 09:05:23
 * @version 5.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {

    /**
     * 在线节点数 (基于 ke_broker_info 表的 cluster_id 和 NetUtils.telnet 检测)
     */
    private Integer onlineNodes;

    /**
     * 主题总数 (基于 ke_topic_info 表的 cluster_id)
     */
    private Integer totalTopics;

    /**
     * 分区总数 (基于 ke_topic_info 表的 cluster_id 的分区数总和)
     */
    private Integer totalPartitions;

    /**
     * 消费者组数 (基于 ke_consumer_group_topic 表的 cluster_id 和今日日期的不重复 group_id)
     */
    private Integer consumerGroups;

    /**
     * 集群ID
     */
    private String clusterId;
}