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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.smartloli.kafka.eagle.common.protocol.KpiInfo;
import org.smartloli.kafka.eagle.common.protocol.bscreen.BScreenProducerInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.CollectorType;
import org.smartloli.kafka.eagle.common.util.KConstants.MBean;
import org.smartloli.kafka.eagle.common.util.StrUtils;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerFactory;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerService;
import org.smartloli.kafka.eagle.web.dao.MBeanDao;
import org.smartloli.kafka.eagle.web.dao.TopicDao;
import org.smartloli.kafka.eagle.web.service.BScreenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Big screen get data.
 * 
 * @author smartloli.
 *
 *         Created by Aug 29, 2019
 */
@Service
public class BScreenServiceImpl implements BScreenService {

	@Autowired
	private TopicDao topicDao;

	@Autowired
	private MBeanDao mbeanDao;

	/** Broker service interface. */
	private static BrokerService brokerService = new BrokerFactory().create();

	/** Get producer and consumer real rate data . */
	public String getProducerAndConsumerRate(String clusterAlias) {
		List<KpiInfo> byteIns = getBrokersKpi(clusterAlias, MBean.BYTEIN);
		List<KpiInfo> byteOuts = getBrokersKpi(clusterAlias, MBean.BYTEOUT);
		long ins = 0L;
		for (KpiInfo kpi : byteIns) {
			try {
				ins += Long.parseLong(kpi.getValue());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		long outs = 0L;
		for (KpiInfo kpi : byteOuts) {
			try {
				outs += Long.parseLong(kpi.getValue());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		JSONObject object = new JSONObject();
		object.put("ins", StrUtils.stringify(ins));
		object.put("outs", StrUtils.stringify(outs));
		return object.toJSONString();
	}

	private List<KpiInfo> getBrokersKpi(String clusterAlias, String key) {
		Map<String, Object> param = new HashMap<>();
		param.put("cluster", clusterAlias);
		param.put("stime", CalendarUtils.getDate());
		param.put("etime", CalendarUtils.getDate());
		param.put("type", CollectorType.KAFKA);
		param.put("key", key);
		param.put("size", brokerService.brokerNumbers(clusterAlias));
		return mbeanDao.getBrokersKpi(param);
	}

	/** Get topic total logsize data . */
	public String getTopicTotalLogSize(String clusterAlias) {
		Map<String, Object> params = new HashMap<>();
		params.put("cluster", clusterAlias);
		params.put("topics", brokerService.topicList(clusterAlias));
		params.put("size", brokerService.topicList(clusterAlias).size());
		params.put("tday", CalendarUtils.getCustomDate("yyyyMMdd"));
		long totalRecords = topicDao.getBScreenTotalRecords(params);
		JSONObject object = new JSONObject();
		object.put("total", totalRecords);
		return object.toJSONString();
	}

	/** Get producer and consumer history data. */
	public String getProducerOrConsumerHistory(String clusterAlias, String type) {
		JSONArray array = new JSONArray();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("cluster", clusterAlias);
		params.put("stime", CalendarUtils.getCustomLastDay(7));
		params.put("etime", CalendarUtils.getCustomDate("yyyyMMdd"));
		if ("producer".equals(type)) {
			List<BScreenProducerInfo> bsProducers = topicDao.queryProducerHistoryBar(params);
			Map<String, Object> bsMaps = new HashMap<>();
			for (BScreenProducerInfo bsProducer : bsProducers) {
				if (bsProducer != null) {
					bsMaps.put(bsProducer.getTm(), bsProducer.getValue());
				}
			}
			for (int i = 6; i >= 0; i--) {
				String tm = CalendarUtils.getCustomLastDay(i);
				if (bsMaps.containsKey(tm)) {
					JSONObject object = new JSONObject();
					object.put("x", CalendarUtils.getCustomLastDay("MM-dd", i));
					object.put("y", bsMaps.get(tm).toString());
					array.add(object);
				} else {
					JSONObject object = new JSONObject();
					object.put("x", CalendarUtils.getCustomLastDay("MM-dd", i));
					object.put("y", 0);
					array.add(object);
				}
			}
		} else {
			// mock consumer
			for (int i = 0; i < 7; i++) {
				JSONObject object = new JSONObject();
				object.put("x", "08-" + (23 + i));
				object.put("y", new Random().nextInt(1000) + 100);
				array.add(object);
			}
		}

		return array.toJSONString();
	}

	@Override
	public String getTodayOrHistoryConsumerProducer(String clusterAlias, String type) {
		List<String> producers = new ArrayList<String>();
		List<String> consumers = new ArrayList<String>();
		JSONObject object = new JSONObject();

		if ("today".equals(type)) {
			for (int i = 0; i < 24; i++) {
				producers.add((new Random().nextInt(1000) + 100) + "");
				consumers.add((new Random().nextInt(2000) + 100) + "");
			}
			object.put("producers", producers);
			object.put("consumers", consumers);
		} else if ("history".equals(type)) {
			List<String> xAxis = new ArrayList<String>();
			for (int i = 0; i < 7; i++) {
				xAxis.add("08-" + (23 + i));
				producers.add((new Random().nextInt(10000) + 100) + "");
				consumers.add((new Random().nextInt(20000) + 100) + "");
			}
			object.put("producers", producers);
			object.put("consumers", consumers);
			object.put("xAxis", xAxis);
		} else if ("lag".equals(type)) {
			List<String> lags = new ArrayList<String>();
			for (int i = 0; i < 24; i++) {
				lags.add((new Random().nextInt(1000) + 100) + "");
			}
			object.put("lags", lags);
		}
		return object.toJSONString();
	}

}
