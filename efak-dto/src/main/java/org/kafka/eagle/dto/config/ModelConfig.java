package org.kafka.eagle.dto.config;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * <p>
 * 大模型配置 DTO，用于管理 AI 模型的配置信息。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/13 10:00:00
 * @version 5.0.0
 */
@Data
public class ModelConfig {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * API类型 (OpenAI, Ollama, DeepSeek等)
     */
    private String apiType;

    /**
     * 接口地址
     */
    private String endpoint;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 超时时间(秒)
     */
    private Integer timeout;

    /**
     * 描述信息
     */
    private String description;

    /**
     * 是否启用 (0:禁用, 1:启用)
     */
    private Integer enabled;

    /**
     * 状态 (0:离线, 1:在线, 2:错误)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;
}