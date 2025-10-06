package org.kafka.eagle.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * <p>
 * Function定义
 * 用于定义AI可以调用的函数
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/10/04 00:00:00
 * @version 5.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionDefinition {

    /**
     * 函数名称
     */
    private String name;

    /**
     * 函数描述
     */
    private String description;

    /**
     * 函数参数定义（JSON Schema格式）
     */
    private Map<String, Object> parameters;
}
