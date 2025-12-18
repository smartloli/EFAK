package org.kafka.eagle.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kafka.eagle.web.service.ChatStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 保存活跃的流式会话，用于前端 stop-stream 主动取消与资源回收
     */
    private final ConcurrentHashMap<String, StreamContext> streamContextMap = new ConcurrentHashMap<>();

    /**
     * 有界线程池：避免 newCachedThreadPool() 在高并发下无限创建线程导致随机卡死/无响应
     */
    private final ThreadPoolExecutor executorService = new ThreadPoolExecutor(
            8,
            32,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(200),
            new NamedThreadFactory("chat-stream-"),
            new ThreadPoolExecutor.AbortPolicy());

    /**
     * SSE 保活：降低反向代理/浏览器空闲断开导致“时好时坏”
     */
    private final ScheduledExecutorService keepAliveScheduler =
            Executors.newScheduledThreadPool(2, new NamedThreadFactory("chat-keepalive-"));

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestParam String modelId,
            @RequestParam String message,
            @RequestParam(required = false) String streamId,
            @RequestParam(required = false) String clusterId,
            @RequestParam(required = false, defaultValue = "false") boolean enableCharts,
            HttpServletResponse response) {

        // 设置SSE响应头
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Cache-Control");

        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时

        // 兼容：如果前端未传 streamId，则服务端生成一个（便于后续stop-stream）
        String finalStreamId = (streamId == null || streamId.trim().isEmpty())
                ? UUID.randomUUID().toString()
                : streamId.trim();

        StreamContext streamContext = new StreamContext(emitter);
        StreamContext old = streamContextMap.put(finalStreamId, streamContext);
        if (old != null) {
            // 同streamId重复时，优先终止旧连接，避免内存/线程泄漏
            old.canceled.set(true);
            safeCancel(old.keepAliveFuture);
            safeComplete(old.emitter);
        }

        // SSE 生命周期回调：用于可靠清理映射与定时任务
        emitter.onCompletion(() -> cleanup(finalStreamId));
        emitter.onTimeout(() -> {
            if (!isCanceled(finalStreamId)) {
                sendJson(emitter, Map.of("type", "error", "message", "SSE连接超时，请重试"));
            }
            safeComplete(emitter);
            cleanup(finalStreamId);
        });
        emitter.onError(ex -> cleanup(finalStreamId));

        // 定时发送 ping，避免长时间无输出被中间层断开
        streamContext.keepAliveFuture = keepAliveScheduler.scheduleAtFixedRate(() -> {
            if (isCanceled(finalStreamId)) {
                return;
            }
            // 只发送轻量 ping，前端可忽略
            boolean ok = sendJson(emitter, Map.of("type", "ping"));
            if (!ok) {
                // 发送失败通常意味着客户端断开，及时清理避免“僵尸流”
                safeComplete(emitter);
                cleanup(finalStreamId);
            }
        }, 15, 15, TimeUnit.SECONDS);

        try {
            executorService.execute(() -> {
                try {
                    chatStreamService.processChatStream(modelId, message, clusterId, enableCharts, emitter);
                } catch (Exception e) {
                    // 用户主动取消时，底层send可能抛出异常，这里不再向前端发送“错误”
                    if (isCanceled(finalStreamId)) {
                        safeComplete(emitter);
                        cleanup(finalStreamId);
                        return;
                    }
                    sendJson(emitter, Map.of("type", "error", "message", "处理聊天请求失败: " + e.getMessage()));
                    safeComplete(emitter);
                    cleanup(finalStreamId);
                }
            });
        } catch (RejectedExecutionException e) {
            // 线程池已满：直接回错误，避免前端一直等待
            sendJson(emitter, Map.of("type", "error", "message", "AI服务繁忙，请稍后重试"));
            safeComplete(emitter);
            cleanup(finalStreamId);
        }

        return emitter;
    }

    /**
     * 前端点击“停止”时调用：根据 streamId 主动终止 SSE
     */
    @PostMapping("/stop-stream")
    public ResponseEntity<Map<String, Object>> stopStream(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String streamId = request.get("streamId");
        if (streamId == null || streamId.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "streamId不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        StreamContext streamContext = streamContextMap.get(streamId.trim());
        if (streamContext == null) {
            // 幂等：可能已结束/已清理
            response.put("success", true);
            response.put("message", "流已结束或不存在");
            return ResponseEntity.ok(response);
        }

        // 标记取消并立即终止 emitter，避免继续占用线程/连接
        streamContext.canceled.set(true);
        safeCancel(streamContext.keepAliveFuture);
        safeComplete(streamContext.emitter);

        response.put("success", true);
        response.put("message", "已停止");
        return ResponseEntity.ok(response);
    }

    private boolean isCanceled(String streamId) {
        StreamContext ctx = streamContextMap.get(streamId);
        return ctx != null && ctx.canceled.get();
    }

    private void cleanup(String streamId) {
        StreamContext removed = streamContextMap.remove(streamId);
        if (removed != null) {
            safeCancel(removed.keepAliveFuture);
        }
    }

    private boolean sendJson(SseEmitter emitter, Map<String, Object> payload) {
        try {
            emitter.send(SseEmitter.event()
                    // 统一使用message事件，避免前端只监听onmessage导致“无返回”
                    .name("message")
                    .data(objectMapper.writeValueAsString(payload)));
            return true;
        } catch (Exception ignore) {
            // 可能是客户端断开/主动取消导致的异常，忽略即可
            return false;
        }
    }

    private void safeComplete(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception ignore) {
        }
    }

    private void safeCancel(ScheduledFuture<?> future) {
        if (future == null) {
            return;
        }
        try {
            future.cancel(true);
        } catch (Exception ignore) {
        }
    }

    @PreDestroy
    public void shutdown() {
        // 应用关闭时释放线程池资源
        executorService.shutdownNow();
        keepAliveScheduler.shutdownNow();
    }

    /**
     * 保存单个stream的上下文信息
     */
    private static class StreamContext {
        private final SseEmitter emitter;
        private final AtomicBoolean canceled = new AtomicBoolean(false);
        private volatile ScheduledFuture<?> keepAliveFuture;

        private StreamContext(SseEmitter emitter) {
            this.emitter = emitter;
        }
    }

    /**
     * 线程命名：便于排查线程堆栈与资源占用
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private final String prefix;
        private final ThreadFactory delegate = Executors.defaultThreadFactory();
        private final AtomicInteger index = new AtomicInteger(1);

        private NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = delegate.newThread(r);
            t.setName(prefix + index.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }

}
