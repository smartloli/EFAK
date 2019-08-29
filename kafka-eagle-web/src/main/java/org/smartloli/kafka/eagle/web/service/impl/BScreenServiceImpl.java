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
import java.util.List;
import java.util.Random;

import org.smartloli.kafka.eagle.web.service.BScreenService;
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

	/** Get producer and consumer real rate data . */
	public String getProducerAndConsumerRate(String clusterAlias) {
		JSONObject object = new JSONObject();
		object.put("producer", new Random().nextInt(1000) + 100);
		object.put("consumer", new Random().nextInt(1200) + 100);
		return object.toJSONString();
	}

	/** Get topic total logsize data . */
	public String getTopicTotalLogSize(String clusterAlias) {
		JSONObject object = new JSONObject();
		object.put("total", new Random().nextInt(100000) + 1000);
		return object.toJSONString();
	}

	/** Get producer history data. */
	public String getProducerOrConsumerHistory(String clusterAlias, String type) {
		JSONArray array = new JSONArray();
		for (int i = 0; i < 7; i++) {
			JSONObject object = new JSONObject();
			object.put("x", "08-" + (23 + i));
			object.put("y", new Random().nextInt(1000) + 100);
			array.add(object);
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
		} else if("history".equals(type)){
			List<String> xAxis = new ArrayList<String>();
			for (int i = 0; i < 7; i++) {
				xAxis.add("08-" + (23 + i));
				producers.add((new Random().nextInt(10000) + 100) + "");
				consumers.add((new Random().nextInt(20000) + 100) + "");
			}
			object.put("producers", producers);
			object.put("consumers", consumers);
			object.put("xAxis", xAxis);
		}else if("lag".equals(type)) {
			List<String> lags = new ArrayList<String>();
			for (int i = 0; i < 24; i++) {
				lags.add((new Random().nextInt(1000) + 100) + "");
			}
			object.put("lags", lags);
		}
		return object.toJSONString();
	}

}
