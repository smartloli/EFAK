package org.kafka.eagle.web.service.gateway.function;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.ai.FunctionCall;
import org.kafka.eagle.dto.ai.FunctionResult;
import org.kafka.eagle.dto.alert.AlertChannel;
import org.kafka.eagle.web.mapper.AlertMapper;
import org.kafka.eagle.web.service.gateway.FunctionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 查询告警渠道信息的Function Executor
 * 根据集群ID查询ke_alert_channels表获取告警渠道（不暴露API地址）
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/10/04 00:00:00
 * @version 5.0.0
 */
@Slf4j
@Component("get_alert_channels")
public class GetAlertChannelsExecutor implements FunctionExecutor {

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

            log.info("执行函数: get_alert_channels, 集群ID: {}", clusterId);

            // 查询集群的告警渠道
            List<AlertChannel> channels = alertMapper.getAlertChannels(clusterId);

            // 构建返回结果（不包含api_url字段）
            List<Map<String, Object>> channelList = new ArrayList<>();
            for (AlertChannel channel : channels) {
                Map<String, Object> channelData = new HashMap<>();
                channelData.put("id", channel.getId());
                channelData.put("cluster_id", channel.getClusterId());
                channelData.put("name", channel.getName());
                channelData.put("type", channel.getType());
                // 不包含 api_url 字段，以保护敏感信息
                channelData.put("enabled", channel.getEnabled());
                channelData.put("created_at", channel.getCreatedAt());
                channelData.put("updated_at", channel.getUpdatedAt());
                channelData.put("created_by", channel.getCreatedBy());
                channelList.add(channelData);
            }

            // 统计各类型渠道数量
            Map<String, Integer> typeCount = new HashMap<>();
            int enabledCount = 0;
            for (AlertChannel channel : channels) {
                typeCount.put(channel.getType(),
                    typeCount.getOrDefault(channel.getType(), 0) + 1);
                if (Boolean.TRUE.equals(channel.getEnabled())) {
                    enabledCount++;
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("cluster_id", clusterId);
            result.put("total_count", channelList.size());
            result.put("enabled_count", enabledCount);
            result.put("type_distribution", typeCount);
            result.put("channels", channelList);

            // 使用fastjson2序列化，自动支持LocalDateTime
            String resultJson = JSON.toJSONString(result);

            log.info("查询告警渠道成功, 集群ID: {}, 渠道数: {}, 启用数: {}",
                clusterId, channelList.size(), enabledCount);

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
        return "get_alert_channels";
    }
}
