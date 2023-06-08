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

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.core.kafka.KafkaClusterFetcher;
import org.kafka.eagle.pojo.cluster.BrokerInfo;
import org.kafka.eagle.pojo.cluster.ClusterCreateInfo;
import org.kafka.eagle.pojo.cluster.ClusterInfo;
import org.kafka.eagle.pojo.kafka.JMXInitializeInfo;
import org.kafka.eagle.web.service.IBrokerDaoService;
import org.kafka.eagle.web.service.IClusterCreateDaoService;
import org.kafka.eagle.web.service.IClusterDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

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

    @Async
    @Scheduled(fixedRate = 60000)
    public void clusterHealthyTask() {
        List<ClusterInfo> clusterInfos = this.clusterDaoService.list();
        for (ClusterInfo clusterInfo : clusterInfos) {
            // 1.submit cluster healthy status to mysql
            List<ClusterCreateInfo> clusterCreateInfos = this.clusterCreateDaoService.clusters(clusterInfo.getClusterId());
            int size = 0;
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
            }

            if (clusterCreateInfos != null && clusterCreateInfos.size() == size) {
                clusterInfo.setStatus(1);
            } else {
                clusterInfo.setStatus(0);
            }

            this.clusterDaoService.update(clusterInfo);


        }

        // 2.submit broker healthy status to mysql


    }

    @Async
    // @Scheduled(fixedRate = 5000)
    public void test1() {
        // log.info("test1, {}", Thread.currentThread().getName());
    }

}
