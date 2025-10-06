package org.kafka.eagle.web.service;

import org.kafka.eagle.dto.scheduler.TaskScheduler;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 任务调度服务接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/20 00:48:25
 * @version 5.0.0
 */
public interface TaskSchedulerService {

    /**
     * 获取任务调度列表
     */
    Map<String, Object> getTaskSchedulerList(String taskName, String taskType, String status,
            String clusterName, int page, int size);

    /**
     * 根据ID获取任务调度
     */
    TaskScheduler getTaskSchedulerById(Long id);

    /**
     * 更新任务调度
     */
    boolean updateTaskScheduler(TaskScheduler taskScheduler);

    /**
     * 启用任务
     */
    boolean enableTask(Long id);

    /**
     * 禁用任务
     */
    boolean disableTask(Long id);

    /**
     * 立即执行任务
     */
    boolean executeTaskNow(Long id);

    /**
     * 获取任务统计信息
     */
    Map<String, Object> getTaskStats();

    /**
     * 获取启用的任务列表
     */
    List<TaskScheduler> getEnabledTasks();

    /**
     * 更新任务执行结果
     */
    boolean updateTaskExecuteResult(Long id, boolean success, String result, String errorMessage);

    /**
     * 根据任务名称获取任务调度
     */
    TaskScheduler getTaskSchedulerByName(String taskName);

    /**
     * 检查任务名称是否存在
     */
    boolean isTaskNameExists(String taskName);

    /**
     * 根据任务类型获取任务列表
     */
    List<TaskScheduler> getTasksByType(String taskType);

    /**
     * 根据集群名称获取任务列表
     */
    List<TaskScheduler> getTasksByCluster(String clusterName);

    /**
     * 获取任务执行历史
     */
    Map<String, Object> getTaskExecutionHistory(Long taskId, int page, int size);

    /**
     * 获取所有任务执行历史
     */
    Map<String, Object> getAllTaskExecutionHistory(String taskName, String status,
            String startDate, String endDate, int page, int size);

    /**
     * 获取任务执行历史详情
     */
    Map<String, Object> getTaskExecutionHistoryDetail(Long id);

    /**
     * 清理过期的任务记录
     */
    boolean cleanExpiredTaskRecords(int daysToKeep);

    /**
     * 验证Cron表达式
     */
    boolean validateCronExpression(String cronExpression);

    /**
     * 获取任务类型列表
     */
    List<Map<String, String>> getTaskTypeList();

    /**
     * 获取集群名称列表
     */
    List<String> getClusterNameList();
}