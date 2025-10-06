package org.kafka.eagle.web.config;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.web.scheduler.UnifiedDistributedScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 统一分布式任务调度初始化器
 * 在Spring Boot启动时自动初始化统一的分布式任务调度系统，合并所有分布式调度初始化逻辑
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/08/24 16:03:36
 * @version 5.0.0
 */
@Slf4j
@Component
public class UnifiedDistributedSchedulerInitializer implements CommandLineRunner {

    @Autowired
    private UnifiedDistributedScheduler unifiedScheduler;

    @Override
    public void run(String... args) throws Exception {
        try {
            // 启动统一分布式任务调度器
            startUnifiedDistributedScheduler();
        } catch (Exception e) {
            log.error("统一分布式任务调度系统初始化失败", e);
        }
    }

    /**
     * 启动统一分布式任务调度器
     */
    private void startUnifiedDistributedScheduler() {
        try {
            // 启动统一分布式任务调度器
            unifiedScheduler.startScheduler();
            // 获取并打印调度器状态
            var status = unifiedScheduler.getSchedulerStatus();
            log.info("统一分布式任务调度器状态: {}", status);
        } catch (Exception e) {
            log.error("启动统一分布式任务调度器失败", e);
        }
    }
}