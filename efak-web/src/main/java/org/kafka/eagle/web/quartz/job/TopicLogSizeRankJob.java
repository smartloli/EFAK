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

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.common.constants.KConstants;
import org.kafka.eagle.core.kafka.KafkaSchemaFactory;
import org.kafka.eagle.core.kafka.KafkaSchemaInitialize;
import org.kafka.eagle.core.kafka.KafkaStoragePlugin;
import org.kafka.eagle.pojo.cluster.BrokerInfo;
import org.kafka.eagle.pojo.cluster.ClusterInfo;
import org.kafka.eagle.pojo.cluster.KafkaClientInfo;
import org.kafka.eagle.pojo.topic.TopicRankInfo;
import org.kafka.eagle.web.service.IBrokerDaoService;
import org.kafka.eagle.web.service.IClusterDaoService;
import org.kafka.eagle.web.service.ITopicRankDaoService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/8/14 16:04
 * @Version: 3.4.0
 */
@Slf4j
public class TopicLogSizeRankJob extends QuartzJobBean {

    @Autowired
    private IClusterDaoService clusterDaoService;

    @Autowired
    private IBrokerDaoService brokerDaoService;

    @Autowired
    private ITopicRankDaoService topicRankDaoService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("Topic rank job has started, class = {}", this.getClass().getName());
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
            Map<String, Long> topicLogSizeMap = ksf.getAllTopicOfLogSize(kafkaClientInfo);

            // 3. get topic rank
            if (topicLogSizeMap != null) {
                for (Map.Entry<String, Long> entry : topicLogSizeMap.entrySet()) {
                    TopicRankInfo topicRankInfo = new TopicRankInfo();
                    topicRankInfo.setClusterId(clusterInfo.getClusterId());
                    topicRankInfo.setTopicName(entry.getKey());
                    topicRankInfo.setTopicKey(KConstants.Topic.LOGSIZE);
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
