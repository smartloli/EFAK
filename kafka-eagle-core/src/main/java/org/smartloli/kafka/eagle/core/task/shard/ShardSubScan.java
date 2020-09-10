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
package org.smartloli.kafka.eagle.core.task.shard;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.protocol.KafkaSqlInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerFactory;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerService;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * The sub task is sharding to run the cpu on the single node.
 *
 * @author smartloli.
 * <p>
 * Created by Sep 10, 2020
 */
public class ShardSubScan {

    private static KafkaService kafkaService = new KafkaFactory().create();
    private static BrokerService brokerService = new BrokerFactory().create();

    private class SubScanTask extends RecursiveTask<List<JSONArray>> {

        private final Logger LOG = LoggerFactory.getLogger(SubScanTask.class);
        private static final int THRESHOLD = 20;

        private KafkaSqlInfo kafkaSql;
        private long start;
        private long end;

        public SubScanTask(KafkaSqlInfo kafkaSql, long start, long end) {
            this.kafkaSql = kafkaSql;
            this.start = start;
            this.end = end;
        }

        private List<JSONArray> submit() {
            LOG.info("Sharding = ∑(" + start + "~" + end + ")");
            return executor(kafkaSql, start, end);
        }

        @Override
        protected List<JSONArray> compute() {
            List<JSONArray> msg = new ArrayList<>();
            if ((end - start) <= THRESHOLD) {
                return submit();
            } else {
                long middle = (start + end) / 2;
                SubScanTask left = new SubScanTask(kafkaSql, start, middle - 1);
                SubScanTask right = new SubScanTask(kafkaSql, middle, end - 1);
                invokeAll(left, right);
                msg.addAll(left.join());
                msg.addAll(right.join());
                return msg;
            }
        }

        private List<JSONArray> executor(KafkaSqlInfo kafkaSql, long start, long end) {
            List<JSONArray> messages = new ArrayList<>();
            Properties props = new Properties();
            props.put(ConsumerConfig.GROUP_ID_CONFIG, KConstants.Kafka.KAFKA_EAGLE_SYSTEM_GROUP);
            props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaService.getKafkaBrokerServer(kafkaSql.getClusterAlias()));
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
            if (SystemConfigUtils.getBooleanProperty("kafka.eagle.sql.fix.error")) {
                props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, KConstants.Kafka.EARLIEST);
            }
            if (SystemConfigUtils.getBooleanProperty(kafkaSql.getClusterAlias() + ".kafka.eagle.sasl.enable")) {
                kafkaService.sasl(props, kafkaSql.getClusterAlias());
            }
            if (SystemConfigUtils.getBooleanProperty(kafkaSql.getClusterAlias() + ".kafka.eagle.ssl.enable")) {
                kafkaService.ssl(props, kafkaSql.getClusterAlias());
            }
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
            List<TopicPartition> topics = new ArrayList<>();
            if (kafkaSql.getPartition().contains(KConstants.Kafka.ALL_PARTITION)) {
                long partitions = brokerService.partitionNumbers(kafkaSql.getClusterAlias(), kafkaSql.getTableName());
                String partitionStr = "(";
                for (int partition = 0; partition < partitions; partition++) {
                    TopicPartition tp = new TopicPartition(kafkaSql.getTableName(), partition);
                    topics.add(tp);
                    partitionStr += partition + ",";
                }
                partitionStr = partitionStr.substring(0, partitionStr.length() - 1) + ")";
                kafkaSql.setSql(kafkaSql.getSql().replace("(" + KConstants.Kafka.ALL_PARTITION + ")", partitionStr));
            } else {
                for (Integer partition : kafkaSql.getPartition()) {
                    TopicPartition tp = new TopicPartition(kafkaSql.getTableName(), partition);
                    topics.add(tp);
                }
            }
            consumer.assign(topics);

            for (TopicPartition tp : topics) {
                // (topicName,partitionId) -> (start,end)
                consumer.seek(tp, start);
            }
            JSONArray datasets = new JSONArray();
            boolean flag = true;
            long counter = 0;
            while (flag) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10000));
                for (ConsumerRecord<String, String> record : records) {
                    counter++;
                    JSONObject object = new JSONObject(new LinkedHashMap<>());
                    object.put(org.smartloli.kafka.eagle.core.sql.schema.TopicSchema.PARTITION, record.partition());
                    object.put(org.smartloli.kafka.eagle.core.sql.schema.TopicSchema.OFFSET, record.offset());
                    object.put(org.smartloli.kafka.eagle.core.sql.schema.TopicSchema.MSG, record.value());
                    object.put(org.smartloli.kafka.eagle.core.sql.schema.TopicSchema.TIMESPAN, record.timestamp());
                    object.put(org.smartloli.kafka.eagle.core.sql.schema.TopicSchema.DATE, CalendarUtils.convertUnixTime(record.timestamp()));
                    datasets.add(object);
                    if (counter == (end - start)) {
                        flag = false;
                        break;
                    }
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

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        KafkaSqlInfo ksql = new KafkaSqlInfo();
        ksql.setClusterAlias("cluster1");
        ksql.setPartition(Arrays.asList(1));
        ksql.setTableName("kjson");
        ksql.setTopic("kjson");
        // Add
        // (topicName,partitionId) -> (start,end)
        List<JSONArray> results = queryTopicData(ksql, 0, 2);
        System.out.println(results);
    }

    public static List<JSONArray> queryTopicData(KafkaSqlInfo kafkaSql, long start, long end) {
        ForkJoinPool pool = new ForkJoinPool();
        ForkJoinTask<List<JSONArray>> result = pool.submit(new ShardSubScan().new SubScanTask(kafkaSql, start, end));
        pool.shutdown();
        return result.invoke();
    }
}
