package org.kafka.eagle.web.controller;

import org.kafka.eagle.dto.ai.ChatHistory;
import org.kafka.eagle.dto.ai.ChatSession;
import org.kafka.eagle.web.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * AI聊天控制器
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/10 01:21:37
 * @version 5.0.0
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    /**
     * 获取当前用户名
     */
    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getName())) {
                return authentication.getName();
            }
            return "admin"; // 默认用户名
        } catch (Exception e) {
            logger.warn("获取当前用户名失败，使用默认用户名", e);
            return "admin";
        }
    }

    /**
     * 创建新会话
     */
    @PostMapping("/session")
    public ResponseEntity<Map<String, Object>> createSession(@RequestBody Map<String, String> request) {
        try {
            String title = request.get("title");
            String modelName = request.get("modelName");
            String username = getCurrentUsername();

            if (title == null || title.trim().isEmpty()) {
                title = "新对话";
            }
            if (modelName == null || modelName.trim().isEmpty()) {
                modelName = "GPT-4";
            }

            ChatSession session = chatService.createSession(username, title, modelName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("session", session);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("创建会话失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建会话失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取用户的最近会话列表
     */
    @GetMapping("/sessions")
    public ResponseEntity<Map<String, Object>> getRecentSessions() {
        try {
            String username = getCurrentUsername();
            List<ChatSession> sessions = chatService.getRecentSessions(username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sessions", sessions);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取会话列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取会话列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取会话历史
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSessionHistory(@PathVariable String sessionId) {
        try {
            String username = getCurrentUsername();
            ChatHistory history = chatService.getSessionHistory(sessionId, username);

            if (history == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "会话不存在或无权限访问");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("history", history);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取会话历史失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取会话历史失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 添加消息
     */
    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> addMessage(@RequestBody Map<String, String> request) {
        try {
            String sessionId = request.get("sessionId");
            String sender = request.get("sender");
            String content = request.get("content");
            String modelName = request.get("modelName");
            String username = getCurrentUsername();

            if (sessionId == null || sessionId.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "会话ID不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            if (sender == null || sender.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "发送者不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            if (content == null || content.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "消息内容不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            if (modelName == null || modelName.trim().isEmpty()) {
                modelName = "GPT-4";
            }

            chatService.addMessage(sessionId, username, sender, content, modelName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("添加消息失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "添加消息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> deleteSession(@PathVariable String sessionId) {
        try {
            String username = getCurrentUsername();
            chatService.deleteSession(sessionId, username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("删除会话失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除会话失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除会话的最后一条消息
     */
    @DeleteMapping("/message/last")
    public ResponseEntity<Map<String, Object>> deleteLastMessage(@RequestBody Map<String, String> request) {
        try {
            String sessionId = request.get("sessionId");
            String sender = request.get("sender");
            String username = getCurrentUsername();

            if (sessionId == null || sessionId.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "会话ID不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            if (sender == null || sender.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "发送者不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            chatService.deleteLastMessage(sessionId, username, sender);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("删除最后一条消息失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除最后一条消息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}