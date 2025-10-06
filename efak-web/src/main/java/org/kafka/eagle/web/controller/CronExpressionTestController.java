package org.kafka.eagle.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.scheduler.TaskScheduler;
import org.kafka.eagle.web.service.CronExpressionUpdateService;
import org.kafka.eagle.web.service.TaskSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Cron表达式测试控制器
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/27 00:10:23
 * @version 5.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/cron-test")
public class CronExpressionTestController {

    @Autowired
    private TaskSchedulerService taskSchedulerService;

    @Autowired
    private CronExpressionUpdateService cronExpressionUpdateService;

    /**
     * 测试Cron表达式更新
     */
    @PostMapping("/update-cron")
    public ResponseEntity<Map<String, Object>> testCronExpressionUpdate(
            @RequestParam Long taskId,
            @RequestParam String newCronExpression) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 获取原始任务
            TaskScheduler originalTask = taskSchedulerService.getTaskSchedulerById(taskId);
            if (originalTask == null) {
                result.put("success", false);
                result.put("message", "任务不存在");
                return ResponseEntity.ok(result);
            }

            String originalCronExpression = originalTask.getCronExpression();

            // 创建更新后的任务对象
            TaskScheduler updatedTask = new TaskScheduler();
            updatedTask.setId(taskId);
            updatedTask.setCronExpression(newCronExpression);
            updatedTask.setDescription(originalTask.getDescription());
            updatedTask.setTimeout(originalTask.getTimeout());

            // 处理Cron表达式更新
            boolean success = cronExpressionUpdateService.handleCronExpressionUpdate(updatedTask,
                    originalCronExpression);

            if (success) {
                // 更新数据库
                boolean updateSuccess = taskSchedulerService.updateTaskScheduler(updatedTask);

                if (updateSuccess) {
                    result.put("success", true);
                    result.put("message", "Cron表达式更新成功");
                    result.put("originalCron", originalCronExpression);
                    result.put("newCron", newCronExpression);
                    result.put("nextExecuteTime", updatedTask.getNextExecuteTime());
                } else {
                    result.put("success", false);
                    result.put("message", "数据库更新失败");
                }
            } else {
                result.put("success", false);
                result.put("message", "Cron表达式处理失败");
            }

        } catch (Exception e) {
            log.error("测试Cron表达式更新失败", e);
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 获取任务信息
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<Map<String, Object>> getTaskInfo(@PathVariable Long taskId) {
        Map<String, Object> result = new HashMap<>();

        try {
            TaskScheduler task = taskSchedulerService.getTaskSchedulerById(taskId);
            if (task != null) {
                result.put("success", true);
                result.put("task", task);
            } else {
                result.put("success", false);
                result.put("message", "任务不存在");
            }
        } catch (Exception e) {
            log.error("获取任务信息失败", e);
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 验证Cron表达式
     */
    @PostMapping("/validate-cron")
    public ResponseEntity<Map<String, Object>> validateCronExpression(@RequestParam String cronExpression) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean isValid = taskSchedulerService.validateCronExpression(cronExpression);
            result.put("success", true);
            result.put("valid", isValid);
            result.put("message", isValid ? "Cron表达式格式正确" : "Cron表达式格式不正确");
        } catch (Exception e) {
            log.error("验证Cron表达式失败", e);
            result.put("success", false);
            result.put("message", "验证失败: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}