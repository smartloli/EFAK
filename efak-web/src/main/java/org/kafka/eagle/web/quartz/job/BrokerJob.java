/**
 * BrokerJob.java
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
import org.kafka.eagle.core.kafka.KafkaClusterFetcher;
import org.kafka.eagle.pojo.cluster.BrokerInfo;
import org.kafka.eagle.pojo.cluster.ClusterCreateInfo;
import org.kafka.eagle.pojo.cluster.ClusterInfo;
import org.kafka.eagle.pojo.kafka.JMXInitializeInfo;
import org.kafka.eagle.web.service.IBrokerDaoService;
import org.kafka.eagle.web.service.IClusterCreateDaoService;
import org.kafka.eagle.web.service.IClusterDaoService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: TODO
 * @Author: smartloli
 * @Date: 2023/7/15 10:31
 * @Version: 3.4.0
 */
@Slf4j
public class BrokerJob extends QuartzJobBean {

    @Autowired
    private IClusterCreateDaoService clusterCreateDaoService;

    @Autowired
    private IClusterDaoService clusterDaoService;

    @Autowired
    private IBrokerDaoService brokerDaoService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        // get parameters
        context.getJobDetail().getJobDataMap().forEach(
                (k, v) -> log.info("param, key:{}, value:{}", k, v)
        );
        // logics
        this.brokerHealthyTask();
    }

    private void brokerHealthyTask() {
        List<ClusterInfo> clusterInfos = this.clusterDaoService.list();
        for (ClusterInfo clusterInfo : clusterInfos) {
            List<ClusterCreateInfo> clusterCreateInfos = this.clusterCreateDaoService.clusters(clusterInfo.getClusterId());
            List<BrokerInfo> brokerInfos = new ArrayList<>();
            for (ClusterCreateInfo clusterCreateInfo : clusterCreateInfos) {
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

        }

    }

}
