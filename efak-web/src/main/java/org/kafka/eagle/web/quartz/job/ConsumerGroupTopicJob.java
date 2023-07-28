/**
 * ConsumerGroupTopicJob.java
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

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.ConsumerGroupState;
import org.kafka.eagle.common.constants.KConstants;
import org.kafka.eagle.core.kafka.KafkaSchemaFactory;
import org.kafka.eagle.core.kafka.KafkaSchemaInitialize;
import org.kafka.eagle.core.kafka.KafkaStoragePlugin;
import org.kafka.eagle.pojo.cluster.BrokerInfo;
import org.kafka.eagle.pojo.cluster.ClusterInfo;
import org.kafka.eagle.pojo.cluster.KafkaClientInfo;
import org.kafka.eagle.pojo.consumer.ConsumerGroupInfo;
import org.kafka.eagle.pojo.consumer.ConsumerGroupTopicInfo;
import org.kafka.eagle.web.service.IBrokerDaoService;
import org.kafka.eagle.web.service.IClusterDaoService;
import org.kafka.eagle.web.service.IConsumerGroupTopicDaoService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/7/27 23:18
 * @Version: 3.4.0
 */
@Slf4j
public class ConsumerGroupTopicJob extends QuartzJobBean {

    @Autowired
    private IClusterDaoService clusterDaoService;

    @Autowired
    private IBrokerDaoService brokerDaoService;

    @Autowired
    private IConsumerGroupTopicDaoService consumerGroupTopicDaoService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("Consumer group topic job has started, class = {}", this.getClass().getName());
        // logics
        this.consumerGroupTopicTask();
    }

    private void consumerGroupTopicTask() {
        // stats consumer group topic logsize, offsets, lag
        List<ClusterInfo> clusterInfos = this.clusterDaoService.list();
        for (ClusterInfo clusterInfo : clusterInfos) {
            // 1. get online brokers
            List<BrokerInfo> brokerInfos = this.brokerDaoService.brokerStatus(clusterInfo.getClusterId(), Short.valueOf("1"));
            KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());

            // 2. init kafka client
            KafkaClientInfo kafkaClientInfo = KafkaSchemaInitialize.init(brokerInfos, clusterInfo);

            // 3. add consumer group topic
            // get realtime consumer group topic offsets info
            List<ConsumerGroupInfo> consumerGroupRealtimeInfos = ksf.getConsumerGroups(kafkaClientInfo);
            Set<String> groupIds = new HashSet<>();
            List<ConsumerGroupTopicInfo> consumerGroupTopicRealtimeInfos = new ArrayList<>();

            for (ConsumerGroupInfo consumerGroupInfo : consumerGroupRealtimeInfos) {
                if (consumerGroupInfo.getState().equals(ConsumerGroupState.STABLE.name())) {
                    // get active group id
                    groupIds.add(consumerGroupInfo.getGroupId());
                }
                // get realtime group id
                ConsumerGroupTopicInfo consumerGroupTopicInfo = new ConsumerGroupTopicInfo();
                consumerGroupTopicInfo.setClusterId(clusterInfo.getClusterId());
                consumerGroupTopicInfo.setGroupId(consumerGroupInfo.getGroupId());
                consumerGroupTopicInfo.setTopicName(consumerGroupInfo.getTopicName());
                consumerGroupTopicRealtimeInfos.add(consumerGroupTopicInfo);
            }

            List<ConsumerGroupTopicInfo> consumerGroupTopicInfoList = this.consumerGroupTopicDaoService.consumerGroupTopicList(clusterInfo.getClusterId());

            List<ConsumerGroupTopicInfo> consumerGroupTopicDiffInfos = new ArrayList<>();
            if (consumerGroupTopicInfoList != null && consumerGroupTopicInfoList.size() > 0) {
                consumerGroupTopicInfoList.stream().filter(consumerGroupInfo -> !consumerGroupTopicRealtimeInfos.contains(consumerGroupInfo)).forEach(consumerGroupTopicDiffInfos::add);
                // clean shutdown consumer group <maybe extract sub job to clean>
                List<Long> consumerGroupTopicIds = consumerGroupTopicDiffInfos.stream().map(ConsumerGroupTopicInfo::getId).collect(Collectors.toList());
                if (consumerGroupTopicIds != null && consumerGroupTopicIds.size() > 0) {
                    this.consumerGroupTopicDaoService.delete(consumerGroupTopicIds);
                }
            }

            List<ConsumerGroupTopicInfo> consumerGroupTopicInfos = ksf.getKafkaConsumerGroupTopic(kafkaClientInfo, groupIds);
            List<ConsumerGroupTopicInfo> storageConsumerTopicInfos = new ArrayList<>();
            for (ConsumerGroupTopicInfo consumerGroupTopicInfo : consumerGroupTopicInfos) {
                ConsumerGroupTopicInfo lastConsumerTopicOfDb = this.consumerGroupTopicDaoService.consumerGroupTopic(consumerGroupTopicInfo);
                if (lastConsumerTopicOfDb == null || lastConsumerTopicOfDb.getOffsets() == 0) {
                    consumerGroupTopicInfo.setOffsetsDiff(0L);
                } else {
                    // maybe server timespan is not synchronized.
                    consumerGroupTopicInfo.setOffsetsDiff(Math.abs(consumerGroupTopicInfo.getOffsets() - lastConsumerTopicOfDb.getOffsets()));
                }

                if (lastConsumerTopicOfDb == null || consumerGroupTopicInfo.getLogsize() == 0) {
                    consumerGroupTopicInfo.setLogsizeDiff(0L);
                } else {
                    // maybe server timespan is not synchronized.
                    consumerGroupTopicInfo.setLogsizeDiff(Math.abs(consumerGroupTopicInfo.getLogsize() - lastConsumerTopicOfDb.getLogsize()));
                }
                storageConsumerTopicInfos.add(consumerGroupTopicInfo);
                if (storageConsumerTopicInfos != null && storageConsumerTopicInfos.size() > KConstants.MYSQL_BATCH_SIZE) {
                    this.consumerGroupTopicDaoService.batch(storageConsumerTopicInfos);
                    storageConsumerTopicInfos.clear();
                }
            }

            System.out.println("storageConsumerTopicInfos final:" + JSON.toJSONString(storageConsumerTopicInfos));
            if (storageConsumerTopicInfos != null && storageConsumerTopicInfos.size() > 0) {
                this.consumerGroupTopicDaoService.batch(storageConsumerTopicInfos);
                storageConsumerTopicInfos.clear();
            }
        }
    }

}
