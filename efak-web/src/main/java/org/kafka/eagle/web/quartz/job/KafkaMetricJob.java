/**
 * KafkaMetricJob.java
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
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.common.constants.JmxConstants.BrokerServer;
import org.kafka.eagle.common.constants.KConstants;
import org.kafka.eagle.common.utils.StrUtils;
import org.kafka.eagle.core.kafka.KafkaMBeanFetcher;
import org.kafka.eagle.pojo.cluster.ClusterCreateInfo;
import org.kafka.eagle.pojo.cluster.ClusterInfo;
import org.kafka.eagle.pojo.cluster.KafkaMBeanInfo;
import org.kafka.eagle.pojo.kafka.JMXInitializeInfo;
import org.kafka.eagle.pojo.topic.MBeanInfo;
import org.kafka.eagle.web.service.IClusterCreateDaoService;
import org.kafka.eagle.web.service.IClusterDaoService;
import org.kafka.eagle.web.service.IKafkaMBeanDaoService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/8/5 11:40
 * @Version: 3.4.0
 */
@Slf4j
public class KafkaMetricJob extends QuartzJobBean {

    @Autowired
    private IClusterCreateDaoService clusterCreateDaoService;

    @Autowired
    private IClusterDaoService clusterDaoService;

    @Autowired
    private IKafkaMBeanDaoService kafkaMBeanDaoService;

    private final Map<String, String> BROKER_MBEAN_MAP_KEYS = new HashMap<String, String>() {{
        put(KConstants.MBean.MESSAGEIN, BrokerServer.MESSAGES_IN_PER_SEC.getValue());
        put(KConstants.MBean.BYTEIN, BrokerServer.BYTES_IN_PER_SEC.getValue());
        put(KConstants.MBean.BYTEOUT, BrokerServer.BYTES_OUT_PER_SEC.getValue());
        put(KConstants.MBean.BYTESREJECTED, BrokerServer.BYTES_REJECTED_PER_SEC.getValue());
        put(KConstants.MBean.FAILEDFETCHREQUEST, BrokerServer.FAILED_FETCH_REQUESTS_PER_SEC.getValue());
        put(KConstants.MBean.FAILEDPRODUCEREQUEST, BrokerServer.FAILED_PRODUCE_REQUESTS_PER_SEC.getValue());
        put(KConstants.MBean.TOTALFETCHREQUESTSPERSEC, BrokerServer.TOTAL_FETCH_REQUESTS_PER_SEC.getValue());
        put(KConstants.MBean.TOTALPRODUCEREQUESTSPERSEC, BrokerServer.TOTAL_PRODUCE_REQUESTS_PER_SEC.getValue());
        put(KConstants.MBean.REPLICATIONBYTESINPERSEC, BrokerServer.REPLICATION_BYTES_IN_PER_SEC.getValue());
        put(KConstants.MBean.REPLICATIONBYTESOUTPERSEC, BrokerServer.REPLICATION_BYTES_OUT_PER_SEC.getValue());
        put(KConstants.MBean.PRODUCEMESSAGECONVERSIONS, BrokerServer.PRODUCE_MESSAGE_CONVERSIONS_PER_SEC.getValue());
        put(KConstants.MBean.OSFREEMEMORY, BrokerServer.BROKER_OS_MEM_FREE.getValue());
        put(KConstants.MBean.CPUUSED, BrokerServer.BROKER_OS_CPU_USED.getValue());
    }};

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("Kafka metric job has started, class = {}", this.getClass().getName());
        // logics
        this.kafkaMetricTask();
    }

    private void kafkaMetricTask() {
        List<ClusterInfo> clusterInfos = this.clusterDaoService.list();

        for (ClusterInfo clusterInfo : clusterInfos) {
            List<KafkaMBeanInfo> kafkaMetricInfos = new ArrayList<>();
            Map<String, String> kafkaMBeanMap = new HashMap<>();
            List<ClusterCreateInfo> clusterCreateInfos = this.clusterCreateDaoService.clusters(clusterInfo.getClusterId());
            for (ClusterCreateInfo clusterCreateInfo : clusterCreateInfos) {
                JMXInitializeInfo initializeInfo = new JMXInitializeInfo();
                initializeInfo.setBrokerId(clusterCreateInfo.getBrokerId());
                initializeInfo.setHost(clusterCreateInfo.getBrokerHost());
                initializeInfo.setPort(clusterCreateInfo.getBrokerJmxPort());
                Map<String, MBeanInfo> mBeanInfoMap = KafkaMBeanFetcher.mbean(initializeInfo, BROKER_MBEAN_MAP_KEYS);
                System.out.println("mBeanInfoMap:" + JSON.toJSONString(mBeanInfoMap));
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

            for (Map.Entry<String, String> entry : kafkaMBeanMap.entrySet()) {
                KafkaMBeanInfo kafkaMetricInfo = new KafkaMBeanInfo();
                kafkaMetricInfo.setClusterId(clusterInfo.getClusterId());
                kafkaMetricInfo.setMbeanKey(entry.getKey());
                kafkaMetricInfo.setMbeanValue(entry.getValue());
                kafkaMetricInfos.add(kafkaMetricInfo);
            }
            System.out.println("kafkaMetricInfos:" + JSON.toJSONString(kafkaMetricInfos));
            this.kafkaMBeanDaoService.batch(kafkaMetricInfos);
        }
    }


}
