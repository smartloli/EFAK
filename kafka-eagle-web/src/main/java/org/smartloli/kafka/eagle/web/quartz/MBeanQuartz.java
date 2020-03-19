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

import org.smartloli.kafka.eagle.common.constant.JmxConstants.BrokerServer;
import org.smartloli.kafka.eagle.common.protocol.BrokersInfo;
import org.smartloli.kafka.eagle.common.protocol.KpiInfo;
import org.smartloli.kafka.eagle.common.protocol.MBeanInfo;
import org.smartloli.kafka.eagle.common.protocol.MBeanOfflineInfo;
import org.smartloli.kafka.eagle.common.protocol.ZkClusterInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.CollectorType;
import org.smartloli.kafka.eagle.common.util.KConstants.MBean;
import org.smartloli.kafka.eagle.common.util.StrUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.common.util.ThrowExceptionUtils;
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

	private static final String zk_packets_received = "zk_packets_received";
	private static final String zk_packets_sent = "zk_packets_sent";
	private static final String zk_num_alive_connections = "zk_num_alive_connections";
	private static final String zk_outstanding_requests = "zk_outstanding_requests";
	private static final String[] zk_kpis = new String[] { zk_packets_received, zk_packets_sent, zk_num_alive_connections, zk_outstanding_requests };

	private static final String[] broker_kpis = new String[] { MBean.MESSAGEIN, MBean.BYTEIN, MBean.BYTEOUT, MBean.BYTESREJECTED, MBean.FAILEDFETCHREQUEST, MBean.FAILEDPRODUCEREQUEST, MBean.TOTALFETCHREQUESTSPERSEC, MBean.TOTALPRODUCEREQUESTSPERSEC, MBean.REPLICATIONBYTESINPERSEC, MBean.REPLICATIONBYTESOUTPERSEC, MBean.PRODUCEMESSAGECONVERSIONS,
			MBean.OSTOTALMEMORY, MBean.OSFREEMEMORY };
	private static final String[] BROKER_KPIS_OFFLINE = new String[] { MBean.MESSAGEIN, MBean.BYTEIN, MBean.BYTEOUT, MBean.BYTESREJECTED, MBean.FAILEDFETCHREQUEST, MBean.FAILEDPRODUCEREQUEST, MBean.TOTALFETCHREQUESTSPERSEC, MBean.TOTALPRODUCEREQUESTSPERSEC, MBean.REPLICATIONBYTESINPERSEC, MBean.REPLICATIONBYTESOUTPERSEC, MBean.PRODUCEMESSAGECONVERSIONS };

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	/** Mx4j service interface. */
	private Mx4jService mx4jService = new Mx4jFactory().create();

	public void clean() {
		if (SystemConfigUtils.getBooleanProperty("kafka.eagle.metrics.charts")) {
			MetricsServiceImpl metrics = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
			int retain = SystemConfigUtils.getIntProperty("kafka.eagle.metrics.retain");
			metrics.remove(Integer.valueOf(CalendarUtils.getCustomLastDay(retain == 0 ? 30 : retain)));
			metrics.cleanTopicLogSize(Integer.valueOf(CalendarUtils.getCustomLastDay(retain == 0 ? 30 : retain)));
			metrics.cleanBScreenConsumerTopic(Integer.valueOf(CalendarUtils.getCustomLastDay(retain == 0 ? 30 : retain)));
			metrics.cleanTopicSqlHistory(Integer.valueOf(CalendarUtils.getCustomLastDay(retain == 0 ? 30 : retain)));
		}
	}

	public void mbeanQuartz() {
		if (SystemConfigUtils.getBooleanProperty("kafka.eagle.metrics.charts")) {
			String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
			for (String clusterAlias : clusterAliass) {
				try {
					kafkaCluster(clusterAlias);
				} catch (Exception e) {
					ThrowExceptionUtils.print(this.getClass()).error("Get kafka cluster metrics has error, msg is ", e);
				}
				try {
					zkCluster(clusterAlias);
				} catch (Exception e) {
					ThrowExceptionUtils.print(this.getClass()).error("Get zookeeper cluster metrics has error, msg is ", e);
				}
				try {
					brokerMbeanOffline(clusterAlias);
				} catch (Exception e) {
					ThrowExceptionUtils.print(this.getClass()).error("Get broker mbean metrics has error, msg is ", e);
				}
			}
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
				kafkaMBeanOfflineAssembly(mx4jService, kpi, mbeanOffline, kafka);
			}
			list.add(mbeanOffline);
		}
		MetricsServiceImpl metrics = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
		try {
			metrics.mbeanOfflineInsert(list);
		} catch (Exception e) {
			ThrowExceptionUtils.print(this.getClass()).error("Collector mbean offline data has error, msg is ", e);
		}
	}

	private void kafkaMBeanOfflineAssembly(Mx4jService mx4jService, String type, MBeanOfflineInfo mbeanOffline, BrokersInfo kafka) {
		String uri = kafka.getHost() + ":" + kafka.getJmxPort();
		switch (type) {
		case MBean.MESSAGEIN:
			MBeanInfo msg = mx4jService.messagesInPerSec(uri);
			if (msg != null) {
				mbeanOffline.setOneMinute(StrUtils.assembly(msg.getOneMinute() == null ? "0.00" : msg.getOneMinute()));
				mbeanOffline.setMeanRate(StrUtils.assembly(msg.getMeanRate() == null ? "0.00" : msg.getMeanRate()));
				mbeanOffline.setFiveMinute(StrUtils.assembly(msg.getFiveMinute() == null ? "0.00" : msg.getFiveMinute()));
				mbeanOffline.setFifteenMinute(StrUtils.assembly(msg.getFifteenMinute() == null ? "0.00" : msg.getFifteenMinute()));
			}
			break;
		case MBean.BYTEIN:
			MBeanInfo bin = mx4jService.bytesInPerSec(uri);
			if (bin != null) {
				mbeanOffline.setOneMinute(StrUtils.assembly(bin.getOneMinute() == null ? "0.00" : bin.getOneMinute()));
				mbeanOffline.setMeanRate(StrUtils.assembly(bin.getMeanRate() == null ? "0.00" : bin.getMeanRate()));
				mbeanOffline.setFiveMinute(StrUtils.assembly(bin.getFiveMinute() == null ? "0.00" : bin.getFiveMinute()));
				mbeanOffline.setFifteenMinute(StrUtils.assembly(bin.getFifteenMinute() == null ? "0.00" : bin.getFifteenMinute()));
			}
			break;
		case MBean.BYTEOUT:
			MBeanInfo bout = mx4jService.bytesOutPerSec(uri);
			if (bout != null) {
				mbeanOffline.setOneMinute(StrUtils.assembly(bout.getOneMinute() == null ? "0.00" : bout.getOneMinute()));
				mbeanOffline.setMeanRate(StrUtils.assembly(bout.getMeanRate() == null ? "0.00" : bout.getMeanRate()));
				mbeanOffline.setFiveMinute(StrUtils.assembly(bout.getFiveMinute() == null ? "0.00" : bout.getFiveMinute()));
				mbeanOffline.setFifteenMinute(StrUtils.assembly(bout.getFifteenMinute() == null ? "0.00" : bout.getFifteenMinute()));
			}
			break;
		case MBean.BYTESREJECTED:
			MBeanInfo bytesRejectedPerSec = mx4jService.bytesRejectedPerSec(uri);
			if (bytesRejectedPerSec != null) {
				mbeanOffline.setOneMinute(StrUtils.assembly(bytesRejectedPerSec.getOneMinute() == null ? "0.00" : bytesRejectedPerSec.getOneMinute()));
				mbeanOffline.setMeanRate(StrUtils.assembly(bytesRejectedPerSec.getMeanRate() == null ? "0.00" : bytesRejectedPerSec.getMeanRate()));
				mbeanOffline.setFiveMinute(StrUtils.assembly(bytesRejectedPerSec.getFiveMinute() == null ? "0.00" : bytesRejectedPerSec.getFiveMinute()));
				mbeanOffline.setFifteenMinute(StrUtils.assembly(bytesRejectedPerSec.getFifteenMinute() == null ? "0.00" : bytesRejectedPerSec.getFifteenMinute()));
			}
			break;
		case MBean.FAILEDFETCHREQUEST:
			MBeanInfo failedFetchRequestsPerSec = mx4jService.failedFetchRequestsPerSec(uri);
			if (failedFetchRequestsPerSec != null) {
				mbeanOffline.setOneMinute(StrUtils.assembly(failedFetchRequestsPerSec.getOneMinute() == null ? "0.00" : failedFetchRequestsPerSec.getOneMinute()));
				mbeanOffline.setMeanRate(StrUtils.assembly(failedFetchRequestsPerSec.getMeanRate() == null ? "0.00" : failedFetchRequestsPerSec.getMeanRate()));
				mbeanOffline.setFiveMinute(StrUtils.assembly(failedFetchRequestsPerSec.getFiveMinute() == null ? "0.00" : failedFetchRequestsPerSec.getFiveMinute()));
				mbeanOffline.setFifteenMinute(StrUtils.assembly(failedFetchRequestsPerSec.getFifteenMinute() == null ? "0.00" : failedFetchRequestsPerSec.getFifteenMinute()));
			}
			break;
		case MBean.FAILEDPRODUCEREQUEST:
			MBeanInfo failedProduceRequestsPerSec = mx4jService.failedProduceRequestsPerSec(uri);
			if (failedProduceRequestsPerSec != null) {
				mbeanOffline.setOneMinute(StrUtils.assembly(failedProduceRequestsPerSec.getOneMinute() == null ? "0.00" : failedProduceRequestsPerSec.getOneMinute()));
				mbeanOffline.setMeanRate(StrUtils.assembly(failedProduceRequestsPerSec.getMeanRate() == null ? "0.00" : failedProduceRequestsPerSec.getMeanRate()));
				mbeanOffline.setFiveMinute(StrUtils.assembly(failedProduceRequestsPerSec.getFiveMinute() == null ? "0.00" : failedProduceRequestsPerSec.getFiveMinute()));
				mbeanOffline.setFifteenMinute(StrUtils.assembly(failedProduceRequestsPerSec.getFifteenMinute() == null ? "0.00" : failedProduceRequestsPerSec.getFifteenMinute()));
			}
			break;
		case MBean.TOTALFETCHREQUESTSPERSEC:
			MBeanInfo totalFetchRequests = mx4jService.totalFetchRequestsPerSec(uri);
			if (totalFetchRequests != null) {
				mbeanOffline.setOneMinute(StrUtils.assembly(totalFetchRequests.getOneMinute() == null ? "0.00" : totalFetchRequests.getOneMinute()));
				mbeanOffline.setMeanRate(StrUtils.assembly(totalFetchRequests.getMeanRate() == null ? "0.00" : totalFetchRequests.getMeanRate()));
				mbeanOffline.setFiveMinute(StrUtils.assembly(totalFetchRequests.getFiveMinute() == null ? "0.00" : totalFetchRequests.getFiveMinute()));
				mbeanOffline.setFifteenMinute(StrUtils.assembly(totalFetchRequests.getFifteenMinute() == null ? "0.00" : totalFetchRequests.getFifteenMinute()));
			}
			break;
		case MBean.TOTALPRODUCEREQUESTSPERSEC:
			MBeanInfo totalProduceRequestsPerSec = mx4jService.totalProduceRequestsPerSec(uri);
			if (totalProduceRequestsPerSec != null) {
				mbeanOffline.setOneMinute(StrUtils.assembly(totalProduceRequestsPerSec.getOneMinute() == null ? "0.00" : totalProduceRequestsPerSec.getOneMinute()));
				mbeanOffline.setMeanRate(StrUtils.assembly(totalProduceRequestsPerSec.getMeanRate() == null ? "0.00" : totalProduceRequestsPerSec.getMeanRate()));
				mbeanOffline.setFiveMinute(StrUtils.assembly(totalProduceRequestsPerSec.getFiveMinute() == null ? "0.00" : totalProduceRequestsPerSec.getFiveMinute()));
				mbeanOffline.setFifteenMinute(StrUtils.assembly(totalProduceRequestsPerSec.getFifteenMinute() == null ? "0.00" : totalProduceRequestsPerSec.getFifteenMinute()));
			}
			break;
		case MBean.REPLICATIONBYTESINPERSEC:
			MBeanInfo replicationBytesInPerSec = mx4jService.replicationBytesInPerSec(uri);
			if (replicationBytesInPerSec != null) {
				mbeanOffline.setOneMinute(StrUtils.assembly(replicationBytesInPerSec.getOneMinute() == null ? "0.00" : replicationBytesInPerSec.getOneMinute()));
				mbeanOffline.setMeanRate(StrUtils.assembly(replicationBytesInPerSec.getMeanRate() == null ? "0.00" : replicationBytesInPerSec.getMeanRate()));
				mbeanOffline.setFiveMinute(StrUtils.assembly(replicationBytesInPerSec.getFiveMinute() == null ? "0.00" : replicationBytesInPerSec.getFiveMinute()));
				mbeanOffline.setFifteenMinute(StrUtils.assembly(replicationBytesInPerSec.getFifteenMinute() == null ? "0.00" : replicationBytesInPerSec.getFifteenMinute()));
			}
			break;
		case MBean.REPLICATIONBYTESOUTPERSEC:
			MBeanInfo replicationBytesOutPerSec = mx4jService.replicationBytesOutPerSec(uri);
			if (replicationBytesOutPerSec != null) {
				mbeanOffline.setOneMinute(StrUtils.assembly(replicationBytesOutPerSec.getOneMinute() == null ? "0.00" : replicationBytesOutPerSec.getOneMinute()));
				mbeanOffline.setMeanRate(StrUtils.assembly(replicationBytesOutPerSec.getMeanRate() == null ? "0.00" : replicationBytesOutPerSec.getMeanRate()));
				mbeanOffline.setFiveMinute(StrUtils.assembly(replicationBytesOutPerSec.getFiveMinute() == null ? "0.00" : replicationBytesOutPerSec.getFiveMinute()));
				mbeanOffline.setFifteenMinute(StrUtils.assembly(replicationBytesOutPerSec.getFifteenMinute() == null ? "0.00" : replicationBytesOutPerSec.getFifteenMinute()));
			}
			break;
		case MBean.PRODUCEMESSAGECONVERSIONS:
			MBeanInfo produceMessageConv = mx4jService.produceMessageConversionsPerSec(uri);
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

	private void kafkaCluster(String clusterAlias) {
		List<BrokersInfo> brokers = kafkaService.getAllBrokersInfo(clusterAlias);
		List<KpiInfo> list = new ArrayList<>();

		for (String kpi : broker_kpis) {
			KpiInfo kpiInfo = new KpiInfo();
			kpiInfo.setCluster(clusterAlias);
			kpiInfo.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
			kpiInfo.setTimespan(CalendarUtils.getTimeSpan());
			kpiInfo.setKey(kpi);
			for (BrokersInfo kafka : brokers) {
				kafkaAssembly(mx4jService, kpi, kpiInfo, kafka);
			}
			kpiInfo.setBroker(clusterAlias);
			kpiInfo.setType(CollectorType.KAFKA);
			list.add(kpiInfo);
		}

		MetricsServiceImpl metrics = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
		try {
			metrics.insert(list);
		} catch (Exception e) {
			ThrowExceptionUtils.print(this.getClass()).error("Collector mbean data has error, msg is ", e);
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
			long totalMemory = kafkaService.getOSMemory(kafka.getHost(), kafka.getJmxPort(), BrokerServer.TOTAL_PHYSICAL_MEMORY_SIZE.getValue());
			kpiInfo.setValue(Long.parseLong(kpiInfo.getValue() == null ? "0" : kpiInfo.getValue()) + totalMemory + "");
			break;
		case MBean.OSFREEMEMORY:
			long freeMemory = kafkaService.getOSMemory(kafka.getHost(), kafka.getJmxPort(), BrokerServer.FREE_PHYSICAL_MEMORY_SIZE.getValue());
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
					ZkClusterInfo zkInfo = ZKMetricsUtils.zkClusterMntrInfo(ip, Integer.parseInt(port));
					zkAssembly(zkInfo, kpi, kpiInfo);
				} catch (Exception ex) {
					ThrowExceptionUtils.print(this.getClass()).error("Transcation string[" + port + "] to int has error, msg is ", ex);
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
			ThrowExceptionUtils.print(this.getClass()).error("Collector zookeeper data has error, msg is ", e);
		}
	}

	private static void zkAssembly(ZkClusterInfo zkInfo, String type, KpiInfo kpiInfo) {
		switch (type) {
		case zk_packets_received:
			kpiInfo.setValue(Long.parseLong(StrUtils.isNull(kpiInfo.getValue()) == true ? "0" : kpiInfo.getValue()) + Long.parseLong(StrUtils.isNull(zkInfo.getZkPacketsReceived()) == true ? "0" : zkInfo.getZkPacketsReceived()) + "");
			break;
		case zk_packets_sent:
			kpiInfo.setValue(Long.parseLong(StrUtils.isNull(kpiInfo.getValue()) == true ? "0" : kpiInfo.getValue()) + Long.parseLong(StrUtils.isNull(zkInfo.getZkPacketsSent()) == true ? "0" : zkInfo.getZkPacketsSent()) + "");
			break;
		case zk_num_alive_connections:
			kpiInfo.setValue(Long.parseLong(StrUtils.isNull(kpiInfo.getValue()) == true ? "0" : kpiInfo.getValue()) + Long.parseLong(StrUtils.isNull(zkInfo.getZkNumAliveConnections()) == true ? "0" : zkInfo.getZkNumAliveConnections()) + "");
			break;
		case zk_outstanding_requests:
			kpiInfo.setValue(Long.parseLong(StrUtils.isNull(kpiInfo.getValue()) == true ? "0" : kpiInfo.getValue()) + Long.parseLong(StrUtils.isNull(zkInfo.getZkOutstandingRequests()) == true ? "0" : zkInfo.getZkOutstandingRequests()) + "");
			break;
		default:
			break;
		}
	}

}
