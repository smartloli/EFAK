package org.kafka.eagle.web.controller;

import org.kafka.eagle.dto.ai.FunctionCall;
import org.kafka.eagle.dto.ai.FunctionResult;
import org.kafka.eagle.web.service.mcp.McpToolRegistry;
import org.kafka.eagle.web.service.mcp.McpToolSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * MCP 工具 HTTP 接口：列出当前用户可用工具，并按权限执行。
 */
@RestController
@RequestMapping("/api/mcp")
public class McpController {

    @Autowired
    private McpToolRegistry mcpToolRegistry;

    @GetMapping("/tools")
    public ResponseEntity<Map<String, Object>> listTools() {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("admin", McpToolSecurity.isAdmin());
        body.put("username", McpToolSecurity.currentUsername());
        body.put("tools", mcpToolRegistry.catalogForCurrentUser());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/tools/{name}/call")
    public ResponseEntity<Map<String, Object>> callTool(@PathVariable String name,
                                                        @RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> arguments = request == null ? Map.of() : request;
        if (arguments.containsKey("arguments") && arguments.get("arguments") instanceof Map<?, ?> nested) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cast = (Map<String, Object>) nested;
            arguments = cast;
        }
        FunctionCall call = FunctionCall.builder()
                .name(name)
                .arguments(com.alibaba.fastjson2.JSON.toJSONString(arguments))
                .build();
        FunctionResult result = mcpToolRegistry.execute(name, call);
        Map<String, Object> body = new HashMap<>();
        body.put("success", result.isSuccess());
        body.put("name", result.getName());
        body.put("result", result.getResult());
        body.put("error", result.getError());
        return ResponseEntity.ok(body);
    }
}
