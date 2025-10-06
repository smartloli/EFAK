package org.kafka.eagle.dto.alert;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 告警类型配置 DTO，用于管理不同类型告警的配置规则和阈值设置。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/12 01:07:59
 * @version 5.0.0
 */
@Data
public class AlertTypeConfig {
    private Long id;
    private String clusterId;
    private String type;
    private String name;
    private String description;
    private Boolean enabled;
    private BigDecimal threshold;
    private String unit;
    private String target;

    private Long channelId;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联的渠道信息
    private AlertChannel channel;

    // 关联的渠道状态信息（用于前端显示）
    private AlertChannelStatus channelStatus;

    /**
     * 获取channelId的JSON字符串，用于MyBatis映射到channel_id字段
     * 将单个channelId转换为JSON数组格式以兼容现有数据库结构
     */
    public String getChannelIdsJson() {
        if (channelId == null) {
            return "[]";
        }
        return "[" + channelId + "]";
    }

    /**
     * 设置channelId的JSON字符串，用于MyBatis映射从channel_id字段
     * 从JSON数组格式中提取第一个channelId
     */
    public void setChannelIdsJson(String channelIdsJson) {
        if (channelIdsJson == null || channelIdsJson.trim().isEmpty() || "[]".equals(channelIdsJson)) {
            this.channelId = null;
            return;
        }

        // 移除方括号并分割
        String content = channelIdsJson.replaceAll("[\\[\\]]", "");
        if (content.trim().isEmpty()) {
            this.channelId = null;
            return;
        }

        String[] ids = content.split(",");
        if (ids.length > 0) {
            try {
                // 只取第一个ID，因为我们现在使用单渠道逻辑
                this.channelId = Long.parseLong(ids[0].trim());
            } catch (NumberFormatException e) {
                this.channelId = null;
            }
        }
    }
}