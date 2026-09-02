package org.kafka.eagle.web.service.mcp;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.ai.FunctionCall;
import org.kafka.eagle.dto.ai.FunctionDefinition;
import org.kafka.eagle.dto.ai.FunctionResult;
import org.kafka.eagle.web.service.gateway.FunctionExecutor;
import org.kafka.eagle.web.util.ToolCallArguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP 工具注册表：把系统能力封装为可被 AI 助手调用的工具，并按角色过滤。
 */
@Slf4j
@Component
public class McpToolRegistry {

    private final Map<String, FunctionExecutor> executors;
    private final Map<String, McpToolMeta> tools = new LinkedHashMap<>();
    private static final ThreadLocal<String> SESSION_CLUSTER_ID = new ThreadLocal<>();

    public static void bindSessionCluster(String clusterId) {
        if (clusterId == null || clusterId.isBlank()) {
            SESSION_CLUSTER_ID.remove();
        } else {
            SESSION_CLUSTER_ID.set(clusterId.trim());
        }
    }

    public static void clearSessionCluster() {
        SESSION_CLUSTER_ID.remove();
    }

    @Autowired
    public McpToolRegistry(Map<String, FunctionExecutor> executors) {
        this.executors = executors;
        registerCatalog();
    }

    public List<FunctionDefinition> definitionsForCurrentUser(String clusterId) {
        String hint = (clusterId != null && !clusterId.isEmpty())
                ? "（当前选中集群: " + clusterId + "，如不指定 cluster_id 则使用此集群）"
                : "";
        boolean admin = McpToolSecurity.isAdmin();
        List<FunctionDefinition> definitions = new ArrayList<>();
        for (McpToolMeta meta : tools.values()) {
            if (admin || meta.getAccess() == McpToolAccess.USER) {
                definitions.add(meta.toDefinition(hint));
            }
        }
        return definitions;
    }

    public List<Map<String, Object>> catalogForCurrentUser() {
        boolean admin = McpToolSecurity.isAdmin();
        List<Map<String, Object>> catalog = new ArrayList<>();
        for (McpToolMeta meta : tools.values()) {
            if (!admin && meta.getAccess() == McpToolAccess.ADMIN) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", meta.getName());
            item.put("description", meta.getDescription());
            item.put("access", meta.getAccess().name());
            item.put("available", executors.containsKey(meta.getName()));
            item.put("parameters", meta.getParameters());
            catalog.add(item);
        }
        return catalog;
    }

    public FunctionResult execute(String functionName, FunctionCall functionCall) {
        McpToolMeta meta = tools.get(functionName);
        if (meta == null || !executors.containsKey(functionName)) {
            return FunctionResult.builder()
                    .name(functionName)
                    .success(false)
                    .error("未找到 MCP 工具: " + functionName)
                    .build();
        }
        if (!McpToolSecurity.canInvoke(meta.getAccess())) {
            log.warn("用户 {} 无权调用 MCP 工具 {}", McpToolSecurity.currentUsername(), functionName);
            return FunctionResult.builder()
                    .name(functionName)
                    .success(false)
                    .error("当前角色无权调用工具: " + functionName)
                    .build();
        }
        FunctionCall normalized = normalizeCall(functionCall);
        return executors.get(functionName).execute(normalized);
    }

    private FunctionCall normalizeCall(FunctionCall functionCall) {
        Map<String, Object> args = ToolCallArguments.parseMap(functionCall.getArguments());
        Object clusterId = args.get("cluster_id");
        String clusterText = ToolCallArguments.stringifyId(clusterId);
        String sessionCluster = SESSION_CLUSTER_ID.get();
        if (clusterText.isEmpty() && sessionCluster != null && !sessionCluster.isBlank()) {
            clusterText = sessionCluster;
        }
        if (!clusterText.isEmpty()) {
            args.put("cluster_id", clusterText);
        }
        return FunctionCall.builder()
                .name(functionCall.getName())
                .arguments(com.alibaba.fastjson2.JSON.toJSONString(args))
                .parsedArguments(args)
                .build();
    }

    private void registerCatalog() {
        register("get_cluster_info",
                "查询 Kafka 集群详情，包括名称、版本、节点数量、健康状态",
                objectSchema(props("cluster_id", "Kafka 集群 ID，可省略，默认当前选中集群"), List.of()),
                McpToolAccess.USER);

        register("get_cluster_brokers",
                "查询集群全部 Broker 节点，包括 ID、主机、端口、状态",
                objectSchema(props("cluster_id", "Kafka 集群 ID，可省略，默认当前选中集群"), List.of()),
                McpToolAccess.USER);

        register("get_topic_info",
                "查询 Topic 分区数、副本数、倾斜度、保留时间等配置，可按 topic 筛选",
                objectSchema(props(
                        "cluster_id", "Kafka 集群 ID",
                        "topic", "Topic 名称（可选）"), List.of("cluster_id")),
                McpToolAccess.USER);

        register("get_topic_partitions",
                "查询指定 Topic 的分区详情，包括 Leader、副本、ISR 和偏移量",
                objectSchema(props(
                        "cluster_id", "Kafka 集群 ID",
                        "topic", "Topic 名称"), List.of("cluster_id", "topic")),
                McpToolAccess.USER);

        Map<String, Object> messageProps = props(
                "cluster_id", "Kafka 集群 ID",
                "topic", "Topic 名称",
                "partition", "分区 ID（可选，不传则查全部分区）",
                "keyword", "按 key/value 过滤的关键字（可选）");
        Map<String, Object> limitProp = new LinkedHashMap<>();
        limitProp.put("type", "integer");
        limitProp.put("description", "返回条数，默认 100，最大 10000");
        messageProps.put("limit", limitProp);
        register("get_topic_messages",
                "查询 Topic 最近消息，最多 10000 条，支持分区和关键字过滤",
                objectSchema(messageProps, List.of("cluster_id", "topic")),
                McpToolAccess.USER);

        register("get_topic_consumers",
                "查询 Topic 对应的消费者组，包括状态、位移和积压",
                objectSchema(props(
                        "cluster_id", "Kafka 集群 ID",
                        "topic", "Topic 名称"), List.of("cluster_id", "topic")),
                McpToolAccess.USER);

        register("get_topic_consume_speed",
                "查询 Topic 的生产/消费速度（byte_in / byte_out）以及容量、消息量",
                objectSchema(props(
                        "cluster_id", "Kafka 集群 ID",
                        "topic", "Topic 名称"), List.of("cluster_id", "topic")),
                McpToolAccess.USER);

        register("get_topic_consumer_lag",
                "查询 Topic 消费者是否有数据积压（lag），可按消费者组筛选",
                objectSchema(props(
                        "cluster_id", "Kafka 集群 ID",
                        "topic", "Topic 名称",
                        "group_id", "消费者组 ID（可选）"), List.of("cluster_id", "topic")),
                McpToolAccess.USER);

        register("get_topic_instant_metrics",
                "查询 Topic 即时指标：容量、log_size、byte_in、byte_out",
                objectSchema(props(
                        "cluster_id", "Kafka 集群 ID",
                        "topic", "Topic 名称（可选）"), List.of("cluster_id")),
                McpToolAccess.USER);

        register("get_topic_metrics_history",
                "查询 Topic 历史指标趋势，支持时间范围",
                objectSchema(props(
                        "cluster_id", "Kafka 集群 ID",
                        "topic", "Topic 名称（可选）",
                        "start_time", "开始时间 yyyy-MM-dd HH:mm:ss（可选）",
                        "end_time", "结束时间 yyyy-MM-dd HH:mm:ss（可选）"), List.of("cluster_id")),
                McpToolAccess.USER);

        register("get_consumer_groups",
                "查询消费者组状态、lag、偏移量，可按 group_id 和 topic 筛选",
                objectSchema(props(
                        "cluster_id", "Kafka 集群 ID",
                        "group_id", "消费者组 ID（可选）",
                        "topic", "Topic 名称（可选）"), List.of("cluster_id")),
                McpToolAccess.USER);

        register("get_alerts",
                "查询集群告警，包括标题、状态、持续时间",
                objectSchema(props("cluster_id", "Kafka 集群 ID，可省略，默认当前选中集群"), List.of()),
                McpToolAccess.USER);

        register("get_broker_metrics",
                "查询 Broker CPU/内存等监控指标，可按 IP 筛选",
                objectSchema(props(
                        "cluster_id", "Kafka 集群 ID",
                        "ip", "Broker IP（可选）"), List.of("cluster_id")),
                McpToolAccess.ADMIN);

        register("get_performance_monitor",
                "查询节点性能：消息/字节流入流出、生产消费耗时",
                objectSchema(props(
                        "cluster_id", "Kafka 集群 ID",
                        "ip", "节点 IP（可选）"), List.of("cluster_id")),
                McpToolAccess.ADMIN);

        register("get_alert_channels",
                "查询告警渠道配置（不含敏感地址）",
                objectSchema(props("cluster_id", "Kafka 集群 ID，可省略，默认当前选中集群"), List.of()),
                McpToolAccess.ADMIN);
    }

    private void register(String name, String description, Map<String, Object> parameters, McpToolAccess access) {
        tools.put(name, new McpToolMeta(name, description, parameters, access));
    }

    private static Map<String, Object> objectSchema(Map<String, Object> properties, List<String> required) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        List<String> filtered = new ArrayList<>();
        if (required != null) {
            for (String name : required) {
                if (!"cluster_id".equals(name)) {
                    filtered.add(name);
                }
            }
        }
        schema.put("required", filtered);
        return schema;
    }

    private static Map<String, Object> props(String... nameAndDesc) {
        Map<String, Object> properties = new LinkedHashMap<>();
        for (int i = 0; i + 1 < nameAndDesc.length; i += 2) {
            Map<String, Object> prop = new LinkedHashMap<>();
            prop.put("type", "string");
            String name = nameAndDesc[i];
            prop.put("description", "cluster_id".equals(name)
                    ? "Kafka 集群 ID，可省略，默认当前选中集群"
                    : nameAndDesc[i + 1]);
            properties.put(name, prop);
        }
        return properties;
    }
}
