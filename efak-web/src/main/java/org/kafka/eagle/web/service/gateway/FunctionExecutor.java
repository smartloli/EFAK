package org.kafka.eagle.web.service.gateway;

import org.kafka.eagle.dto.ai.FunctionCall;
import org.kafka.eagle.dto.ai.FunctionResult;

/**
 * <p>
 * Function执行器接口
 * 定义如何执行AI调用的函数
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/10/04 00:00:00
 * @version 5.0.0
 */
public interface FunctionExecutor {

    /**
     * 执行函数调用
     *
     * @param functionCall 函数调用信息
     * @return 执行结果
     */
    FunctionResult execute(FunctionCall functionCall);

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    String getFunctionName();
}
