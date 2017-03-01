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

import org.smartloli.kafka.eagle.factory.KafkaFactory;
import org.smartloli.kafka.eagle.factory.KafkaService;
import org.smartloli.kafka.eagle.service.TopicService;
import org.smartloli.kafka.eagle.sql.execute.KafkaSqlParser;
import org.springframework.stereotype.Service;

/**
 * Kafka topic implements service interface.
 * 
 * @author smartloli.
 *
 *         Created by Aug 14, 2016.
 * 
 *         Update by hexiang 20170216
 */
@Service
public class TopicServiceImpl implements TopicService {

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	/** Find topic name in all topics. */
	public boolean hasTopic(String clusterAlias, String topicName) {
		boolean target = false;
		JSONArray topicAndPartitions = JSON.parseArray(kafkaService.getAllPartitions(clusterAlias));
		for (Object topicAndPartition : topicAndPartitions) {
			JSONObject object = (JSONObject) topicAndPartition;
			String topic = object.getString("topic");
			if (topicName.equals(topic)) {
				target = true;
				break;
			}
		}
		return target;
	}

	/** Get metadata in topic. */
	public String metadata(String clusterAlias, String topicName) {
		return kafkaService.findLeader(clusterAlias, topicName).toString();
	}

	/** List all the topic under Kafka in partition. */
	public String list(String clusterAlias) {
		return kafkaService.getAllPartitions(clusterAlias);
	}

	/** Execute kafka execute query sql and viewer topic message. */
	public String execute(String clusterAlias, String sql) {
		return KafkaSqlParser.execute(clusterAlias, sql);
	}

}
