package org.kafka.eagle.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.scheduler.TaskScheduler;
import org.kafka.eagle.web.service.TaskSchedulerService;
import org.kafka.eagle.web.mapper.TaskSchedulerMapper;
import org.kafka.eagle.web.scheduler.UnifiedDistributedScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 任务调度控制器
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/27 02:30:22
 * @version 5.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/scheduler")
public class TaskSchedulerController {

    @Autowired
    private TaskSchedulerService taskSchedulerService;

    @Autowired
    private TaskSchedulerMapper taskSchedulerMapper;

    @Autowired
    private UnifiedDistributedScheduler unifiedScheduler;

    /**
     * 获取任务列表
     */
    @GetMapping("")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTaskList(
            @RequestParam(defaultValue = "") String taskName,
            @RequestParam(defaultValue = "") String taskType,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") String clusterName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Map<String, Object> result = taskSchedulerService.getTaskSchedulerList(
                taskName, taskType, status, clusterName, page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取任务列表（兼容旧路径）
     */
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTaskListLegacy(
            @RequestParam(defaultValue = "") String taskName,
            @RequestParam(defaultValue = "") String taskType,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") String clusterName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Map<String, Object> result = taskSchedulerService.getTaskSchedulerList(
                taskName, taskType, status, clusterName, page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTaskDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        TaskScheduler task = taskSchedulerService.getTaskSchedulerById(id);
        if (task != null) {
            result.put("success", true);
            result.put("data", task);
        } else {
            result.put("success", false);
            result.put("message", "任务不存在");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 获取任务详情（兼容旧路径）
     */
    @GetMapping("/detail/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTaskDetailLegacy(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        TaskScheduler task = taskSchedulerService.getTaskSchedulerById(id);
        if (task != null) {
            result.put("success", true);
            result.put("data", task);
        } else {
            result.put("success", false);
            result.put("message", "任务不存在");
        }

        return ResponseEntity.ok(result);
    }

    // 移除创建任务接口

    /**
     * 更新任务
     */
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateTask(@PathVariable Long id,
            @RequestBody TaskScheduler taskScheduler) {
        Map<String, Object> result = new HashMap<>();

        try {
            taskScheduler.setId(id);

            // 验证Cron表达式
            if (!taskSchedulerService.validateCronExpression(taskScheduler.getCronExpression())) {
                result.put("success", false);
                result.put("message", "Cron表达式格式不正确");
                return ResponseEntity.ok(result);
            }

            boolean success = taskSchedulerService.updateTaskScheduler(taskScheduler);
            result.put("success", success);
            result.put("message", success ? "更新成功" : "更新失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 更新任务（兼容旧路径）
     */
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateTaskLegacy(@RequestBody TaskScheduler taskScheduler) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 验证Cron表达式
            if (!taskSchedulerService.validateCronExpression(taskScheduler.getCronExpression())) {
                result.put("success", false);
                result.put("message", "Cron表达式格式不正确");
                return ResponseEntity.ok(result);
            }

            boolean success = taskSchedulerService.updateTaskScheduler(taskScheduler);
            result.put("success", success);
            result.put("message", success ? "更新成功" : "更新失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    // 移除删除任务相关接口

    /**
     * 启用任务
     */
    @PostMapping("/enable/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> enableTask(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = taskSchedulerService.enableTask(id);
            result.put("success", success);
            result.put("message", success ? "启用成功" : "启用失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "启用失败：" + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 禁用任务
     */
    @PostMapping("/disable/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> disableTask(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = taskSchedulerService.disableTask(id);
            result.put("success", success);
            result.put("message", success ? "禁用成功" : "禁用失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "禁用失败：" + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 立即执行任务
     */
    @PostMapping("/execute/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> executeTask(@PathVariable Long id) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 使用统一调度器执行任务
            boolean success = unifiedScheduler.triggerTask(id);
            result.put("success", success);
            result.put("message", success ? "执行成功" : "执行失败");
        } catch (Exception e) {
            log.error("=== 任务执行异常: {} ===", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "执行失败：" + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 获取任务统计信息
     */
    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTaskStats() {
        try {
            Map<String, Object> stats = taskSchedulerService.getTaskStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "获取任务统计信息失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResult);
        }
    }

    /**
     * 获取调度器状态
     */
    @GetMapping("/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSchedulerStatus() {
        try {
            Map<String, Object> status = unifiedScheduler.getSchedulerStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "获取调度器状态失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResult);
        }
    }

    /**
     * 获取运行中的任务
     */
    @GetMapping("/running")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRunningTasks() {
        try {
            List<Map<String, Object>> runningTasks = unifiedScheduler.getRunningTasks();
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", runningTasks);
            result.put("count", runningTasks.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "获取运行中任务失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResult);
        }
    }

    /**
     * 获取任务类型列表
     */
    @GetMapping("/task-types")
    @ResponseBody
    public ResponseEntity<List<Map<String, String>>> getTaskTypes() {
        List<Map<String, String>> taskTypes = taskSchedulerService.getTaskTypeList();
        return ResponseEntity.ok(taskTypes);
    }

    /**
     * 获取集群名称列表
     */
    @GetMapping("/cluster-names")
    @ResponseBody
    public ResponseEntity<List<String>> getClusterNames() {
        List<String> clusterNames = taskSchedulerService.getClusterNameList();
        return ResponseEntity.ok(clusterNames);
    }

    /**
     * 获取任务执行历史
     */
    @GetMapping("/history/{taskId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTaskHistory(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Map<String, Object> result = taskSchedulerService.getTaskExecutionHistory(taskId, page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取所有任务执行历史
     */
    @GetMapping("/history")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllTaskHistory(
            @RequestParam(defaultValue = "") String taskName,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") String startDate,
            @RequestParam(defaultValue = "") String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            // 添加参数验证和日志
            log.debug("getAllTaskHistory called with params: taskName={}, status={}, startDate={}, endDate={}, page={}, size={}",
                    taskName, status, startDate, endDate, page, size);

            Map<String, Object> result = taskSchedulerService.getAllTaskExecutionHistory(
                    taskName, status, startDate, endDate, page, size);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in getAllTaskHistory: {}", e.getMessage(), e);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "获取历史记录失败");
            errorResult.put("message", e.getMessage());
            errorResult.put("status", 500);
            return ResponseEntity.status(500).body(errorResult);
        }
    }

    /**
     * 获取单个历史执行记录详情
     */
    @GetMapping("/history/detail/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getHistoryDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, Object> history = taskSchedulerService.getTaskExecutionHistoryDetail(id);
            if (history != null) {
                result.put("success", true);
                result.put("history", history);
            } else {
                result.put("success", false);
                result.put("message", "历史记录不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取历史详情失败：" + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 验证Cron表达式
     */
    @PostMapping("/validate-cron")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateCron(@RequestParam String cronExpression) {
        Map<String, Object> result = new HashMap<>();

        boolean isValid = taskSchedulerService.validateCronExpression(cronExpression);
        result.put("valid", isValid);
        result.put("message", isValid ? "Cron表达式格式正确" : "Cron表达式格式不正确");

        return ResponseEntity.ok(result);
    }

    /**
     * 测试路由
     */
    @GetMapping("/test-route")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testRoute() {
        log.debug("测试路由API被调用");

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "路由正常工作");
        result.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(result);
    }

}