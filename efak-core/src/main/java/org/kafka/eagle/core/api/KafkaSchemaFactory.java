/**
 * KafkaSchemaFactory.java
 * <p>
 * Copyright 2025 smartloli
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kafka.eagle.core.api;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.TopicConfig;
import org.kafka.eagle.core.constant.ClusterMetricsConst;
import org.kafka.eagle.core.constant.ConsumerGroupConst;
import org.kafka.eagle.core.constant.JmxMetricsConst;
import org.kafka.eagle.core.dto.ConsumerGroupDescInfo;
import org.kafka.eagle.core.util.MathUtils;
import org.kafka.eagle.core.util.NetUtils;
import org.kafka.eagle.core.util.StrUtils;
import org.kafka.eagle.dto.broker.BrokerInfo;
import org.kafka.eagle.dto.cluster.KafkaClientInfo;
import org.kafka.eagle.dto.consumer.ConsumerGroupDetailInfo;
import org.kafka.eagle.dto.consumer.ConsumerGroupTopicInfo;
import org.kafka.eagle.dto.jmx.JMXInitializeInfo;
import org.kafka.eagle.dto.topic.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * <p>
 * Kafka 主题与消费者元数据管理：支持创建、删除、分区管理、偏移量与日志大小统计等。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/9/27 16:01:42
 * @version 5.0.0
 */
@Slf4j
public class KafkaSchemaFactory {

    private final KafkaStoragePlugin plugin;

    public KafkaSchemaFactory(KafkaStoragePlugin plugin) {
        this.plugin = plugin;
    }

    /* ======================= TOPIC MANAGEMENT ======================= */

    /**
     * 创建新的 Kafka 主题
     */
    public boolean createTopicIfNotExists(KafkaClientInfo clientInfo, NewTopicInfo topicInfo) {
        return executeAdmin(clientInfo, admin -> {
            NewTopic newTopic = new NewTopic(topicInfo.getTopicName(), topicInfo.getPartitions(), topicInfo.getReplication());
            newTopic.configs(Collections.singletonMap(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(topicInfo.getRetainMs())));
            admin.createTopics(Collections.singleton(newTopic)).all().get();
        }, "创建主题");
    }

    /**
     * 为已存在的主题新增分区
     */
    public boolean increaseTopicPartitions(KafkaClientInfo clientInfo, NewTopicInfo topicInfo) {
        return executeAdmin(clientInfo, admin -> {
            Map<String, NewPartitions> partitionsMap = Collections.singletonMap(
                    topicInfo.getTopicName(), NewPartitions.increaseTo(topicInfo.getPartitions())
            );
            admin.createPartitions(partitionsMap).all().get();
        }, "增加主题分区");
    }

    /**
     * 更新主题保留期（retention.ms）
     */
    public boolean updateTopicRetention(KafkaClientInfo clientInfo, NewTopicInfo topicInfo) {
        return executeAdmin(clientInfo, admin -> {
            AlterConfigOp op = new AlterConfigOp(
                    new ConfigEntry(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(topicInfo.getRetainMs())),
                    AlterConfigOp.OpType.SET
            );
            ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topicInfo.getTopicName());
            Map<ConfigResource, Collection<AlterConfigOp>> update = new HashMap<>();
            update.put(resource, Collections.singleton(op));
            admin.incrementalAlterConfigs(update).all().get();
        }, "更新主题保留期");
    }

    /**
     * 获取主题配置
     */
    public Map<String, String> getTopicConfig(KafkaClientInfo clientInfo, String topic) {
        Map<String, String> configMap = new HashMap<>();
        try (AdminClient admin = AdminClient.create(plugin.buildAdminClientProps(clientInfo))) {
            ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topic);
            DescribeConfigsResult result = admin.describeConfigs(Collections.singleton(resource));
            Config config = result.all().get().get(resource);
            config.entries().forEach(entry -> configMap.put(entry.name(), entry.value()));
        } catch (Exception e) {
            log.error("获取主题 '{}' 在 '{}' 的配置失败：", topic, clientInfo, e);
        }

        return configMap;
    }

    /**
     * 删除已存在的主题
     */
    public boolean removeTopic(KafkaClientInfo clientInfo, String topic) {
        return executeAdmin(clientInfo, admin -> {
            admin.deleteTopics(Collections.singleton(topic)).all().get();
        }, "删除主题");
    }

    /**
     * 列出所有主题（排除内部偏移量主题）
     */
    public Set<String> listTopicNames(KafkaClientInfo clientInfo) {
        Set<String> topics = new HashSet<>();
        try (KafkaConsumer<?, ?> consumer = new KafkaConsumer<>(plugin.buildConsumerProps(clientInfo))) {
            topics.addAll(consumer.listTopics().keySet());
        } catch (Exception e) {
            log.error("列出 '{}' 的主题失败：", clientInfo, e);
        }
        topics.remove(ClusterMetricsConst.Cluster.CONSUMER_OFFSET_TOPIC.key());
        return topics;
    }

    /* ======================= TOPIC PARTITIONS ======================= */

    /**
     * 获取主题的所有分区ID
     */
    public Set<Integer> listTopicPartitions(KafkaClientInfo clientInfo, String topic) {
        Set<Integer> partitions = new HashSet<>();
        try (AdminClient admin = AdminClient.create(plugin.buildAdminClientProps(clientInfo))) {
            DescribeTopicsResult result = admin.describeTopics(Collections.singleton(topic));
            result.allTopicNames().get().get(topic).partitions().forEach(tp -> partitions.add(tp.partition()));
        } catch (Exception e) {
            log.error("获取主题 '{}' 的分区列表失败（{}）：", topic, clientInfo, e);
        }
        return partitions;
    }

    /**
     * 获取主题分区数量
     */
    public int getPartitionCount(KafkaClientInfo clientInfo, String topic) {
        return listTopicPartitions(clientInfo, topic).size();
    }

    /* ======================= TOPIC LOG SIZE ======================= */

    /**
     * 获取单个分区的日志大小
     */
    public long getPartitionLogSize(KafkaClientInfo clientInfo, String topic, int partitionId) {
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(plugin.buildConsumerProps(clientInfo))) {
            TopicPartition tp = new TopicPartition(topic, partitionId);
            consumer.assign(Collections.singleton(tp));
            long start = consumer.beginningOffsets(Collections.singleton(tp)).get(tp);
            long end = consumer.endOffsets(Collections.singleton(tp)).get(tp);
            return end - start;
        } catch (Exception e) {
            log.error("获取主题 '{}' 分区 {} 的日志大小失败：", topic, partitionId, e);
            return 0L;
        }
    }

    /**
     * 获取主题所有分区的日志总大小
     */
    public long getTotalTopicLogSize(KafkaClientInfo clientInfo, String topic) {
        long total = 0;
        Set<Integer> partitions = listTopicPartitions(clientInfo, topic);
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(plugin.buildConsumerProps(clientInfo))) {
            Set<TopicPartition> tps = partitions.stream().map(p -> new TopicPartition(topic, p)).collect(Collectors.toSet());
            consumer.assign(tps);
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(tps);
            for (TopicPartition tp : tps) {
                total += endOffsets.get(tp);
            }
        } catch (Exception e) {
            log.error("获取主题 '{}' 的日志总大小失败：", topic, e);
        }
        return total;
    }

    /**
     * 获取主题的实际日志大小（消息总数）
     */
    public long getTotalActualTopicLogSize(KafkaClientInfo clientInfo, String topic) {
        long total = 0;
        Set<Integer> partitions = listTopicPartitions(clientInfo, topic);
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(plugin.buildConsumerProps(clientInfo))) {
            Set<TopicPartition> tps = partitions.stream().map(p -> new TopicPartition(topic, p)).collect(Collectors.toSet());
            consumer.assign(tps);
            Map<TopicPartition, Long> startOffsets = consumer.beginningOffsets(tps);
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(tps);
            for (TopicPartition tp : tps) {
                total += endOffsets.get(tp) - startOffsets.get(tp);
            }
        } catch (Exception e) {
            log.error("获取主题 '{}' 的实际日志大小失败：", topic, e);
        }
        return total;
    }


    /**
     * 获取主题各分区的末端位移
     */
    public Map<TopicPartition, Long> getEndOffsets(KafkaClientInfo clientInfo, String topic) {
        Map<TopicPartition, Long> offsets = new HashMap<>();
        Set<Integer> partitions = listTopicPartitions(clientInfo, topic);
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(plugin.buildConsumerProps(clientInfo))) {
            Set<TopicPartition> tps = partitions.stream().map(p -> new TopicPartition(topic, p)).collect(Collectors.toSet());
            consumer.assign(tps);
            offsets.putAll(consumer.endOffsets(tps));
        } catch (Exception e) {
            log.error("获取主题 '{}' 的末端位移失败：", topic, e);
        }
        return offsets;
    }

    /* ======================= MESSAGE OPERATIONS ======================= */

    /**
     * 向指定主题发送一条消息
     */
    public boolean sendMessage(KafkaClientInfo clientInfo, String topic, String message) {
        try (Producer<String, String> producer = new KafkaProducer<>(plugin.buildProducerProps(clientInfo))) {
            producer.send(new ProducerRecord<>(topic, StrUtils.getUUid(), message));
            return true;
        } catch (Exception e) {
            log.error("发送消息 '{}' 至主题 '{}' 失败：", message, topic, e);
            return false;
        }
    }

    /**
     * 从指定分区拉取最新消息（默认最近10条）
     */
    public String fetchLatestMessages(KafkaClientInfo clientInfo, String topic, int partitionId) {
        JSONArray results = new JSONArray();
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(plugin.buildConsumerProps(clientInfo))) {
            TopicPartition tp = new TopicPartition(topic, partitionId);
            consumer.assign(Collections.singleton(tp));
            long end = consumer.endOffsets(Collections.singleton(tp)).get(tp);
            long start = Math.max(0, end - 10);
            consumer.seek(tp, start);

            boolean polling = true;
            while (polling) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    JSONObject obj = new JSONObject();
                    obj.put("partition", record.partition());
                    obj.put("offset", record.offset());
                    obj.put("value", record.value());
                    obj.put("timestamp", record.timestamp());
                    results.add(obj);
                }
                if (records.isEmpty()) polling = false;
            }
        } catch (Exception e) {
            log.error("获取主题 '{}' 的最新消息失败：", topic, e);
        }
        return results.toJSONString();
    }

    /* ======================= ADMIN CLIENT HELPER ======================= */

    /**
     * 执行 AdminClient 操作（带错误处理）
     */
    private boolean executeAdmin(KafkaClientInfo clientInfo, AdminAction action, String actionDesc) {
        try (AdminClient admin = AdminClient.create(plugin.buildAdminClientProps(clientInfo))) {
            action.execute(admin);
            return true;
        } catch (Exception e) {
            log.error("{} 在 '{}' 上执行失败：", actionDesc, clientInfo, e);
            return false;
        }
    }

    /**
     * Retrieve all broker information from the given Kafka cluster.
     *
     * @param clientInfo The Kafka bootstrap server address, e.g. "localhost:9092"
     * @return List<BrokerInfo> Broker 详情列表（brokerId、host、port）
     */
    public List<BrokerInfo> getClusterBrokers(KafkaClientInfo clientInfo) {
        try (AdminClient adminClient = AdminClient.create(plugin.buildAdminClientProps(clientInfo))) {
            DescribeClusterResult clusterResult = adminClient.describeCluster();
            Collection<Node> nodes = clusterResult.nodes().get();

            return nodes.stream()
                    .map(node -> {
                        BrokerInfo brokerInfo = new BrokerInfo();
                        brokerInfo.setBrokerId(node.id());
                        brokerInfo.setHostIp(node.host());
                        brokerInfo.setPort(node.port());
                        return brokerInfo;
                    })
                    .toList(); // Java 16+，如果你用的是 Java 8，可以换成 collect(Collectors.toList())
        } catch (Exception e) {
            log.error("获取 '{}' 的集群 Broker 列表失败：", clientInfo, e);
        }
        return Collections.emptyList();
    }

    /**
     * Get detailed topic metadata including broker spread and skew information for multiple topics
     *
     * @param clientInfo  Kafka client configuration
     * @param topics      Set of topic names to process
     * @param brokerInfos List of broker information for spread and skew calculations
     * @return Map of topic name to TopicDetailedStats containing detailed metadata
     */
    public Map<String, TopicDetailedStats> getTopicMetaData(KafkaClientInfo clientInfo, Set<String> topics, List<BrokerInfo> brokerInfos) {
        Map<String, TopicDetailedStats> topicMetas = new HashMap<>();

        if (topics == null || topics.isEmpty()) {
            return topicMetas;
        }

        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(plugin.buildAdminClientProps(clientInfo));

            // 1. Get topics description
            DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(topics);

            // 2. Prepare config resources for batch config retrieval
            List<ConfigResource> configResources = new ArrayList<>();
            for (String topic : topics) {
                ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topic);
                configResources.add(resource);
            }
            DescribeConfigsResult describeConfigsResult = adminClient.describeConfigs(configResources);
            Map<ConfigResource, Config> topicConfigDescMap = describeConfigsResult.all().get();

            // 3. Process each topic
            for (Map.Entry<String, TopicDescription> entry : describeTopicsResult.allTopicNames().get().entrySet()) {
                String topicName = entry.getKey();
                TopicDescription description = entry.getValue();

                TopicDetailedStats topicMetaData = new TopicDetailedStats();
                topicMetaData.setTopicName(topicName);

                // Get basic topic information
                List<TopicPartitionInfo> partitions = description.partitions();
                topicMetaData.setPartitionCount(partitions.size());
                topicMetaData.setReplicationFactor(partitions.isEmpty() ? (short) 0 : (short) partitions.get(0).replicas().size());

                // Get topic configuration
                ConfigResource configResource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
                Config config = topicConfigDescMap.get(configResource);
                if (config != null) {
                    ConfigEntry retentionEntry = config.get(TopicConfig.RETENTION_MS_CONFIG);
                    if (retentionEntry != null) {
                        topicMetaData.setRetentionMs(retentionEntry.value());
                    }
                }

                // Calculate broker spread and skew
                calculateBrokerMetrics(topicMetaData, partitions, brokerInfos);

                topicMetas.put(topicName, topicMetaData);
            }
        } catch (Exception e) {
            log.error("获取集群 '{}' 中主题 '{}' 的元数据失败: ", clientInfo.getClusterId(), topics, e);
        } finally {
            if (adminClient != null) {
                plugin.registerResourceForClose(adminClient);
            }
        }

        return topicMetas;
    }

    /**
     * Get detailed topic metadata including broker spread and skew information for multiple topics (without broker info)
     *
     * @param clientInfo Kafka client configuration
     * @param topics     Set of topic names to process
     * @return Map of topic name to TopicDetailedStats containing detailed metadata
     */
    public Map<String, TopicDetailedStats> getTopicMetaData(KafkaClientInfo clientInfo, Set<String> topics) {
        return getTopicMetaData(clientInfo, topics, Collections.emptyList());
    }

    /**
     * Get detailed topic metadata for a single topic (backward compatibility)
     *
     * @param clientInfo Kafka client configuration
     * @param topicName  Topic name
     * @return TopicDetailedStats containing detailed metadata
     */
    public TopicDetailedStats getTopicMetaData(KafkaClientInfo clientInfo, String topicName) {
        Map<String, TopicDetailedStats> result = getTopicMetaData(clientInfo, Collections.singleton(topicName));
        return result.getOrDefault(topicName, new TopicDetailedStats());
    }

    /**
     * Calculate broker spread and skew information using enhanced logic
     *
     * @param topicMetaData Topic metadata to update
     * @param partitions    Topic partition information
     * @param brokerInfos   List of broker information for calculations
     */
    private void calculateBrokerMetrics(TopicDetailedStats topicMetaData, List<TopicPartitionInfo> partitions, List<BrokerInfo> brokerInfos) {
        if (partitions.isEmpty()) {
            return;
        }

        // Initialize counters
        int partitionAndReplicaTopics = 0;
        Map<Integer, Integer> brokers = new HashMap<>();
        Set<Integer> brokerSizes = new HashSet<>();
        Map<Integer, Integer> brokerLeaders = new HashMap<>();

        // Process each partition to collect broker statistics
        for (TopicPartitionInfo partition : partitions) {
            // Collect replica information
            List<Integer> replicaIds = new ArrayList<>();
            for (Node replica : partition.replicas()) {
                replicaIds.add(replica.id());
            }

            brokerSizes.addAll(replicaIds);
            partitionAndReplicaTopics += replicaIds.size();

            // Count replicas per broker
            for (Integer brokerId : replicaIds) {
                brokers.put(brokerId, brokers.getOrDefault(brokerId, 0) + 1);
            }

            // Count leaders per broker
            if (partition.leader() != null) {
                int leaderId = partition.leader().id();
                brokerLeaders.put(leaderId, brokerLeaders.getOrDefault(leaderId, 0) + 1);
            }
        }

        // Calculate broker spread, skew, and leader skew using the reference logic
        int brokerSize = brokerInfos.size();
        if (brokerSize == 0) {
            // Fallback: use actual brokers involved in the topic
            brokerSize = brokerSizes.size();
        }

        int spread = 0;
        int skewed = 0;
        int leaderSkewed = 0;

        if (brokerSize > 0) {
            // Calculate broker spread: percentage of brokers used for this topic
            spread = brokerSizes.size() * 100 / brokerSize;

            // Calculate broker skew: percentage of brokers that have more than normal replicas
            int normalSkewedValue = MathUtils.ceil(partitionAndReplicaTopics, brokerSize);
            int brokerSkewSize = 0;
            for (Map.Entry<Integer, Integer> entry : brokers.entrySet()) {
                if (entry.getValue() > normalSkewedValue) {
                    brokerSkewSize++;
                }
            }
            skewed = brokerSkewSize * 100 / brokerSize;

            // Calculate leader skew: percentage of brokers that have more than normal leaders
            int brokerSkewLeaderNormal = MathUtils.ceil(partitions.size(), brokerSize);
            int brokerSkewLeaderSize = 0;
            for (Map.Entry<Integer, Integer> entry : brokerLeaders.entrySet()) {
                if (entry.getValue() > brokerSkewLeaderNormal) {
                    brokerSkewLeaderSize++;
                }
            }
            leaderSkewed = brokerSkewLeaderSize * 100 / brokerSize;
        }

        // Set calculated values
        topicMetaData.setBrokerSpread(spread);
        topicMetaData.setBrokerSkewed(skewed);
        topicMetaData.setLeaderSkewed(leaderSkewed);

        log.debug("主题 '{}' 代理指标 - 分布: {}%, 代理倾斜: {}%, 领导者倾斜: {}% " +
                        "(代理大小: {}, 代理大小数: {}, 分区和副本主题: {})", topicMetaData.getTopicName(), spread, skewed, leaderSkewed, brokerSize, brokerSizes.size(), partitionAndReplicaTopics);
    }

    /**
     * Calculate broker spread and skew information (backward compatibility without broker info)
     */
    private void calculateBrokerMetrics(TopicDetailedStats topicMetaData, List<TopicPartitionInfo> partitions) {
        calculateBrokerMetrics(topicMetaData, partitions, Collections.emptyList());
    }

    /**
     * Get topic record capacity count by summing up all partition sizes
     *
     * @param kafkaClientInfo Kafka client configuration
     * @param brokerInfos     List of broker information for JMX connections
     * @param topic           Topic name to get capacity for
     * @return Total record count across all partitions
     */
    public Long getTopicRecordCapacityNum(KafkaClientInfo kafkaClientInfo, List<BrokerInfo> brokerInfos, String topic) {
        Long capacity = 0L;
        try {
            List<MetadataInfo> metadataInfos = getTopicPartitionMetadata(kafkaClientInfo, topic);
            for (MetadataInfo metadataInfo : metadataInfos) {
                JMXInitializeInfo initializeInfo = getBrokerJmxRmiOfLeaderId(brokerInfos, metadataInfo.getLeader());
                if (NetUtils.telnet(initializeInfo.getHost(), initializeInfo.getPort())) {
                    initializeInfo.setObjectName(String.format(JmxMetricsConst.Log.SIZE.key(), topic, metadataInfo.getPartitionId()));
                    capacity += KafkaClusterFetcher.fetchTopicRecordCount(initializeInfo);
                }
            }
        } catch (Exception e) {
            log.error("获取主题 '{}' 的记录容量失败: ", topic, e);
        }
        return capacity;
    }

    /**
     * Get topic partition metadata information including partition details
     *
     * @param kafkaClientInfo Kafka client configuration
     * @param topic           Topic name
     * @return List of MetadataInfo containing partition details
     */
    public List<MetadataInfo> getTopicPartitionMetadata(KafkaClientInfo kafkaClientInfo, String topic) {
        List<MetadataInfo> metadataInfos = new ArrayList<>();

        try (AdminClient adminClient = AdminClient.create(plugin.buildAdminClientProps(kafkaClientInfo))) {
            DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Collections.singleton(topic));
            TopicDescription description = describeTopicsResult.allTopicNames().get().get(topic);

            if (description != null) {
                for (TopicPartitionInfo partitionInfo : description.partitions()) {
                    MetadataInfo metadataInfo = new MetadataInfo();
                    metadataInfo.setPartitionId(partitionInfo.partition());
                    metadataInfo.setLeader(partitionInfo.leader() != null ? partitionInfo.leader().id() : -1);

                    // Set replicas as comma-separated string
                    String replicas = partitionInfo.replicas().stream()
                            .map(node -> String.valueOf(node.id()))
                            .collect(Collectors.joining(","));
                    metadataInfo.setReplicas(replicas);

                    // Set ISR as comma-separated string
                    String isr = partitionInfo.isr().stream()
                            .map(node -> String.valueOf(node.id()))
                            .collect(Collectors.joining(","));
                    metadataInfo.setIsr(isr);

                    // Get log size for this partition
                    long logSize = getPartitionLogSize(kafkaClientInfo, topic, partitionInfo.partition());
                    metadataInfo.setLogSize(logSize);

                    metadataInfos.add(metadataInfo);
                }
            }
        } catch (Exception e) {
            log.error("获取集群 '{}' 中主题 '{}' 的元数据失败: ", kafkaClientInfo.getClusterId(), topic, e);
        }

        return metadataInfos;
    }

    /**
     * Get JMX initialization info for a specific broker leader
     *
     * @param brokerInfos List of broker information
     * @param leadId      Leader broker ID
     * @return JMXInitializeInfo configured for the leader broker
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

    /**
     * Get paginated topic partition information with detailed metadata
     *
     * @param kafkaClientInfo Kafka client configuration
     * @param topic           Topic name
     * @param params          Pagination parameters (start, length)
     * @return TopicPartitionPageResult with paginated partition data
     */
    public TopicPartitionPageResult getTopicPartitionPage(KafkaClientInfo kafkaClientInfo, String topic, Map<String, Object> params) {
        TopicPartitionPageResult result = new TopicPartitionPageResult();
        AdminClient adminClient = null;

        try {
            adminClient = AdminClient.create(plugin.buildAdminClientProps(kafkaClientInfo));
            DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Collections.singleton(topic));

            Map<String, TopicDescription> topicDescriptions = describeTopicsResult.allTopicNames().get();
            TopicDescription topicDescription = topicDescriptions.get(topic);

            if (topicDescription == null) {
                log.warn("在集群 '{}' 中未找到主题 '{}'", topic, kafkaClientInfo.getClusterId());
                result.setTotal(0);
                return result;
            }

            List<TopicPartitionInfo> partitions = topicDescription.partitions();
            result.setTotal(partitions.size());

            // Sort partitions by partition ID (ascending order)
            partitions.sort(Comparator.comparing(TopicPartitionInfo::partition));

            // Apply pagination
            int start = Integer.parseInt(params.getOrDefault("start", "0").toString());
            int length = Integer.parseInt(params.getOrDefault("length", "10").toString());

            int endIndex = Math.min(start + length, partitions.size());
            List<TopicPartitionInfo> pagePartitions = partitions.subList(start, endIndex);

            // Build partition records with detailed information
            for (TopicPartitionInfo partitionInfo : pagePartitions) {
                TopicRecordInfo recordInfo = buildTopicRecordInfo(kafkaClientInfo, topic, partitionInfo);
                result.getRecords().add(recordInfo);
            }

            log.debug("Retrieved {} partition records for topic '{}' (start: {}, length: {})",
                    result.getRecords().size(), topic, start, length);

        } catch (Exception e) {
            log.error("获取集群 '{}' 中主题 '{}' 的分区页面失败: ", kafkaClientInfo.getClusterId(), topic, e);
            result.setTotal(0);
        } finally {
            if (adminClient != null) {
                plugin.registerResourceForClose(adminClient);
            }
        }

        return result;
    }

    /**
     * Build detailed TopicRecordInfo for a single partition
     *
     * @param kafkaClientInfo Kafka client configuration
     * @param topic           Topic name
     * @param partitionInfo   Partition information from Kafka
     * @return TopicRecordInfo with complete partition details
     */
    private TopicRecordInfo buildTopicRecordInfo(KafkaClientInfo kafkaClientInfo, String topic, TopicPartitionInfo partitionInfo) {
        TopicRecordInfo recordInfo = new TopicRecordInfo();

        // Basic partition information
        recordInfo.setPartitionId(partitionInfo.partition());

        // Leader information
        if (partitionInfo.leader() != null) {
            recordInfo.setLeader(String.valueOf(partitionInfo.leader().id()));
        } else {
            recordInfo.setLeader("-1"); // No leader
        }

        // Replicas information
        String replicas = partitionInfo.replicas().stream()
                .map(node -> String.valueOf(node.id()))
                .collect(Collectors.joining(","));
        recordInfo.setReplicas(replicas);

        // ISR (In-Sync Replicas) information
        String isr = partitionInfo.isr().stream()
                .map(node -> String.valueOf(node.id()))
                .collect(Collectors.joining(","));
        recordInfo.setIsr(isr);

        // Under replicated status
        boolean underReplicated = partitionInfo.isr().size() < partitionInfo.replicas().size();
        recordInfo.setUnderReplicated(underReplicated);

        // Preferred leader status
        boolean preferredLeader = false;
        if (partitionInfo.leader() != null && !partitionInfo.replicas().isEmpty()) {
            // Preferred leader is the first replica in the replica list
            int firstReplicaId = partitionInfo.replicas().get(0).id();
            int currentLeaderId = partitionInfo.leader().id();
            preferredLeader = (firstReplicaId == currentLeaderId);
        }
        recordInfo.setPreferredLeader(preferredLeader);

        // Log size (message count) for this partition
        try {
            long logSize = getPartitionLogSize(kafkaClientInfo, topic, partitionInfo.partition());
            recordInfo.setLogSize(logSize);
        } catch (Exception e) {
            log.warn("获取主题 '{}' 分区 {} 的日志大小失败: {}", topic, partitionInfo.partition(), e.getMessage());
            recordInfo.setLogSize(0L);
        }

        return recordInfo;
    }

    /* ======================= CONSUMER GROUP OPERATIONS ======================= */

    /**
     * Get all consumer groups information including their descriptions
     *
     * @param kafkaClientInfo Kafka client configuration
     * @return ConsumerGroupDescInfo containing consumer group IDs and descriptions
     */
    public ConsumerGroupDescInfo getKafkaConsumerGroups(KafkaClientInfo kafkaClientInfo) {
        AdminClient adminClient = null;
        ConsumerGroupDescInfo consumerGroupDescInfo = new ConsumerGroupDescInfo();
        try {
            adminClient = AdminClient.create(plugin.buildAdminClientProps(kafkaClientInfo));
            for (ConsumerGroupListing consumerGroupListing : adminClient.listConsumerGroups().all().get()) {
                String groupId = consumerGroupListing.groupId();
                if (!groupId.equals(ClusterMetricsConst.Cluster.EFAK_SYSTEM_GROUP.key())) {
                    consumerGroupDescInfo.getGroupIds().add(groupId);
                }
            }
            Map<String, ConsumerGroupDescription> descConsumerGroup = adminClient.describeConsumerGroups(consumerGroupDescInfo.getGroupIds()).all().get();
            consumerGroupDescInfo.getDescConsumerGroup().putAll(descConsumerGroup);
        } catch (Exception e) {
            log.error("获取数据库 '{}' 的消费者组对象失败: ", kafkaClientInfo, e);
        } finally {
            if (adminClient != null) {
                plugin.registerResourceForClose(adminClient);
            }
        }

        return consumerGroupDescInfo;
    }

    /**
     * Get consumer group topic logsize, offset, and lag information
     *
     * @param kafkaClientInfo Kafka client configuration
     * @param groupIds        Set of consumer group IDs to process
     * @return List of ConsumerGroupTopicInfo containing offset and lag data
     */
    public List<ConsumerGroupTopicInfo> getKafkaConsumerGroupTopic(KafkaClientInfo kafkaClientInfo, Set<String> groupIds) {
        List<ConsumerGroupTopicInfo> consumerGroupTopicInfos = new ArrayList<>();
        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(plugin.buildAdminClientProps(kafkaClientInfo));

            // 批量获取消费者组状态，减少API请求次数
            Map<String, ConsumerGroupDescription> descConsumerGroup = adminClient.describeConsumerGroups(groupIds).all().get();

            for (String groupId : groupIds) {
                Map<String, Long> topicOffsetsMap = new HashMap<>();
                ListConsumerGroupOffsetsResult offsetsResult = adminClient.listConsumerGroupOffsets(groupId);
                Map<TopicPartition, OffsetAndMetadata> offsets = offsetsResult.partitionsToOffsetAndMetadata().get();
                if (offsets != null && !offsets.isEmpty()) {
                    for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : offsets.entrySet()) {
                        if (topicOffsetsMap.containsKey(entry.getKey().topic())) {
                            topicOffsetsMap.put(entry.getKey().topic(), topicOffsetsMap.get(entry.getKey().topic()) + entry.getValue().offset());
                        } else {
                            topicOffsetsMap.put(entry.getKey().topic(), entry.getValue().offset());
                        }
                    }
                }
                for (Map.Entry<String, Long> entry : topicOffsetsMap.entrySet()) {
                    ConsumerGroupTopicInfo consumerGroupTopicInfo = new ConsumerGroupTopicInfo();
                    consumerGroupTopicInfo.setClusterId(kafkaClientInfo.getClusterId());
                    consumerGroupTopicInfo.setGroupId(groupId);
                    consumerGroupTopicInfo.setTopicName(entry.getKey());

                    // 使用批量获取的消费者组状态
                    if (descConsumerGroup.containsKey(groupId)) {
                        consumerGroupTopicInfo.setState(descConsumerGroup.get(groupId).state().name());
                    } else {
                        consumerGroupTopicInfo.setState("UNKNOWN");
                    }

                    Long logsizeValue = getTotalTopicLogSize(kafkaClientInfo, entry.getKey());
                    Long offsetValue = entry.getValue();
                    consumerGroupTopicInfo.setOffsets(offsetValue);
                    consumerGroupTopicInfo.setLogsize(logsizeValue);
                    // Calculate lag
                    consumerGroupTopicInfo.setLags(Math.abs(logsizeValue - offsetValue));

                    // Set collection time and date
                    java.time.LocalDateTime now = java.time.LocalDateTime.now();
                    consumerGroupTopicInfo.setCollectTime(now);
                    consumerGroupTopicInfo.setCollectDate(now.toLocalDate());

                    // add to list
                    consumerGroupTopicInfos.add(consumerGroupTopicInfo);
                }
            }
        } catch (Exception e) {
            log.error("获取消费者组主题偏移量时出错，数据库 {}: ", kafkaClientInfo, e);
        } finally {
            if (adminClient != null) {
                plugin.registerResourceForClose(adminClient);
            }
        }
        return consumerGroupTopicInfos;
    }

    /**
     * 获取消费者组ID列表
     */
    public Set<String> getConsumerGroupIds(KafkaClientInfo kafkaClientInfo) {
        AdminClient adminClient = null;
        Set<String> groupIdSets = new HashSet<>();
        try {
            adminClient = AdminClient.create(plugin.buildAdminClientProps(kafkaClientInfo));

            for (ConsumerGroupListing consumerGroupListing : adminClient.listConsumerGroups().all().get()) {
                String groupId = consumerGroupListing.groupId();
                if (!groupId.equals(ClusterMetricsConst.Cluster.EFAK_SYSTEM_GROUP.key())) {
                    groupIdSets.add(groupId);
                }
            }

        } catch (Exception e) {
            log.error("加载数据库 '{}' 的 Kafka 客户端失败: ", kafkaClientInfo, e);
        } finally {
            if (adminClient != null) {
                plugin.registerResourceForClose(adminClient);
            }
        }
        return groupIdSets;
    }

    /**
     * Get detailed consumer group information for all consumer groups
     *
     * @param kafkaClientInfo Kafka client configuration
     * @return List of ConsumerGroupDetailInfo containing detailed consumer group information
     */
    public List<ConsumerGroupDetailInfo> getConsumerGroups(KafkaClientInfo kafkaClientInfo) {
        AdminClient adminClient = null;
        List<ConsumerGroupDetailInfo> consumerGroupInfos = new ArrayList<>();

        try {
            adminClient = AdminClient.create(plugin.buildAdminClientProps(kafkaClientInfo));
            Iterator<ConsumerGroupListing> itors = adminClient.listConsumerGroups().all().get().iterator();
            Set<String> groupIdSets = new HashSet<>();
            while (itors.hasNext()) {
                String groupId = itors.next().groupId();
                if (!groupId.equals(ClusterMetricsConst.Cluster.EFAK_SYSTEM_GROUP.key())) {
                    groupIdSets.add(groupId);
                }
            }
            Map<String, ConsumerGroupDescription> descConsumerGroup = adminClient.describeConsumerGroups(groupIdSets).all().get();
            for (String groupId : groupIdSets) {
                if (descConsumerGroup.containsKey(groupId)) {// active
                    ConsumerGroupDescription consumerGroupDescription = descConsumerGroup.get(groupId);
                    ConsumerGroupDetailInfo consumerGroupInfo = new ConsumerGroupDetailInfo();
                    consumerGroupInfo.setClusterId(kafkaClientInfo.getClusterId());
                    consumerGroupInfo.setGroupId(groupId);
                    consumerGroupInfo.setState(consumerGroupDescription.state().name());
                    Node node = consumerGroupDescription.coordinator();
                    consumerGroupInfo.setCoordinator(node.host() + ":" + node.port());
                    Collection<MemberDescription> members = consumerGroupDescription.members();
                    if (members != null && members.size() > 0) {
                        members.iterator().forEachRemaining(member -> {
                            consumerGroupInfo.setOwner(member.host().replaceAll("/", "") + "-" + member.consumerId());
                            consumerGroupInfo.setStatus(StrUtils.isNotBlank(member.consumerId()) ? ConsumerGroupConst.Status.RUNNING : ConsumerGroupConst.Status.SHUTDOWN);
                            Set<TopicPartition> tps = member.assignment().topicPartitions();
                            if (tps != null && tps.size() > 0) {
                                Set<String> topics = tps.stream().map(TopicPartition::topic).collect(Collectors.toSet());
                                topics.forEach(topic -> {
                                    ConsumerGroupDetailInfo consumerGroupInfoSub = null;
                                    try {
                                        consumerGroupInfoSub = (ConsumerGroupDetailInfo) consumerGroupInfo.clone();
                                        consumerGroupInfoSub.setTopicName(topic);
                                    } catch (Exception e) {
                                        log.error("Copy object value has error, msg is {}", e);
                                    }
                                    consumerGroupInfos.add(consumerGroupInfoSub);
                                });
                            } else {
                                consumerGroupInfos.add(consumerGroupInfo);
                            }
                        });
                    } else {
                        consumerGroupInfos.add(consumerGroupInfo);
                    }
                } else {
                    ConsumerGroupDetailInfo consumerGroupInfo = new ConsumerGroupDetailInfo();
                    consumerGroupInfo.setClusterId(kafkaClientInfo.getClusterId());
                    consumerGroupInfo.setGroupId(groupId);
                    consumerGroupInfo.setState("DEAD");
                    consumerGroupInfos.add(consumerGroupInfo);
                }
            }
        } catch (Exception e) {
            log.error("加载数据库 '{}' 的 Kafka 客户端失败: ", kafkaClientInfo, e);
        } finally {
            if (adminClient != null) {
                plugin.registerResourceForClose(adminClient);
            }
        }

        return consumerGroupInfos;
    }

    /**
     * Get detailed consumer group information for a specific consumer group
     *
     * @param kafkaClientInfo Kafka client configuration
     * @param groupIds        Consumer group list to query
     * @return List of ConsumerGroupDetailInfo containing detailed consumer group information
     */
    public List<ConsumerGroupDetailInfo> getConsumerGroups(KafkaClientInfo kafkaClientInfo, Set<String> groupIds) {
        AdminClient adminClient = null;
        List<ConsumerGroupDetailInfo> consumerGroupInfos = new ArrayList<>();

        try {
            adminClient = AdminClient.create(plugin.buildAdminClientProps(kafkaClientInfo));

            Map<String, ConsumerGroupDescription> descConsumerGroup = adminClient.describeConsumerGroups(groupIds).all().get();
            for (String groupId : groupIds) {
                if (descConsumerGroup.containsKey(groupId)) {// active
                    ConsumerGroupDescription consumerGroupDescription = descConsumerGroup.get(groupId);
                    ConsumerGroupDetailInfo consumerGroupInfo = new ConsumerGroupDetailInfo();
                    consumerGroupInfo.setClusterId(kafkaClientInfo.getClusterId());
                    consumerGroupInfo.setGroupId(groupId);
                    consumerGroupInfo.setState(consumerGroupDescription.state().name());
                    Node node = consumerGroupDescription.coordinator();
                    consumerGroupInfo.setCoordinator(node.host() + ":" + node.port());
                    Collection<MemberDescription> members = consumerGroupDescription.members();
                    if (members != null && !members.isEmpty()) {
                        members.iterator().forEachRemaining(member -> {
                            consumerGroupInfo.setOwner(member.host().replaceAll("/", "") + "-" + member.consumerId());
                            consumerGroupInfo.setStatus(StrUtils.isNotBlank(member.consumerId()) ? ConsumerGroupConst.Status.RUNNING : ConsumerGroupConst.Status.SHUTDOWN);
                            Set<TopicPartition> tps = member.assignment().topicPartitions();
                            if (tps != null && !tps.isEmpty()) {
                                Set<String> topics = tps.stream().map(TopicPartition::topic).collect(Collectors.toSet());
                                topics.forEach(topic -> {
                                    ConsumerGroupDetailInfo consumerGroupInfoSub = null;
                                    try {
                                        consumerGroupInfoSub = (ConsumerGroupDetailInfo) consumerGroupInfo.clone();
                                        consumerGroupInfoSub.setTopicName(topic);
                                    } catch (Exception e) {
                                        log.error("复制对象值时出错，消息是 ", e);
                                    }
                                    consumerGroupInfos.add(consumerGroupInfoSub);
                                });
                            } else {
                                consumerGroupInfos.add(consumerGroupInfo);
                            }
                        });
                    } else {
                        consumerGroupInfos.add(consumerGroupInfo);
                    }
                } else {
                    ConsumerGroupDetailInfo consumerGroupInfo = new ConsumerGroupDetailInfo();
                    consumerGroupInfo.setClusterId(kafkaClientInfo.getClusterId());
                    consumerGroupInfo.setGroupId(groupId);
                    consumerGroupInfo.setState("DEAD");
                    consumerGroupInfos.add(consumerGroupInfo);
                }
            }
        } catch (Exception e) {
            log.error("Failure while loading kafka client for database '{}': ", kafkaClientInfo, e);
        } finally {
            if (adminClient != null) {
                plugin.registerResourceForClose(adminClient);
            }
        }

        return consumerGroupInfos;
    }

    @FunctionalInterface
    private interface AdminAction {
        void execute(AdminClient admin) throws ExecutionException, InterruptedException;
    }

    /* ======================= CONSUMER GROUP OFFSET MANAGEMENT ======================= */

    /**
     * 重置消费者组的 topic 偏移量（全局生效）
     *
     * @param clientInfo Kafka客户端配置信息
     * @param groupId     消费者组 ID
     * @param topic       目标 topic
     * @param mode        重置模式: earliest, latest, timestamp, specific
     * @param value       当 mode=timestamp 时是毫秒时间戳；
     *                    当 mode=specific 时是指定 offset；
     *                    其他模式忽略
     * @return true 重置成功，false 重置失败
     */
    public boolean resetConsumerGroupOffsets(KafkaClientInfo clientInfo, String groupId,
                                           String topic, String mode, Long value) {
        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(plugin.buildAdminClientProps(clientInfo));

            // 1. 获取 topic 的分区信息
            DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Collections.singleton(topic));
            TopicDescription topicDescription = describeTopicsResult.topicNameValues().get(topic).get();

            List<TopicPartition> partitions = new ArrayList<>();
            for (TopicPartitionInfo pInfo : topicDescription.partitions()) {
                partitions.add(new TopicPartition(topic, pInfo.partition()));
            }

            // 2. 准备新的 offset 映射
            Map<TopicPartition, OffsetAndMetadata> newOffsets = new HashMap<>();

            switch (mode.toLowerCase()) {
                case "earliest":
                case "latest": {
                    // 使用 AdminClient 的 ListOffsets API
                    Map<TopicPartition, OffsetSpec> request = new HashMap<>();
                    for (TopicPartition tp : partitions) {
                        request.put(tp, mode.equals("earliest") ? OffsetSpec.earliest() : OffsetSpec.latest());
                    }
                    ListOffsetsResult offsetsResult = adminClient.listOffsets(request);
                    for (TopicPartition tp : partitions) {
                        long offset = offsetsResult.partitionResult(tp).get().offset();
                        newOffsets.put(tp, new OffsetAndMetadata(offset));
                    }
                    break;
                }

                case "specific": {
                    if (value == null) {
                        log.error("特定偏移量模式需要值");
                        return false;
                    }
                    for (TopicPartition tp : partitions) {
                        newOffsets.put(tp, new OffsetAndMetadata(value));
                    }
                    break;
                }

                case "timestamp": {
                    if (value == null) {
                        log.error("时间戳模式需要值");
                        return false;
                    }
                    Map<TopicPartition, OffsetSpec> request = new HashMap<>();
                    for (TopicPartition tp : partitions) {
                        request.put(tp, OffsetSpec.forTimestamp(value));
                    }
                    ListOffsetsResult offsetsResult = adminClient.listOffsets(request);
                    for (TopicPartition tp : partitions) {
                        ListOffsetsResult.ListOffsetsResultInfo info = offsetsResult.partitionResult(tp).get();
                        if (info != null && info.offset() >= 0) {
                            newOffsets.put(tp, new OffsetAndMetadata(info.offset()));
                        } else {
                            log.warn("No valid offset found for partition {} at timestamp {}", tp.partition(), value);
                            // 如果指定时间戳没有找到对应的偏移量，使用最早的偏移量
                            Map<TopicPartition, OffsetSpec> fallbackRequest = new HashMap<>();
                            fallbackRequest.put(tp, OffsetSpec.earliest());
                            ListOffsetsResult fallbackResult = adminClient.listOffsets(fallbackRequest);
                            long fallbackOffset = fallbackResult.partitionResult(tp).get().offset();
                            newOffsets.put(tp, new OffsetAndMetadata(fallbackOffset));
                        }
                    }
                    break;
                }

                default:
                    log.error("Unsupported reset mode: {}", mode);
                    return false;
            }

            // 3. 提交新的偏移量
            adminClient.alterConsumerGroupOffsets(groupId, newOffsets).all().get();

            log.info("Successfully reset consumer group [{}] offsets for topic [{}] to mode [{}]",
                    groupId, topic, mode);
            return true;

        } catch (Exception e) {
            log.error("Failed to reset consumer group [{}] offsets for topic [{}] with mode [{}]: ",
                    groupId, topic, mode, e);
            return false;
        } finally {
            if (adminClient != null) {
                plugin.registerResourceForClose(adminClient);
            }
        }
    }

    /**
     * 重置消费者组的所有 topic 偏移量
     *
     * @param clientInfo Kafka客户端配置信息
     * @param groupId     消费者组 ID
     * @param mode        重置模式: earliest, latest, timestamp, specific
     * @param value       当 mode=timestamp 时是毫秒时间戳；
     *                    当 mode=specific 时是指定 offset；
     *                    其他模式忽略
     * @return true 重置成功，false 重置失败
     */
    public boolean resetConsumerGroupOffsetsForAllTopics(KafkaClientInfo clientInfo, String groupId,
                                                        String mode, Long value) {
        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(plugin.buildAdminClientProps(clientInfo));

            // 1. 获取消费者组消费的所有 topic 和分区
            ListConsumerGroupOffsetsResult offsetsResult = adminClient.listConsumerGroupOffsets(groupId);
            Map<TopicPartition, OffsetAndMetadata> currentOffsets = offsetsResult.partitionsToOffsetAndMetadata().get();

            if (currentOffsets.isEmpty()) {
                log.warn("Consumer group [{}] has no committed offsets", groupId);
                return true;
            }

            // 2. 按 topic 分组处理
            Map<String, List<TopicPartition>> topicPartitions = new HashMap<>();
            for (TopicPartition tp : currentOffsets.keySet()) {
                topicPartitions.computeIfAbsent(tp.topic(), k -> new ArrayList<>()).add(tp);
            }

            // 3. 为每个 topic 重置偏移量
            Map<TopicPartition, OffsetAndMetadata> newOffsets = new HashMap<>();

            for (Map.Entry<String, List<TopicPartition>> entry : topicPartitions.entrySet()) {
                String topic = entry.getKey();
                List<TopicPartition> partitions = entry.getValue();

                switch (mode.toLowerCase()) {
                    case "earliest":
                    case "latest": {
                        Map<TopicPartition, OffsetSpec> request = new HashMap<>();
                        for (TopicPartition tp : partitions) {
                            request.put(tp, mode.equals("earliest") ? OffsetSpec.earliest() : OffsetSpec.latest());
                        }
                        ListOffsetsResult offsetsRes = adminClient.listOffsets(request);
                        for (TopicPartition tp : partitions) {
                            long offset = offsetsRes.partitionResult(tp).get().offset();
                            newOffsets.put(tp, new OffsetAndMetadata(offset));
                        }
                        break;
                    }

                    case "specific": {
                        if (value == null) {
                            log.error("Value is required for specific offset mode");
                            return false;
                        }
                        for (TopicPartition tp : partitions) {
                            newOffsets.put(tp, new OffsetAndMetadata(value));
                        }
                        break;
                    }

                    case "timestamp": {
                        if (value == null) {
                            log.error("Value is required for timestamp mode");
                            return false;
                        }
                        Map<TopicPartition, OffsetSpec> request = new HashMap<>();
                        for (TopicPartition tp : partitions) {
                            request.put(tp, OffsetSpec.forTimestamp(value));
                        }
                        ListOffsetsResult offsetsRes = adminClient.listOffsets(request);
                        for (TopicPartition tp : partitions) {
                            ListOffsetsResult.ListOffsetsResultInfo info = offsetsRes.partitionResult(tp).get();
                            if (info != null && info.offset() >= 0) {
                                newOffsets.put(tp, new OffsetAndMetadata(info.offset()));
                            } else {
                                // 如果指定时间戳没有找到对应的偏移量，使用最早的偏移量
                                Map<TopicPartition, OffsetSpec> fallbackRequest = new HashMap<>();
                                fallbackRequest.put(tp, OffsetSpec.earliest());
                                ListOffsetsResult fallbackResult = adminClient.listOffsets(fallbackRequest);
                                long fallbackOffset = fallbackResult.partitionResult(tp).get().offset();
                                newOffsets.put(tp, new OffsetAndMetadata(fallbackOffset));
                            }
                        }
                        break;
                    }

                    default:
                        log.error("Unsupported reset mode: {}", mode);
                        return false;
                }
            }

            // 4. 提交新的偏移量
            adminClient.alterConsumerGroupOffsets(groupId, newOffsets).all().get();

            log.info("Successfully reset consumer group [{}] offsets for all topics to mode [{}]",
                    groupId, mode);
            return true;

        } catch (Exception e) {
            log.error("Failed to reset consumer group [{}] offsets for all topics with mode [{}]: ",
                    groupId, mode, e);
            return false;
        } finally {
            if (adminClient != null) {
                plugin.registerResourceForClose(adminClient);
            }
        }
    }
}
