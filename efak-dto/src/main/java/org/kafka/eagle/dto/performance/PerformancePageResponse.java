package org.kafka.eagle.dto.performance;

import lombok.Data;
import java.util.List;

/**
 * <p>
 * 性能监控分页响应类，用于返回性能监控数据的分页结果。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/9/24 03:05:23
 * @version 5.0.0
 */
@Data
public class PerformancePageResponse {
    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页数据
     */
    private List<PerformanceMonitor> records;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer totalPages;
}