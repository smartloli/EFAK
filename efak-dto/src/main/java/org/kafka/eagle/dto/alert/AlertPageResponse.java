package org.kafka.eagle.dto.alert;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 告警分页响应 DTO，用于返回告警列表的分页查询结果。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/27 10:00:13
 * @version 5.0.0
 */
@Data
public class AlertPageResponse {
    private List<AlertInfo> alerts;
    private Long total;
    private Integer page;
    private Integer pageSize;
    private Map<String, Long> stats;
}