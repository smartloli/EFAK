package org.kafka.eagle.web.util;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 任务配置类
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/28 11:31:07
 * @version 5.0.0
 */
public class TaskConfig {

    // 任务状态常量
    public static final String STATUS_ENABLED = "enabled";
    public static final String STATUS_DISABLED = "disabled";
    public static final String STATUS_RUNNING = "running";
    public static final String STATUS_ERROR = "error";

    // 执行状态常量
    public static final String EXECUTION_STATUS_RUNNING = "RUNNING";
    public static final String EXECUTION_STATUS_SUCCESS = "SUCCESS";
    public static final String EXECUTION_STATUS_FAILED = "FAILED";
    public static final String EXECUTION_STATUS_CANCELLED = "CANCELLED";

    // 触发类型常量
    public static final String TRIGGER_TYPE_SCHEDULED = "SCHEDULED";
    public static final String TRIGGER_TYPE_MANUAL = "MANUAL";

    // 任务类型常量
    public static final String TASK_TYPE_TOPIC_MONITOR = "topic_monitor";
    public static final String TASK_TYPE_CONSUMER_MONITOR = "consumer_monitor";
    public static final String TASK_TYPE_CLUSTER_MONITOR = "cluster_monitor";
    public static final String TASK_TYPE_ALERT_MONITOR = "alert_monitor";
    public static final String TASK_TYPE_DATA_CLEANUP = "data_cleanup";
    public static final String TASK_TYPE_PERFORMANCE_STATS = "performance_stats";

    // 分页配置
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    // 任务执行配置
    public static final int DEFAULT_TASK_TIMEOUT = 300; // 5分钟
    public static final int MAX_TASK_TIMEOUT = 3600; // 1小时
    public static final int MIN_TASK_TIMEOUT = 30; // 30秒

    // 数据保留配置
    public static final int DEFAULT_HISTORY_RETENTION_DAYS = 30;
    public static final int MAX_HISTORY_RETENTION_DAYS = 365;
    public static final int MIN_HISTORY_RETENTION_DAYS = 1;

    // 任务名称配置
    public static final int MAX_TASK_NAME_LENGTH = 100;
    public static final int MIN_TASK_NAME_LENGTH = 2;

    // 描述配置
    public static final int MAX_DESCRIPTION_LENGTH = 500;

    // 集群名称配置
    public static final int MAX_CLUSTER_NAME_LENGTH = 50;

    // 默认Cron表达式
    public static final String DEFAULT_CRON_EXPRESSION = "0 0 12 * * ?"; // 每天中午12点

    // 任务类型列表
    public static final List<String> TASK_TYPES = Arrays.asList(
            TASK_TYPE_TOPIC_MONITOR,
            TASK_TYPE_CONSUMER_MONITOR,
            TASK_TYPE_CLUSTER_MONITOR,
            TASK_TYPE_ALERT_MONITOR,
            TASK_TYPE_DATA_CLEANUP,
            TASK_TYPE_PERFORMANCE_STATS);

    // 状态列表
    public static final List<String> STATUS_LIST = Arrays.asList(
            STATUS_ENABLED,
            STATUS_DISABLED,
            STATUS_RUNNING,
            STATUS_ERROR);

    // 执行状态列表
    public static final List<String> EXECUTION_STATUS_LIST = Arrays.asList(
            EXECUTION_STATUS_RUNNING,
            EXECUTION_STATUS_SUCCESS,
            EXECUTION_STATUS_FAILED,
            EXECUTION_STATUS_CANCELLED);

    // 触发类型列表
    public static final List<String> TRIGGER_TYPE_LIST = Arrays.asList(
            TRIGGER_TYPE_SCHEDULED,
            TRIGGER_TYPE_MANUAL);

    /**
     * 验证任务类型
     */
    public static boolean isValidTaskType(String taskType) {
        return TASK_TYPES.contains(taskType);
    }

    /**
     * 验证状态
     */
    public static boolean isValidStatus(String status) {
        return STATUS_LIST.contains(status);
    }

    /**
     * 验证执行状态
     */
    public static boolean isValidExecutionStatus(String executionStatus) {
        return EXECUTION_STATUS_LIST.contains(executionStatus);
    }

    /**
     * 验证触发类型
     */
    public static boolean isValidTriggerType(String triggerType) {
        return TRIGGER_TYPE_LIST.contains(triggerType);
    }

    /**
     * 验证任务名称长度
     */
    public static boolean isValidTaskNameLength(String taskName) {
        return taskName != null &&
                taskName.length() >= MIN_TASK_NAME_LENGTH &&
                taskName.length() <= MAX_TASK_NAME_LENGTH;
    }

    /**
     * 验证描述长度
     */
    public static boolean isValidDescriptionLength(String description) {
        return description == null || description.length() <= MAX_DESCRIPTION_LENGTH;
    }

    /**
     * 验证集群名称长度
     */
    public static boolean isValidClusterNameLength(String clusterName) {
        return clusterName == null || clusterName.length() <= MAX_CLUSTER_NAME_LENGTH;
    }

    /**
     * 验证任务超时时间
     */
    public static boolean isValidTaskTimeout(int timeout) {
        return timeout >= MIN_TASK_TIMEOUT && timeout <= MAX_TASK_TIMEOUT;
    }

    /**
     * 验证历史记录保留天数
     */
    public static boolean isValidHistoryRetentionDays(int days) {
        return days >= MIN_HISTORY_RETENTION_DAYS && days <= MAX_HISTORY_RETENTION_DAYS;
    }

    /**
     * 验证分页大小
     */
    public static boolean isValidPageSize(int pageSize) {
        return pageSize > 0 && pageSize <= MAX_PAGE_SIZE;
    }

    /**
     * 获取默认分页大小
     */
    public static int getDefaultPageSize() {
        return DEFAULT_PAGE_SIZE;
    }

    /**
     * 获取最大分页大小
     */
    public static int getMaxPageSize() {
        return MAX_PAGE_SIZE;
    }

    /**
     * 获取默认任务超时时间
     */
    public static int getDefaultTaskTimeout() {
        return DEFAULT_TASK_TIMEOUT;
    }

    /**
     * 获取默认历史记录保留天数
     */
    public static int getDefaultHistoryRetentionDays() {
        return DEFAULT_HISTORY_RETENTION_DAYS;
    }

    /**
     * 获取默认Cron表达式
     */
    public static String getDefaultCronExpression() {
        return DEFAULT_CRON_EXPRESSION;
    }
}