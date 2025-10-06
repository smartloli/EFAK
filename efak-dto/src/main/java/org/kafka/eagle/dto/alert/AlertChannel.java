package org.kafka.eagle.dto.alert;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * <p>
 * 告警渠道 DTO，用于管理告警通知渠道的配置信息。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/27 10:00:11
 * @version 5.0.0
 */
@Data
public class AlertChannel {
    private Long id;
    private String clusterId;
    private String name;
    private String type;
    private String apiUrl;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}