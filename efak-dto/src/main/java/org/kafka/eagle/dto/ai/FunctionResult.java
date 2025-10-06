package org.kafka.eagle.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Function执行结果
 * 执行函数后返回的结果
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/10/04 00:00:00
 * @version 5.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionResult {

    /**
     * 函数名称
     */
    private String name;

    /**
     * 执行结果（JSON格式字符串）
     */
    private String result;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误信息（如果失败）
     */
    private String error;
}
