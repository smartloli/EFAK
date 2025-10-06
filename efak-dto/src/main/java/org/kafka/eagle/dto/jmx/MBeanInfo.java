package org.kafka.eagle.dto.jmx;

import lombok.Data;

/**
 * <p>
 * JMX 指标的 MBean 信息类，用于存储各种时间间隔的速率指标。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/9/13 19:05:23
 * @version 5.0.0
 */
@Data
public class MBeanInfo {

    /**
     * 1分钟速率
     */
    private String oneMinute;

    /**
     * 5分钟速率
     */
    private String fiveMinute;

    /**
     * 15分钟速率
     */
    private String fifteenMinute;

    /**
     * 平均速率
     */
    private String meanRate;
}
