package org.kafka.eagle.web.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.scheduler.TaskScheduler;
import org.kafka.eagle.web.mapper.TaskSchedulerMapper;
import org.kafka.eagle.web.service.CronExpressionUpdateService;
import org.kafka.eagle.web.util.TaskUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Description: Cron expression update service implementation for EFAK web
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/27 02:59:40
 * @version 5.0.0
 */
@Slf4j
@Service
public class CronExpressionUpdateServiceImpl implements CronExpressionUpdateService {

    @Autowired
    private TaskSchedulerMapper taskSchedulerMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CRON_UPDATE_NOTIFICATION_KEY = "efak:cron:update:notification:";

    @Override
    public boolean handleCronExpressionUpdate(TaskScheduler taskScheduler, String originalCronExpression) {
        try {

            // 验证新的Cron表达式
            if (!TaskUtils.validateCronExpression(taskScheduler.getCronExpression())) {
                log.error("任务 {} 的Cron表达式格式不正确: {}",
                        taskScheduler.getTaskName(),
                        taskScheduler.getCronExpression());
                return false;
            }

            // 重新计算下次执行时间
            boolean recalculateSuccess = recalculateNextExecuteTime(taskScheduler);
            if (!recalculateSuccess) {
                log.error("重新计算任务 {} 的下次执行时间失败", taskScheduler.getTaskName());
                return false;
            }

            // 更新数据库中的下次执行时间
            String nextExecuteTimeStr = taskScheduler.getNextExecuteTime() != null
                    ? taskScheduler.getNextExecuteTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    : null;

            int updateResult = taskSchedulerMapper.updateNextExecuteTime(
                    taskScheduler.getId(),
                    nextExecuteTimeStr,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            if (updateResult > 0) {

                // 通知分布式调度器
                notifyDistributedScheduler(taskScheduler);
                return true;
            } else {
                log.error("更新任务 {} 的下次执行时间失败", taskScheduler.getTaskName());
                return false;
            }

        } catch (Exception e) {
            log.error("处理Cron表达式更新失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean recalculateNextExecuteTime(TaskScheduler taskScheduler) {
        try {
            String cronExpression = taskScheduler.getCronExpression();

            // 解析Cron表达式: 秒 分 时 日 月 周
            String[] cronFields = cronExpression.trim().split("\\s+");
            if (cronFields.length != 6) {
                log.warn("Cron表达式格式错误，字段数量不为6: {}", cronExpression);
                return false;
            }

            LocalDateTime now = LocalDateTime.now();

            // 简化的Cron解析实现
            int minutes = parseCronField(cronFields[1], 0, 59);
            int hours = parseCronField(cronFields[2], 0, 23);
            int stepMinutes = parseStepFromCronField(cronFields[1]);
            int stepHours = parseStepFromCronField(cronFields[2]);

            LocalDateTime nextTime = now;

            // 处理分钟级别的步长（如 */5 表示每5分钟）
            if (stepMinutes > 0) {
                // 计算下一个符合步长的时间点
                int currentMinute = now.getMinute();
                int nextMinute = ((currentMinute / stepMinutes) + 1) * stepMinutes;
                if (nextMinute >= 60) {
                    nextTime = nextTime.plusHours(1).withMinute(nextMinute - 60);
                } else {
                    nextTime = nextTime.withMinute(nextMinute);
                }
                nextTime = nextTime.withSecond(0).withNano(0);
            }
            // 处理小时级别的步长（如 0 */1 表示每小时）
            else if (stepHours > 0) {
                // 计算下一个符合步长的时间点
                int currentHour = now.getHour();
                int nextHour = ((currentHour / stepHours) + 1) * stepHours;
                if (nextHour >= 24) {
                    nextTime = nextTime.plusDays(1).withHour(nextHour - 24);
                } else {
                    nextTime = nextTime.withHour(nextHour);
                }
                nextTime = nextTime.withMinute(0).withSecond(0).withNano(0);
            }
            // 处理固定时间点
            else if (minutes >= 0 && hours >= 0) {
                // 计算下一个指定时间点
                LocalDateTime today = now.withHour(hours).withMinute(minutes).withSecond(0).withNano(0);
                if (today.isBefore(now) || today.equals(now)) {
                    nextTime = today.plusDays(1);
                } else {
                    nextTime = today;
                }
            }
            // 默认情况：使用当前时间加5分钟
            else {
                nextTime = now.plusMinutes(5);
            }

            taskScheduler.setNextExecuteTime(nextTime);

            return true;

        } catch (Exception e) {
            log.error("重新计算下次执行时间失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public LocalDateTime calculateNextExecuteTime(String cronExpression, LocalDateTime lastExecuteTime) {
        try {
            // 解析Cron表达式: 秒 分 时 日 月 周
            String[] cronFields = cronExpression.trim().split("\\s+");
            if (cronFields.length != 6) {
                log.warn("Cron表达式格式错误，字段数量不为6: {}", cronExpression);
                return lastExecuteTime.plusHours(1); // 默认1小时后
            }

            LocalDateTime baseTime = lastExecuteTime != null ? lastExecuteTime : LocalDateTime.now();

            // 简化的Cron解析实现
            int minutes = parseCronField(cronFields[1], 0, 59);
            int hours = parseCronField(cronFields[2], 0, 23);
            int stepMinutes = parseStepFromCronField(cronFields[1]);
            int stepHours = parseStepFromCronField(cronFields[2]);

            LocalDateTime nextTime = baseTime;

            // 处理分钟级别的步长（如 */5 表示每5分钟）
            if (stepMinutes > 0) {
                // 计算下一个符合步长的时间点
                int currentMinute = baseTime.getMinute();
                int nextMinute = ((currentMinute / stepMinutes) + 1) * stepMinutes;
                if (nextMinute >= 60) {
                    nextTime = nextTime.plusHours(1).withMinute(nextMinute - 60);
                } else {
                    nextTime = nextTime.withMinute(nextMinute);
                }
                nextTime = nextTime.withSecond(0).withNano(0);
            }
            // 处理小时级别的步长（如 0 */1 表示每小时）
            else if (stepHours > 0) {
                // 计算下一个符合步长的时间点
                int currentHour = baseTime.getHour();
                int nextHour = ((currentHour / stepHours) + 1) * stepHours;
                if (nextHour >= 24) {
                    nextTime = nextTime.plusDays(1).withHour(nextHour - 24);
                } else {
                    nextTime = nextTime.withHour(nextHour);
                }
                nextTime = nextTime.withMinute(0).withSecond(0).withNano(0);
            }
            // 处理固定时间点
            else if (minutes >= 0 && hours >= 0) {
                // 计算下一个指定时间点
                LocalDateTime today = baseTime.withHour(hours).withMinute(minutes).withSecond(0).withNano(0);
                if (today.isBefore(baseTime) || today.equals(baseTime)) {
                    nextTime = today.plusDays(1);
                } else {
                    nextTime = today;
                }
            }
            // 默认情况：使用上次执行时间加1小时
            else {
                nextTime = baseTime.plusHours(1);
            }

            return nextTime;

        } catch (Exception e) {
            log.error("计算下次执行时间失败: {}", e.getMessage(), e);
            return lastExecuteTime != null ? lastExecuteTime.plusHours(1) : LocalDateTime.now().plusHours(1);
        }
    }

    @Override
    public void notifyDistributedScheduler(TaskScheduler taskScheduler) {
        try {
            // 创建通知消息
            Map<String, Object> notification = new HashMap<>();
            notification.put("taskId", taskScheduler.getId());
            notification.put("taskName", taskScheduler.getTaskName());
            notification.put("taskType", taskScheduler.getTaskType());
            notification.put("cronExpression", taskScheduler.getCronExpression());
            notification.put("nextExecuteTime", taskScheduler.getNextExecuteTime() != null
                    ? taskScheduler.getNextExecuteTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    : null);
            notification.put("updateTime",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            notification.put("notificationType", "CRON_EXPRESSION_UPDATED");

            // 发送Redis消息通知分布式调度器
            String notificationKey = CRON_UPDATE_NOTIFICATION_KEY + taskScheduler.getId();
            redisTemplate.opsForValue().set(notificationKey, notification, 24, java.util.concurrent.TimeUnit.HOURS);

        } catch (Exception e) {
            log.error("通知分布式调度器失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 解析Cron字段
     */
    private int parseCronField(String field, int min, int max) {
        if ("*".equals(field) || field.contains("*")) {
            return -1; // 表示任意值
        }
        try {
            int value = Integer.parseInt(field);
            return (value >= min && value <= max) ? value : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * 从Cron字段中解析步长（如每5分钟的步长值5）
     */
    private int parseStepFromCronField(String field) {
        if (field.startsWith("*/")) {
            try {
                return Integer.parseInt(field.substring(2));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}