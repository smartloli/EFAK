package org.kafka.eagle.web.service.impl;

import org.kafka.eagle.dto.ai.ChatHistory;
import org.kafka.eagle.dto.ai.ChatMessage;
import org.kafka.eagle.dto.ai.ChatSession;
import org.kafka.eagle.web.mapper.ChatMessageMapper;
import org.kafka.eagle.web.mapper.ChatSessionMapper;
import org.kafka.eagle.web.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * AI聊天服务实现类
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/10 01:22:04
 * @version 5.0.0
 */
@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Override
    @Transactional
    public ChatSession createSession(String username, String title, String modelName) {
        try {
            // 生成会话ID
            String sessionId = UUID.randomUUID().toString().replace("-", "");

            // 创建会话
            ChatSession chatSession = new ChatSession(username, sessionId, title, modelName);
            int result = chatSessionMapper.insert(chatSession);

            if (result > 0) {
                logger.info("创建会话成功: sessionId={}, username={}, title={}", sessionId, username, title);

                // 清理旧会话
                cleanOldSessions(username);

                return chatSession;
            } else {
                logger.error("创建会话失败: username={}, title={}", username, title);
                throw new RuntimeException("创建会话失败");
            }
        } catch (Exception e) {
            logger.error("创建会话异常: username={}, title={}", username, title, e);
            throw new RuntimeException("创建会话失败: " + e.getMessage());
        }
    }

    @Override
    public List<ChatSession> getRecentSessions(String username) {
        try {
            List<ChatSession> sessions = chatSessionMapper.selectRecentByUsername(username);
            logger.info("获取用户会话列表成功: username={}, count={}", username, sessions.size());
            return sessions;
        } catch (Exception e) {
            logger.error("获取用户会话列表失败: username={}", username, e);
            throw new RuntimeException("获取会话列表失败: " + e.getMessage());
        }
    }

    @Override
    public ChatHistory getSessionHistory(String sessionId, String username) {
        try {
            // 获取会话信息
            ChatSession session = chatSessionMapper.selectBySessionId(sessionId);
            if (session == null || !session.getUsername().equals(username)) {
                logger.warn("会话不存在或无权限访问: sessionId={}, username={}", sessionId, username);
                return null;
            }

            // 获取消息列表
            List<ChatMessage> messages = chatMessageMapper.selectBySessionId(sessionId);

            // 构建历史记录
            ChatHistory history = new ChatHistory(sessionId, session.getTitle(), session.getModelName());
            history.setCreateTime(session.getCreateTime());
            history.setUpdateTime(session.getUpdateTime());
            history.setMessageCount(session.getMessageCount());
            history.setMessages(messages);

            logger.info("获取会话历史成功: sessionId={}, username={}, messageCount={}", sessionId, username, messages.size());
            return history;
        } catch (Exception e) {
            logger.error("获取会话历史失败: sessionId={}, username={}", sessionId, username, e);
            throw new RuntimeException("获取会话历史失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void addMessage(String sessionId, String username, String sender, String content, String modelName) {
        try {
            // 验证会话是否存在
            ChatSession session = chatSessionMapper.selectBySessionId(sessionId);
            if (session == null || !session.getUsername().equals(username)) {
                logger.warn("会话不存在或无权限访问: sessionId={}, username={}", sessionId, username);
                throw new RuntimeException("会话不存在或无权限访问");
            }

            // 创建消息
            ChatMessage message = new ChatMessage(sessionId, username, sender, content, modelName);
            int result = chatMessageMapper.insert(message);

            if (result > 0) {
                // 更新会话消息数量
                chatSessionMapper.incrementMessageCount(sessionId);

                // 更新会话标题（如果是第一条用户消息）
                if ("user".equals(sender) && session.getMessageCount() == 0) {
                    String title = content.length() > 50 ? content.substring(0, 50) + "..." : content;
                    session.setTitle(title);
                    session.setUpdateTime(LocalDateTime.now());
                    chatSessionMapper.updateBySessionId(session);
                }

                logger.info("添加消息成功: sessionId={}, username={}, sender={}, contentLength={}",
                        sessionId, username, sender, content.length());
            } else {
                logger.error("添加消息失败: sessionId={}, username={}, sender={}", sessionId, username, sender);
                throw new RuntimeException("添加消息失败");
            }
        } catch (Exception e) {
            logger.error("添加消息异常: sessionId={}, username={}, sender={}", sessionId, username, sender, e);
            throw new RuntimeException("添加消息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteSession(String sessionId, String username) {
        try {
            // 验证会话是否存在
            ChatSession session = chatSessionMapper.selectBySessionId(sessionId);
            if (session == null || !session.getUsername().equals(username)) {
                logger.warn("会话不存在或无权限访问: sessionId={}, username={}", sessionId, username);
                throw new RuntimeException("会话不存在或无权限访问");
            }

            // 软删除会话和消息
            chatSessionMapper.deleteBySessionId(sessionId);
            chatMessageMapper.deleteBySessionId(sessionId);

            logger.info("删除会话成功: sessionId={}, username={}", sessionId, username);
        } catch (Exception e) {
            logger.error("删除会话失败: sessionId={}, username={}", sessionId, username, e);
            throw new RuntimeException("删除会话失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void cleanOldSessions(String username) {
        try {
            int deletedCount = chatSessionMapper.cleanOldSessions(username);
            if (deletedCount > 0) {
                logger.info("清理旧会话成功: username={}, deletedCount={}", username, deletedCount);
            }
        } catch (Exception e) {
            logger.error("清理旧会话失败: username={}", username, e);
            // 不抛出异常，避免影响主要功能
        }
    }

    @Override
    @Transactional
    public void deleteLastMessage(String sessionId, String username, String sender) {
        try {
            // 验证会话是否存在
            ChatSession session = chatSessionMapper.selectBySessionId(sessionId);
            if (session == null || !session.getUsername().equals(username)) {
                logger.warn("会话不存在或无权限访问: sessionId={}, username={}", sessionId, username);
                throw new RuntimeException("会话不存在或无权限访问");
            }

            // 删除最后一条指定发送者的消息
            int deletedCount = chatMessageMapper.deleteLastMessageBySender(sessionId, sender);

            if (deletedCount > 0) {
                // 更新会话消息数量
                chatSessionMapper.decrementMessageCount(sessionId);
                logger.info("删除最后一条消息成功: sessionId={}, username={}, sender={}", sessionId, username, sender);
            } else {
                logger.warn("没有找到要删除的消息: sessionId={}, sender={}", sessionId, sender);
                throw new RuntimeException("没有找到要删除的消息");
            }
        } catch (Exception e) {
            logger.error("删除最后一条消息失败: sessionId={}, username={}, sender={}", sessionId, username, sender, e);
            throw new RuntimeException("删除最后一条消息失败: " + e.getMessage());
        }
    }
}