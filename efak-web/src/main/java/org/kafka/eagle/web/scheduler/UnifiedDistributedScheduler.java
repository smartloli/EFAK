package org.kafka.eagle.web.scheduler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.scheduler.TaskExecutionResult;
import org.kafka.eagle.dto.scheduler.TaskScheduler;
import org.kafka.eagle.web.config.EfakRuntimeProperties;
import org.kafka.eagle.web.mapper.TaskExecutionHistoryMapper;
import org.kafka.eagle.web.mapper.TaskSchedulerMapper;
import org.kafka.eagle.web.service.CronExpressionUpdateService;
import org.kafka.eagle.web.service.TaskExecutorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * UnifiedDistributed 调度器
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/30 01:14:40
 * @version 5.0.0
 */
@Slf4j
@Service
public class UnifiedDistributedScheduler {

    @Autowired
    private TaskSchedulerMapper taskSchedulerMapper;

    @Autowired
    private TaskExecutionHistoryMapper taskExecutionHistoryMapper;

    @Autowired
    private TaskExecutorManager taskExecutorManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private DistributedTaskCoordinator taskCoordinator;

    @Autowired
    private CronExpressionUpdateService cronExpressionUpdateService;

    @Autowired
    private EfakRuntimeProperties runtimeProperties;

    private final AtomicBoolean schedulerEnabled = new AtomicBoolean(true);
    private final ScheduledExecutorService schedulerExecutor = newScheduler();

    private static ScheduledExecutorService newScheduler() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2, namedThreadFactory("efak-sched"));
        executor.setRemoveOnCancelPolicy(true);
        return executor;
    }
    private final ExecutorService taskExecutor = new ThreadPoolExecutor(
            2, 8, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(32),
            namedThreadFactory("efak-task"),
            new ThreadPoolExecutor.CallerRunsPolicy());
    private final Map<Long, Future<?>> runningTasks = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<Long, TaskScheduler> registeredTasks = new java.util.concurrent.ConcurrentHashMap<>();

    private static final String CRON_UPDATE_NOTIFICATION_KEY = "efak:unified:cron:update:";

    // 支持的任务类型
    private static final List<String> SUPPORTED_TASK_TYPES = List.of(
            "topic_monitor",
            "consumer_monitor",
            "cluster_monitor",
            "alert_monitor",
            "data_cleanup",
            "performance_stats");

    @PostConstruct
    public void init() {
        try {
            taskCoordinator.initializeNode();
            startNodeHeartbeat();
            if (!runtimeProperties.isWorker()) {
                schedulerEnabled.set(false);
                log.info("Process role={} registers for cluster view but skips collector scheduling",
                        runtimeProperties.normalizedRole());
                return;
            }
            startCronExpressionChangeListener();
        } catch (Exception e) {
            log.error("统一分布式任务调度器初始化失败", e);
        }
    }

    @PreDestroy
    public void destroy() {
        log.info("销毁统一分布式任务调度器");
        stopScheduler();
        schedulerExecutor.shutdown();
        taskExecutor.shutdown();
    }

    private static ThreadFactory namedThreadFactory(String prefix) {
        AtomicInteger sequence = new AtomicInteger(1);
        return runnable -> {
            Thread thread = new Thread(runnable, prefix + "-" + sequence.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
    }

    /**
     * 启动Cron表达式变化监听器
     */
    private void startCronExpressionChangeListener() {
        schedulerExecutor.scheduleAtFixedRate(() -> {
            try {
                checkCronExpressionUpdates();
            } catch (Exception e) {
                log.error("检查Cron表达式更新失败", e);
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    /**
     * 启动节点心跳
     */
    private void startNodeHeartbeat() {
        schedulerExecutor.scheduleAtFixedRate(() -> {
            try {
                taskCoordinator.updateHeartbeat();
                taskCoordinator.cleanupOfflineServices();
            } catch (Exception e) {
                log.error("Node heartbeat update failed", e);
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    /**
     * 扫描并执行任务
     */
    @Scheduled(fixedRate = 60000)
    public void scanAndExecuteTasks() {
        if (!schedulerEnabled.get()) {
            return;
        }

        try {
            List<TaskScheduler> enabledTasks = getEnabledTasksFromDatabase();
            for (TaskScheduler task : enabledTasks) {
                if (!shouldExecuteTask(task)) {
                    continue;
                }
                executeTask(task);
            }
        } catch (Exception e) {
            log.error("Failed to scan scheduled tasks", e);
        }
    }

    /**
     * 从数据库获取启用的任务
     */
    private List<TaskScheduler> getEnabledTasksFromDatabase() {
        try {
            List<TaskScheduler> enabledTasks = taskSchedulerMapper.selectEnabledTasks();
            if (enabledTasks.isEmpty()) {
                return new ArrayList<>();
            }

            // 过滤出支持的任务类型
            List<TaskScheduler> supportedTasks = new ArrayList<>();
            int skippedTasks = 0;

            for (TaskScheduler task : enabledTasks) {
                if (SUPPORTED_TASK_TYPES.contains(task.getTaskType())) {
                    supportedTasks.add(task);
                } else {
                    skippedTasks++;
                    log.warn("跳过不支持的任务类型: 任务ID={}, 任务名称={}, 任务类型={}",
                            task.getId(), task.getTaskName(), task.getTaskType());
                }
            }

            if (skippedTasks > 0) {
                log.warn("部分任务被跳过: 启用任务数={}, 支持的任务数={}, 跳过的任务数={}",
                        enabledTasks.size(), supportedTasks.size(), skippedTasks);
            } else {
            }

            return supportedTasks;

        } catch (Exception e) {
            log.error("从数据库读取启用任务时发生异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 判断任务是否应该执行
     */
    private boolean shouldExecuteTask(TaskScheduler task) {
        String openRound = taskCoordinator.getOpenRoundId(task.getId());
        if (openRound != null && !taskCoordinator.hasCompletedRound(task.getId(), openRound)) {
            return true;
        }
        return isTaskDue(task);
    }

    private boolean isTaskDue(TaskScheduler task) {
        if (task.getLastExecuteTime() == null) {
            return true;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextExecuteTime = calculateNextExecuteTime(task.getLastExecuteTime(), task.getCronExpression());
        return !now.isBefore(nextExecuteTime);
    }

    /**
     * 计算下次执行时间
     */
    private LocalDateTime calculateNextExecuteTime(LocalDateTime lastExecuteTime, String cronExpression) {
        try {
            // 使用CronExpressionUpdateService计算下次执行时间
            return cronExpressionUpdateService.calculateNextExecuteTime(cronExpression, lastExecuteTime);
        } catch (Exception e) {
            log.error("计算下次执行时间失败: {}", e.getMessage());
            // 默认1小时后执行
            return lastExecuteTime.plusHours(1);
        }
    }

    /**
     * 执行任务
     */
    private void executeTask(TaskScheduler task) {
        executeTaskWithTriggerType(task, "SCHEDULED");
    }

    /**
     * 手动执行任务
     */
    private void executeTaskManually(TaskScheduler task) {
        executeTaskWithTriggerType(task, "MANUAL");
    }

    /**
     * 执行任务（通用方法）
     */
    private void executeTaskWithTriggerType(TaskScheduler task, String triggerType) {
        if (!runtimeProperties.isWorker()) {
            log.warn("Process role={} cannot execute collector task {}",
                    runtimeProperties.normalizedRole(), task.getTaskName());
            return;
        }
        if (runningTasks.containsKey(task.getId())) {
            log.warn("Task {} is already running on this node, skip", task.getTaskName());
            return;
        }

        String roundId = resolveRoundId(task, triggerType);
        if (!"MANUAL".equals(triggerType) && taskCoordinator.hasCompletedRound(task.getId(), roundId)) {
            return;
        }

        if (!taskCoordinator.acquireTaskLock(task.getTaskType(), 600)) {
            log.warn("Task {} is locked on this node, skip", task.getTaskName());
            return;
        }

        boolean submitted = false;
        try {
        Future<?> future = taskExecutor.submit(() -> {
            Long executionId = null;
            ScheduledFuture<?> renewFuture = schedulerExecutor.scheduleAtFixedRate(
                    () -> taskCoordinator.renewTaskLock(task.getTaskType(), 600),
                    60, 60, TimeUnit.SECONDS);
            try {
                executionId = recordTaskExecutionStart(task, triggerType);
                TaskExecutionResult result = taskExecutorManager.executeTask(task);
                recordTaskExecutionEnd(executionId, result);
                recordShardProgress(task, result);

                if (!"MANUAL".equals(triggerType)) {
                    taskCoordinator.markRoundDone(task.getId(), roundId);
                    if (taskCoordinator.tryClaimStatsUpdate(task.getId(), roundId)) {
                        updateTaskStatus(task, result.isSuccess() ? "SUCCESS" : "FAILED");
                    }
                } else if (taskCoordinator.tryClaimStatsUpdate(task.getId(), "manual-" + System.currentTimeMillis())) {
                    updateTaskStatus(task, result.isSuccess() ? "SUCCESS" : "FAILED");
                }
            } catch (Exception e) {
                log.error("Task {} failed", task.getTaskName(), e);
                if (executionId != null) {
                    recordTaskExecutionEnd(executionId, createErrorResult(e.getMessage()));
                }
                if (!"MANUAL".equals(triggerType)) {
                    taskCoordinator.markRoundDone(task.getId(), roundId);
                    if (taskCoordinator.tryClaimStatsUpdate(task.getId(), roundId)) {
                        updateTaskStatus(task, "FAILED");
                    }
                }
            } finally {
                renewFuture.cancel(false);
                runningTasks.remove(task.getId());
                taskCoordinator.releaseTaskLock(task.getTaskType());
            }
        });

        runningTasks.put(task.getId(), future);
        submitted = true;
        } finally {
            if (!submitted) {
                taskCoordinator.releaseTaskLock(task.getTaskType());
            }
        }
    }

    private String resolveRoundId(TaskScheduler task, String triggerType) {
        if ("MANUAL".equals(triggerType)) {
            return "manual-" + System.currentTimeMillis();
        }
        if (isTaskDue(task)) {
            String roundId = buildRoundId(task);
            taskCoordinator.openRoundWindow(task.getId(), roundId, 120);
            return roundId;
        }
        String openRound = taskCoordinator.getOpenRoundId(task.getId());
        return openRound != null ? openRound : buildRoundId(task);
    }

    private String buildRoundId(TaskScheduler task) {
        return LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
    }

    /**
     * Snapshot shard results already written to Redis. Does not sleep or
     * delete other nodes' keys; MySQL is the source of truth.
     */
    private void recordShardProgress(TaskScheduler task, TaskExecutionResult result) {
        try {
            Map<String, Object> shardResults = taskCoordinator.getAllShardResults(task.getTaskType());
            log.info("Task {} finished on node {}, shard reports so far: {}",
                    task.getTaskName(), taskCoordinator.getCurrentNodeId(), shardResults.size());
            if (result != null && result.getData() != null) {
                result.getData().put("shardReportCount", shardResults.size());
            }
        } catch (Exception e) {
            log.warn("Failed to read shard progress for {}", task.getTaskName(), e);
        }
    }

    /**
     * 记录任务执行开始
     */
    private Long recordTaskExecutionStart(TaskScheduler task, String triggerType) {
        try {
            Map<String, Object> history = new HashMap<>();
            history.put("taskId", task.getId());
            history.put("taskName", task.getTaskName());
            history.put("taskType", task.getTaskType());
            history.put("clusterName", task.getClusterName());
            history.put("executionStatus", "RUNNING");
            history.put("startTime", LocalDateTime.now());
            history.put("executorNode", getCurrentNodeId());
            history.put("triggerType", triggerType);
            history.put("createdTime", LocalDateTime.now());

            taskExecutionHistoryMapper.insertTaskExecutionHistory(history);

            // 安全地处理自增主键的类型转换
            Object idObj = history.get("id");
            if (idObj instanceof BigInteger) {
                return ((BigInteger) idObj).longValue();
            } else if (idObj instanceof Long) {
                return (Long) idObj;
            } else if (idObj instanceof Number) {
                return ((Number) idObj).longValue();
            } else {
                log.warn("Unexpected ID type: {}, value: {}",
                        idObj != null ? idObj.getClass().getName() : "null", idObj);
                return null;
            }
        } catch (Exception e) {
            log.error("记录任务执行开始失败", e);
            return null;
        }
    }

    /**
     * 记录任务执行结束
     */
    private void recordTaskExecutionEnd(Long executionId, TaskExecutionResult result) {
        if (executionId == null) {
            log.warn("executionId为null，无法记录任务执行结束");
            return;
        }

        try {
            Map<String, Object> history = new HashMap<>();
            history.put("id", executionId);
            history.put("endTime", LocalDateTime.now());
            history.put("executionStatus", result.isSuccess() ? "SUCCESS" : "FAILED");
            history.put("resultMessage", result.getResult());
            history.put("errorMessage", result.getErrorMessage());
            // 设置执行时长
            Long duration = result.getDuration();
            if (duration == null || duration <= 0) {
                // 如果result中没有duration，尝试从数据库计算
                duration = calculateDurationFromDatabase(executionId);
            }
            history.put("duration", duration);

            int updateResult = taskExecutionHistoryMapper.updateTaskExecutionHistory(history);
            if (updateResult > 0) {
            } else {
                log.warn("更新任务执行历史记录失败，影响行数为0，executionId: {}", executionId);
            }
        } catch (Exception e) {
            log.error("记录任务执行结束失败，executionId: {}", executionId, e);
        }
    }

    /**
     * 从数据库计算执行时长
     */
    private Long calculateDurationFromDatabase(Long executionId) {
        try {
            Map<String, Object> record = taskExecutionHistoryMapper.selectTaskExecutionHistoryById(executionId);
            if (record != null && record.get("start_time") != null) {
                LocalDateTime startTime = (LocalDateTime) record.get("start_time");
                LocalDateTime endTime = LocalDateTime.now();
                return java.time.Duration.between(startTime, endTime).toMillis();
            }
        } catch (Exception e) {
            log.warn("从数据库计算执行时长失败，executionId: {}", executionId, e);
        }
        return null;
    }



    /**
     * 创建错误结果
     */
    private TaskExecutionResult createErrorResult(String errorMessage) {
        TaskExecutionResult result = new TaskExecutionResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        return result;
    }

    /**
     * 更新任务状态
     */
    private void updateTaskStatus(TaskScheduler task, String status) {
        try {
            LocalDateTime now = LocalDateTime.now();

            // 计算下次执行时间
            LocalDateTime nextExecuteTime = calculateNextExecuteTime(now, task.getCronExpression());

            // 格式化时间字符串
            String lastExecuteTimeStr = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String nextExecuteTimeStr = nextExecuteTime
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String updateTimeStr = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 确定成功和失败的增量
            int successIncrement = "SUCCESS".equals(status) ? 1 : 0;
            int failIncrement = "FAILED".equals(status) ? 1 : 0;

            // 设置执行结果信息
            String lastExecuteResult = "SUCCESS".equals(status) ? "执行成功" : "执行失败";
            String errorMessage = "FAILED".equals(status) ? "任务执行失败" : null;

            // 使用updateTaskExecuteResult方法更新执行统计
            int updateResult = taskSchedulerMapper.updateTaskExecuteResult(
                    task.getId(),
                    lastExecuteTimeStr,
                    nextExecuteTimeStr,
                    successIncrement,
                    failIncrement,
                    lastExecuteResult,
                    errorMessage,
                    updateTimeStr);

            if (updateResult > 0) {
                // 更新本地对象的统计信息
                task.setLastExecuteTime(now);
                task.setNextExecuteTime(nextExecuteTime);
                task.setExecuteCount(task.getExecuteCount() + 1);
                if ("SUCCESS".equals(status)) {
                    task.setSuccessCount(task.getSuccessCount() + 1);
                } else if ("FAILED".equals(status)) {
                    task.setFailCount(task.getFailCount() + 1);
                }

            } else {
                log.warn("任务 {} 执行统计更新失败，数据库更新返回0行", task.getTaskName());
            }
        } catch (Exception e) {
            log.error("更新任务状态失败: 任务={}, 状态={}", task.getTaskName(), status, e);
        }
    }

    /**
     * 检查Cron表达式更新
     */
    private void checkCronExpressionUpdates() {
        try {
            String notification = (String) redisTemplate.opsForValue().get(CRON_UPDATE_NOTIFICATION_KEY);
            if (notification != null) {
                handleCronExpressionUpdate(notification);
                redisTemplate.delete(CRON_UPDATE_NOTIFICATION_KEY);
            }
        } catch (Exception e) {
            log.error("检查Cron表达式更新失败", e);
        }
    }

    /**
     * 处理Cron表达式更新
     */
    private void handleCronExpressionUpdate(String notification) {
        try {
            // 解析通知中的任务ID
            Long taskId = Long.parseLong(notification);
            TaskScheduler task = taskSchedulerMapper.selectTaskSchedulerById(taskId);

            if (task != null) {
                // 由于没有原始Cron表达式，我们使用当前任务的Cron表达式作为原始值
                cronExpressionUpdateService.handleCronExpressionUpdate(task, task.getCronExpression());
            }
        } catch (Exception e) {
            log.error("处理Cron表达式更新失败", e);
        }
    }

    private String getCurrentNodeId() {
        return taskCoordinator.getCurrentNodeId();
    }

    /**
     * 启动调度器
     */
    public void startScheduler() {
        if (!runtimeProperties.isWorker()) {
            schedulerEnabled.set(false);
            log.info("Process role={} does not start the collector scheduler",
                    runtimeProperties.normalizedRole());
            return;
        }
        schedulerEnabled.set(true);
    }

    /**
     * 停止调度器
     */
    public void stopScheduler() {
        schedulerEnabled.set(false);

        // 停止所有正在运行的任务
        for (Map.Entry<Long, Future<?>> entry : runningTasks.entrySet()) {
            entry.getValue().cancel(false);
            log.info("停止任务: {}", entry.getKey());
        }
        runningTasks.clear();

        log.info("统一分布式任务调度器已停止");
    }

    /**
     * 手动触发任务执行
     */
    public boolean triggerTask(Long taskId) {
        try {
            TaskScheduler task = taskSchedulerMapper.selectTaskSchedulerById(taskId);
            if (task == null) {
                log.warn("任务不存在: {}", taskId);
                return false;
            }

            executeTaskManually(task);
            return true;
        } catch (Exception e) {
            log.error("手动触发任务失败", e);
            return false;
        }
    }

    /**
     * 获取调度器状态
     */
    public Map<String, Object> getSchedulerStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", schedulerEnabled.get());
        status.put("runningTasks", runningTasks.size());
        status.put("registeredTasks", registeredTasks.size());
        status.put("nodeId", runtimeProperties.isWorker() ? getCurrentNodeId() : "-");
        status.put("role", runtimeProperties.normalizedRole());
        status.put("timestamp", LocalDateTime.now());
        return status;
    }

    /**
     * 获取运行中的任务
     */
    public List<Map<String, Object>> getRunningTasks() {
        List<Map<String, Object>> tasks = new ArrayList<>();
        for (Map.Entry<Long, Future<?>> entry : runningTasks.entrySet()) {
            Map<String, Object> taskInfo = new HashMap<>();
            taskInfo.put("taskId", entry.getKey());
            taskInfo.put("cancelled", entry.getValue().isCancelled());
            taskInfo.put("done", entry.getValue().isDone());
            tasks.add(taskInfo);
        }
        return tasks;
    }

    /**
     * 每天0点重置计数器
     */
    @Scheduled(cron = "0 0 0 * * ?") // 每天0点执行
    public void resetDailyCounters() {
        if (!schedulerEnabled.get()) {
            return;
        }

        try {

            // 重置所有任务的每日统计计数器
            int resetCount = taskSchedulerMapper.resetDailyTaskStats();

        } catch (Exception e) {
            log.error("重置每日计数器失败", e);
        }
    }

    /**
     * 设置调度器启用状态
     */
    public void setSchedulerEnabled(boolean enabled) {
        schedulerEnabled.set(enabled);
        log.info("调度器状态设置为: {}", enabled ? "启用" : "禁用");
    }
}