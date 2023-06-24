/**
 * ClusterManageTask.java
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
package org.kafka.eagle.web.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.common.constants.KConstants;
import org.kafka.eagle.common.utils.MathUtil;
import org.kafka.eagle.core.kafka.KafkaClusterFetcher;
import org.kafka.eagle.core.kafka.KafkaSchemaFactory;
import org.kafka.eagle.core.kafka.KafkaSchemaInitialize;
import org.kafka.eagle.core.kafka.KafkaStoragePlugin;
import org.kafka.eagle.pojo.cluster.BrokerInfo;
import org.kafka.eagle.pojo.cluster.ClusterCreateInfo;
import org.kafka.eagle.pojo.cluster.ClusterInfo;
import org.kafka.eagle.pojo.cluster.KafkaClientInfo;
import org.kafka.eagle.pojo.kafka.JMXInitializeInfo;
import org.kafka.eagle.pojo.topic.MetadataInfo;
import org.kafka.eagle.pojo.topic.TopicInfo;
import org.kafka.eagle.pojo.topic.TopicMetadataInfo;
import org.kafka.eagle.web.service.IBrokerDaoService;
import org.kafka.eagle.web.service.IClusterCreateDaoService;
import org.kafka.eagle.web.service.IClusterDaoService;
import org.kafka.eagle.web.service.ITopicDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/6/7 14:35
 * @Version: 3.4.0
 */
@Slf4j
@Component
@EnableScheduling
@EnableAsync
public class ClusterManageTask {


    @Autowired
    private IClusterCreateDaoService clusterCreateDaoService;

    @Autowired
    private IClusterDaoService clusterDaoService;

    @Autowired
    private IBrokerDaoService brokerDaoService;

    @Autowired
    private ITopicDaoService topicDaoService;

    /**
     * check cluster and broker healthy status task.
     */
    @Async
    @Scheduled(fixedRate = 60000)
    public void clusterHealthyTask() {
        List<ClusterInfo> clusterInfos = this.clusterDaoService.list();
        for (ClusterInfo clusterInfo : clusterInfos) {
            // 1.submit cluster healthy status to mysql
            List<ClusterCreateInfo> clusterCreateInfos = this.clusterCreateDaoService.clusters(clusterInfo.getClusterId());
            int size = 0;
            List<BrokerInfo> brokerInfos = new ArrayList<>();
            for (ClusterCreateInfo clusterCreateInfo : clusterCreateInfos) {
                boolean status = KafkaClusterFetcher.getKafkaAliveStatus(clusterCreateInfo.getBrokerHost(), clusterCreateInfo.getBrokerPort());
                if (status) {
                    size++;
                }
                JMXInitializeInfo initializeInfo = new JMXInitializeInfo();
                initializeInfo.setBrokerId(clusterCreateInfo.getBrokerId());
                initializeInfo.setHost(clusterCreateInfo.getBrokerHost());
                initializeInfo.setPort(clusterCreateInfo.getBrokerJmxPort());
                BrokerInfo brokerInfo = KafkaClusterFetcher.getKafkaJmxInfo(initializeInfo);
                brokerInfo.setBrokerId(clusterCreateInfo.getBrokerId());
                brokerInfo.setBrokerPort(clusterCreateInfo.getBrokerPort());
                brokerInfo.setBrokerPortStatus(KafkaClusterFetcher.getBrokerStatus(clusterCreateInfo.getBrokerHost(), clusterCreateInfo.getBrokerPort()));
                brokerInfo.setClusterId(clusterInfo.getClusterId());
                this.brokerDaoService.update(brokerInfo);

                brokerInfos.add(brokerInfo);
                if (brokerInfos != null && brokerInfos.size() > KConstants.MYSQL_BATCH_SIZE) {
                    this.brokerDaoService.update(brokerInfos);
                    brokerInfos.clear();
                }
            }

            if (brokerInfos.size() > 0) {
                this.brokerDaoService.update(brokerInfos);
                brokerInfos.clear();
            }

            if (clusterCreateInfos != null && clusterCreateInfos.size() == size) {
                clusterInfo.setStatus(1);
            } else {
                clusterInfo.setStatus(0);
            }

            this.clusterDaoService.update(clusterInfo);


        }

    }

    /**
     * update topic info task.
     */
    @Async
    @Scheduled(fixedRate = 60000)
    public void topicMetadataTask() {
        List<TopicInfo> topicInfos = new ArrayList<>();

        List<ClusterInfo> clusterInfos = this.clusterDaoService.list();
        for (ClusterInfo clusterInfo : clusterInfos) {
            // 1. get online brokers
            List<BrokerInfo> brokerInfos = this.brokerDaoService.brokerStatus(clusterInfo.getClusterId(), Short.valueOf("1"));
            KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());

            // 2. init kafka client
            KafkaClientInfo kafkaClientInfo = KafkaSchemaInitialize.init(brokerInfos, clusterInfo);

            // 3. get kafka info by @KafkaSchemaFactory
            Set<String> topicNames = ksf.getTopicNames(kafkaClientInfo);

            // 4. delete topics that do not exist in kafka
            List<TopicInfo> topicInfoList = this.topicDaoService.topics(clusterInfo.getClusterId());
            List<Long> waitDeleteTopicIds = new ArrayList<>();
            for (TopicInfo topicInfo : topicInfoList) {
                if (!topicNames.contains(topicInfo.getTopicName())) {
                    waitDeleteTopicIds.add(topicInfo.getId());
                }
            }
            if (waitDeleteTopicIds != null && waitDeleteTopicIds.size() > 0) {
                this.topicDaoService.delete(waitDeleteTopicIds);
            }

            // 5. update topic info
            Map<String, TopicMetadataInfo> topicMetaMaps = ksf.getTopicMetaData(kafkaClientInfo, topicNames);

            for (String topicName : topicNames) {
                TopicInfo topicInfo = new TopicInfo();
                topicInfo.setClusterId(clusterInfo.getClusterId());
                topicInfo.setTopicName(topicName);
                TopicMetadataInfo metadataInfos = topicMetaMaps.get(topicName);
                topicInfo.setPartitions(metadataInfos == null ? 0 : metadataInfos.getMetadataInfos().size());

                int partitionAndReplicaTopics = 0;
                Map<Integer, Integer> brokers = new HashMap<>();
                Set<Integer> brokerSizes = new HashSet<>();
                Map<Integer, Integer> brokerLeaders = new HashMap<>();
                for (MetadataInfo meta : metadataInfos.getMetadataInfos()) {
                    List<Integer> replicasIntegers = new ArrayList<>();
                    try {
                        replicasIntegers = JSON.parseObject(meta.getReplicas(), new TypeReference<ArrayList<Integer>>() {
                        });
                    } catch (Exception e) {
                        log.error("Parse string to int list has error, msg is {}", e);
                    }
                    brokerSizes.addAll(replicasIntegers);
                    partitionAndReplicaTopics += replicasIntegers.size();
                    for (Integer brokerId : replicasIntegers) {
                        if (brokers.containsKey(brokerId)) {
                            int value = brokers.get(brokerId);
                            brokers.put(brokerId, value + 1);
                        } else {
                            brokers.put(brokerId, 1);
                        }
                    }
                    if (brokerLeaders.containsKey(meta.getLeader())) {
                        int value = brokerLeaders.get(meta.getLeader());
                        brokerLeaders.put(meta.getLeader(), value + 1);
                    } else {
                        brokerLeaders.put(meta.getLeader(), 1);
                    }
                }
                topicInfo.setReplications(brokerSizes.size());

                int brokerSize = brokerInfos.size();
                int normalSkewedValue = MathUtil.ceil(brokerSize, partitionAndReplicaTopics);
                int brokerSkewSize = 0;
                for (Map.Entry<Integer, Integer> entry : brokers.entrySet()) {
                    if (entry.getValue() > normalSkewedValue) {
                        brokerSkewSize++;
                    }
                }

                int brokerSkewLeaderNormal = MathUtil.ceil(brokerSize, metadataInfos.getMetadataInfos().size());
                int brokerSkewLeaderSize = 0;
                for (Map.Entry<Integer, Integer> entry : brokerLeaders.entrySet()) {
                    if (entry.getValue() > brokerSkewLeaderNormal) {
                        brokerSkewLeaderSize++;
                    }
                }

                int spread = 0;
                int skewed = 0;
                int leaderSkewed = 0;
                if (brokerSize > 0) {
                    spread = brokerSizes.size() * 100 / brokerSize;
                    skewed = brokerSkewSize * 100 / brokerSize;
                    leaderSkewed = brokerSkewLeaderSize * 100 / brokerSize;
                }

                topicInfo.setBrokerSpread(spread);
                topicInfo.setBrokerSkewed(skewed);
                topicInfo.setBrokerLeaderSkewed(leaderSkewed);

                topicInfo.setRetainMs(metadataInfos == null ? 0 : Long.parseLong(metadataInfos.getRetainMs()));
                this.topicDaoService.update(topicInfo);

                topicInfos.add(topicInfo);
                if (topicInfos != null && topicInfos.size() > KConstants.MYSQL_BATCH_SIZE) {
                    this.topicDaoService.update(topicInfos);
                    topicInfos.clear();
                }
            }
            if (topicInfos.size() > 0) {
                this.topicDaoService.update(topicInfos);
                topicInfos.clear();
            }
        }
    }

}
