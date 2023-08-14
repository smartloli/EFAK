/**
 * KafkaSchemaFactory.java
 * <p>
 * Copyright 2023 smartloli
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
package org.kafka.eagle.core.kafka;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
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
import org.apache.kafka.common.*;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.TopicConfig;
import org.kafka.eagle.common.constants.JmxConstants;
import org.kafka.eagle.common.constants.KConstants;
import org.kafka.eagle.common.utils.CalendarUtil;
import org.kafka.eagle.common.utils.StrUtils;
import org.kafka.eagle.pojo.cluster.BrokerInfo;
import org.kafka.eagle.pojo.cluster.KafkaClientInfo;
import org.kafka.eagle.pojo.consumer.*;
import org.kafka.eagle.pojo.kafka.JMXInitializeInfo;
import org.kafka.eagle.pojo.topic.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Get the Kafka topic metadata information through the broker address.
 *
 * @Author: smartloli
 * @Date: 2023/6/18 19:56
 * @Version: 3.4.0
 */
@Slf4j
public class KafkaSchemaFactory {
    private final KafkaStoragePlugin plugin;
    // private Set<String> tableNames;

    public KafkaSchemaFactory(final KafkaStoragePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean createTopicName(KafkaClientInfo kafkaClientInfo, NewTopicInfo newTopicInfo) {
        boolean status = false;
        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(plugin.getKafkaAdminClientProps(kafkaClientInfo));
            NewTopic newTopic = new NewTopic(newTopicInfo.getTopicName(), newTopicInfo.getPartitions(), newTopicInfo.getReplication());
            newTopic.configs(Collections.singletonMap(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(newTopicInfo.getRetainMs())));
            adminClient.createTopics(Collections.singleton(newTopic)).all().get();
            status = true;
        } catch (Exception e) {
            status = false;
            log.error("Create kafka topic has error, new topic [{}], msg is {}", newTopicInfo, e);
        } finally {
            adminClient.close();
        }

        return status;
    }

    public boolean addTopicPartitions(KafkaClientInfo kafkaClientInfo, NewTopicInfo newTopicInfo) {
        boolean status = false;
        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(plugin.getKafkaAdminClientProps(kafkaClientInfo));
            Map<String, NewPartitions> newPartitions = new HashMap<String, NewPartitions>();
            newPartitions.put(newTopicInfo.getTopicName(), NewPartitions.increaseTo(newTopicInfo.getPartitions()));
            adminClient.createPartitions(newPartitions);
            status = true;
        } catch (Exception e) {
            status = false;
            log.error("Add kafka topic partition has error, new topic [{}], msg is {}", newTopicInfo, e);
        } finally {
            adminClient.close();
        }

        return status;
    }

    public boolean deleteTopic(KafkaClientInfo kafkaClientInfo, NewTopicInfo newTopicInfo) {
        boolean status = false;
        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(plugin.getKafkaAdminClientProps(kafkaClientInfo));
            // create delete object
            NewTopic topic = new NewTopic(newTopicInfo.getTopicName(), Collections.emptyMap());
            // delete  topic
            DeleteTopicsResult deleteResult = adminClient.deleteTopics(Collections.singleton(topic.name()), new DeleteTopicsOptions());
            KafkaFuture<Void> future = deleteResult.values().get(topic.name());
            // wait delete finished
            future.get();
            status = true;
        } catch (Exception e) {
            status = false;
            log.error("Delete kafka topic has error, new topic [{}], msg is {}", newTopicInfo, e);
        } finally {
            adminClient.close();
        }

        return status;
    }


    public Set<String> getTopicNames(KafkaClientInfo kafkaClientInfo) {
        Set<String> topicNames = new HashSet<>();

        KafkaConsumer<?, ?> kafkaConsumer = null;
        try {
            kafkaConsumer = new KafkaConsumer<>(plugin.getKafkaConsumerProps(kafkaClientInfo));
            topicNames = kafkaConsumer.listTopics().keySet();
        } catch (Exception e) {
            log.error("Failure while loading kafka client for database '{}': {}", kafkaClientInfo, e);
        } finally {
            plugin.registerToClose(kafkaConsumer);
        }

        if (topicNames != null && topicNames.contains(KConstants.Topic.CONSUMER_OFFSET_TOPIC)) {
            topicNames.remove(KConstants.Topic.CONSUMER_OFFSET_TOPIC);
        }
        return topicNames;
    }

    public List<String> getTopicPartitionsOfString(KafkaClientInfo kafkaClientInfo, String topic) {
        List<String> partitions = new ArrayList<>();
        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(plugin.getKafkaAdminClientProps(kafkaClientInfo));
            DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Arrays.asList(topic));
            for (TopicPartitionInfo tp : describeTopicsResult.all().get().get(topic).partitions()) {
                partitions.add(String.valueOf(tp.partition()));
            }
        } catch (Exception e) {
            log.error("Failure while loading topic '{}' meta for kafka '{}': {}", kafkaClientInfo, topic, e);
        } finally {
            plugin.registerToClose(adminClient);
        }
        return partitions;
    }

    public Set<Integer> getTopicPartitionsOfInt(KafkaClientInfo kafkaClientInfo, String topic) {
        Set<Integer> partitions = new HashSet<>();
        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(plugin.getKafkaAdminClientProps(kafkaClientInfo));
            DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Arrays.asList(topic));
            for (TopicPartitionInfo tp : describeTopicsResult.all().get().get(topic).partitions()) {
                partitions.add(tp.partition());
            }
        } catch (Exception e) {
            log.error("Failure while loading topic '{}' meta for kafka '{}': {}", kafkaClientInfo, topic, e);
        } finally {
            plugin.registerToClose(adminClient);
        }
        return partitions;
    }

    /**
     * Get topic partitions size.
     *
     * @param kafkaClientInfo
     * @param topic
     * @return
     */
    public Integer getTopicPartitionsOfSize(KafkaClientInfo kafkaClientInfo, String topic) {
        Integer partitions = 0;
        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(plugin.getKafkaAdminClientProps(kafkaClientInfo));
            DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Collections.singleton(topic));
            partitions = describeTopicsResult.all().get().get(topic).partitions().size();
        } catch (Exception e) {
            log.error("Failure while loading topic '{}' meta for kafka '{}': {}", kafkaClientInfo, topic, e);
        } finally {
            plugin.registerToClose(adminClient);
        }
        return partitions;
    }

    public Long getTopicPartitionOfLogSize(KafkaClientInfo kafkaClientInfo, String topic, Integer partitionId) {
        Long logSize = 0L;

        // get topic partition logsize
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(plugin.getKafkaConsumerProps(kafkaClientInfo));
        TopicPartition tp = new TopicPartition(topic, partitionId);
        consumer.assign(Collections.singleton(tp));
        java.util.Map<TopicPartition, Long> endLogSize = consumer.endOffsets(Collections.singleton(tp));
        java.util.Map<TopicPartition, Long> startLogSize = consumer.beginningOffsets(Collections.singleton(tp));
        try {
            logSize = endLogSize.get(tp).longValue() - startLogSize.get(tp).longValue();
        } catch (Exception e) {
            log.error("Get topic:{}, partition:{}, logsize has error, msg is {}", topic, partitionId, e);
        } finally {
            if (consumer != null) {
                consumer.close();
            }
        }

        return logSize;

    }

    public Long getTopicOfLogSize(KafkaClientInfo kafkaClientInfo, String topic) {
        Long logSize = 0L;

        // get topic logsize
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(plugin.getKafkaConsumerProps(kafkaClientInfo));

        Set<Integer> partitionIds = getTopicPartitionsOfInt(kafkaClientInfo, topic);
        Set<TopicPartition> tps = new HashSet<>();
        for (int partitionid : partitionIds) {
            TopicPartition tp = new TopicPartition(topic, partitionid);
            tps.add(tp);
        }
        consumer.assign(tps);
        java.util.Map<TopicPartition, Long> endLogSize = consumer.endOffsets(tps);
        java.util.Map<TopicPartition, Long> startLogSize = consumer.beginningOffsets(tps);

        try {
            long endSumLogSize = 0L;
            long startSumLogSize = 0L;
            for (Map.Entry<TopicPartition, Long> entry : endLogSize.entrySet()) {
                endSumLogSize += entry.getValue();
            }
            for (Map.Entry<TopicPartition, Long> entry : startLogSize.entrySet()) {
                startSumLogSize += entry.getValue();
            }
            logSize = endSumLogSize - startSumLogSize;
        } catch (Exception e) {
            log.error("Get topic[{}] logsize diff val has error, msg is {}", topic, e);
        } finally {
            if (consumer != null) {
                consumer.close();
            }
        }
        return logSize;
    }

    /**
     * Get all topic logsize.
     *
     * @param kafkaClientInfo
     * @return
     */
    public Map<String, Long> getAllTopicOfLogSize(KafkaClientInfo kafkaClientInfo) {
        // get topic logsize
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(plugin.getKafkaConsumerProps(kafkaClientInfo));

        Set<String> topics = getTopicNames(kafkaClientInfo);
        Set<TopicPartition> tps = new HashSet<>();
        for (String topic : topics) {
            Set<Integer> partitionIds = getTopicPartitionsOfInt(kafkaClientInfo, topic);
            for (int partitionid : partitionIds) {
                TopicPartition tp = new TopicPartition(topic, partitionid);
                tps.add(tp);
            }
        }

        consumer.assign(tps);
        java.util.Map<TopicPartition, Long> endLogSize = consumer.endOffsets(tps);
        java.util.Map<TopicPartition, Long> startLogSize = consumer.beginningOffsets(tps);

        Map<String, Long> topicLogSizeMap = new HashMap<>();

        try {
            Map<String, Long> topicEndSumLogSize = new HashMap<>();
            for (Map.Entry<TopicPartition, Long> entry : endLogSize.entrySet()) {
                String topic = entry.getKey().topic();
                if (topicEndSumLogSize.containsKey(topic)) {
                    topicEndSumLogSize.put(topic, topicEndSumLogSize.get(topic) + entry.getValue());
                } else {
                    topicEndSumLogSize.put(topic, entry.getValue());
                }
            }

            Map<String, Long> topicStartSumLogSize = new HashMap<>();
            for (Map.Entry<TopicPartition, Long> entry : startLogSize.entrySet()) {
                String topic = entry.getKey().topic();
                if (topicStartSumLogSize.containsKey(topic)) {
                    topicStartSumLogSize.put(topic, topicStartSumLogSize.get(topic) + entry.getValue());
                } else {
                    topicStartSumLogSize.put(topic, entry.getValue());
                }
            }

            for (Map.Entry<String, Long> entry : topicEndSumLogSize.entrySet()) {
                Long endSumLogSize = entry.getValue();
                Long logsizeDiffVal = endSumLogSize - topicStartSumLogSize.get(entry.getKey());
                topicLogSizeMap.put(entry.getKey(), logsizeDiffVal);
            }
        } catch (Exception e) {
            log.error("Get all topic logsize diff val has error, msg is {}", e);
        } finally {
            if (consumer != null) {
                consumer.close();
            }
        }
        return topicLogSizeMap;
    }

    public Long getTopicOfTotalLogSize(KafkaClientInfo kafkaClientInfo, String topic) {
        Long logSize = 0L;

        // get topic logsize
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(plugin.getKafkaConsumerProps(kafkaClientInfo));

        Set<Integer> partitionIds = getTopicPartitionsOfInt(kafkaClientInfo, topic);
        Set<TopicPartition> tps = new HashSet<>();
        for (int partitionid : partitionIds) {
            TopicPartition tp = new TopicPartition(topic, partitionid);
            tps.add(tp);
        }
        consumer.assign(tps);
        java.util.Map<TopicPartition, Long> endLogSize = consumer.endOffsets(tps);

        try {
            long endSumLogSize = 0L;
            for (Map.Entry<TopicPartition, Long> entry : endLogSize.entrySet()) {
                endSumLogSize += entry.getValue();
            }
            logSize = endSumLogSize;
        } catch (Exception e) {
            log.error("Get topic[{}] real logsize has error, msg is {}", topic, e);
        } finally {
            if (consumer != null) {
                consumer.close();
            }
        }
        return logSize;
    }

    public Map<TopicPartition, Long> getTopicOfEndLogSize(KafkaClientInfo kafkaClientInfo, String topic) {

        Map<TopicPartition, Long> endLogSize = new HashMap<>();
        // get topic logsize
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(plugin.getKafkaConsumerProps(kafkaClientInfo));

        Set<Integer> partitionIds = getTopicPartitionsOfInt(kafkaClientInfo, topic);
        Set<TopicPartition> tps = new HashSet<>();
        for (int partitionid : partitionIds) {
            TopicPartition tp = new TopicPartition(topic, partitionid);
            tps.add(tp);
        }
        consumer.assign(tps);

        try {
            endLogSize = consumer.endOffsets(tps);
        } catch (Exception e) {
            log.error("Get topic[{}] real end object logsize has error, msg is {}", topic, e);
        } finally {
            if (consumer != null) {
                consumer.close();
            }
        }
        return endLogSize;
    }

    public TopicRecordPageInfo getTopicMetaPageOfRecord(KafkaClientInfo kafkaClientInfo, String topic, Map<String, Object> params) {
        TopicRecordPageInfo topicRecordPageInfo = new TopicRecordPageInfo();
        Integer partitions = 0;
        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(plugin.getKafkaAdminClientProps(kafkaClientInfo));
            DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Collections.singleton(topic));
            List<TopicPartitionInfo> tps = describeTopicsResult.all().get().get(topic).partitions();
            partitions = tps.size();

            Set<Integer> partitionSet = new TreeSet<>();
            for (TopicPartitionInfo tp : tps) {
                partitionSet.add(tp.partition());
            }

            Set<Integer> partitionSortSet = new TreeSet<>(new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    int diff = o1 - o2;// asc
                    if (diff > 0) {
                        return 1;
                    } else if (diff < 0) {
                        return -1;
                    }
                    return 0;
                }
            });
            partitionSortSet.addAll(partitionSet);

            // get topic meta page default 10 records
            int start = Integer.parseInt(params.get("start").toString());
            int length = Integer.parseInt(params.get("length").toString());
            int offset = 0;
            for (int partition : partitionSortSet) {
                if (offset >= start && offset < (start + length)) {
                    TopicRecordInfo topicRecordInfo = new TopicRecordInfo();
                    topicRecordInfo.setPartitionId(partition);
                    for (TopicPartitionInfo tp : tps) {
                        if (partition == tp.partition()) {
                            topicRecordInfo.setLeader(tp.leader().idString());
                            topicRecordInfo.setReplicas(tp.replicas().stream()
                                    .map(Node::idString)
                                    .collect(Collectors.toList()).toString());
                            topicRecordInfo.setIsr(tp.isr().stream()
                                    .map(Node::idString)
                                    .collect(Collectors.toList()).toString());
                            if (tp.isr().size() != tp.replicas().size()) {
                                // replicas lost
                                topicRecordInfo.setUnderReplicated(true);
                            } else {
                                // replicas normal
                                topicRecordInfo.setUnderReplicated(false);
                            }
                            if (tp.replicas() != null && tp.replicas().size() > 0 && tp.replicas().get(0).idString().equals(tp.leader().idString())) {
                                // partition preferred leader
                                topicRecordInfo.setPreferredLeader(true);
                            } else {
                                // partition occurs preferred leader exception
                                topicRecordInfo.setPreferredLeader(false);
                            }
                            break;
                        }
                    }

                    topicRecordInfo.setLogSize(getTopicPartitionOfLogSize(kafkaClientInfo, topic, partition));
                    topicRecordPageInfo.getRecords().add(topicRecordInfo);
                }
                offset++;
            }


        } catch (Exception e) {
            log.error("Failure while loading topic '{}' meta for kafka '{}': {}", kafkaClientInfo, topic, e);
        } finally {
            plugin.registerToClose(adminClient);
        }
        topicRecordPageInfo.setTotal(partitions);
        return topicRecordPageInfo;
    }

    public List<MetadataInfo> getTopicPartitionsLeader(KafkaClientInfo kafkaClientInfo, String topic) {
        List<MetadataInfo> partitions = new ArrayList<>();
        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(plugin.getKafkaAdminClientProps(kafkaClientInfo));
            DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Arrays.asList(topic));
            for (TopicPartitionInfo tp : describeTopicsResult.all().get().get(topic).partitions()) {
                MetadataInfo metadata = new MetadataInfo();
                metadata.setPartitionId(tp.partition());
                metadata.setLeader(tp.leader().id());
                partitions.add(metadata);
            }
        } catch (Exception e) {
            log.error("Failure while loading topic '{}' meta for kafka '{}': {}", kafkaClientInfo, topic, e);
        } finally {
            plugin.registerToClose(adminClient);
        }
        return partitions;
    }

    public List<MetadataInfo> getTopicMetaData(KafkaClientInfo kafkaClientInfo, String topic) {
        List<MetadataInfo> partitions = new ArrayList<>();
        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(plugin.getKafkaAdminClientProps(kafkaClientInfo));
            DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Arrays.asList(topic));
            for (TopicPartitionInfo tp : describeTopicsResult.all().get().get(topic).partitions()) {
                MetadataInfo metadata = new MetadataInfo();
                metadata.setPartitionId(tp.partition());
                metadata.setLeader(tp.leader().id());
                List<Integer> isr = new ArrayList<>();
                for (Node node : tp.isr()) {
                    isr.add(node.id());
                }
                metadata.setIsr(isr.toString());

                List<Integer> replicas = new ArrayList<>();
                for (Node node : tp.replicas()) {
                    replicas.add(node.id());
                }
                metadata.setReplicas(replicas.toString());
                partitions.add(metadata);
            }
        } catch (Exception e) {
            log.error("Failure while loading topic '{}' meta for kafka '{}': {}", kafkaClientInfo, topic, e);
        } finally {
            plugin.registerToClose(adminClient);
        }
        return partitions;
    }

    public TopicJmxInfo getTopicRecordCapacity(KafkaClientInfo kafkaClientInfo, List<BrokerInfo> brokerInfos, String topic) {
        TopicJmxInfo topicJmxInfo = new TopicJmxInfo();
        Long capacity = 0L;
        List<MetadataInfo> metadataInfos = getTopicMetaData(kafkaClientInfo, topic);
        for (MetadataInfo metadataInfo : metadataInfos) {
            JMXInitializeInfo initializeInfo = getBrokerJmxRmiOfLeaderId(brokerInfos, metadataInfo.getLeader());
            initializeInfo.setObjectName(String.format(JmxConstants.KafkaLog.SIZE.getValue(), topic, metadataInfo.getPartitionId()));
            capacity += KafkaClusterFetcher.getTopicRecordJmxInfo(initializeInfo);
        }
        Long size = StrUtils.stringifyByObject(capacity).getLong("size");
        String type = StrUtils.stringifyByObject(capacity).getString("type");
        topicJmxInfo.setCapacity(size);
        topicJmxInfo.setUnit(type);
        return topicJmxInfo;
    }

    public Long getTopicRecordCapacityNum(KafkaClientInfo kafkaClientInfo, List<BrokerInfo> brokerInfos, String topic) {
        Long capacity = 0L;
        List<MetadataInfo> metadataInfos = getTopicMetaData(kafkaClientInfo, topic);
        for (MetadataInfo metadataInfo : metadataInfos) {
            JMXInitializeInfo initializeInfo = getBrokerJmxRmiOfLeaderId(brokerInfos, metadataInfo.getLeader());
            initializeInfo.setObjectName(String.format(JmxConstants.KafkaLog.SIZE.getValue(), topic, metadataInfo.getPartitionId()));
            capacity += KafkaClusterFetcher.getTopicRecordJmxInfo(initializeInfo);
        }
        return capacity;
    }

    private JMXInitializeInfo getBrokerJmxRmiOfLeaderId(List<BrokerInfo> brokerInfos, Integer leadId) {
        JMXInitializeInfo initializeInfo = new JMXInitializeInfo();
        for (BrokerInfo brokerInfo : brokerInfos) {
            if (String.valueOf(leadId).equals(brokerInfo.getBrokerId())) {
                initializeInfo.setBrokerId(String.valueOf(leadId));
                initializeInfo.setHost(brokerInfo.getBrokerHost());
                initializeInfo.setPort(brokerInfo.getBrokerJmxPort());
                break;
            }
        }
        return initializeInfo;
    }

    public Map<String, TopicMetadataInfo> getTopicMetaData(KafkaClientInfo kafkaClientInfo, Set<String> topics) {
        Map<String, TopicMetadataInfo> topicMetas = new HashMap<>();

        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(plugin.getKafkaAdminClientProps(kafkaClientInfo));
            DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(topics);

            List<ConfigResource> configResources = new ArrayList<>();
            for (String topic : topics) {
                ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topic);
                configResources.add(resource);
            }
            DescribeConfigsResult describeConfigsResult = adminClient.describeConfigs(configResources);
            Map<ConfigResource, Config> topicConfigDescMap = describeConfigsResult.all().get();

            for (Map.Entry<String, TopicDescription> entry : describeTopicsResult.allTopicNames().get().entrySet()) {
                TopicMetadataInfo topicMetadataInfo = new TopicMetadataInfo();
                List<MetadataInfo> partitions = new ArrayList<>();
                for (TopicPartitionInfo tp : entry.getValue().partitions()) {
                    MetadataInfo metadata = new MetadataInfo();
                    metadata.setPartitionId(tp.partition());
                    metadata.setLeader(tp.leader().id());
                    List<Integer> isr = new ArrayList<>();
                    for (Node node : tp.isr()) {
                        isr.add(node.id());
                    }
                    metadata.setIsr(isr.toString());

                    List<Integer> replicas = new ArrayList<>();
                    for (Node node : tp.replicas()) {
                        replicas.add(node.id());
                    }
                    metadata.setReplicas(replicas.toString());
                    partitions.add(metadata);
                }

                ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, entry.getKey());
                Config topicConfig = topicConfigDescMap.get(resource);
                String retentionMs = topicConfig.get(TopicConfig.RETENTION_MS_CONFIG).value();

                topicMetadataInfo.setMetadataInfos(partitions);
                topicMetadataInfo.setRetainMs(retentionMs);

                topicMetas.put(entry.getKey(), topicMetadataInfo);
            }
        } catch (Exception e) {
            log.error("Failure while loading topics meta for kafka '{}': {}", kafkaClientInfo, e);
        } finally {
            plugin.registerToClose(adminClient);
        }
        return topicMetas;
    }

    public boolean sendMsg(KafkaClientInfo kafkaClientInfo, String topic, String msg) {
        boolean flag = false;
        Producer<String, String> producer = null;
        try {
            producer = new KafkaProducer<>(plugin.getKafkaProducerProps(kafkaClientInfo));
            producer.send(new ProducerRecord<>(topic, StrUtils.getUUid(), msg));
            flag = true;
        } catch (Exception e) {
            log.error("Failure while sending msg '{}' to topic '{}' for kafka '{}': {}", msg, topic, kafkaClientInfo, e);
        } finally {
            if (producer != null) {
                producer.close();
            }
        }
        return flag;
    }

    public String getMsg(KafkaClientInfo kafkaClientInfo, String topic, Integer partitionId) {
        JSONArray dataSets = new JSONArray();
        KafkaConsumer<String, String> consumer = null;
        try {
            consumer = new KafkaConsumer<>(plugin.getKafkaConsumerProps(kafkaClientInfo));
            TopicPartition topicPartition = new TopicPartition(topic, partitionId);
            consumer.assign(Collections.singleton(topicPartition));
            Map<TopicPartition, Long> offsets = consumer.endOffsets(Collections.singleton(topicPartition));
            long position = KConstants.Topic.PREVIEW_SIZE;
            if (offsets.get(topicPartition).longValue() > position) {
                consumer.seek(topicPartition, offsets.get(topicPartition).longValue() - position);
            } else {
                consumer.seek(topicPartition, 0);
            }

            boolean flag = true;
            while (flag) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(KConstants.Kafka.TIME_OUT));
                for (ConsumerRecord<String, String> record : records) {
                    JSONObject object = new JSONObject(new LinkedHashMap<>());
                    object.put(TopicSchema.PARTITION, record.partition());
                    object.put(TopicSchema.OFFSET, record.offset());
                    object.put(TopicSchema.MSG, record.value());
                    object.put(TopicSchema.TIMESPAN, record.timestamp());
                    object.put(TopicSchema.DATE, CalendarUtil.convertUnixTime(record.timestamp()));
                    dataSets.add(object);
                }
                if (records.isEmpty()) {
                    flag = false;
                }
            }

        } catch (Exception e) {
            log.error("Failure while get msg to topic '{}' for kafka '{}': {}", topic, kafkaClientInfo, e);
        } finally {
            if (consumer != null) {
                consumer.close();
            }
        }
        return dataSets.toJSONString();
    }

    public List<ConsumerGroupInfo> getConsumerGroups(KafkaClientInfo kafkaClientInfo) {
        AdminClient adminClient = null;
        List<ConsumerGroupInfo> consumerGroupInfos = new ArrayList<>();

        try {
            adminClient = AdminClient.create(plugin.getKafkaAdminClientProps(kafkaClientInfo));
            Iterator<ConsumerGroupListing> itors = adminClient.listConsumerGroups().all().get().iterator();
            Set<String> groupIdSets = new HashSet<>();
            while (itors.hasNext()) {
                String groupId = itors.next().groupId();
                if (!groupId.equals(KConstants.Kafka.EFAK_SYSTEM_GROUP)) {
                    groupIdSets.add(groupId);
                }
            }
            Map<String, ConsumerGroupDescription> descConsumerGroup = adminClient.describeConsumerGroups(groupIdSets).all().get();
            for (String groupId : groupIdSets) {
                if (descConsumerGroup.containsKey(groupId)) {// active
                    ConsumerGroupDescription consumerGroupDescription = descConsumerGroup.get(groupId);
                    ConsumerGroupInfo consumerGroupInfo = new ConsumerGroupInfo();
                    consumerGroupInfo.setClusterId(kafkaClientInfo.getClusterId());
                    consumerGroupInfo.setGroupId(groupId);
                    consumerGroupInfo.setState(consumerGroupDescription.state().name());
                    Node node = consumerGroupDescription.coordinator();
                    consumerGroupInfo.setCoordinator(node.host() + ":" + node.port());
                    Collection<MemberDescription> members = consumerGroupDescription.members();
                    if (members != null && members.size() > 0) {
                        members.iterator().forEachRemaining(member -> {
                            consumerGroupInfo.setOwner(member.host().replaceAll("/", "") + "-" + member.consumerId());
                            consumerGroupInfo.setStatus(StrUtil.isNotBlank(member.consumerId()) == true ? KConstants.Topic.RUNNING : KConstants.Topic.SHUTDOWN);
                            Set<TopicPartition> tps = member.assignment().topicPartitions();
                            if (tps != null && tps.size() > 0) {
                                Set<String> topics = tps.stream().map(TopicPartition::topic).collect(Collectors.toSet());
                                topics.forEach(topic -> {
                                    ConsumerGroupInfo consumerGroupInfoSub = null;
                                    try {
                                        consumerGroupInfoSub = (ConsumerGroupInfo) consumerGroupInfo.clone();
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
                    ConsumerGroupInfo consumerGroupInfo = new ConsumerGroupInfo();
                    consumerGroupInfo.setClusterId(kafkaClientInfo.getClusterId());
                    consumerGroupInfo.setGroupId(groupId);
                    consumerGroupInfo.setState(ConsumerGroupState.DEAD.name());
                    consumerGroupInfos.add(consumerGroupInfo);
                }
            }
        } catch (Exception e) {
            log.error("Failure while loading kafka client for database '{}': {}", kafkaClientInfo, e);
        } finally {
            plugin.registerToClose(adminClient);
        }

        return consumerGroupInfos;
    }

    public List<ConsumerGroupInfo> getConsumerGroups(KafkaClientInfo kafkaClientInfo, String groupId) {
        AdminClient adminClient = null;
        List<ConsumerGroupInfo> consumerGroupInfos = new ArrayList<>();

        try {
            adminClient = AdminClient.create(plugin.getKafkaAdminClientProps(kafkaClientInfo));
            Iterator<ConsumerGroupListing> itors = adminClient.listConsumerGroups().all().get().iterator();

            Map<String, ConsumerGroupDescription> descConsumerGroup = adminClient.describeConsumerGroups(Collections.singleton(groupId)).all().get();
            if (descConsumerGroup.containsKey(groupId)) {// active
                ConsumerGroupDescription consumerGroupDescription = descConsumerGroup.get(groupId);
                ConsumerGroupInfo consumerGroupInfo = new ConsumerGroupInfo();
                consumerGroupInfo.setClusterId(kafkaClientInfo.getClusterId());
                consumerGroupInfo.setGroupId(groupId);
                consumerGroupInfo.setState(consumerGroupDescription.state().name());
                Node node = consumerGroupDescription.coordinator();
                consumerGroupInfo.setCoordinator(node.host() + ":" + node.port());
                Collection<MemberDescription> members = consumerGroupDescription.members();
                if (members != null && members.size() > 0) {
                    members.iterator().forEachRemaining(member -> {
                        consumerGroupInfo.setOwner(member.host().replaceAll("/", "") + "-" + member.consumerId());
                        consumerGroupInfo.setStatus(StrUtil.isNotBlank(member.consumerId()) == true ? KConstants.Topic.RUNNING : KConstants.Topic.SHUTDOWN);
                        Set<TopicPartition> tps = member.assignment().topicPartitions();
                        if (tps != null && tps.size() > 0) {
                            Set<String> topics = tps.stream().map(TopicPartition::topic).collect(Collectors.toSet());
                            topics.forEach(topic -> {
                                ConsumerGroupInfo consumerGroupInfoSub = null;
                                try {
                                    consumerGroupInfoSub = (ConsumerGroupInfo) consumerGroupInfo.clone();
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
                ConsumerGroupInfo consumerGroupInfo = new ConsumerGroupInfo();
                consumerGroupInfo.setClusterId(kafkaClientInfo.getClusterId());
                consumerGroupInfo.setGroupId(groupId);
                consumerGroupInfo.setState(ConsumerGroupState.DEAD.name());
                consumerGroupInfos.add(consumerGroupInfo);
            }
        } catch (Exception e) {
            log.error("Failure while loading kafka client for database '{}': {}", kafkaClientInfo, e);
        } finally {
            plugin.registerToClose(adminClient);
        }

        return consumerGroupInfos;
    }

    public ConsumerGroupDescInfo getKafkaConsumerGroups(KafkaClientInfo kafkaClientInfo) {
        AdminClient adminClient = null;
        ConsumerGroupDescInfo consumerGroupDescInfo = new ConsumerGroupDescInfo();
        try {
            adminClient = AdminClient.create(plugin.getKafkaAdminClientProps(kafkaClientInfo));
            Iterator<ConsumerGroupListing> itors = adminClient.listConsumerGroups().all().get().iterator();
            while (itors.hasNext()) {
                String groupId = itors.next().groupId();
                if (!groupId.equals(KConstants.Kafka.EFAK_SYSTEM_GROUP)) {
                    consumerGroupDescInfo.getGroupIds().add(groupId);
                }
            }
            Map<String, ConsumerGroupDescription> descConsumerGroup = adminClient.describeConsumerGroups(consumerGroupDescInfo.getGroupIds()).all().get();
            consumerGroupDescInfo.getDescConsumerGroup().putAll(descConsumerGroup);
        } catch (Exception e) {
            log.error("Failure while get consumer group object for database '{}': {}", kafkaClientInfo, e);
        } finally {
            plugin.registerToClose(adminClient);
        }

        return consumerGroupDescInfo;
    }

    /**
     * Get consumer group topic logsize, offset.
     *
     * @param kafkaClientInfo
     * @param groupIds
     * @return
     */
    public List<ConsumerGroupTopicInfo> getKafkaConsumerGroupTopic(KafkaClientInfo kafkaClientInfo, Set<String> groupIds) {
        List<ConsumerGroupTopicInfo> consumerGroupTopicInfos = new ArrayList<>();
        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(plugin.getKafkaAdminClientProps(kafkaClientInfo));

            for (String groupId : groupIds) {
                Map<String, Long> topicOffsetsMap = new HashMap<>();
                ListConsumerGroupOffsetsResult offsetsResult = adminClient.listConsumerGroupOffsets(groupId);
                Map<TopicPartition, OffsetAndMetadata> offsets = offsetsResult.partitionsToOffsetAndMetadata().get();
                if (offsets != null && offsets.size() > 0) {
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
                    Long logsizeValue = getTopicOfTotalLogSize(kafkaClientInfo, entry.getKey());
                    ;
                    Long offsetValue = entry.getValue();
                    consumerGroupTopicInfo.setOffsets(offsetValue);
                    consumerGroupTopicInfo.setLogsize(logsizeValue);
                    // maybe server timespan is not synchronized.
                    consumerGroupTopicInfo.setLags(Math.abs(logsizeValue - offsetValue));
                    // add to list
                    consumerGroupTopicInfos.add(consumerGroupTopicInfo);
                }
            }
        } catch (Exception e) {
            log.error("Failure while get consumer group topic offset has error, database {}: {} ", kafkaClientInfo, e);
        } finally {
            plugin.registerToClose(adminClient);
        }
        return consumerGroupTopicInfos;
    }

    public ConsumerOffsetPageInfo getConsumerOffsetPageOfRecord(KafkaClientInfo kafkaClientInfo, String groupId, String topicName, Map<String, Object> params) {
        ConsumerOffsetPageInfo consumerOffsetPageInfo = new ConsumerOffsetPageInfo();
        Integer partitions = 0;
        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(plugin.getKafkaAdminClientProps(kafkaClientInfo));

            DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Collections.singleton(topicName));
            List<TopicPartitionInfo> tps = describeTopicsResult.all().get().get(topicName).partitions();
            partitions = tps.size();

            List<TopicPartition> tpList = new ArrayList<>();

            Set<Integer> partitionSet = new TreeSet<>();
            for (TopicPartitionInfo tp : tps) {
                partitionSet.add(tp.partition());
                TopicPartition topicPartition = new TopicPartition(topicName, tp.partition());
                tpList.add(topicPartition);
            }

            ListConsumerGroupOffsetsOptions consumerOffsetOptions = new ListConsumerGroupOffsetsOptions();
            consumerOffsetOptions.topicPartitions(tpList);

            // get offsets
            ListConsumerGroupOffsetsResult offsets = adminClient.listConsumerGroupOffsets(groupId, consumerOffsetOptions);
            // get end logsize
            Map<TopicPartition, Long> endLogsizes = getTopicOfEndLogSize(kafkaClientInfo, topicName);
            Set<Integer> partitionSortSet = new TreeSet<>(new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    int diff = o1 - o2;// asc
                    if (diff > 0) {
                        return 1;
                    } else if (diff < 0) {
                        return -1;
                    }
                    return 0;
                }
            });
            partitionSortSet.addAll(partitionSet);

            // get topic meta page default 10 records
            int start = Integer.parseInt(params.get("start").toString());
            int length = Integer.parseInt(params.get("length").toString());
            int offset = 0;
            for (int partition : partitionSortSet) {
                if (offset >= start && offset < (start + length)) {
                    ConsumerOffsetInfo consumerOffsetInfo = new ConsumerOffsetInfo();
                    consumerOffsetInfo.setGroupId(groupId);
                    consumerOffsetInfo.setTopicName(topicName);
                    consumerOffsetInfo.setPartitionId(partition);
                    if (offsets.partitionsToOffsetAndMetadata().get().containsKey(new TopicPartition(topicName, partition))) {
                        consumerOffsetInfo.setOffsets(offsets.partitionsToOffsetAndMetadata().get().get(new TopicPartition(topicName, partition)).offset());
                    }
                    if (endLogsizes.containsKey(new TopicPartition(topicName, partition))) {
                        consumerOffsetInfo.setLogsize(endLogsizes.get(new TopicPartition(topicName, partition)));
                    }
                    consumerOffsetInfo.setLags(Math.abs(consumerOffsetInfo.getLogsize() - consumerOffsetInfo.getOffsets()));
                    consumerOffsetPageInfo.getRecords().add(consumerOffsetInfo);
                }
                offset++;
            }


        } catch (Exception e) {
            log.error("Failure while loading topic '{}' offset for kafka '{}': {}", kafkaClientInfo, topicName, e);
        } finally {
            plugin.registerToClose(adminClient);
        }
        consumerOffsetPageInfo.setTotal(partitions);
        return consumerOffsetPageInfo;
    }


    public static void main(String[] args) {
        KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());

        KafkaClientInfo kafkaClientInfo = new KafkaClientInfo();
        kafkaClientInfo.setBrokerServer("127.0.0.1:9092");

        Set<String> groupIds = new HashSet<>();
        groupIds.add("group_id_3");
        log.info("consumer group toipc:{}", JSON.toJSONString(ksf.getKafkaConsumerGroupTopic(kafkaClientInfo, groupIds)));
    }
}
