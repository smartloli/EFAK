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
package org.smartloli.kafka.eagle.core.ipc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.UUID;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.thrift.TException;
import org.smartloli.kafka.eagle.common.util.ConstantUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import kafka.admin.AdminClient;
import kafka.admin.AdminClient.ConsumerSummary;
import kafka.common.OffsetAndMetadata;
import kafka.coordinator.GroupTopicPartition;
import scala.Option;
import scala.collection.Iterator;

/**
 * Implements kafka rpc api.
 * 
 * @author smartloli.
 *
 *         Created by Jan 5, 2017
 */
public class KafkaOffsetServerImpl extends KafkaOffsetGetter implements KafkaOffsetServer.Iface {

	/** Get offset in Kafka topic. */
	@Override
	public String getOffset(String clusterAlias, String bootstrapServers) throws TException {
		JSONArray targets = new JSONArray();
		Map<String, Boolean> activer = getActiver(clusterAlias);
		for (Entry<GroupTopicPartition, OffsetAndMetadata> entry : multiKafkaConsumerOffsets.get(clusterAlias).entrySet()) {
			JSONObject object = new JSONObject();
			object.put("group", entry.getKey().group());
			object.put("topic", entry.getKey().topicPartition().topic());
			object.put("partition", entry.getKey().topicPartition().partition());
			object.put("offset", entry.getValue().offset());
			object.put("timestamp", entry.getValue().commitTimestamp());
			String key = entry.getKey().group() + ConstantUtils.Separator.EIGHT + entry.getKey().topicPartition().topic() + ConstantUtils.Separator.EIGHT + entry.getKey().topicPartition().partition();
			if (activer.get(key)) {
				String owner = owners(bootstrapServers, entry.getKey().group());
				if (owner == null || owner == "") {
					object.put("owner", "");
				} else {
					UUID uuid = UUID.randomUUID();
					String threadId = String.format("%s_%s-%d-%s-%d", entry.getKey().group(), owner, System.currentTimeMillis(), (uuid.getMostSignificantBits() + "").substring(0, 8), entry.getKey().topicPartition().partition());
					object.put("owner", threadId);
				}
			} else {
				object.put("owner", "");
			}
			targets.add(object);
		}
		return targets.toJSONString();
	}

	/** Using SQL to get data from Kafka in topic. */
	@Override
	public String sql(String sql, String clusterAlias) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	/** Get consumer from Kafka in topic. */
	@Override
	public String getConsumer(String clusterAlias) throws TException {
		Map<String, Set<String>> targets = new HashMap<>();
		for (Entry<GroupTopicPartition, OffsetAndMetadata> entry : multiKafkaConsumerOffsets.get(clusterAlias).entrySet()) {
			String group = entry.getKey().group();
			String topic = entry.getKey().topicPartition().topic();
			if (targets.containsKey(group)) {
				Set<String> topics = targets.get(group);
				topics.add(topic);
			} else {
				Set<String> topics = new HashSet<>();
				topics.add(topic);
				targets.put(group, topics);
			}
		}
		Map<String, List<String>> targets2 = new HashMap<>();
		for (Entry<String, Set<String>> entry : targets.entrySet()) {
			List<String> topics = new ArrayList<>();
			for (String topic : entry.getValue()) {
				topics.add(topic);
			}
			targets2.put(entry.getKey(), topics);
		}
		return targets2.toString();
	}

	/** Get activer from Kafka in topic. */
	private Map<String, Boolean> getActiver(String clusterAlias) throws TException {
		long mill = System.currentTimeMillis();
		Map<String, Boolean> active = new ConcurrentHashMap<>();
		for (Entry<GroupTopicPartition, OffsetAndMetadata> entry : multiKafkaConsumerOffsets.get(clusterAlias).entrySet()) {
			String group = entry.getKey().group();
			String topic = entry.getKey().topicPartition().topic();
			int partition = entry.getKey().topicPartition().partition();
			long timespan = entry.getValue().commitTimestamp();
			String key = group + ConstantUtils.Separator.EIGHT + topic + ConstantUtils.Separator.EIGHT + partition;
			if (active.containsKey(key)) {
				if ((mill - timespan) <= ConstantUtils.Kafka.ACTIVER_INTERVAL) {
					active.put(key, true);
				} else {
					active.put(key, false);
				}
			} else {
				active.put(key, true);
			}
		}
		return active;
	}

	/** Get active consumer from Kafka in topic. */
	@Override
	public String getActiverConsumer(String clusterAlias) throws TException {
		long mill = System.currentTimeMillis();
		for (Entry<GroupTopicPartition, OffsetAndMetadata> entry : multiKafkaConsumerOffsets.get(clusterAlias).entrySet()) {
			String group = entry.getKey().group();
			String topic = entry.getKey().topicPartition().topic();
			int partition = entry.getKey().topicPartition().partition();
			long timespan = entry.getValue().commitTimestamp();
			String key = group + ConstantUtils.Separator.EIGHT + topic + ConstantUtils.Separator.EIGHT + partition;
			if (multiKafkaActiveConsumers.containsKey(clusterAlias)) {
				Map<String, Boolean> kafkaActiveConsumers = multiKafkaActiveConsumers.get(clusterAlias);
				if (kafkaActiveConsumers.containsKey(key)) {
					if ((mill - timespan) <= ConstantUtils.Kafka.ACTIVER_INTERVAL) {
						kafkaActiveConsumers.put(key, true);
					} else {
						kafkaActiveConsumers.put(key, false);
					}
				} else {
					kafkaActiveConsumers.put(key, true);
				}
			} else {
				Map<String, Boolean> kafkaActiveConsumers = new ConcurrentHashMap<>();
				kafkaActiveConsumers.put(key, true);
				multiKafkaActiveConsumers.put(clusterAlias, kafkaActiveConsumers);
			}

		}

		Map<String, Set<String>> targets = new HashMap<>();
		for (Entry<String, Boolean> entry : multiKafkaActiveConsumers.get(clusterAlias).entrySet()) {
			if (entry.getValue()) {
				String[] kk = entry.getKey().split(ConstantUtils.Separator.EIGHT);
				String key = kk[0] + "_" + kk[1];
				String topic = kk[1];
				if (targets.containsKey(key)) {
					targets.get(key).add(topic);
				} else {
					Set<String> topics = new HashSet<>();
					topics.add(topic);
					targets.put(key, topics);
				}
			}
		}

		Map<String, List<String>> targets2 = new HashMap<>();
		for (Entry<String, Set<String>> entry : targets.entrySet()) {
			List<String> topics = new ArrayList<>();
			for (String topic : entry.getValue()) {
				topics.add(topic);
			}
			targets2.put(entry.getKey(), topics);
		}

		return targets2.toString();
	}

	/** Get consumer page data from Kafka in topic. */
	@Override
	public String getConsumerPage(String search, int iDisplayStart, int iDisplayLength, String clusterAlias) throws TException {
		Map<String, Set<String>> targets = new HashMap<>();
		int offset = 0;
		for (Entry<GroupTopicPartition, OffsetAndMetadata> entry : multiKafkaConsumerOffsets.get(clusterAlias).entrySet()) {
			String group = entry.getKey().group();
			String topic = entry.getKey().topicPartition().topic();
			if (search.length() > 0 && search.equals(group)) {
				if (targets.containsKey(group)) {
					Set<String> topics = targets.get(group);
					topics.add(topic);
				} else {
					Set<String> topics = new HashSet<>();
					topics.add(topic);
					targets.put(group, topics);
				}
				break;
			} else if (search.length() == 0) {
				if (offset < (iDisplayLength + iDisplayStart) && offset >= iDisplayStart) {
					if (targets.containsKey(group)) {
						Set<String> topics = targets.get(group);
						topics.add(topic);
					} else {
						Set<String> topics = new HashSet<>();
						topics.add(topic);
						targets.put(group, topics);
					}
				}
				offset++;
			}
		}
		Map<String, List<String>> targets2 = new HashMap<>();
		for (Entry<String, Set<String>> entry : targets.entrySet()) {
			List<String> topics = new ArrayList<>();
			for (String topic : entry.getValue()) {
				topics.add(topic);
			}
			targets2.put(entry.getKey(), topics);
		}
		return targets2.toString();
	}

	/** Get kafka 0.10.2+ consuemr owner. */
	private String owners(String bootstrapServers, String group) {
		String target = "";
		Properties prop = new Properties();
		prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		AdminClient adminClient = AdminClient.create(prop);
		try {
			Option<scala.collection.immutable.List<ConsumerSummary>> opts = adminClient.describeConsumerGroup(group).consumers();
			Iterator<ConsumerSummary> consumerSummarys = opts.get().iterator();
			while (consumerSummarys.hasNext()) {
				ConsumerSummary consumerSummary = consumerSummarys.next();
				target = consumerSummary.host();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			adminClient.close();
		}
		return target.replaceAll("/", "");
	}

	/** Load kafka eagle system consumer topic. */
	@Override
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

}
