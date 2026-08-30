package org.kafka.eagle.web.service.mcp;

/**
 * MCP 工具访问级别。
 * USER：普通角色可调用的查询类工具；ADMIN：管理员可用全部工具。
 */
public enum McpToolAccess {
    USER,
    ADMIN
}
