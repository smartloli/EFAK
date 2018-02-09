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

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.smartloli.kafka.eagle.common.protocol.KpiInfo;
import org.smartloli.kafka.eagle.common.protocol.MBeanInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

/**
 * Achieve access to the kafka monitoring data interface through jmx.
 * 
 * @author smartloli.
 *
 *         Created by Jul 17, 2017
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

			if (mbeans.containsKey(MBean.MESSAGES_IN)) {
				MBeanInfo msgIn = mbeans.get(MBean.MESSAGES_IN);
				long fifteenMinute = Math.round(StrUtils.numberic(msgIn.getFifteenMinute())) + Math.round(StrUtils.numberic(messageIn.getFifteenMinute()));
				long fiveMinute = Math.round(StrUtils.numberic(msgIn.getFiveMinute())) + Math.round(StrUtils.numberic(messageIn.getFiveMinute()));
				long meanRate = Math.round(StrUtils.numberic(msgIn.getMeanRate())) + Math.round(StrUtils.numberic(messageIn.getMeanRate()));
				long oneMinute = Math.round(StrUtils.numberic(msgIn.getOneMinute())) + Math.round(StrUtils.numberic(messageIn.getOneMinute()));
				msgIn.setFifteenMinute(String.valueOf(fifteenMinute));
				msgIn.setFiveMinute(String.valueOf(fiveMinute));
				msgIn.setMeanRate(String.valueOf(meanRate));
				msgIn.setOneMinute(String.valueOf(oneMinute));
			} else {
				mbeans.put(MBean.MESSAGES_IN, messageIn);
			}

			if (mbeans.containsKey(MBean.BYTES_IN)) {
				MBeanInfo byteIn = mbeans.get(MBean.BYTES_IN);
				long fifteenMinute = Math.round(StrUtils.numberic(byteIn.getFifteenMinute())) + Math.round(StrUtils.numberic(bytesIn.getFifteenMinute()));
				long fiveMinute = Math.round(StrUtils.numberic(byteIn.getFiveMinute())) + Math.round(StrUtils.numberic(bytesIn.getFiveMinute()));
				long meanRate = Math.round(StrUtils.numberic(byteIn.getMeanRate())) + Math.round(StrUtils.numberic(bytesIn.getMeanRate()));
				long oneMinute = Math.round(StrUtils.numberic(byteIn.getOneMinute())) + Math.round(StrUtils.numberic(bytesIn.getOneMinute()));
				byteIn.setFifteenMinute(String.valueOf(fifteenMinute));
				byteIn.setFiveMinute(String.valueOf(fiveMinute));
				byteIn.setMeanRate(String.valueOf(meanRate));
				byteIn.setOneMinute(String.valueOf(oneMinute));
			} else {
				mbeans.put(MBean.BYTES_IN, bytesIn);
			}

			if (mbeans.containsKey(MBean.BYTES_OUT)) {
				MBeanInfo byteOut = mbeans.get(MBean.BYTES_OUT);
				long fifteenMinute = Math.round(StrUtils.numberic(byteOut.getFifteenMinute())) + Math.round(StrUtils.numberic(bytesOut.getFifteenMinute()));
				long fiveMinute = Math.round(StrUtils.numberic(byteOut.getFiveMinute())) + Math.round(StrUtils.numberic(bytesOut.getFiveMinute()));
				long meanRate = Math.round(StrUtils.numberic(byteOut.getMeanRate())) + Math.round(StrUtils.numberic(bytesOut.getMeanRate()));
				long oneMinute = Math.round(StrUtils.numberic(byteOut.getOneMinute())) + Math.round(StrUtils.numberic(bytesOut.getOneMinute()));
				byteOut.setFifteenMinute(String.valueOf(fifteenMinute));
				byteOut.setFiveMinute(String.valueOf(fiveMinute));
				byteOut.setMeanRate(String.valueOf(meanRate));
				byteOut.setOneMinute(String.valueOf(oneMinute));
			} else {
				mbeans.put(MBean.BYTES_OUT, bytesOut);
			}

			if (mbeans.containsKey(MBean.BYTES_REJECTED)) {
				MBeanInfo byteRejected = mbeans.get(MBean.BYTES_REJECTED);
				long fifteenMinute = Math.round(StrUtils.numberic(byteRejected.getFifteenMinute())) + Math.round(StrUtils.numberic(bytesRejected.getFifteenMinute()));
				long fiveMinute = Math.round(StrUtils.numberic(byteRejected.getFiveMinute())) + Math.round(StrUtils.numberic(bytesRejected.getFiveMinute()));
				long meanRate = Math.round(StrUtils.numberic(byteRejected.getMeanRate())) + Math.round(StrUtils.numberic(bytesRejected.getMeanRate()));
				long oneMinute = Math.round(StrUtils.numberic(byteRejected.getOneMinute())) + Math.round(StrUtils.numberic(bytesRejected.getOneMinute()));
				byteRejected.setFifteenMinute(String.valueOf(fifteenMinute));
				byteRejected.setFiveMinute(String.valueOf(fiveMinute));
				byteRejected.setMeanRate(String.valueOf(meanRate));
				byteRejected.setOneMinute(String.valueOf(oneMinute));
			} else {
				mbeans.put(MBean.BYTES_REJECTED, bytesRejected);
			}

			if (mbeans.containsKey(MBean.FAILED_FETCH_REQUEST)) {
				MBeanInfo failedFetch = mbeans.get(MBean.FAILED_FETCH_REQUEST);
				long fifteenMinute = Math.round(StrUtils.numberic(failedFetch.getFifteenMinute())) + Math.round(StrUtils.numberic(failedFetchRequest.getFifteenMinute()));
				long fiveMinute = Math.round(StrUtils.numberic(failedFetch.getFiveMinute())) + Math.round(StrUtils.numberic(failedFetchRequest.getFiveMinute()));
				long meanRate = Math.round(StrUtils.numberic(failedFetch.getMeanRate())) + Math.round(StrUtils.numberic(failedFetchRequest.getMeanRate()));
				long oneMinute = Math.round(StrUtils.numberic(failedFetch.getOneMinute())) + Math.round(StrUtils.numberic(failedFetchRequest.getOneMinute()));
				failedFetch.setFifteenMinute(String.valueOf(fifteenMinute));
				failedFetch.setFiveMinute(String.valueOf(fiveMinute));
				failedFetch.setMeanRate(String.valueOf(meanRate));
				failedFetch.setOneMinute(String.valueOf(oneMinute));
			} else {
				mbeans.put(MBean.FAILED_FETCH_REQUEST, failedFetchRequest);
			}

			if (mbeans.containsKey(MBean.FAILED_PRODUCE_REQUEST)) {
				MBeanInfo failedProduce = mbeans.get(MBean.FAILED_PRODUCE_REQUEST);
				long fifteenMinute = Math.round(StrUtils.numberic(failedProduce.getFifteenMinute())) + Math.round(StrUtils.numberic(failedProduceRequest.getFifteenMinute()));
				long fiveMinute = Math.round(StrUtils.numberic(failedProduce.getFiveMinute())) + Math.round(StrUtils.numberic(failedProduceRequest.getFiveMinute()));
				long meanRate = Math.round(StrUtils.numberic(failedProduce.getMeanRate())) + Math.round(StrUtils.numberic(failedProduceRequest.getMeanRate()));
				long oneMinute = Math.round(StrUtils.numberic(failedProduce.getOneMinute())) + Math.round(StrUtils.numberic(failedProduceRequest.getOneMinute()));
				failedProduce.setFifteenMinute(String.valueOf(fifteenMinute));
				failedProduce.setFiveMinute(String.valueOf(fiveMinute));
				failedProduce.setMeanRate(String.valueOf(meanRate));
				failedProduce.setOneMinute(String.valueOf(oneMinute));
			} else {
				mbeans.put(MBean.FAILED_PRODUCE_REQUEST, failedProduceRequest);
			}

		}
		for (Entry<String, MBeanInfo> entry : mbeans.entrySet()) {
			entry.getValue().setFifteenMinute(StrUtils.assembly(entry.getValue().getFifteenMinute()));
			entry.getValue().setFiveMinute(StrUtils.assembly(entry.getValue().getFiveMinute()));
			entry.getValue().setMeanRate(StrUtils.assembly(entry.getValue().getMeanRate()));
			entry.getValue().setOneMinute(StrUtils.assembly(entry.getValue().getOneMinute()));
		}
		return new Gson().toJson(mbeans);
	}

	/** Collection statistics data from kafka jmx & insert into table. */
	public int insert(List<KpiInfo> kpi) {
		return mbeanDao.insert(kpi);
	}

	/** Query MBean data in different dimensions. */
	public String query(Map<String, Object> params) throws ParseException {
		String type = params.get("type").toString();
		List<KpiInfo> kpis = null;
		if ("daily".equals(type)) {
			kpis = mbeanDao.daily(params);
		} else if ("day".equals(type)) {
			kpis = mbeanDao.day(params);
		}
		JSONArray messageIns = new JSONArray();
		JSONArray byteInOuts = new JSONArray();
		JSONArray failedReqstProds = new JSONArray();

		JSONArray zkSendPackets = new JSONArray();
		JSONArray zkReceivedPackets = new JSONArray();
		JSONArray zkAvgLatency = new JSONArray();
		JSONArray zkNumAliveConnections = new JSONArray();
		JSONArray zkOutstandingRequests = new JSONArray();
		JSONArray zkOpenFileDescriptorCount = new JSONArray();
		for (KpiInfo kpi : kpis) {
			if (MBean.MESSAGEIN.equals(kpi.getKey())) {
				assembly(type, messageIns, kpi);
			}

			if (MBean.BYTEIN.equals(kpi.getKey()) || MBean.BYTEOUT.equals(kpi.getKey())) {
				boolean flag = true;
				for (Object object : byteInOuts) {
					JSONObject byteInOut = (JSONObject) object;
					if ("daily".equals(type)) {
						if (byteInOut.getString("xkey").equals(CalendarUtils.convertDate2Date(kpi.getTm()) + " " + kpi.getHour() + ":00")) {
							flag = false;
							byteInOut.put(kpi.getKey(), StrUtils.numberic(kpi.getValue()));
							break;
						}
					} else if ("day".equals(type)) {
						if (byteInOut.getString("xkey").equals(CalendarUtils.convertDate2Date(kpi.getTm()))) {
							flag = false;
							byteInOut.put(kpi.getKey(), StrUtils.numberic(kpi.getValue()));
							break;
						}
					}

				}

				if (flag) {
					assembly(type, byteInOuts, kpi);
				}
			}

			if (MBean.FAILEDFETCHREQUEST.equals(kpi.getKey()) || MBean.FAILEDPRODUCEREQUEST.equals(kpi.getKey())) {
				boolean flag = true;
				for (Object object : failedReqstProds) {
					JSONObject failedReqstProd = (JSONObject) object;
					if ("daily".equals(type)) {
						if (failedReqstProd.getString("xkey").equals(CalendarUtils.convertDate2Date(kpi.getTm()) + " " + kpi.getHour() + ":00")) {
							flag = false;
							failedReqstProd.put(kpi.getKey(), StrUtils.numberic(kpi.getValue()));
							break;
						}
					} else if ("day".equals(type)) {
						if (failedReqstProd.getString("xkey").equals(CalendarUtils.convertDate2Date(kpi.getTm()))) {
							flag = false;
							failedReqstProd.put(kpi.getKey(), StrUtils.numberic(kpi.getValue()));
							break;
						}
					}
				}

				if (flag) {
					assembly(type, failedReqstProds, kpi);
				}
			}

			// if (ZK.ZK_SEND_PACKETS.equals(kpi.getKey())) {
			// assembly(type, zkSendPackets, kpi);
			// }

			switch (kpi.getKey()) {
			case ZK.ZK_SEND_PACKETS:
				assembly(type, zkSendPackets, kpi);
				break;
			case ZK.ZK_RECEIVEDPACKETS:
				assembly(type, zkReceivedPackets, kpi);
				break;
			case ZK.ZK_AVGLATENCY:
				assembly(type, zkAvgLatency, kpi);
				break;
			case ZK.ZK_OUTSTANDING_REQUESTS:
				assembly(type, zkOutstandingRequests, kpi);
				break;
			case ZK.ZK_NUM_ALIVECONNRCTIONS:
				assembly(type, zkNumAliveConnections, kpi);
				break;
			case ZK.ZK_OPENFILE_DESCRIPTOR_COUNT:
				assembly(type, zkOpenFileDescriptorCount, kpi);
				break;
			default:
				break;
			}
		}

		JSONObject target = new JSONObject();
		target.put("message", messageIns);
		target.put("inout", byteInOuts);
		target.put("failed", failedReqstProds);

		target.put("send", zkSendPackets);
		target.put("received", zkReceivedPackets);
		target.put("queue", zkOutstandingRequests);
		target.put("avg", zkAvgLatency);
		target.put("openfile", zkOpenFileDescriptorCount);
		target.put("alive", zkNumAliveConnections);

		return target.toJSONString();
	}

	private void assembly(String type, JSONArray assemblys, KpiInfo kpi) throws ParseException {
		JSONObject object = new JSONObject();
		if ("daily".equals(type)) {
			object.put("xkey", CalendarUtils.convertDate2Date(kpi.getTm()) + " " + kpi.getHour() + ":00");
		} else if ("day".equals(type)) {
			object.put("xkey", CalendarUtils.convertDate2Date(kpi.getTm()));
		}
		object.put(kpi.getKey(), StrUtils.numberic(kpi.getValue()));
		assemblys.add(object);
	}

	/** Crontab clean data. */
	public void remove(int tm) {
		mbeanDao.remove(tm);
	}

}
