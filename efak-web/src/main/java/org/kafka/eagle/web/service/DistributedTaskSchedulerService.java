package org.kafka.eagle.web.service;

import org.kafka.eagle.dto.scheduler.TaskScheduler;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 分布式任务调度服务接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/19 00:35:12
 * @version 5.0.0
 */
public interface DistributedTaskSchedulerService {

    /**
     * 启动分布式任务调度器
     */
    void startScheduler();

    /**
     * 停止分布式任务调度器
     */
    void stopScheduler();

    /**
     * 注册任务到调度器
     */
    boolean registerTask(TaskScheduler task);

    /**
     * 取消任务注册
     */
    boolean unregisterTask(Long taskId);

    /**
     * 更新任务调度
     */
    boolean updateTaskSchedule(TaskScheduler task);

    /**
     * 立即执行任务
     */
    boolean executeTaskNow(Long taskId);

    /**
     * 获取调度器状态
     */
    Map<String, Object> getSchedulerStatus();

    /**
     * 获取所有已注册的任务
     */
    List<TaskScheduler> getRegisteredTasks();

    /**
     * 获取任务执行计划
     */
    Map<String, Object> getTaskExecutionPlan(Long taskId);

    /**
     * 获取任务执行历史
     */
    Map<String, Object> getTaskExecutionHistory(Long taskId, int page, int size);

    /**
     * 获取分布式节点信息
     */
    Map<String, Object> getDistributedNodes();

    /**
     * 获取任务统计信息
     */
    Map<String, Object> getTaskStatistics();

    /**
     * 清理过期的执行记录
     */
    boolean cleanupExpiredRecords(int daysToKeep);

    /**
     * 验证任务配置
     */
    boolean validateTaskConfiguration(TaskScheduler task);

    /**
     * 获取任务执行日志
     */
    List<Map<String, Object>> getTaskExecutionLogs(Long taskId, int limit);

    /**
     * 暂停任务调度
     */
    boolean pauseTask(Long taskId);

    /**
     * 恢复任务调度
     */
    boolean resumeTask(Long taskId);

    /**
     * 获取任务执行状态
     */
    Map<String, Object> getTaskExecutionStatus(Long taskId);
}