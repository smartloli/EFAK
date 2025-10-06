package org.kafka.eagle.web.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * <p>
 * 任务调度工具类
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/28 11:57:12
 * @version 5.0.0
 */
public class TaskUtils {

    // 简化的Cron表达式验证正则（基本格式检查）
    private static final Pattern CRON_PATTERN = Pattern.compile(
            "^\\s*([*]|([0-5]?[0-9]))\\s+" + // 秒 0-59
                    "([*]|([0-5]?[0-9]))\\s+" + // 分 0-59
                    "([*]|([01]?[0-9]|2[0-3]))\\s+" + // 时 0-23
                    "([*]|([1-9]|[12][0-9]|3[01]))\\s+" + // 日 1-31
                    "([*]|([1-9]|1[0-2]))\\s+" + // 月 1-12
                    "([*]|[0-7]|\\?)\\s*$"); // 周 0-7 或 ?

    private static final Map<String, String> TASK_TYPE_NAMES = new HashMap<>();
    private static final Map<String, String> TASK_TYPE_DESCRIPTIONS = new HashMap<>();

    static {
        // 任务类型名称映射
        TASK_TYPE_NAMES.put("topic_monitor", "主题监控");
        TASK_TYPE_NAMES.put("consumer_monitor", "消费者监控");
        TASK_TYPE_NAMES.put("cluster_monitor", "集群监控");
        TASK_TYPE_NAMES.put("alert_monitor", "告警监控");
        TASK_TYPE_NAMES.put("data_cleanup", "数据清理");
        TASK_TYPE_NAMES.put("performance_stats", "性能统计");

        // 任务类型描述映射
        TASK_TYPE_DESCRIPTIONS.put("topic_monitor", "监控Kafka主题的状态、分区、消息数量等信息");
        TASK_TYPE_DESCRIPTIONS.put("consumer_monitor", "监控消费者组的状态、延迟、消费进度等信息");
        TASK_TYPE_DESCRIPTIONS.put("cluster_monitor", "监控Kafka集群的健康状态、Broker状态等信息");
        TASK_TYPE_DESCRIPTIONS.put("alert_monitor", "监控系统告警状态，实时检测异常情况");
        TASK_TYPE_DESCRIPTIONS.put("data_cleanup", "清理过期的监控数据，保持数据库性能");
        TASK_TYPE_DESCRIPTIONS.put("performance_stats", "统计系统性能指标，生成性能报告");
    }

    /**
     * 验证Cron表达式格式
     */
    public static boolean validateCronExpression(String cronExpression) {
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            return false;
        }

        String trimmedCron = cronExpression.trim();

        // 检查字段数量
        String[] fields = trimmedCron.split("\\s+");
        if (fields.length != 6) {
            return false;
        }

        try {
            // 基本格式检查
            if (!CRON_PATTERN.matcher(trimmedCron).matches()) {
                // 如果正则匹配失败，尝试手动验证每个字段
                return validateCronFields(fields);
            }
            return true;
        } catch (Exception e) {
            // 如果正则验证失败，尝试手动验证
            return validateCronFields(fields);
        }
    }

    /**
     * 手动验证Cron字段
     */
    private static boolean validateCronFields(String[] fields) {
        if (fields.length != 6) {
            return false;
        }

        try {
            // 验证每个字段的有效性
            return validateCronField(fields[0], 0, 59) && // 秒
                    validateCronField(fields[1], 0, 59) && // 分
                    validateCronField(fields[2], 0, 23) && // 时
                    validateCronField(fields[3], 1, 31) && // 日
                    validateCronField(fields[4], 1, 12) && // 月
                    validateCronField(fields[5], 0, 7); // 周 0-7 (周日可以是0或7)
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证Cron表达式字段
     */
    private static boolean validateCronField(String field, int min, int max) {
        if ("*".equals(field) || "?".equals(field)) {
            return true;
        }

        // 检查范围表达式
        if (field.contains("-")) {
            String[] range = field.split("-");
            if (range.length != 2) {
                return false;
            }
            try {
                int start = Integer.parseInt(range[0]);
                int end = Integer.parseInt(range[1]);
                return start >= min && end <= max && start <= end;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        // 检查步长表达式
        if (field.contains("/")) {
            String[] step = field.split("/");
            if (step.length != 2) {
                return false;
            }
            try {
                int stepValue = Integer.parseInt(step[1]);
                return stepValue > 0;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        // 检查列表表达式
        if (field.contains(",")) {
            String[] values = field.split(",");
            for (String value : values) {
                try {
                    int num = Integer.parseInt(value);
                    if (num < min || num > max) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            return true;
        }

        // 检查单个数值
        try {
            int num = Integer.parseInt(field);
            return num >= min && num <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 获取任务类型名称
     */
    public static String getTaskTypeName(String taskType) {
        return TASK_TYPE_NAMES.getOrDefault(taskType, taskType);
    }

    /**
     * 获取任务类型描述
     */
    public static String getTaskTypeDescription(String taskType) {
        return TASK_TYPE_DESCRIPTIONS.getOrDefault(taskType, "未知任务类型");
    }

    /**
     * 获取所有任务类型
     */
    public static Map<String, String> getAllTaskTypes() {
        return new HashMap<>(TASK_TYPE_NAMES);
    }

    /**
     * 格式化持续时间
     */
    public static String formatDuration(Long durationMs) {
        if (durationMs == null) {
            return "未知";
        }

        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%d天%d小时%d分钟", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d分钟%d秒", minutes, seconds % 60);
        } else {
            return String.format("%d秒", seconds);
        }
    }

    /**
     * 格式化文件大小
     */
    public static String formatFileSize(Long bytes) {
        if (bytes == null) {
            return "0 B";
        }

        String[] units = { "B", "KB", "MB", "GB", "TB" };
        int unitIndex = 0;
        double size = bytes;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.1f %s", size, units[unitIndex]);
    }

    /**
     * 格式化日期时间
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 获取状态显示名称
     */
    public static String getStatusDisplayName(String status) {
        switch (status) {
            case "enabled":
                return "已启用";
            case "disabled":
                return "已禁用";
            case "running":
                return "运行中";
            case "error":
                return "错误";
            case "success":
                return "成功";
            case "failed":
                return "失败";
            default:
                return status;
        }
    }

    /**
     * 获取状态CSS类
     */
    public static String getStatusCssClass(String status) {
        switch (status) {
            case "enabled":
            case "success":
                return "text-success";
            case "disabled":
                return "text-muted";
            case "running":
                return "text-warning";
            case "error":
            case "failed":
                return "text-danger";
            default:
                return "text-secondary";
        }
    }

    /**
     * 生成任务名称建议
     */
    public static String generateTaskNameSuggestion(String taskType, String clusterName) {
        String typeName = getTaskTypeName(taskType);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_%s_%s", typeName, clusterName, timestamp);
    }

    /**
     * 验证任务名称
     */
    public static boolean validateTaskName(String taskName) {
        if (taskName == null || taskName.trim().isEmpty()) {
            return false;
        }

        // 任务名称只能包含字母、数字、下划线、中文字符
        return taskName.matches("^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$");
    }

    /**
     * 获取默认Cron表达式
     */
    public static String getDefaultCronExpression(String taskType) {
        switch (taskType) {
            case "topic_monitor":
            case "consumer_monitor":
            case "cluster_monitor":
                return "0 */5 * * * ?"; // 每5分钟
            case "alert_monitor":
                return "0 */5 * * * ?"; // 每5分钟
            case "data_cleanup":
                return "0 0 2 * * ?"; // 每天凌晨2点
            case "performance_stats":
                return "0 0 */1 * * ?"; // 每小时
            case "log_analysis":
                return "0 0 3 * * ?"; // 每天凌晨3点
            default:
                return "0 0 12 * * ?"; // 每天中午12点
        }
    }
}