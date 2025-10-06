package org.kafka.eagle.dto.scheduler;

import java.time.LocalDateTime;

/**
 * <p>
 * EFAK 任务调度器类，用于存储任务调度的配置信息和执行状态。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/18 01:54:43
 * @version 5.0.0
 */
public class TaskScheduler {

    /**
     * 任务ID
     */
    private Long id;
    
    /**
     * 任务名称
     */
    private String taskName;
    
    /**
     * 任务类型
     */
    private String taskType;
    
    /**
     * Cron 表达式
     */
    private String cronExpression;
    
    /**
     * 集群名称
     */
    private String clusterName;
    
    /**
     * 任务描述
     */
    private String description;
    
    /**
     * 任务状态
     */
    private String status;
    
    /**
     * 执行次数
     */
    private Integer executeCount;
    
    /**
     * 成功次数
     */
    private Integer successCount;
    
    /**
     * 失败次数
     */
    private Integer failCount;
    
    /**
     * 上次执行时间
     */
    private LocalDateTime lastExecuteTime;
    
    /**
     * 下次执行时间
     */
    private LocalDateTime nextExecuteTime;
    
    /**
     * 上次执行结果
     */
    private String lastExecuteResult;
    
    /**
     * 上次错误信息
     */
    private String lastErrorMessage;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新人
     */
    private String updatedBy;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 任务配置
     */
    private String config;
    
    /**
     * 超时时间（秒）
     */
    private Integer timeout;
    
    /**
     * 节点ID
     */
    private String nodeId;

    // 构造函数
    public TaskScheduler() {
    }

    public TaskScheduler(String taskName, String taskType, String cronExpression, String clusterName,
            String description) {
        this.taskName = taskName;
        this.taskType = taskType;
        this.cronExpression = cronExpression;
        this.clusterName = clusterName;
        this.description = description;
    }

    // 获取和设置方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getExecuteCount() {
        return executeCount;
    }

    public void setExecuteCount(Integer executeCount) {
        this.executeCount = executeCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailCount() {
        return failCount;
    }

    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }

    public LocalDateTime getLastExecuteTime() {
        return lastExecuteTime;
    }

    public void setLastExecuteTime(LocalDateTime lastExecuteTime) {
        this.lastExecuteTime = lastExecuteTime;
    }

    public LocalDateTime getNextExecuteTime() {
        return nextExecuteTime;
    }

    public void setNextExecuteTime(LocalDateTime nextExecuteTime) {
        this.nextExecuteTime = nextExecuteTime;
    }

    public String getLastExecuteResult() {
        return lastExecuteResult;
    }

    public void setLastExecuteResult(String lastExecuteResult) {
        this.lastExecuteResult = lastExecuteResult;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setLastErrorMessage(String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String toString() {
        return "TaskScheduler{" +
                "id=" + id +
                ", taskName='" + taskName + '\'' +
                ", taskType='" + taskType + '\'' +
                ", cronExpression='" + cronExpression + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", executeCount=" + executeCount +
                ", successCount=" + successCount +
                ", failCount=" + failCount +
                ", lastExecuteTime=" + lastExecuteTime +
                ", nextExecuteTime=" + nextExecuteTime +
                ", lastExecuteResult='" + lastExecuteResult + '\'' +
                ", lastErrorMessage='" + lastErrorMessage + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createTime=" + createTime +
                ", updatedBy='" + updatedBy + '\'' +
                ", updateTime=" + updateTime +
                ", config='" + config + '\'' +
                ", timeout=" + timeout +
                ", nodeId='" + nodeId + '\'' +
                '}';
    }
}