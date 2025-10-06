package org.kafka.eagle.web.service.gateway.function;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.ai.FunctionCall;
import org.kafka.eagle.dto.ai.FunctionResult;
import org.kafka.eagle.web.mapper.AlertMapper;
import org.kafka.eagle.web.service.gateway.FunctionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 获取告警信息的Function Executor
 * 根据集群ID查询告警信息
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/10/06 00:00:00
 * @version 5.0.0
 */
@Slf4j
@Component("get_alerts")
public class GetAlertsExecutor implements FunctionExecutor {

    @Autowired
    private AlertMapper alertMapper;

    @Override
    public FunctionResult execute(FunctionCall functionCall) {
        try {
            // 解析参数 - 使用fastjson2
            Map<String, Object> params = JSON.parseObject(
                functionCall.getArguments(),
                Map.class
            );

            String clusterId = (String) params.get("cluster_id");

            if (clusterId == null || clusterId.trim().isEmpty()) {
                return FunctionResult.builder()
                    .name(getFunctionName())
                    .success(false)
                    .error("cluster_id参数不能为空")
                    .build();
            }

            log.info("执行函数: get_alerts, 集群ID: {}", clusterId);

            // 查询告警信息
            List<Map<String, Object>> alerts = alertMapper.findByClusterId(clusterId);

            if (alerts == null || alerts.isEmpty()) {
                return FunctionResult.builder()
                    .name(getFunctionName())
                    .success(true)
                    .result("[]")
                    .build();
            }

            // 使用fastjson2序列化，自动支持LocalDateTime
            String resultJson = JSON.toJSONString(alerts);

            log.info("查询告警信息成功, 集群ID: {}, 告警数量: {}", clusterId, alerts.size());

            return FunctionResult.builder()
                .name(getFunctionName())
                .result(resultJson)
                .success(true)
                .build();

        } catch (Exception e) {
            log.error("执行函数失败: " + getFunctionName(), e);
            return FunctionResult.builder()
                .name(getFunctionName())
                .success(false)
                .error(e.getMessage())
                .build();
        }
    }

    @Override
    public String getFunctionName() {
        return "get_alerts";
    }
}
