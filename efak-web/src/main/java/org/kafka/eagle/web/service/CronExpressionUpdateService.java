package org.kafka.eagle.web.service;

import org.kafka.eagle.dto.scheduler.TaskScheduler;

import java.time.LocalDateTime;

/**
 * <p>
 * Cron表达式更新服务接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/27 02:59:40
 * @version 5.0.0
 */
public interface CronExpressionUpdateService {

    /**
     * 处理Cron表达式更新
     * 
     * @param taskScheduler          更新后的任务调度
     * @param originalCronExpression 原始的Cron表达式
     * @return 是否处理成功
     */
    boolean handleCronExpressionUpdate(TaskScheduler taskScheduler, String originalCronExpression);

    /**
     * 重新计算任务的下次执行时间
     * 
     * @param taskScheduler 任务调度
     * @return 是否计算成功
     */
    boolean recalculateNextExecuteTime(TaskScheduler taskScheduler);

    /**
     * 通知分布式调度器任务配置已更新
     * 
     * @param taskScheduler 任务调度
     */
    void notifyDistributedScheduler(TaskScheduler taskScheduler);

    /**
     * 计算下次执行时间
     * 
     * @param cronExpression  Cron表达式
     * @param lastExecuteTime 上次执行时间
     * @return 下次执行时间
     */
    LocalDateTime calculateNextExecuteTime(String cronExpression, LocalDateTime lastExecuteTime);
}