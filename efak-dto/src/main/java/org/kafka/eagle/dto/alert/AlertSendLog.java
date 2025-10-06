package org.kafka.eagle.dto.alert;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * <p>
 * 告警发送记录 DTO，用于记录告警通知的发送状态和日志信息。
 * 对应 ke_alert_send_logs 表结构
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/9/29 20:00:00
 * @version 5.0.0
 */
@Data
public class AlertSendLog {
    private Long id;                    // 主键ID
    private String clusterId;           // 集群ID
    private Long alertId;               // 告警ID
    private Long channelId;             // 渠道ID
    private String channelType;         // 渠道类型
    private String status;              // 发送状态
    private LocalDateTime sendTime;     // 发送时间
    private String errorMessage;        // 错误信息
    private Integer retryCount;         // 重试次数
    private LocalDateTime createdAt;    // 创建时间

    // 扩展字段（用于查询显示）
    private String channelName;         // 渠道名称（从 ke_alert_channels 表）
    private String alertTitle;          // 告警标题（从 ke_alerts 表）
}