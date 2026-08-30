package org.kafka.eagle.web.service;

import org.kafka.eagle.dto.broker.BrokerInfo;
import org.kafka.eagle.dto.broker.BrokerQueryRequest;
import org.kafka.eagle.dto.broker.BrokerPageResponse;

import java.util.List;

/**
 * <p>
 * Broker 服务接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/27 13:08:58
 * @version 5.0.0
 */
public interface BrokerService {

    /**
     * 分页查询Broker列表
     */
    BrokerPageResponse queryBrokers(BrokerQueryRequest request);

    /**
     * 根据ID查询Broker详情
     */
    BrokerInfo getBrokerById(Long id);

    /**
     * 根据Broker ID查询Broker信息
     */
    BrokerInfo getBrokerByBrokerId(Integer brokerId);

    /**
     * 创建Broker
     */
    boolean createBroker(BrokerInfo broker);

    /**
     * 更新Broker
     */
    boolean updateBroker(BrokerInfo broker);

    /**
     * 删除Broker
     */
    boolean deleteBroker(Long id);

    /**
     * 更新Broker状态
     */
    boolean updateBrokerStatus(Long id, String status, java.math.BigDecimal cpuUsage,
            java.math.BigDecimal memoryUsage, java.time.LocalDateTime startupTime);

    /**
     * 检查Broker是否存在
     */
    boolean checkBrokerExists(Integer brokerId, String hostIp, Integer port);

    /**
     * 检查Broker连接状态
     */
    boolean checkBrokerConnection(String hostIp, Integer port);

    /**
     * 获取集群统计信息（可按集群过滤）
     */
    java.util.Map<String, Object> getClusterStats(String clusterId);

    /**
     * 根据集群ID获取所有Broker信息
     */
    List<BrokerInfo> getBrokersByClusterId(String clusterId);
}