/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.web.quartz.shard.task.sub;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.common.TopicPartition;
import org.smartloli.kafka.eagle.common.protocol.OwnerInfo;
import org.smartloli.kafka.eagle.common.protocol.bscreen.BScreenConsumerInfo;
import org.smartloli.kafka.eagle.common.protocol.consumer.ConsumerGroupsInfo;
import org.smartloli.kafka.eagle.common.protocol.consumer.ConsumerSummaryInfo;
import org.smartloli.kafka.eagle.common.util.*;
import org.smartloli.kafka.eagle.common.util.KConstants.Topic;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerFactory;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerService;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.ConsumerServiceImpl;
import org.smartloli.kafka.eagle.web.service.impl.MetricsServiceImpl;

import java.util.*;
import java.util.Map.Entry;

/**
 * Collector kafka consumer topic lag trend, metrics broker topic.
 *
 * @author smartloli.
 * <p>
 * Created by Jul 26, 2022
 * <p>
 */
public class BScreenConsumerSubTask extends Thread {

    /**
     * Kafka service interface.
     */
    private KafkaService kafkaService = new KafkaFactory().create();

    /**
     * Broker service interface.
     */
    private static BrokerService brokerService = new BrokerFactory().create();


    @Override
    public synchronized void run() {
        this.metricsConsumerTopicQuartz();
    }

    private void metricsConsumerTopicQuartz() {
        try {
            bscreenConsumerTopicStats();
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Collector consumer topic data has error, msg is ", e);
        }
    }

    private void bscreenConsumerTopicStats() {
        MetricsServiceImpl metricsServiceImpl = null;
        try {
            metricsServiceImpl = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Get metricsServiceImpl bean has error, msg is ", e);
            return;
        }

        ConsumerServiceImpl consumerServiceImpl = null;
        try {
            consumerServiceImpl = StartupListener.getBean("consumerServiceImpl", ConsumerServiceImpl.class);
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Get consumerServiceImpl bean has error, msg is ", e);
            return;
        }

        List<BScreenConsumerInfo> bscreenConsumers = new ArrayList<>();
        List<ConsumerSummaryInfo> consumerSummarys = new ArrayList<>();
        List<ConsumerGroupsInfo> consumerGroupTopics = new ArrayList<>();
        String[] clusterAliass = SystemConfigUtils.getPropertyArray("efak.zk.cluster.alias", ",");
        for (String clusterAlias : clusterAliass) {
            JSONArray consumerGroups = new JSONArray();
            if (SystemConfigUtils.getBooleanProperty("efak.distributed.enable")) {
                consumerGroups = JSON.parseArray(kafkaService.getDistributeKafkaConsumer(clusterAlias));
            } else {
                consumerGroups = JSON.parseArray(kafkaService.getKafkaConsumer(clusterAlias));
            }
            for (Object object : consumerGroups) {
                JSONObject consumerGroup = (JSONObject) object;
                String group = consumerGroup.getString("group");

                String ip = NetUtils.ip();
                if (!StrUtils.isNull(ip)) {
                    String host = consumerGroup.getString("host");
                    if (!ip.equals(host)) {
                        break;
                    }
                }

                // storage offline consumer summary
                OwnerInfo ownerInfo = kafkaService.getKafkaActiverNotOwners(clusterAlias, group);
                ConsumerSummaryInfo csi = new ConsumerSummaryInfo();
                csi.setCluster(clusterAlias);
                csi.setGroup(group);
                csi.setTopicNumbers(ownerInfo.getTopicSets().size());
                csi.setCoordinator(consumerGroup.getString("node"));
                csi.setActiveTopic(getKafkaActiveTopicNumbers(clusterAlias, group, consumerServiceImpl));
                csi.setActiveThread(ownerInfo.getActiveSize());
                consumerSummarys.add(csi);
                if (consumerSummarys.size() > Topic.BATCH_SIZE) {
                    try {
                        metricsServiceImpl.writeConsumerSummaryTopics(consumerSummarys);
                        consumerSummarys.clear();
                    } catch (Exception e) {
                        LoggerUtils.print(this.getClass()).error("Storage kafka topic consumer summary has error, msg is ", e);
                    }
                }

                for (String topic : kafkaService.getKafkaConsumerTopics(clusterAlias, group)) {
                    // storage offline consumer group
                    ConsumerGroupsInfo consumerGroupTopic = new ConsumerGroupsInfo();
                    consumerGroupTopic.setCluster(clusterAlias);
                    consumerGroupTopic.setGroup(group);
                    consumerGroupTopic.setTopic(topic);
                    consumerGroupTopic.setStatus(getKafkaConsumerTopicStatus(clusterAlias, group, topic, consumerServiceImpl));

                    consumerGroupTopics.add(consumerGroupTopic);
                    if (consumerGroupTopics.size() > Topic.BATCH_SIZE) {
                        try {
                            metricsServiceImpl.writeConsumerGroupTopics(consumerGroupTopics);
                            consumerGroupTopics.clear();
                        } catch (Exception e) {
                            LoggerUtils.print(this.getClass()).error("Storage kafka consumer group topic has error, msg is ", e);
                        }
                    }

                    // kafka eagle bscreen datasets
                    BScreenConsumerInfo bscreenConsumer = new BScreenConsumerInfo();
                    bscreenConsumer.setCluster(clusterAlias);
                    bscreenConsumer.setGroup(group);
                    bscreenConsumer.setTopic(topic);

                    List<String> partitions = kafkaService.findTopicPartition(clusterAlias, topic);
                    Set<Integer> partitionsInts = new HashSet<>();
                    for (String partition : partitions) {
                        try {
                            partitionsInts.add(Integer.parseInt(partition));
                        } catch (Exception e) {
                            LoggerUtils.print(this.getClass()).error("Parse partitionid string to integer has error, msg is ", e);
                        }
                    }

                    Map<Integer, Long> partitionOffset = kafkaService.getKafkaOffset(bscreenConsumer.getCluster(), bscreenConsumer.getGroup(), bscreenConsumer.getTopic(), partitionsInts);
                    Map<TopicPartition, Long> tps = kafkaService.getKafkaLogSize(bscreenConsumer.getCluster(), bscreenConsumer.getTopic(), partitionsInts);
                    long logsize = 0L;
                    long offsets = 0L;
                    if (tps != null && partitionOffset != null) {
                        for (Entry<TopicPartition, Long> entrySet : tps.entrySet()) {
                            try {
                                logsize += entrySet.getValue();
                                offsets += partitionOffset.get(entrySet.getKey().partition());
                            } catch (Exception e) {
                                LoggerUtils.print(this.getClass()).error("Get logsize and offsets has error, msg is ", e);
                            }
                        }
                    }

                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("cluster", clusterAlias);
                    params.put("group", group);
                    params.put("topic", topic);
                    BScreenConsumerInfo lastBScreenConsumerTopic = metricsServiceImpl.readBScreenLastTopic(params);
                    if (lastBScreenConsumerTopic == null || lastBScreenConsumerTopic.getLogsize() == 0) {
                        bscreenConsumer.setDifflogsize(0);
                    } else {
                        // maybe server timespan is not synchronized.
                        bscreenConsumer.setDifflogsize(Math.abs(logsize - lastBScreenConsumerTopic.getLogsize()));
                    }
                    if (lastBScreenConsumerTopic == null || lastBScreenConsumerTopic.getOffsets() == 0) {
                        bscreenConsumer.setDiffoffsets(0);
                    } else {
                        // maybe server timespan is not synchronized.
                        bscreenConsumer.setDiffoffsets(Math.abs(offsets - lastBScreenConsumerTopic.getOffsets()));
                    }
                    bscreenConsumer.setLogsize(logsize);
                    bscreenConsumer.setOffsets(offsets);
                    bscreenConsumer.setLag(Math.abs(logsize - offsets));
                    bscreenConsumer.setTimespan(CalendarUtils.getTimeSpan());
                    bscreenConsumer.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
                    bscreenConsumers.add(bscreenConsumer);
                    if (bscreenConsumers.size() > Topic.BATCH_SIZE) {
                        try {
                            metricsServiceImpl.writeBSreenConsumerTopic(bscreenConsumers);
                            bscreenConsumers.clear();
                        } catch (Exception e) {
                            LoggerUtils.print(this.getClass()).error("Storage bsreen kafka topic consumer has error, msg is ", e);
                        }
                    }
                }
            }
        }
        try {
            if (bscreenConsumers.size() > 0) {
                try {
                    metricsServiceImpl.writeBSreenConsumerTopic(bscreenConsumers);
                    bscreenConsumers.clear();
                } catch (Exception e) {
                    LoggerUtils.print(this.getClass()).error("Storage bscreen final topic consumer has error, msg is ", e);
                }
            }
            if (consumerSummarys.size() > 0) {
                try {
                    metricsServiceImpl.writeConsumerSummaryTopics(consumerSummarys);
                    consumerSummarys.clear();
                } catch (Exception e) {
                    LoggerUtils.print(this.getClass()).error("Storage kafka final topic consumer summary has error, msg is ", e);
                }
            }
            if (consumerGroupTopics.size() > 0) {
                try {
                    metricsServiceImpl.writeConsumerGroupTopics(consumerGroupTopics);
                    consumerGroupTopics.clear();
                } catch (Exception e) {
                    LoggerUtils.print(this.getClass()).error("Storage kafka final consumer group topic has error, msg is ", e);
                }
            }
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Collector consumer lag data has error, msg is ", e);
        }
    }

    private int getKafkaConsumerTopicStatus(String clusterAlias, String group, String topicSearch, ConsumerServiceImpl consumerServiceImpl) {
        Set<String> consumerTopics = kafkaService.getKafkaConsumerTopic(clusterAlias, group);
        Set<String> activerTopics = kafkaService.getKafkaActiverTopics(clusterAlias, group);
        for (String topic : consumerTopics) {
            if (consumerServiceImpl.isConsumering(clusterAlias, group, topic) == Topic.RUNNING) {
                activerTopics.add(topic);
            }
        }

        if (activerTopics.contains(topicSearch)) {
            return Topic.RUNNING;
        } else {
            return consumerServiceImpl.isConsumering(clusterAlias, group, topicSearch);
        }

    }

    private int getKafkaActiveTopicNumbers(String clusterAlias, String group, ConsumerServiceImpl consumerServiceImpl) {
        Set<String> consumerTopics = kafkaService.getKafkaConsumerTopic(clusterAlias, group);
        Set<String> activerTopics = kafkaService.getKafkaActiverTopics(clusterAlias, group);
        for (String topic : consumerTopics) {
            if (consumerServiceImpl.isConsumering(clusterAlias, group, topic) == Topic.RUNNING) {
                activerTopics.add(topic);
            }
        }
        int active = 0;
        for (String topic : consumerTopics) {
            if (activerTopics.contains(topic)) {
                active++;
            } else {
                if (consumerServiceImpl.isConsumering(clusterAlias, group, topic) == Topic.RUNNING) {
                    active++;
                }
            }
        }
        return active;
    }

    private int getActiveNumber(String clusterAlias, String group, List<String> topics) {
        Map<String, List<String>> activeTopics = kafkaService.getActiveTopic(clusterAlias);
        int sum = 0;
        for (String topic : topics) {
            if (activeTopics.containsKey(group + "_" + topic)) {
                sum++;
            }
        }
        return sum;
    }

    private void cleanUnExistKafkaConsumerGroup(String cluster, List<ConsumerGroupsInfo> allConsumerGroups, JSONArray consumerGroups, MetricsServiceImpl metricsServiceImpl) {
        if (allConsumerGroups != null && consumerGroups != null) {
            Map<String, Set<String>> allConsumerGroupMap = new HashMap<>();
            for (ConsumerGroupsInfo allConsumerGroup : allConsumerGroups) {
                if (allConsumerGroupMap.containsKey(allConsumerGroup.getGroup())) {
                    allConsumerGroupMap.get(allConsumerGroup.getGroup()).add(allConsumerGroup.getTopic());
                } else {
                    Set<String> topics = new HashSet<>();
                    topics.add(allConsumerGroup.getTopic());
                    allConsumerGroupMap.put(allConsumerGroup.getGroup(), topics);
                }
            }
            try {
                List<String> realGroups = new ArrayList<>();
                for (Object object : consumerGroups) {
                    JSONObject consumerGroup = (JSONObject) object;
                    realGroups.add(consumerGroup.getString("group"));
                }
                for (Entry<String, Set<String>> group : allConsumerGroupMap.entrySet()) {
                    if (realGroups.contains(group.getKey())) {
                        for (String topic : allConsumerGroupMap.get(group.getKey())) {
                            if (!kafkaService.getKafkaConsumerTopics(cluster, group.getKey()).contains(topic)) {
                                Map<String, Object> cleanParams = new HashMap<>();
                                cleanParams.put("cluster", cluster);
                                cleanParams.put("group", group.getKey());
                                cleanParams.put("topic", topic);
                                try {
                                    metricsServiceImpl.cleanConsumerGroupTopic(cleanParams);
                                } catch (Exception e) {
                                    LoggerUtils.print(this.getClass()).error("Clean kafka consumer cluster[" + cluster + "] group[" + group.getKey() + "] has error, msg is ", e);
                                }
                            }
                        }
                    } else {
                        Map<String, Object> cleanParams = new HashMap<>();
                        cleanParams.put("cluster", cluster);
                        cleanParams.put("group", group.getKey());
                        try {
                            metricsServiceImpl.cleanConsumerGroupTopic(cleanParams);
                        } catch (Exception e) {
                            LoggerUtils.print(this.getClass()).error("Clean kafka consumer cluster[" + cluster + "] group[" + group + "] has error, msg is ", e);
                        }
                    }
                }
            } catch (Exception e) {
                LoggerUtils.print(this.getClass()).error("Clean kafka consumer final has error, msg is ", e);
            }
        }
    }

    private void cleanUnExistKafkaConsumerSummary(String cluster, List<ConsumerSummaryInfo> allConsumerGroups, JSONArray consumerGroups, MetricsServiceImpl metricsServiceImpl) {
        if (allConsumerGroups != null && consumerGroups != null) {
            try {
                List<String> realGroups = new ArrayList<>();
                for (Object object : consumerGroups) {
                    JSONObject consumerGroup = (JSONObject) object;
                    realGroups.add(consumerGroup.getString("group"));
                }

                for (ConsumerSummaryInfo cgi : allConsumerGroups) {
                    if (!realGroups.contains(cgi.getGroup())) {
                        Map<String, Object> cleanParams = new HashMap<>();
                        cleanParams.put("cluster", cluster);
                        cleanParams.put("group", cgi.getGroup());
                        metricsServiceImpl.cleanConsumerSummaryTopic(cleanParams);
                    }
                }
            } catch (Exception e) {
                LoggerUtils.print(this.getClass()).error("Clean kafka consumer summary final has error, msg is ", e);
            }
        }
    }
}