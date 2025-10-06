package org.kafka.eagle.dto.scheduler;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * <p>
 * EFAK 任务执行结果类，用于存储任务执行的结果信息和状态。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/18 01:57:30
 * @version 5.0.0
 */
public class TaskExecutionResult {

    /**
     * 执行是否成功
     */
    private boolean success;
    
    /**
     * 执行结果信息
     */
    private String result;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 执行数据
     */
    private Map<String, Object> data;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 执行耗时（毫秒）
     */
    private Long duration;
    
    /**
     * 执行节点
     */
    private String executorNode;
    
    /**
     * 触发类型
     */
    private String triggerType;
    
    /**
     * 触发用户
     */
    private String triggerUser;

    // 构造函数
    public TaskExecutionResult() {
        this.startTime = LocalDateTime.now();
    }

    public TaskExecutionResult(boolean success, String result, String errorMessage) {
        this.success = success;
        this.result = result;
        this.errorMessage = errorMessage;
        this.startTime = LocalDateTime.now();
    }

    // 获取和设置方法
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getExecutorNode() {
        return executorNode;
    }

    public void setExecutorNode(String executorNode) {
        this.executorNode = executorNode;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getTriggerUser() {
        return triggerUser;
    }

    public void setTriggerUser(String triggerUser) {
        this.triggerUser = triggerUser;
    }

    /**
     * 完成执行并计算持续时间
     */
    public void complete() {
        this.endTime = LocalDateTime.now();
        if (this.startTime != null) {
            this.duration = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
    }

    /**
     * 创建成功结果
     */
    public static TaskExecutionResult success(String result) {
        TaskExecutionResult executionResult = new TaskExecutionResult();
        executionResult.setSuccess(true);
        executionResult.setResult(result);
        executionResult.complete();
        return executionResult;
    }

    /**
     * 创建成功结果（带数据）
     */
    public static TaskExecutionResult success(String result, Map<String, Object> data) {
        TaskExecutionResult executionResult = success(result);
        executionResult.setData(data);
        return executionResult;
    }

    /**
     * 创建失败结果
     */
    public static TaskExecutionResult failure(String errorMessage) {
        TaskExecutionResult executionResult = new TaskExecutionResult();
        executionResult.setSuccess(false);
        executionResult.setErrorMessage(errorMessage);
        executionResult.complete();
        return executionResult;
    }

    @Override
    public String toString() {
        return "TaskExecutionResult{" +
                "success=" + success +
                ", result='" + result + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", data=" + data +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", duration=" + duration +
                ", executorNode='" + executorNode + '\'' +
                ", triggerType='" + triggerType + '\'' +
                ", triggerUser='" + triggerUser + '\'' +
                '}';
    }
}