package org.kafka.eagle.web.service.impl;

import org.kafka.eagle.web.service.BrokerService;
import org.kafka.eagle.web.service.BrokerMetricsService;
import org.kafka.eagle.dto.broker.BrokerInfo;
import org.kafka.eagle.dto.broker.BrokerQueryRequest;
import org.kafka.eagle.dto.broker.BrokerPageResponse;
import org.kafka.eagle.dto.broker.BrokerStats;
import org.kafka.eagle.web.mapper.BrokerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * <p>
 * Broker服务实现类
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/27 13:09:22
 * @version 5.0.0
 */
@Service
public class BrokerServiceImpl implements BrokerService {

    @Autowired
    private BrokerMapper brokerMapper;

    @Autowired
    private BrokerMetricsService brokerMetricsService;

    @Override
    public BrokerPageResponse queryBrokers(BrokerQueryRequest request) {
        BrokerPageResponse response = new BrokerPageResponse();

        int offset = (request.getPage() - 1) * request.getPageSize();
        List<BrokerInfo> brokers = brokerMapper.queryBrokers(
                request.getSearch(),
                request.getStatus(),
                request.getClusterId(),
                request.getSortField(),
                request.getSortOrder(),
                offset,
                request.getPageSize());
        Long total = brokerMapper.countBrokers(request.getSearch(), request.getStatus(), request.getClusterId());

        // 获取统计信息（根据是否传入clusterId选择范围）
        Map<String, Object> statsMap = (request.getClusterId() != null && !request.getClusterId().isEmpty())
                ? brokerMapper.getBrokerStatsByClusterId(request.getClusterId())
                : brokerMapper.getBrokerStats();
        BrokerStats stats = new BrokerStats();
        stats.setTotalCount(((Number) statsMap.get("total_count")).intValue());
        stats.setOnlineCount(((Number) statsMap.get("online_count")).intValue());
        stats.setOfflineCount(((Number) statsMap.get("offline_count")).intValue());

        if (statsMap.get("avg_cpu_usage") != null) {
            stats.setAvgCpuUsage(((Number) statsMap.get("avg_cpu_usage")).doubleValue());
        }
        if (statsMap.get("avg_memory_usage") != null) {
            stats.setAvgMemoryUsage(((Number) statsMap.get("avg_memory_usage")).doubleValue());
        }

        response.setBrokers(brokers);
        response.setTotal(total);
        response.setPage(request.getPage());
        response.setPageSize(request.getPageSize());
        response.setStats(stats);

        return response;
    }

    @Override
    public BrokerInfo getBrokerById(Long id) {
        return brokerMapper.getBrokerById(id);
    }

    @Override
    public BrokerInfo getBrokerByBrokerId(Integer brokerId) {
        return brokerMapper.getBrokerByBrokerId(brokerId);
    }

    @Override
    public boolean createBroker(BrokerInfo broker) {
        // 检查Broker是否已存在
        if (checkBrokerExists(broker.getClusterId(), broker.getBrokerId(), broker.getHostIp(), broker.getPort())) {
            throw new IllegalArgumentException(
                    "Broker已存在: " + broker.getClusterId() + ":" + broker.getBrokerId() + "@" + broker.getHostIp() + ":" + broker.getPort());
        }

        // 检查连接状态
        boolean isOnline = checkBrokerConnection(broker.getHostIp(), broker.getPort());
        broker.setStatus(isOnline ? "online" : "offline");

        return brokerMapper.createBroker(broker) > 0;
    }

    @Override
    public boolean updateBroker(BrokerInfo broker) {
        // 检查Broker是否存在
        BrokerInfo existingBroker = brokerMapper.getBrokerById(broker.getId());
        if (existingBroker == null) {
            return false;
        }

        // 检查连接状态
        boolean isOnline = checkBrokerConnection(broker.getHostIp(), broker.getPort());
        broker.setStatus(isOnline ? "online" : "offline");

        return brokerMapper.updateBroker(broker) > 0;
    }

    @Override
    public boolean deleteBroker(Long id) {
        return brokerMapper.deleteBroker(id) > 0;
    }

    @Override
    public boolean updateBrokerStatus(Long id, String status, BigDecimal cpuUsage,
            BigDecimal memoryUsage, LocalDateTime startupTime) {
        return brokerMapper.updateBrokerStatus(id, status, cpuUsage, memoryUsage, startupTime) > 0;
    }

    @Override
    public boolean checkBrokerExists(String clusterId, Integer brokerId, String hostIp, Integer port) {
        return brokerMapper.checkBrokerExists(clusterId, brokerId, hostIp, port) > 0;
    }

    @Override
    public boolean checkBrokerConnection(String hostIp, Integer port) {
        try {
            // 简单的TCP连接检查
            java.net.Socket socket = new java.net.Socket(hostIp, port);
            socket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Map<String, Object> getClusterStats(String clusterId) {
        Map<String, Object> stats = new java.util.HashMap<>();
        
        try {
            // 获取broker统计信息（可按集群过滤）
            Map<String, Object> brokerStats = (clusterId != null && !clusterId.isEmpty())
                    ? brokerMapper.getBrokerStatsByClusterId(clusterId)
                    : brokerMapper.getBrokerStats();
            
            // 获取统计数据 - 安全的类型转换
            Long totalCount = null;
            Long onlineCount = null;
            BigDecimal avgCpuUsage = null;
            BigDecimal avgMemoryUsage = null;
            
            // 安全转换 total_count
            Object totalCountObj = brokerStats.get("total_count");
            if (totalCountObj instanceof Number) {
                totalCount = ((Number) totalCountObj).longValue();
            }
            
            // 安全转换 online_count
            Object onlineCountObj = brokerStats.get("online_count");
            if (onlineCountObj instanceof Number) {
                onlineCount = ((Number) onlineCountObj).longValue();
            }
            
            // 安全转换 avg_cpu_usage
            Object avgCpuObj = brokerStats.get("avg_cpu_usage");
            if (avgCpuObj instanceof BigDecimal) {
                avgCpuUsage = (BigDecimal) avgCpuObj;
            } else if (avgCpuObj instanceof Number) {
                avgCpuUsage = new BigDecimal(avgCpuObj.toString());
            }
            
            // 安全转换 avg_memory_usage
            Object avgMemoryObj = brokerStats.get("avg_memory_usage");
            if (avgMemoryObj instanceof BigDecimal) {
                avgMemoryUsage = (BigDecimal) avgMemoryObj;
            } else if (avgMemoryObj instanceof Number) {
                avgMemoryUsage = new BigDecimal(avgMemoryObj.toString());
            }
            
            // 计算在线率
            double onlineRate = 0.0;
            if (totalCount != null && totalCount > 0) {
                onlineRate = (double) (onlineCount != null ? onlineCount : 0) / totalCount * 100;
            }
            
            // 集群状态：在线率大于80%为健康，否则异常
            String clusterStatus = onlineRate > 80 ? "健康" : "异常";
            
            // 计算运行时长（取最早的启动时间，按集群可选）
            String runtime = calculateClusterRuntime(clusterId);
            
            // 计算平均负载（CPU和内存的综合平均值）
            double averageLoad = calculateAverageLoad(avgCpuUsage, avgMemoryUsage);
            
            stats.put("clusterStatus", clusterStatus);
            stats.put("onlineRate", Math.round(onlineRate * 100.0) / 100.0);
            stats.put("runtime", runtime);
            stats.put("averageLoad", Math.round(averageLoad * 100.0) / 100.0);
            stats.put("onlineNodes", onlineCount != null ? onlineCount : 0);
            stats.put("totalNodes", totalCount != null ? totalCount : 0);
            stats.put("averageCpuUsage", avgCpuUsage != null ? avgCpuUsage : BigDecimal.ZERO);
            stats.put("averageMemoryUsage", avgMemoryUsage != null ? avgMemoryUsage : BigDecimal.ZERO);
            
        } catch (Exception e) {
            stats.put("error", "获取集群统计信息失败: " + e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * 计算集群运行时长
     */
    private String calculateClusterRuntime(String clusterId) {
        try {
            LocalDateTime earliestStartTime = (clusterId != null && !clusterId.isEmpty())
                    ? brokerMapper.getEarliestStartupTimeByClusterId(clusterId)
                    : brokerMapper.getEarliestStartupTime();
            if (earliestStartTime == null) {
                return "未知";
            }
            
            LocalDateTime now = LocalDateTime.now();
            long days = java.time.Duration.between(earliestStartTime, now).toDays();
            long hours = java.time.Duration.between(earliestStartTime, now).toHours() % 24;
            
            return days + "天" + hours + "小时";
        } catch (Exception e) {
            return "未知";
        }
    }
    
    /**
     * 计算平均负载（CPU和内存使用率的综合平均值）
     */
    private double calculateAverageLoad(BigDecimal avgCpuUsage, BigDecimal avgMemoryUsage) {
        try {
            if (avgCpuUsage != null && avgMemoryUsage != null) {
                return (avgCpuUsage.doubleValue() + avgMemoryUsage.doubleValue()) / 2.0;
            }
            return 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public List<BrokerInfo> getBrokersByClusterId(String clusterId) {
        if (clusterId == null || clusterId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return brokerMapper.getBrokersByClusterId(clusterId);
    }


}