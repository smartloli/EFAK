/**
 * TopicSummaryJob.java
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
import org.kafka.eagle.pojo.topic.TopicSummaryInfo;
import org.kafka.eagle.web.service.IBrokerDaoService;
import org.kafka.eagle.web.service.IClusterDaoService;
import org.kafka.eagle.web.service.ITopicDaoService;
import org.kafka.eagle.web.service.ITopicSummaryDaoService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Description: TODO
 * @Author: smartloli
 * @Date: 2023/7/15 10:40
 * @Version: 3.4.0
 */
@Slf4j
public class TopicSummaryJob extends QuartzJobBean {

    @Autowired
    private IClusterDaoService clusterDaoService;

    @Autowired
    private IBrokerDaoService brokerDaoService;

    @Autowired
    private ITopicDaoService topicDaoService;

    @Autowired
    private ITopicSummaryDaoService topicSummaryDaoService;


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("Topic summary job has started, class = {}", this.getClass().getName());
        // logics
        this.topicSummaryTask();
    }

    public void topicSummaryTask() {
        List<TopicSummaryInfo> topicSummaryInfos = new ArrayList<>();

        List<ClusterInfo> clusterInfos = this.clusterDaoService.list();
        for (ClusterInfo clusterInfo : clusterInfos) {
            // 1. get online brokers
            List<BrokerInfo> brokerInfos = this.brokerDaoService.brokerStatus(clusterInfo.getClusterId(), Short.valueOf("1"));
            KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());

            // 2. init kafka client
            KafkaClientInfo kafkaClientInfo = KafkaSchemaInitialize.init(brokerInfos, clusterInfo);

            // 3. get kafka info by @KafkaSchemaFactory
            Set<String> topicNames = ksf.getTopicNames(kafkaClientInfo);

            // 4. update topic summary info
            for (String topicName : topicNames) {
                updateTopicSummary(topicSummaryInfos, clusterInfo, ksf, kafkaClientInfo, topicName);
            }

            // 5. batch update of topic summary
            if (topicSummaryInfos.size() > 0) {
                this.topicSummaryDaoService.batch(topicSummaryInfos);
                topicSummaryInfos.clear();
            }

        }
    }

    private void updateTopicSummary(List<TopicSummaryInfo> topicSummaryInfos, ClusterInfo clusterInfo, KafkaSchemaFactory ksf, KafkaClientInfo kafkaClientInfo, String topicName) {
        TopicSummaryInfo topicSummaryInfo = new TopicSummaryInfo();
        topicSummaryInfo.setClusterId(clusterInfo.getClusterId());
        topicSummaryInfo.setTopicName(topicName);
        try {
            topicSummaryInfo.setLogSize(ksf.getTopicOfTotalLogSize(kafkaClientInfo, topicName));
            TopicSummaryInfo latestTopicSummaryInfo = topicSummaryDaoService.topicOfLatest(clusterInfo.getClusterId(), topicName, topicSummaryInfo.getDay());
            if (latestTopicSummaryInfo == null || latestTopicSummaryInfo.getLogSize() == 0) {
                topicSummaryInfo.setLogSizeDiffVal(0L);
            } else {
                // maybe server timespan is not synchronized.
                topicSummaryInfo.setLogSizeDiffVal(Math.abs(topicSummaryInfo.getLogSize() - latestTopicSummaryInfo.getLogSize()));
            }
        } catch (Exception e) {
            log.error("Get topic logsize diff val has error, msg is {}", e);
        }

        topicSummaryInfos.add(topicSummaryInfo);
        if (topicSummaryInfos != null && topicSummaryInfos.size() > KConstants.MYSQL_BATCH_SIZE) {
            this.topicSummaryDaoService.batch(topicSummaryInfos);
            topicSummaryInfos.clear();
        }
    }


}
