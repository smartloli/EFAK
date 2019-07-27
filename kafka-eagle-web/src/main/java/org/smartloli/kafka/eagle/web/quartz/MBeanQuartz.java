/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.web.quartz;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.constant.JmxConstants.KafkaServer;
import org.smartloli.kafka.eagle.common.protocol.BrokersInfo;
import org.smartloli.kafka.eagle.common.protocol.KpiInfo;
import org.smartloli.kafka.eagle.common.protocol.MBeanInfo;
import org.smartloli.kafka.eagle.common.protocol.ZkClusterInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.CollectorType;
import org.smartloli.kafka.eagle.common.util.KConstants.MBean;
import org.smartloli.kafka.eagle.common.util.StrUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.common.util.ZKMetricsUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.Mx4jFactory;
import org.smartloli.kafka.eagle.core.factory.Mx4jService;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.MetricsServiceImpl;

/**
 * Per mins to stats mbean from kafka jmx.
 * 
 * @author smartloli.
 *
 *         Created by Jul 19, 2017
 */
public class MBeanQuartz {

	private Logger LOG = LoggerFactory.getLogger(MBeanQuartz.class);

	private static final String zk_packets_received = "zk_packets_received";
	private static final String zk_packets_sent = "zk_packets_sent";
	private static final String zk_num_alive_connections = "zk_num_alive_connections";
	private static final String zk_outstanding_requests = "zk_outstanding_requests";
	private static final String[] zk_kpis = new String[] { zk_packets_received, zk_packets_sent, zk_num_alive_connections, zk_outstanding_requests };

	private static final String[] broker_kpis = new String[] { MBean.MESSAGEIN, MBean.BYTEIN, MBean.BYTEOUT, MBean.BYTESREJECTED, MBean.FAILEDFETCHREQUEST, MBean.FAILEDPRODUCEREQUEST, MBean.TOTALFETCHREQUESTSPERSEC,
			MBean.TOTALPRODUCEREQUESTSPERSEC, MBean.REPLICATIONBYTESINPERSEC, MBean.REPLICATIONBYTESOUTPERSEC, MBean.PRODUCEMESSAGECONVERSIONS, MBean.OSTOTALMEMORY, MBean.OSFREEMEMORY };

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	/** Mx4j service interface. */
	private Mx4jService mx4jService = new Mx4jFactory().create();

	public void clean() {
		if (SystemConfigUtils.getBooleanProperty("kafka.eagle.metrics.charts")) {
			MetricsServiceImpl metrics = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
			int retain = SystemConfigUtils.getIntProperty("kafka.eagle.metrics.retain");
			metrics.remove(Integer.valueOf(CalendarUtils.getCustomLastDay(retain == 0 ? 7 : retain)));
			metrics.cleanConsumerTopic(Integer.valueOf(CalendarUtils.getCustomLastDay(retain == 0 ? 7 : retain)));
		}
	}

	public void mbeanQuartz() {
		if (SystemConfigUtils.getBooleanProperty("kafka.eagle.metrics.charts")) {
			String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
			for (String clusterAlias : clusterAliass) {
				kafkaCluster(clusterAlias);
				zkCluster(clusterAlias);
			}
		}
	}

	private void kafkaCluster(String clusterAlias) {
		List<BrokersInfo> brokers = kafkaService.getAllBrokersInfo(clusterAlias);
		List<KpiInfo> list = new ArrayList<>();

		for (String kpi : broker_kpis) {
			KpiInfo kpiInfo = new KpiInfo();
			kpiInfo.setCluster(clusterAlias);
			kpiInfo.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
			kpiInfo.setTimespan(CalendarUtils.getTimeSpan());
			kpiInfo.setKey(kpi);
			String broker = "";
			for (BrokersInfo kafka : brokers) {
				broker += kafka.getHost() + ",";
				kafkaAssembly(mx4jService, kpi, kpiInfo, kafka);
			}
			kpiInfo.setBroker(broker.length() == 0 ? "unkowns" : broker.substring(0, broker.length() - 1));
			kpiInfo.setType(CollectorType.KAFKA);
			list.add(kpiInfo);
		}

		MetricsServiceImpl metrics = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
		try {
			metrics.insert(list);
		} catch (Exception e) {
			LOG.error("Collector mbean data has error,msg is " + e.getMessage());
		}
	}

	private void kafkaAssembly(Mx4jService mx4jService, String type, KpiInfo kpiInfo, BrokersInfo kafka) {
		String uri = kafka.getHost() + ":" + kafka.getJmxPort();
		switch (type) {
		case MBean.MESSAGEIN:
			MBeanInfo msg = mx4jService.messagesInPerSec(uri);
			if (msg != null) {
				kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(msg.getOneMinute()) + "");
			}
			break;
		case MBean.BYTEIN:
			MBeanInfo bin = mx4jService.bytesInPerSec(uri);
			if (bin != null) {
				kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(bin.getOneMinute()) + "");
			}
			break;
		case MBean.BYTEOUT:
			MBeanInfo bout = mx4jService.bytesOutPerSec(uri);
			if (bout != null) {
				kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(bout.getOneMinute()) + "");
			}
			break;
		case MBean.BYTESREJECTED:
			MBeanInfo bytesRejectedPerSec = mx4jService.bytesRejectedPerSec(uri);
			if (bytesRejectedPerSec != null) {
				kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(bytesRejectedPerSec.getOneMinute()) + "");
			}
			break;
		case MBean.FAILEDFETCHREQUEST:
			MBeanInfo failedFetchRequestsPerSec = mx4jService.failedFetchRequestsPerSec(uri);
			if (failedFetchRequestsPerSec != null) {
				kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(failedFetchRequestsPerSec.getOneMinute()) + "");
			}
			break;
		case MBean.FAILEDPRODUCEREQUEST:
			MBeanInfo failedProduceRequestsPerSec = mx4jService.failedProduceRequestsPerSec(uri);
			if (failedProduceRequestsPerSec != null) {
				kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(failedProduceRequestsPerSec.getOneMinute()) + "");
			}
			break;
		case MBean.TOTALFETCHREQUESTSPERSEC:
			MBeanInfo totalFetchRequests = mx4jService.totalFetchRequestsPerSec(uri);
			if (totalFetchRequests != null) {
				kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(totalFetchRequests.getOneMinute()) + "");
			}
			break;
		case MBean.TOTALPRODUCEREQUESTSPERSEC:
			MBeanInfo totalProduceRequestsPerSec = mx4jService.totalProduceRequestsPerSec(uri);
			if (totalProduceRequestsPerSec != null) {
				kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(totalProduceRequestsPerSec.getOneMinute()) + "");
			}
			break;
		case MBean.REPLICATIONBYTESINPERSEC:
			MBeanInfo replicationBytesInPerSec = mx4jService.replicationBytesInPerSec(uri);
			if (replicationBytesInPerSec != null) {
				kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(replicationBytesInPerSec.getOneMinute()) + "");
			}
			break;
		case MBean.REPLICATIONBYTESOUTPERSEC:
			MBeanInfo replicationBytesOutPerSec = mx4jService.replicationBytesOutPerSec(uri);
			if (replicationBytesOutPerSec != null) {
				kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(replicationBytesOutPerSec.getOneMinute()) + "");
			}
			break;
		case MBean.PRODUCEMESSAGECONVERSIONS:
			MBeanInfo produceMessageConv = mx4jService.produceMessageConversionsPerSec(uri);
			if (produceMessageConv != null) {
				kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(produceMessageConv.getOneMinute()) + "");
			}
			break;
		case MBean.OSTOTALMEMORY:
			long totalMemory = kafkaService.getOSMemory(kafka.getHost(), kafka.getJmxPort(), KafkaServer.OS.totalPhysicalMemorySize);
			kpiInfo.setValue(Long.parseLong(kpiInfo.getValue() == null ? "0" : kpiInfo.getValue()) + totalMemory + "");
			break;
		case MBean.OSFREEMEMORY:
			long freeMemory = kafkaService.getOSMemory(kafka.getHost(), kafka.getJmxPort(), KafkaServer.OS.freePhysicalMemorySize);
			kpiInfo.setValue(Long.parseLong(kpiInfo.getValue() == null ? "0" : kpiInfo.getValue()) + freeMemory + "");
			break;
		default:
			break;
		}
	}

	private void zkCluster(String clusterAlias) {
		List<KpiInfo> list = new ArrayList<>();
		String zkList = SystemConfigUtils.getProperty(clusterAlias + ".zk.list");
		String[] zks = zkList.split(",");
		for (String kpi : zk_kpis) {
			KpiInfo kpiInfo = new KpiInfo();
			kpiInfo.setCluster(clusterAlias);
			kpiInfo.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
			kpiInfo.setTimespan(CalendarUtils.getTimeSpan());
			kpiInfo.setKey(kpi);
			String broker = "";
			for (String zk : zks) {
				String ip = zk.split(":")[0];
				String port = zk.split(":")[1];
				if (port.contains("/")) {
					port = port.split("/")[0];
				}
				broker += ip + ",";
				try {
					ZkClusterInfo zkInfo = ZKMetricsUtils.zkClusterInfo(ip, Integer.parseInt(port));
					zkAssembly(zkInfo, kpi, kpiInfo);
				} catch (Exception ex) {
					ex.printStackTrace();
					LOG.error("Transcation string to int has error,msg is " + ex.getMessage());
				}
			}
			kpiInfo.setBroker(broker.length() == 0 ? "unkowns" : broker.substring(0, broker.length() - 1));
			kpiInfo.setType(CollectorType.ZK);
			list.add(kpiInfo);
		}

		MetricsServiceImpl metrics = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
		try {
			metrics.insert(list);
		} catch (Exception e) {
			LOG.error("Collector zookeeper data has error,msg is " + e.getMessage());
		}
	}

	private static void zkAssembly(ZkClusterInfo zkInfo, String type, KpiInfo kpiInfo) {
		switch (type) {
		case zk_packets_received:
			kpiInfo.setValue(Long.parseLong(kpiInfo.getValue() == null ? "0" : kpiInfo.getValue()) + Long.parseLong(zkInfo.getZkPacketsReceived()) + "");
			break;
		case zk_packets_sent:
			kpiInfo.setValue(Long.parseLong(kpiInfo.getValue() == null ? "0" : kpiInfo.getValue()) + Long.parseLong(zkInfo.getZkPacketsSent()) + "");
			break;
		case zk_num_alive_connections:
			kpiInfo.setValue(Long.parseLong(kpiInfo.getValue() == null ? "0" : kpiInfo.getValue()) + Long.parseLong(zkInfo.getZkNumAliveConnections()) + "");
			break;
		case zk_outstanding_requests:
			kpiInfo.setValue(Long.parseLong(kpiInfo.getValue() == null ? "0" : kpiInfo.getValue()) + Long.parseLong(zkInfo.getZkOutstandingRequests()) + "");
			break;
		default:
			break;
		}
	}

}
