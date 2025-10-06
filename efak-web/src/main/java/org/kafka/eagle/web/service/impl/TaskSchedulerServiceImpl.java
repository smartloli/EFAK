package org.kafka.eagle.web.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.scheduler.TaskExecutionResult;
import org.kafka.eagle.dto.scheduler.TaskScheduler;
import org.kafka.eagle.web.mapper.TaskExecutionHistoryMapper;
import org.kafka.eagle.web.mapper.TaskSchedulerMapper;
import org.kafka.eagle.web.service.CronExpressionUpdateService;
import org.kafka.eagle.web.service.TaskExecutorManager;
import org.kafka.eagle.web.service.TaskSchedulerService;
import org.kafka.eagle.web.scheduler.UnifiedDistributedScheduler;
import org.kafka.eagle.web.util.TaskUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 任务调度服务接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/08/02 01:25:26
 * @version 5.0.0
 */
@Slf4j
@Service
public class TaskSchedulerServiceImpl implements TaskSchedulerService {

    @Autowired
    private TaskSchedulerMapper taskSchedulerMapper;

    @Autowired
    private TaskExecutionHistoryMapper taskExecutionHistoryMapper;

    @Autowired
    private TaskExecutorManager taskExecutorManager;

    @Autowired
    private CronExpressionUpdateService cronExpressionUpdateService;

    @Autowired
    private UnifiedDistributedScheduler unifiedScheduler;

    @Override
    public Map<String, Object> getTaskSchedulerList(String taskName, String taskType, String status,
            String clusterName, int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("taskName", taskName);
        params.put("taskType", taskType);
        params.put("status", status);
        params.put("clusterName", clusterName);
        params.put("offset", (page - 1) * size);
        params.put("size", size);

        List<TaskScheduler> tasks = taskSchedulerMapper.selectTaskSchedulerList(params);
        int total = taskSchedulerMapper.selectTaskSchedulerCount(params);

        Map<String, Object> result = new HashMap<>();
        result.put("tasks", tasks);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));

        return result;
    }

    @Override
    public TaskScheduler getTaskSchedulerById(Long id) {
        return taskSchedulerMapper.selectTaskSchedulerById(id);
    }

    @Override
    public boolean updateTaskScheduler(TaskScheduler taskScheduler) {
        try {
            // 获取原始任务信息
            TaskScheduler originalTask = taskSchedulerMapper.selectTaskSchedulerById(taskScheduler.getId());
            if (originalTask == null) {
                return false;
            }

            // 检查Cron表达式是否被修改
            boolean cronExpressionChanged = !originalTask.getCronExpression().equals(taskScheduler.getCronExpression());

            taskScheduler.setUpdateTime(LocalDateTime.now());

            // 如果Cron表达式被修改，需要重新计算下次执行时间
            if (cronExpressionChanged) {
                // 使用专门的Cron表达式更新服务
                boolean updateSuccess = cronExpressionUpdateService.handleCronExpressionUpdate(
                        taskScheduler, originalTask.getCronExpression());

                if (!updateSuccess) {
                    log.error("处理Cron表达式更新失败: {}", taskScheduler.getTaskName());
                    return false;
                }
            }

            // 使用安全的更新方法，只更新允许修改的字段
            boolean success = taskSchedulerMapper.updateTaskSchedulerSafe(taskScheduler) > 0;

            return success;
        } catch (Exception e) {
            log.error("更新任务调度失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean enableTask(Long id) {
        try {
            String updateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            int result = taskSchedulerMapper.updateTaskStatus(id, "enabled", updateTime);
            return result > 0;
        } catch (Exception e) {
            log.error("启用任务失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean disableTask(Long id) {
        try {
            String updateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            int result = taskSchedulerMapper.updateTaskStatus(id, "disabled", updateTime);
            return result > 0;
        } catch (Exception e) {
            log.error("禁用任务失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean executeTaskNow(Long id) {
        try {

            // 获取任务信息
            TaskScheduler task = taskSchedulerMapper.selectTaskSchedulerById(id);
            if (task == null) {
                log.warn("任务不存在，任务ID: {}", id);
                return false;
            }

            // 检查任务状态
            if (!"enabled".equals(task.getStatus())) {
                log.warn("任务状态为 {}，无法执行，任务ID: {}", task.getStatus(), id);
                return false;
            }

            // 使用统一调度器执行任务
            boolean success = unifiedScheduler.triggerTask(id);

            return success;

        } catch (Exception e) {
            log.error("执行任务失败，任务ID: {}", id, e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getTaskStats() {
        return taskSchedulerMapper.selectTaskStats();
    }

    @Override
    public List<TaskScheduler> getEnabledTasks() {
        return taskSchedulerMapper.selectEnabledTasks();
    }

    @Override
    public boolean updateTaskExecuteResult(Long id, boolean success, String result, String errorMessage) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String nextExecuteTime = LocalDateTime.now().plusMinutes(5)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return taskSchedulerMapper.updateTaskExecuteResult(id, now, nextExecuteTime,
                success ? 1 : 0, success ? 0 : 1, result, errorMessage, now) > 0;
    }

    @Override
    public TaskScheduler getTaskSchedulerByName(String taskName) {
        return taskSchedulerMapper.selectTaskSchedulerByName(taskName);
    }

    @Override
    public boolean isTaskNameExists(String taskName) {
        return taskSchedulerMapper.countByTaskName(taskName) > 0;
    }

    @Override
    public List<TaskScheduler> getTasksByType(String taskType) {
        return taskSchedulerMapper.selectTasksByType(taskType);
    }

    @Override
    public List<TaskScheduler> getTasksByCluster(String clusterName) {
        return taskSchedulerMapper.selectTasksByCluster(clusterName);
    }

    @Override
    public Map<String, Object> getTaskExecutionHistory(Long taskId, int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("taskId", taskId);
        params.put("offset", (page - 1) * size);
        params.put("size", size);

        List<Map<String, Object>> history = taskExecutionHistoryMapper.selectTaskExecutionHistory(params);
        
        // 处理执行时间字段，使用计算出的duration作为执行时间
        for (Map<String, Object> record : history) {
            Object calculatedDuration = record.get("calculated_duration");
            if (calculatedDuration != null) {
                record.put("duration", calculatedDuration);
            }
            // 移除临时字段
            record.remove("calculated_duration");
        }
        
        int total = taskExecutionHistoryMapper.selectTaskExecutionHistoryCount(taskId);

        Map<String, Object> result = new HashMap<>();
        result.put("history", history);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));

        return result;
    }

    @Override
    public Map<String, Object> getAllTaskExecutionHistory(String taskName, String status,
            String startDate, String endDate, int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("taskName", taskName);
        params.put("status", status);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("offset", (page - 1) * size);
        params.put("size", size);

        List<Map<String, Object>> history = taskExecutionHistoryMapper.selectAllTaskExecutionHistory(params);
        
        // 处理执行时间字段，使用计算出的duration作为执行时间
        for (Map<String, Object> record : history) {
            Object calculatedDuration = record.get("calculated_duration");
            if (calculatedDuration != null) {
                record.put("duration", calculatedDuration);
            }
            // 移除临时字段
            record.remove("calculated_duration");
        }
        
        int total = taskExecutionHistoryMapper.selectAllTaskExecutionHistoryCount(params);

        Map<String, Object> result = new HashMap<>();
        result.put("history", history);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));

        return result;
    }

    @Override
    public Map<String, Object> getTaskExecutionHistoryDetail(Long id) {
        Map<String, Object> record = taskExecutionHistoryMapper.selectTaskExecutionHistoryById(id);
        
        if (record != null) {
            // 处理执行时间字段，使用计算出的duration作为执行时间
            Object calculatedDuration = record.get("calculated_duration");
            if (calculatedDuration != null) {
                record.put("duration", calculatedDuration);
            }
            // 移除临时字段
            record.remove("calculated_duration");
        }
        
        return record;
    }

    @Override
    public boolean cleanExpiredTaskRecords(int daysToKeep) {
        return taskSchedulerMapper.deleteExpiredTasks(daysToKeep) > 0;
    }

    @Override
    public boolean validateCronExpression(String cronExpression) {
        return TaskUtils.validateCronExpression(cronExpression);
    }

    @Override
    public List<Map<String, String>> getTaskTypeList() {
        List<String> taskTypes = taskSchedulerMapper.selectTaskTypes();
        List<Map<String, String>> result = new ArrayList<>();

        for (String type : taskTypes) {
            Map<String, String> typeInfo = new HashMap<>();
            typeInfo.put("value", type);
            typeInfo.put("label", TaskUtils.getTaskTypeName(type));
            typeInfo.put("description", TaskUtils.getTaskTypeDescription(type));
            result.add(typeInfo);
        }

        return result;
    }

    @Override
    public List<String> getClusterNameList() {
        return taskSchedulerMapper.selectClusterNames();
    }

    /**
     * 记录任务执行开始
     */
    public Long recordTaskExecutionStart(Long taskId, String taskName, String taskType, String clusterName) {
        Map<String, Object> history = new HashMap<>();
        history.put("taskId", taskId);
        history.put("taskName", taskName);
        history.put("taskType", taskType);
        history.put("clusterName", clusterName);
        history.put("startTime", LocalDateTime.now());
        history.put("executionStatus", "RUNNING");
        history.put("triggerType", "MANUAL");
        history.put("executorNode", "manual-executor");
        history.put("createdTime", LocalDateTime.now());

        taskExecutionHistoryMapper.insertTaskExecutionHistory(history);

        // 修复：安全地处理自增主键的类型转换
        Object idObj = history.get("id");
        if (idObj instanceof BigInteger) {
            return ((BigInteger) idObj).longValue();
        } else if (idObj instanceof Long) {
            return (Long) idObj;
        } else if (idObj instanceof Number) {
            return ((Number) idObj).longValue();
        } else {
            log.warn("Unexpected ID type: {}, value: {}", idObj != null ? idObj.getClass().getName() : "null", idObj);
            return null;
        }
    }

    /**
     * 记录任务执行结束
     */
    public void recordTaskExecutionEnd(Long executionId, boolean success, String result, String errorMessage) {
        LocalDateTime endTime = LocalDateTime.now();

        // 获取开始时间来计算持续时间
        Map<String, Object> existingHistory = taskExecutionHistoryMapper.selectTaskExecutionHistoryById(executionId);
        Long duration = null;
        if (existingHistory != null && existingHistory.get("start_time") != null) {
            LocalDateTime startTime = (LocalDateTime) existingHistory.get("start_time");
            duration = java.time.Duration.between(startTime, endTime).toMillis();
        }

        Map<String, Object> history = new HashMap<>();
        history.put("id", executionId);
        history.put("endTime", endTime);
        history.put("executionStatus", success ? "SUCCESS" : "FAILED");
        history.put("resultMessage", result);
        history.put("errorMessage", errorMessage);
        history.put("duration", duration);

        taskExecutionHistoryMapper.updateTaskExecutionHistory(history);
    }
}