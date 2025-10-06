package org.kafka.eagle.dto.broker;

import lombok.Data;
import java.util.List;

/**
 * <p>
 * Broker 分页响应 DTO，用于返回 Broker 列表的分页查询结果。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/13 10:00:00
 * @version 5.0.0
 */
@Data
public class BrokerPageResponse {
    private List<BrokerInfo> brokers;
    private Long total;
    private Integer page;
    private Integer pageSize;
    private BrokerStats stats;
}