package org.kafka.eagle.web.service.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.ai.FunctionCall;
import org.kafka.eagle.dto.ai.FunctionDefinition;
import org.kafka.eagle.dto.ai.FunctionResult;
import org.kafka.eagle.dto.config.ModelConfig;
import org.kafka.eagle.web.service.ModelConfigService;
import org.kafka.eagle.web.service.mcp.McpToolRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Native Anthropic Messages API client with SSE streaming and tool use.
 */
@Slf4j
@Service
public class AnthropicGatewayServiceImpl implements GatewayService {

    private static final String ANTHROPIC_VERSION = "2023-06-01";

    @Autowired
    private ModelConfigService modelConfigService;

    @Autowired(required = false)
    private Map<String, FunctionExecutor> functionExecutors = new HashMap<>();

    @Autowired
    private McpToolRegistry mcpToolRegistry;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public void streamChat(String modelId, String modelName, String message, SseEmitter emitter) {
        streamChatWithFunctions(modelId, modelName, message, null, emitter);
    }

    @Override
    public void streamChatWithFunctions(String modelId, String modelName, String message,
                                        List<FunctionDefinition> functions, SseEmitter emitter) {
        try {
            Long modelIdLong = Long.parseLong(modelId);
            ModelConfig modelConfig = modelConfigService.getModelConfigById(modelIdLong);
            if (modelConfig == null) {
                sendError(emitter, "模型配置不存在");
                return;
            }

            String apiUrl = modelConfig.getEndpoint();
            String apiKey = modelConfig.getApiKey();
            String systemPrompt = modelConfig.getSystemPrompt();
            if (apiUrl == null || apiUrl.isBlank()) {
                sendError(emitter, "Anthropic API地址未配置");
                return;
            }
            if (!org.kafka.eagle.web.util.ModelApiProtocol.isCustom(modelConfig.getApiType())
                    && (apiKey == null || apiKey.isBlank())) {
                sendError(emitter, "Anthropic API密钥未配置");
                return;
            }

            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(Map.of("role", "user", "content", message));
            callAnthropic(apiUrl, apiKey, modelName, systemPrompt, messages, functions, emitter);
        } catch (NumberFormatException e) {
            sendError(emitter, "无效的模型ID: " + modelId);
        } catch (Exception e) {
            log.error("Anthropic API调用失败", e);
            sendError(emitter, "Anthropic API调用失败: " + e.getMessage());
        }
    }

    private void callAnthropic(String apiUrl, String apiKey, String modelName, String systemPrompt,
                               List<Map<String, Object>> messages, List<FunctionDefinition> functions,
                               SseEmitter emitter) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("model", modelName);
        request.put("max_tokens", 4096);
        request.put("stream", true);
        request.put("messages", messages);
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            request.put("system", systemPrompt);
        }
        if (functions != null && !functions.isEmpty()) {
            List<Map<String, Object>> tools = new ArrayList<>();
            for (FunctionDefinition func : functions) {
                Map<String, Object> tool = new HashMap<>();
                tool.put("name", func.getName());
                tool.put("description", func.getDescription());
                tool.put("input_schema", func.getParameters() != null ? func.getParameters() : Map.of("type", "object"));
                tools.add(tool);
            }
            request.put("tools", tools);
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("anthropic-version", ANTHROPIC_VERSION)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)));
        if (apiKey != null && !apiKey.isBlank()) {
            requestBuilder.header("x-api-key", apiKey);
        }
        HttpRequest httpRequest = requestBuilder.build();

        final String[] eventName = {""};
        final String[] toolId = {null};
        final String[] toolName = {null};
        final StringBuilder toolArgs = new StringBuilder();
        final List<Map<String, Object>> assistantBlocks = new ArrayList<>();
        final StringBuilder textBuffer = new StringBuilder();

        httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .body()
                .forEach(line -> {
                    try {
                        if (line.startsWith("event:")) {
                            eventName[0] = line.substring(6).trim();
                            return;
                        }
                        if (!line.startsWith("data:")) {
                            return;
                        }
                        String data = line.substring(5).trim();
                        if (data.isEmpty()) {
                            return;
                        }
                        JsonNode node = objectMapper.readTree(data);
                        String type = node.has("type") ? node.get("type").asText() : eventName[0];

                        if ("content_block_start".equals(type) && node.has("content_block")) {
                            JsonNode block = node.get("content_block");
                            if ("tool_use".equals(block.path("type").asText())) {
                                toolId[0] = block.path("id").asText(null);
                                toolName[0] = block.path("name").asText(null);
                                toolArgs.setLength(0);
                            }
                            return;
                        }

                        if ("content_block_delta".equals(type) && node.has("delta")) {
                            JsonNode delta = node.get("delta");
                            String deltaType = delta.path("type").asText();
                            if ("text_delta".equals(deltaType)) {
                                String text = delta.path("text").asText("");
                                if (!text.isEmpty()) {
                                    textBuffer.append(text);
                                    emit(emitter, Map.of("type", "content", "content", text));
                                }
                            } else if ("input_json_delta".equals(deltaType)) {
                                toolArgs.append(delta.path("partial_json").asText(""));
                                if (toolName[0] != null) {
                                    emit(emitter, Map.of(
                                            "type", "function_call",
                                            "name", toolName[0],
                                            "arguments", toolArgs.toString()));
                                }
                            }
                            return;
                        }

                        if ("content_block_stop".equals(type)) {
                            if (toolName[0] != null) {
                                Map<String, Object> toolBlock = new HashMap<>();
                                toolBlock.put("type", "tool_use");
                                toolBlock.put("id", toolId[0]);
                                toolBlock.put("name", toolName[0]);
                                try {
                                    toolBlock.put("input", objectMapper.readValue(toolArgs.toString(),
                                            objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)));
                                } catch (Exception e) {
                                    toolBlock.put("input", Map.of());
                                }
                                assistantBlocks.add(toolBlock);
                            } else if (textBuffer.length() > 0) {
                                assistantBlocks.add(Map.of("type", "text", "text", textBuffer.toString()));
                                textBuffer.setLength(0);
                            }
                            return;
                        }

                        if ("message_delta".equals(type)) {
                            String stopReason = node.path("delta").path("stop_reason").asText("");
                            if ("tool_use".equals(stopReason) && toolName[0] != null) {
                                executeToolAndContinue(apiUrl, apiKey, modelName, systemPrompt, messages,
                                        functions, toolId[0], toolName[0], toolArgs.toString(),
                                        assistantBlocks, emitter);
                            }
                            return;
                        }

                        if ("message_stop".equals(type)) {
                            if (toolName[0] == null) {
                                emit(emitter, Map.of("type", "end"));
                                emitter.complete();
                            }
                        }
                    } catch (IOException e) {
                        log.error("解析Anthropic响应失败", e);
                        sendError(emitter, "解析Anthropic响应失败: " + e.getMessage());
                    }
                });
    }

    private void executeToolAndContinue(String apiUrl, String apiKey, String modelName, String systemPrompt,
                                        List<Map<String, Object>> messages, List<FunctionDefinition> functions,
                                        String toolId, String functionName, String functionArgs,
                                        List<Map<String, Object>> assistantBlocks, SseEmitter emitter) {
        try {
            FunctionCall functionCall = FunctionCall.builder()
                    .name(functionName)
                    .arguments(functionArgs)
                    .build();
            FunctionResult result = mcpToolRegistry != null
                    ? mcpToolRegistry.execute(functionName, functionCall)
                    : executeLegacy(functionName, functionCall);
            if (result == null) {
                sendError(emitter, "未找到函数执行器: " + functionName);
                return;
            }
            emit(emitter, Map.of(
                    "type", "function_result",
                    "name", functionName,
                    "result", result.getResult() != null ? result.getResult() : "",
                    "success", result.isSuccess()));
            if (!result.isSuccess()) {
                sendError(emitter, "函数执行失败: " + result.getError());
                return;
            }

            messages.add(Map.of("role", "assistant", "content", assistantBlocks));
            Map<String, Object> toolResult = new HashMap<>();
            toolResult.put("type", "tool_result");
            toolResult.put("tool_use_id", toolId != null ? toolId : "toolu_" + System.currentTimeMillis());
            toolResult.put("content", result.getResult());
            messages.add(Map.of("role", "user", "content", List.of(toolResult)));
            messages.add(Map.of("role", "user",
                    "content", "请基于上述函数调用返回的数据进行深入分析。要求：\n1. 展示你的思考和推理过程\n2. 对数据进行解读和总结\n3. 给出专业的建议或结论"));

            emit(emitter, Map.of("type", "info", "content", "正在基于查询结果生成分析..."));
            callAnthropic(apiUrl, apiKey, modelName, systemPrompt, messages, null, emitter);
        } catch (Exception e) {
            log.error("执行Anthropic工具失败", e);
            sendError(emitter, "执行函数失败: " + e.getMessage());
        }
    }

    private FunctionResult executeLegacy(String functionName, FunctionCall functionCall) {
        FunctionExecutor executor = functionExecutors.get(functionName);
        return executor == null ? null : executor.execute(functionCall);
    }

    private void emit(SseEmitter emitter, Map<String, Object> payload) throws IOException {
        emitter.send(SseEmitter.event()
                .name("message")
                .data(objectMapper.writeValueAsString(payload)));
    }

    private void sendError(SseEmitter emitter, String message) {
        try {
            emit(emitter, Map.of("type", "error", "message", message));
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }
}
