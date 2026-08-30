package org.kafka.eagle.web.service.gateway.function;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.ai.FunctionCall;
import org.kafka.eagle.dto.ai.FunctionResult;
import org.kafka.eagle.web.service.TopicService;
import org.kafka.eagle.web.service.gateway.FunctionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MCP 工具：查询 Topic 对应的消费者组信息。
 */
@Slf4j
@Component("get_topic_consumers")
public class GetTopicConsumersExecutor implements FunctionExecutor {

    @Autowired
    private TopicService topicService;

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

            Map<String, Object> result = topicService.getTopicConsumerGroups(topic, clusterId, 1, 200);
            result.put("topic", topic);
            result.put("cluster_id", clusterId);

            return FunctionResult.builder()
                    .name(getFunctionName())
                    .success(true)
                    .result(JSON.toJSONString(result))
                    .build();
        } catch (Exception e) {
            log.error("执行 MCP 工具失败: {}", getFunctionName(), e);
            return FunctionResult.builder().name(getFunctionName()).success(false).error(e.getMessage()).build();
        }
    }

    @Override
    public String getFunctionName() {
        return "get_topic_consumers";
    }
}
