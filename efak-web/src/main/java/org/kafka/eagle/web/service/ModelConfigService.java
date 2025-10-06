package org.kafka.eagle.web.service;

import org.kafka.eagle.dto.config.ModelConfig;
import org.kafka.eagle.dto.config.ModelConfigStatisticsDTO;
import java.util.Map;
import java.util.List;

/**
 * <p>
 * ModelConfig 服务接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/15 23:33:01
 * @version 5.0.0
 */
public interface ModelConfigService {

    /**
     * 分页查询模型配置列表
     */
    Map<String, Object> getModelConfigList(int page, int size, String search, String apiType, Integer enabled);

    /**
     * 根据ID查询模型配置
     */
    ModelConfig getModelConfigById(Long id);

    /**
     * 根据模型名称查询模型配置
     */
    ModelConfig getModelConfigByName(String modelName);

    /**
     * 添加模型配置
     */
    boolean addModelConfig(ModelConfig modelConfig);

    /**
     * 更新模型配置
     */
    boolean updateModelConfig(ModelConfig modelConfig);

    /**
     * 删除模型配置
     */
    boolean deleteModelConfig(Long id);

    /**
     * 更新模型状态
     */
    boolean updateModelStatus(Long id, Integer status);

    /**
     * 测试模型连接
     */
    boolean testModelConnection(Long id);

    /**
     * 获取所有启用的模型配置
     */
    List<ModelConfig> getEnabledModelConfigs();

    /**
     * 获取模型配置统计数据
     */
    ModelConfigStatisticsDTO getModelConfigStatistics();
}