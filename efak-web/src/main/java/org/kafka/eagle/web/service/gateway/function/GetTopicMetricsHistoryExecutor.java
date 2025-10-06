package org.kafka.eagle.web.service.gateway.function;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.ai.FunctionCall;
import org.kafka.eagle.dto.ai.FunctionResult;
import org.kafka.eagle.web.mapper.TopicMetricsMapper;
import org.kafka.eagle.web.service.gateway.FunctionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 获取Topic历史指标数据的Function Executor
 * 根据集群ID查询Topic的历史容量、消息分布等指标，支持按topic名称和时间范围筛选
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/10/06 00:00:00
 * @version 5.0.0
 */
@Slf4j
@Component("get_topic_metrics_history")
public class GetTopicMetricsHistoryExecutor implements FunctionExecutor {

    @Autowired
    private TopicMetricsMapper topicMetricsMapper;

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
            String startTime = (String) params.get("start_time");
            String endTime = (String) params.get("end_time");

            if (clusterId == null || clusterId.trim().isEmpty()) {
                return FunctionResult.builder()
                    .name(getFunctionName())
                    .success(false)
                    .error("cluster_id参数不能为空")
                    .build();
            }

            log.info("执行函数: get_topic_metrics_history, 集群ID: {}, Topic: {}, 时间范围: {} - {}",
                clusterId, topic, startTime, endTime);

            // 查询Topic历史指标
            List<Map<String, Object>> metrics;
            if (topic != null && !topic.trim().isEmpty() && startTime != null && endTime != null) {
                metrics = topicMetricsMapper.findByClusterIdAndTopicAndTimeRange(clusterId, topic, startTime, endTime);
            } else if (topic != null && !topic.trim().isEmpty()) {
                metrics = topicMetricsMapper.findByClusterIdAndTopic(clusterId, topic);
            } else if (startTime != null && endTime != null) {
                metrics = topicMetricsMapper.findByClusterIdAndTimeRange(clusterId, startTime, endTime);
            } else {
                metrics = topicMetricsMapper.findByClusterId(clusterId);
            }

            if (metrics == null || metrics.isEmpty()) {
                return FunctionResult.builder()
                    .name(getFunctionName())
                    .success(true)
                    .result("[]")
                    .build();
            }

            // 使用fastjson2序列化，自动支持LocalDateTime
            String resultJson = JSON.toJSONString(metrics);

            log.info("查询Topic历史指标成功, 集群ID: {}, 数据条数: {}", clusterId, metrics.size());

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
        return "get_topic_metrics_history";
    }
}
