package org.kafka.eagle.web.service.gateway.function;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.ai.FunctionCall;
import org.kafka.eagle.dto.ai.FunctionResult;
import org.kafka.eagle.web.mapper.ConsumerGroupTopicMapper;
import org.kafka.eagle.web.service.gateway.FunctionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP 工具：查询 Topic 消费者是否积压。
 */
@Slf4j
@Component("get_topic_consumer_lag")
public class GetTopicConsumerLagExecutor implements FunctionExecutor {

    @Autowired
    private ConsumerGroupTopicMapper consumerGroupTopicMapper;

    @Override
    public FunctionResult execute(FunctionCall functionCall) {
        try {
            Map<String, Object> params = JSON.parseObject(functionCall.getArguments(), Map.class);
            String clusterId = params.get("cluster_id") == null ? "" : String.valueOf(params.get("cluster_id")).trim();
            String topic = params.get("topic") == null ? "" : String.valueOf(params.get("topic")).trim();
            String groupId = params.get("group_id") == null ? "" : String.valueOf(params.get("group_id")).trim();
            if (clusterId.isEmpty() || topic.isEmpty()) {
                return FunctionResult.builder().name(getFunctionName()).success(false)
                        .error("cluster_id 和 topic 不能为空").build();
            }

            List<Map<String, Object>> groups;
            if (!groupId.isEmpty()) {
                groups = consumerGroupTopicMapper.findByClusterIdAndGroupIdAndTopic(clusterId, groupId, topic);
            } else {
                groups = consumerGroupTopicMapper.findByClusterIdAndTopic(clusterId, topic);
            }

            long totalLag = 0L;
            int backlogGroups = 0;
            List<Map<String, Object>> details = new ArrayList<>();
            if (groups != null) {
                for (Map<String, Object> group : groups) {
                    long lag = toLong(group.get("lags"));
                    if (lag <= 0) {
                        lag = toLong(group.get("lag"));
                    }
                    totalLag += Math.max(lag, 0);
                    boolean backlog = lag > 0;
                    if (backlog) {
                        backlogGroups++;
                    }
                    Map<String, Object> row = new HashMap<>();
                    row.put("group_id", group.get("group_id"));
                    row.put("topic", group.get("topic_name") != null ? group.get("topic_name") : topic);
                    row.put("lag", lag);
                    row.put("has_backlog", backlog);
                    row.put("state", group.get("state"));
                    row.put("logsize", group.get("logsize"));
                    row.put("offsets", group.get("offsets"));
                    details.add(row);
                }
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("topic", topic);
            payload.put("cluster_id", clusterId);
            payload.put("has_backlog", totalLag > 0);
            payload.put("total_lag", totalLag);
            payload.put("backlog_group_count", backlogGroups);
            payload.put("consumer_count", details.size());
            payload.put("consumers", details);

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
        return "get_topic_consumer_lag";
    }

    private static long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
