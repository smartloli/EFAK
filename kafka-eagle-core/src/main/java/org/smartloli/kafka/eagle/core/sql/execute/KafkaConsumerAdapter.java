/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.core.sql.execute;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.smartloli.kafka.eagle.common.protocol.KafkaSqlInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.Kafka;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerFactory;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerService;
import org.smartloli.kafka.eagle.core.sql.schema.TopicSchema;

import java.time.Duration;
import java.util.*;

/**
 * Parse the sql statement, and execute the sql content, get the message record
 * of kafka in topic, and map to sql tree to query operation.
 *
 * @author smartloli.
 * <p>
 * Created by Jun 23, 2017
 */
public class KafkaConsumerAdapter {

    private static KafkaService kafkaService = new KafkaFactory().create();
    private static BrokerService brokerService = new BrokerFactory().create();

    private KafkaConsumerAdapter() {

    }

    /**
     * Executor ksql query topic data.
     */
    public static List<JSONArray> executor(KafkaSqlInfo kafkaSql) {
        List<JSONArray> messages = new ArrayList<>();
        Properties props = new Properties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Kafka.KAFKA_EAGLE_SYSTEM_GROUP);
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaService.getKafkaBrokerServer(kafkaSql.getClusterAlias()));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, Kafka.EARLIEST);
        if (SystemConfigUtils.getBooleanProperty(kafkaSql.getClusterAlias() + ".kafka.eagle.sasl.enable")) {
            kafkaService.sasl(props, kafkaSql.getClusterAlias());
        }
        if (SystemConfigUtils.getBooleanProperty(kafkaSql.getClusterAlias() + ".kafka.eagle.ssl.enable")) {
            kafkaService.ssl(props, kafkaSql.getClusterAlias());
        }
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        List<TopicPartition> topics = new ArrayList<>();
        if (kafkaSql.getPartition().contains(Kafka.ALL_PARTITION)) {
            long partitions = brokerService.partitionNumbers(kafkaSql.getClusterAlias(), kafkaSql.getTableName());
            String partitionStr = "(";
            for (int partition = 0; partition < partitions; partition++) {
                TopicPartition tp = new TopicPartition(kafkaSql.getTableName(), partition);
                topics.add(tp);
                partitionStr += partition + ",";
            }
            partitionStr = partitionStr.substring(0, partitionStr.length() - 1) + ")";
            kafkaSql.setSql(kafkaSql.getSql().replace("(" + Kafka.ALL_PARTITION + ")", partitionStr));
        } else {
            for (Integer partition : kafkaSql.getPartition()) {
                TopicPartition tp = new TopicPartition(kafkaSql.getTableName(), partition);
                topics.add(tp);
            }
        }
        consumer.assign(topics);

        for (TopicPartition tp : topics) {
            Map<TopicPartition, Long> offsets = consumer.endOffsets(Collections.singleton(tp));
            long position = Kafka.POSITION;
            if (offsets.get(tp).longValue() > position) {
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
                JSONObject object = new JSONObject(new LinkedHashMap<>());
                object.put(TopicSchema.PARTITION, record.partition());
                object.put(TopicSchema.OFFSET, record.offset());
                object.put(TopicSchema.MSG, record.value());
                object.put(TopicSchema.TIMESPAN, record.timestamp());
                object.put(TopicSchema.DATE, CalendarUtils.convertUnixTime(record.timestamp()));
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

    /**
     * Get preview topic message.
     */
    public static List<JSONArray> preview(KafkaSqlInfo kafkaSql) {
        List<JSONArray> messages = new ArrayList<>();
        Properties props = new Properties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Kafka.KAFKA_EAGLE_SYSTEM_GROUP);
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaService.getKafkaBrokerServer(kafkaSql.getClusterAlias()));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, Kafka.EARLIEST);
        if (SystemConfigUtils.getBooleanProperty(kafkaSql.getClusterAlias() + ".kafka.eagle.sasl.enable")) {
            kafkaService.sasl(props, kafkaSql.getClusterAlias());
        }
        if (SystemConfigUtils.getBooleanProperty(kafkaSql.getClusterAlias() + ".kafka.eagle.ssl.enable")) {
            kafkaService.ssl(props, kafkaSql.getClusterAlias());
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
            long position = Kafka.PREVIEW;
            if (offsets.get(tp).longValue() > position) {
                consumer.seek(tp, offsets.get(tp).longValue() - Kafka.PREVIEW);
            } else {
                consumer.seek(tp, 0);
            }
        }
        JSONArray datasets = new JSONArray();
        boolean flag = true;
        while (flag) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(Kafka.TIME_OUT));
            for (ConsumerRecord<String, String> record : records) {
                JSONObject object = new JSONObject(new LinkedHashMap<>());
                object.put(TopicSchema.PARTITION, record.partition());
                object.put(TopicSchema.OFFSET, record.offset());
                object.put(TopicSchema.MSG, record.value());
                object.put(TopicSchema.TIMESPAN, record.timestamp());
                object.put(TopicSchema.DATE, CalendarUtils.convertUnixTime(record.timestamp()));
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
