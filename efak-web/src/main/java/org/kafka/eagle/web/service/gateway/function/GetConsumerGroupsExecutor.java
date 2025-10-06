package org.kafka.eagle.web.service.gateway.function;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.ai.FunctionCall;
import org.kafka.eagle.dto.ai.FunctionResult;
import org.kafka.eagle.web.mapper.ConsumerGroupTopicMapper;
import org.kafka.eagle.web.service.gateway.FunctionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 获取消费者组信息的Function Executor
 * 根据集群ID查询消费者组信息，支持按group_id和topic筛选
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/10/06 00:00:00
 * @version 5.0.0
 */
@Slf4j
@Component("get_consumer_groups")
public class GetConsumerGroupsExecutor implements FunctionExecutor {

    @Autowired
    private ConsumerGroupTopicMapper consumerGroupTopicMapper;

    @Override
    public FunctionResult execute(FunctionCall functionCall) {
        try {
            // 解析参数 - 使用fastjson2
            Map<String, Object> params = JSON.parseObject(
                functionCall.getArguments(),
                Map.class
            );

            String clusterId = (String) params.get("cluster_id");
            String groupId = (String) params.get("group_id");
            String topic = (String) params.get("topic");

            if (clusterId == null || clusterId.trim().isEmpty()) {
                return FunctionResult.builder()
                    .name(getFunctionName())
                    .success(false)
                    .error("cluster_id参数不能为空")
                    .build();
            }

            log.info("执行函数: get_consumer_groups, 集群ID: {}, GroupID: {}, Topic: {}",
                clusterId, groupId, topic);

            // 查询消费者组信息
            List<Map<String, Object>> consumerGroups;
            if (groupId != null && !groupId.trim().isEmpty() && topic != null && !topic.trim().isEmpty()) {
                consumerGroups = consumerGroupTopicMapper.findByClusterIdAndGroupIdAndTopic(clusterId, groupId, topic);
            } else if (groupId != null && !groupId.trim().isEmpty()) {
                consumerGroups = consumerGroupTopicMapper.findByClusterIdAndGroupId(clusterId, groupId);
            } else if (topic != null && !topic.trim().isEmpty()) {
                consumerGroups = consumerGroupTopicMapper.findByClusterIdAndTopic(clusterId, topic);
            } else {
                consumerGroups = consumerGroupTopicMapper.findByClusterId(clusterId);
            }

            if (consumerGroups == null || consumerGroups.isEmpty()) {
                return FunctionResult.builder()
                    .name(getFunctionName())
                    .success(true)
                    .result("[]")
                    .build();
            }

            // 使用fastjson2序列化，自动支持LocalDateTime
            String resultJson = JSON.toJSONString(consumerGroups);

            log.info("查询消费者组信息成功, 集群ID: {}, 数据条数: {}", clusterId, consumerGroups.size());

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
        return "get_consumer_groups";
    }
}
