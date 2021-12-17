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

import org.apache.kafka.clients.admin.ConfigEntry;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicRank;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.KConstants.Topic;
import org.smartloli.kafka.eagle.common.util.LoggerUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerFactory;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerService;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsFactory;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsService;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.DashboardServiceImpl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clean topic unuse dataset.
 *
 * @author smartloli.
 * <p>
 * Created by Dec 09, 2021
 */
public class CleanTopicSubTask extends Thread {

    /**
     * Kafka service interface.
     */
    private KafkaMetricsService kafkaMetricsService = new KafkaMetricsFactory().create();

    /**
     * Broker service interface.
     */
    private static BrokerService brokerService = new BrokerFactory().create();

    @Override
    public synchronized void run() {
        try {
            this.topicCleanTask();
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Clean topic logsize has error, msg is ", e);
        }
    }

    private void topicCleanTask() {
        DashboardServiceImpl dashboardServiceImpl = null;
        try {
            dashboardServiceImpl = StartupListener.getBean("dashboardServiceImpl", DashboardServiceImpl.class);
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Get dashboardServiceImpl bean has error, msg is ", e);
        }
        String[] clusterAliass = SystemConfigUtils.getPropertyArray("efak.zk.cluster.alias", ",");
        for (String clusterAlias : clusterAliass) {
            Map<String, Object> params = new HashMap<>();
            params.put("cluster", clusterAlias);
            List<TopicRank> allCleanTopics = dashboardServiceImpl.getCleanTopicList(params);
            if (allCleanTopics != null) {
                for (TopicRank tr : allCleanTopics) {
                    long logSize = brokerService.getTopicRealLogSize(clusterAlias, tr.getTopic());
                    if (logSize > 0) {
                        String cleanUpPolicyLog = kafkaMetricsService.changeTopicConfig(clusterAlias, tr.getTopic(), Topic.ADD, new ConfigEntry(Topic.CLEANUP_POLICY_KEY, Topic.CLEANUP_POLICY_VALUE));
                        String retentionMsLog = kafkaMetricsService.changeTopicConfig(clusterAlias, tr.getTopic(), Topic.ADD, new ConfigEntry(Topic.RETENTION_MS_KEY, Topic.RETENTION_MS_VALUE));
                        LoggerUtils.print(this.getClass()).info("Add [" + Topic.CLEANUP_POLICY_KEY + "] topic[" + tr.getTopic() + "] property result," + cleanUpPolicyLog);
                        LoggerUtils.print(this.getClass()).info("Add [" + Topic.RETENTION_MS_KEY + "] topic[" + tr.getTopic() + "] property result," + retentionMsLog);
                    } else {
                        // delete znode
                        String cleanUpPolicyLog = kafkaMetricsService.changeTopicConfig(clusterAlias, tr.getTopic(), Topic.DELETE, new ConfigEntry(Topic.CLEANUP_POLICY_KEY, ""));
                        String retentionMsLog = kafkaMetricsService.changeTopicConfig(clusterAlias, tr.getTopic(), Topic.DELETE, new ConfigEntry(Topic.RETENTION_MS_KEY, ""));
                        LoggerUtils.print(this.getClass()).info("Delete [" + Topic.CLEANUP_POLICY_KEY + "] topic[" + tr.getTopic() + "] property result," + cleanUpPolicyLog);
                        LoggerUtils.print(this.getClass()).info("Delete [" + KConstants.Topic.RETENTION_MS_KEY + "] topic[" + tr.getTopic() + "] property result," + retentionMsLog);
                        // update db state
                        tr.setTvalue(1);
                        dashboardServiceImpl.writeTopicRank(Arrays.asList(tr));
                    }
                }
            }
        }
    }
}
