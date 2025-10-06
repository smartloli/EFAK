package org.kafka.eagle.web.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.core.api.KafkaSchemaFactory;
import org.kafka.eagle.core.api.KafkaStoragePlugin;
import org.kafka.eagle.core.constant.JmxMetricsConst;
import org.kafka.eagle.core.constant.MBeanMetricsConst;
import org.kafka.eagle.dto.broker.BrokerInfo;
import org.kafka.eagle.dto.cluster.KafkaClientInfo;
import org.kafka.eagle.dto.jmx.JMXInitializeInfo;
import org.kafka.eagle.dto.topic.*;
import org.kafka.eagle.web.mapper.BrokerMapper;
import org.kafka.eagle.web.mapper.ConsumerGroupTopicMapper;
import org.kafka.eagle.web.mapper.TopicInstantMetricsMapper;
import org.kafka.eagle.web.mapper.TopicMapper;
import org.kafka.eagle.web.mapper.TopicMetricsMapper;
import org.kafka.eagle.web.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Topic 服务实现类
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/27 03:32:22
 * @version 5.0.0
 */
@Slf4j
@Service
public class TopicServiceImpl implements TopicService {

    @Autowired
    private TopicMapper topicMapper;

    @Autowired
    private BrokerMapper brokerMapper;

    @Autowired
    private TopicInstantMetricsMapper topicInstantMetricsMapper;

    @Autowired
    private TopicMetricsMapper topicMetricsMapper;

    @Autowired
    private ConsumerGroupTopicMapper consumerGroupTopicMapper;

    @Override
    public TopicPageResponse getTopicPage(TopicQueryRequest request) {
        if (request == null) {
            return new TopicPageResponse(List.of(), 0L, 1, 10);
        }
        Integer page = request.getPage() != null ? request.getPage() : 1;
        Integer pageSize = request.getPageSize() != null ? request.getPageSize() : 10;
        // 查询总数
        Long total = topicMapper.countTopic(request);
        if (total == null || total == 0) {
            return new TopicPageResponse(List.of(), 0L, page, pageSize);
        }
        int offset = (page - 1) * pageSize;
        List<TopicInfo> list = topicMapper.selectTopicPage(request, offset, pageSize);
        return new TopicPageResponse(list, total, page, pageSize);
    }

    @Override
    public TopicInfo getTopicById(Long id) {
        if (id == null) {
            return null;
        }
        return topicMapper.selectTopicById(id);
    }

    @Override
    public TopicInfo getTopicByNameAndCluster(String topicName, String clusterId) {
        if (!StringUtils.hasText(topicName)) {
            return null;
        }
        return topicMapper.selectTopicByNameAndCluster(topicName, clusterId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createTopic(TopicCreateRequest request) {
        try {
            // 参数校验
            if (request == null || !StringUtils.hasText(request.getTopicName())) {
                log.error("创建Topic失败：参数不能为空");
                return false;
            }

            // 记录集群ID信息
            String clusterId = request.getClusterId();

            if (clusterId == null || clusterId.trim().isEmpty()) {
                log.warn("创建Topic时集群ID为空，可能影响集群关联，主题: {}", request.getTopicName());
            }

            // 检查Topic是否已存在（在指定集群中）
            TopicInfo existingTopic = topicMapper.selectTopicByNameAndCluster(request.getTopicName(), clusterId);
            if (existingTopic != null) {
                log.error("创建Topic失败：Topic [{}] 在集群 [{}] 中已存在", request.getTopicName(), clusterId);
                return false;
            }

            // 1. 先调用KafkaSchemaFactory创建Kafka中的Topic
            KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());

            // 创建KafkaClientInfo
            KafkaClientInfo kafkaClientInfo = new KafkaClientInfo();
            kafkaClientInfo.setClusterId(clusterId);

            // 获取集群的broker信息用于连接
            List<BrokerInfo> brokerInfos = List.of();
            if (StringUtils.hasText(clusterId)) {
                brokerInfos = brokerMapper.getBrokersByClusterId(clusterId);
            } else {
                log.warn("集群ID为空，将使用默认连接配置");
            }

            if (!brokerInfos.isEmpty()) {
                // 使用第一个broker的信息作为连接信息
                BrokerInfo firstBroker = brokerInfos.get(0);
                kafkaClientInfo.setBrokerServer(firstBroker.getHostIp() + ":" + firstBroker.getPort());
                log.info("使用broker连接信息: {}:{}", firstBroker.getHostIp(), firstBroker.getPort());
            } else {
                log.error("集群 {} 没有可用的broker信息，请检查配置", clusterId);
                return false;
            }

            // 创建NewTopicInfo
            NewTopicInfo newTopicInfo = new NewTopicInfo();
            newTopicInfo.setTopicName(request.getTopicName());
            newTopicInfo.setPartitions(request.getPartitions());
            newTopicInfo.setReplication(request.getReplicas());

            // 设置保留时间（如果有）
            if (StringUtils.hasText(request.getRetentionTime())) {
                try {
                    long retainMs = Long.parseLong(request.getRetentionTime());
                    newTopicInfo.setRetainMs(retainMs);
                    log.info("设置Topic保留时间: {} ms", retainMs);
                } catch (NumberFormatException e) {
                    log.warn("保留时间格式错误，使用默认值: {}", request.getRetentionTime());
                }
            }

            // 调用KafkaSchemaFactory创建Topic
            boolean kafkaCreateSuccess = ksf.createTopicIfNotExists(kafkaClientInfo, newTopicInfo);
            if (!kafkaCreateSuccess) {
                log.error("创建Topic失败：Kafka创建操作失败，主题: {}", request.getTopicName());
                return false;
            }

            // 2. Kafka创建成功后，写入数据库
            TopicInfo topicInfo = new TopicInfo();
            topicInfo.setTopicName(request.getTopicName());
            topicInfo.setClusterId(clusterId); // 确保设置集群ID
            topicInfo.setPartitions(request.getPartitions());
            topicInfo.setReplicas(request.getReplicas());
            topicInfo.setRetentionTime(request.getRetentionTime());
            topicInfo.setBrokerSpread("100"); // 默认值
            topicInfo.setBrokerSkewed("0"); // 默认值
            topicInfo.setLeaderSkewed("0"); // 默认值
            topicInfo.setIcon(request.getIcon()); // 设置图标
            topicInfo.setCreateBy(request.getCreateBy());
            topicInfo.setUpdateBy(request.getCreateBy());

            // 确认集群ID已正确设置
            if (topicInfo.getClusterId() != null && !topicInfo.getClusterId().trim().isEmpty()) {
                log.info("✓ 集群ID已正确设置: {}", topicInfo.getClusterId());
            } else {
                log.warn("⚠ 警告: 集群ID为空，主题将无法关联到特定集群");
            }

            // 插入数据库
            int result = topicMapper.insertTopic(topicInfo);

            if (result > 0) {
                return true;
            } else {
                log.error("Topic [{}] 创建失败：数据库插入失败", request.getTopicName());
                // 注意：此时Kafka中的主题已经创建，但数据库插入失败
                // 可以考虑记录这种不一致状态，或者通过定时任务同步
                return false;
            }

        } catch (Exception e) {
            log.error("创建Topic失败：{}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTopic(String topicName, String clusterId) {
        try {
            if (!StringUtils.hasText(topicName)) {
                log.error("删除Topic失败：topic名称不能为空");
                return false;
            }

            // 先从数据库查询Topic信息
            TopicInfo topicInfo = topicMapper.selectTopicByNameAndCluster(topicName, clusterId);
            if (topicInfo == null || topicInfo.getId() == null) {
                log.error("删除Topic失败：Topic [{}] 在集群 [{}] 中不存在", topicName, clusterId);
                return false;
            }

            // 1. 先调用KafkaSchemaFactory删除Kafka中的主题
            KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());

            // 创建KafkaClientInfo
            KafkaClientInfo kafkaClientInfo = new KafkaClientInfo();
            kafkaClientInfo.setClusterId(clusterId);

            // 获取集群的broker信息用于连接
            List<BrokerInfo> brokerInfos = List.of();
            if (StringUtils.hasText(clusterId)) {
                brokerInfos = brokerMapper.getBrokersByClusterId(clusterId);
            }

            if (!brokerInfos.isEmpty()) {
                // 使用第一个broker的信息作为连接信息
                BrokerInfo firstBroker = brokerInfos.get(0);
                kafkaClientInfo.setBrokerServer(firstBroker.getHostIp() + ":" + firstBroker.getPort());
            } else {
                log.error("删除Topic失败：集群 [{}] 中没有Broker信息", clusterId);
                return false;
            }

            // 调用KafkaSchemaFactory删除Topic
            boolean kafkaDeleteSuccess = ksf.removeTopic(kafkaClientInfo, topicName);
            if (!kafkaDeleteSuccess) {
                log.error("删除Topic失败：Kafka删除失败，名称 = {}", topicName);
                return false;
            }

            // 2. Kafka删除成功后，删除数据库中的所有相关记录（原子操作）
            try {
                // 删除 ke_topic_instant_metrics 表中的相关记录
                int instantMetricsDeleted = topicInstantMetricsMapper.deleteByTopicNameAndClusterId(clusterId, topicName);

                // 删除 ke_topics_metrics 表中的相关记录
                int topicMetricsDeleted = topicMetricsMapper.deleteByTopicNameAndClusterId(clusterId, topicName);

                // 最后删除 ke_topic_info 表中的主记录
                int result = topicMapper.deleteTopic(topicInfo.getId());

                if (result > 0) {
                    return true;
                } else {
                    log.error("删除Topic失败：ke_topic_info表删除失败，名称 = {}", topicName);
                    // 注意：此时Kafka中的主题已经被删除，但数据库删除失败，需要手动处理
                    return false;
                }
            } catch (Exception e) {
                log.error("删除Topic的数据库记录失败：{}", e.getMessage(), e);
                // 注意：此时Kafka中的主题已经被删除，但数据库删除失败，需要手动处理
                return false;
            }

        } catch (Exception e) {
            log.error("删除Topic失败：{}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean scaleTopic(String topicName, Integer newPartitions, String clusterId,String username) {
        try {
            if (!StringUtils.hasText(topicName) || newPartitions == null || newPartitions <= 0) {
                log.error("扩容Topic失败：参数不能为空");
                return false;
            }

            // 1. 先从数据库获取现有主题信息
            TopicInfo existingTopic = topicMapper.selectTopicByNameAndCluster(topicName, clusterId);
            if (existingTopic == null) {
                log.error("扩容Topic失败：Topic [{}] 不存在，集群ID: {}", topicName, clusterId);
                return false;
            }

            // 2. 验证新分区数是否大于当前分区数
            if (newPartitions <= existingTopic.getPartitions()) {
                log.error("扩容Topic失败：新分区数 {} 必须大于当前分区数 {}", newPartitions, existingTopic.getPartitions());
                return false;
            }

            // 3. 获取broker信息
            List<BrokerInfo> brokerInfos = List.of();
            if (StringUtils.hasText(clusterId)) {
                brokerInfos = brokerMapper.getBrokersByClusterId(clusterId);
                if (brokerInfos.isEmpty()) {
                    log.warn("集群 {} 中未找到broker信息，尝试使用默认连接", clusterId);
                }
            }

            // 4. 创建KafkaClientInfo
            KafkaClientInfo kafkaClientInfo = new KafkaClientInfo();
            kafkaClientInfo.setClusterId(clusterId);

            if (!brokerInfos.isEmpty()) {
                // 使用第一个broker的信息作为连接信息
                BrokerInfo firstBroker = brokerInfos.get(0);
                kafkaClientInfo.setBrokerServer(firstBroker.getHostIp() + ":" + firstBroker.getPort());
            } else {
                log.error("扩容Topic失败：集群 {} 中未找到broker信息，请检查配置", clusterId);
                return false;
            }

            // 5. 创建NewTopicInfo用于扩容操作
            NewTopicInfo newTopicInfo = new NewTopicInfo();
            newTopicInfo.setTopicName(topicName);
            newTopicInfo.setPartitions(newPartitions);
            // 副本数保持不变
            newTopicInfo.setReplication(existingTopic.getReplicas().shortValue());

            // 6. 调用KafkaSchemaFactory执行实际的Kafka扩容操作
            KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());
            boolean kafkaSuccess = ksf.increaseTopicPartitions(kafkaClientInfo, newTopicInfo);

            if (!kafkaSuccess) {
                log.error("扩容Topic失败：Kafka扩容操作失败，主题: {}, 新分区数: {}", topicName, newPartitions);
                return false;
            }

            // 7. Kafka扩容成功后，更新数据库中的分区数
            existingTopic.setPartitions(newPartitions);
            existingTopic.setUpdateBy(username);
            int updateResult = topicMapper.updateTopic(existingTopic);

            if (updateResult > 0) {
                return true;
            } else {
                log.error("Topic扩容失败：数据库更新失败，主题: {}", topicName);
                // 注意：此时Kafka中的分区已经扩容，但数据库更新失败
                // 可以考虑记录这种不一致状态，或者通过定时任务同步
                return false;
            }

        } catch (Exception e) {
            log.error("扩容Topic失败：{}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean setTopicRetention(String topicName, Long retentionMs, String clusterId,String username) {
        try {
            if (!StringUtils.hasText(topicName) || retentionMs == null || retentionMs <= 0) {
                log.error("设置Topic保留时间失败：参数不能为空");
                return false;
            }

            // 1. 先调用KafkaSchemaFactory更新Kafka中的保留时间
            KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());

            // 创建KafkaClientInfo
            KafkaClientInfo kafkaClientInfo = new KafkaClientInfo();
            kafkaClientInfo.setClusterId(clusterId);

            // 获取集群的broker信息用于连接
            List<BrokerInfo> brokerInfos = List.of();
            if (StringUtils.hasText(clusterId)) {
                brokerInfos = brokerMapper.getBrokersByClusterId(clusterId);
            }

            if (!brokerInfos.isEmpty()) {
                // 使用第一个broker的信息作为连接信息
                BrokerInfo firstBroker = brokerInfos.get(0);
                kafkaClientInfo.setBrokerServer(firstBroker.getHostIp() + ":" + firstBroker.getPort());
            } else {
                log.error("集群 {} 中未找到broker信息，请检查配置", clusterId);
                return false;
            }

            // 创建NewTopicInfo
            NewTopicInfo newTopicInfo = new NewTopicInfo();
            newTopicInfo.setTopicName(topicName);
            newTopicInfo.setRetainMs(retentionMs);

            // 调用KafkaSchemaFactory更新保留时间
            boolean kafkaUpdateSuccess = ksf.updateTopicRetention(kafkaClientInfo, newTopicInfo);
            if (!kafkaUpdateSuccess) {
                log.error("设置Topic保留时间失败：Kafka设置失败，主题: {}", topicName);
                return false;
            }

            // 2. Kafka设置成功后，更新数据库中的保留时间
            TopicInfo topicInfo = topicMapper.selectTopicByNameAndCluster(topicName, clusterId);
            if (topicInfo != null) {
                topicInfo.setRetentionTime(String.valueOf(retentionMs));
                topicInfo.setUpdateBy(username);
                int result = topicMapper.updateTopic(topicInfo);
                if (result > 0) {
                } else {
                    log.warn("数据库中的Topic保留时间更新失败，主题: {}", topicName);
                    // 注意：此时Kafka中的保留时间已经更新，但数据库更新失败
                }
            } else {
                log.warn("未找到Topic [{}] 在集群 [{}] 中的数据库记录", topicName, clusterId);
            }

            return true;

        } catch (Exception e) {
            log.error("设置Topic保留时间失败：{}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendTestData(String topicName, String dataType, Integer messageCount) {
        try {
            if (!StringUtils.hasText(topicName)) {
                log.error("发送测试数据失败：Topic名称不能为空");
                return false;
            }
            if (messageCount == null || messageCount <= 0) {
                messageCount = 10;
            }
            if (!StringUtils.hasText(dataType)) {
                dataType = "json";
            }

            // 此功能需要实现 Kafka Producer 发送测试数据
            // 当前未实现，返回 true 表示接口调用成功
            log.warn("发送测试数据功能尚未实现，主题: {}, 数据类型: {}, 消息数量: {}", topicName, dataType, messageCount);
            return true;

        } catch (Exception e) {
            log.error("向Topic [{}] 发送测试数据失败：{}", topicName, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<String> getAllTopicNames() {
        try {
            List<String> topicNames = topicMapper.selectAllTopicNames();
            return topicNames != null ? topicNames : List.of();
        } catch (Exception e) {
            log.error("获取主题名称列表失败：{}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public Map<String, Object> getTopicDetailedStats(String topicName, String clusterId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRecords", 0L);
        stats.put("totalSize", 0L);
        stats.put("writeSpeed", 0.0);
        stats.put("readSpeed", 0.0);

        try {
            if (!StringUtils.hasText(topicName)) {
                log.warn("主题名称为空，返回默认统计数据");
                return stats;
            }

            // 获取集群的broker信息
            List<BrokerInfo> brokerInfos = List.of();
            if (StringUtils.hasText(clusterId)) {
                brokerInfos = brokerMapper.getBrokersByClusterId(clusterId);
                if (brokerInfos.isEmpty()) {
                    log.warn("集群 {} 中未找到任何broker信息", clusterId);
                    return stats;
                }
            }

            // 创建KafkaClientInfo
            KafkaClientInfo kafkaClientInfo = new KafkaClientInfo();
            kafkaClientInfo.setClusterId(clusterId);
            if (!brokerInfos.isEmpty()) {
                // 使用第一个broker的信息作为连接信息
                BrokerInfo firstBroker = brokerInfos.get(0);
                kafkaClientInfo.setBrokerServer(firstBroker.getHostIp() + ":" + firstBroker.getPort());
            }

            KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());

            // 获取主题记录数
            try {
                Long capacity = ksf.getTopicRecordCapacityNum(kafkaClientInfo, brokerInfos, topicName);
                stats.put("totalSize", capacity != null ? capacity : 0L);
            } catch (Exception e) {
                log.warn("获取主题 {} 记录数失败：{}", topicName, e.getMessage());
            }

            // 获取主题大小
            try {
                // totalRecords
                Long logsize = ksf.getTotalActualTopicLogSize(kafkaClientInfo, topicName);
                stats.put("totalRecords", logsize != null ? logsize : 0L);
            } catch (Exception e) {
                log.warn("获取主题 {} 大小失败：{}", topicName, e.getMessage());
            }

            // 获取读写速度
            try {
                TopicDetailedStats topicStats = ksf.getTopicMetaData(kafkaClientInfo, topicName);
                if (topicStats != null && !brokerInfos.isEmpty()) {
                    // 获取写入和读取速度
                    BigDecimal writeSpeed = getTopicJmxMetric(brokerInfos, topicName, JmxMetricsConst.Server.BYTES_IN_PER_SEC_TOPIC.key());
                    BigDecimal readSpeed = getTopicJmxMetric(brokerInfos, topicName, JmxMetricsConst.Server.BYTES_OUT_PER_SEC_TOPIC.key());

                    stats.put("writeSpeed", writeSpeed != null ? writeSpeed.doubleValue() : 0.0);
                    stats.put("readSpeed", readSpeed != null ? readSpeed.doubleValue() : 0.0);
                }
            } catch (Exception e) {
                log.warn("获取主题 {} 元数据失败：{}", topicName, e.getMessage());
            }

        } catch (Exception e) {
            log.error("获取主题 {} 详细统计信息失败：{}", topicName, e.getMessage(), e);
        }

        return stats;
    }

    @Override
    public Map<String, Object> getTopicPartitionPage(String topicName, String clusterId, Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取broker信息
            List<BrokerInfo> brokerInfos = brokerMapper.getBrokersByClusterId(clusterId);

            if (brokerInfos.isEmpty()) {
                log.warn("未找到集群 {} 的broker信息", clusterId);
                result.put("records", List.of());
                result.put("total", 0);
                return result;
            }

            // 创建KafkaClientInfo
            KafkaClientInfo kafkaClientInfo = new KafkaClientInfo();
            kafkaClientInfo.setClusterId(clusterId);
            // 使用第一个broker的信息作为连接信息
            BrokerInfo firstBroker = brokerInfos.get(0);
            kafkaClientInfo.setBrokerServer(firstBroker.getHostIp() + ":" + firstBroker.getPort());

            // 调用KafkaSchemaFactory获取分区分页数据
            KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());
            TopicPartitionPageResult pageResult = ksf.getTopicPartitionPage(kafkaClientInfo, topicName, params);

            result.put("records", pageResult.getRecords());
            result.put("total", pageResult.getTotal());
        } catch (Exception e) {
            log.error("获取主题 {} 分区分页数据失败：{}", topicName, e.getMessage(), e);
            result.put("records", List.of());
            result.put("total", 0);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getTopicPartitionMessages(String topicName, String clusterId, Integer partition, Integer limit) {
        List<Map<String, Object>> messages = new ArrayList<>();

        try {
            // 获取broker信息
            List<BrokerInfo> brokerInfos = brokerMapper.getBrokersByClusterId(clusterId);

            if (brokerInfos.isEmpty()) {
                log.warn("未找到集群 {} 的broker信息", clusterId);
                return messages;
            }

            // 创建KafkaClientInfo
            KafkaClientInfo kafkaClientInfo = new KafkaClientInfo();
            kafkaClientInfo.setClusterId(clusterId);
            // 使用第一个broker的信息作为连接信息
            BrokerInfo firstBroker = brokerInfos.get(0);
            kafkaClientInfo.setBrokerServer(firstBroker.getHostIp() + ":" + firstBroker.getPort());

            // 调用KafkaSchemaFactory获取分区消息
            KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());
            String messagesJson = ksf.fetchLatestMessages(kafkaClientInfo, topicName, partition);

            // 解析JSON字符串为List<Map<String, Object>>
            if (messagesJson != null && !messagesJson.trim().isEmpty()) {
                try {
                    com.alibaba.fastjson2.JSONArray jsonArray = com.alibaba.fastjson2.JSONArray.parseArray(messagesJson);
                    for (int i = 0; i < jsonArray.size(); i++) {
                        com.alibaba.fastjson2.JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Map<String, Object> messageMap = new HashMap<>();
                        messageMap.put("partition", jsonObject.getInteger("partition"));
                        messageMap.put("offset", jsonObject.getLong("offset"));
                        messageMap.put("value", jsonObject.getString("value"));
                        messageMap.put("timestamp", jsonObject.getLong("timestamp"));
                        // 检查是否有key字段
                        if (jsonObject.containsKey("key")) {
                            messageMap.put("key", jsonObject.getString("key"));
                        }
                        messages.add(messageMap);
                    }
                } catch (Exception e) {
                    log.error("解析消息JSON失败: {}", e.getMessage(), e);
                }
            }

        } catch (Exception e) {
            log.error("获取主题 {} 分区 {} 消息失败：{}", topicName, partition, e.getMessage(), e);
        }

        return messages;
    }

    @Override
    public Map<String, String> getTopicConfig(String topicName, String clusterId) {
        Map<String, String> topicConfig = new HashMap<>();

        try {
            if (!StringUtils.hasText(topicName)) {
                log.warn("主题名称为空，返回空配置");
                return topicConfig;
            }

            // 获取集群的broker信息
            List<BrokerInfo> brokerInfos = List.of();
            if (StringUtils.hasText(clusterId)) {
                brokerInfos = brokerMapper.getBrokersByClusterId(clusterId);
                if (brokerInfos.isEmpty()) {
                    log.warn("集群 {} 中未找到任何broker信息", clusterId);
                    return topicConfig;
                }
            } else {
                log.warn("集群ID为空，无法获取主题配置");
                return topicConfig;
            }

            // 创建KafkaClientInfo
            KafkaClientInfo kafkaClientInfo = new KafkaClientInfo();
            kafkaClientInfo.setClusterId(clusterId);

            // 使用第一个broker的信息作为连接信息
            BrokerInfo firstBroker = brokerInfos.get(0);
            kafkaClientInfo.setBrokerServer(firstBroker.getHostIp() + ":" + firstBroker.getPort());

            // 调用KafkaSchemaFactory获取主题配置
            KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());
            topicConfig = ksf.getTopicConfig(kafkaClientInfo, topicName);

            if (topicConfig != null && !topicConfig.isEmpty()) {
            } else {
                log.warn("主题 {} 配置信息为空", topicName);
                topicConfig = new HashMap<>();
            }

        } catch (Exception e) {
            log.error("获取主题 {} 配置信息失败：{}", topicName, e.getMessage(), e);
            return new HashMap<>();
        }

        return topicConfig;
    }

    /**
     * 获取主题JMX指标
     */
    private BigDecimal getTopicJmxMetric(List<BrokerInfo> brokerInfos, String topicName, String metricKey) {
        try {
            BigDecimal totalMetric = BigDecimal.ZERO;
            int validBrokers = 0;

            for (BrokerInfo brokerInfo : brokerInfos) {
                try {
                    JMXInitializeInfo jmxInfo = new JMXInitializeInfo();
                    jmxInfo.setBrokerId(String.valueOf(brokerInfo.getBrokerId()));
                    jmxInfo.setHost(brokerInfo.getHostIp());
                    jmxInfo.setPort(brokerInfo.getJmxPort());

                    String objectName = String.format(metricKey, topicName);
                    BigDecimal metricValue = executeJmxOperation(jmxInfo, objectName, MBeanMetricsConst.Common.ONE_MINUTE_RATE.key());

                    if (metricValue != null) {
                        totalMetric = totalMetric.add(metricValue);
                        validBrokers++;
                    }
                } catch (Exception e) {
                    log.warn("获取Broker {} JMX指标失败：{}", brokerInfo.getBrokerId(), e.getMessage());
                }
            }

            return validBrokers > 0 ? totalMetric : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("获取主题 {} JMX指标失败：{}", topicName, e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 执行JMX操作
     */
    private BigDecimal executeJmxOperation(JMXInitializeInfo jmxInfo, String objectName, String attribute) {
        JMXConnector connector = null;
        try {
            String jmxUrl = String.format("service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi", jmxInfo.getHost(), jmxInfo.getPort());
            JMXServiceURL serviceURL = new JMXServiceURL(jmxUrl);
            connector = JMXConnectorFactory.connect(serviceURL);
            MBeanServerConnection connection = connector.getMBeanServerConnection();

            Object value = connection.getAttribute(new javax.management.ObjectName(objectName), attribute);
            if (value instanceof Number) {
                return new BigDecimal(value.toString());
            }
        } catch (Exception e) {
            log.warn("执行JMX操作失败：{}", e.getMessage());
        } finally {
            if (connector != null) {
                try {
                    connector.close();
                } catch (Exception e) {
                    log.warn("关闭JMX连接失败：{}", e.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * 根据Leader ID获取对应的Broker JMX连接信息
     */
    private JMXInitializeInfo getBrokerJmxRmiOfLeaderId(List<BrokerInfo> brokerInfos, Integer leadId) {
        JMXInitializeInfo initializeInfo = new JMXInitializeInfo();
        for (BrokerInfo brokerInfo : brokerInfos) {
            if (leadId.equals(brokerInfo.getBrokerId())) {
                initializeInfo.setBrokerId(String.valueOf(leadId));
                initializeInfo.setHost(brokerInfo.getHostIp());
                initializeInfo.setPort(brokerInfo.getJmxPort());
                break;
            }
        }
        return initializeInfo;
    }

    @Override
    public Map<String, Object> getTopicConsumerGroups(String topicName, String clusterId, Integer page, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 设置默认值
            int currentPage = page != null && page > 0 ? page : 1;
            int size = pageSize != null && pageSize > 0 ? pageSize : 5;
            int offset = (currentPage - 1) * size;

            // 查询消费者组数据
            List<Map<String, Object>> consumerGroups = consumerGroupTopicMapper.getConsumerGroupsByTopicForDetail(
                clusterId, topicName, offset, size);

            // 查询总数
            Long total = consumerGroupTopicMapper.countConsumerGroupsByTopicForDetail(clusterId, topicName);

            // 处理数据格式，转换为前端需要的格式
            List<Map<String, Object>> formattedGroups = new ArrayList<>();
            for (Map<String, Object> group : consumerGroups) {
                Map<String, Object> formattedGroup = new HashMap<>();
                formattedGroup.put("groupId", group.get("group_id"));
                formattedGroup.put("topicName", group.get("topic_name"));
                formattedGroup.put("logsize", group.get("logsize"));
                formattedGroup.put("offsets", group.get("offsets"));
                formattedGroup.put("lag", group.get("lags"));

                // 转换状态显示
                String state = (String) group.get("state");
                String stateDisplay;
                switch (state) {
                    case "EMPTY":
                        stateDisplay = "空闲";
                        break;
                    case "STABLE":
                        stateDisplay = "活跃";
                        break;
                    case "DEAD":
                        stateDisplay = "停止";
                        break;
                    default:
                        stateDisplay = state;
                }
                formattedGroup.put("state", stateDisplay);
                formattedGroup.put("stateCode", state); // 保留原始状态码

                formattedGroups.add(formattedGroup);
            }

            result.put("data", formattedGroups);
            result.put("total", total != null ? total : 0L);
            result.put("page", currentPage);
            result.put("pageSize", size);
            result.put("totalPages", total != null ? (int) Math.ceil((double) total / size) : 0);

        } catch (Exception e) {
            log.error("获取主题 {} 的消费者组信息失败：{}", topicName, e.getMessage(), e);
            result.put("data", new ArrayList<>());
            result.put("total", 0L);
            result.put("page", page != null ? page : 1);
            result.put("pageSize", pageSize != null ? pageSize : 5);
            result.put("totalPages", 0);
        }

        return result;
    }
}