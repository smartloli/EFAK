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
import java.util.*;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.protocol.KafkaSqlInfo;
import org.smartloli.kafka.eagle.common.util.KConstants.Kafka;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;

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

	private static final Logger log = LoggerFactory.getLogger(KafkaConsumerAdapter.class);

	private KafkaConsumerAdapter() {

	}

	/**
	 * Executor ksql query topic data.
	 */
	public static ConcurrentMap<String, List<List<String>>> executor(KafkaSqlInfo kafkaSql) {
		ConcurrentMap<String, List<List<String>>> messages = Maps.newConcurrentMap();
		Properties props = new Properties();
		props.put(ConsumerConfig.GROUP_ID_CONFIG, Kafka.KAFKA_EAGLE_SYSTEM_GROUP);
		props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaService.getKafkaBrokerServer(kafkaSql.getClusterAlias()));
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		if (SystemConfigUtils.getBooleanProperty("kafka.eagle.sql.fix.error")) {
			props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, Kafka.EARLIEST);
		}
		if (SystemConfigUtils.getBooleanProperty(kafkaSql.getClusterAlias() + ".kafka.eagle.sasl.enable")) {
			kafkaService.sasl(props, kafkaSql.getClusterAlias());
		}
		if (SystemConfigUtils.getBooleanProperty(kafkaSql.getClusterAlias() + ".kafka.eagle.ssl.enable")) {
			kafkaService.ssl(props, kafkaSql.getClusterAlias());
		}

		Set<String> tableNames = kafkaSql.getTableName();
		tableNames.parallelStream().forEach(tableName -> {
			KafkaConsumer<String, String> consumer = null;
			try {
				consumer = new KafkaConsumer<>(props);
				List<PartitionInfo> partitions = Optional.ofNullable(consumer.listTopics().get(tableName))
						.orElseThrow(() -> new NullPointerException(tableName + " not exist"));

				List<TopicPartition> topics = new ArrayList<>();
				for (PartitionInfo partition : partitions) {
					TopicPartition tp = new TopicPartition(tableName, partition.partition());
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

				List<List<String>> datasets = Lists.newLinkedList();
				boolean flag = true;
				while (flag) {
					ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(Kafka.TIME_OUT));
					for (ConsumerRecord<String, String> record : records) {
						List<String> object = Lists.newLinkedList();
						/**
						 * 这边注意顺序，这个很重要，按字母顺序添加元素
						 */
						object.add(record.value());
						object.add(String.valueOf(record.timestamp()));
						object.add(String.valueOf(record.offset()));
						object.add(String.valueOf(record.partition()));
						datasets.add(object);
					}
					if (records.isEmpty()) {
						flag = false;
					}
					Collections.sort(datasets, (list1, list2) -> ComparisonChain.start()
							.compare(list1.get(1), list2.get(1))
							.result());
				}
				messages.put(tableName, datasets);
			} catch (Exception e) {
				log.error(ExceptionUtils.getStackTrace(e));
				messages.put(tableName, Collections.emptyList());
			} finally {
				if (consumer != null) {
					consumer.close();
				}
			}
		});
		return messages;
	}

}
