package org.kafka.eagle.web.service.gateway.function;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.ai.FunctionCall;
import org.kafka.eagle.dto.ai.FunctionResult;
import org.kafka.eagle.web.service.TopicService;
import org.kafka.eagle.web.service.gateway.FunctionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP 工具：查询 Topic 最近消息，最多 10000 条，支持分区和关键字过滤。
 */
@Slf4j
@Component("get_topic_messages")
public class GetTopicMessagesExecutor implements FunctionExecutor {

    @Autowired
    private TopicService topicService;

    @Override
    public FunctionResult execute(FunctionCall functionCall) {
        try {
            Map<String, Object> params = JSON.parseObject(functionCall.getArguments(), Map.class);
            String clusterId = asString(params.get("cluster_id"));
            String topic = asString(params.get("topic"));
            Integer partition = asInt(params.get("partition"));
            Integer limit = asInt(params.get("limit"));
            String keyword = asString(params.get("keyword"));

            if (clusterId.isEmpty() || topic.isEmpty()) {
                return fail("cluster_id 和 topic 不能为空");
            }
            if (limit == null || limit <= 0) {
                limit = 100;
            }
            limit = Math.min(limit, 10000);

            log.info("MCP get_topic_messages cluster={}, topic={}, partition={}, limit={}, keyword={}",
                    clusterId, topic, partition, limit, keyword);

            List<Map<String, Object>> messages = topicService.getTopicPartitionMessages(
                    topic, clusterId, partition, limit, keyword);

            Map<String, Object> payload = new HashMap<>();
            payload.put("topic", topic);
            payload.put("cluster_id", clusterId);
            payload.put("count", messages.size());
            payload.put("limit", limit);
            payload.put("keyword", keyword);
            payload.put("messages", messages);

            return FunctionResult.builder()
                    .name(getFunctionName())
                    .success(true)
                    .result(JSON.toJSONString(payload))
                    .build();
        } catch (Exception e) {
            log.error("执行 MCP 工具失败: {}", getFunctionName(), e);
            return fail(e.getMessage());
        }
    }

    @Override
    public String getFunctionName() {
        return "get_topic_messages";
    }

    private FunctionResult fail(String error) {
        return FunctionResult.builder().name(getFunctionName()).success(false).error(error).build();
    }

    private static String asString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static Integer asInt(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
