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
import java.util.Map.Entry;

import org.smartloli.kafka.eagle.common.protocol.KpiInfo;
import org.smartloli.kafka.eagle.common.protocol.bscreen.BScreenBarInfo;
import org.smartloli.kafka.eagle.common.protocol.bscreen.BScreenConsumerInfo;
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
			List<BScreenBarInfo> bsProducers = topicDao.queryProducerHistoryBar(params);
			Map<String, Object> bsMaps = new HashMap<>();
			for (BScreenBarInfo bsProducer : bsProducers) {
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
			List<BScreenBarInfo> bsConsumers = topicDao.queryConsumerHistoryBar(params);
			Map<String, Object> bsMaps = new HashMap<>();
			for (BScreenBarInfo bsConsumer : bsConsumers) {
				if (bsConsumer != null) {
					bsMaps.put(bsConsumer.getTm(), bsConsumer.getValue());
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
		}

		return array.toJSONString();
	}

	@Override
	public String getTodayOrHistoryConsumerProducer(String clusterAlias, String type) {
		JSONObject object = new JSONObject();
		if ("producers".equals(type)) {
			List<Long> producers = new ArrayList<Long>();
			Map<String, Object> params = new HashMap<>();
			params.put("cluster", clusterAlias);
			params.put("tday", CalendarUtils.getCustomDate("yyyyMMdd"));
			List<BScreenConsumerInfo> bscreenConsumers = topicDao.queryTodayBScreenConsumer(params);
			Map<String, List<Long>> producerKeys = new HashMap<>();
			for (int i = 0; i < 24; i++) {
				if (i < 10) {
					producerKeys.put("0" + i, new ArrayList<>());
				} else {
					producerKeys.put(i + "", new ArrayList<>());
				}
			}
			for (BScreenConsumerInfo bscreenConsumer : bscreenConsumers) {
				String key = CalendarUtils.convertUnixTime(bscreenConsumer.getTimespan(), "HH");
				if (producerKeys.containsKey(key)) {
					producerKeys.get(key).add(bscreenConsumer.getDifflogsize());
				}
			}
			for (Entry<String, List<Long>> entry : producerKeys.entrySet()) {
				long sum = 0L;
				for (Long logsize : entry.getValue()) {
					sum += logsize;
				}
				producers.add(sum);
			}
			object.put("producers", producers);
		} else if ("consumers".equals(type)) {
			List<Long> consumers = new ArrayList<Long>();
			Map<String, Object> params = new HashMap<>();
			params.put("cluster", clusterAlias);
			params.put("tday", CalendarUtils.getCustomDate("yyyyMMdd"));
			List<BScreenConsumerInfo> bscreenConsumers = topicDao.queryTodayBScreenConsumer(params);
			Map<String, List<Long>> consumerKeys = new HashMap<>();
			for (int i = 0; i < 24; i++) {
				if (i < 10) {
					consumerKeys.put("0" + i, new ArrayList<>());
				} else {
					consumerKeys.put(i + "", new ArrayList<>());
				}
			}
			for (BScreenConsumerInfo bscreenConsumer : bscreenConsumers) {
				String key = CalendarUtils.convertUnixTime(bscreenConsumer.getTimespan(), "HH");
				if (consumerKeys.containsKey(key)) {
					consumerKeys.get(key).add(bscreenConsumer.getDiffoffsets());
				}
			}
			for (Entry<String, List<Long>> entry : consumerKeys.entrySet()) {
				long sum = 0L;
				for (Long offsets : entry.getValue()) {
					sum += offsets;
				}
				consumers.add(sum);
			}			
			object.put("consumers", consumers);
		} else if ("lag".equals(type)) {
			Map<String, Object> params = new HashMap<>();
			params.put("cluster", clusterAlias);
			params.put("tday", CalendarUtils.getCustomDate("yyyyMMdd"));
			List<BScreenConsumerInfo> bscreenConsumers = topicDao.queryTodayBScreenConsumer(params);
			List<Long> lags = new ArrayList<Long>();
			Map<String, List<Long>> keys = new HashMap<>();
			for (int i = 0; i < 24; i++) {
				if (i < 10) {
					keys.put("0" + i, new ArrayList<>());
				} else {
					keys.put(i + "", new ArrayList<>());
				}
			}
			for (BScreenConsumerInfo bscreenConsumer : bscreenConsumers) {
				String key = CalendarUtils.convertUnixTime(bscreenConsumer.getTimespan(), "HH");
				if (keys.containsKey(key)) {
					keys.get(key).add(bscreenConsumer.getLag());
				}
			}
			for (Entry<String, List<Long>> entry : keys.entrySet()) {
				long sum = 0L;
				for (Long lag : entry.getValue()) {
					sum += lag;
				}
				lags.add(sum);
			}
			object.put("lags", lags);
		}
		return object.toJSONString();
	}

}
