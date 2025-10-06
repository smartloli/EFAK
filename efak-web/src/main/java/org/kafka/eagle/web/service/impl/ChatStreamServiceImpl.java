package org.kafka.eagle.web.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.ai.FunctionDefinition;
import org.kafka.eagle.dto.config.ModelConfig;
import org.kafka.eagle.web.service.ChatStreamService;
import org.kafka.eagle.web.service.ModelConfigService;
import org.kafka.eagle.web.service.gateway.DeepSeekGatewayService;
import org.kafka.eagle.web.service.gateway.GatewayService;
import org.kafka.eagle.web.service.gateway.OllamaGatewayService;
import org.kafka.eagle.web.service.gateway.OpenAIGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 聊天流式传输服务实现类
 * 根据模型配置路由到不同的AI Gateway服务，实现统一的流式聊天接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/18 00:12:55
 * @version 5.0.0
 */
@Slf4j
@Service
public class ChatStreamServiceImpl implements ChatStreamService {

    @Autowired
    private ModelConfigService modelConfigService;

    @Autowired
    private OllamaGatewayService ollamaGatewayService;

    @Autowired
    private DeepSeekGatewayService deepSeekGatewayService;

    @Autowired
    private OpenAIGatewayService openAIGatewayService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void processChatStream(String modelId, String message, String clusterId, SseEmitter emitter) {
        processChatStream(modelId, message, clusterId, false, emitter);
    }

    @Override
    public void processChatStream(String modelId, String message, String clusterId, boolean enableCharts, SseEmitter emitter) {
        try {
            // 将String类型的modelId转换为Long类型
            Long modelIdLong;
            try {
                modelIdLong = Long.parseLong(modelId);
            } catch (NumberFormatException e) {
                sendError(emitter, "无效的模型ID: " + modelId);
                return;
            }

            // 根据模型ID获取模型配置
            ModelConfig modelConfig = modelConfigService.getModelConfigById(modelIdLong);
            if (modelConfig == null) {
                sendError(emitter, "模型配置不存在");
                return;
            }

            String apiType = modelConfig.getApiType();
            String modelName = modelConfig.getModelName();

            // 根据API类型选择对应的网关服务
            GatewayService gatewayService = getGatewayService(apiType);
            if (gatewayService == null) {
                sendError(emitter, "不支持的API类型: " + apiType);
                return;
            }

            // 获取可用的Function定义
            List<FunctionDefinition> functions = buildFunctionDefinitions(clusterId);

            // 如果启用图表生成，在消息中添加提示
            String enhancedMessage = message;
            if (enableCharts) {
                enhancedMessage = message + "\n\n【系统提示】用户已开启图表生成功能，如果查询到时序数据，请使用Chart.js格式返回图表配置JSON，并在回答中用```chart```代码块包裹。";
            }

            // 调用对应的网关服务（支持Function Calling）
            gatewayService.streamChatWithFunctions(modelId, modelName, enhancedMessage, functions, emitter);

        } catch (Exception e) {
            sendError(emitter, "处理聊天请求失败: " + e.getMessage());
        }
    }

    /**
     * 构建可用的Function定义列表
     * @param clusterId 集群ID（可选），如果提供则在函数描述中说明
     */
    private List<FunctionDefinition> buildFunctionDefinitions(String clusterId) {
        List<FunctionDefinition> functions = new ArrayList<>();

        // 构建集群ID描述后缀
        String clusterIdHint = (clusterId != null && !clusterId.isEmpty())
            ? "（当前选中集群: " + clusterId + "，如不指定cluster_id参数则使用此集群）"
            : "";

        // 定义 get_cluster_info 函数
        Map<String, Object> clusterInfoParams = new HashMap<>();
        clusterInfoParams.put("type", "object");
        Map<String, Object> clusterInfoProps = new HashMap<>();
        Map<String, Object> clusterIdProp = new HashMap<>();
        clusterIdProp.put("type", "string");
        clusterIdProp.put("description", "Kafka集群ID" + (clusterId != null ? "，默认: " + clusterId : ""));
        clusterInfoProps.put("cluster_id", clusterIdProp);
        clusterInfoParams.put("properties", clusterInfoProps);
        clusterInfoParams.put("required", List.of("cluster_id"));

        functions.add(FunctionDefinition.builder()
                .name("get_cluster_info")
                .description("根据集群ID查询Kafka集群的详细信息，包括集群名称、版本、节点数量、健康状态等" + clusterIdHint)
                .parameters(clusterInfoParams)
                .build());

        // 定义 get_cluster_brokers 函数
        Map<String, Object> brokersParams = new HashMap<>();
        brokersParams.put("type", "object");
        Map<String, Object> brokersProps = new HashMap<>();
        Map<String, Object> brokerClusterIdProp = new HashMap<>();
        brokerClusterIdProp.put("type", "string");
        brokerClusterIdProp.put("description", "Kafka集群ID" + (clusterId != null ? "，默认: " + clusterId : ""));
        brokersProps.put("cluster_id", brokerClusterIdProp);
        brokersParams.put("properties", brokersProps);
        brokersParams.put("required", List.of("cluster_id"));

        functions.add(FunctionDefinition.builder()
                .name("get_cluster_brokers")
                .description("根据集群ID查询Kafka集群的所有Broker节点信息，包括节点ID、主机地址、端口、状态等" + clusterIdHint)
                .parameters(brokersParams)
                .build());

        // 定义 get_alert_channels 函数
        Map<String, Object> alertParams = new HashMap<>();
        alertParams.put("type", "object");
        Map<String, Object> alertProps = new HashMap<>();
        Map<String, Object> alertClusterIdProp = new HashMap<>();
        alertClusterIdProp.put("type", "string");
        alertClusterIdProp.put("description", "Kafka集群ID" + (clusterId != null ? "，默认: " + clusterId : ""));
        alertProps.put("cluster_id", alertClusterIdProp);
        alertParams.put("properties", alertProps);
        alertParams.put("required", List.of("cluster_id"));

        functions.add(FunctionDefinition.builder()
                .name("get_alert_channels")
                .description("根据集群ID查询该集群配置的所有告警渠道信息，包括渠道名称、类型、启用状态等（不包含敏感的API地址）" + clusterIdHint)
                .parameters(alertParams)
                .build());

        // 定义 get_broker_metrics 函数
        Map<String, Object> brokerMetricsParams = new HashMap<>();
        brokerMetricsParams.put("type", "object");
        Map<String, Object> brokerMetricsProps = new HashMap<>();
        Map<String, Object> bmClusterIdProp = new HashMap<>();
        bmClusterIdProp.put("type", "string");
        bmClusterIdProp.put("description", "Kafka集群ID" + (clusterId != null ? "，默认: " + clusterId : ""));
        brokerMetricsProps.put("cluster_id", bmClusterIdProp);
        Map<String, Object> ipProp = new HashMap<>();
        ipProp.put("type", "string");
        ipProp.put("description", "Broker节点IP地址（可选），用于筛选特定节点的监控数据");
        brokerMetricsProps.put("ip", ipProp);
        brokerMetricsParams.put("properties", brokerMetricsProps);
        brokerMetricsParams.put("required", List.of("cluster_id"));

        functions.add(FunctionDefinition.builder()
                .name("get_broker_metrics")
                .description("根据集群ID查询Broker节点的CPU使用率、内存使用率等监控指标，支持按IP地址筛选特定节点" + clusterIdHint)
                .parameters(brokerMetricsParams)
                .build());

        // 定义 get_alerts 函数
        Map<String, Object> alertsParams = new HashMap<>();
        alertsParams.put("type", "object");
        Map<String, Object> alertsProps = new HashMap<>();
        Map<String, Object> alertsClusterIdProp = new HashMap<>();
        alertsClusterIdProp.put("type", "string");
        alertsClusterIdProp.put("description", "Kafka集群ID" + (clusterId != null ? "，默认: " + clusterId : ""));
        alertsProps.put("cluster_id", alertsClusterIdProp);
        alertsParams.put("properties", alertsProps);
        alertsParams.put("required", List.of("cluster_id"));

        functions.add(FunctionDefinition.builder()
                .name("get_alerts")
                .description("根据集群ID查询该集群的所有告警信息，包括告警标题、描述、状态、持续时间等" + clusterIdHint)
                .parameters(alertsParams)
                .build());

        // 定义 get_consumer_groups 函数
        Map<String, Object> consumerGroupsParams = new HashMap<>();
        consumerGroupsParams.put("type", "object");
        Map<String, Object> cgProps = new HashMap<>();
        Map<String, Object> cgClusterIdProp = new HashMap<>();
        cgClusterIdProp.put("type", "string");
        cgClusterIdProp.put("description", "Kafka集群ID" + (clusterId != null ? "，默认: " + clusterId : ""));
        cgProps.put("cluster_id", cgClusterIdProp);
        Map<String, Object> groupIdProp = new HashMap<>();
        groupIdProp.put("type", "string");
        groupIdProp.put("description", "消费者组ID（可选），用于筛选特定消费者组");
        cgProps.put("group_id", groupIdProp);
        Map<String, Object> topicProp = new HashMap<>();
        topicProp.put("type", "string");
        topicProp.put("description", "Topic名称（可选），用于筛选特定主题");
        cgProps.put("topic", topicProp);
        consumerGroupsParams.put("properties", cgProps);
        consumerGroupsParams.put("required", List.of("cluster_id"));

        functions.add(FunctionDefinition.builder()
                .name("get_consumer_groups")
                .description("根据集群ID查询消费者组信息，包括消费者组状态、消费滞后（lag）、偏移量等，支持按group_id和topic筛选" + clusterIdHint)
                .parameters(consumerGroupsParams)
                .build());

        // 定义 get_performance_monitor 函数
        Map<String, Object> perfMonParams = new HashMap<>();
        perfMonParams.put("type", "object");
        Map<String, Object> pmProps = new HashMap<>();
        Map<String, Object> pmClusterIdProp = new HashMap<>();
        pmClusterIdProp.put("type", "string");
        pmClusterIdProp.put("description", "Kafka集群ID" + (clusterId != null ? "，默认: " + clusterId : ""));
        pmProps.put("cluster_id", pmClusterIdProp);
        Map<String, Object> pmIpProp = new HashMap<>();
        pmIpProp.put("type", "string");
        pmIpProp.put("description", "Kafka节点IP地址（可选），用于筛选特定节点的性能数据");
        pmProps.put("ip", pmIpProp);
        perfMonParams.put("properties", pmProps);
        perfMonParams.put("required", List.of("cluster_id"));

        functions.add(FunctionDefinition.builder()
                .name("get_performance_monitor")
                .description("根据集群ID查询Kafka节点的性能监控数据，包括消息流入流出、字节流入流出、生产消费耗时等，支持按IP筛选" + clusterIdHint)
                .parameters(perfMonParams)
                .build());

        // 定义 get_topic_info 函数
        Map<String, Object> topicInfoParams = new HashMap<>();
        topicInfoParams.put("type", "object");
        Map<String, Object> tiProps = new HashMap<>();
        Map<String, Object> tiClusterIdProp = new HashMap<>();
        tiClusterIdProp.put("type", "string");
        tiClusterIdProp.put("description", "Kafka集群ID" + (clusterId != null ? "，默认: " + clusterId : ""));
        tiProps.put("cluster_id", tiClusterIdProp);
        Map<String, Object> tiTopicProp = new HashMap<>();
        tiTopicProp.put("type", "string");
        tiTopicProp.put("description", "Topic名称（可选），用于查询特定主题的详细信息");
        tiProps.put("topic", tiTopicProp);
        topicInfoParams.put("properties", tiProps);
        topicInfoParams.put("required", List.of("cluster_id"));

        functions.add(FunctionDefinition.builder()
                .name("get_topic_info")
                .description("根据集群ID查询Topic的分区数、副本数、数据倾斜度、保留时间等详细配置信息，支持按topic名称筛选" + clusterIdHint)
                .parameters(topicInfoParams)
                .build());

        // 定义 get_topic_instant_metrics 函数
        Map<String, Object> topicInstantParams = new HashMap<>();
        topicInstantParams.put("type", "object");
        Map<String, Object> timProps = new HashMap<>();
        Map<String, Object> timClusterIdProp = new HashMap<>();
        timClusterIdProp.put("type", "string");
        timClusterIdProp.put("description", "Kafka集群ID" + (clusterId != null ? "，默认: " + clusterId : ""));
        timProps.put("cluster_id", timClusterIdProp);
        Map<String, Object> timTopicProp = new HashMap<>();
        timTopicProp.put("type", "string");
        timTopicProp.put("description", "Topic名称（可选），用于查询特定主题的即时指标");
        timProps.put("topic", timTopicProp);
        topicInstantParams.put("properties", timProps);
        topicInstantParams.put("required", List.of("cluster_id"));

        functions.add(FunctionDefinition.builder()
                .name("get_topic_instant_metrics")
                .description("根据集群ID查询Topic的即时指标数据，包括容量（capacity）、日志大小（log_size）、字节流入（byte_in）、字节流出（byte_out）等，支持按topic名称筛选" + clusterIdHint)
                .parameters(topicInstantParams)
                .build());

        // 定义 get_topic_metrics_history 函数
        Map<String, Object> topicHistoryParams = new HashMap<>();
        topicHistoryParams.put("type", "object");
        Map<String, Object> thmProps = new HashMap<>();
        Map<String, Object> thmClusterIdProp = new HashMap<>();
        thmClusterIdProp.put("type", "string");
        thmClusterIdProp.put("description", "Kafka集群ID" + (clusterId != null ? "，默认: " + clusterId : ""));
        thmProps.put("cluster_id", thmClusterIdProp);
        Map<String, Object> thmTopicProp = new HashMap<>();
        thmTopicProp.put("type", "string");
        thmTopicProp.put("description", "Topic名称（可选），用于查询特定主题的历史数据");
        thmProps.put("topic", thmTopicProp);
        Map<String, Object> startTimeProp = new HashMap<>();
        startTimeProp.put("type", "string");
        startTimeProp.put("description", "开始时间（可选），格式: yyyy-MM-dd HH:mm:ss");
        thmProps.put("start_time", startTimeProp);
        Map<String, Object> endTimeProp = new HashMap<>();
        endTimeProp.put("type", "string");
        endTimeProp.put("description", "结束时间（可选），格式: yyyy-MM-dd HH:mm:ss");
        thmProps.put("end_time", endTimeProp);
        topicHistoryParams.put("properties", thmProps);
        topicHistoryParams.put("required", List.of("cluster_id"));

        functions.add(FunctionDefinition.builder()
                .name("get_topic_metrics_history")
                .description("根据集群ID查询Topic的历史指标数据，包括历史容量、消息分布、读写速度等趋势数据，支持按topic名称和时间范围筛选" + clusterIdHint)
                .parameters(topicHistoryParams)
                .build());

        return functions;
    }

    private GatewayService getGatewayService(String apiType) {
        switch (apiType.toLowerCase()) {
            case "ollama":
                return ollamaGatewayService;
            case "deepseek":
                return deepSeekGatewayService;
            case "openai":
                return openAIGatewayService;
            default:
                return null;
        }
    }

    private void sendError(SseEmitter emitter, String message) {
        try {
            Map<String, Object> errorData = Map.of(
                    "type", "error",
                    "message", message);
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(objectMapper.writeValueAsString(errorData)));
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }
}