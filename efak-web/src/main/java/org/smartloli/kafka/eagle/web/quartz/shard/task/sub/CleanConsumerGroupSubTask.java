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
import org.smartloli.kafka.eagle.common.protocol.consumer.ConsumerGroupsInfo;
import org.smartloli.kafka.eagle.common.util.LoggerUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerFactory;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerService;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
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
public class CleanConsumerGroupSubTask extends Thread {

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

        String[] clusterAliass = SystemConfigUtils.getPropertyArray("efak.zk.cluster.alias", ",");
        for (String clusterAlias : clusterAliass) {

            // get all consumer summary from database
            Map<String, Object> paramsSummary = new HashMap<>();
            paramsSummary.put("cluster", clusterAlias);

            // get all consumer group from database
            Map<String, Object> paramsGroup = new HashMap<>();
            paramsGroup.put("cluster", clusterAlias);
            List<ConsumerGroupsInfo> allConsumerGroups = metricsServiceImpl.getAllConsumerGroups(paramsGroup);

            JSONArray consumerGroups = JSON.parseArray(kafkaService.getKafkaConsumer(clusterAlias));

            // clean offline consumer group
            cleanUnExistKafkaConsumerGroup(clusterAlias, allConsumerGroups, consumerGroups, metricsServiceImpl);
        }
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

}