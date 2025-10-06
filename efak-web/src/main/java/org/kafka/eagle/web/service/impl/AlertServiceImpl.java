package org.kafka.eagle.web.service.impl;

import org.kafka.eagle.dto.alert.*;
import org.kafka.eagle.web.mapper.AlertMapper;
import org.kafka.eagle.web.mapper.BrokerMapper;
import org.kafka.eagle.web.mapper.ConsumerGroupTopicMapper;
import org.kafka.eagle.web.mapper.TopicInstantMetricsMapper;
import org.kafka.eagle.web.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 告警Service实现类
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/30 01:39:39
 * @version 5.0.0
 */
@Service
public class AlertServiceImpl implements AlertService {

    @Autowired
    private AlertMapper alertMapper;

    @Autowired
    private BrokerMapper brokerMapper;

    @Autowired
    private ConsumerGroupTopicMapper consumerGroupTopicMapper;

    @Autowired
    private TopicInstantMetricsMapper topicInstantMetricsMapper;

    @Override
    public AlertPageResponse queryAlerts(AlertQueryRequest request) {
        AlertPageResponse response = new AlertPageResponse();

        // 计算偏移量
        int offset = (request.getPage() - 1) * request.getPageSize();

        // 查询告警列表
        List<AlertInfo> alerts = alertMapper.queryAlerts(
                request.getClusterId(),
                request.getSearch(),
                request.getStatus(),
                request.getType(),
                request.getChannel(),
                request.getTimeRange(),
                request.getSortField(),
                request.getSortOrder(),
                offset,
                request.getPageSize());

        // 查询总数
        Long total = alertMapper.countAlerts(
                request.getClusterId(),
                request.getSearch(),
                request.getStatus(),
                request.getType(),
                request.getChannel(),
                request.getTimeRange());

        // 查询统计信息
        Map<String, Object> rawStats = alertMapper.getAlertStats(request.getClusterId());

        // 转换BigDecimal到Long
        Map<String, Long> stats = new java.util.HashMap<>();
        if (rawStats != null) {
            for (Map.Entry<String, Object> entry : rawStats.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Number) {
                    stats.put(entry.getKey(), ((Number) value).longValue());
                } else {
                    stats.put(entry.getKey(), 0L);
                }
            }
        }

        response.setAlerts(alerts);
        response.setTotal(total);
        response.setPage(request.getPage());
        response.setPageSize(request.getPageSize());
        response.setStats(stats);

        return response;
    }

    @Override
    public AlertPageResponse queryAlertsForNotifications(AlertQueryRequest request) {
        AlertPageResponse response = new AlertPageResponse();

        // 计算偏移量
        int offset = (request.getPage() - 1) * request.getPageSize();

        // 查询告警通知
        List<AlertInfo> alerts = alertMapper.queryAlertsForNotifications(
                request.getClusterId(),
                request.getSortField(),
                request.getSortOrder(),
                offset,
                request.getPageSize());

        // 查询总数
        Long total = alertMapper.countAlertsForNotifications(request.getClusterId());

        response.setAlerts(alerts);
        response.setTotal(total);
        response.setPage(request.getPage());
        response.setPageSize(request.getPageSize());

        return response;
    }

    @Override
    public AlertInfo getAlertById(Long id) {
        return alertMapper.getAlertById(id);
    }

    @Override
    public boolean updateAlertStatus(Long alertTaskId, String clusterId, Integer status) {
        return alertMapper.updateAlertStatus(alertTaskId, clusterId, status) > 0;
    }

    @Override
    public boolean updateAlertStatusById(Long id, String clusterId, Integer status) {
        return alertMapper.updateAlertStatusById(id, clusterId, status) > 0;
    }

    @Override
    public List<AlertChannel> getAlertChannels(String clusterId) {
        return alertMapper.getAlertChannels(clusterId);
    }

    @Override
    public AlertChannel getAlertChannelById(Long id) {
        return alertMapper.getAlertChannelById(id);
    }

    @Override
    public boolean createAlertChannel(AlertChannel channel) {
        return alertMapper.createAlertChannel(channel) > 0;
    }

    @Override
    public boolean updateAlertChannel(AlertChannel channel) {
        // 先获取原有的渠道信息
        AlertChannel existingChannel = alertMapper.getAlertChannelById(channel.getId());
        if (existingChannel == null) {
            return false;
        }

        // 只更新允许修改的字段，保持原有字段不变
        existingChannel.setApiUrl(channel.getApiUrl());
        existingChannel.setEnabled(channel.getEnabled());

        return alertMapper.updateAlertChannel(existingChannel) > 0;
    }

    @Override
    public boolean deleteAlertChannel(Long id) {
        return alertMapper.deleteAlertChannel(id) > 0;
    }

    @Override
    public List<AlertTypeConfig> getAlertTypeConfigs(String clusterId) {
        return alertMapper.getAlertTypeConfigs(clusterId);
    }

    @Override
    public AlertPageResponse getAlertTypeConfigsPage(String clusterId, Integer page, Integer pageSize) {
        AlertPageResponse response = new AlertPageResponse();

        int offset = (page - 1) * pageSize;
        List<AlertTypeConfig> configs = alertMapper.getAlertTypeConfigsPage(clusterId, offset, pageSize);
        Long total = alertMapper.countAlertTypeConfigs(clusterId);

        // 将AlertTypeConfig转换为AlertInfo以兼容现有的响应结构
        List<AlertInfo> alertInfos = configs.stream().map(config -> {
            AlertInfo alertInfo = new AlertInfo();
            alertInfo.setId(config.getId());
            alertInfo.setType(config.getType());
            alertInfo.setTitle(config.getName());
            alertInfo.setDescription(config.getDescription());
            alertInfo.setThreshold(String.valueOf(config.getThreshold()));
            alertInfo.setUnit(config.getUnit());
            alertInfo.setCreatedAt(config.getCreatedAt());
            alertInfo.setUpdatedAt(config.getUpdatedAt());
            alertInfo.setCreatedBy(config.getCreatedBy());
            // 设置渠道信息 - 从实际的渠道表中获取渠道信息
            if (config.getChannelId() != null) {
                AlertChannel channel = alertMapper.getAlertChannelById(config.getChannelId());
                if (channel != null) {
                    alertInfo.setChannelName(channel.getName());
                    alertInfo.setType(channel.getType());
                } else {
                    alertInfo.setChannelName("未知渠道");
                    alertInfo.setType("unknown");
                }
            }
            return alertInfo;
        }).collect(java.util.stream.Collectors.toList());

        response.setAlerts(alertInfos);
        response.setTotal(total);
        response.setPage(page);
        response.setPageSize(pageSize);

        return response;
    }

    @Override
    public AlertTypeConfig getAlertTypeConfigById(Long id) {
        AlertTypeConfig config = alertMapper.getAlertTypeConfigById(id);
        if (config != null && config.getChannelId() != null) {
            // 获取关联的渠道信息，转换为AlertChannelStatus格式
            AlertChannel channel = alertMapper.getAlertChannelById(config.getChannelId());
            if (channel != null) {
                AlertChannelStatus channelStatus = new AlertChannelStatus();
                channelStatus.setId(config.getChannelId());
                channelStatus.setChannelType(channel.getType());
                channelStatus.setChannelName(channel.getName());
                channelStatus.setStatus("success"); // 默认状态
                config.setChannelStatus(channelStatus);
            }
        }
        return config;
    }

    @Override
    public boolean createAlertTypeConfig(AlertTypeConfig config) {
        // 检查监控目标是否存在
        if (!checkTargetExists(config.getClusterId(), config.getTarget())) {
            throw new IllegalArgumentException("监控目标不存在: " + config.getTarget());
        }

        // channelId会通过getChannelIdsJson()方法自动转换为JSON格式存储到channel_id字段
        return alertMapper.createAlertTypeConfig(config) > 0;
    }

    @Override
    public boolean updateAlertTypeConfig(AlertTypeConfig config) {
        // 先获取原有的配置信息
        AlertTypeConfig existingConfig = alertMapper.getAlertTypeConfigById(config.getId());
        if (existingConfig == null) {
            return false;
        }

        // 只更新允许修改的字段，保持其他字段不变
        existingConfig.setThreshold(config.getThreshold());
        existingConfig.setUnit(config.getUnit());
        existingConfig.setChannelId(config.getChannelId());
        existingConfig.setEnabled(config.getEnabled());

        return alertMapper.updateAlertTypeConfig(existingConfig) > 0;
    }

    @Override
    public boolean deleteAlertTypeConfig(Long id) {
        return alertMapper.deleteAlertTypeConfig(id) > 0;
    }

    @Override
    public boolean checkTargetExists(String clusterId, String target) {
        if (target == null || target.trim().isEmpty()) {
            return false;
        }

        String topicName = null;
        String groupId = null;
        String brokerId = null;

        // 解析目标参数 - 新格式（无前缀）
        if ("all".equals(target)) {
            // 全局目标，总是存在
            return true;
        } else if (target.contains(",")) {
            // 消费者组积压告警格式: group_id,topic
            String[] parts = target.split(",", 2);
            if (parts.length == 2) {
                groupId = parts[0].trim();
                topicName = parts[1].trim();
            }
        } else {
            // 检查是否为IP地址（broker可用性告警）
            if (isValidIpAddress(target)) {
                brokerId = target; // IP地址
            } else {
                // 主题相关告警格式: topic_name
                topicName = target;
            }
        }

        return alertMapper.checkTargetExists(clusterId, target, topicName, groupId, brokerId) > 0;
    }

    /**
     * 验证是否为有效的IP地址
     */
    private boolean isValidIpAddress(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }

        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }

        for (String part : parts) {
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<AlertTypeConfig> getAlertTypeConfigsByTypeAndTarget(String clusterId, String type, String target) {
        return alertMapper.getAlertTypeConfigsByTypeAndTarget(clusterId, type, target);
    }

    @Override
    public boolean performRebalance(Long alertTaskId, String clusterId) {
        try {
            // 这里可以实现具体的数据均衡逻辑
            // 例如调用Kafka的工具进行数据重新分布

            // 将告警状态更新为已处理 (1)
            boolean success = alertMapper.updateAlertStatus(alertTaskId, clusterId, 1) > 0;

            if (success) {
                // 可以在这里添加告警解决的记录
                // alertMapper.addAlertResolveLog(alertTaskId, "执行数据均衡操作");
            }

            return success;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean performRebalanceById(Long id, String clusterId) {
        try {
            // 这里可以实现具体的数据均衡逻辑
            // 例如调用Kafka的工具进行数据重新分布

            // 将告警状态更新为已处理 (1)
            boolean success = alertMapper.updateAlertStatusById(id, clusterId, 1) > 0;

            if (success) {
                // 可以在这里添加告警解决的记录
                // alertMapper.addAlertResolveLogById(id, "执行数据均衡操作");
            }

            return success;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getBrokersByClusterId(String clusterId) {
        try {
            return brokerMapper.getBrokersForAlert(clusterId);
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getConsumerGroupsByClusterId(String clusterId) {
        try {
            return consumerGroupTopicMapper.getConsumerGroupsByClusterId(clusterId);
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getConsumerTopicsByGroupId(String clusterId, String groupId) {
        try {
            return consumerGroupTopicMapper.getTopicsByGroupId(clusterId, groupId);
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getTopicsByMetricType(String clusterId, String metricType) {
        try {
            return topicInstantMetricsMapper.getTopicsByMetricType(clusterId, metricType);
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }
}