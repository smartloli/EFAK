package org.kafka.eagle.web.service;

import org.kafka.eagle.dto.alert.AlertInfo;
import org.kafka.eagle.dto.alert.AlertQueryRequest;
import org.kafka.eagle.dto.alert.AlertPageResponse;
import org.kafka.eagle.dto.alert.AlertChannel;
import org.kafka.eagle.dto.alert.AlertTypeConfig;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 告警Service接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/30 01:39:00
 * @version 5.0.0
 */
public interface AlertService {

    /**
     * 查询告警列表
     */
    AlertPageResponse queryAlerts(AlertQueryRequest request);

    /**
     * 查询告警通知列表（用于通知中心）
     */
    AlertPageResponse queryAlertsForNotifications(AlertQueryRequest request);

    /**
     * 根据ID查询告警详情
     */
    AlertInfo getAlertById(Long id);

    /**
     * 更新告警状态
     */
    boolean updateAlertStatus(Long alertTaskId, String clusterId, Integer status);

    /**
     * 根据告警ID更新告警状态
     */
    boolean updateAlertStatusById(Long id, String clusterId, Integer status);

    /**
     * 查询告警渠道列表
     */
    List<AlertChannel> getAlertChannels(String clusterId);

    /**
     * 根据ID查询告警渠道
     */
    AlertChannel getAlertChannelById(Long id);

    /**
     * 创建告警渠道
     */
    boolean createAlertChannel(AlertChannel channel);

    /**
     * 更新告警渠道
     */
    boolean updateAlertChannel(AlertChannel channel);

    /**
     * 删除告警渠道
     */
    boolean deleteAlertChannel(Long id);

    /**
     * 查询告警类型配置列表
     */
    List<AlertTypeConfig> getAlertTypeConfigs(String clusterId);

    /**
     * 分页查询告警类型配置列表
     */
    AlertPageResponse getAlertTypeConfigsPage(String clusterId, Integer page, Integer pageSize);

    /**
     * 根据ID查询告警类型配置
     */
    AlertTypeConfig getAlertTypeConfigById(Long id);

    /**
     * 创建告警类型配置
     */
    boolean createAlertTypeConfig(AlertTypeConfig config);

    /**
     * 更新告警类型配置
     */
    boolean updateAlertTypeConfig(AlertTypeConfig config);

    /**
     * 删除告警类型配置
     */
    boolean deleteAlertTypeConfig(Long id);

    /**
     * 检查监控目标是否存在
     */
    boolean checkTargetExists(String clusterId, String target);

    /**
     * 根据类型和目标查询告警类型配置
     */
    List<AlertTypeConfig> getAlertTypeConfigsByTypeAndTarget(String clusterId, String type, String target);

    /**
     * 执行数据均衡操作
     */
    boolean performRebalance(Long alertTaskId, String clusterId);

    /**
     * 根据告警ID执行数据均衡操作
     */
    boolean performRebalanceById(Long id, String clusterId);

    /**
     * 根据集群ID获取Broker列表
     */
    List<Map<String, Object>> getBrokersByClusterId(String clusterId);

    /**
     * 根据集群ID获取消费者组列表
     */
    List<Map<String, Object>> getConsumerGroupsByClusterId(String clusterId);

    /**
     * 根据消费者组ID获取Topic列表
     */
    List<Map<String, Object>> getConsumerTopicsByGroupId(String clusterId, String groupId);

    /**
     * 根据指标类型获取Topic列表
     */
    List<Map<String, Object>> getTopicsByMetricType(String clusterId, String metricType);
}