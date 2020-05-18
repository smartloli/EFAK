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
package org.smartloli.kafka.eagle.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.kafka.common.TopicPartition;
import org.smartloli.kafka.eagle.common.util.KafkaZKPoolUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import kafka.admin.ReassignPartitionsCommand;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.JavaConverters;
import scala.collection.Map;
import scala.collection.Seq;

/**
 * TODO
 * 
 * @author smartloli.
 *
 *         Created by May 18, 2020
 */
public class TestKafkaAdminServer {
	private static KafkaZKPoolUtils kafkaZKPool = KafkaZKPoolUtils.getInstance();

	public static void main(String[] args) {
		JSONObject topics = new JSONObject();
		JSONArray values = new JSONArray();
		JSONObject topic01 = new JSONObject();
		topic01.put("topic", "k20200326");
		values.add(topic01);
		topics.put("topics", values);
		topics.put("version", 1);
		List<Object> ids = new ArrayList<Object>();
		ids.add(0);
		Seq<Object> seq = JavaConverters.asScalaIteratorConverter(ids.iterator()).asScala().toSeq();
		Tuple2<Map<TopicPartition, Seq<Object>>, Map<TopicPartition, Seq<Object>>> tuple = ReassignPartitionsCommand.generateAssignment(kafkaZKPool.getZkClient("cluster1"), seq, topics.toJSONString(), true);
		System.out.println("Proposed partition reassignment configuartion");
		for (Entry<TopicPartition, Seq<Object>> entry : JavaConversions.mapAsJavaMap(tuple._1).entrySet()) {
			List<Object> object = JavaConversions.seqAsJavaList(entry.getValue());
			System.out.println(entry.getKey().topic() + "," + entry.getKey().partition() + "," + object.toString());
		}
		System.out.println("Current partition replica assignment");
		for (Entry<TopicPartition, Seq<Object>> entry : JavaConversions.mapAsJavaMap(tuple._2).entrySet()) {
			List<Object> object = JavaConversions.seqAsJavaList(entry.getValue());
			System.out.println(entry.getKey().topic() + "," + entry.getKey().partition() + "," + object.toString());
		}
	}
}
