package org.kafka.eagle.web.service.gateway;

import org.kafka.eagle.dto.ai.FunctionDefinition;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * <p>
 * AI Gateway服务接口
 * 定义AI模型网关的通用接口，支持多种AI服务提供商
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/07 23:34:09
 * @version 5.0.0
 */
public interface GatewayService {

    /**
     * 流式聊天
     *
     * @param modelId   模型ID
     * @param modelName 模型名称
     * @param message   用户消息
     * @param emitter   SSE发射器
     */
    void streamChat(String modelId, String modelName, String message, SseEmitter emitter);

    /**
     * 流式聊天（支持Function Calling）
     *
     * @param modelId   模型ID
     * @param modelName 模型名称
     * @param message   用户消息
     * @param functions 可用的函数定义列表
     * @param emitter   SSE发射器
     */
    void streamChatWithFunctions(String modelId, String modelName, String message,
                                  List<FunctionDefinition> functions, SseEmitter emitter);
}