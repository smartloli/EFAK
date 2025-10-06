package org.kafka.eagle.dto.alert;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 告警信息 DTO，用于管理告警事件的详细信息和状态。
 * 对应 ke_alerts 表结构
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/27 10:00:32
 * @version 5.0.0
 */
@Data
public class AlertInfo {
    // ke_alerts表直接字段
    private Long id;                    // 自增ID字段
    private Long alertTaskId;           // 告警任务ID（关联 ke_alert_type_configs 表的 ID）
    private String clusterId;           // 集群ID
    private String title;               // 告警标题
    private String description;         // 告警描述
    private Long channelId;             // 告警渠道ID（关联 ke_alert_channels 表）
    private String duration;            // 持续时间
    private Integer status;             // 告警状态：0-未处理，1-已处理，2-已忽略
    private LocalDateTime createdAt;    // 创建时间
    private LocalDateTime updatedAt;    // 更新时间

    // 从关联表查询的扩展字段（用于显示）
    private String type;                // 渠道类型（从 ke_alert_channels 表）
    private String channelName;         // 渠道名称（从 ke_alert_channels 表）
    private String alertType;           // 告警类型（从 ke_alert_type_configs 表）
    private String threshold;           // 告警阈值（从 ke_alert_type_configs 表）
    private String unit;                // 单位（从 ke_alert_type_configs 表）
    private String object;              // 监控目标（从 ke_alert_type_configs 表的 target 字段）

    // 兼容性字段（用于告警类型配置等其他场景）
    private String createdBy;           // 创建人
    private List<AlertSendLog> sendLogs; // 告警发送记录列表

    // 状态常量
    public static final int STATUS_UNPROCESSED = 0; // 未处理
    public static final int STATUS_PROCESSED = 1;   // 已处理
    public static final int STATUS_IGNORED = 2;     // 已忽略
    public static final int STATUS_RESOLVED = 3;    // 已解决

    // 状态转换辅助方法
    public String getStatusText() {
        Integer s = this.status;
        if (s == null) {
            return "未知";
        }
        switch (s) {
            case STATUS_UNPROCESSED:
                return "未处理";
            case STATUS_PROCESSED:
                return "已处理";
            case STATUS_IGNORED:
                return "已忽略";
            case STATUS_RESOLVED:
                return "已解决";
            default:
                return "未知";
        }
    }

    public static Integer getStatusByText(String statusText) {
        switch (statusText) {
            case "unprocessed":
                return STATUS_UNPROCESSED;
            case "processed":
                return STATUS_PROCESSED;
            case "ignored":
                return STATUS_IGNORED;
            case "resolved":
                return STATUS_RESOLVED;
            default:
                return STATUS_UNPROCESSED;
        }
    }
}