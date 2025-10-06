package org.kafka.eagle.web.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.kafka.eagle.web.mapper.ModelConfigMapper;
import org.kafka.eagle.web.service.ModelConfigService;
import org.kafka.eagle.web.service.gateway.OllamaGatewayService;
import org.kafka.eagle.dto.config.ModelConfig;
import org.kafka.eagle.dto.config.ModelConfigStatisticsDTO;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * ModelConfig 服务实现类
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/15 23:33:37
 * @version 5.0.0
 */
@Slf4j
@Service
public class ModelConfigServiceImpl implements ModelConfigService {

    @Autowired
    private ModelConfigMapper modelConfigMapper;

    @Autowired
    private OllamaGatewayService ollamaGatewayService;

    @Override
    public Map<String, Object> getModelConfigList(int page, int size, String search, String apiType, Integer enabled) {
        Map<String, Object> result = new HashMap<>();

        // 计算偏移量
        int offset = (page - 1) * size;

        // 查询数据列表
        List<ModelConfig> modelConfigs = modelConfigMapper.selectModelConfigList(search, apiType, enabled, offset,
                size);

        // 查询总数
        Long total = modelConfigMapper.selectModelConfigCount(search, apiType, enabled);

        // 计算总页数
        int totalPages = (int) Math.ceil((double) total / size);

        result.put("modelConfigs", modelConfigs);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", totalPages);

        return result;
    }

    @Override
    public ModelConfig getModelConfigById(Long id) {
        return modelConfigMapper.selectModelConfigById(id);
    }

    @Override
    public ModelConfig getModelConfigByName(String modelName) {
        return modelConfigMapper.selectModelConfigByName(modelName);
    }

    @Override
    public boolean addModelConfig(ModelConfig modelConfig) {
        try {
            // 设置默认值
            if (modelConfig.getTimeout() == null) {
                modelConfig.setTimeout(30);
            }
            if (modelConfig.getEnabled() == null) {
                modelConfig.setEnabled(1);
            }
            if (modelConfig.getStatus() == null) {
                modelConfig.setStatus(0);
            }

            modelConfig.setCreateTime(LocalDateTime.now());
            modelConfig.setUpdateTime(LocalDateTime.now());

            int result = modelConfigMapper.insertModelConfig(modelConfig);
            return result > 0;
        } catch (Exception e) {
            log.error("添加模型配置失败", e);
            return false;
        }
    }

    @Override
    public boolean updateModelConfig(ModelConfig modelConfig) {
        try {
            modelConfig.setUpdateTime(LocalDateTime.now());
            int result = modelConfigMapper.updateModelConfig(modelConfig);
            return result > 0;
        } catch (Exception e) {
            log.error("更新模型配置失败", e);
            return false;
        }
    }

    @Override
    public boolean deleteModelConfig(Long id) {
        try {
            int result = modelConfigMapper.deleteModelConfigById(id);
            return result > 0;
        } catch (Exception e) {
            log.error("删除模型配置失败", e);
            return false;
        }
    }

    @Override
    public boolean updateModelStatus(Long id, Integer status) {
        try {
            int result = modelConfigMapper.updateModelStatus(id, status);
            return result > 0;
        } catch (Exception e) {
            log.error("更新模型状态失败", e);
            return false;
        }
    }

    @Override
    public boolean testModelConnection(Long id) {
        try {
            ModelConfig modelConfig = modelConfigMapper.selectModelConfigById(id);
            if (modelConfig == null) {
                log.error("模型配置不存在，ID: {}", id);
                return false;
            }

            // 根据API类型进行不同的连接测试
            boolean testResult = false;
            switch (modelConfig.getApiType()) {
                case "OpenAI":
                    testResult = testOpenAIConnection(modelConfig);
                    break;
                case "Ollama":
                    testResult = testOllamaConnection(modelConfig);
                    break;
                case "DeepSeek":
                    testResult = testDeepSeekConnection(modelConfig);
                    break;
                default:
                    log.warn("未知的API类型: {}", modelConfig.getApiType());
                    testResult = false;
                    break;
            }

            return testResult;

        } catch (Exception e) {
            log.error("测试模型连接失败，ID: {}", id, e);
            return false;
        }
    }

    /**
     * 测试OpenAI连接
     */
    private boolean testOpenAIConnection(ModelConfig modelConfig) {
        try {
            // 检查必要参数
            if (modelConfig.getApiKey() == null || modelConfig.getApiKey().trim().isEmpty()) {
                log.error("OpenAI API密钥未设置");
                return false;
            }

            // 这里可以添加实际的OpenAI API调用测试
            // 例如发送一个简单的测试请求到OpenAI API
            log.info("测试OpenAI连接: {}", modelConfig.getEndpoint());

            // 暂时返回true，实际实现时可以调用OpenAI API
            return true;
        } catch (Exception e) {
            log.error("测试OpenAI连接失败", e);
            return false;
        }
    }

    /**
     * 测试Ollama连接
     */
    private boolean testOllamaConnection(ModelConfig modelConfig) {
        try {
            log.info("测试Ollama连接: {}", modelConfig.getEndpoint());

            // 使用Ollama网关服务进行连接测试
            return ollamaGatewayService.testModelConnection(modelConfig.getModelName(),modelConfig.getEndpoint());
        } catch (Exception e) {
            log.error("测试Ollama连接失败", e);
            return false;
        }
    }

    /**
     * 测试DeepSeek连接
     */
    private boolean testDeepSeekConnection(ModelConfig modelConfig) {
        try {
            // 检查必要参数
            if (modelConfig.getApiKey() == null || modelConfig.getApiKey().trim().isEmpty()) {
                log.error("DeepSeek API密钥未设置");
                return false;
            }

            log.info("测试DeepSeek连接: {}", modelConfig.getEndpoint());

            // 这里可以添加实际的DeepSeek API调用测试
            // 例如发送一个简单的测试请求到DeepSeek API
            // 暂时返回true，实际实现时可以调用DeepSeek API
            return true;
        } catch (Exception e) {
            log.error("测试DeepSeek连接失败", e);
            return false;
        }
    }

    @Override
    public List<ModelConfig> getEnabledModelConfigs() {
        return modelConfigMapper.selectEnabledModelConfigs();
    }

    @Override
    public ModelConfigStatisticsDTO getModelConfigStatistics() {
        try {
            ModelConfigStatisticsDTO statistics = modelConfigMapper.selectModelConfigStatistics();

            if (statistics == null) {
                log.warn("查询到的统计数据为空，返回默认统计数据");
                statistics = new ModelConfigStatisticsDTO();
            }

            return statistics;
        } catch (Exception e) {
            log.error("查询模型配置统计数据失败", e);
            // 返回默认的空统计数据
            return new ModelConfigStatisticsDTO();
        }
    }
}