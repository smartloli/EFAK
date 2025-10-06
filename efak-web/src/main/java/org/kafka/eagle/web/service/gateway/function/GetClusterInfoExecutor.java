package org.kafka.eagle.web.service.gateway.function;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.ai.FunctionCall;
import org.kafka.eagle.dto.ai.FunctionResult;
import org.kafka.eagle.dto.cluster.KafkaClusterInfo;
import org.kafka.eagle.web.mapper.BrokerMapper;
import org.kafka.eagle.web.mapper.ClusterMapper;
import org.kafka.eagle.web.service.gateway.FunctionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 获取Kafka集群信息的Function Executor
 * 根据集群ID查询集群基本信息和统计数据
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/10/04 00:00:00
 * @version 5.0.0
 */
@Slf4j
@Component("get_cluster_info")
public class GetClusterInfoExecutor implements FunctionExecutor {

    @Autowired
    private ClusterMapper clusterMapper;

    @Autowired
    private BrokerMapper brokerMapper;

    @Override
    public FunctionResult execute(FunctionCall functionCall) {
        try {
            // 解析参数 - 使用fastjson2
            Map<String, Object> params = JSON.parseObject(
                functionCall.getArguments(),
                Map.class
            );

            String clusterId = (String) params.get("cluster_id");

            if (clusterId == null || clusterId.trim().isEmpty()) {
                return FunctionResult.builder()
                    .name(getFunctionName())
                    .success(false)
                    .error("cluster_id参数不能为空")
                    .build();
            }

            log.info("执行函数: get_cluster_info, 集群ID: {}", clusterId);

            // 查询集群基本信息
            KafkaClusterInfo clusterInfo = clusterMapper.findByClusterId(clusterId);

            if (clusterInfo == null) {
                return FunctionResult.builder()
                    .name(getFunctionName())
                    .success(false)
                    .error("集群不存在: " + clusterId)
                    .build();
            }

            // 查询Broker统计信息
            Map<String, Object> brokerStats = brokerMapper.getBrokerStatsByClusterId(clusterId);
            Long brokerCount = brokerMapper.countByClusterId(clusterId);
            Long onlineBrokerCount = brokerMapper.countOnlineByClusterId(clusterId);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("cluster_id", clusterInfo.getClusterId());
            result.put("cluster_name", clusterInfo.getName());
            result.put("cluster_type", clusterInfo.getClusterType());
            result.put("auth_enabled", clusterInfo.getAuth());
            result.put("availability", clusterInfo.getAvailability());
            result.put("total_nodes", clusterInfo.getTotalNodes());
            result.put("online_nodes", clusterInfo.getOnlineNodes());
            result.put("broker_count", brokerCount);
            result.put("online_broker_count", onlineBrokerCount);
            result.put("broker_stats", brokerStats);
            result.put("created_at", clusterInfo.getCreatedAt());
            result.put("updated_at", clusterInfo.getUpdatedAt());

            // 计算集群健康状态
            String healthStatus = "unknown";
            if (onlineBrokerCount != null && brokerCount != null && brokerCount > 0) {
                double onlineRate = (double) onlineBrokerCount / brokerCount;
                if (onlineRate >= 0.9) {
                    healthStatus = "healthy";
                } else if (onlineRate >= 0.5) {
                    healthStatus = "degraded";
                } else {
                    healthStatus = "critical";
                }
            }
            result.put("health_status", healthStatus);

            // 使用fastjson2序列化，自动支持LocalDateTime
            String resultJson = JSON.toJSONString(result);

            log.info("查询集群信息成功, 集群ID: {}, 名称: {}, 状态: {}",
                clusterId, clusterInfo.getName(), healthStatus);

            return FunctionResult.builder()
                .name(getFunctionName())
                .result(resultJson)
                .success(true)
                .build();

        } catch (Exception e) {
            log.error("执行函数失败: " + getFunctionName(), e);
            return FunctionResult.builder()
                .name(getFunctionName())
                .success(false)
                .error(e.getMessage())
                .build();
        }
    }

    @Override
    public String getFunctionName() {
        return "get_cluster_info";
    }
}
