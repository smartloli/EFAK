/**
 * MockJob.java
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
import org.kafka.eagle.web.service.IBrokerDaoService;
import org.kafka.eagle.web.service.IClusterDaoService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/8/4 23:47
 * @Version: 3.4.0
 */
@Slf4j
public class MockJob extends QuartzJobBean {

    @Autowired
    private IClusterDaoService clusterDaoService;

    @Autowired
    private IBrokerDaoService brokerDaoService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("Mock job has started, class = {}", this.getClass().getName());
        // logics
        ClusterInfo clusterInfo = clusterDaoService.clusters(7L);
        List<BrokerInfo> brokerInfos = brokerDaoService.clusters(clusterInfo.getClusterId());
        KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());
        KafkaClientInfo kafkaClientInfo = KafkaSchemaInitialize.init(brokerInfos, clusterInfo);
        Random random = new Random();
        int size = random.nextInt(10);
        log.info("send mock test data size[{}], kafka[{}] ", size, kafkaClientInfo.toString());
        for (int i = 0; i < size; i++) {
            ksf.sendMsg(kafkaClientInfo, "ke28", "test_" + i + "_" + new Date().getTime());
        }
    }

}
