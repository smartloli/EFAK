package org.kafka.eagle.web.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.kafka.eagle.web.service.AIGatewayService;
import org.kafka.eagle.web.service.ModelConfigService;
import org.kafka.eagle.dto.config.ModelConfig;
import com.alibaba.fastjson2.JSON;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * <p>
 * AIGateway 服务实现类
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/06 00:00:58
 * @version 5.0.0
 */
@Slf4j
@Service
public class AIGatewayServiceImpl implements AIGatewayService {

    @Autowired
    private ModelConfigService modelConfigService;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void sendChatRequest(String modelName, Map<String, Object> requestData, HttpServletResponse response) {
        try {
            // 获取模型配置
            ModelConfig modelConfig = getModelConfigByName(modelName);
            if (modelConfig == null) {
                sendErrorResponse(response, "模型配置不存在: " + modelName);
                return;
            }

            // 检查模型是否启用
            if (modelConfig.getEnabled() != 1) {
                sendErrorResponse(response, "模型未启用: " + modelName);
                return;
            }

            // 设置SSE响应头
            setupSSEResponse(response);

            // 根据API类型选择不同的处理方式
            switch (modelConfig.getApiType()) {
                case "Ollama":
                    handleOllamaChatRequest(modelConfig, requestData, response);
                    break;
                case "OpenAI":
                    handleOpenAIChatRequest(modelConfig, requestData, response);
                    break;
                case "DeepSeek":
                    handleDeepSeekChatRequest(modelConfig, requestData, response);
                    break;
                default:
                    sendErrorResponse(response, "不支持的模型类型: " + modelConfig.getApiType());
                    break;
            }

        } catch (Exception e) {
            log.error("发送聊天请求异常: {}", e.getMessage(), e);
            sendErrorResponse(response, "请求异常: " + e.getMessage());
        }
    }

    @Override
    public void sendCompletionRequest(String modelName, Map<String, Object> requestData, HttpServletResponse response) {
        try {
            // 获取模型配置
            ModelConfig modelConfig = getModelConfigByName(modelName);
            if (modelConfig == null) {
                sendErrorResponse(response, "模型配置不存在: " + modelName);
                return;
            }

            // 检查模型是否启用
            if (modelConfig.getEnabled() != 1) {
                sendErrorResponse(response, "模型未启用: " + modelName);
                return;
            }

            // 设置SSE响应头
            setupSSEResponse(response);

            // 根据API类型选择不同的处理方式
            switch (modelConfig.getApiType()) {
                case "Ollama":
                    handleOllamaCompletionRequest(modelConfig, requestData, response);
                    break;
                case "OpenAI":
                    handleOpenAICompletionRequest(modelConfig, requestData, response);
                    break;
                case "DeepSeek":
                    handleDeepSeekCompletionRequest(modelConfig, requestData, response);
                    break;
                default:
                    sendErrorResponse(response, "不支持的模型类型: " + modelConfig.getApiType());
                    break;
            }

        } catch (Exception e) {
            log.error("发送完成请求异常: {}", e.getMessage(), e);
            sendErrorResponse(response, "请求异常: " + e.getMessage());
        }
    }

    @Override
    public boolean testModelConnection(String modelName) {
        try {
            ModelConfig modelConfig = getModelConfigByName(modelName);
            if (modelConfig == null) {
                return false;
            }

            switch (modelConfig.getApiType()) {
                case "Ollama":
                    return testOllamaConnection(modelConfig);
                case "OpenAI":
                    return testOpenAIConnection(modelConfig);
                case "DeepSeek":
                    return testDeepSeekConnection(modelConfig);
                default:
                    return false;
            }
        } catch (Exception e) {
            log.error("测试模型连接异常: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public ModelConfig getModelConfigByName(String modelName) {
        return modelConfigService.getModelConfigByName(modelName);
    }

    @Override
    public String[] getSupportedModelTypes() {
        return new String[] { "Ollama", "OpenAI", "DeepSeek" };
    }

    /**
     * 设置SSE响应头
     */
    private void setupSSEResponse(HttpServletResponse response) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Cache-Control");
    }

    /**
     * 发送SSE数据
     */
    private void sendSSEData(HttpServletResponse response, String data) {
        try {
            PrintWriter writer = response.getWriter();
            writer.write("data: " + data + "\n\n");
            writer.flush();
        } catch (IOException e) {
            log.error("发送SSE数据失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, String error) {
        try {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", error);
            errorData.put("success", false);

            sendSSEData(response, JSON.toJSONString(errorData));
        } catch (Exception e) {
            log.error("发送错误响应失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理Ollama聊天请求
     */
    private void handleOllamaChatRequest(ModelConfig modelConfig, Map<String, Object> requestData,
            HttpServletResponse response) {
        try {
            String endpoint = modelConfig.getEndpoint();

            // 构建Ollama请求
            Map<String, Object> ollamaRequest = new HashMap<>();
            ollamaRequest.put("model", modelConfig.getModelName());
            ollamaRequest.put("messages", requestData.get("messages"));
            ollamaRequest.put("stream", true);

            if (requestData.containsKey("options")) {
                ollamaRequest.put("options", requestData.get("options"));
            }

            // 发送请求并处理响应
            sendRequestAndHandleResponse(endpoint, ollamaRequest, response, "Ollama");

        } catch (Exception e) {
            log.error("处理Ollama聊天请求失败: {}", e.getMessage(), e);
            sendErrorResponse(response, "Ollama请求失败: " + e.getMessage());
        }
    }

    /**
     * 处理OpenAI聊天请求
     */
    private void handleOpenAIChatRequest(ModelConfig modelConfig, Map<String, Object> requestData,
            HttpServletResponse response) {
        try {
            String endpoint = modelConfig.getEndpoint();

            // 构建OpenAI请求
            Map<String, Object> openaiRequest = new HashMap<>();
            openaiRequest.put("model", modelConfig.getModelName());
            openaiRequest.put("messages", requestData.get("messages"));
            openaiRequest.put("stream", true);

            if (requestData.containsKey("temperature")) {
                openaiRequest.put("temperature", requestData.get("temperature"));
            }
            if (requestData.containsKey("max_tokens")) {
                openaiRequest.put("max_tokens", requestData.get("max_tokens"));
            }

            // 设置OpenAI API密钥
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(modelConfig.getApiKey());

            // 发送请求并处理响应
            sendRequestWithAuthAndHandleResponse(endpoint, openaiRequest, headers, response, "OpenAI");

        } catch (Exception e) {
            log.error("处理OpenAI聊天请求失败: {}", e.getMessage(), e);
            sendErrorResponse(response, "OpenAI请求失败: " + e.getMessage());
        }
    }

    /**
     * 处理DeepSeek聊天请求
     */
    private void handleDeepSeekChatRequest(ModelConfig modelConfig, Map<String, Object> requestData,
            HttpServletResponse response) {
        try {
            String endpoint = modelConfig.getEndpoint();

            // 构建DeepSeek请求
            Map<String, Object> deepseekRequest = new HashMap<>();
            deepseekRequest.put("model", modelConfig.getModelName());
            deepseekRequest.put("messages", requestData.get("messages"));
            deepseekRequest.put("stream", true);

            if (requestData.containsKey("temperature")) {
                deepseekRequest.put("temperature", requestData.get("temperature"));
            }
            if (requestData.containsKey("max_tokens")) {
                deepseekRequest.put("max_tokens", requestData.get("max_tokens"));
            }

            // 设置DeepSeek API密钥
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(modelConfig.getApiKey());

            // 发送请求并处理响应
            sendRequestWithAuthAndHandleResponse(endpoint, deepseekRequest, headers, response, "DeepSeek");

        } catch (Exception e) {
            log.error("处理DeepSeek聊天请求失败: {}", e.getMessage(), e);
            sendErrorResponse(response, "DeepSeek请求失败: " + e.getMessage());
        }
    }

    /**
     * 处理Ollama完成请求
     */
    private void handleOllamaCompletionRequest(ModelConfig modelConfig, Map<String, Object> requestData,
            HttpServletResponse response) {
        try {
            String endpoint = modelConfig.getEndpoint().replace("/api/chat", "/api/generate");

            // 构建Ollama请求
            Map<String, Object> ollamaRequest = new HashMap<>();
            ollamaRequest.put("model", modelConfig.getModelName());
            ollamaRequest.put("prompt", requestData.get("prompt"));
            ollamaRequest.put("stream", true);

            if (requestData.containsKey("options")) {
                ollamaRequest.put("options", requestData.get("options"));
            }

            // 发送请求并处理响应
            sendRequestAndHandleResponse(endpoint, ollamaRequest, response, "Ollama");

        } catch (Exception e) {
            log.error("处理Ollama完成请求失败: {}", e.getMessage(), e);
            sendErrorResponse(response, "Ollama请求失败: " + e.getMessage());
        }
    }

    /**
     * 处理OpenAI完成请求
     */
    private void handleOpenAICompletionRequest(ModelConfig modelConfig, Map<String, Object> requestData,
            HttpServletResponse response) {
        try {
            String endpoint = modelConfig.getEndpoint().replace("/chat/completions", "/completions");

            // 构建OpenAI请求
            Map<String, Object> openaiRequest = new HashMap<>();
            openaiRequest.put("model", modelConfig.getModelName());
            openaiRequest.put("prompt", requestData.get("prompt"));
            openaiRequest.put("stream", true);

            if (requestData.containsKey("temperature")) {
                openaiRequest.put("temperature", requestData.get("temperature"));
            }
            if (requestData.containsKey("max_tokens")) {
                openaiRequest.put("max_tokens", requestData.get("max_tokens"));
            }

            // 设置OpenAI API密钥
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(modelConfig.getApiKey());

            // 发送请求并处理响应
            sendRequestWithAuthAndHandleResponse(endpoint, openaiRequest, headers, response, "OpenAI");

        } catch (Exception e) {
            log.error("处理OpenAI完成请求失败: {}", e.getMessage(), e);
            sendErrorResponse(response, "OpenAI请求失败: " + e.getMessage());
        }
    }

    /**
     * 处理DeepSeek完成请求
     */
    private void handleDeepSeekCompletionRequest(ModelConfig modelConfig, Map<String, Object> requestData,
            HttpServletResponse response) {
        try {
            String endpoint = modelConfig.getEndpoint().replace("/chat/completions", "/completions");

            // 构建DeepSeek请求
            Map<String, Object> deepseekRequest = new HashMap<>();
            deepseekRequest.put("model", modelConfig.getModelName());
            deepseekRequest.put("prompt", requestData.get("prompt"));
            deepseekRequest.put("stream", true);

            if (requestData.containsKey("temperature")) {
                deepseekRequest.put("temperature", requestData.get("temperature"));
            }
            if (requestData.containsKey("max_tokens")) {
                deepseekRequest.put("max_tokens", requestData.get("max_tokens"));
            }

            // 设置DeepSeek API密钥
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(modelConfig.getApiKey());

            // 发送请求并处理响应
            sendRequestWithAuthAndHandleResponse(endpoint, deepseekRequest, headers, response, "DeepSeek");

        } catch (Exception e) {
            log.error("处理DeepSeek完成请求失败: {}", e.getMessage(), e);
            sendErrorResponse(response, "DeepSeek请求失败: " + e.getMessage());
        }
    }

    /**
     * 发送请求并处理响应（无认证）
     */
    private void sendRequestAndHandleResponse(String endpoint, Map<String, Object> requestData,
            HttpServletResponse response, String modelType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestData, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    endpoint, HttpMethod.POST, requestEntity, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                // 模拟流式响应
                String responseBody = responseEntity.getBody();
                if (responseBody != null) {
                    // 将响应数据按行分割，模拟流式输出
                    String[] lines = responseBody.split("\n");
                    for (String line : lines) {
                        if (!line.trim().isEmpty()) {
                            sendSSEData(response, line);
                            try {
                                Thread.sleep(50); // 模拟流式延迟
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }
                }
            } else {
                sendErrorResponse(response, "请求失败，状态码: " + responseEntity.getStatusCode());
            }

        } catch (Exception e) {
            log.error("发送{}请求失败: {}", modelType, e.getMessage(), e);
            sendErrorResponse(response, modelType + "请求失败: " + e.getMessage());
        }
    }

    /**
     * 发送请求并处理响应（带认证）
     */
    private void sendRequestWithAuthAndHandleResponse(String endpoint, Map<String, Object> requestData,
            HttpHeaders headers, HttpServletResponse response, String modelType) {
        try {
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestData, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    endpoint, HttpMethod.POST, requestEntity, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                // 模拟流式响应
                String responseBody = responseEntity.getBody();
                if (responseBody != null) {
                    // 将响应数据按行分割，模拟流式输出
                    String[] lines = responseBody.split("\n");
                    for (String line : lines) {
                        if (!line.trim().isEmpty()) {
                            sendSSEData(response, line);
                            try {
                                Thread.sleep(50); // 模拟流式延迟
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }
                }
            } else {
                sendErrorResponse(response, "请求失败，状态码: " + responseEntity.getStatusCode());
            }

        } catch (Exception e) {
            log.error("发送{}请求失败: {}", modelType, e.getMessage(), e);
            sendErrorResponse(response, modelType + "请求失败: " + e.getMessage());
        }
    }

    /**
     * 测试Ollama连接
     */
    private boolean testOllamaConnection(ModelConfig modelConfig) {
        try {
            String endpoint = modelConfig.getEndpoint().replace("/api/chat", "/api/tags");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint, HttpMethod.GET, requestEntity, String.class);

            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("测试Ollama连接失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 测试OpenAI连接
     */
    private boolean testOpenAIConnection(ModelConfig modelConfig) {
        try {
            if (modelConfig.getApiKey() == null || modelConfig.getApiKey().trim().isEmpty()) {
                return false;
            }

            String endpoint = modelConfig.getEndpoint().replace("/chat/completions", "/models");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(modelConfig.getApiKey());

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint, HttpMethod.GET, requestEntity, String.class);

            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("测试OpenAI连接失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 测试DeepSeek连接
     */
    private boolean testDeepSeekConnection(ModelConfig modelConfig) {
        try {
            if (modelConfig.getApiKey() == null || modelConfig.getApiKey().trim().isEmpty()) {
                return false;
            }

            String endpoint = modelConfig.getEndpoint().replace("/chat/completions", "/models");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(modelConfig.getApiKey());

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint, HttpMethod.GET, requestEntity, String.class);

            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("测试DeepSeek连接失败: {}", e.getMessage(), e);
            return false;
        }
    }
}