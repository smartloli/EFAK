package org.kafka.eagle.web.service.gateway;

import org.kafka.eagle.dto.ai.FunctionDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * <p>
 * OpenAI Gateway服务
 * 实现与OpenAI API的对接，提供流式聊天功能
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/07 23:40:14
 * @version 5.0.0
 */
@Service
public class OpenAIGatewayService implements GatewayService {

    @Autowired
    private OpenAIGatewayServiceImpl openAIGatewayServiceImpl;

    @Override
    public void streamChat(String modelId, String modelName, String message, SseEmitter emitter) {
        openAIGatewayServiceImpl.streamChat(modelId, modelName, message, emitter);
    }

    @Override
    public void streamChatWithFunctions(String modelId, String modelName, String message,
                                         List<FunctionDefinition> functions, SseEmitter emitter) {
        openAIGatewayServiceImpl.streamChatWithFunctions(modelId, modelName, message, functions, emitter);
    }
}