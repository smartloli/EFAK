package org.kafka.eagle.web.mapper;

import org.apache.ibatis.annotations.*;
import org.kafka.eagle.dto.ai.ChatMessage;

import java.util.List;

/**
 * <p>
 * AI聊天消息Mapper
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/10 01:22:00
 * @version 5.0.0
 */
@Mapper
public interface ChatMessageMapper {

        /**
         * 插入消息
         */
        @Insert("INSERT INTO ke_chat_message (session_id, username, sender, content, model_name, " +
                        "message_type, enable_markdown, enable_charts, enable_highlight, create_time, status) " +
                        "VALUES (#{sessionId}, #{username}, #{sender}, #{content}, #{modelName}, " +
                        "#{messageType}, #{enableMarkdown}, #{enableCharts}, #{enableHighlight}, #{createTime}, #{status})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(ChatMessage chatMessage);

        /**
         * 根据会话ID查询消息列表
         */
        @Select("SELECT * FROM ke_chat_message WHERE session_id = #{sessionId} AND status = 1 " +
                        "ORDER BY create_time ASC")
        List<ChatMessage> selectBySessionId(String sessionId);

        /**
         * 根据会话ID查询最近的消息
         */
        @Select("SELECT * FROM ke_chat_message WHERE session_id = #{sessionId} AND status = 1 " +
                        "ORDER BY create_time DESC LIMIT #{limit}")
        List<ChatMessage> selectRecentBySessionId(@Param("sessionId") String sessionId, @Param("limit") int limit);

        /**
         * 根据用户名查询所有消息
         */
        @Select("SELECT * FROM ke_chat_message WHERE username = #{username} AND status = 1 " +
                        "ORDER BY create_time DESC")
        List<ChatMessage> selectAllByUsername(String username);

        /**
         * 删除会话的所有消息（软删除）
         */
        @Update("UPDATE ke_chat_message SET status = 0 WHERE session_id = #{sessionId}")
        int deleteBySessionId(String sessionId);

        /**
         * 删除用户的所有消息（软删除）
         */
        @Update("UPDATE ke_chat_message SET status = 0 WHERE username = #{username}")
        int deleteByUsername(String username);

        /**
         * 统计会话的消息数量
         */
        @Select("SELECT COUNT(*) FROM ke_chat_message WHERE session_id = #{sessionId} AND status = 1")
        int countBySessionId(String sessionId);

        /**
         * 删除会话中最后一条指定发送者的消息
         */
        @Delete("DELETE FROM ke_chat_message WHERE id = (" +
                        "SELECT id FROM ke_chat_message " +
                        "WHERE session_id = #{sessionId} AND sender = #{sender} AND status = 1 " +
                        "ORDER BY create_time DESC LIMIT 1)")
        int deleteLastMessageBySender(@Param("sessionId") String sessionId, @Param("sender") String sender);
}