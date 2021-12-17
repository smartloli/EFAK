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

import org.smartloli.kafka.eagle.common.protocol.topic.TopicRank;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.LoggerUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerFactory;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerService;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.DashboardServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collector topic logsize, capacity etc.
 *
 * @author smartloli.
 * <p>
 * Created by Dec 09, 2021
 */
public class LogsizeStatsSubTask extends Thread {

    /**
     * Broker service interface.
     */
    private static BrokerService brokerService = new BrokerFactory().create();

    @Override
    public synchronized void run() {

        try {
            this.topicLogsizeStats();
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Collector topic logsize has error, msg is ", e);
        }

    }

    private void topicLogsizeStats() {
        DashboardServiceImpl dashboardServiceImpl = null;
        try {
            dashboardServiceImpl = StartupListener.getBean("dashboardServiceImpl", DashboardServiceImpl.class);
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Get dashboardServiceImpl bean be used for topic rank logsize has error,msg is ", e);
        }

        List<TopicRank> topicRanks = new ArrayList<>();
        String[] clusterAliass = SystemConfigUtils.getPropertyArray("efak.zk.cluster.alias", ",");
        for (String clusterAlias : clusterAliass) {
            List<String> topics = brokerService.topicList(clusterAlias);

            // clean up nonexistent topic
            Map<String, Object> params = new HashMap<>();
            params.put("cluster", clusterAlias);
            params.put("tkey", KConstants.Topic.LOGSIZE);
            List<TopicRank> trs = dashboardServiceImpl.getAllTopicRank(params);
            for (TopicRank tr : trs) {
                try {
                    if (!topics.contains(tr.getTopic())) {
                        Map<String, Object> clean = new HashMap<>();
                        clean.put("cluster", clusterAlias);
                        clean.put("topic", tr.getTopic());
                        clean.put("tkey", KConstants.Topic.LOGSIZE);
                        dashboardServiceImpl.removeTopicRank(clean);
                    }
                } catch (Exception e) {
                    LoggerUtils.print(this.getClass()).error("Failed to clean up nonexistent topic, msg is ", e);
                }
            }

            for (String topic : topics) {
                long logsize = brokerService.getTopicRealLogSize(clusterAlias, topic);
                TopicRank topicRank = new TopicRank();
                topicRank.setCluster(clusterAlias);
                topicRank.setTopic(topic);
                topicRank.setTkey(KConstants.Topic.LOGSIZE);
                topicRank.setTvalue(logsize);
                topicRanks.add(topicRank);
                if (topicRanks.size() > KConstants.Topic.BATCH_SIZE) {
                    try {
                        dashboardServiceImpl.writeTopicRank(topicRanks);
                        topicRanks.clear();
                    } catch (Exception e) {
                        LoggerUtils.print(this.getClass()).error("Storage topic rank logsize has error, msg is ", e);
                    }
                }
            }
        }
        try {
            if (topicRanks.size() > 0) {
                dashboardServiceImpl.writeTopicRank(topicRanks);
                topicRanks.clear();
            }
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Storage topic rank logsize end data has error,msg is ", e);
        }
    }
}
