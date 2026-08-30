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
 * <p>
 * OpenAI Gateway服务实现类
 * 负责与OpenAI API的HTTP通信，处理流式响应，支持Function Calling
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/18 00:12:55
 * @version 5.0.0
 */
@Slf4j
@Service
public class OpenAIGatewayServiceImpl implements GatewayService {

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
            // 从数据库获取模型配置
            Long modelIdLong = Long.parseLong(modelId);
            ModelConfig modelConfig = modelConfigService.getModelConfigById(modelIdLong);

            if (modelConfig == null) {
                sendError(emitter, "模型配置不存在");
                return;
            }

            String apiUrl = modelConfig.getEndpoint();
            String apiKey = modelConfig.getApiKey();
            String systemPrompt = modelConfig.getSystemPrompt();

            if (apiUrl == null || apiUrl.trim().isEmpty()) {
                sendError(emitter, "OpenAI API地址未配置");
                return;
            }

            // 构建消息列表
            List<Map<String, Object>> messages = new ArrayList<>();

            // 如果有系统提示词，添加到消息列表
            if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
                messages.add(Map.of("role", "system", "content", systemPrompt));
            }

            // 添加用户消息
            messages.add(Map.of("role", "user", "content", message));

            // 调用AI并处理可能的Function Call
            callAIWithFunctionHandling(apiUrl, apiKey, modelName, messages, functions, emitter);

        } catch (NumberFormatException e) {
            sendError(emitter, "无效的模型ID: " + modelId);
        } catch (Exception e) {
            log.error("OpenAI API调用失败", e);
            sendError(emitter, "OpenAI API调用失败: " + e.getMessage());
        }
    }

    /**
     * 调用AI并处理Function Call
     */
    private void callAIWithFunctionHandling(String apiUrl, String apiKey, String modelName,
                                             List<Map<String, Object>> messages,
                                             List<FunctionDefinition> functions,
                                             SseEmitter emitter) throws Exception {
        // 构建请求体
        Map<String, Object> request = new HashMap<>();
        request.put("model", modelName);
        request.put("messages", messages);
        request.put("stream", true);

        // 如果提供了函数定义，添加到请求中
        if (functions != null && !functions.isEmpty()) {
            List<Map<String, Object>> tools = new ArrayList<>();
            for (FunctionDefinition func : functions) {
                Map<String, Object> tool = new HashMap<>();
                tool.put("type", "function");
                Map<String, Object> function = new HashMap<>();
                function.put("name", func.getName());
                function.put("description", func.getDescription());
                function.put("parameters", func.getParameters());
                tool.put("function", function);
                tools.add(tool);
            }
            request.put("tools", tools);
            request.put("tool_choice", "auto");

            // 输出Function定义日志用于调试
            log.debug("OpenAI Function Calling - Tools定义: {}", objectMapper.writeValueAsString(tools));
        }

        String requestBody = objectMapper.writeValueAsString(request);

        // 创建HTTP请求
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));
        if (apiKey != null && !apiKey.isBlank()) {
            requestBuilder.header("Authorization", "Bearer " + apiKey);
        }
        HttpRequest httpRequest = requestBuilder.build();

        // 用于收集Function Call信息
        final String[] functionName = {null};
        final StringBuilder functionArgs = new StringBuilder();
        final String[] toolCallId = {null};

        // 发送请求并处理流式响应
        httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .body()
                .forEach(line -> {
                    try {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if (data.equals("[DONE]")) {
                                // 检查是否有完整的function call需要执行
                                if (functionName[0] != null && functionArgs.length() > 0) {
                                    // 执行函数并获取结果
                                    executeFunctionAndContinue(apiUrl, apiKey, modelName, messages,
                                            functions, functionName[0], functionArgs.toString(),
                                            toolCallId[0], emitter);
                                } else {
                                    // 没有function call，正常结束
                                    Map<String, Object> endData = Map.of("type", "end");
                                    emitter.send(SseEmitter.event()
                                            .name("message")
                                            .data(objectMapper.writeValueAsString(endData)));
                                    emitter.complete();
                                }
                                return;
                            }

                            JsonNode response = objectMapper.readTree(data);
                            if (response.has("choices") && response.get("choices").isArray()) {
                                JsonNode choice = response.get("choices").get(0);

                                // 检查finish_reason
                                if (choice.has("finish_reason") && choice.get("finish_reason").asText().equals("tool_calls")) {
                                    log.info("AI请求调用函数: {}", functionName[0]);
                                    return; // 等待[DONE]后执行函数
                                }

                                if (choice.has("delta")) {
                                    JsonNode delta = choice.get("delta");

                                    // 收集函数调用信息
                                    if (delta.has("tool_calls") && !delta.get("tool_calls").isNull()) {
                                        JsonNode toolCalls = delta.get("tool_calls");
                                        if (toolCalls.isArray() && toolCalls.size() > 0) {
                                            JsonNode toolCall = toolCalls.get(0);

                                            // 获取tool_call_id
                                            if (toolCall.has("id")) {
                                                toolCallId[0] = toolCall.get("id").asText();
                                            }

                                            if (toolCall.has("function")) {
                                                JsonNode function = toolCall.get("function");

                                                // 收集函数名称
                                                if (function.has("name")) {
                                                    functionName[0] = function.get("name").asText();
                                                }

                                                if (function.has("arguments")) {
                                                    JsonNode argsNode = function.get("arguments");
                                                    String argsStr = argsNode.isTextual()
                                                            ? argsNode.asText()
                                                            : objectMapper.writeValueAsString(argsNode);
                                                    org.kafka.eagle.web.util.ToolCallArguments.accumulate(functionArgs, argsStr);
                                                }

                                                // 发送函数调用信息到前端
                                                if (functionName[0] != null) {
                                                    Map<String, Object> functionCallData = Map.of(
                                                            "type", "function_call",
                                                            "name", functionName[0],
                                                            "arguments", functionArgs.toString());
                                                    emitter.send(SseEmitter.event()
                                                            .name("message")
                                                            .data(objectMapper.writeValueAsString(functionCallData)));
                                                }
                                            }
                                        }
                                    }

                                    // 处理思考内容 (reasoning_content)
                                    if (delta.has("reasoning_content") && !delta.get("reasoning_content").isNull()) {
                                        String reasoningContent = delta.get("reasoning_content").asText();
                                        if (!reasoningContent.isEmpty()) {
                                            Map<String, Object> thinkingData = Map.of(
                                                    "type", "thinking",
                                                    "content", reasoningContent);
                                            emitter.send(SseEmitter.event()
                                                    .name("message")
                                                    .data(objectMapper.writeValueAsString(thinkingData)));
                                        }
                                    }

                                    // 处理回答内容 (content)
                                    if (delta.has("content") && !delta.get("content").isNull()) {
                                        String content = delta.get("content").asText();
                                        if (!content.isEmpty()) {
                                            Map<String, Object> contentData = Map.of(
                                                    "type", "content",
                                                    "content", content);
                                            emitter.send(SseEmitter.event()
                                                    .name("message")
                                                    .data(objectMapper.writeValueAsString(contentData)));
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        log.error("解析OpenAI响应失败", e);
                        sendError(emitter, "解析OpenAI响应失败: " + e.getMessage());
                    }
                });
    }

    /**
     * 执行函数并继续对话
     */
    private void executeFunctionAndContinue(String apiUrl, String apiKey, String modelName,
                                             List<Map<String, Object>> messages,
                                             List<FunctionDefinition> functions,
                                             String functionName, String functionArgs,
                                             String toolCallId, SseEmitter emitter) {
        try {
            log.info("执行函数: {}, 参数: {}", functionName, functionArgs);

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

            // 发送函数执行结果到前端
            Map<String, Object> functionResultData = Map.of(
                    "type", "function_result",
                    "name", functionName,
                    "result", result.getResult() != null ? result.getResult() : "",
                    "success", result.isSuccess());
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(objectMapper.writeValueAsString(functionResultData)));

            if (!result.isSuccess()) {
                log.error("函数执行失败: {}, 错误: {}", functionName, result.getError());
                sendError(emitter, "函数执行失败: " + result.getError());
                return;
            }

            // 将AI的assistant消息（包含tool_calls）添加到历史
            Map<String, Object> assistantMessage = new HashMap<>();
            assistantMessage.put("role", "assistant");
            assistantMessage.put("content", null);

            List<Map<String, Object>> toolCalls = new ArrayList<>();
            Map<String, Object> toolCall = new HashMap<>();
            toolCall.put("id", toolCallId != null ? toolCallId : "call_" + System.currentTimeMillis());
            toolCall.put("type", "function");
            Map<String, Object> function = new HashMap<>();
            function.put("name", functionName);
            function.put("arguments", functionArgs);
            toolCall.put("function", function);
            toolCalls.add(toolCall);

            assistantMessage.put("tool_calls", toolCalls);
            messages.add(assistantMessage);

            // 将函数结果添加到消息历史
            Map<String, Object> toolMessage = new HashMap<>();
            toolMessage.put("role", "tool");
            toolMessage.put("tool_call_id", toolCallId != null ? toolCallId : "call_" + System.currentTimeMillis());
            toolMessage.put("name", functionName);
            toolMessage.put("content", result.getResult());
            messages.add(toolMessage);

            // 添加用户指令，要求AI基于函数结果进行深入分析并展示思考过程
            // 使用user角色而非system角色，避免与初始system_prompt冲突
            Map<String, Object> analysisInstruction = new HashMap<>();
            analysisInstruction.put("role", "user");
            analysisInstruction.put("content", "请基于上述函数调用返回的数据进行深入分析。要求：\n1. 展示你的思考和推理过程\n2. 对数据进行解读和总结\n3. 给出专业的建议或结论");
            messages.add(analysisInstruction);
            log.debug("【OpenAI】已添加分析指令到历史");

            // 重新调用AI，让它基于函数结果生成最终答案
            log.info("基于函数结果重新调用AI生成最终答案");

            // 发送提示信息
            Map<String, Object> infoData = Map.of(
                    "type", "info",
                    "content", "正在基于查询结果生成分析...");
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(objectMapper.writeValueAsString(infoData)));

            callAIWithFunctionHandling(apiUrl, apiKey, modelName, messages, null, emitter);

        } catch (Exception e) {
            log.error("执行函数失败", e);
            sendError(emitter, "执行函数失败: " + e.getMessage());
        }
    }

    private FunctionResult executeLegacy(String functionName, FunctionCall functionCall) {
        FunctionExecutor executor = functionExecutors.get(functionName);
        if (executor == null) {
            return null;
        }
        return executor.execute(functionCall);
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
