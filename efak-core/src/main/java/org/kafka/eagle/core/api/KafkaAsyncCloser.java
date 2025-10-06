package org.kafka.eagle.core.api;

import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.*;

/**
 * <p>
 * 异步关闭Kafka消费者资源以提高查询执行性能。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/8/23 21:47:55
 * @version 1.0
 */
@Slf4j
public class KafkaAsyncCloser implements AutoCloseable {

    private volatile ExecutorService executorService;

    /**
     * 在单独的线程中关闭给定资源。
     *
     * @param autoCloseable 要关闭的资源
     */
    public void close(AutoCloseable autoCloseable) {
        if (autoCloseable != null) {
            ExecutorService executorService = executorService();
            executorService.submit(() -> {
                try {
                    autoCloseable.close();
                } catch (Exception e) {
                    log.error("资源 {} 关闭失败: {}", autoCloseable.getClass().getCanonicalName(), e.getMessage());
                }
            });
        }
    }

    @Override
    public void close() throws Exception {
        if (executorService != null) {
            log.trace("正在关闭Kafka异步关闭器: {}", executorService);
            executorService.shutdownNow();
        }
    }

    /**
     * 使用DCL初始化执行器服务实例。
     *
     * @return 执行器服务实例
     */
    private ExecutorService executorService() {
        if (executorService == null) {
            synchronized (this) {
                if (executorService == null) {
                    this.executorService = new ThreadPoolExecutor(0, 1, 0L,
                            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new KafkaThreadFactory());
                }
            }
        }
        return executorService;
    }

    /**
     * 线程工厂，为线程名称添加Kafka关闭器前缀。
     */
    private static class KafkaThreadFactory implements ThreadFactory {

        private static final String THREAD_PREFIX = "efak-kafka-closer-";
        private final ThreadFactory delegate = Executors.defaultThreadFactory();

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = delegate.newThread(r);
            thread.setName(THREAD_PREFIX + thread.getName());
            return thread;
        }
    }
}
