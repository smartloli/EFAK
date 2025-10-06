package org.kafka.eagle.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * <p>
 * Function调用信息
 * AI决定调用某个函数时返回的信息
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/10/04 00:00:00
 * @version 5.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionCall {

    /**
     * 函数名称
     */
    private String name;

    /**
     * 函数参数（JSON格式字符串）
     */
    private String arguments;

    /**
     * 解析后的参数（可选）
     */
    private Map<String, Object> parsedArguments;
}
