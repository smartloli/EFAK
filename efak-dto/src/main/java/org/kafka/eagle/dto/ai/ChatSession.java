package org.kafka.eagle.dto.ai;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * <p>
 * AI 聊天会话 DTO，用于 EFAK Web 界面的聊天会话管理。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/27 10:00:05
 * @version 5.0.0
 */
@Data
public class ChatSession {

    private Long id;
    private String username; // 用户名
    private String sessionId; // 会话标识
    private String title; // 会话标题
    private String modelName; // AI模型名称
    private Integer messageCount; // 消息数量
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
    private Integer status; // 状态：1-活跃，0-已删除

    // 构造函数
    public ChatSession() {
    }

    public ChatSession(String username, String sessionId, String title, String modelName) {
        this.username = username;
        this.sessionId = sessionId;
        this.title = title;
        this.modelName = modelName;
        this.messageCount = 0;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.status = 1;
    }

}