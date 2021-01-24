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
package org.smartloli.kafka.eagle.web.quartz;

import org.apache.kafka.clients.admin.ConfigEntry;
import org.smartloli.kafka.eagle.common.protocol.BrokersInfo;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicLogSize;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicRank;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.ErrorUtils;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.KConstants.Topic;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.Mx4jFactory;
import org.smartloli.kafka.eagle.core.factory.Mx4jService;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerFactory;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerService;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsFactory;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsService;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.DashboardServiceImpl;

import java.util.*;

/**
 * Collector topic logsize, capacity etc.
 *
 * @author smartloli.
 * <p>
 * Created by Jul 27, 2019
 */
public class TopicRankSubTask extends Thread {

    /**
     * Kafka service interface.
     */
    private KafkaMetricsService kafkaMetricsService = new KafkaMetricsFactory().create();

    /**
     * Broker service interface.
     */
    private static BrokerService brokerService = new BrokerFactory().create();

    /**
     * Kafka service interface.
     */
    private KafkaService kafkaService = new KafkaFactory().create();

    /**
     * Mx4j service interface.
     */
    private static Mx4jService mx4jService = new Mx4jFactory().create();

    @Override
    public void run() {
        this.topicRankQuartz();
    }

    private void topicRankQuartz() {
        // logsize thread
        new LogsizeStatsSubThread().start();

        // capacity thread
        new CapacityStatsSubThread().start();

        // producer logsize
        new ProducerLogSizeStatsSubThread().start();

        // performance topic
        new PerformanceByTopicStatsSubThread().start();

        // topic clean
        new CleanSubThread().start();

        // topic throughput
        new TopicThroughputThread().start();
    }

    class TopicThroughputThread extends Thread {
        @Override
        public void run() {
            try {
                this.throughput();
            } catch (Exception e) {
                ErrorUtils.print(this.getClass()).error("Stats topic throughput has error, msg is ", e);
            }
        }

        private void throughput() {
            throughputByteIn();
            throughputByteOut();
        }

        private void throughputByteIn() {
            DashboardServiceImpl dashboardServiceImpl = null;

            try {
                dashboardServiceImpl = StartupListener.getBean("dashboardServiceImpl", DashboardServiceImpl.class);
            } catch (Exception e) {
                ErrorUtils.print(this.getClass()).error("Get dashboardServiceImpl bean be used for topic rank byte in has error,msg is ", e);
            }

            List<TopicRank> topicRanks = new ArrayList<>();
            String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
            for (String clusterAlias : clusterAliass) {
                List<String> topics = brokerService.topicList(clusterAlias);
                List<BrokersInfo> brokers = kafkaService.getAllBrokersInfo(clusterAlias);

                // clean up nonexistent topic
                Map<String, Object> params = new HashMap<>();
                params.put("cluster", clusterAlias);
                params.put("tkey", Topic.BYTE_IN);
                List<TopicRank> trs = dashboardServiceImpl.getAllTopicRank(params);
                for (TopicRank tr : trs) {
                    try {
                        if (!topics.contains(tr.getTopic())) {
                            Map<String, Object> clean = new HashMap<>();
                            clean.put("cluster", clusterAlias);
                            clean.put("topic", tr.getTopic());
                            clean.put("tkey", Topic.BYTE_IN);
                            dashboardServiceImpl.removeTopicRank(clean);
                        }
                    } catch (Exception e) {
                        ErrorUtils.print(this.getClass()).error("Byte in failed to clean up nonexistent topic, msg is ", e);
                    }
                }

                for (String topic : topics) {
                    long byteIn = 0L;
                    for (BrokersInfo kafka : brokers) {
                        String meanRate = mx4jService.bytesInPerSec(clusterAlias, kafka.getHost() + ":" + kafka.getJmxPort(), topic).getMeanRate();
                        try {
                            byteIn += new Double(Double.parseDouble(meanRate)).longValue();
                        } catch (Exception e) {
                            ErrorUtils.print(this.getClass()).error("Byte in parse string to long has error, msg is ", e);
                        }
                    }
                    TopicRank topicRank = new TopicRank();
                    topicRank.setCluster(clusterAlias);
                    topicRank.setTopic(topic);
                    topicRank.setTkey(Topic.BYTE_IN);
                    topicRank.setTvalue(byteIn);
                    topicRanks.add(topicRank);
                    if (topicRanks.size() > Topic.BATCH_SIZE) {
                        try {
                            dashboardServiceImpl.writeTopicRank(topicRanks);
                            topicRanks.clear();
                        } catch (Exception e) {
                            ErrorUtils.print(this.getClass()).error("Storage topic rank byte in has error, msg is ", e);
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
                ErrorUtils.print(this.getClass()).error("Storage topic rank byte in end data has error,msg is ", e);
            }
        }

        private void throughputByteOut() {
            DashboardServiceImpl dashboardServiceImpl = null;

            try {
                dashboardServiceImpl = StartupListener.getBean("dashboardServiceImpl", DashboardServiceImpl.class);
            } catch (Exception e) {
                ErrorUtils.print(this.getClass()).error("Get dashboardServiceImpl bean be used for topic rank byte out has error,msg is ", e);
            }

            List<TopicRank> topicRanks = new ArrayList<>();
            String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
            for (String clusterAlias : clusterAliass) {
                List<String> topics = brokerService.topicList(clusterAlias);
                List<BrokersInfo> brokers = kafkaService.getAllBrokersInfo(clusterAlias);

                // clean up nonexistent topic
                Map<String, Object> params = new HashMap<>();
                params.put("cluster", clusterAlias);
                params.put("tkey", Topic.BYTE_OUT);
                List<TopicRank> trs = dashboardServiceImpl.getAllTopicRank(params);
                for (TopicRank tr : trs) {
                    try {
                        if (!topics.contains(tr.getTopic())) {
                            Map<String, Object> clean = new HashMap<>();
                            clean.put("cluster", clusterAlias);
                            clean.put("topic", tr.getTopic());
                            clean.put("tkey", Topic.BYTE_OUT);
                            dashboardServiceImpl.removeTopicRank(clean);
                        }
                    } catch (Exception e) {
                        ErrorUtils.print(this.getClass()).error("Byte out failed to clean up nonexistent topic, msg is ", e);
                    }
                }

                for (String topic : topics) {
                    long byteOut = 0L;
                    for (BrokersInfo kafka : brokers) {
                        String meanRate = mx4jService.bytesOutPerSec(clusterAlias, kafka.getHost() + ":" + kafka.getJmxPort(), topic).getMeanRate();
                        try {
                            byteOut += new Double(Double.parseDouble(meanRate)).longValue();
                        } catch (Exception e) {
                            ErrorUtils.print(this.getClass()).error("Byte out parse string to long has error, msg is ", e);
                        }
                    }
                    TopicRank topicRank = new TopicRank();
                    topicRank.setCluster(clusterAlias);
                    topicRank.setTopic(topic);
                    topicRank.setTkey(Topic.BYTE_OUT);
                    topicRank.setTvalue(byteOut);
                    topicRanks.add(topicRank);
                    if (topicRanks.size() > Topic.BATCH_SIZE) {
                        try {
                            dashboardServiceImpl.writeTopicRank(topicRanks);
                            topicRanks.clear();
                        } catch (Exception e) {
                            ErrorUtils.print(this.getClass()).error("Storage topic rank byte out has error, msg is ", e);
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
                ErrorUtils.print(this.getClass()).error("Storage topic rank byte out end data has error,msg is ", e);
            }
        }
    }

    class LogsizeStatsSubThread extends Thread {
        @Override
        public void run() {
            try {
                this.topicLogsizeStats();
            } catch (Exception e) {
                ErrorUtils.print(this.getClass()).error("Collector topic logsize has error, msg is ", e);
            }
        }

        private void topicLogsizeStats() {
            DashboardServiceImpl dashboardServiceImpl = null;
            try {
                dashboardServiceImpl = StartupListener.getBean("dashboardServiceImpl", DashboardServiceImpl.class);
            } catch (Exception e) {
                ErrorUtils.print(this.getClass()).error("Get dashboardServiceImpl bean be used for topic rank logsize has error,msg is ", e);
            }

            List<TopicRank> topicRanks = new ArrayList<>();
            String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
            for (String clusterAlias : clusterAliass) {
                List<String> topics = brokerService.topicList(clusterAlias);

                // clean up nonexistent topic
                Map<String, Object> params = new HashMap<>();
                params.put("cluster", clusterAlias);
                params.put("tkey", Topic.LOGSIZE);
                List<TopicRank> trs = dashboardServiceImpl.getAllTopicRank(params);
                for (TopicRank tr : trs) {
                    try {
                        if (!topics.contains(tr.getTopic())) {
                            Map<String, Object> clean = new HashMap<>();
                            clean.put("cluster", clusterAlias);
                            clean.put("topic", tr.getTopic());
                            clean.put("tkey", Topic.LOGSIZE);
                            dashboardServiceImpl.removeTopicRank(clean);
                        }
                    } catch (Exception e) {
                        ErrorUtils.print(this.getClass()).error("Failed to clean up nonexistent topic, msg is ", e);
                    }
                }

                for (String topic : topics) {
                    long logsize = brokerService.getTopicRealLogSize(clusterAlias, topic);
                    TopicRank topicRank = new TopicRank();
                    topicRank.setCluster(clusterAlias);
                    topicRank.setTopic(topic);
                    topicRank.setTkey(Topic.LOGSIZE);
                    topicRank.setTvalue(logsize);
                    topicRanks.add(topicRank);
                    if (topicRanks.size() > Topic.BATCH_SIZE) {
                        try {
                            dashboardServiceImpl.writeTopicRank(topicRanks);
                            topicRanks.clear();
                        } catch (Exception e) {
                            ErrorUtils.print(this.getClass()).error("Storage topic rank logsize has error, msg is ", e);
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
                ErrorUtils.print(this.getClass()).error("Storage topic rank logsize end data has error,msg is ", e);
            }
        }
    }

    class CapacityStatsSubThread extends Thread {
        @Override
        public void run() {
            try {
                this.topicCapacityStats();
            } catch (Exception e) {
                ErrorUtils.print(this.getClass()).error("Collector topic capacity has error, msg is ", e);
            }
        }

        private void topicCapacityStats() {
            DashboardServiceImpl dashboardServiceImpl = null;
            try {
                dashboardServiceImpl = StartupListener.getBean("dashboardServiceImpl", DashboardServiceImpl.class);
            } catch (Exception e) {
                ErrorUtils.print(this.getClass()).error("Get dashboardServiceImpl bean be used for topic rank capacity has error, msg is ", e);
            }

            List<TopicRank> topicRanks = new ArrayList<>();
            String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
            for (String clusterAlias : clusterAliass) {
                List<String> topics = brokerService.topicList(clusterAlias);

                // clean up nonexistent topic
                Map<String, Object> params = new HashMap<>();
                params.put("cluster", clusterAlias);
                params.put("tkey", Topic.CAPACITY);
                List<TopicRank> trs = dashboardServiceImpl.getAllTopicRank(params);
                for (TopicRank tr : trs) {
                    try {
                        if (!topics.contains(tr.getTopic())) {
                            Map<String, Object> clean = new HashMap<>();
                            clean.put("cluster", clusterAlias);
                            clean.put("topic", tr.getTopic());
                            clean.put("tkey", Topic.CAPACITY);
                            dashboardServiceImpl.removeTopicRank(clean);
                        }
                    } catch (Exception e) {
                        ErrorUtils.print(this.getClass()).error("Failed to clean up nonexistent topic, msg is ", e);
                    }
                }

                for (String topic : topics) {
                    long capacity = 0L;
                    try {
                        capacity = kafkaMetricsService.topicCapacity(clusterAlias, topic);
                    } catch (Exception e) {
                        ErrorUtils.print(this.getClass()).error("Get topic capacity has error, msg is ", e);
                    }
                    TopicRank topicRank = new TopicRank();
                    topicRank.setCluster(clusterAlias);
                    topicRank.setTopic(topic);
                    topicRank.setTkey(Topic.CAPACITY);
                    topicRank.setTvalue(capacity);
                    topicRanks.add(topicRank);
                    if (topicRanks.size() > Topic.BATCH_SIZE) {
                        try {
                            dashboardServiceImpl.writeTopicRank(topicRanks);
                            topicRanks.clear();
                        } catch (Exception e) {
                            ErrorUtils.print(this.getClass()).error("Storage topic rank capacity has error, msg is ", e);
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
                ErrorUtils.print(this.getClass()).error("Storage topic rank capacity end data has error,msg is ", e);
            }

        }
    }

    class ProducerLogSizeStatsSubThread extends Thread {
        @Override
        public void run() {
            try {
                this.topicProducerLogSizeStats();
            } catch (Exception e) {
                ErrorUtils.print(this.getClass()).error("Collector topic logsize has error, msg is ", e);
            }
        }

        private void topicProducerLogSizeStats() {
            DashboardServiceImpl dashboardServiceImpl = null;
            try {
                dashboardServiceImpl = StartupListener.getBean("dashboardServiceImpl", DashboardServiceImpl.class);
            } catch (Exception e) {
                ErrorUtils.print(this.getClass()).error("Get dashboardServiceImpl bean be used for topic producer logsize has error,msg is ", e);
            }

            List<TopicRank> topicRanks = new ArrayList<>();
            List<TopicLogSize> topicLogSizes = new ArrayList<>();
            String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
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
                    if (topicLogSizes.size() > Topic.BATCH_SIZE) {
                        try {
                            dashboardServiceImpl.writeTopicLogSize(topicLogSizes);
                            topicLogSizes.clear();
                        } catch (Exception e) {
                            ErrorUtils.print(this.getClass()).error("Storage topic producer logsize has error, msg is ", e);
                        }
                    }
                }
                // stats producers thread
                try {
                    TopicRank topicRank = new TopicRank();
                    topicRank.setCluster(clusterAlias);
                    topicRank.setTopic(Topic.PRODUCER_THREADS_KEY);
                    topicRank.setTkey(Topic.PRODUCER_THREADS);
                    topicRank.setTvalue(producerThreads);
                    topicRanks.add(topicRank);
                    if (topicRanks.size() > Topic.BATCH_SIZE) {
                        try {
                            dashboardServiceImpl.writeTopicRank(topicRanks);
                            topicRanks.clear();
                        } catch (Exception e) {
                            ErrorUtils.print(this.getClass()).error("Storage topic producers has error, msg is ", e);
                        }
                    }
                } catch (Exception e) {
                    ErrorUtils.print(this.getClass()).error("Stats producers thread has error, msg is ", e);
                }
            }
            try {
                if (topicLogSizes.size() > 0) {
                    dashboardServiceImpl.writeTopicLogSize(topicLogSizes);
                    topicLogSizes.clear();
                }
            } catch (Exception e) {
                ErrorUtils.print(this.getClass()).error("Storage topic producer logsize end data has error, msg is ", e);
            }
            try {
                if (topicRanks.size() > 0) {
                    dashboardServiceImpl.writeTopicRank(topicRanks);
                    topicRanks.clear();
                }
            } catch (Exception e) {
                ErrorUtils.print(this.getClass()).error("Storage topic producer threads end data has error,msg is ", e);
            }
        }
    }

    class PerformanceByTopicStatsSubThread extends Thread {
        @Override
        public void run() {
            try {
                for (String bType : Topic.BROKER_PERFORMANCE_LIST) {
                    this.brokerPerformanceByTopicStats(bType);
                }
            } catch (Exception e) {
                ErrorUtils.print(this.getClass()).error("Collector broker spread by topic has error, msg is ", e);
            }
        }

        private void brokerPerformanceByTopicStats(String bType) {
            DashboardServiceImpl dashboardServiceImpl = null;
            try {
                dashboardServiceImpl = StartupListener.getBean("dashboardServiceImpl", DashboardServiceImpl.class);
            } catch (Exception e) {
                ErrorUtils.print(this.getClass()).error("Get dashboardServiceImpl bean be used for (spread, skewed, leader skewed) has error, msg is ", e);
            }

            List<TopicRank> topicRanks = new ArrayList<>();
            String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
            for (String clusterAlias : clusterAliass) {
                List<String> topics = brokerService.topicList(clusterAlias);

                // clean performance topics
                this.cleanPerformanceByTopics(clusterAlias, bType, dashboardServiceImpl, topics);

                for (String topic : topics) {
                    int tValue = 0;
                    if (bType.equals(Topic.BROKER_SPREAD)) {
                        tValue = brokerService.getBrokerSpreadByTopic(clusterAlias, topic);
                    } else if (bType.equals(Topic.BROKER_SKEWED)) {
                        tValue = brokerService.getBrokerSkewedByTopic(clusterAlias, topic);
                    } else if (bType.equals(Topic.BROKER_LEADER_SKEWED)) {
                        tValue = brokerService.getBrokerLeaderSkewedByTopic(clusterAlias, topic);
                    }
                    TopicRank topicRank = new TopicRank();
                    topicRank.setCluster(clusterAlias);
                    topicRank.setTopic(topic);
                    topicRank.setTkey(bType);
                    topicRank.setTvalue(tValue);
                    topicRanks.add(topicRank);
                    if (topicRanks.size() > Topic.BATCH_SIZE) {
                        try {
                            dashboardServiceImpl.writeTopicRank(topicRanks);
                            topicRanks.clear();
                        } catch (Exception e) {
                            ErrorUtils.print(this.getClass()).error("Storage topic [" + bType + "] has error, msg is ", e);
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
                ErrorUtils.print(this.getClass()).error("Storage topic [" + bType + "] end data has error, msg is ", e);
            }
        }

        private void cleanPerformanceByTopics(String clusterAlias, String type, DashboardServiceImpl dashboardServiceImpl, List<String> topics) {
            // clean up nonexistent topic
            Map<String, Object> params = new HashMap<>();
            params.put("cluster", clusterAlias);
            params.put("tkey", type);
            List<TopicRank> trs = dashboardServiceImpl.getAllTopicRank(params);
            for (TopicRank tr : trs) {
                try {
                    if (!topics.contains(tr.getTopic())) {
                        Map<String, Object> clean = new HashMap<>();
                        clean.put("cluster", clusterAlias);
                        clean.put("topic", tr.getTopic());
                        clean.put("tkey", type);
                        dashboardServiceImpl.removeTopicRank(clean);
                    }
                } catch (Exception e) {
                    ErrorUtils.print(this.getClass()).error("Failed to clean up nonexistent performance topic, msg is ", e);
                }
            }
        }
    }

    class CleanSubThread extends Thread {
        @Override
        public void run() {
            try {
                this.topicCleanTask();
            } catch (Exception e) {
                ErrorUtils.print(this.getClass()).error("Clean topic logsize has error, msg is ", e);
            }
        }

        private void topicCleanTask() {
            DashboardServiceImpl dashboardServiceImpl = null;
            try {
                dashboardServiceImpl = StartupListener.getBean("dashboardServiceImpl", DashboardServiceImpl.class);
            } catch (Exception e) {
                ErrorUtils.print(this.getClass()).error("Get dashboardServiceImpl bean has error, msg is ", e);
            }
            String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
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
                            ErrorUtils.print(this.getClass()).info("Add [" + Topic.CLEANUP_POLICY_KEY + "] topic[" + tr.getTopic() + "] property result," + cleanUpPolicyLog);
                            ErrorUtils.print(this.getClass()).info("Add [" + Topic.RETENTION_MS_KEY + "] topic[" + tr.getTopic() + "] property result," + retentionMsLog);
                        } else {
                            // delete znode
                            String cleanUpPolicyLog = kafkaMetricsService.changeTopicConfig(clusterAlias, tr.getTopic(), Topic.DELETE, new ConfigEntry(Topic.CLEANUP_POLICY_KEY, ""));
                            String retentionMsLog = kafkaMetricsService.changeTopicConfig(clusterAlias, tr.getTopic(), Topic.DELETE, new ConfigEntry(Topic.RETENTION_MS_KEY, ""));
                            ErrorUtils.print(this.getClass()).info("Delete [" + Topic.CLEANUP_POLICY_KEY + "] topic[" + tr.getTopic() + "] property result," + cleanUpPolicyLog);
                            ErrorUtils.print(this.getClass()).info("Delete [" + KConstants.Topic.RETENTION_MS_KEY + "] topic[" + tr.getTopic() + "] property result," + retentionMsLog);
                            // update db state
                            tr.setTvalue(1);
                            dashboardServiceImpl.writeTopicRank(Arrays.asList(tr));
                        }
                    }
                }
            }
        }
    }
}