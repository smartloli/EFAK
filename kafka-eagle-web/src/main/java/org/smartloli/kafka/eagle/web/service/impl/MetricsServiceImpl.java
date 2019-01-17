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
package org.smartloli.kafka.eagle.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import org.smartloli.kafka.eagle.common.protocol.KpiInfo;
import org.smartloli.kafka.eagle.common.protocol.MBeanInfo;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicLagInfo;
import org.smartloli.kafka.eagle.common.util.KConstants.MBean;
import org.smartloli.kafka.eagle.common.util.KConstants.ZK;
import org.smartloli.kafka.eagle.common.util.StrUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.Mx4jFactory;
import org.smartloli.kafka.eagle.core.factory.Mx4jService;
import org.smartloli.kafka.eagle.web.dao.MBeanDao;
import org.smartloli.kafka.eagle.web.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Achieve access to the kafka monitoring data interface through jmx.
 * 
 * @author smartloli.
 *
 *         Created by Jul 17, 2017 Update by No 3, 2018 by cocodroid
 */
@Service
public class MetricsServiceImpl implements MetricsService {

	@Autowired
	private MBeanDao mbeanDao;

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	/** Mx4j service interface. */
	private Mx4jService mx4jService = new Mx4jFactory().create();

	/** Gets summary monitoring data for all broker. */
	public String getAllBrokersMBean(String clusterAlias) {
		JSONArray brokers = JSON.parseArray(kafkaService.getAllBrokersInfo(clusterAlias));
		Map<String, MBeanInfo> mbeans = new HashMap<>();
		for (Object object : brokers) {
			JSONObject broker = (JSONObject) object;
			String uri = broker.getString("host") + ":" + broker.getInteger("jmxPort");
			MBeanInfo bytesIn = mx4jService.bytesInPerSec(uri);
			MBeanInfo bytesOut = mx4jService.bytesOutPerSec(uri);
			MBeanInfo bytesRejected = mx4jService.bytesRejectedPerSec(uri);
			MBeanInfo failedFetchRequest = mx4jService.failedFetchRequestsPerSec(uri);
			MBeanInfo failedProduceRequest = mx4jService.failedProduceRequestsPerSec(uri);
			MBeanInfo messageIn = mx4jService.messagesInPerSec(uri);
			MBeanInfo produceMessageConversions = mx4jService.produceMessageConversionsPerSec(uri);
			MBeanInfo totalFetchRequests = mx4jService.totalFetchRequestsPerSec(uri);
			MBeanInfo totalProduceRequests = mx4jService.totalProduceRequestsPerSec(uri);
			MBeanInfo replicationBytesInPerSec = mx4jService.replicationBytesInPerSec(uri);
			MBeanInfo replicationBytesOutPerSec = mx4jService.replicationBytesOutPerSec(uri);

			assembleMBeanInfo(mbeans, MBean.MESSAGES_IN, messageIn);

			assembleMBeanInfo(mbeans, MBean.BYTES_IN, bytesIn);

			assembleMBeanInfo(mbeans, MBean.BYTES_OUT, bytesOut);

			assembleMBeanInfo(mbeans, MBean.BYTES_REJECTED, bytesRejected);

			assembleMBeanInfo(mbeans, MBean.FAILED_FETCH_REQUEST, failedFetchRequest);

			assembleMBeanInfo(mbeans, MBean.FAILED_PRODUCE_REQUEST, failedProduceRequest);

			assembleMBeanInfo(mbeans, MBean.PRODUCEMESSAGECONVERSIONS, produceMessageConversions);

			assembleMBeanInfo(mbeans, MBean.TOTALFETCHREQUESTSPERSEC, totalFetchRequests);

			assembleMBeanInfo(mbeans, MBean.TOTALPRODUCEREQUESTSPERSEC, totalProduceRequests);

			assembleMBeanInfo(mbeans, MBean.REPLICATIONBYTESINPERSEC, replicationBytesInPerSec);

			assembleMBeanInfo(mbeans, MBean.REPLICATIONBYTESOUTPERSEC, replicationBytesOutPerSec);

		}
		for (Entry<String, MBeanInfo> entry : mbeans.entrySet()) {
			if (entry == null || entry.getValue() == null) {
				continue;
			}
			entry.getValue().setFifteenMinute(StrUtils.assembly(entry.getValue().getFifteenMinute()));
			entry.getValue().setFiveMinute(StrUtils.assembly(entry.getValue().getFiveMinute()));
			entry.getValue().setMeanRate(StrUtils.assembly(entry.getValue().getMeanRate()));
			entry.getValue().setOneMinute(StrUtils.assembly(entry.getValue().getOneMinute()));
		}
		return new Gson().toJson(mbeans);
	}

	private void assembleMBeanInfo(Map<String, MBeanInfo> mbeans, String mBeanInfoKey, MBeanInfo mBeanInfo) {
		if (mbeans.containsKey(mBeanInfoKey) && mBeanInfo != null) {
			MBeanInfo mbeanInfo = mbeans.get(mBeanInfoKey);
			long fifteenMinute = Math.round(StrUtils.numberic(mbeanInfo.getFifteenMinute())) + Math.round(StrUtils.numberic(mBeanInfo.getFifteenMinute()));
			long fiveMinute = Math.round(StrUtils.numberic(mbeanInfo.getFiveMinute())) + Math.round(StrUtils.numberic(mBeanInfo.getFiveMinute()));
			long meanRate = Math.round(StrUtils.numberic(mbeanInfo.getMeanRate())) + Math.round(StrUtils.numberic(mBeanInfo.getMeanRate()));
			long oneMinute = Math.round(StrUtils.numberic(mbeanInfo.getOneMinute())) + Math.round(StrUtils.numberic(mBeanInfo.getOneMinute()));
			mbeanInfo.setFifteenMinute(String.valueOf(fifteenMinute));
			mbeanInfo.setFiveMinute(String.valueOf(fiveMinute));
			mbeanInfo.setMeanRate(String.valueOf(meanRate));
			mbeanInfo.setOneMinute(String.valueOf(oneMinute));
		} else {
			mbeans.put(mBeanInfoKey, mBeanInfo);
		}
	}

	/** Collection statistics data from kafka jmx & insert into table. */
	public int insert(List<KpiInfo> kpi) {
		return mbeanDao.insert(kpi);
	}

	/** Query MBean data in different dimensions. */
	public String query(Map<String, Object> params) throws ParseException {

		List<KpiInfo> kpis = mbeanDao.query(params);

		JSONArray messageIns = new JSONArray();
		JSONArray byteIns = new JSONArray();
		JSONArray byteOuts = new JSONArray();
		JSONArray byteRejected = new JSONArray();
		JSONArray failedFetchRequest = new JSONArray();
		JSONArray failedProduceRequest = new JSONArray();
		JSONArray produceMessageConversions = new JSONArray();
		JSONArray totalFetchRequests = new JSONArray();
		JSONArray totalProduceRequests = new JSONArray();
		JSONArray replicationBytesOuts = new JSONArray();
		JSONArray replicationBytesIns = new JSONArray();

		JSONArray zkSendPackets = new JSONArray();
		JSONArray zkReceivedPackets = new JSONArray();
		JSONArray zkNumAliveConnections = new JSONArray();
		JSONArray zkOutstandingRequests = new JSONArray();
		String zks = "";
		for (KpiInfo kpi : kpis) {
			if ("".equals(zks) || zks == null) {
				zks = kpi.getBroker();
			}
			switch (kpi.getKey()) {
			case ZK.ZK_SEND_PACKETS:
				assembly(zkSendPackets, kpi);
				break;
			case ZK.ZK_RECEIVEDPACKETS:
				assembly(zkReceivedPackets, kpi);
				break;
			case ZK.ZK_OUTSTANDING_REQUESTS:
				assembly(zkOutstandingRequests, kpi);
				break;
			case ZK.ZK_NUM_ALIVECONNRCTIONS:
				assembly(zkNumAliveConnections, kpi);
				break;
			case MBean.MESSAGEIN:
				assembly(messageIns, kpi);
				break;
			case MBean.BYTEIN:
				assembly(byteIns, kpi);
				break;
			case MBean.BYTEOUT:
				assembly(byteOuts, kpi);
				break;
			case MBean.BYTESREJECTED:
				assembly(byteRejected, kpi);
				break;
			case MBean.FAILEDFETCHREQUEST:
				assembly(failedFetchRequest, kpi);
				break;
			case MBean.FAILEDPRODUCEREQUEST:
				assembly(failedProduceRequest, kpi);
				break;
			case MBean.PRODUCEMESSAGECONVERSIONS:
				assembly(produceMessageConversions, kpi);
				break;
			case MBean.TOTALFETCHREQUESTSPERSEC:
				assembly(totalFetchRequests, kpi);
				break;
			case MBean.TOTALPRODUCEREQUESTSPERSEC:
				assembly(totalProduceRequests, kpi);
				break;
			case MBean.REPLICATIONBYTESINPERSEC:
				assembly(replicationBytesOuts, kpi);
				break;
			case MBean.REPLICATIONBYTESOUTPERSEC:
				assembly(replicationBytesIns, kpi);
				break;
			default:
				break;
			}
		}
		JSONObject target = new JSONObject();
		target.put("send", zkSendPackets);
		target.put("received", zkReceivedPackets);
		target.put("queue", zkOutstandingRequests);
		target.put("alive", zkNumAliveConnections);
		target.put("msg", messageIns);
		target.put("ins", byteIns);
		target.put("outs", byteOuts);
		target.put("byteRejected", byteRejected);
		target.put("failedFetchRequest", failedFetchRequest);
		target.put("failedProduceRequest", failedProduceRequest);
		target.put("produceMessageConversions", produceMessageConversions);
		target.put("totalFetchRequests", totalFetchRequests);
		target.put("totalProduceRequests", totalProduceRequests);
		target.put("replicationBytesIns", replicationBytesIns);
		target.put("replicationBytesOuts", replicationBytesOuts);

		target.put("zks", zks.split(","));

		return target.toJSONString();
	}

	private void assembly(JSONArray assemblys, KpiInfo kpi) throws ParseException {
		JSONObject object = new JSONObject();
		object.put(kpi.getKey(), kpi.getValue());
		assemblys.add(object);
	}

	/** Crontab clean data. */
	public void remove(int tm) {
		mbeanDao.remove(tm);
	}

	@Override
	public int setConsumerLag(List<TopicLagInfo> topicLag) {
		return mbeanDao.setConsumerLag(topicLag);
	}

	@Override
	public List<TopicLagInfo> getConsumerLag(Map<String, Object> params) {
		return mbeanDao.getConsumerLag(params);
	}

	@Override
	public void cleanLagData(int tm) {
		mbeanDao.cleanLagData(tm);
	}

}
