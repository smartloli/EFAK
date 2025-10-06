package org.kafka.eagle.web.service;

import jakarta.servlet.http.HttpServletResponse;
import org.kafka.eagle.dto.config.ModelConfig;

import java.util.Map;

/**
 * <p>
 * AIGateway 服务接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/05 23:57:00
 * @version 5.0.0
 */
public interface AIGatewayService {

    /**
     * 发送聊天请求（支持流式响应）
     */
    void sendChatRequest(String modelName, Map<String, Object> requestData,
                         HttpServletResponse response);

    /**
     * 发送完成请求（支持流式响应）
     */
    void sendCompletionRequest(String modelName, Map<String, Object> requestData,
            HttpServletResponse response);

    /**
     * 测试模型连接
     */
    boolean testModelConnection(String modelName);

    /**
     * 根据模型名称获取模型配置
     */
    ModelConfig getModelConfigByName(String modelName);

    /**
     * 获取支持的模型类型
     */
    String[] getSupportedModelTypes();
}