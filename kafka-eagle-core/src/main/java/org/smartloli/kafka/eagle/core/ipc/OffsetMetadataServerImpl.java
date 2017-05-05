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
/**
 * 
 */
package org.smartloli.kafka.eagle.core.ipc;

import java.util.Arrays;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.thrift.TException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import kafka.common.OffsetAndMetadata;
import kafka.coordinator.GroupTopicPartition;

/**
 * Get offset metadata from kafka topic.
 * 
 * @author smartloli.
 *
 *         Created by May 5, 2017
 */
public class OffsetMetadataServerImpl extends KafkaOffsetGetter implements OffsetMetadataServer.Iface {

	/** Load kafka eagle system consumer topic. */
	public void system(String bootstrapServers) throws TException {
		Properties props = new Properties();
		props.put("bootstrap.servers", bootstrapServers);
		props.put("group.id", "kafka.eagle");
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Arrays.asList("__system.topic__"));
		ConsumerRecords<String, String> records = consumer.poll(100);
		for (ConsumerRecord<String, String> record : records)
			System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
		consumer.close();
	}

	/** Get kafka offset metadata from topic(__consumer_offsets). */
	public String getOffset(String clusterAlias) throws TException {
		if (multiKafkaConsumerOffsets.containsKey(clusterAlias)) {
			JSONArray targets = new JSONArray();
			for (Entry<GroupTopicPartition, OffsetAndMetadata> entry : multiKafkaConsumerOffsets.get(clusterAlias).entrySet()) {
				JSONObject object = new JSONObject();
				object.put("group", entry.getKey().group());
				object.put("topic", entry.getKey().topicPartition().topic());
				object.put("partition", entry.getKey().topicPartition().partition());
				object.put("offset", entry.getValue().offset());
				object.put("timestamp", entry.getValue().commitTimestamp());
				targets.add(object);
			}
			return targets.toJSONString();
		}
		return "";
	}

}
