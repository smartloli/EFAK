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
import org.apache.kafka.common.protocol.types.BoundField;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.protocol.types.Schema;
import org.apache.kafka.common.protocol.types.Struct;
import org.apache.kafka.common.protocol.types.Type;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.smartloli.kafka.eagle.common.protocol.offsets.KeyAndValueSchemasInfo;
import org.smartloli.kafka.eagle.common.protocol.offsets.MessageValueStructAndVersionInfo;

import kafka.common.OffsetAndMetadata;
import kafka.common.OffsetMetadata;
import kafka.coordinator.group.BaseKey;
import kafka.coordinator.group.GroupMetadataManager;
import kafka.coordinator.group.GroupTopicPartition;
import kafka.coordinator.group.OffsetKey;

/**
 * TODO
 * 
 * @author smartloli.
 *
 *         Created by Aug 4, 2019
 */
public class TestBrokerAdminClient {
	
	/** ============================ Start Filter ========================= */
	private static Schema OFFSET_COMMIT_KEY_SCHEMA_V0 = new Schema(new Field("group", Type.STRING), new Field("topic", Type.STRING), new Field("partition", Type.INT32));
	private static BoundField KEY_GROUP_FIELD = OFFSET_COMMIT_KEY_SCHEMA_V0.get("group");
	private static BoundField KEY_TOPIC_FIELD = OFFSET_COMMIT_KEY_SCHEMA_V0.get("topic");
	private static BoundField KEY_PARTITION_FIELD = OFFSET_COMMIT_KEY_SCHEMA_V0.get("partition");

	private static Schema OFFSET_COMMIT_VALUE_SCHEMA_V0 = new Schema(new Field("offset", Type.INT64), new Field("metadata", Type.STRING, "Associated metadata.", ""), new Field("timestamp", Type.INT64));

	private static Schema OFFSET_COMMIT_VALUE_SCHEMA_V1 = new Schema(new Field("offset", Type.INT64), new Field("metadata", Type.STRING, "Associated metadata.", ""), new Field("commit_timestamp", Type.INT64),
			new Field("expire_timestamp", Type.INT64));

	private static Schema OFFSET_COMMIT_VALUE_SCHEMA_V2 = new Schema(new Field("offset", Type.INT64), new Field("metadata", Type.STRING, "Associated metadata.", ""), new Field("commit_timestamp", Type.INT64));

	/** GroupMetadataManager . */
	private static Schema OFFSET_COMMIT_VALUE_SCHEMA_V3 = new Schema(new Field("offset", Type.INT64), new Field("leader_epoch", Type.INT32), new Field("metadata", Type.STRING, "Associated metadata.", ""),
			new Field("commit_timestamp", Type.INT64));

	private static BoundField VALUE_OFFSET_FIELD_V0 = OFFSET_COMMIT_VALUE_SCHEMA_V0.get("offset");
	private static BoundField VALUE_METADATA_FIELD_V0 = OFFSET_COMMIT_VALUE_SCHEMA_V0.get("metadata");
	private static BoundField VALUE_TIMESTAMP_FIELD_V0 = OFFSET_COMMIT_VALUE_SCHEMA_V0.get("timestamp");

	private static BoundField VALUE_OFFSET_FIELD_V1 = OFFSET_COMMIT_VALUE_SCHEMA_V1.get("offset");
	private static BoundField VALUE_METADATA_FIELD_V1 = OFFSET_COMMIT_VALUE_SCHEMA_V1.get("metadata");
	private static BoundField VALUE_COMMIT_TIMESTAMP_FIELD_V1 = OFFSET_COMMIT_VALUE_SCHEMA_V1.get("commit_timestamp");
	private static BoundField OFFSET_VALUE_EXPIRE_TIMESTAMP_FIELD_V1 = OFFSET_COMMIT_VALUE_SCHEMA_V1.get("expire_timestamp");

	private static BoundField VALUE_OFFSET_FIELD_V2 = OFFSET_COMMIT_VALUE_SCHEMA_V2.get("offset");
	private static BoundField VALUE_METADATA_FIELD_V2 = OFFSET_COMMIT_VALUE_SCHEMA_V2.get("metadata");
	private static BoundField VALUE_COMMIT_TIMESTAMP_FIELD_V2 = OFFSET_COMMIT_VALUE_SCHEMA_V2.get("commit_timestamp");

	private static BoundField VALUE_OFFSET_FIELD_V3 = OFFSET_COMMIT_VALUE_SCHEMA_V3.get("offset");
	private static BoundField VALUE_METADATA_FIELD_V3 = OFFSET_COMMIT_VALUE_SCHEMA_V3.get("metadata");
	private static BoundField VALUE_COMMIT_TIMESTAMP_FIELD_V3 = OFFSET_COMMIT_VALUE_SCHEMA_V3.get("commit_timestamp");
	/** ============================ End Filter ========================= */

	/** Kafka offset memory in schema. */
	@SuppressWarnings("serial")
	private static Map<Integer, KeyAndValueSchemasInfo> OFFSET_SCHEMAS = new HashMap<Integer, KeyAndValueSchemasInfo>() {
		{
			KeyAndValueSchemasInfo ks0 = new KeyAndValueSchemasInfo();
			ks0.setKeySchema(OFFSET_COMMIT_KEY_SCHEMA_V0);
			ks0.setValueSchema(OFFSET_COMMIT_VALUE_SCHEMA_V0);
			put(0, ks0);

			KeyAndValueSchemasInfo ks1 = new KeyAndValueSchemasInfo();
			ks1.setKeySchema(OFFSET_COMMIT_KEY_SCHEMA_V0);
			ks1.setValueSchema(OFFSET_COMMIT_VALUE_SCHEMA_V1);
			put(1, ks1);

			KeyAndValueSchemasInfo ks2 = new KeyAndValueSchemasInfo();
			ks2.setKeySchema(OFFSET_COMMIT_KEY_SCHEMA_V0);
			ks2.setValueSchema(OFFSET_COMMIT_VALUE_SCHEMA_V2);
			put(2, ks2);

			/** Fixed by kafka-2.1.0 version. */
			KeyAndValueSchemasInfo ks3 = new KeyAndValueSchemasInfo();
			ks3.setKeySchema(OFFSET_COMMIT_KEY_SCHEMA_V0);
			ks3.setValueSchema(OFFSET_COMMIT_VALUE_SCHEMA_V3);
			put(3, ks3);
		}
	};
	
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
					BaseKey key = GroupMetadataManager.readMessageKey(buffer);
					//OffsetAndMetadata gm = readOffsetMessageValue(buffer);
					System.out.println(key);
				}
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}
	
	private static BaseKey readMessageKey(ByteBuffer buffer) {
		short version = buffer.getShort();
		Schema keySchema = schemaFor(version).getKeySchema();
		Struct key = null;
		// OffsetKey 
		if(version<=1) {
			String group = key.getString(KEY_GROUP_FIELD);
			String topic = key.getString(KEY_TOPIC_FIELD);
			int partition = key.getInt(KEY_PARTITION_FIELD);
			return new OffsetKey(version, new GroupTopicPartition(group, new TopicPartition(topic, partition)));
		}else if(version==2) {
			return  null;
		}
		
		try {
			key = (Struct) keySchema.read(buffer);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("error version: " + version);
		}

		return null;
	}
	
	private static OffsetAndMetadata readOffsetMessageValue(ByteBuffer buffer) {
		MessageValueStructAndVersionInfo structAndVersion = readMessageValueStruct(buffer);
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
				long expireTimestamp = structAndVersion.getValue().getLong(OFFSET_VALUE_EXPIRE_TIMESTAMP_FIELD_V1);
				return new OffsetAndMetadata(new OffsetMetadata(offset, metadata), commitTimestamp, expireTimestamp);
			} else if (structAndVersion.getVersion() == 2) {
				long offset = structAndVersion.getValue().getLong(VALUE_OFFSET_FIELD_V2);
				String metadata = structAndVersion.getValue().getString(VALUE_METADATA_FIELD_V2);
				long commitTimestamp = structAndVersion.getValue().getLong(VALUE_COMMIT_TIMESTAMP_FIELD_V2);
				return new OffsetAndMetadata(new OffsetMetadata(offset, metadata), commitTimestamp, commitTimestamp);
			} else if (structAndVersion.getVersion() == 3) {
				long offset = structAndVersion.getValue().getLong(VALUE_OFFSET_FIELD_V3);
				String metadata = structAndVersion.getValue().getString(VALUE_METADATA_FIELD_V3);
				long commitTimestamp = structAndVersion.getValue().getLong(VALUE_COMMIT_TIMESTAMP_FIELD_V3);
				return new OffsetAndMetadata(new OffsetMetadata(offset, metadata), commitTimestamp, commitTimestamp);
			} else {
				throw new IllegalStateException("Unknown offset message version: " + structAndVersion.getVersion());
			}
		}
	}

	/** Analysis of struct data structure in metadata in Kafka. */
	private static MessageValueStructAndVersionInfo readMessageValueStruct(ByteBuffer buffer) {
		MessageValueStructAndVersionInfo mvs = new MessageValueStructAndVersionInfo();
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
	
	/** Get instance K&V schema. */
	private static KeyAndValueSchemasInfo schemaFor(int version) {
		return OFFSET_SCHEMAS.get(version);
	}
}
