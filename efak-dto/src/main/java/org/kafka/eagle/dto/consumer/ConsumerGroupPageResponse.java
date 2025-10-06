package org.kafka.eagle.dto.consumer;

import java.util.List;

/**
 * <p>
 * 消费者组分页响应 DTO，用于返回消费者组的分页查询结果。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/13
 * @version 1.0
 */
public class ConsumerGroupPageResponse {

    /** 数据列表 */
    private List<ConsumerGroupInfo> records;

    /** 总记录数 */
    private Long total;

    /** 当前页 */
    private Integer page;

    /** 每页大小 */
    private Integer pageSize;

    /** 总页数 */
    private Integer totalPages;

    public ConsumerGroupPageResponse() {
    }

    public ConsumerGroupPageResponse(List<ConsumerGroupInfo> records, Long total, Integer page, Integer pageSize) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = total != null && pageSize != null && pageSize > 0
                ? (int) Math.ceil((double) total / pageSize)
                : 0;
    }

    public List<ConsumerGroupInfo> getRecords() {
        return records;
    }

    public void setRecords(List<ConsumerGroupInfo> records) {
        this.records = records;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    @Override
    public String toString() {
        return "ConsumerGroupPageResponse{" +
                "records=" + records +
                ", total=" + total +
                ", page=" + page +
                ", pageSize=" + pageSize +
                ", totalPages=" + totalPages +
                '}';
    }
}