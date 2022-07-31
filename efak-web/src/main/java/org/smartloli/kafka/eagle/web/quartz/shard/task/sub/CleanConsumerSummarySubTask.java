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
import org.smartloli.kafka.eagle.common.protocol.consumer.ConsumerSummaryInfo;
import org.smartloli.kafka.eagle.common.util.LoggerUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerFactory;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerService;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.MetricsServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collector kafka consumer topic lag trend, metrics broker topic.
 *
 * @author smartloli.
 * <p>
 * Created by Jul 26, 2022
 * <p>
 */
public class CleanConsumerSummarySubTask extends Thread {

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
            List<ConsumerSummaryInfo> allConsumerSummary = metricsServiceImpl.getAllConsumerSummary(paramsSummary);

            // get all consumer group from database
            Map<String, Object> paramsGroup = new HashMap<>();
            paramsGroup.put("cluster", clusterAlias);

            JSONArray consumerGroups = JSON.parseArray(kafkaService.getKafkaConsumer(clusterAlias));

            // clean offline consumer summary
            cleanUnExistKafkaConsumerSummary(clusterAlias, allConsumerSummary, consumerGroups, metricsServiceImpl);
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