package org.kafka.eagle.dto.dashboard;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * <p>
 * 仪表板 API 响应 DTO，用于统一封装仪表板接口的响应结果。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/9/13 11:05:07
 * @version 5.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse<T> {

    /**
     * 成功标识
     */
    private Boolean success;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应代码
     */
    private Integer code;

    public static <T> DashboardResponse<T> success(T data) {
        return DashboardResponse.<T>builder()
                .success(true)
                .code(200)
                .message("成功")
                .data(data)
                .build();
    }

    public static <T> DashboardResponse<T> error(String message) {
        return DashboardResponse.<T>builder()
                .success(false)
                .code(500)
                .message(message)
                .data(null)
                .build();
    }
}