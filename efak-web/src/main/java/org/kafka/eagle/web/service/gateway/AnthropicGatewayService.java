package org.kafka.eagle.web.service.gateway;

import org.kafka.eagle.dto.ai.FunctionDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * Anthropic Messages API gateway (native protocol, not OpenAI-compatible).
 */
@Service
public class AnthropicGatewayService implements GatewayService {

    @Autowired
    private AnthropicGatewayServiceImpl anthropicGatewayServiceImpl;

    @Override
    public void streamChat(String modelId, String modelName, String message, SseEmitter emitter) {
        anthropicGatewayServiceImpl.streamChat(modelId, modelName, message, emitter);
    }

    @Override
    public void streamChatWithFunctions(String modelId, String modelName, String message,
                                        List<FunctionDefinition> functions, SseEmitter emitter) {
        anthropicGatewayServiceImpl.streamChatWithFunctions(modelId, modelName, message, functions, emitter);
    }
}
