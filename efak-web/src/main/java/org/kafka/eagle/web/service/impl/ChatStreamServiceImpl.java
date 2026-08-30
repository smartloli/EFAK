package org.kafka.eagle.web.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.ai.FunctionDefinition;
import org.kafka.eagle.dto.config.ModelConfig;
import org.kafka.eagle.web.service.ChatStreamService;
import org.kafka.eagle.web.service.ModelConfigService;
import org.kafka.eagle.web.service.gateway.AnthropicGatewayService;
import org.kafka.eagle.web.service.gateway.GatewayService;
import org.kafka.eagle.web.service.gateway.OllamaGatewayService;
import org.kafka.eagle.web.service.gateway.OpenAIGatewayService;
import org.kafka.eagle.web.service.mcp.McpToolRegistry;
import org.kafka.eagle.web.util.ModelApiProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
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
    private OpenAIGatewayService openAIGatewayService;

    @Autowired
    private AnthropicGatewayService anthropicGatewayService;

    @Autowired
    private McpToolRegistry mcpToolRegistry;

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
                sendError(emitter, "不支持的大模型协议: " + apiType);
                return;
            }

            // 按当前用户角色提供 MCP 工具（管理员全部，普通用户仅查询类）
            List<FunctionDefinition> functions = mcpToolRegistry.definitionsForCurrentUser(clusterId);

            // 如果启用图表生成，在消息中添加提示
            String enhancedMessage = message;
            if (enableCharts) {
                enhancedMessage = message + "\n\n【系统提示】用户已开启图表生成功能，如果查询到时序数据，请使用Chart.js格式返回图表配置JSON，并在回答中用```chart```代码块包裹。";
            }

            McpToolRegistry.bindSessionCluster(clusterId);
            try {
                gatewayService.streamChatWithFunctions(modelId, modelName, enhancedMessage, functions, emitter);
            } finally {
                McpToolRegistry.clearSessionCluster();
            }

        } catch (Exception e) {
            sendError(emitter, "处理聊天请求失败: " + e.getMessage());
        }
    }

    private GatewayService getGatewayService(String apiType) {
        switch (ModelApiProtocol.of(apiType)) {
            case ModelApiProtocol.OLLAMA:
                return ollamaGatewayService;
            case ModelApiProtocol.ANTHROPIC:
                return anthropicGatewayService;
            default:
                return openAIGatewayService;
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