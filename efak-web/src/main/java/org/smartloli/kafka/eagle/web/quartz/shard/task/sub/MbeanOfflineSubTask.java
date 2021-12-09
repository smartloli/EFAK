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

import org.smartloli.kafka.eagle.common.protocol.BrokersInfo;
import org.smartloli.kafka.eagle.common.protocol.MBeanInfo;
import org.smartloli.kafka.eagle.common.protocol.MBeanOfflineInfo;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.KConstants.MBean;
import org.smartloli.kafka.eagle.common.util.LoggerUtils;
import org.smartloli.kafka.eagle.common.util.StrUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.Mx4jFactory;
import org.smartloli.kafka.eagle.core.factory.Mx4jService;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.MetricsServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Collect kafka broker mbean offline dataset.
 *
 * @author smartloli.
 * <p>
 * Created by Dec 09, 2021
 */
public class MbeanOfflineSubTask extends Thread {

    private static final String zk_packets_received = "zk_packets_received";
    private static final String zk_packets_sent = "zk_packets_sent";
    private static final String zk_num_alive_connections = "zk_num_alive_connections";
    private static final String zk_outstanding_requests = "zk_outstanding_requests";
    private static final String[] zk_kpis = new String[]{zk_packets_received, zk_packets_sent, zk_num_alive_connections, zk_outstanding_requests};

    private static final String[] broker_kpis = new String[]{KConstants.MBean.MESSAGEIN, MBean.BYTEIN, MBean.BYTEOUT, MBean.BYTESREJECTED, MBean.FAILEDFETCHREQUEST, MBean.FAILEDPRODUCEREQUEST, MBean.TOTALFETCHREQUESTSPERSEC, MBean.TOTALPRODUCEREQUESTSPERSEC, MBean.REPLICATIONBYTESINPERSEC, MBean.REPLICATIONBYTESOUTPERSEC, MBean.PRODUCEMESSAGECONVERSIONS,
            KConstants.MBean.OSTOTALMEMORY, KConstants.MBean.OSFREEMEMORY, MBean.CPUUSED};
    private static final String[] BROKER_KPIS_OFFLINE = new String[]{MBean.MESSAGEIN, MBean.BYTEIN, MBean.BYTEOUT, MBean.BYTESREJECTED, MBean.FAILEDFETCHREQUEST, MBean.FAILEDPRODUCEREQUEST, MBean.TOTALFETCHREQUESTSPERSEC, MBean.TOTALPRODUCEREQUESTSPERSEC, MBean.REPLICATIONBYTESINPERSEC, MBean.REPLICATIONBYTESOUTPERSEC, MBean.PRODUCEMESSAGECONVERSIONS};

    /**
     * Kafka service interface.
     */
    private KafkaService kafkaService = new KafkaFactory().create();

    /**
     * Mx4j service interface.
     */
    private Mx4jService mx4jService = new Mx4jFactory().create();

    @Override
    public void run() {
        try {
            if (SystemConfigUtils.getBooleanProperty("efak.metrics.charts")) {
                String[] clusterAliass = SystemConfigUtils.getPropertyArray("efak.zk.cluster.alias", ",");
                for (String clusterAlias : clusterAliass) {
                    this.brokerMbeanOffline(clusterAlias);
                }
            }
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Get broker mbean metrics has error, msg is ", e);
        }
    }

    private void brokerMbeanOffline(String clusterAlias) {
        List<BrokersInfo> brokers = kafkaService.getAllBrokersInfo(clusterAlias);
        List<MBeanOfflineInfo> list = new ArrayList<>();

        for (String kpi : BROKER_KPIS_OFFLINE) {
            MBeanOfflineInfo mbeanOffline = new MBeanOfflineInfo();
            mbeanOffline.setCluster(clusterAlias);
            mbeanOffline.setKey(kpi);
            for (BrokersInfo kafka : brokers) {
                this.kafkaMBeanOfflineAssembly(clusterAlias, mx4jService, kpi, mbeanOffline, kafka);
            }
            list.add(mbeanOffline);
        }
        MetricsServiceImpl metrics = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
        try {
            metrics.mbeanOfflineInsert(list);
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Collector mbean offline data has error, msg is ", e);
        }
    }

    private void kafkaMBeanOfflineAssembly(String clusterAlias, Mx4jService mx4jService, String type, MBeanOfflineInfo mbeanOffline, BrokersInfo kafka) {
        String uri = kafka.getHost() + ":" + kafka.getJmxPort();
        switch (type) {
            case MBean.MESSAGEIN:
                MBeanInfo msg = mx4jService.messagesInPerSec(clusterAlias, uri);
                if (msg != null) {
                    mbeanOffline.setOneMinute(StrUtils.assembly(msg.getOneMinute() == null ? "0.00" : msg.getOneMinute()));
                    mbeanOffline.setMeanRate(StrUtils.assembly(msg.getMeanRate() == null ? "0.00" : msg.getMeanRate()));
                    mbeanOffline.setFiveMinute(StrUtils.assembly(msg.getFiveMinute() == null ? "0.00" : msg.getFiveMinute()));
                    mbeanOffline.setFifteenMinute(StrUtils.assembly(msg.getFifteenMinute() == null ? "0.00" : msg.getFifteenMinute()));
                }
                break;
            case MBean.BYTEIN:
                MBeanInfo bin = mx4jService.bytesInPerSec(clusterAlias, uri);
                if (bin != null) {
                    mbeanOffline.setOneMinute(StrUtils.assembly(bin.getOneMinute() == null ? "0.00" : bin.getOneMinute()));
                    mbeanOffline.setMeanRate(StrUtils.assembly(bin.getMeanRate() == null ? "0.00" : bin.getMeanRate()));
                    mbeanOffline.setFiveMinute(StrUtils.assembly(bin.getFiveMinute() == null ? "0.00" : bin.getFiveMinute()));
                    mbeanOffline.setFifteenMinute(StrUtils.assembly(bin.getFifteenMinute() == null ? "0.00" : bin.getFifteenMinute()));
                }
                break;
            case MBean.BYTEOUT:
                MBeanInfo bout = mx4jService.bytesOutPerSec(clusterAlias, uri);
                if (bout != null) {
                    mbeanOffline.setOneMinute(StrUtils.assembly(bout.getOneMinute() == null ? "0.00" : bout.getOneMinute()));
                    mbeanOffline.setMeanRate(StrUtils.assembly(bout.getMeanRate() == null ? "0.00" : bout.getMeanRate()));
                    mbeanOffline.setFiveMinute(StrUtils.assembly(bout.getFiveMinute() == null ? "0.00" : bout.getFiveMinute()));
                    mbeanOffline.setFifteenMinute(StrUtils.assembly(bout.getFifteenMinute() == null ? "0.00" : bout.getFifteenMinute()));
                }
                break;
            case MBean.BYTESREJECTED:
                MBeanInfo bytesRejectedPerSec = mx4jService.bytesRejectedPerSec(clusterAlias, uri);
                if (bytesRejectedPerSec != null) {
                    mbeanOffline.setOneMinute(StrUtils.assembly(bytesRejectedPerSec.getOneMinute() == null ? "0.00" : bytesRejectedPerSec.getOneMinute()));
                    mbeanOffline.setMeanRate(StrUtils.assembly(bytesRejectedPerSec.getMeanRate() == null ? "0.00" : bytesRejectedPerSec.getMeanRate()));
                    mbeanOffline.setFiveMinute(StrUtils.assembly(bytesRejectedPerSec.getFiveMinute() == null ? "0.00" : bytesRejectedPerSec.getFiveMinute()));
                    mbeanOffline.setFifteenMinute(StrUtils.assembly(bytesRejectedPerSec.getFifteenMinute() == null ? "0.00" : bytesRejectedPerSec.getFifteenMinute()));
                }
                break;
            case MBean.FAILEDFETCHREQUEST:
                MBeanInfo failedFetchRequestsPerSec = mx4jService.failedFetchRequestsPerSec(clusterAlias, uri);
                if (failedFetchRequestsPerSec != null) {
                    mbeanOffline.setOneMinute(StrUtils.assembly(failedFetchRequestsPerSec.getOneMinute() == null ? "0.00" : failedFetchRequestsPerSec.getOneMinute()));
                    mbeanOffline.setMeanRate(StrUtils.assembly(failedFetchRequestsPerSec.getMeanRate() == null ? "0.00" : failedFetchRequestsPerSec.getMeanRate()));
                    mbeanOffline.setFiveMinute(StrUtils.assembly(failedFetchRequestsPerSec.getFiveMinute() == null ? "0.00" : failedFetchRequestsPerSec.getFiveMinute()));
                    mbeanOffline.setFifteenMinute(StrUtils.assembly(failedFetchRequestsPerSec.getFifteenMinute() == null ? "0.00" : failedFetchRequestsPerSec.getFifteenMinute()));
                }
                break;
            case MBean.FAILEDPRODUCEREQUEST:
                MBeanInfo failedProduceRequestsPerSec = mx4jService.failedProduceRequestsPerSec(clusterAlias, uri);
                if (failedProduceRequestsPerSec != null) {
                    mbeanOffline.setOneMinute(StrUtils.assembly(failedProduceRequestsPerSec.getOneMinute() == null ? "0.00" : failedProduceRequestsPerSec.getOneMinute()));
                    mbeanOffline.setMeanRate(StrUtils.assembly(failedProduceRequestsPerSec.getMeanRate() == null ? "0.00" : failedProduceRequestsPerSec.getMeanRate()));
                    mbeanOffline.setFiveMinute(StrUtils.assembly(failedProduceRequestsPerSec.getFiveMinute() == null ? "0.00" : failedProduceRequestsPerSec.getFiveMinute()));
                    mbeanOffline.setFifteenMinute(StrUtils.assembly(failedProduceRequestsPerSec.getFifteenMinute() == null ? "0.00" : failedProduceRequestsPerSec.getFifteenMinute()));
                }
                break;
            case MBean.TOTALFETCHREQUESTSPERSEC:
                MBeanInfo totalFetchRequests = mx4jService.totalFetchRequestsPerSec(clusterAlias, uri);
                if (totalFetchRequests != null) {
                    mbeanOffline.setOneMinute(StrUtils.assembly(totalFetchRequests.getOneMinute() == null ? "0.00" : totalFetchRequests.getOneMinute()));
                    mbeanOffline.setMeanRate(StrUtils.assembly(totalFetchRequests.getMeanRate() == null ? "0.00" : totalFetchRequests.getMeanRate()));
                    mbeanOffline.setFiveMinute(StrUtils.assembly(totalFetchRequests.getFiveMinute() == null ? "0.00" : totalFetchRequests.getFiveMinute()));
                    mbeanOffline.setFifteenMinute(StrUtils.assembly(totalFetchRequests.getFifteenMinute() == null ? "0.00" : totalFetchRequests.getFifteenMinute()));
                }
                break;
            case MBean.TOTALPRODUCEREQUESTSPERSEC:
                MBeanInfo totalProduceRequestsPerSec = mx4jService.totalProduceRequestsPerSec(clusterAlias, uri);
                if (totalProduceRequestsPerSec != null) {
                    mbeanOffline.setOneMinute(StrUtils.assembly(totalProduceRequestsPerSec.getOneMinute() == null ? "0.00" : totalProduceRequestsPerSec.getOneMinute()));
                    mbeanOffline.setMeanRate(StrUtils.assembly(totalProduceRequestsPerSec.getMeanRate() == null ? "0.00" : totalProduceRequestsPerSec.getMeanRate()));
                    mbeanOffline.setFiveMinute(StrUtils.assembly(totalProduceRequestsPerSec.getFiveMinute() == null ? "0.00" : totalProduceRequestsPerSec.getFiveMinute()));
                    mbeanOffline.setFifteenMinute(StrUtils.assembly(totalProduceRequestsPerSec.getFifteenMinute() == null ? "0.00" : totalProduceRequestsPerSec.getFifteenMinute()));
                }
                break;
            case MBean.REPLICATIONBYTESINPERSEC:
                MBeanInfo replicationBytesInPerSec = mx4jService.replicationBytesInPerSec(clusterAlias, uri);
                if (replicationBytesInPerSec != null) {
                    mbeanOffline.setOneMinute(StrUtils.assembly(replicationBytesInPerSec.getOneMinute() == null ? "0.00" : replicationBytesInPerSec.getOneMinute()));
                    mbeanOffline.setMeanRate(StrUtils.assembly(replicationBytesInPerSec.getMeanRate() == null ? "0.00" : replicationBytesInPerSec.getMeanRate()));
                    mbeanOffline.setFiveMinute(StrUtils.assembly(replicationBytesInPerSec.getFiveMinute() == null ? "0.00" : replicationBytesInPerSec.getFiveMinute()));
                    mbeanOffline.setFifteenMinute(StrUtils.assembly(replicationBytesInPerSec.getFifteenMinute() == null ? "0.00" : replicationBytesInPerSec.getFifteenMinute()));
                }
                break;
            case MBean.REPLICATIONBYTESOUTPERSEC:
                MBeanInfo replicationBytesOutPerSec = mx4jService.replicationBytesOutPerSec(clusterAlias, uri);
                if (replicationBytesOutPerSec != null) {
                    mbeanOffline.setOneMinute(StrUtils.assembly(replicationBytesOutPerSec.getOneMinute() == null ? "0.00" : replicationBytesOutPerSec.getOneMinute()));
                    mbeanOffline.setMeanRate(StrUtils.assembly(replicationBytesOutPerSec.getMeanRate() == null ? "0.00" : replicationBytesOutPerSec.getMeanRate()));
                    mbeanOffline.setFiveMinute(StrUtils.assembly(replicationBytesOutPerSec.getFiveMinute() == null ? "0.00" : replicationBytesOutPerSec.getFiveMinute()));
                    mbeanOffline.setFifteenMinute(StrUtils.assembly(replicationBytesOutPerSec.getFifteenMinute() == null ? "0.00" : replicationBytesOutPerSec.getFifteenMinute()));
                }
                break;
            case KConstants.MBean.PRODUCEMESSAGECONVERSIONS:
                MBeanInfo produceMessageConv = mx4jService.produceMessageConversionsPerSec(clusterAlias, uri);
                if (produceMessageConv != null) {
                    mbeanOffline.setOneMinute(StrUtils.assembly(produceMessageConv.getOneMinute() == null ? "0.00" : produceMessageConv.getOneMinute()));
                    mbeanOffline.setMeanRate(StrUtils.assembly(produceMessageConv.getMeanRate() == null ? "0.00" : produceMessageConv.getMeanRate()));
                    mbeanOffline.setFiveMinute(StrUtils.assembly(produceMessageConv.getFiveMinute() == null ? "0.00" : produceMessageConv.getFiveMinute()));
                    mbeanOffline.setFifteenMinute(StrUtils.assembly(produceMessageConv.getFifteenMinute() == null ? "0.00" : produceMessageConv.getFifteenMinute()));
                }
                break;
            default:
                break;
        }
    }
}
