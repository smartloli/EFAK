package org.kafka.eagle.web.service;

import org.kafka.eagle.dto.cluster.ClusterPageResponse;
import org.kafka.eagle.dto.cluster.ClusterQueryRequest;
import org.kafka.eagle.dto.cluster.KafkaClusterInfo;
import org.kafka.eagle.dto.cluster.CreateClusterRequest;
import java.util.Map;

/**
 * <p>
 * Cluster集群管理服务接口
 * 定义Kafka集群的管理操作，包括增删改查、统计等功能
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/07 16:54:55
 * @version 5.0.0
 */
public interface ClusterService {

    Map<String, Object> getClusterStats();

    ClusterPageResponse list(ClusterQueryRequest req);

    KafkaClusterInfo getById(Long id);

    KafkaClusterInfo findByClusterId(String clusterId);

    // 创建集群，支持批量节点
    KafkaClusterInfo create(CreateClusterRequest request);

    int update(KafkaClusterInfo info);

    int delete(Long id);

    int deleteByClusterId(String clusterId);

    int updateSummaryByClusterId(String clusterId);
}