package org.kafka.eagle.web.service.mcp;

import org.kafka.eagle.dto.ai.FunctionDefinition;

import java.util.Map;

/**
 * MCP 工具元数据：名称、描述、参数 schema、访问级别。
 */
public class McpToolMeta {

    private final String name;
    private final String description;
    private final Map<String, Object> parameters;
    private final McpToolAccess access;

    public McpToolMeta(String name, String description, Map<String, Object> parameters, McpToolAccess access) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.access = access;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public McpToolAccess getAccess() {
        return access;
    }

    public FunctionDefinition toDefinition(String clusterIdHint) {
        String desc = description;
        if (clusterIdHint != null && !clusterIdHint.isEmpty()) {
            desc = description + clusterIdHint;
        }
        return FunctionDefinition.builder()
                .name(name)
                .description(desc)
                .parameters(parameters)
                .build();
    }
}
