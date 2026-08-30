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
import java.util.Map;

/**
 * MCP 工具：查询 Topic 分区信息。
 */
@Slf4j
@Component("get_topic_partitions")
public class GetTopicPartitionsExecutor implements FunctionExecutor {

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

            Map<String, Object> pageParams = new HashMap<>();
            pageParams.put("start", 0);
            pageParams.put("length", 10000);
            Map<String, Object> page = topicService.getTopicPartitionPage(topic, clusterId, pageParams);

            Map<String, Object> payload = new HashMap<>();
            payload.put("topic", topic);
            payload.put("cluster_id", clusterId);
            payload.put("total", page.get("total"));
            payload.put("partitions", page.get("records"));

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
        return "get_topic_partitions";
    }
}
