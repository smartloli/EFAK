package org.kafka.eagle.web.service.gateway.function;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.ai.FunctionCall;
import org.kafka.eagle.dto.ai.FunctionResult;
import org.kafka.eagle.web.mapper.PerformanceMonitorMapper;
import org.kafka.eagle.web.service.gateway.FunctionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 获取Kafka节点性能监控数据的Function Executor
 * 根据集群ID查询性能监控数据，支持按IP筛选
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/10/06 00:00:00
 * @version 5.0.0
 */
@Slf4j
@Component("get_performance_monitor")
public class GetPerformanceMonitorExecutor implements FunctionExecutor {

    @Autowired
    private PerformanceMonitorMapper performanceMonitorMapper;

    @Override
    public FunctionResult execute(FunctionCall functionCall) {
        try {
            // 获取原始参数字符串
            String argsJson = functionCall.getArguments();
            log.info("执行函数: get_performance_monitor, 原始参数: {}", argsJson);

            // 验证参数不为空且是有效的JSON
            if (argsJson == null || argsJson.trim().isEmpty()) {
                return FunctionResult.builder()
                    .name(getFunctionName())
                    .success(false)
                    .error("函数参数为空")
                    .build();
            }

            // 解析参数 - 使用fastjson2
            Map<String, Object> params;
            try {
                params = JSON.parseObject(argsJson, Map.class);
            } catch (Exception e) {
                log.error("解析函数参数失败, 参数内容: [{}], 错误: {}", argsJson, e.getMessage());
                return FunctionResult.builder()
                    .name(getFunctionName())
                    .success(false)
                    .error("参数格式错误: " + e.getMessage())
                    .build();
            }

            String clusterId = (String) params.get("cluster_id");
            String ip = (String) params.get("ip");

            if (clusterId == null || clusterId.trim().isEmpty()) {
                return FunctionResult.builder()
                    .name(getFunctionName())
                    .success(false)
                    .error("cluster_id参数不能为空")
                    .build();
            }

            log.info("执行函数: get_performance_monitor, 集群ID: {}, IP过滤: {}", clusterId, ip);

            // 查询性能监控数据
            List<Map<String, Object>> performanceData;
            if (ip != null && !ip.trim().isEmpty()) {
                performanceData = performanceMonitorMapper.findByClusterIdAndIp(clusterId, ip);
            } else {
                performanceData = performanceMonitorMapper.findByClusterId(clusterId);
            }

            if (performanceData == null || performanceData.isEmpty()) {
                String message = ip != null
                    ? String.format("未找到集群 %s 中IP为 %s 的性能监控数据", clusterId, ip)
                    : String.format("未找到集群 %s 的性能监控数据", clusterId);
                return FunctionResult.builder()
                    .name(getFunctionName())
                    .success(false)
                    .error(message)
                    .build();
            }

            // 使用fastjson2序列化，自动支持LocalDateTime
            String resultJson = JSON.toJSONString(performanceData);

            log.info("查询性能监控数据成功, 集群ID: {}, 数据条数: {}", clusterId, performanceData.size());

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
        return "get_performance_monitor";
    }
}
