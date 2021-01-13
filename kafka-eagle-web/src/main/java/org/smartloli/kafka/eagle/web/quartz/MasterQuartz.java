/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.web.quartz;

import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.ErrorUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.MBean;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.common.util.WorkUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.MetricsServiceImpl;

import java.util.List;

/**
 * Used to periodically generate all tasks and issue work node execution policy.
 *
 * @author smartloli.
 * <p>
 * Created by Jul 06, 2020
 */
public class MasterQuartz {

    /**
     * Service interface area, include  {@link KafkaService}.
     */
    private KafkaService kafkaService = new KafkaFactory().create();

    private static final String[] MBEAN_TASK_KEYS = new String[]{MBean.MESSAGEIN, MBean.BYTEIN, MBean.BYTEOUT, MBean.BYTESREJECTED, MBean.FAILEDFETCHREQUEST, MBean.FAILEDPRODUCEREQUEST, MBean.TOTALFETCHREQUESTSPERSEC, MBean.TOTALPRODUCEREQUESTSPERSEC, MBean.REPLICATIONBYTESINPERSEC, MBean.REPLICATIONBYTESOUTPERSEC, MBean.PRODUCEMESSAGECONVERSIONS, MBean.OSTOTALMEMORY, MBean.OSFREEMEMORY, MBean.CPUUSED};

    public void clean() {
        ErrorUtils.print(this.getClass()).info("Master node starts cleaning up expired data.");
        if (SystemConfigUtils.getBooleanProperty("kafka.eagle.metrics.charts")) {
            MetricsServiceImpl metrics = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
            int retain = SystemConfigUtils.getIntProperty("kafka.eagle.metrics.retain");
            metrics.remove(Integer.valueOf(CalendarUtils.getCustomLastDay(retain == 0 ? 30 : retain)));
            metrics.cleanTopicLogSize(Integer.valueOf(CalendarUtils.getCustomLastDay(retain == 0 ? 30 : retain)));
            metrics.cleanBScreenConsumerTopic(Integer.valueOf(CalendarUtils.getCustomLastDay(retain == 0 ? 30 : retain)));
            metrics.cleanTopicSqlHistory(Integer.valueOf(CalendarUtils.getCustomLastDay(retain == 0 ? 30 : retain)));
        }
    }

    public void masterJobQuartz() {
        // whether is enable distributed
        if (SystemConfigUtils.getBooleanProperty("kafka.eagle.distributed.enable")) {
            // jobForDistributedAllTasks();
        } else {
            jobForStandaloneAllTasks();
        }
    }

    private void jobForDistributedAllTasks() {
        String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
        // get all kafka eagle work node
        List<String> workNodes = WorkUtils.getWorkNodes();
        for (String clusterAlias : clusterAliass) {
            //
            strategyForBrokerMetrics(clusterAlias, workNodes);
            // get all topics

            // get all consumer groups and topic

            // get all
        }
    }

    private void jobForStandaloneAllTasks() {
        // topic
        new TopicRankSubTask().start();

        // broker metrics
        new MetricsSubTask().start();

        // broker mbean
        new MBeanSubTask().start();
    }

    private void strategyForBrokerMetrics(String clusterAlias, List<String> workNodes) {
        // List<BrokersInfo> brokers = kafkaService.getAllBrokersInfo(clusterAlias);

    }

}
