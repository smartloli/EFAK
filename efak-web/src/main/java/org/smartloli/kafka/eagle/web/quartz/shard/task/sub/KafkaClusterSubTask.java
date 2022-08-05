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
package org.smartloli.kafka.eagle.web.quartz.shard.task.sub;

import org.smartloli.kafka.eagle.common.constant.JmxConstants;
import org.smartloli.kafka.eagle.common.protocol.BrokersInfo;
import org.smartloli.kafka.eagle.common.protocol.KpiInfo;
import org.smartloli.kafka.eagle.common.protocol.MBeanInfo;
import org.smartloli.kafka.eagle.common.protocol.cache.BrokerCache;
import org.smartloli.kafka.eagle.common.util.*;
import org.smartloli.kafka.eagle.common.util.KConstants.MBean;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.Mx4jFactory;
import org.smartloli.kafka.eagle.core.factory.Mx4jService;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.MetricsServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Collect kafka cluster dataset.
 *
 * @author smartloli.
 * <p>
 * Created by Dec 09, 2021
 */
public class KafkaClusterSubTask extends Thread {


    private static final String[] BROKER_KPIS = new String[]{MBean.MESSAGEIN, MBean.BYTEIN, MBean.BYTEOUT, MBean.BYTESREJECTED, MBean.FAILEDFETCHREQUEST, MBean.FAILEDPRODUCEREQUEST, MBean.TOTALFETCHREQUESTSPERSEC, MBean.TOTALPRODUCEREQUESTSPERSEC, MBean.REPLICATIONBYTESINPERSEC, MBean.REPLICATIONBYTESOUTPERSEC, MBean.PRODUCEMESSAGECONVERSIONS,
            KConstants.MBean.OSTOTALMEMORY, MBean.OSFREEMEMORY, MBean.CPUUSED};

    /**
     * Kafka service interface.
     */
    private KafkaService kafkaService = new KafkaFactory().create();

    /**
     * Mx4j service interface.
     */
    private Mx4jService mx4jService = new Mx4jFactory().create();


    @Override
    public synchronized void run() {
        try {
            if (SystemConfigUtils.getBooleanProperty("efak.metrics.charts")) {
                String[] clusterAliass = SystemConfigUtils.getPropertyArray("efak.zk.cluster.alias", ",");
                for (String clusterAlias : clusterAliass) {
                    this.kafkaCluster(clusterAlias);
                }
            }
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Get kafka cluster metrics has error, msg is ", e);
        }
    }

    private void kafkaCluster(String clusterAlias) {
        // List<BrokersInfo> brokers = kafkaService.getAllBrokersInfo(clusterAlias);
        List<BrokersInfo> brokers = BrokerCache.META_CACHE.get(clusterAlias);
        List<KpiInfo> list = new ArrayList<>();

        for (String kpi : BROKER_KPIS) {
            KpiInfo kpiInfo = new KpiInfo();
            kpiInfo.setCluster(clusterAlias);
            kpiInfo.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
            kpiInfo.setTimespan(CalendarUtils.getTimeSpan());
            kpiInfo.setKey(kpi);
            for (BrokersInfo kafka : brokers) {
                this.kafkaAssembly(clusterAlias, mx4jService, kpi, kpiInfo, kafka);
            }
            kpiInfo.setBroker(clusterAlias);
            kpiInfo.setType(KConstants.CollectorType.KAFKA);
            list.add(kpiInfo);
        }

        MetricsServiceImpl metrics = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
        try {
            metrics.insert(list);
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Collector mbean data has error, msg is ", e);
        }
    }

    private void kafkaAssembly(String clusterAlias, Mx4jService mx4jService, String type, KpiInfo kpiInfo, BrokersInfo kafka) {
        String uri = kafka.getHost() + ":" + kafka.getJmxPort();
        switch (type) {
            case MBean.MESSAGEIN:
                MBeanInfo msg = mx4jService.messagesInPerSec(clusterAlias, uri);
                if (msg != null) {
                    kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(msg.getOneMinute()) + "");
                }
                break;
            case MBean.BYTEIN:
                MBeanInfo bin = mx4jService.bytesInPerSec(clusterAlias, uri);
                if (bin != null) {
                    kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(bin.getOneMinute()) + "");
                }
                break;
            case MBean.BYTEOUT:
                MBeanInfo bout = mx4jService.bytesOutPerSec(clusterAlias, uri);
                if (bout != null) {
                    kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(bout.getOneMinute()) + "");
                }
                break;
            case MBean.BYTESREJECTED:
                MBeanInfo bytesRejectedPerSec = mx4jService.bytesRejectedPerSec(clusterAlias, uri);
                if (bytesRejectedPerSec != null) {
                    kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(bytesRejectedPerSec.getOneMinute()) + "");
                }
                break;
            case MBean.FAILEDFETCHREQUEST:
                MBeanInfo failedFetchRequestsPerSec = mx4jService.failedFetchRequestsPerSec(clusterAlias, uri);
                if (failedFetchRequestsPerSec != null) {
                    kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(failedFetchRequestsPerSec.getOneMinute()) + "");
                }
                break;
            case MBean.FAILEDPRODUCEREQUEST:
                MBeanInfo failedProduceRequestsPerSec = mx4jService.failedProduceRequestsPerSec(clusterAlias, uri);
                if (failedProduceRequestsPerSec != null) {
                    kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(failedProduceRequestsPerSec.getOneMinute()) + "");
                }
                break;
            case MBean.TOTALFETCHREQUESTSPERSEC:
                MBeanInfo totalFetchRequests = mx4jService.totalFetchRequestsPerSec(clusterAlias, uri);
                if (totalFetchRequests != null) {
                    kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(totalFetchRequests.getOneMinute()) + "");
                }
                break;
            case MBean.TOTALPRODUCEREQUESTSPERSEC:
                MBeanInfo totalProduceRequestsPerSec = mx4jService.totalProduceRequestsPerSec(clusterAlias, uri);
                if (totalProduceRequestsPerSec != null) {
                    kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(totalProduceRequestsPerSec.getOneMinute()) + "");
                }
                break;
            case MBean.REPLICATIONBYTESINPERSEC:
                MBeanInfo replicationBytesInPerSec = mx4jService.replicationBytesInPerSec(clusterAlias, uri);
                if (replicationBytesInPerSec != null) {
                    kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(replicationBytesInPerSec.getOneMinute()) + "");
                }
                break;
            case MBean.REPLICATIONBYTESOUTPERSEC:
                MBeanInfo replicationBytesOutPerSec = mx4jService.replicationBytesOutPerSec(clusterAlias, uri);
                if (replicationBytesOutPerSec != null) {
                    kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(replicationBytesOutPerSec.getOneMinute()) + "");
                }
                break;
            case MBean.PRODUCEMESSAGECONVERSIONS:
                MBeanInfo produceMessageConv = mx4jService.produceMessageConversionsPerSec(clusterAlias, uri);
                if (produceMessageConv != null) {
                    kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(produceMessageConv.getOneMinute()) + "");
                }
                break;
            case MBean.OSTOTALMEMORY:
                long totalMemory = kafkaService.getOSMemory(clusterAlias, kafka.getHost(), kafka.getJmxPort(), JmxConstants.BrokerServer.TOTAL_PHYSICAL_MEMORY_SIZE.getValue());
                kpiInfo.setValue(Long.parseLong(kpiInfo.getValue() == null ? "0" : kpiInfo.getValue()) + totalMemory + "");
                break;
            case MBean.OSFREEMEMORY:
                long freeMemory = kafkaService.getOSMemory(clusterAlias, kafka.getHost(), kafka.getJmxPort(), JmxConstants.BrokerServer.FREE_PHYSICAL_MEMORY_SIZE.getValue());
                kpiInfo.setValue(Long.parseLong(kpiInfo.getValue() == null ? "0" : kpiInfo.getValue()) + freeMemory + "");
                break;
            case MBean.CPUUSED:
                double cpu = kafkaService.getUsedCpuValue(clusterAlias, kafka.getHost(), kafka.getJmxPort());
                kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.00" : kpiInfo.getValue()) + cpu + "");
                break;
            default:
                break;
        }
    }
}
