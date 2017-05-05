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

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.protocol.types.Schema;
import org.apache.kafka.common.protocol.types.Struct;
import org.apache.kafka.common.protocol.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.domain.offsets.KeyAndValueSchemasDomain;
import org.smartloli.kafka.eagle.common.domain.offsets.MessageValueStructAndVersionDomain;
import org.smartloli.kafka.eagle.common.util.ConstantUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;

import kafka.common.OffsetAndMetadata;
import kafka.common.OffsetMetadata;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.coordinator.GroupTopicPartition;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;

/**
 * New offset storage formats: kafka
 * 
 * @author smartloli.
 *
 *         Created by Jan 3, 2017
 */
public class KafkaOffsetGetter extends Thread {

	private final static Logger LOG = LoggerFactory.getLogger(KafkaOffsetGetter.class);

	/** Consumer offsets in kafka topic. */
	private final static String CONSUMER_OFFSET_TOPIC = ConstantUtils.Kafka.CONSUMER_OFFSET_TOPIC;
	// /** Store consumer offset in memory. */
	// private static Map<GroupTopicPartition, OffsetAndMetadata>
	// kafkaConsumerOffsets = new ConcurrentHashMap<>();

	/** Multi cluster information. */
	protected static Map<String, Map<GroupTopicPartition, OffsetAndMetadata>> multiKafkaConsumerOffsets = new ConcurrentHashMap<>();
	protected static Map<String, Map<String, Boolean>> multiKafkaActiveConsumers = new ConcurrentHashMap<>();

	/** ============================ Start Filter ========================= */
	// massive code stealing from kafka.server.OffsetManager
	private static Schema OFFSET_COMMIT_KEY_SCHEMA_V0 = new Schema(new Field("group", Type.STRING), new Field("topic", Type.STRING), new Field("partition", Type.INT32));
	private static Field KEY_GROUP_FIELD = OFFSET_COMMIT_KEY_SCHEMA_V0.get("group");
	private static Field KEY_TOPIC_FIELD = OFFSET_COMMIT_KEY_SCHEMA_V0.get("topic");
	private static Field KEY_PARTITION_FIELD = OFFSET_COMMIT_KEY_SCHEMA_V0.get("partition");

	private static Schema OFFSET_COMMIT_VALUE_SCHEMA_V0 = new Schema(new Field("offset", Type.INT64), new Field("metadata", Type.STRING, "Associated metadata.", ""),
			new Field("timestamp", Type.INT64));

	private static Schema OFFSET_COMMIT_VALUE_SCHEMA_V1 = new Schema(new Field("offset", Type.INT64), new Field("metadata", Type.STRING, "Associated metadata.", ""),
			new Field("commit_timestamp", Type.INT64), new Field("expire_timestamp", Type.INT64));

	private static Field VALUE_OFFSET_FIELD_V0 = OFFSET_COMMIT_VALUE_SCHEMA_V0.get("offset");
	private static Field VALUE_METADATA_FIELD_V0 = OFFSET_COMMIT_VALUE_SCHEMA_V0.get("metadata");
	private static Field VALUE_TIMESTAMP_FIELD_V0 = OFFSET_COMMIT_VALUE_SCHEMA_V0.get("timestamp");

	private static Field VALUE_OFFSET_FIELD_V1 = OFFSET_COMMIT_VALUE_SCHEMA_V1.get("offset");
	private static Field VALUE_METADATA_FIELD_V1 = OFFSET_COMMIT_VALUE_SCHEMA_V1.get("metadata");
	private static Field VALUE_COMMIT_TIMESTAMP_FIELD_V1 = OFFSET_COMMIT_VALUE_SCHEMA_V1.get("commit_timestamp");
	/** ============================ End Filter ========================= */

	/** Kafka offset memory in schema. */
	@SuppressWarnings("serial")
	private static Map<Integer, KeyAndValueSchemasDomain> OFFSET_SCHEMAS = new HashMap<Integer, KeyAndValueSchemasDomain>() {
		{
			KeyAndValueSchemasDomain ks0 = new KeyAndValueSchemasDomain();
			ks0.setKeySchema(OFFSET_COMMIT_KEY_SCHEMA_V0);
			ks0.setValueSchema(OFFSET_COMMIT_VALUE_SCHEMA_V0);
			put(0, ks0);

			KeyAndValueSchemasDomain ks1 = new KeyAndValueSchemasDomain();
			ks1.setKeySchema(OFFSET_COMMIT_KEY_SCHEMA_V0);
			ks1.setValueSchema(OFFSET_COMMIT_VALUE_SCHEMA_V1);
			put(1, ks1);
		}
	};

	/** Listening offset thread method. */
	private static synchronized void startOffsetListener(String clusterAlias, ConsumerConnector consumerConnector) {
		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put(CONSUMER_OFFSET_TOPIC, new Integer(1));
		KafkaStream<byte[], byte[]> offsetMsgStream = consumerConnector.createMessageStreams(topicCountMap).get(CONSUMER_OFFSET_TOPIC).get(0);

		ConsumerIterator<byte[], byte[]> it = offsetMsgStream.iterator();
		while (true) {
			MessageAndMetadata<byte[], byte[]> offsetMsg = it.next();
			if (ByteBuffer.wrap(offsetMsg.key()).getShort() < 2) {
				try {
					GroupTopicPartition commitKey = readMessageKey(ByteBuffer.wrap(offsetMsg.key()));
					if (offsetMsg.message() == null) {
						continue;
					}
					OffsetAndMetadata commitValue = readMessageValue(ByteBuffer.wrap(offsetMsg.message()));
					if (multiKafkaConsumerOffsets.containsKey(clusterAlias)) {
						multiKafkaConsumerOffsets.get(clusterAlias).put(commitKey, commitValue);
					} else {
						Map<GroupTopicPartition, OffsetAndMetadata> kafkaConsumerOffsets = new ConcurrentHashMap<>();
						kafkaConsumerOffsets.put(commitKey, commitValue);
						multiKafkaConsumerOffsets.put(clusterAlias, kafkaConsumerOffsets);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/** Get instance K&V schema. */
	private static KeyAndValueSchemasDomain schemaFor(int version) {
		return OFFSET_SCHEMAS.get(version);
	}

	/** Analysis of Kafka data in topic in buffer. */
	private static GroupTopicPartition readMessageKey(ByteBuffer buffer) {
		short version = buffer.getShort();
		Schema keySchema = schemaFor(version).getKeySchema();
		Struct key = (Struct) keySchema.read(buffer);
		String group = key.getString(KEY_GROUP_FIELD);
		String topic = key.getString(KEY_TOPIC_FIELD);
		int partition = key.getInt(KEY_PARTITION_FIELD);
		return new GroupTopicPartition(group, new TopicPartition(topic, partition));
	}

	/** Analysis of buffer data in metadata in Kafka. */
	private static OffsetAndMetadata readMessageValue(ByteBuffer buffer) {
		MessageValueStructAndVersionDomain structAndVersion = readMessageValueStruct(buffer);
		if (structAndVersion.getValue() == null) {
			return null;
		} else {
			if (structAndVersion.getVersion() == 0) {
				long offset = structAndVersion.getValue().getLong(VALUE_OFFSET_FIELD_V0);
				String metadata = structAndVersion.getValue().getString(VALUE_METADATA_FIELD_V0);
				long timestamp = structAndVersion.getValue().getLong(VALUE_TIMESTAMP_FIELD_V0);
				return new OffsetAndMetadata(new OffsetMetadata(offset, metadata), timestamp, timestamp);
			} else if (structAndVersion.getVersion() == 1) {
				long offset = structAndVersion.getValue().getLong(VALUE_OFFSET_FIELD_V1);
				String metadata = structAndVersion.getValue().getString(VALUE_METADATA_FIELD_V1);
				long commitTimestamp = structAndVersion.getValue().getLong(VALUE_COMMIT_TIMESTAMP_FIELD_V1);
				return new OffsetAndMetadata(new OffsetMetadata(offset, metadata), commitTimestamp, commitTimestamp);
			} else {
				throw new IllegalStateException("Unknown offset message version: " + structAndVersion.getVersion());
			}
		}
	}

	/** Analysis of struct data structure in metadata in Kafka. */
	private static MessageValueStructAndVersionDomain readMessageValueStruct(ByteBuffer buffer) {
		MessageValueStructAndVersionDomain mvs = new MessageValueStructAndVersionDomain();
		if (buffer == null) {
			mvs.setValue(null);
			mvs.setVersion(Short.valueOf("-1"));
		} else {
			short version = buffer.getShort();
			Schema valueSchema = schemaFor(version).getValueSchema();
			Struct value = (Struct) valueSchema.read(buffer);
			mvs.setValue(value);
			mvs.setVersion(version);
		}
		return mvs;
	}

	static {
		LOG.info("Initialize KafkaOffsetGetter clazz.");
		KafkaOffsetGetter kafka = new KafkaOffsetGetter();
		kafka.start();
	}

	/** Instance KafkaOffsetGetter clazz. */
	public static void getInstance() {
		LOG.info(KafkaOffsetGetter.class.getName());
	}

	/** Run method for running thread. */
	@Override
	public void run() {
		String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
		for (String clusterAlias : clusterAliass) {
			String zk = SystemConfigUtils.getProperty(clusterAlias + ".zk.list");
			Properties props = new Properties();
			props.put("group.id", "kafka.eagle.system.group");
			props.put("zookeeper.connect", zk);
			props.put("exclude.internal.topics", "false");
			ConsumerConnector consumer = Consumer.createJavaConsumerConnector(new ConsumerConfig(props));
			startOffsetListener(clusterAlias, consumer);
		}
	}

}
