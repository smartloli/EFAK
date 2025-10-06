package org.kafka.eagle.web.service.impl;

import lombok.RequiredArgsConstructor;
import org.kafka.eagle.core.util.NetUtils;
import org.kafka.eagle.dto.broker.BrokerInfo;
import org.kafka.eagle.dto.cluster.ClusterPageResponse;
import org.kafka.eagle.dto.cluster.ClusterQueryRequest;
import org.kafka.eagle.dto.cluster.KafkaClusterInfo;
import org.kafka.eagle.dto.cluster.CreateClusterRequest;
import org.kafka.eagle.tool.util.ClusterIdGenerator;
import org.kafka.eagle.web.mapper.BrokerMapper;
import org.kafka.eagle.web.mapper.ClusterMapper;
import org.kafka.eagle.web.service.ClusterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClusterServiceImpl implements ClusterService {

    private final ClusterMapper clusterMapper;
    private final BrokerMapper brokerMapper;

    @Override
    public Map<String, Object> getClusterStats() {
        Map<String, Object> stats = new HashMap<>();

        // 获取所有集群
        List<KafkaClusterInfo> allClusters = clusterMapper.queryClusters(null, null, null, "id", "ASC", 0,
                Integer.MAX_VALUE);

        int totalClusters = allClusters.size();
        int onlineClusters = 0;
        int offlineClusters = 0;
        BigDecimal totalAvailability = BigDecimal.ZERO;
        int clustersWithAvailability = 0;

        for (KafkaClusterInfo cluster : allClusters) {
            // 计算每个集群的实时状态
            Long totalNodesL = brokerMapper.countByClusterId(cluster.getClusterId());
            Long onlineNodesL = brokerMapper.countOnlineByClusterId(cluster.getClusterId());
            int totalNodes = totalNodesL == null ? 0 : totalNodesL.intValue();
            int onlineNodes = onlineNodesL == null ? 0 : onlineNodesL.intValue();

            if (totalNodes > 0) {
                BigDecimal availability = new BigDecimal(onlineNodes)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalNodes), 2, RoundingMode.HALF_UP);

                // 判断集群状态：可用度>80%为在线，否则为离线
                if (availability.compareTo(BigDecimal.valueOf(80)) > 0) {
                    onlineClusters++;
                } else {
                    offlineClusters++;
                }

                totalAvailability = totalAvailability.add(availability);
                clustersWithAvailability++;
            } else {
                offlineClusters++;
            }
        }

        // 计算平均可用度
        BigDecimal avgAvailability = BigDecimal.ZERO;
        if (clustersWithAvailability > 0) {
            avgAvailability = totalAvailability.divide(BigDecimal.valueOf(clustersWithAvailability), 2,
                    RoundingMode.HALF_UP);
        }

        stats.put("totalClusters", totalClusters);
        stats.put("onlineClusters", onlineClusters);
        stats.put("offlineClusters", offlineClusters);
        stats.put("avgAvailability", avgAvailability);

        return stats;
    }

    @Override
    public ClusterPageResponse list(ClusterQueryRequest req) {
        int offset = (req.getPage() - 1) * req.getPageSize();
        List<KafkaClusterInfo> list = clusterMapper.queryClusters(
                req.getSearch(), req.getClusterType(), req.getAuth(),
                req.getSortField(), req.getSortOrder(), offset, req.getPageSize());
        long total = clusterMapper.countClusters(req.getSearch(), req.getClusterType(), req.getAuth());
        // enrich UI fields
        for (KafkaClusterInfo c : list) {
            Long totalNodesL = brokerMapper.countByClusterId(c.getClusterId());
            Long onlineNodesL = brokerMapper.countOnlineByClusterId(c.getClusterId());
            int totalNodes = totalNodesL == null ? 0 : totalNodesL.intValue();
            int onlineNodes = onlineNodesL == null ? 0 : onlineNodesL.intValue();
            c.setTotalNodes(totalNodes);
            c.setOnlineNodes(onlineNodes);
            if (totalNodes > 0) {
                BigDecimal availability = new BigDecimal(onlineNodes)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalNodes), 2, RoundingMode.HALF_UP);
                c.setAvailability(availability);
            }
        }
        ClusterPageResponse resp = new ClusterPageResponse();
        resp.setClusters(list);
        resp.setTotal(total);
        resp.setPage(req.getPage());
        resp.setPageSize(req.getPageSize());
        return resp;
    }

    @Override
    public KafkaClusterInfo getById(Long id) {
        return clusterMapper.getById(id);
    }

    @Override
    public KafkaClusterInfo findByClusterId(String clusterId) {
        KafkaClusterInfo info = clusterMapper.findByClusterId(clusterId);
        if (info == null) return null;
        // 映射前端所需别名字段
        info.setType(info.getClusterType());
        info.setSecurityConfig(info.getAuthConfig());
        info.setSecurityEnabled("Y".equalsIgnoreCase(info.getAuth()));
        // 附带当前brokers列表
        List<BrokerInfo> brokers = brokerMapper.getBrokersByClusterId(clusterId);
        if (brokers != null) {
            for (BrokerInfo b : brokers) {
                b.setKafkaPort(b.getPort());
            }
        }
        info.setBrokers(brokers);
        return info;
    }

    @Override
    @Transactional
    public KafkaClusterInfo create(CreateClusterRequest request) {
        // 生成集群ID
        String clusterId = ClusterIdGenerator.generate();

        // 组装并插入集群信息（初始统计暂为0）
        KafkaClusterInfo clusterInfo = new KafkaClusterInfo();
        clusterInfo.setClusterId(clusterId);
        clusterInfo.setName(request.getName());
        clusterInfo.setClusterType(request.getClusterType());
        clusterInfo.setAuth(request.getAuth());
        clusterInfo.setAuthConfig(request.getAuthConfig());
        clusterInfo.setAvailability(BigDecimal.ZERO);
        clusterInfo.setCreatedAt(LocalDateTime.now());
        clusterInfo.setUpdatedAt(LocalDateTime.now());

        int inserted = clusterMapper.insertCluster(clusterInfo);
        if (inserted <= 0) {
            throw new RuntimeException("创建集群失败");
        }

        // 插入所有Broker节点
        if (request.getNodes() != null) {
            for (CreateClusterRequest.Node n : request.getNodes()) {
                BrokerInfo broker = new BrokerInfo();
                broker.setClusterId(clusterId);
                broker.setBrokerId(n.getBrokerId());
                broker.setHostIp(n.getHostIp());
                broker.setPort(n.getPort());
                broker.setJmxPort(n.getJmxPort());
                broker.setStatus(NetUtils.telnet(n.getHostIp(), n.getPort()) ? "online" : "offline");
                broker.setCreatedBy(request.getCreator() == null ? "system" : request.getCreator());
                brokerMapper.createBroker(broker);
            }
        }

        // 统计该集群的Broker数量与在线数量，并计算可用度
        Long totalNodesL = brokerMapper.countByClusterId(clusterId);
        Long onlineNodesL = brokerMapper.countOnlineByClusterId(clusterId);
        int totalNodes = totalNodesL == null ? 0 : totalNodesL.intValue();
        int onlineNodes = onlineNodesL == null ? 0 : onlineNodesL.intValue();
        BigDecimal availability = BigDecimal.ZERO;
        if (totalNodes > 0) {
            availability = new BigDecimal(onlineNodes)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalNodes), 2, RoundingMode.HALF_UP);
        }

        // 更新集群统计
        clusterMapper.updateSummaryByClusterId(clusterId, totalNodes, onlineNodes, availability);

        // 返回创建后的集群信息
        return clusterMapper.findByClusterId(clusterId);
    }

    @Override
    @Transactional
    public int update(KafkaClusterInfo info) {
        // 1) 兼容前端字段到持久化字段
        if (info.getType() != null && (info.getClusterType() == null || info.getClusterType().isEmpty())) {
            info.setClusterType(info.getType());
        }
        if (info.getSecurityConfig() != null && (info.getAuthConfig() == null || info.getAuthConfig().isEmpty())) {
            info.setAuthConfig(info.getSecurityConfig());
        }
        if (info.getSecurityEnabled() != null) {
            info.setAuth(Boolean.TRUE.equals(info.getSecurityEnabled()) ? "Y" : "N");
        }

        // 2) 根据clusterId查出现有记录，拿到id供updateCluster使用
        KafkaClusterInfo exist = clusterMapper.findByClusterId(info.getClusterId());
        if (exist == null) {
            throw new RuntimeException("集群不存在: " + info.getClusterId());
        }
        info.setId(exist.getId());

        // 3) 更新集群基本信息
        int updated = clusterMapper.updateCluster(info);

        // 4) 重建Broker节点（简单做法：全部删除再新增）
        if (info.getBrokers() != null) {
            brokerMapper.deleteBrokersByClusterId(info.getClusterId());
            for (BrokerInfo b : info.getBrokers()) {
                if (b.getBrokerId() == null || b.getHostIp() == null || b.getHostIp().isEmpty()) continue;
                BrokerInfo toInsert = new BrokerInfo();
                toInsert.setClusterId(info.getClusterId());
                toInsert.setBrokerId(b.getBrokerId());
                toInsert.setHostIp(b.getHostIp());
                // 兼容前端的kafkaPort字段
                Integer port = b.getPort() != null ? b.getPort() : b.getKafkaPort();
                toInsert.setPort(port == null ? 9092 : port);
                toInsert.setJmxPort(b.getJmxPort() == null ? 9999 : b.getJmxPort());
                toInsert.setStatus(NetUtils.telnet(toInsert.getHostIp(), toInsert.getPort()) ? "online" : "offline");
                toInsert.setCreatedBy("system");
                brokerMapper.createBroker(toInsert);
            }
        }

        // 5) 更新汇总统计
        updateSummaryByClusterId(info.getClusterId());
        return updated;
    }

    @Override
    public int delete(Long id) {
        return clusterMapper.deleteCluster(id);
    }

    @Override
    @Transactional
    public int deleteByClusterId(String clusterId) {
        // 先删除该集群下的所有Broker节点
        int deletedBrokers = brokerMapper.deleteBrokersByClusterId(clusterId);

        // 再删除集群信息
        KafkaClusterInfo cluster = clusterMapper.findByClusterId(clusterId);
        if (cluster != null) {
            int deletedCluster = clusterMapper.deleteCluster(cluster.getId());
            return deletedCluster;
        }
        return 0;
    }

    @Override
    public int updateSummaryByClusterId(String clusterId) {
        // 统计并更新汇总
        Long totalNodesL = brokerMapper.countByClusterId(clusterId);
        Long onlineNodesL = brokerMapper.countOnlineByClusterId(clusterId);
        int totalNodes = totalNodesL == null ? 0 : totalNodesL.intValue();
        int onlineNodes = onlineNodesL == null ? 0 : onlineNodesL.intValue();
        BigDecimal availability = BigDecimal.ZERO;
        if (totalNodes > 0) {
            availability = new BigDecimal(onlineNodes)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalNodes), 2, RoundingMode.HALF_UP);
        }
        return clusterMapper.updateSummaryByClusterId(clusterId, totalNodes, onlineNodes, availability);
    }
}