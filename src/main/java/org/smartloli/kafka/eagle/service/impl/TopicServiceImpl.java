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
package org.smartloli.kafka.eagle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.smartloli.kafka.eagle.service.TopicService;
import org.smartloli.kafka.eagle.util.KafkaClusterUtils;
import org.smartloli.kafka.eagle.util.KafkaMetaUtils;
import org.springframework.stereotype.Service;

/**
 * Kafka topic implements service interface.
 * 
 * @author smartloli.
 *
 *         Created by Aug 14, 2016
 */
@Service
public class TopicServiceImpl implements TopicService {

	/** Find topic name in all topics. */
	public boolean hasTopic(String topicName, String ip) {
		boolean ret = false;
		JSONArray arr = JSON.parseArray(KafkaClusterUtils.getAllPartitions());
		for (Object object : arr) {
			JSONObject obj = (JSONObject) object;
			String topic = obj.getString("topic");
			if (topicName.equals(topic)) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	/** Get metadata in topic. */
	public String metadata(String topicName) {
		return KafkaMetaUtils.findLeader(topicName).toString();
	}

	/** List all the topic under Kafka in partition. */
	public String list() {
		return KafkaClusterUtils.getAllPartitions();
	}

}
