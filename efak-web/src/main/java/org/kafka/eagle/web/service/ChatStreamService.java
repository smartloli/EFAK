package org.kafka.eagle.web.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * <p>
 * 聊天流式传输服务接口
 * 定义基于SSE的实时聊天流式处理接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/10 00:17:47
 * @version 5.0.0
 */
public interface ChatStreamService {

    /**
     * 处理聊天流式请求
     *
     * @param modelId 模型ID
     * @param message 用户消息
     * @param clusterId 集群ID（可选）
     * @param emitter SSE发射器
     */
    void processChatStream(String modelId, String message, String clusterId, SseEmitter emitter);

    /**
     * 处理聊天流式请求（支持图表生成）
     *
     * @param modelId 模型ID
     * @param message 用户消息
     * @param clusterId 集群ID（可选）
     * @param enableCharts 是否启用图表生成
     * @param emitter SSE发射器
     */
    void processChatStream(String modelId, String message, String clusterId, boolean enableCharts, SseEmitter emitter);
}