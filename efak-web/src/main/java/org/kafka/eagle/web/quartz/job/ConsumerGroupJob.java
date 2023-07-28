/**
 * ConsumerGroupJob.java
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
import org.kafka.eagle.core.kafka.KafkaSchemaFactory;
import org.kafka.eagle.core.kafka.KafkaSchemaInitialize;
import org.kafka.eagle.core.kafka.KafkaStoragePlugin;
import org.kafka.eagle.pojo.cluster.BrokerInfo;
import org.kafka.eagle.pojo.cluster.ClusterInfo;
import org.kafka.eagle.pojo.cluster.KafkaClientInfo;
import org.kafka.eagle.pojo.consumer.ConsumerGroupInfo;
import org.kafka.eagle.web.service.IBrokerDaoService;
import org.kafka.eagle.web.service.IClusterDaoService;
import org.kafka.eagle.web.service.IConsumerGroupDaoService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/7/16 09:09
 * @Version: 3.4.0
 */
@Slf4j
public class ConsumerGroupJob extends QuartzJobBean {

    @Autowired
    private IClusterDaoService clusterDaoService;

    @Autowired
    private IBrokerDaoService brokerDaoService;

    @Autowired
    private IConsumerGroupDaoService consumerGroupDaoService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("Consumer group job has started, class = {}", this.getClass().getName());
        // logics
        this.consumerGroupTask();
    }

    private void consumerGroupTask() {
        List<ClusterInfo> clusterInfos = this.clusterDaoService.list();
        for (ClusterInfo clusterInfo : clusterInfos) {
            // 1. get online brokers
            List<BrokerInfo> brokerInfos = this.brokerDaoService.brokerStatus(clusterInfo.getClusterId(), Short.valueOf("1"));
            KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());

            // 2. init kafka client
            KafkaClientInfo kafkaClientInfo = KafkaSchemaInitialize.init(brokerInfos, clusterInfo);

            // 3. update consumer group
            // get realtime consumer group info
            List<ConsumerGroupInfo> consumerGroupRealtimeInfos = ksf.getConsumerGroups(kafkaClientInfo);
            if (consumerGroupRealtimeInfos == null || consumerGroupRealtimeInfos.size() == 0) {
                continue;
            }

            List<ConsumerGroupInfo> consumerGroupInfos = this.consumerGroupDaoService.consumerGroupList(clusterInfo.getClusterId());
            if (consumerGroupInfos != null && consumerGroupInfos.size() > 0) {
                List<ConsumerGroupInfo> consumerGroupDiffInfos = new ArrayList<>();
                consumerGroupInfos.stream().filter(consumerGroupInfo -> !consumerGroupRealtimeInfos.contains(consumerGroupInfo)).forEach(consumerGroupDiffInfos::add);

                // clean shutdown consumer group <maybe extract sub job to clean>
                List<Long> consumerGroupIds = consumerGroupDiffInfos.stream().map(ConsumerGroupInfo::getId).collect(Collectors.toList());
                if (consumerGroupIds != null && consumerGroupIds.size() > 0) {
                    this.consumerGroupDaoService.delete(consumerGroupIds);
                }
            }

            // add new consumer group
            consumerGroupRealtimeInfos.stream().forEach(consumerGroupInfo -> {
                this.consumerGroupDaoService.update(consumerGroupInfo);
            });

        }
    }
}
