package org.kafka.eagle.web.mapper;

import org.apache.ibatis.annotations.*;
import org.kafka.eagle.dto.ai.ChatSession;

import java.util.List;

/**
 * <p>
 * AI聊天会话Mapper
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/10 01:22:24
 * @version 5.0.0
 */
@Mapper
public interface ChatSessionMapper {

        /**
         * 插入会话
         */
        @Insert("INSERT INTO ke_chat_session (username, session_id, title, model_name, message_count, create_time, update_time, status) "
                        +
                        "VALUES (#{username}, #{sessionId}, #{title}, #{modelName}, #{messageCount}, #{createTime}, #{updateTime}, #{status})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(ChatSession chatSession);

        /**
         * 根据会话ID查询会话
         */
        @Select("SELECT * FROM ke_chat_session WHERE session_id = #{sessionId} AND status = 1")
        ChatSession selectBySessionId(String sessionId);

        /**
         * 根据用户名查询最近的会话列表（最多5个）
         */
        @Select("SELECT * FROM ke_chat_session WHERE username = #{username} AND status = 1 " +
                        "ORDER BY update_time DESC LIMIT 5")
        List<ChatSession> selectRecentByUsername(String username);

        /**
         * 根据用户名查询所有会话
         */
        @Select("SELECT * FROM ke_chat_session WHERE username = #{username} AND status = 1 " +
                        "ORDER BY update_time DESC")
        List<ChatSession> selectAllByUsername(String username);

        /**
         * 更新会话信息
         */
        @Update("UPDATE ke_chat_session SET title = #{title}, model_name = #{modelName}, " +
                        "message_count = #{messageCount}, update_time = #{updateTime} WHERE session_id = #{sessionId}")
        int updateBySessionId(ChatSession chatSession);

        /**
         * 更新消息数量
         */
        @Update("UPDATE ke_chat_session SET message_count = message_count + 1, update_time = NOW() " +
                        "WHERE session_id = #{sessionId}")
        int incrementMessageCount(String sessionId);

        /**
         * 减少消息数量
         */
        @Update("UPDATE ke_chat_session SET message_count = GREATEST(message_count - 1, 0), update_time = NOW() " +
                        "WHERE session_id = #{sessionId}")
        int decrementMessageCount(String sessionId);

        /**
         * 删除会话（软删除）
         */
        @Update("UPDATE ke_chat_session SET status = 0 WHERE session_id = #{sessionId}")
        int deleteBySessionId(String sessionId);

        /**
         * 删除用户的所有会话（软删除）
         */
        @Update("UPDATE ke_chat_session SET status = 0 WHERE username = #{username}")
        int deleteByUsername(String username);

        /**
         * 清理用户超过5个的旧会话
         */
        @Delete("DELETE FROM ke_chat_session WHERE username = #{username} AND status = 1 " +
                        "AND id NOT IN (SELECT id FROM (SELECT id FROM ke_chat_session " +
                        "WHERE username = #{username} AND status = 1 ORDER BY update_time DESC LIMIT 5) t)")
        int cleanOldSessions(String username);
}