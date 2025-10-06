package org.kafka.eagle.web.service.gateway.function;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.ai.FunctionCall;
import org.kafka.eagle.dto.ai.FunctionResult;
import org.kafka.eagle.web.mapper.BrokerMetricsMapper;
import org.kafka.eagle.web.service.gateway.FunctionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 获取Broker节点监控指标的Function Executor
 * 根据集群ID查询CPU使用率、内存使用率等监控数据，支持按IP筛选
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/10/06 00:00:00
 * @version 5.0.0
 */
@Slf4j
@Component("get_broker_metrics")
public class GetBrokerMetricsExecutor implements FunctionExecutor {

    @Autowired
    private BrokerMetricsMapper brokerMetricsMapper;

    @Override
    public FunctionResult execute(FunctionCall functionCall) {
        try {
            // 解析参数 - 使用fastjson2
            Map<String, Object> params = JSON.parseObject(
                functionCall.getArguments(),
                Map.class
            );

            String clusterId = (String) params.get("cluster_id");
            String ip = (String) params.get("ip");

            if (clusterId == null || clusterId.trim().isEmpty()) {
                return FunctionResult.builder()
                    .name(getFunctionName())
                    .success(false)
                    .error("cluster_id参数不能为空")
                    .build();
            }

            log.info("执行函数: get_broker_metrics, 集群ID: {}, IP过滤: {}", clusterId, ip);

            // 查询Broker监控指标
            List<Map<String, Object>> metrics;
            if (ip != null && !ip.trim().isEmpty()) {
                metrics = brokerMetricsMapper.findByClusterIdAndIp(clusterId, ip);
            } else {
                metrics = brokerMetricsMapper.findByClusterId(clusterId);
            }

            if (metrics == null || metrics.isEmpty()) {
                String message = ip != null
                    ? String.format("未找到集群 %s 中IP为 %s 的监控数据", clusterId, ip)
                    : String.format("未找到集群 %s 的监控数据", clusterId);
                return FunctionResult.builder()
                    .name(getFunctionName())
                    .success(false)
                    .error(message)
                    .build();
            }

            // 使用fastjson2序列化，自动支持LocalDateTime
            String resultJson = JSON.toJSONString(metrics);

            log.info("查询Broker监控指标成功, 集群ID: {}, 数据条数: {}", clusterId, metrics.size());

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
        return "get_broker_metrics";
    }
}
