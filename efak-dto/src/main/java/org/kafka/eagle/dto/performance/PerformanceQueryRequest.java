package org.kafka.eagle.dto.performance;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * <p>
 * 性能监控查询请求参数类，用于封装性能监控数据查询的各种条件参数。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/9/24 03:05:23
 * @version 5.0.0
 */
@Data
public class PerformanceQueryRequest {
    /**
     * 集群ID
     */
    private String clusterId;

    /**
     * Kafka节点HOST
     */
    private String kafkaHost;

    /**
     * 内存使用率阈值(%)
     */
    private BigDecimal memoryUsageThreshold;

    /**
     * CPU使用率阈值(%)
     */
    private BigDecimal cpuUsageThreshold;

    /**
     * 消息写入速率阈值
     */
    private BigDecimal messageInThreshold;

    /**
     * 字节写入速率阈值
     */
    private BigDecimal byteInThreshold;

    /**
     * 字节读取速率阈值
     */
    private BigDecimal byteOutThreshold;

    /**
     * 写入耗时阈值(毫秒)
     */
    private BigDecimal timeMsProduceThreshold;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}