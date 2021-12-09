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

import org.smartloli.kafka.eagle.common.protocol.topic.TopicLogSize;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicRank;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
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
 * Collect topic producer logsize dataset.
 *
 * @author smartloli.
 * <p>
 * Created by Dec 09, 2021
 */
public class ProducerLogSizeStatsSubTask extends Thread {

    /**
     * Broker service interface.
     */
    private static BrokerService brokerService = new BrokerFactory().create();

    @Override
    public void run() {
        try {
            this.topicProducerLogSizeStats();
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Collector topic logsize has error, msg is ", e);
        }
    }

    private void topicProducerLogSizeStats() {
        DashboardServiceImpl dashboardServiceImpl = null;
        try {
            dashboardServiceImpl = StartupListener.getBean("dashboardServiceImpl", DashboardServiceImpl.class);
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Get dashboardServiceImpl bean be used for topic producer logsize has error,msg is ", e);
        }

        List<TopicRank> topicRanks = new ArrayList<>();
        List<TopicLogSize> topicLogSizes = new ArrayList<>();
        String[] clusterAliass = SystemConfigUtils.getPropertyArray("efak.zk.cluster.alias", ",");
        for (String clusterAlias : clusterAliass) {
            long producerThreads = 0L;
            List<String> topics = brokerService.topicList(clusterAlias);
            for (String topic : topics) {
                long logsize = brokerService.getTopicProducerLogSize(clusterAlias, topic);
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("cluster", clusterAlias);
                params.put("topic", topic);
                TopicLogSize lastTopicLogSize = dashboardServiceImpl.readLastTopicLogSize(params);
                TopicLogSize topicLogSize = new TopicLogSize();
                if (lastTopicLogSize == null || lastTopicLogSize.getLogsize() == 0) {
                    topicLogSize.setDiffval(0);
                } else {
                    // maybe server timespan is not synchronized.
                    topicLogSize.setDiffval(Math.abs(logsize - lastTopicLogSize.getLogsize()));
                }
                // stats producer threads
                if (topicLogSize.getDiffval() > 0) {
                    producerThreads++;
                }
                topicLogSize.setCluster(clusterAlias);
                topicLogSize.setTopic(topic);
                topicLogSize.setLogsize(logsize);
                topicLogSize.setTimespan(CalendarUtils.getTimeSpan());
                topicLogSize.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
                topicLogSizes.add(topicLogSize);
                if (topicLogSizes.size() > KConstants.Topic.BATCH_SIZE) {
                    try {
                        dashboardServiceImpl.writeTopicLogSize(topicLogSizes);
                        topicLogSizes.clear();
                    } catch (Exception e) {
                        LoggerUtils.print(this.getClass()).error("Storage topic producer logsize has error, msg is ", e);
                    }
                }
            }
            // stats producers thread
            try {
                TopicRank topicRank = new TopicRank();
                topicRank.setCluster(clusterAlias);
                topicRank.setTopic(KConstants.Topic.PRODUCER_THREADS_KEY);
                topicRank.setTkey(KConstants.Topic.PRODUCER_THREADS);
                topicRank.setTvalue(producerThreads);
                topicRanks.add(topicRank);
                if (topicRanks.size() > KConstants.Topic.BATCH_SIZE) {
                    try {
                        dashboardServiceImpl.writeTopicRank(topicRanks);
                        topicRanks.clear();
                    } catch (Exception e) {
                        LoggerUtils.print(this.getClass()).error("Storage topic producers has error, msg is ", e);
                    }
                }
            } catch (Exception e) {
                LoggerUtils.print(this.getClass()).error("Stats producers thread has error, msg is ", e);
            }
        }
        try {
            if (topicLogSizes.size() > 0) {
                dashboardServiceImpl.writeTopicLogSize(topicLogSizes);
                topicLogSizes.clear();
            }
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Storage topic producer logsize end data has error, msg is ", e);
        }
        try {
            if (topicRanks.size() > 0) {
                dashboardServiceImpl.writeTopicRank(topicRanks);
                topicRanks.clear();
            }
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Storage topic producer threads end data has error,msg is ", e);
        }
    }
}
