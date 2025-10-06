package org.kafka.eagle.web.service.gateway.function;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.ai.FunctionCall;
import org.kafka.eagle.dto.ai.FunctionResult;
import org.kafka.eagle.web.mapper.TopicInstantMetricsMapper;
import org.kafka.eagle.web.service.gateway.FunctionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 获取Topic即时指标数据的Function Executor
 * 根据集群ID查询Topic的容量、日志大小、字节流入流出等即时指标，支持按topic名称筛选
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/10/06 00:00:00
 * @version 5.0.0
 */
@Slf4j
@Component("get_topic_instant_metrics")
public class GetTopicInstantMetricsExecutor implements FunctionExecutor {

    @Autowired
    private TopicInstantMetricsMapper topicInstantMetricsMapper;

    @Override
    public FunctionResult execute(FunctionCall functionCall) {
        try {
            // 解析参数 - 使用fastjson2
            Map<String, Object> params = JSON.parseObject(
                functionCall.getArguments(),
                Map.class
            );

            String clusterId = (String) params.get("cluster_id");
            String topic = (String) params.get("topic");

            if (clusterId == null || clusterId.trim().isEmpty()) {
                return FunctionResult.builder()
                    .name(getFunctionName())
                    .success(false)
                    .error("cluster_id参数不能为空")
                    .build();
            }

            log.info("执行函数: get_topic_instant_metrics, 集群ID: {}, Topic: {}", clusterId, topic);

            // 查询Topic即时指标
            List<Map<String, Object>> metrics;
            if (topic != null && !topic.trim().isEmpty()) {
                metrics = topicInstantMetricsMapper.findByClusterIdAndTopic(clusterId, topic);
            } else {
                metrics = topicInstantMetricsMapper.findByClusterId(clusterId);
            }

            if (metrics == null || metrics.isEmpty()) {
                String message = topic != null
                    ? String.format("未找到集群 %s 中名为 %s 的Topic即时指标数据", clusterId, topic)
                    : String.format("未找到集群 %s 的Topic即时指标数据", clusterId);
                return FunctionResult.builder()
                    .name(getFunctionName())
                    .success(false)
                    .error(message)
                    .build();
            }

            // 使用fastjson2序列化，自动支持LocalDateTime
            String resultJson = JSON.toJSONString(metrics);

            log.info("查询Topic即时指标成功, 集群ID: {}, Topic数量: {}", clusterId, metrics.size());

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
        return "get_topic_instant_metrics";
    }
}
