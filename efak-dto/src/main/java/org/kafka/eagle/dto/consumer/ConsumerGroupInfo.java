package org.kafka.eagle.dto.consumer;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 消费者组信息 DTO，用于存储 Kafka 消费者组的基本信息和消费状态。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/9/13 11:05:07
 * @version 5.0.0
 */
public class ConsumerGroupInfo {

    /** 消费者组ID */
    private String groupId;

    /** 主题名称 */
    private String topicName;

    /** 状态 */
    private String state;

    /** 总延迟 */
    private Long totalLag;

    /** 日志大小 */
    private Long logsize;

    /** 延迟率 */
    private BigDecimal lagRate;

    /** 延迟等级 */
    private String lagLevel;

    /** 采集时间 */
    private Date collectTime;

    public ConsumerGroupInfo() {
        this.totalLag = 0L;
        this.logsize = 0L;
        this.lagRate = BigDecimal.ZERO;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getTotalLag() {
        return totalLag;
    }

    public void setTotalLag(Long totalLag) {
        this.totalLag = totalLag;
    }

    public Long getLogsize() {
        return logsize;
    }

    public void setLogsize(Long logsize) {
        this.logsize = logsize;
    }

    public BigDecimal getLagRate() {
        return lagRate;
    }

    public void setLagRate(BigDecimal lagRate) {
        this.lagRate = lagRate;
    }

    public String getLagLevel() {
        return lagLevel;
    }

    public void setLagLevel(String lagLevel) {
        this.lagLevel = lagLevel;
    }

    public Date getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(Date collectTime) {
        this.collectTime = collectTime;
    }

    @Override
    public String toString() {
        return "ConsumerGroupInfo{" +
                "groupId='" + groupId + '\'' +
                ", topicName='" + topicName + '\'' +
                ", state='" + state + '\'' +
                ", totalLag=" + totalLag +
                ", logsize=" + logsize +
                ", lagRate=" + lagRate +
                ", lagLevel='" + lagLevel + '\'' +
                ", collectTime=" + collectTime +
                '}';
    }
}