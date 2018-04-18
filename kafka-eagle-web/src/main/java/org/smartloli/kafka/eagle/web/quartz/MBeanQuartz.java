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
import org.smartloli.kafka.eagle.common.protocol.KpiInfo;
import org.smartloli.kafka.eagle.common.protocol.MBeanInfo;
import org.smartloli.kafka.eagle.common.protocol.ZkClusterInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.common.util.ZKMetricsUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.Mx4jFactory;
import org.smartloli.kafka.eagle.core.factory.Mx4jService;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.MetricsServiceImpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Per 5 mins to stats mbean from kafka jmx.
 * 
 * @author smartloli.
 *
 *         Created by Jul 19, 2017
 */
public class MBeanQuartz {

	private Logger LOG = LoggerFactory.getLogger(MBeanQuartz.class);

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	/** Mx4j service interface. */
	private Mx4jService mx4jService = new Mx4jFactory().create();

	public void clean() {
		MetricsServiceImpl metrics = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
		int retain = SystemConfigUtils.getIntProperty("kafka.eagle.metrics.retain");
		metrics.remove(Integer.valueOf(CalendarUtils.getCustomLastDay(retain == 0 ? 7 : retain)));
	}

	public void mbeanQuartz() {
		String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
		for (String clusterAlias : clusterAliass) {
			mbean(clusterAlias);
			//zookeeper(clusterAlias);
			zkCluster(clusterAlias);
		}
	}
	
	private void broker(){
		
	}

//	private void zookeeper(String clusterAlias) {
//		List<KpiInfo> list = new ArrayList<>();
//		String zkList = SystemConfigUtils.getProperty(clusterAlias + ".zk.list");
//		String[] zks = zkList.split(",");
//		for (String zk : zks) {
//			String ip = zk.split(":")[0];
//			String port = zk.split(":")[1];
//			if (port.contains("/")) {
//				port = port.split("/")[0];
//			}
//			try {
//				ZkClusterInfo zkInfo = ZKMetricsUtils.zkClusterInfo(ip, Integer.parseInt(port));
//				KpiInfo kpiSend = new KpiInfo();
//				kpiSend.setCluster(clusterAlias);
//				kpiSend.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
//				kpiSend.setHour(CalendarUtils.getCustomDate("HH"));
//				kpiSend.setKey("ZKSendPackets");
//				kpiSend.setValue(zkInfo.getZkPacketsSent());
//				kpiSend.setBroker(ip);
//				list.add(kpiSend);
//
//				KpiInfo kpiReceived = new KpiInfo();
//				kpiReceived.setCluster(clusterAlias);
//				kpiReceived.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
//				kpiReceived.setHour(CalendarUtils.getCustomDate("HH"));
//				kpiReceived.setKey("ZKReceivedPackets");
//				kpiReceived.setValue(zkInfo.getZkPacketsReceived());
//				kpiReceived.setBroker(ip);
//				list.add(kpiReceived);
//
//				KpiInfo kpiAvgLatency = new KpiInfo();
//				kpiAvgLatency.setCluster(clusterAlias);
//				kpiAvgLatency.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
//				kpiAvgLatency.setHour(CalendarUtils.getCustomDate("HH"));
//				kpiAvgLatency.setKey("ZKAvgLatency");
//				kpiAvgLatency.setValue(zkInfo.getZkAvgLatency());
//				kpiAvgLatency.setBroker(ip);
//				list.add(kpiAvgLatency);
//
//				KpiInfo kpiNumAliveConnections = new KpiInfo();
//				kpiNumAliveConnections.setCluster(clusterAlias);
//				kpiNumAliveConnections.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
//				kpiNumAliveConnections.setHour(CalendarUtils.getCustomDate("HH"));
//				kpiNumAliveConnections.setKey("ZKNumAliveConnections");
//				kpiNumAliveConnections.setValue(zkInfo.getZkNumAliveConnections());
//				kpiNumAliveConnections.setBroker(ip);
//				list.add(kpiNumAliveConnections);
//
//				KpiInfo kpiOutstandingRequests = new KpiInfo();
//				kpiOutstandingRequests.setCluster(clusterAlias);
//				kpiOutstandingRequests.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
//				kpiOutstandingRequests.setHour(CalendarUtils.getCustomDate("HH"));
//				kpiOutstandingRequests.setKey("ZKOutstandingRequests");
//				kpiOutstandingRequests.setValue(zkInfo.getZkOutstandingRequests());
//				kpiOutstandingRequests.setBroker(ip);
//				list.add(kpiOutstandingRequests);
//
//				KpiInfo kpiOpenFileDescriptorCount = new KpiInfo();
//				kpiOpenFileDescriptorCount.setCluster(clusterAlias);
//				kpiOpenFileDescriptorCount.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
//				kpiOpenFileDescriptorCount.setHour(CalendarUtils.getCustomDate("HH"));
//				kpiOpenFileDescriptorCount.setKey("ZKOpenFileDescriptorCount");
//				kpiOpenFileDescriptorCount.setValue(zkInfo.getZkOpenFileDescriptorCount());
//				kpiOpenFileDescriptorCount.setBroker(ip);
//				list.add(kpiOpenFileDescriptorCount);
//
//			} catch (Exception ex) {
//				LOG.error("Transcation string to int has error,msg is " + ex.getMessage());
//			}
//		}
//
//		MetricsServiceImpl metrics = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
//		try {
//			metrics.insert(list);
//		} catch (Exception e) {
//			LOG.error("Collector zookeeper data has error,msg is " + e.getMessage());
//		}
//	}

	private void zkCluster(String clusterAlias) {
		List<KpiInfo> list = new ArrayList<>();
		String zkList = SystemConfigUtils.getProperty(clusterAlias + ".zk.list");
		String[] zks = zkList.split(",");
		for (String zk : zks) {
			String ip = zk.split(":")[0];
			String port = zk.split(":")[1];
			if (port.contains("/")) {
				port = port.split("/")[0];
			}
			try {
				ZkClusterInfo zkInfo = ZKMetricsUtils.zkClusterInfo(ip, Integer.parseInt(port));
				KpiInfo kpiSend = new KpiInfo();
				kpiSend.setCluster(clusterAlias);
				kpiSend.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
				kpiSend.setTimespan(CalendarUtils.getTimeSpan());
				kpiSend.setKey("ZKSendPackets");
				kpiSend.setValue(zkInfo.getZkPacketsSent());
				kpiSend.setBroker(ip + ":" + port);
				list.add(kpiSend);

				KpiInfo kpiReceived = new KpiInfo();
				kpiReceived.setCluster(clusterAlias);
				kpiReceived.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
				kpiReceived.setTimespan(CalendarUtils.getTimeSpan());
				kpiReceived.setKey("ZKReceivedPackets");
				kpiReceived.setValue(zkInfo.getZkPacketsReceived());
				kpiReceived.setBroker(ip + ":" + port);
				list.add(kpiReceived);

				KpiInfo kpiAvgLatency = new KpiInfo();
				kpiAvgLatency.setCluster(clusterAlias);
				kpiAvgLatency.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
				kpiAvgLatency.setTimespan(CalendarUtils.getTimeSpan());
				kpiAvgLatency.setKey("ZKAvgLatency");
				kpiAvgLatency.setValue(zkInfo.getZkAvgLatency());
				kpiAvgLatency.setBroker(ip + ":" + port);
				list.add(kpiAvgLatency);

				KpiInfo kpiNumAliveConnections = new KpiInfo();
				kpiNumAliveConnections.setCluster(clusterAlias);
				kpiNumAliveConnections.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
				kpiNumAliveConnections.setTimespan(CalendarUtils.getTimeSpan());
				kpiNumAliveConnections.setKey("ZKNumAliveConnections");
				kpiNumAliveConnections.setValue(zkInfo.getZkNumAliveConnections());
				kpiNumAliveConnections.setBroker(ip + ":" + port);
				list.add(kpiNumAliveConnections);

				KpiInfo kpiOutstandingRequests = new KpiInfo();
				kpiOutstandingRequests.setCluster(clusterAlias);
				kpiOutstandingRequests.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
				kpiOutstandingRequests.setTimespan(CalendarUtils.getTimeSpan());
				kpiOutstandingRequests.setKey("ZKOutstandingRequests");
				kpiOutstandingRequests.setValue(zkInfo.getZkOutstandingRequests());
				kpiOutstandingRequests.setBroker(ip + ":" + port);
				list.add(kpiOutstandingRequests);

				KpiInfo kpiOpenFileDescriptorCount = new KpiInfo();
				kpiOpenFileDescriptorCount.setCluster(clusterAlias);
				kpiOpenFileDescriptorCount.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
				kpiOpenFileDescriptorCount.setTimespan(CalendarUtils.getTimeSpan());
				kpiOpenFileDescriptorCount.setKey("ZKOpenFileDescriptorCount");
				kpiOpenFileDescriptorCount.setValue(zkInfo.getZkOpenFileDescriptorCount());
				kpiOpenFileDescriptorCount.setBroker(ip + ":" + port);
				list.add(kpiOpenFileDescriptorCount);

			} catch (Exception ex) {
				LOG.error("Transcation string to int has error,msg is " + ex.getMessage());
			}
		}

		MetricsServiceImpl metrics = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
		try {
			metrics.insert(list);
		} catch (Exception e) {
			LOG.error("Collector zookeeper data has error,msg is " + e.getMessage());
		}
	}

	private void mbean(String clusterAlias) {
		JSONArray brokers = JSON.parseArray(kafkaService.getAllBrokersInfo(clusterAlias));
		List<KpiInfo> list = new ArrayList<>();
		for (Object object : brokers) {
			JSONObject broker = (JSONObject) object;
			String uri = broker.getString("host") + ":" + broker.getInteger("jmxPort");
			MBeanInfo bytesIn = mx4jService.bytesInPerSec(uri);
			KpiInfo kpiByteIn = new KpiInfo();
			kpiByteIn.setCluster(clusterAlias);
			kpiByteIn.setKey("ByteIn");
			kpiByteIn.setValue(bytesIn.getMeanRate());
			kpiByteIn.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
			kpiByteIn.setTimespan(CalendarUtils.getTimeSpan());
			kpiByteIn.setBroker(broker.getString("host") + ":" + broker.getInteger("port"));
			list.add(kpiByteIn);

			MBeanInfo bytesOut = mx4jService.bytesOutPerSec(uri);
			KpiInfo kpiByteOut = new KpiInfo();
			kpiByteOut.setCluster(clusterAlias);
			kpiByteOut.setKey("ByteOut");
			kpiByteOut.setValue(bytesOut.getMeanRate());
			kpiByteOut.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
			kpiByteOut.setTimespan(CalendarUtils.getTimeSpan());
			kpiByteOut.setBroker(broker.getString("host") + ":" + broker.getInteger("port"));
			list.add(kpiByteOut);

			MBeanInfo failedFetchRequest = mx4jService.failedFetchRequestsPerSec(uri);
			KpiInfo kpiFailedFetchRequest = new KpiInfo();
			kpiFailedFetchRequest.setCluster(clusterAlias);
			kpiFailedFetchRequest.setKey("FailedFetchRequest");
			kpiFailedFetchRequest.setValue(failedFetchRequest.getMeanRate());
			kpiFailedFetchRequest.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
			kpiFailedFetchRequest.setTimespan(CalendarUtils.getTimeSpan());
			kpiFailedFetchRequest.setBroker(broker.getString("host") + ":" + broker.getInteger("port"));
			list.add(kpiFailedFetchRequest);

			MBeanInfo failedProduceRequest = mx4jService.failedProduceRequestsPerSec(uri);
			KpiInfo kpiFailedProduceRequest = new KpiInfo();
			kpiFailedProduceRequest.setCluster(clusterAlias);
			kpiFailedProduceRequest.setKey("FailedProduceRequest");
			kpiFailedProduceRequest.setValue(failedProduceRequest.getMeanRate());
			kpiFailedProduceRequest.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
			kpiFailedProduceRequest.setTimespan(CalendarUtils.getTimeSpan());
			kpiFailedProduceRequest.setBroker(broker.getString("host") + ":" + broker.getInteger("port"));
			list.add(kpiFailedProduceRequest);

			MBeanInfo messageIn = mx4jService.messagesInPerSec(uri);
			KpiInfo kpiMessageIn = new KpiInfo();
			kpiMessageIn.setCluster(clusterAlias);
			kpiMessageIn.setKey("MessageIn");
			kpiMessageIn.setValue(messageIn.getMeanRate());
			kpiMessageIn.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
			kpiMessageIn.setTimespan(CalendarUtils.getTimeSpan());
			kpiMessageIn.setBroker(broker.getString("host") + ":" + broker.getInteger("port"));
			list.add(kpiMessageIn);
		}

		MetricsServiceImpl metrics = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
		try {
			metrics.insert(list);
		} catch (Exception e) {
			LOG.error("Collector mbean data has error,msg is " + e.getMessage());
		}
	}

}
