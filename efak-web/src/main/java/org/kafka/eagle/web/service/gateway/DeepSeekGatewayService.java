package org.kafka.eagle.web.service.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kafka.eagle.dto.ai.FunctionDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * DeepSeek AI Gateway服务
 * 实现与DeepSeek API的对接，提供流式聊天功能
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/18 00:12:55
 * @version 5.0.0
 */
@Service
public class DeepSeekGatewayService implements GatewayService {

    @Autowired
    private DeepSeekGatewayServiceImpl deepSeekGatewayServiceImpl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void streamChat(String modelId, String modelName, String message, SseEmitter emitter) {
        try {
            // 调用DeepSeek服务
            deepSeekGatewayServiceImpl.streamChat(modelId, modelName,message, emitter);

        } catch (Exception e) {
            sendError(emitter, "DeepSeek服务调用失败: " + e.getMessage());
        }
    }

    @Override
    public void streamChatWithFunctions(String modelId, String modelName, String message,
                                         List<FunctionDefinition> functions, SseEmitter emitter) {
        try {
            // 调用DeepSeek服务（支持Function Calling）
            deepSeekGatewayServiceImpl.streamChatWithFunctions(modelId, modelName, message, functions, emitter);
        } catch (Exception e) {
            sendError(emitter, "DeepSeek服务调用失败: " + e.getMessage());
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