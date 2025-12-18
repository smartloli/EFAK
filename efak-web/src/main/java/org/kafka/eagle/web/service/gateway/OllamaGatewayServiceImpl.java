package org.kafka.eagle.web.service.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.ai.FunctionCall;
import org.kafka.eagle.dto.ai.FunctionDefinition;
import org.kafka.eagle.dto.ai.FunctionResult;
import org.kafka.eagle.dto.config.ModelConfig;
import org.kafka.eagle.web.service.ModelConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

/**
 * <p>
 * Ollama AI Gateway服务实现类
 * 负责与Ollama API的HTTP通信，处理流式响应，支持Function Calling
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/18 00:12:55
 * @version 5.0.0
 */
@Slf4j
@Service
public class OllamaGatewayServiceImpl implements GatewayService {

    @Autowired
    private ModelConfigService modelConfigService;

    @Autowired(required = false)
    private Map<String, FunctionExecutor> functionExecutors = new HashMap<>();

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
            String systemPrompt = modelConfig.getSystemPrompt();

            if (apiUrl == null || apiUrl.trim().isEmpty()) {
                sendError(emitter, "Ollama API地址未配置");
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
            callAIWithFunctionHandling(apiUrl, modelName, messages, functions, emitter);

        } catch (NumberFormatException e) {
            sendError(emitter, "无效的模型ID: " + modelId);
        } catch (Exception e) {
            log.error("Ollama API调用失败", e);
            sendError(emitter, "Ollama API调用失败: " + e.getMessage());
        }
    }

    /**
     * 调用AI并处理Function Call
     */
    private void callAIWithFunctionHandling(String apiUrl, String modelName,
                                             List<Map<String, Object>> messages,
                                             List<FunctionDefinition> functions,
                                             SseEmitter emitter) throws Exception {
        boolean isSecondCall = (functions == null);
        log.info("【Ollama】开始AI调用 - {}", isSecondCall ? "第二次调用(生成最终答案)" : "第一次调用(可能触发Function Call)");
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

            // 输出Function定义日志用于调试（Ollama格式）
            try {
                log.debug("Ollama Function Calling - Tools定义: {}", objectMapper.writeValueAsString(tools));
            } catch (Exception e) {
                log.warn("无法序列化tools定义", e);
            }
        }

        String requestBody = objectMapper.writeValueAsString(request);

        // 创建HTTP请求
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // 用于收集Function Call信息
        final String[] functionName = {null};
        final StringBuilder functionArgs = new StringBuilder();
        final String[] toolCallId = {null};

        // 用于标识当前调用
        final String callType = isSecondCall ? "第二次" : "第一次";

        // 用于累积思考内容
        StringBuilder thinkingContent = new StringBuilder();
        java.util.concurrent.atomic.AtomicBoolean isInThinking = new java.util.concurrent.atomic.AtomicBoolean(false);

        // 发送请求并处理流式响应
        httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .body()
                .forEach(line -> {
                    try {
                        if (!line.trim().isEmpty()) {
                            log.debug("【Ollama-{}】响应行: {}", callType, line);
                            JsonNode response = objectMapper.readTree(line);

                            // 检查是否结束
                            if (response.has("done") && response.get("done").asBoolean()) {
                                log.info("【Ollama-{}】响应结束, functionName={}, functionArgs={}",
                                    callType, functionName[0], functionArgs.toString());

                                // 检查是否有完整的function call需要执行
                                if (functionName[0] != null && functionArgs.length() > 0) {
                                    log.info("【Ollama-{}】开始执行Function Call: {}", callType, functionName[0]);
                                    // 执行函数并获取结果
                                    executeFunctionAndContinue(apiUrl, modelName, messages,
                                            functions, functionName[0], functionArgs.toString(),
                                            toolCallId[0], emitter);
                                } else {
                                    log.info("【Ollama-{}】没有Function Call，正常结束", callType);
                                    // 没有function call，正常结束
                                    Map<String, Object> endData = Map.of("type", "end");
                                    emitter.send(SseEmitter.event()
                                            .name("message")
                                            .data(objectMapper.writeValueAsString(endData)));
                                    emitter.complete();
                                    log.info("【Ollama-{}】已发送end消息并完成emitter", callType);
                                }
                                return;
                            }

                            // 处理 Ollama chat API 响应格式
                            if (response.has("message")) {
                                JsonNode message = response.get("message");

                                // 收集函数调用信息
                                if (message.has("tool_calls") && !message.get("tool_calls").isNull()) {
                                    log.info("【Ollama-{}】检测到tool_calls", callType);
                                    JsonNode toolCalls = message.get("tool_calls");
                                    if (toolCalls.isArray() && toolCalls.size() > 0) {
                                        JsonNode toolCall = toolCalls.get(0);
                                        log.debug("toolCall内容: {}", toolCall.toString());

                                        // 获取tool_call_id
                                        if (toolCall.has("id")) {
                                            toolCallId[0] = toolCall.get("id").asText();
                                            log.debug("tool_call_id: {}", toolCallId[0]);
                                        }

                                        if (toolCall.has("function")) {
                                            JsonNode function = toolCall.get("function");

                                            // 收集函数名称
                                            if (function.has("name")) {
                                                functionName[0] = function.get("name").asText();
                                                log.info("收集到函数名称: {}", functionName[0]);
                                            }

                                            // 收集函数参数
                                            if (function.has("arguments")) {
                                                // Ollama会多次发送完整的arguments对象，每次内容可能更完整
                                                // 例如: {"cluster_id":"xxx"} -> {"cluster_id":"xxx","topic":"yyy"}
                                                // 策略：每次都替换为最新的完整参数（不追加）
                                                JsonNode argsNode = function.get("arguments");
                                                String argsStr;
                                                if (argsNode.isTextual()) {
                                                    argsStr = argsNode.asText();
                                                } else {
                                                    argsStr = objectMapper.writeValueAsString(argsNode);
                                                }

                                                // 清空并设置为最新参数
                                                functionArgs.setLength(0);
                                                functionArgs.append(argsStr);
                                                log.info("更新函数参数: {}", functionArgs.toString());
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
                                                log.debug("已发送function_call到前端");
                                            }
                                        }
                                    }
                                }

                                // 处理内容
                                if (message.has("content") && !message.get("content").isNull()) {
                                    String content = message.get("content").asText();

                                    if (content != null && !content.isEmpty()) {
                                        // 检查是否包含<think>标签
                                        if (content.contains("<think>")) {
                                            isInThinking.set(true);
                                            // 提取<think>标签内的内容
                                            String thinkContent = extractThinkContent(content);
                                            if (!thinkContent.isEmpty()) {
                                                thinkingContent.append(thinkContent);

                                                // 发送思考内容
                                                Map<String, Object> thinkingData = Map.of(
                                                        "type", "thinking",
                                                        "content", thinkContent);

                                                emitter.send(SseEmitter.event()
                                                        .name("message")
                                                        .data(objectMapper.writeValueAsString(thinkingData)));
                                            }
                                        } else if (content.contains("</think>")) {
                                            isInThinking.set(false);
                                            // 发送思考结束信号
                                            Map<String, Object> thinkingEndData = Map.of("type", "thinking_end");
                                            emitter.send(SseEmitter.event()
                                                    .name("message")
                                                    .data(objectMapper.writeValueAsString(thinkingEndData)));
                                        } else if (isInThinking.get()) {
                                            // 在思考标签内的内容
                                            thinkingContent.append(content);

                                            // 发送思考内容
                                            Map<String, Object> thinkingData = Map.of(
                                                    "type", "thinking",
                                                    "content", content);

                                            emitter.send(SseEmitter.event()
                                                    .name("message")
                                                    .data(objectMapper.writeValueAsString(thinkingData)));
                                        } else {
                                            // 正式回答内容
                                            Map<String, Object> contentData = Map.of(
                                                    "type", "content",
                                                    "content", content);

                                            emitter.send(SseEmitter.event()
                                                    .name("message")
                                                    .data(objectMapper.writeValueAsString(contentData)));
                                            log.debug("【Ollama-{}】已发送content到前端: {}", callType,
                                                content.length() > 50 ? content.substring(0, 50) + "..." : content);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        // 这里可能包含JSON解析异常、客户端断开导致的send异常等，统一兜底避免线程异常退出
                        log.error("处理Ollama流式响应失败", e);
                        sendError(emitter, "处理Ollama流式响应失败: " + e.getMessage());
                    }
                });
    }

    /**
     * 执行函数并继续对话
     */
    private void executeFunctionAndContinue(String apiUrl, String modelName,
                                             List<Map<String, Object>> messages,
                                             List<FunctionDefinition> functions,
                                             String functionName, String functionArgs,
                                             String toolCallId, SseEmitter emitter) {
        try {
            log.info("【Ollama】执行函数: {}, 参数: {}", functionName, functionArgs);

            // 执行函数
            FunctionExecutor executor = functionExecutors.get(functionName);
            if (executor == null) {
                log.error("【Ollama】未找到函数执行器: {}", functionName);
                sendError(emitter, "未找到函数执行器: " + functionName);
                return;
            }

            // 构建FunctionCall对象
            FunctionCall functionCall = FunctionCall.builder()
                    .name(functionName)
                    .arguments(functionArgs)
                    .build();

            // 执行函数
            log.info("【Ollama】开始执行函数...");
            FunctionResult result = executor.execute(functionCall);
            log.info("【Ollama】函数执行完成, success={}, result长度={}",
                result.isSuccess(), result.getResult() != null ? result.getResult().length() : 0);

            // 发送函数执行结果到前端
            Map<String, Object> functionResultData = Map.of(
                    "type", "function_result",
                    "name", functionName,
                    "result", result.getResult() != null ? result.getResult() : "",
                    "success", result.isSuccess());
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(objectMapper.writeValueAsString(functionResultData)));
            log.info("【Ollama】已发送function_result到前端");

            if (!result.isSuccess()) {
                log.error("【Ollama】函数执行失败: {}, 错误: {}", functionName, result.getError());
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

            // Ollama要求arguments是JSON对象，不是字符串
            // 将JSON字符串解析为Map对象
            try {
                Map<String, Object> argsMap = objectMapper.readValue(functionArgs, Map.class);
                function.put("arguments", argsMap);
                log.debug("【Ollama】已将arguments解析为对象: {}", argsMap);
            } catch (Exception e) {
                log.warn("【Ollama】无法解析arguments为对象，使用原始字符串: {}", functionArgs, e);
                function.put("arguments", functionArgs);
            }

            toolCall.put("function", function);
            toolCalls.add(toolCall);

            assistantMessage.put("tool_calls", toolCalls);
            messages.add(assistantMessage);
            log.debug("【Ollama】已添加assistant消息到历史");

            // 将函数结果添加到消息历史
            Map<String, Object> toolMessage = new HashMap<>();
            toolMessage.put("role", "tool");
            toolMessage.put("tool_call_id", toolCallId != null ? toolCallId : "call_" + System.currentTimeMillis());
            toolMessage.put("name", functionName);
            toolMessage.put("content", result.getResult());
            messages.add(toolMessage);
            log.debug("【Ollama】已添加tool消息到历史");

            // 添加用户指令，要求AI基于函数结果进行深入分析并展示思考过程
            // 使用user角色而非system角色，避免与初始system_prompt冲突
            Map<String, Object> analysisInstruction = new HashMap<>();
            analysisInstruction.put("role", "user");
            analysisInstruction.put("content", "请基于上述函数调用返回的数据进行深入分析。要求：\n1. 在<think>标签中展示你的思考和推理过程\n2. 在</think>标签外对数据进行解读和总结\n3. 给出专业的建议或结论");
            messages.add(analysisInstruction);
            log.debug("【Ollama】已添加分析指令到历史");

            // 重新调用AI，让它基于函数结果生成最终答案
            log.info("【Ollama】基于函数结果重新调用Ollama AI生成最终答案");

            // 发送提示信息
            Map<String, Object> infoData = Map.of(
                    "type", "info",
                    "content", "正在基于查询结果生成分析...");
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(objectMapper.writeValueAsString(infoData)));

            log.info("【Ollama】准备第二次调用AI，messages数量: {}", messages.size());
            callAIWithFunctionHandling(apiUrl, modelName, messages, null, emitter);
            log.info("【Ollama】第二次AI调用已发起");

        } catch (Exception e) {
            log.error("【Ollama】执行函数失败", e);
            sendError(emitter, "执行函数失败: " + e.getMessage());
        }
    }

    /**
     * 提取<think>标签内的内容
     */
    private String extractThinkContent(String content) {
        int startIndex = content.indexOf("<think>");
        if (startIndex != -1) {
            int endIndex = content.indexOf("</think>", startIndex);
            if (endIndex != -1) {
                return content.substring(startIndex + 7, endIndex);
            } else {
                // 如果只有开始标签，返回开始标签后的内容
                return content.substring(startIndex + 7);
            }
        }
        return "";
    }

    /**
     * 发送聊天请求
     */
    public Map<String, Object> sendChatRequest(String modelName, Map<String, Object> requestData, String apiUrl) {
        try {
            // 构建请求体 - 使用 messages 格式
            java.util.List<Map<String, Object>> messages = new java.util.ArrayList<>();
            messages.add(Map.of("role", "user", "content", requestData.get("prompt")));

            Map<String, Object> request = Map.of(
                    "model", modelName,
                    "messages", messages,
                    "stream", true);
            String requestBody = objectMapper.writeValueAsString(request);

            // 创建HTTP请求
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // 发送请求
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode responseNode = objectMapper.readTree(response.body());
                // Ollama chat API 响应格式: {"message": {"role": "assistant", "content": "..."}}
                String content = responseNode.get("message").get("content").asText();
                return Map.of(
                        "success", true,
                        "response", content,
                        "model", modelName);
            } else {
                return Map.of(
                        "success", false,
                        "error", "HTTP " + response.statusCode(),
                        "model", modelName);
            }
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "model", modelName);
        }
    }

    /**
     * 测试模型连接
     */
    public boolean testModelConnection(String modelName, String apiUrl) {
        try {
            // 发送一个简单的测试请求
            Map<String, Object> testRequest = Map.of(
                    "model", modelName,
                    "prompt", "test",
                    "stream", false);

            Map<String, Object> result = sendChatRequest(modelName, testRequest, apiUrl);
            return (Boolean) result.get("success");
        } catch (Exception e) {
            return false;
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
        } catch (Exception ignore) {
            // 客户端断开/主动取消时，这里可能抛出异常，直接结束即可
            try {
                emitter.complete();
            } catch (Exception e) {
                // ignore
            }
        }
    }
}
