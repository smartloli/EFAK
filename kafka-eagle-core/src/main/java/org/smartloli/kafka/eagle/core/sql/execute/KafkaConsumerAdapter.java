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
package org.smartloli.kafka.eagle.core.sql.execute;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.sql.schema.TopicSchema;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.smartloli.kafka.eagle.common.protocol.KafkaSqlInfo;
import org.smartloli.kafka.eagle.common.util.KConstants.Kafka;

/**
 * Parse the sql statement, and execute the sql content, get the message record
 * of kafka in topic, and map to sql tree to query operation.
 * 
 * @author smartloli.
 *
 *         Created by Jun 23, 2017
 */
public class KafkaConsumerAdapter {

	private static KafkaService kafkaService = new KafkaFactory().create();

	public static List<JSONArray> executor(KafkaSqlInfo kafkaSql) {
		List<JSONArray> messages = new ArrayList<>();
		Properties props = new Properties();
		props.put(ConsumerConfig.GROUP_ID_CONFIG, Kafka.KAFKA_EAGLE_SYSTEM_GROUP);
		props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaService.getKafkaBrokerServer(kafkaSql.getClusterAlias()));
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		if (SystemConfigUtils.getBooleanProperty("kafka.eagle.sql.fix.error")) {
			props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, Kafka.EARLIEST);
		}
		if (SystemConfigUtils.getBooleanProperty("kafka.eagle.sasl.enable")) {
			props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SystemConfigUtils.getProperty("kafka.eagle.sasl.protocol"));
			props.put(SaslConfigs.SASL_MECHANISM, SystemConfigUtils.getProperty("kafka.eagle.sasl.mechanism"));
		}
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
		List<TopicPartition> topics = new ArrayList<>();
		for (Integer partition : kafkaSql.getPartition()) {
			TopicPartition tp = new TopicPartition(kafkaSql.getTableName(), partition);
			topics.add(tp);
		}
		consumer.assign(topics);

		for (TopicPartition tp : topics) {
			Map<TopicPartition, Long> offsets = consumer.endOffsets(Collections.singleton(tp));
			if (offsets.get(tp).longValue() > Kafka.POSITION) {
				consumer.seek(tp, offsets.get(tp).longValue() - Kafka.POSITION);
			} else {
				consumer.seek(tp, 0);
			}
		}
		JSONArray datasets = new JSONArray();
		boolean flag = true;
		while (flag) {
			ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(Kafka.TIME_OUT));
			for (ConsumerRecord<String, String> record : records) {
				JSONObject object = new JSONObject();
				object.put(TopicSchema.MSG, record.value());
				object.put(TopicSchema.OFFSET, record.offset());
				object.put(TopicSchema.PARTITION, record.partition());
				datasets.add(object);
			}
			if (records.isEmpty()) {
				flag = false;
			}
		}
		consumer.close();
		messages.add(datasets);
		return messages;
	}

}
