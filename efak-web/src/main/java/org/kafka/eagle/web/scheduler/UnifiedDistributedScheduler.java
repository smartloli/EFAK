package org.kafka.eagle.web.scheduler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.scheduler.TaskExecutionResult;
import org.kafka.eagle.dto.scheduler.TaskScheduler;
import org.kafka.eagle.web.config.DistributedTaskConfig;
import org.kafka.eagle.web.mapper.TaskExecutionHistoryMapper;
import org.kafka.eagle.web.mapper.TaskSchedulerMapper;
import org.kafka.eagle.web.service.CronExpressionUpdateService;
import org.kafka.eagle.web.service.ShardResultAggregationService;
import org.kafka.eagle.web.service.TaskExecutorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private ShardResultAggregationService shardResultAggregationService;

    @Autowired
    private DistributedTaskConfig taskConfig;

    @Autowired
    private Environment environment;

    @Value("${server.port:8080}")
    private int serverPort;

    // 调度器状态
    private final AtomicBoolean schedulerEnabled = new AtomicBoolean(true);
    private final ScheduledExecutorService schedulerExecutor = Executors.newScheduledThreadPool(10);
    private final Map<Long, Future<?>> runningTasks = new ConcurrentHashMap<>();
    private final Map<Long, TaskScheduler> registeredTasks = new ConcurrentHashMap<>();

    // Redis键前缀
    private static final String TASK_LOCK_KEY = "efak:unified:scheduler:lock";
    private static final String TASK_STATS_KEY = "efak:unified:scheduler:stats";
    private static final String NODE_REGISTRY_KEY = "efak:unified:nodes";
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
            // 初始化分布式任务协调器
            taskCoordinator.initializeNode();

            // 启动任务扫描
            startTaskScanner();

            // 启动Cron表达式变化监听器
            startCronExpressionChangeListener();

            // 启动节点心跳
            startNodeHeartbeat();

        } catch (Exception e) {
            log.error("统一分布式任务调度器初始化失败", e);
        }
    }

    @PreDestroy
    public void destroy() {
        log.info("销毁统一分布式任务调度器");
        stopScheduler();
        schedulerExecutor.shutdown();
    }

    /**
     * 启动任务扫描器
     */
    private void startTaskScanner() {
        schedulerExecutor.scheduleAtFixedRate(() -> {
            if (schedulerEnabled.get()) {
                scanAndExecuteTasks();
            }
        }, 0, 60, TimeUnit.SECONDS);
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
                // 使用分布式任务协调器更新心跳
                taskCoordinator.updateHeartbeat();

                // 清理离线服务
                taskCoordinator.cleanupOfflineServices();

                // 保持原有的心跳逻辑作为备份
                updateNodeHeartbeat();
                cleanupOfflineNodes();
            } catch (Exception e) {
                log.error("节点心跳更新失败", e);
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    /**
     * 扫描并执行任务
     */
    @Scheduled(fixedRate = 60000) // 每分钟扫描一次
    public void scanAndExecuteTasks() {
        if (!schedulerEnabled.get()) {
            return;
        }

        // 尝试获取分布式锁
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(TASK_LOCK_KEY,
                getCurrentNodeId(), 60, TimeUnit.SECONDS);

        if (lockAcquired == null || !lockAcquired) {
            return;
        }

        try {

            // 获取启用的任务
            List<TaskScheduler> enabledTasks = getEnabledTasksFromDatabase();

            for (TaskScheduler task : enabledTasks) {
                if (shouldExecuteTask(task)) {
                    executeTask(task);
                } else {
                }
            }

        } catch (Exception e) {
            log.error("扫描任务时发生错误", e);
        } finally {
            // 释放分布式锁
            redisTemplate.delete(TASK_LOCK_KEY);
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
        if (task.getLastExecuteTime() == null) {
            return true; // 首次执行
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextExecuteTime = calculateNextExecuteTime(task.getLastExecuteTime(), task.getCronExpression());

        return now.isAfter(nextExecuteTime) || now.isEqual(nextExecuteTime);
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
        if (runningTasks.containsKey(task.getId())) {
            log.warn("任务 {} 正在执行中，跳过本次执行", task.getTaskName());
            return;
        }

        Future<?> future = schedulerExecutor.submit(() -> {
            Long executionId = null;
            try {
                // 记录任务执行开始
                executionId = recordTaskExecutionStart(task, triggerType);

                // 执行任务
                TaskExecutionResult result = taskExecutorManager.executeTask(task);

                // 记录任务执行结束
                recordTaskExecutionEnd(executionId, result);

                // 汇总分片结果
                aggregateShardResults(task, result);

                // 更新任务状态
                updateTaskStatus(task, result.isSuccess() ? "SUCCESS" : "FAILED");

            } catch (Exception e) {
                log.error("任务 {} 执行异常", task.getTaskName(), e);

                if (executionId != null) {
                    recordTaskExecutionEnd(executionId, createErrorResult(e.getMessage()));
                }

                updateTaskStatus(task, "FAILED");
            } finally {
                runningTasks.remove(task.getId());
            }
        });

        runningTasks.put(task.getId(), future);
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
     * 汇总分片结果
     */
    private void aggregateShardResults(TaskScheduler task, TaskExecutionResult result) {
        try {

            Map<String, Object> aggregatedResult = null;
            String taskType = task.getTaskType();

            // 等待其他节点完成任务，然后汇总结果
            int waitTimeSeconds = taskConfig.getShardResultWaitTime();

            switch (taskType) {
                case "cluster_monitor":
                    aggregatedResult = shardResultAggregationService.aggregateClusterMonitorResults(waitTimeSeconds);
                    break;
                case "topic_monitor":
                    aggregatedResult = shardResultAggregationService.aggregateTopicMonitorResults(waitTimeSeconds);
                    break;
                case "consumer_monitor":
                    aggregatedResult = shardResultAggregationService.aggregateConsumerMonitorResults(waitTimeSeconds);
                    break;
                case "alert_monitor":
                    aggregatedResult = shardResultAggregationService.aggregateAlertMonitorResults(waitTimeSeconds);
                    break;
                case "data_cleanup":
                    aggregatedResult = shardResultAggregationService.aggregateDataCleanupResults(waitTimeSeconds);
                    break;
                case "performance_stats":
                    aggregatedResult = shardResultAggregationService.aggregatePerformanceStatsResults(waitTimeSeconds);
                    break;
                default:
                    log.warn("不支持的任务类型: {}", taskType);
                    return;
            }

            if (aggregatedResult != null) {

                // 清理分片结果
                taskCoordinator.clearShardResults(taskType);
            }

        } catch (Exception e) {
            log.error("汇总任务 {} 分片结果失败", task.getTaskName(), e);
        }
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

    /**
     * 更新节点心跳
     */
    private void updateNodeHeartbeat() {
        try {
            String nodeId = getCurrentNodeId();
            Map<String, Object> nodeInfo = new HashMap<>();
            nodeInfo.put("nodeId", nodeId);
            nodeInfo.put("lastHeartbeat", LocalDateTime.now());
            nodeInfo.put("status", "ONLINE");

            redisTemplate.opsForHash().put(NODE_REGISTRY_KEY, nodeId, nodeInfo);
            redisTemplate.expire(NODE_REGISTRY_KEY, 30, TimeUnit.MINUTES);

        } catch (Exception e) {
            log.error("更新节点心跳失败", e);
        }
    }

    /**
     * 清理离线节点
     */
    private void cleanupOfflineNodes() {
        try {
            Map<Object, Object> nodes = redisTemplate.opsForHash().entries(NODE_REGISTRY_KEY);
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(2);

            for (Map.Entry<Object, Object> entry : nodes.entrySet()) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nodeInfo = (Map<String, Object>) entry.getValue();

                    // 安全地获取lastHeartbeat，处理不同的数据类型
                    LocalDateTime lastHeartbeat = null;
                    Object heartbeatObj = nodeInfo.get("lastHeartbeat");

                    if (heartbeatObj instanceof LocalDateTime) {
                        lastHeartbeat = (LocalDateTime) heartbeatObj;
                    } else if (heartbeatObj instanceof String) {
                        try {
                            lastHeartbeat = LocalDateTime.parse((String) heartbeatObj);
                        } catch (Exception e) {
                            log.warn("无法解析心跳时间字符串: {} - {}", heartbeatObj, e.getMessage());
                        }
                    } else if (heartbeatObj instanceof java.util.Date) {
                        lastHeartbeat = ((java.util.Date) heartbeatObj).toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDateTime();
                    } else if (heartbeatObj instanceof java.sql.Timestamp) {
                        lastHeartbeat = ((java.sql.Timestamp) heartbeatObj).toLocalDateTime();
                    } else if (heartbeatObj instanceof java.time.Instant) {
                        lastHeartbeat = ((java.time.Instant) heartbeatObj)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDateTime();
                    } else if (heartbeatObj instanceof java.util.ArrayList) {
                        // 处理ArrayList格式的时间数据
                        try {
                            @SuppressWarnings("unchecked")
                            java.util.ArrayList<Integer> timeList = (java.util.ArrayList<Integer>) heartbeatObj;
                            if (timeList.size() >= 6) {
                                // 格式: [year, month, day, hour, minute, second, nano]
                                lastHeartbeat = LocalDateTime.of(
                                        timeList.get(0), timeList.get(1), timeList.get(2),
                                        timeList.get(3), timeList.get(4), timeList.get(5));
                            } else {
                                log.warn("节点 {} ArrayList格式不正确，元素数量: {}", entry.getKey(), timeList.size());
                            }
                        } catch (Exception e) {
                            log.warn("节点 {} 无法解析ArrayList格式的心跳时间: {} - {}",
                                    entry.getKey(), heartbeatObj, e.getMessage());
                        }
                    } else if (heartbeatObj instanceof java.util.List) {
                        // 处理List格式的时间数据（非ArrayList的其他List实现）
                        try {
                            @SuppressWarnings("unchecked")
                            java.util.List<Integer> timeList = (java.util.List<Integer>) heartbeatObj;
                            if (timeList.size() >= 6) {
                                // 格式: [year, month, day, hour, minute, second, nano]
                                lastHeartbeat = LocalDateTime.of(
                                        timeList.get(0), timeList.get(1), timeList.get(2),
                                        timeList.get(3), timeList.get(4), timeList.get(5));
                            } else {
                                log.warn("节点 {} List格式不正确，元素数量: {}", entry.getKey(), timeList.size());
                            }
                        } catch (Exception e) {
                            log.warn("节点 {} 无法解析List格式的心跳时间: {} - {}",
                                    entry.getKey(), heartbeatObj, e.getMessage());
                        }
                    } else if (heartbeatObj != null) {
                        log.warn("节点 {} 未知的心跳时间数据类型: {} - {}",
                                entry.getKey(), heartbeatObj.getClass().getName(), heartbeatObj);
                    } else {
                    }

                    if (lastHeartbeat != null && lastHeartbeat.isBefore(threshold)) {
                        redisTemplate.opsForHash().delete(NODE_REGISTRY_KEY, entry.getKey());
                    }
                } catch (Exception e) {
                    log.error("处理节点 {} 时发生错误: {}", entry.getKey(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("清理离线节点失败", e);
        }
    }

    /**
     * 获取当前节点ID
     */
    private String getCurrentNodeId() {
        try {
            // 获取应用名称
            String applicationName = environment.getProperty("spring.application.name", "efak-ai");

            // 获取IP地址
            String ipAddress = java.net.InetAddress.getLocalHost().getHostAddress();

            // 获取当前时间戳，格式为yyyyMMddHHmmss
            String timestamp = LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            return applicationName + "-" + ipAddress + "-" + serverPort + "-" + timestamp;
        } catch (Exception e) {
            log.warn("获取节点ID失败，使用默认格式: {}", e.getMessage());
            return "efak-ai-unknown-" + System.currentTimeMillis();
        }
    }

    /**
     * 启动调度器
     */
    public void startScheduler() {
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
        status.put("nodeId", getCurrentNodeId());
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