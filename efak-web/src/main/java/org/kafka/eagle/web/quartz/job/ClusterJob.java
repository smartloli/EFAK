/**
 * ClusterJob.java
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
import org.kafka.eagle.core.kafka.KafkaClusterFetcher;
import org.kafka.eagle.pojo.cluster.ClusterCreateInfo;
import org.kafka.eagle.pojo.cluster.ClusterInfo;
import org.kafka.eagle.web.service.IClusterCreateDaoService;
import org.kafka.eagle.web.service.IClusterDaoService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.List;

/**
 * Description: TODO
 * @Author: smartloli
 * @Date: 2023/7/15 10:19
 * @Version: 3.4.0
 */
@Slf4j
public class ClusterJob extends QuartzJobBean {

    @Autowired
    private IClusterCreateDaoService clusterCreateDaoService;

    @Autowired
    private IClusterDaoService clusterDaoService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("Cluster job has started, class = {}", this.getClass().getName());
        // logics
        this.clusterHealthyTask();
    }

    private void clusterHealthyTask() {
        List<ClusterInfo> clusterInfos = this.clusterDaoService.list();
        for (ClusterInfo clusterInfo : clusterInfos) {
            List<ClusterCreateInfo> clusterCreateInfos = this.clusterCreateDaoService.clusters(clusterInfo.getClusterId());
            int size = 0;
            for (ClusterCreateInfo clusterCreateInfo : clusterCreateInfos) {
                boolean status = KafkaClusterFetcher.getKafkaAliveStatus(clusterCreateInfo.getBrokerHost(), clusterCreateInfo.getBrokerPort());
                if (status) {
                    size++;
                }
            }

            if (clusterCreateInfos != null && clusterCreateInfos.size() == size) {
                clusterInfo.setStatus(1);
            } else {
                clusterInfo.setStatus(0);
            }

            this.clusterDaoService.update(clusterInfo);
        }

    }

}
