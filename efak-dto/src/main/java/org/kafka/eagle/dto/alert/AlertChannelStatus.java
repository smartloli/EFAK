package org.kafka.eagle.dto.alert;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * <p>
 * 告警渠道状态 DTO，用于记录告警通知渠道的发送状态信息。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/27 10:00:22
 * @version 5.0.0
 */
@Data
public class AlertChannelStatus {
    private Long id;
    private String clusterId;
    private String channelType;
    private String channelName; // 添加渠道名称字段
    private String status;
    private LocalDateTime sendTime;
    private String errorMessage;
    private Integer retryCount;
}