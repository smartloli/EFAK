package org.kafka.eagle.dto.ai;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * <p>
 * AI 聊天消息 DTO，用于 EFAK Web 界面的聊天消息管理。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/7 22:57:49
 * @version 5.0.0
 */
@Data
public class ChatMessage {

    private Long id;
    private String sessionId; // 会话ID
    private String username; // 用户名
    private String sender; // 发送者：用户/助手
    private String content; // 消息内容
    private String modelName; // AI模型名称
    private Integer messageType; // 消息类型：1-文本，2-图片，3-文件
    private Integer enableMarkdown; // 是否启用Markdown格式：1-启用，0-禁用
    private Integer enableCharts; // 是否启用图表：1-启用，0-禁用
    private Integer enableHighlight; // 是否启用代码高亮：1-启用，0-禁用
    private LocalDateTime createTime; // 创建时间
    private Integer status; // 状态：1-正常，0-已删除

    // 构造函数
    public ChatMessage() {
    }

    public ChatMessage(String sessionId, String username, String sender, String content, String modelName) {
        this.sessionId = sessionId;
        this.username = username;
        this.sender = sender;
        this.content = content;
        this.modelName = modelName;
        this.messageType = 1;
        this.enableMarkdown = 1;
        this.enableCharts = 1;
        this.enableHighlight = 1;
        this.createTime = LocalDateTime.now();
        this.status = 1;
    }

}