package org.kafka.eagle.web.controller;

import org.kafka.eagle.web.service.ChatStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>
 * 聊天流式传输控制器
 * 提供SSE（Server-Sent Events）方式的实时聊天流式响应接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/10 00:17:45
 * @version 5.0.0
 */
@RestController
@RequestMapping("/api/chat")
public class ChatStreamController {

    @Autowired
    private ChatStreamService chatStreamService;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestParam String modelId,
            @RequestParam String message,
            @RequestParam(required = false) String clusterId,
            @RequestParam(required = false, defaultValue = "false") boolean enableCharts,
            HttpServletResponse response) {

        // 设置SSE响应头
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Cache-Control");

        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时

        executorService.execute(() -> {
            try {
                chatStreamService.processChatStream(modelId, message, clusterId, enableCharts, emitter);
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("{\"type\":\"error\",\"message\":\"" + e.getMessage() + "\"}"));
                    emitter.complete();
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            }
        });

        return emitter;
    }
}