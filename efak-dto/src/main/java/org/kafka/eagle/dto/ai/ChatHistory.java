package org.kafka.eagle.dto.ai;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * AI 聊天历史记录 DTO，用于 EFAK Web 界面的聊天历史管理。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/7 22:58:08
 * @version 5.0.0
 */
@Data
public class ChatHistory {

    private String sessionId; // 会话标识
    private String title; // 会话标题
    private String modelName; // AI模型名称
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
    private Integer messageCount; // 消息数量
    private List<ChatMessage> messages; // 消息列表

    // 构造函数
    public ChatHistory() {
    }

    public ChatHistory(String sessionId, String title, String modelName) {
        this.sessionId = sessionId;
        this.title = title;
        this.modelName = modelName;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.messageCount = 0;
    }

}