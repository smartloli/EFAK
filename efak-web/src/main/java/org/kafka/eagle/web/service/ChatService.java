package org.kafka.eagle.web.service;

import org.kafka.eagle.dto.ai.ChatHistory;
import org.kafka.eagle.dto.ai.ChatMessage;
import org.kafka.eagle.dto.ai.ChatSession;

import java.util.List;

/**
 * <p>
 * AI聊天服务接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/10 01:21:42
 * @version 5.0.0
 */
public interface ChatService {

    /**
     * 创建新会话
     */
    ChatSession createSession(String username, String title, String modelName);

    /**
     * 获取用户的最近会话列表
     */
    List<ChatSession> getRecentSessions(String username);

    /**
     * 获取会话详情
     */
    ChatHistory getSessionHistory(String sessionId, String username);

    /**
     * 添加消息到会话
     */
    void addMessage(String sessionId, String username, String sender, String content, String modelName);

    /**
     * 删除会话
     */
    void deleteSession(String sessionId, String username);

    /**
     * 清理用户的旧会话（保留最近5个）
     */
    void cleanOldSessions(String username);

    /**
     * 删除会话的最后一条消息
     */
    void deleteLastMessage(String sessionId, String username, String sender);
}