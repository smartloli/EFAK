package org.kafka.eagle.web.service.gateway.function;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.ai.FunctionCall;
import org.kafka.eagle.dto.ai.FunctionResult;
import org.kafka.eagle.web.mapper.TopicInstantMetricsMapper;
import org.kafka.eagle.web.service.TopicService;
import org.kafka.eagle.web.service.gateway.FunctionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP 工具：查询 Topic 读写速度。
 */
@Slf4j
@Component("get_topic_consume_speed")
public class GetTopicConsumeSpeedExecutor implements FunctionExecutor {

    @Autowired
    private TopicService topicService;

    @Autowired
    private TopicInstantMetricsMapper topicInstantMetricsMapper;

    @Override
    public FunctionResult execute(FunctionCall functionCall) {
        try {
            Map<String, Object> params = JSON.parseObject(functionCall.getArguments(), Map.class);
            String clusterId = params.get("cluster_id") == null ? "" : String.valueOf(params.get("cluster_id")).trim();
            String topic = params.get("topic") == null ? "" : String.valueOf(params.get("topic")).trim();
            if (clusterId.isEmpty() || topic.isEmpty()) {
                return FunctionResult.builder().name(getFunctionName()).success(false)
                        .error("cluster_id 和 topic 不能为空").build();
            }

            Map<String, Object> stats = topicService.getTopicDetailedStats(topic, clusterId);
            List<Map<String, Object>> instant = topicInstantMetricsMapper.findByClusterIdAndTopic(clusterId, topic);

            Map<String, Object> payload = new HashMap<>();
            payload.put("topic", topic);
            payload.put("cluster_id", clusterId);
            payload.put("write_speed_bytes", stats.get("writeSpeed"));
            payload.put("read_speed_bytes", stats.get("readSpeed"));
            payload.put("total_records", stats.get("totalRecords"));
            payload.put("total_size", stats.get("totalSize"));
            payload.put("instant_metrics", instant);

            return FunctionResult.builder()
                    .name(getFunctionName())
                    .success(true)
                    .result(JSON.toJSONString(payload))
                    .build();
        } catch (Exception e) {
            log.error("执行 MCP 工具失败: {}", getFunctionName(), e);
            return FunctionResult.builder().name(getFunctionName()).success(false).error(e.getMessage()).build();
        }
    }

    @Override
    public String getFunctionName() {
        return "get_topic_consume_speed";
    }
}
