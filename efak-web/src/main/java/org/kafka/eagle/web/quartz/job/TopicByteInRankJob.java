/**
 * TopicRankJob.java
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
package org.kafka.eagle.web.quartz.job;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.common.constants.JmxConstants;
import org.kafka.eagle.common.constants.KConstants;
import org.kafka.eagle.common.utils.StrUtils;
import org.kafka.eagle.core.kafka.KafkaMBeanFetcher;
import org.kafka.eagle.core.kafka.KafkaSchemaFactory;
import org.kafka.eagle.core.kafka.KafkaSchemaInitialize;
import org.kafka.eagle.core.kafka.KafkaStoragePlugin;
import org.kafka.eagle.pojo.cluster.BrokerInfo;
import org.kafka.eagle.pojo.cluster.ClusterCreateInfo;
import org.kafka.eagle.pojo.cluster.ClusterInfo;
import org.kafka.eagle.pojo.cluster.KafkaClientInfo;
import org.kafka.eagle.pojo.kafka.JMXInitializeInfo;
import org.kafka.eagle.pojo.topic.MBeanInfo;
import org.kafka.eagle.pojo.topic.TopicRankInfo;
import org.kafka.eagle.web.service.IBrokerDaoService;
import org.kafka.eagle.web.service.IClusterCreateDaoService;
import org.kafka.eagle.web.service.IClusterDaoService;
import org.kafka.eagle.web.service.ITopicRankDaoService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.*;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/8/14 16:04
 * @Version: 3.4.0
 */
@Slf4j
public class TopicByteInRankJob extends QuartzJobBean {

    @Autowired
    private IClusterDaoService clusterDaoService;

    @Autowired
    private IClusterCreateDaoService clusterCreateDaoService;

    @Autowired
    private IBrokerDaoService brokerDaoService;

    @Autowired
    private ITopicRankDaoService topicRankDaoService;


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("Topic byte in rank job has started, class = {}", this.getClass().getName());
        // logics
        this.topicRank();
    }

    private void topicRank() {
        List<TopicRankInfo> topicRankInfoList = new ArrayList<>();
        List<ClusterInfo> clusterInfos = this.clusterDaoService.list();
        for (ClusterInfo clusterInfo : clusterInfos) {

            // 1. get online brokers
            List<BrokerInfo> brokerInfos = this.brokerDaoService.brokerStatus(clusterInfo.getClusterId(), Short.valueOf("1"));
            KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());

            // 2. init kafka client
            KafkaClientInfo kafkaClientInfo = KafkaSchemaInitialize.init(brokerInfos, clusterInfo);
            Set<String> topics = ksf.getTopicNames(kafkaClientInfo);
            Map<String, String> topicMBeanKey = new HashMap<String, String>();
            for (String topic : topics) {
                topicMBeanKey.put(KConstants.MBean.BYTEIN + JmxConstants.MBEAN_KEY_SEPARATOR + topic, String.format(JmxConstants.BrokerServer.BYTES_IN_PER_SEC_TOPIC.getValue(), topic));
            }

            Map<String, String> kafkaMBeanMap = new HashMap<>();
            List<ClusterCreateInfo> clusterCreateInfos = this.clusterCreateDaoService.clusters(clusterInfo.getClusterId());
            for (ClusterCreateInfo clusterCreateInfo : clusterCreateInfos) {
                JMXInitializeInfo initializeInfo = new JMXInitializeInfo();
                initializeInfo.setBrokerId(clusterCreateInfo.getBrokerId());
                initializeInfo.setHost(clusterCreateInfo.getBrokerHost());
                initializeInfo.setPort(clusterCreateInfo.getBrokerJmxPort());
                Map<String, MBeanInfo> mBeanInfoMap = KafkaMBeanFetcher.mbean(initializeInfo, topicMBeanKey);
                for (Map.Entry<String, MBeanInfo> entry : mBeanInfoMap.entrySet()) {
                    if (kafkaMBeanMap.containsKey(entry.getKey())) {
                        String value = kafkaMBeanMap.get(entry.getKey());
                        Double sum = StrUtils.numberic(StrUtil.isBlank(entry.getValue().getOneMinute()) == true ? "0.0" : entry.getValue().getOneMinute()) + StrUtils.numberic(value);
                        kafkaMBeanMap.put(entry.getKey(), String.valueOf(StrUtils.numberic(sum)));
                    } else {
                        String value = StrUtil.isBlank(entry.getValue().getOneMinute()) == true ? "0.0" : entry.getValue().getOneMinute();
                        kafkaMBeanMap.put(entry.getKey(), String.valueOf(StrUtils.numberic(value)));
                    }
                }
            }

            // 3. get topic rank
            if (kafkaMBeanMap != null) {
                for (Map.Entry<String, String> entry : kafkaMBeanMap.entrySet()) {
                    TopicRankInfo topicRankInfo = new TopicRankInfo();
                    topicRankInfo.setClusterId(clusterInfo.getClusterId());
                    topicRankInfo.setTopicName(entry.getKey().split(JmxConstants.MBEAN_KEY_SEPARATOR)[1]);
                    topicRankInfo.setTopicKey(entry.getKey().split(JmxConstants.MBEAN_KEY_SEPARATOR)[0]);
                    topicRankInfo.setTopicValue(String.valueOf(entry.getValue()));
                    topicRankInfoList.add(topicRankInfo);
                    // 4. save topic rank
                    if (topicRankInfoList.size() > KConstants.MYSQL_BATCH_SIZE) {
                        this.topicRankDaoService.replace(topicRankInfoList);
                        topicRankInfoList.clear();
                    }
                }
                if (topicRankInfoList.size() > 0) {
                    this.topicRankDaoService.replace(topicRankInfoList);
                    topicRankInfoList.clear();
                }
            }
        }
    }
}
