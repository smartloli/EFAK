package org.kafka.eagle.dto.config;

import lombok.Data;

/**
 * <p>
 * 模型配置统计数据传输对象 DTO，用于提供模型配置的统计信息。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/13 09:05:23
 * @version 5.0.0
 */
@Data
public class ModelConfigStatisticsDTO {

    /**
     * 总模型数
     */
    private long totalModels;

    /**
     * 在线模型数 (status = 1)
     */
    private long onlineModels;

    /**
     * 离线模型数 (status = 0)
     */
    private long offlineModels;

    /**
     * 禁用模型数 (enabled = 0)
     */
    private long disabledModels;

    /**
     * 启用模型数 (enabled = 1)
     */
    private long enabledModels;

    /**
     * 错误状态模型数 (status = 2)
     */
    private long errorModels;

    /**
     * OpenAI类型模型数
     */
    private long openaiModels;

    /**
     * Ollama类型模型数
     */
    private long ollamaModels;

    /**
     * DeepSeek类型模型数
     */
    private long deepseekModels;

    /**
     * 其他类型模型数
     */
    private long otherModels;

    /**
     * 默认构造函数
     */
    public ModelConfigStatisticsDTO() {
        this.totalModels = 0;
        this.onlineModels = 0;
        this.offlineModels = 0;
        this.disabledModels = 0;
        this.enabledModels = 0;
        this.errorModels = 0;
        this.openaiModels = 0;
        this.ollamaModels = 0;
        this.deepseekModels = 0;
        this.otherModels = 0;
    }

    /**
     * 带参数的构造函数
     */
    public ModelConfigStatisticsDTO(long totalModels, long onlineModels, long offlineModels, long disabledModels) {
        this.totalModels = totalModels;
        this.onlineModels = onlineModels;
        this.offlineModels = offlineModels;
        this.disabledModels = disabledModels;
        this.enabledModels = totalModels - disabledModels;
        this.errorModels = 0;
        this.openaiModels = 0;
        this.ollamaModels = 0;
        this.deepseekModels = 0;
        this.otherModels = 0;
    }
}