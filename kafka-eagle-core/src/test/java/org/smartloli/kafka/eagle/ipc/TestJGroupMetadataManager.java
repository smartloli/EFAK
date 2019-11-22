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
package org.smartloli.kafka.eagle.ipc;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.protocol.types.ArrayOf;
import org.apache.kafka.common.protocol.types.BoundField;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.protocol.types.Schema;
import org.apache.kafka.common.protocol.types.Struct;
import org.apache.kafka.common.protocol.types.Type;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kafka.common.OffsetAndMetadata;
import kafka.common.OffsetMetadata;
import kafka.coordinator.group.BaseKey;
import kafka.coordinator.group.GroupMetadataKey;
import kafka.coordinator.group.GroupTopicPartition;
import kafka.coordinator.group.OffsetKey;

/**
 * Messages stored for the group topic has versions for both the key and value
 * fields. Key version is used to indicate the type of the message (also to
 * differentiate different types of messages from being compacted together if
 * they have the same field values); and value version is used to evolve the
 * messages within their data types:
 *
 * key version 0: group consumption offset <br>
 * -> value version 0: [offset, metadata, timestamp]
 *
 * key version 1: group consumption offset <br>
 * -> value version 1: [offset, metadata, commit_timestamp, expire_timestamp]
 *
 * key version 2: group metadata <br>
 * -> value version 0: [protocol_type, generation, protocol, leader, members]
 * 
 * @author smartloli.
 *
 *         Created by Aug 5, 2019
 */
public class TestJGroupMetadataManager {

	private final static Logger LOG = LoggerFactory.getLogger(TestJGroupMetadataManager.class);

	private static short CURRENT_OFFSET_KEY_SCHEMA_VERSION = 1;
	private static short CURRENT_GROUP_KEY_SCHEMA_VERSION = 2;
	private static short CURRENT_GROUP_KEY_SCHEMA_VERSION_V3 = 3;

	private static Schema OFFSET_COMMIT_KEY_SCHEMA = new Schema(new Field("group", Type.STRING), new Field("topic", Type.STRING), new Field("partition", Type.INT32));

	private static BoundField OFFSET_KEY_GROUP_FIELD = OFFSET_COMMIT_KEY_SCHEMA.get("group");
	private static BoundField OFFSET_KEY_TOPIC_FIELD = OFFSET_COMMIT_KEY_SCHEMA.get("topic");
	private static BoundField OFFSET_KEY_PARTITION_FIELD = OFFSET_COMMIT_KEY_SCHEMA.get("partition");

	private static Schema OFFSET_COMMIT_VALUE_SCHEMA_V0 = new Schema(new Field("offset", Type.INT64), new Field("metadata", Type.STRING, "Associated metadata.", ""), new Field("timestamp", Type.INT64));
	private static BoundField OFFSET_VALUE_OFFSET_FIELD_V0 = OFFSET_COMMIT_VALUE_SCHEMA_V0.get("offset");
	private static BoundField OFFSET_VALUE_METADATA_FIELD_V0 = OFFSET_COMMIT_VALUE_SCHEMA_V0.get("metadata");
	private static BoundField OFFSET_VALUE_TIMESTAMP_FIELD_V0 = OFFSET_COMMIT_VALUE_SCHEMA_V0.get("timestamp");

	private static Schema OFFSET_COMMIT_VALUE_SCHEMA_V1 = new Schema(new Field("offset", Type.INT64), new Field("metadata", Type.STRING, "Associated metadata.", ""), new Field("commit_timestamp", Type.INT64),
			new Field("expire_timestamp", Type.INT64));
	private static BoundField OFFSET_VALUE_OFFSET_FIELD_V1 = OFFSET_COMMIT_VALUE_SCHEMA_V1.get("offset");
	private static BoundField OFFSET_VALUE_METADATA_FIELD_V1 = OFFSET_COMMIT_VALUE_SCHEMA_V1.get("metadata");
	private static BoundField OFFSET_VALUE_COMMIT_TIMESTAMP_FIELD_V1 = OFFSET_COMMIT_VALUE_SCHEMA_V1.get("commit_timestamp");
	private static BoundField OFFSET_VALUE_EXPIRE_TIMESTAMP_FIELD_V1 = OFFSET_COMMIT_VALUE_SCHEMA_V1.get("expire_timestamp");

	private static Schema OFFSET_COMMIT_VALUE_SCHEMA_V2 = new Schema(new Field("offset", Type.INT64), new Field("metadata", Type.STRING, "Associated metadata.", ""), new Field("commit_timestamp", Type.INT64));
	private static BoundField OFFSET_VALUE_OFFSET_FIELD_V2 = OFFSET_COMMIT_VALUE_SCHEMA_V2.get("offset");
	private static BoundField OFFSET_VALUE_METADATA_FIELD_V2 = OFFSET_COMMIT_VALUE_SCHEMA_V2.get("metadata");
	private static BoundField OFFSET_VALUE_COMMIT_TIMESTAMP_FIELD_V2 = OFFSET_COMMIT_VALUE_SCHEMA_V2.get("commit_timestamp");

	private static Schema OFFSET_COMMIT_VALUE_SCHEMA_V3 = new Schema(new Field("offset", Type.INT64), new Field("leader_epoch", Type.INT32), new Field("metadata", Type.STRING, "Associated metadata.", ""),
			new Field("commit_timestamp", Type.INT64));
	private static BoundField OFFSET_VALUE_OFFSET_FIELD_V3 = OFFSET_COMMIT_VALUE_SCHEMA_V3.get("offset");
	private static BoundField OFFSET_VALUE_LEADER_EPOCH_FIELD_V3 = OFFSET_COMMIT_VALUE_SCHEMA_V3.get("leader_epoch");
	private static BoundField OFFSET_VALUE_METADATA_FIELD_V3 = OFFSET_COMMIT_VALUE_SCHEMA_V3.get("metadata");
	private static BoundField OFFSET_VALUE_COMMIT_TIMESTAMP_FIELD_V3 = OFFSET_COMMIT_VALUE_SCHEMA_V3.get("commit_timestamp");

	private static Schema GROUP_METADATA_KEY_SCHEMA = new Schema(new Field("group", Type.STRING));
	private static BoundField GROUP_KEY_GROUP_FIELD = GROUP_METADATA_KEY_SCHEMA.get("group");

	private static String MEMBER_ID_KEY = "member_id";
	private static String GROUP_INSTANCE_ID_KEY = "group_instance_id";
	private static String CLIENT_ID_KEY = "client_id";
	private static String CLIENT_HOST_KEY = "client_host";
	private static String REBALANCE_TIMEOUT_KEY = "rebalance_timeout";
	private static String SESSION_TIMEOUT_KEY = "session_timeout";
	private static String SUBSCRIPTION_KEY = "subscription";
	private static String ASSIGNMENT_KEY = "assignment";

	private static Schema MEMBER_METADATA_V0 = new Schema(new Field(MEMBER_ID_KEY, Type.STRING), new Field(CLIENT_ID_KEY, Type.STRING), new Field(CLIENT_HOST_KEY, Type.STRING), new Field(SESSION_TIMEOUT_KEY, Type.INT32),
			new Field(SUBSCRIPTION_KEY, Type.BYTES), new Field(ASSIGNMENT_KEY, Type.BYTES));

	private static Schema MEMBER_METADATA_V1 = new Schema(new Field(MEMBER_ID_KEY, Type.STRING), new Field(CLIENT_ID_KEY, Type.STRING), new Field(CLIENT_HOST_KEY, Type.STRING), new Field(REBALANCE_TIMEOUT_KEY, Type.INT32),
			new Field(SESSION_TIMEOUT_KEY, Type.INT32), new Field(SUBSCRIPTION_KEY, Type.BYTES), new Field(ASSIGNMENT_KEY, Type.BYTES));

	private static Schema MEMBER_METADATA_V2 = MEMBER_METADATA_V1;

	private static Schema MEMBER_METADATA_V3 = new Schema(new Field(MEMBER_ID_KEY, Type.STRING), new Field(GROUP_INSTANCE_ID_KEY, Type.NULLABLE_STRING), new Field(CLIENT_ID_KEY, Type.STRING), new Field(CLIENT_HOST_KEY, Type.STRING),
			new Field(REBALANCE_TIMEOUT_KEY, Type.INT32), new Field(SESSION_TIMEOUT_KEY, Type.INT32), new Field(SUBSCRIPTION_KEY, Type.BYTES), new Field(ASSIGNMENT_KEY, Type.BYTES));

	private static String PROTOCOL_TYPE_KEY = "protocol_type";
	private static String GENERATION_KEY = "generation";
	private static String PROTOCOL_KEY = "protocol";
	private static String LEADER_KEY = "leader";
	private static String CURRENT_STATE_TIMESTAMP_KEY = "current_state_timestamp";
	private static String MEMBERS_KEY = "members";

	private static Schema GROUP_METADATA_VALUE_SCHEMA_V0 = new Schema(new Field(PROTOCOL_TYPE_KEY, Type.STRING), new Field(GENERATION_KEY, Type.INT32), new Field(PROTOCOL_KEY, Type.NULLABLE_STRING),
			new Field(LEADER_KEY, Type.NULLABLE_STRING), new Field(MEMBERS_KEY, new ArrayOf(MEMBER_METADATA_V0)));

	private static Schema GROUP_METADATA_VALUE_SCHEMA_V1 = new Schema(new Field(PROTOCOL_TYPE_KEY, Type.STRING), new Field(GENERATION_KEY, Type.INT32), new Field(PROTOCOL_KEY, Type.NULLABLE_STRING),
			new Field(LEADER_KEY, Type.NULLABLE_STRING), new Field(MEMBERS_KEY, new ArrayOf(MEMBER_METADATA_V1)));

	private static Schema GROUP_METADATA_VALUE_SCHEMA_V2 = new Schema(new Field(PROTOCOL_TYPE_KEY, Type.STRING), new Field(GENERATION_KEY, Type.INT32), new Field(PROTOCOL_KEY, Type.NULLABLE_STRING),
			new Field(LEADER_KEY, Type.NULLABLE_STRING), new Field(CURRENT_STATE_TIMESTAMP_KEY, Type.INT64), new Field(MEMBERS_KEY, new ArrayOf(MEMBER_METADATA_V2)));

	private static Schema GROUP_METADATA_VALUE_SCHEMA_V3 = new Schema(new Field(PROTOCOL_TYPE_KEY, Type.STRING), new Field(GENERATION_KEY, Type.INT32), new Field(PROTOCOL_KEY, Type.NULLABLE_STRING),
			new Field(LEADER_KEY, Type.NULLABLE_STRING), new Field(CURRENT_STATE_TIMESTAMP_KEY, Type.INT64), new Field(MEMBERS_KEY, new ArrayOf(MEMBER_METADATA_V3)));

	// map of versions to key schemas as data types
	private static Map<Integer, Schema> MESSAGE_TYPE_SCHEMAS = new HashMap<Integer, Schema>() {
		{
			put(0, OFFSET_COMMIT_KEY_SCHEMA);
			put(1, OFFSET_COMMIT_KEY_SCHEMA);
			put(2, GROUP_METADATA_KEY_SCHEMA);
			put(3, GROUP_METADATA_KEY_SCHEMA);
		}
	};

	// map of version of offset value schemas
	private static Map<Integer, Schema> OFFSET_VALUE_SCHEMAS = new HashMap<Integer, Schema>() {
		{
			put(0, OFFSET_COMMIT_VALUE_SCHEMA_V0);
			put(1, OFFSET_COMMIT_VALUE_SCHEMA_V1);
			put(2, OFFSET_COMMIT_VALUE_SCHEMA_V2);
			put(3, OFFSET_COMMIT_VALUE_SCHEMA_V3);
		}
	};

	// map of version of group metadata value schemas
	private static Map<Integer, Schema> GROUP_VALUE_SCHEMAS = new HashMap<Integer, Schema>() {
		{
			put(0, GROUP_METADATA_VALUE_SCHEMA_V0);
			put(1, GROUP_METADATA_VALUE_SCHEMA_V1);
			put(2, GROUP_METADATA_VALUE_SCHEMA_V2);
			put(3, GROUP_METADATA_VALUE_SCHEMA_V3);
		}
	};

	private static Schema CURRENT_OFFSET_KEY_SCHEMA = schemaForKey(CURRENT_OFFSET_KEY_SCHEMA_VERSION);
	private static Schema CURRENT_GROUP_KEY_SCHEMA = schemaForKey(CURRENT_GROUP_KEY_SCHEMA_VERSION);

	private static Schema schemaForKey(int version) {
		if (MESSAGE_TYPE_SCHEMAS.containsKey(version)) {
			return MESSAGE_TYPE_SCHEMAS.get(version);
		} else {
			LOG.error("Unknown message key schema version " + version);
			return null;
		}
	}

	private static Schema schemaForOffsetValue(int version) {
		if (OFFSET_VALUE_SCHEMAS.containsKey(version)) {
			return OFFSET_VALUE_SCHEMAS.get(version);
		} else {
			LOG.error("Unknown offset schema version " + version);
			return null;
		}
	}

	private static Schema schemaForGroupValue(int version) {
		if (GROUP_VALUE_SCHEMAS.containsKey(version)) {
			return GROUP_VALUE_SCHEMAS.get(version);
		} else {
			LOG.error("Unknown group metadata version " + version);
			return null;
		}
	}

	/**
	 * Decodes the offset messages' key
	 *
	 * @param buffer
	 *            input byte-buffer
	 * @return an GroupTopicPartition object
	 */
	private static BaseKey readMessageKey(ByteBuffer buffer) {
		short version = buffer.getShort();
		Schema keySchema = schemaForKey(version);
		Struct key = keySchema.read(buffer);

		if (version <= CURRENT_OFFSET_KEY_SCHEMA_VERSION) {
			// version 0 and 1 refer to offset
			String group = key.getString(OFFSET_KEY_GROUP_FIELD);
			String topic = key.getString(OFFSET_KEY_TOPIC_FIELD);
			int partition = key.getInt(OFFSET_KEY_PARTITION_FIELD);
			return new OffsetKey(version, new GroupTopicPartition(group, new TopicPartition(topic, partition)));
		} else if (version == CURRENT_GROUP_KEY_SCHEMA_VERSION) {
			// version 2 refers to offset
			String group = key.getString(GROUP_KEY_GROUP_FIELD);
			return new GroupMetadataKey(version, group);
		}else if (version == CURRENT_GROUP_KEY_SCHEMA_VERSION_V3) {
			// version 3 refers to offset
			String group = key.getString(GROUP_KEY_GROUP_FIELD);
			return new GroupMetadataKey(version, group);
		} else {
			throw new IllegalStateException("Unknown group metadata message version: " + version);
		}
	}

	/**
	 * Decodes the offset messages' payload and retrieves offset and metadata
	 * from it
	 *
	 * @param buffer
	 *            input byte-buffer
	 * @return an offset-metadata object from the message
	 */
	private static OffsetAndMetadata readOffsetMessageValue(ByteBuffer buffer) {
		if (buffer == null) { // tombstone
			return null;
		} else {
			short version = buffer.getShort();
			Schema valueSchema = schemaForOffsetValue(version);
			Struct value = valueSchema.read(buffer);

			if (version == 0) {
				long offset = value.getLong(OFFSET_VALUE_OFFSET_FIELD_V0);
				String metadata = value.getString(OFFSET_VALUE_METADATA_FIELD_V0);
				long timestamp = value.getLong(OFFSET_VALUE_TIMESTAMP_FIELD_V0);

				return new OffsetAndMetadata(new OffsetMetadata(offset, metadata), timestamp, timestamp);
			} else if (version == 1) {
				long offset = value.getLong(OFFSET_VALUE_OFFSET_FIELD_V1);
				String metadata = value.getString(OFFSET_VALUE_METADATA_FIELD_V1);
				long commitTimestamp = value.getLong(OFFSET_VALUE_COMMIT_TIMESTAMP_FIELD_V1);
				long expireTimestamp = value.getLong(OFFSET_VALUE_EXPIRE_TIMESTAMP_FIELD_V1);

				return new OffsetAndMetadata(new OffsetMetadata(offset, metadata), commitTimestamp, expireTimestamp);
			} else if (version == 2) {
				long offset = value.getLong(OFFSET_VALUE_OFFSET_FIELD_V2);
				String metadata = value.getString(OFFSET_VALUE_METADATA_FIELD_V2);
				long commitTimestamp = value.getLong(OFFSET_VALUE_COMMIT_TIMESTAMP_FIELD_V2);

				return new OffsetAndMetadata(new OffsetMetadata(offset, metadata), commitTimestamp, commitTimestamp);
			} else if (version == 3) {
				long offset = value.getLong(OFFSET_VALUE_OFFSET_FIELD_V3);
				String metadata = value.getString(OFFSET_VALUE_METADATA_FIELD_V3);
				long commitTimestamp = value.getLong(OFFSET_VALUE_COMMIT_TIMESTAMP_FIELD_V3);

				return new OffsetAndMetadata(new OffsetMetadata(offset, metadata), commitTimestamp, commitTimestamp);
			} else {
				throw new IllegalStateException("Unknown offset message version: " + version);
			}
		}
	}
	
	public static void main(String[] args) {
		Properties prop = new Properties();
		prop.put("group.id", "kafka.eagle.system.group");
		prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
		prop.put("exclude.internal.topics", "false");
		prop.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		prop.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(prop);
		consumer.subscribe(Arrays.asList("__consumer_offsets"));
		boolean flag = true;
		while (flag) {
			try {
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
				for (ConsumerRecord<String, String> record : records) {
					ByteBuffer buffer = ByteBuffer.wrap(record.value().getBytes());
					
//					OffsetAndMetadata meta = GroupMetadataManager.readOffsetMessageValue();
//					BaseKey key = readMessageKey(buffer);
					OffsetAndMetadata gm = readOffsetMessageValue(buffer);
					System.out.println(gm);
				}
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}
}
