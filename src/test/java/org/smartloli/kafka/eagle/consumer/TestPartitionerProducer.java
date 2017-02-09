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
package org.smartloli.kafka.eagle.consumer;

import java.util.Properties;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

/**
 * Test kafka8 topic partition data producer.
 *
 * @author smartloli.
 *
 *         Created by Mar 14, 2016
 */
public class TestPartitionerProducer {

	private static JSONArray dataSets = new JSONArray();
	static {
		for (int i = 0; i < 10; i++) {
			JSONObject dataset = new JSONObject();
			dataset.put("_plat", "100" + i);
			dataset.put("_uid", "53622890" + i);
			dataset.put("_tm", 1461309791);
			dataset.put("ip", "1.186.76.34");
			dataSets.add(dataset);
		}
	}

	public static void main(String[] args) {
		sender();
	}

	private static void sender() {
		Properties props = new Properties();
		props.put("serializer.class", "kafka.serializer.StringEncoder");
		props.put("metadata.broker.list", "slave01:9094,slave01:9095");
		props.put("partitioner.class", "org.smartloli.kafka.eagle.consumer.TestSimplePartitioner");
		Producer<String, String> producer = new Producer<String, String>(new ProducerConfig(props));
		String topic = "kv2_topic";
		int i = 0;
		for (Object object : dataSets) {
			String k = "key" + i;
			JSONObject v = (JSONObject) object;
			System.out.println(v.toJSONString());
			producer.send(new KeyedMessage<String, String>(topic, k, v.toJSONString()));
			i++;
		}
		producer.close();
	}
}
