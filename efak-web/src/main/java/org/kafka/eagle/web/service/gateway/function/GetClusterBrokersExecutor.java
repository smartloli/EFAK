package org.kafka.eagle.web.service.gateway.function;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.ai.FunctionCall;
import org.kafka.eagle.dto.ai.FunctionResult;
import org.kafka.eagle.dto.broker.BrokerInfo;
import org.kafka.eagle.web.mapper.BrokerMapper;
import org.kafka.eagle.web.service.gateway.FunctionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 查询Kafka集群节点信息的Function Executor
 * 根据集群ID查询ke_broker_info表获取Broker节点信息
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/10/04 00:00:00
 * @version 5.0.0
 */
@Slf4j
@Component("get_cluster_brokers")
public class GetClusterBrokersExecutor implements FunctionExecutor {

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

            log.info("执行函数: get_cluster_brokers, 集群ID: {}", clusterId);

            // 查询集群的所有Broker节点
            List<BrokerInfo> brokers = brokerMapper.getBrokersByClusterId(clusterId);

            // 构建返回结果
            List<Map<String, Object>> brokerList = new ArrayList<>();
            for (BrokerInfo broker : brokers) {
                Map<String, Object> brokerData = new HashMap<>();
                brokerData.put("broker_id", broker.getBrokerId());
                brokerData.put("host_ip", broker.getHostIp());
                brokerData.put("port", broker.getPort());
                brokerData.put("jmx_port", broker.getJmxPort());
                brokerData.put("status", broker.getStatus());
                brokerData.put("cpu_usage", broker.getCpuUsage());
                brokerData.put("memory_usage", broker.getMemoryUsage());
                brokerData.put("startup_time", broker.getStartupTime());
                brokerData.put("version", broker.getVersion());
                brokerList.add(brokerData);
            }

            // 统计信息
            Map<String, Object> stats = brokerMapper.getBrokerStatsByClusterId(clusterId);

            Map<String, Object> result = new HashMap<>();
            result.put("cluster_id", clusterId);
            result.put("broker_count", brokerList.size());
            result.put("brokers", brokerList);
            result.put("statistics", stats);

            // 使用fastjson2序列化，自动支持LocalDateTime
            String resultJson = JSON.toJSONString(result);

            log.info("查询集群节点成功, 集群ID: {}, 节点数: {}", clusterId, brokerList.size());

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
        return "get_cluster_brokers";
    }
}
