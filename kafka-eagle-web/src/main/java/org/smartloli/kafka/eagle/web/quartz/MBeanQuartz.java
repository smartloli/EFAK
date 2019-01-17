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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.protocol.KpiInfo;
import org.smartloli.kafka.eagle.common.protocol.MBeanInfo;
import org.smartloli.kafka.eagle.common.protocol.ZkClusterInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.CollectorType;
import org.smartloli.kafka.eagle.common.util.KConstants.MBean;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.common.util.ZKMetricsUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.Mx4jFactory;
import org.smartloli.kafka.eagle.core.factory.Mx4jService;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.MetricsServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Per 5 mins to stats mbean from kafka jmx.
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

	private static final String[] broker_kpis = new String[] { MBean.MESSAGEIN, MBean.BYTEIN, MBean.BYTEOUT, MBean.BYTESREJECTED,
            MBean.FAILEDFETCHREQUEST, MBean.FAILEDPRODUCEREQUEST, MBean.TOTALFETCHREQUESTSPERSEC, MBean.TOTALPRODUCEREQUESTSPERSEC
            , MBean.REPLICATIONBYTESINPERSEC, MBean.REPLICATIONBYTESOUTPERSEC, MBean.PRODUCEMESSAGECONVERSIONS};

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	/** Mx4j service interface. */
	private Mx4jService mx4jService = new Mx4jFactory().create();

	public void clean() {
		if (SystemConfigUtils.getBooleanProperty("kafka.eagle.metrics.charts")) {
			MetricsServiceImpl metrics = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
			int retain = SystemConfigUtils.getIntProperty("kafka.eagle.metrics.retain");
			metrics.remove(Integer.valueOf(CalendarUtils.getCustomLastDay(retain == 0 ? 7 : retain)));
			metrics.cleanLagData(Integer.valueOf(CalendarUtils.getCustomLastDay(retain == 0 ? 7 : retain)));
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
		JSONArray brokers = JSON.parseArray(kafkaService.getAllBrokersInfo(clusterAlias));
		List<KpiInfo> list = new ArrayList<>();

		for (String kpi : broker_kpis) {
			KpiInfo kpiInfo = new KpiInfo();
			kpiInfo.setCluster(clusterAlias);
			kpiInfo.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
			kpiInfo.setTimespan(CalendarUtils.getTimeSpan());
			kpiInfo.setKey(kpi);
			String broker = "";
			JSONObject value = new JSONObject();
			value.put("y", CalendarUtils.getDate());
			for (Object object : brokers) {
				JSONObject kafka = (JSONObject) object;
				broker += kafka.getString("host") + ",";
				kafkaAssembly(mx4jService, kpi, value, kafka);
			}
			kpiInfo.setBroker(broker.length() == 0 ? "unkowns" : broker.substring(0, broker.length() - 1));
			kpiInfo.setType(CollectorType.KAFKA);
			kpiInfo.setValue(value.toJSONString());
			list.add(kpiInfo);
		}

		MetricsServiceImpl metrics = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
		try {
			metrics.insert(list);
		} catch (Exception e) {
			LOG.error("Collector mbean data has error,msg is " + e.getMessage());
		}
	}

	private void kafkaAssembly(Mx4jService mx4jService, String type, JSONObject value, JSONObject kafka) {
		String uri = kafka.getString("host") + ":" + kafka.getInteger("jmxPort");
		switch (type) {
		case MBean.MESSAGEIN:
			MBeanInfo msg = mx4jService.messagesInPerSec(uri);
            if (msg != null) {
                value.put(kafka.getString("host"), msg.getMeanRate());
            }
			break;
		case MBean.BYTEIN:
			MBeanInfo bin = mx4jService.bytesInPerSec(uri);
            if (bin != null) {
                value.put(kafka.getString("host"), bin.getMeanRate());
            }
			break;
		case MBean.BYTEOUT:
			MBeanInfo bout = mx4jService.bytesOutPerSec(uri);
            if (bout != null) {
                value.put(kafka.getString("host"), bout.getMeanRate());
            }
			break;
		case MBean.BYTESREJECTED:
			MBeanInfo bytesRejectedPerSec = mx4jService.bytesRejectedPerSec(uri);
            if (bytesRejectedPerSec != null) {
                value.put(kafka.getString("host"), bytesRejectedPerSec.getMeanRate());
            }
			break;
		case MBean.FAILEDFETCHREQUEST:
			MBeanInfo failedFetchRequestsPerSec = mx4jService.failedFetchRequestsPerSec(uri);
            if (failedFetchRequestsPerSec != null) {
                value.put(kafka.getString("host"), failedFetchRequestsPerSec.getMeanRate());
            }
			break;
		case MBean.FAILEDPRODUCEREQUEST:
			MBeanInfo failedProduceRequestsPerSec = mx4jService.failedProduceRequestsPerSec(uri);
            if (failedProduceRequestsPerSec != null) {
                value.put(kafka.getString("host"), failedProduceRequestsPerSec.getMeanRate());
            }
			break;
		case MBean.TOTALFETCHREQUESTSPERSEC:
            MBeanInfo totalFetchRequests= mx4jService.totalFetchRequestsPerSec(uri);
            if (totalFetchRequests != null) {
                value.put(kafka.getString("host"), totalFetchRequests.getMeanRate());
            }
            break;
		case MBean.TOTALPRODUCEREQUESTSPERSEC:
            MBeanInfo totalProduceRequestsPerSec= mx4jService.totalProduceRequestsPerSec(uri);
            if (totalProduceRequestsPerSec != null) {
                value.put(kafka.getString("host"), totalProduceRequestsPerSec.getMeanRate());
            }
            break;
		case MBean.REPLICATIONBYTESINPERSEC:
            MBeanInfo replicationBytesInPerSec= mx4jService.replicationBytesInPerSec(uri);
            if (replicationBytesInPerSec != null) {
                value.put(kafka.getString("host"), replicationBytesInPerSec.getMeanRate());
            }
            break;
		case MBean.REPLICATIONBYTESOUTPERSEC:
            MBeanInfo replicationBytesOutPerSec= mx4jService.replicationBytesOutPerSec(uri);
            if (replicationBytesOutPerSec != null) {
                value.put(kafka.getString("host"), replicationBytesOutPerSec.getMeanRate());
            }
            break;
        case MBean.PRODUCEMESSAGECONVERSIONS:
            MBeanInfo produceMessageConv= mx4jService.produceMessageConversionsPerSec(uri);
            if (produceMessageConv != null) {
                value.put(kafka.getString("host"), produceMessageConv.getMeanRate());
            }
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
			JSONObject object = new JSONObject();
			object.put("y", CalendarUtils.getDate());
			for (String zk : zks) {
				String ip = zk.split(":")[0];
				String port = zk.split(":")[1];
				if (port.contains("/")) {
					port = port.split("/")[0];
				}
				broker += ip + ",";
				try {
					ZkClusterInfo zkInfo = ZKMetricsUtils.zkClusterInfo(ip, Integer.parseInt(port));
					assembly(zkInfo, kpi, object, ip);

				} catch (Exception ex) {
					LOG.error("Transcation string to int has error,msg is " + ex.getMessage());
				}
			}
			kpiInfo.setBroker(broker.length() == 0 ? "unkowns" : broker.substring(0, broker.length() - 1));
			kpiInfo.setType(CollectorType.ZK);
			kpiInfo.setValue(object.toJSONString());
			list.add(kpiInfo);
		}

		MetricsServiceImpl metrics = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
		try {
			metrics.insert(list);
		} catch (Exception e) {
			LOG.error("Collector zookeeper data has error,msg is " + e.getMessage());
		}
	}

	private static void assembly(ZkClusterInfo zkInfo, String type, JSONObject object, String ip) {
		switch (type) {
		case zk_packets_received:
			object.put(ip, zkInfo.getZkPacketsReceived());
			break;
		case zk_packets_sent:
			object.put(ip, zkInfo.getZkPacketsSent());
			break;
		case zk_num_alive_connections:
			object.put(ip, zkInfo.getZkNumAliveConnections());
			break;
		case zk_outstanding_requests:
			object.put(ip, zkInfo.getZkOutstandingRequests());
			break;
		default:
			break;
		}
	}

	// private void mbean(String clusterAlias) {
	// JSONArray brokers =
	// JSON.parseArray(kafkaService.getAllBrokersInfo(clusterAlias));
	// List<KpiInfo> list = new ArrayList<>();
	// for (Object object : brokers) {
	// JSONObject broker = (JSONObject) object;
	// String uri = broker.getString("host") + ":" +
	// broker.getInteger("jmxPort");
	// JSONObject values = new JSONObject();
	//
	// MBeanInfo bytesIn = mx4jService.bytesInPerSec(uri);
	// KpiInfo kpiByteIn = new KpiInfo();
	// kpiByteIn.setCluster(clusterAlias);
	// kpiByteIn.setKey("ByteIn");
	// kpiByteIn.setValue(bytesIn.getMeanRate());
	// kpiByteIn.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
	// kpiByteIn.setTimespan(CalendarUtils.getTimeSpan());
	// kpiByteIn.setBroker(broker.getString("host") + ":" +
	// broker.getInteger("port"));
	// list.add(kpiByteIn);
	//
	// MBeanInfo bytesOut = mx4jService.bytesOutPerSec(uri);
	// KpiInfo kpiByteOut = new KpiInfo();
	// kpiByteOut.setCluster(clusterAlias);
	// kpiByteOut.setKey("ByteOut");
	// kpiByteOut.setValue(bytesOut.getMeanRate());
	// kpiByteOut.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
	// kpiByteOut.setTimespan(CalendarUtils.getTimeSpan());
	// kpiByteOut.setBroker(broker.getString("host") + ":" +
	// broker.getInteger("port"));
	// list.add(kpiByteOut);
	//
	// MBeanInfo failedFetchRequest =
	// mx4jService.failedFetchRequestsPerSec(uri);
	// KpiInfo kpiFailedFetchRequest = new KpiInfo();
	// kpiFailedFetchRequest.setCluster(clusterAlias);
	// kpiFailedFetchRequest.setKey("FailedFetchRequest");
	// kpiFailedFetchRequest.setValue(failedFetchRequest.getMeanRate());
	// kpiFailedFetchRequest.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
	// kpiFailedFetchRequest.setTimespan(CalendarUtils.getTimeSpan());
	// kpiFailedFetchRequest.setBroker(broker.getString("host") + ":" +
	// broker.getInteger("port"));
	// list.add(kpiFailedFetchRequest);
	//
	// MBeanInfo failedProduceRequest =
	// mx4jService.failedProduceRequestsPerSec(uri);
	// KpiInfo kpiFailedProduceRequest = new KpiInfo();
	// kpiFailedProduceRequest.setCluster(clusterAlias);
	// kpiFailedProduceRequest.setKey("FailedProduceRequest");
	// kpiFailedProduceRequest.setValue(failedProduceRequest.getMeanRate());
	// kpiFailedProduceRequest.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
	// kpiFailedProduceRequest.setTimespan(CalendarUtils.getTimeSpan());
	// kpiFailedProduceRequest.setBroker(broker.getString("host") + ":" +
	// broker.getInteger("port"));
	// list.add(kpiFailedProduceRequest);
	//
	// MBeanInfo messageIn = mx4jService.messagesInPerSec(uri);
	// KpiInfo kpiMessageIn = new KpiInfo();
	// kpiMessageIn.setCluster(clusterAlias);
	// kpiMessageIn.setKey("MessageIn");
	// kpiMessageIn.setValue(messageIn.getMeanRate());
	// kpiMessageIn.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
	// kpiMessageIn.setTimespan(CalendarUtils.getTimeSpan());
	// kpiMessageIn.setBroker(broker.getString("host") + ":" +
	// broker.getInteger("port"));
	// list.add(kpiMessageIn);
	// }
	//
	// MetricsServiceImpl metrics =
	// StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
	// try {
	// metrics.insert(list);
	// } catch (Exception e) {
	// LOG.error("Collector mbean data has error,msg is " + e.getMessage());
	// }
	// }

}
