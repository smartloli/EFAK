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

import com.alibaba.fastjson.JSON;
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
import org.smartloli.kafka.eagle.common.util.*;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerFactory;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerService;
import org.smartloli.kafka.eagle.core.sql.schema.TopicSchema;
import org.smartloli.kafka.eagle.core.task.cache.LogCacheFactory;
import org.smartloli.kafka.eagle.core.task.strategy.FieldSchemaStrategy;
import org.smartloli.kafka.eagle.core.task.strategy.KSqlStrategy;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
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
        private final int THRESHOLD = SystemConfigUtils.getIntProperty("kafka.eagle.sql.worknode.fetch.threshold");
        private final long TIMEOUT = SystemConfigUtils.getIntProperty("kafka.eagle.sql.worknode.fetch.timeout");

        private KSqlStrategy ksql;
        private String topic;
        private int partition;
        private long start;
        private long end;

        public SubScanTask(KSqlStrategy ksql, long start, long end) {
            this.ksql = ksql;
            this.start = start;
            this.end = end;
            this.topic = ksql.getTopic();
            this.partition = ksql.getPartition();
        }

        private List<JSONArray> submit() {
            LOG.info("WorkNodeServer[" + NetUtils.hostname() + "], Cluster[" + ksql.getCluster() + "], Topic[" + this.topic + "], Partition[" + this.partition + "], Sharding = ∑(" + start + "~" + end + ")");
            return executor(ksql, start, end);
        }

        @Override
        protected List<JSONArray> compute() {
            List<JSONArray> msg = new ArrayList<>();
            if ((end - start) <= THRESHOLD) {
                return submit();
            } else {
                long middle = (start + end) / 2;
                ErrorUtils.print(this.getClass()).info("Split: [" + start + "," + end + "]");
                SubScanTask left = new SubScanTask(ksql, start, middle);
                SubScanTask right = new SubScanTask(ksql, middle, end);
                invokeAll(left, right);
                msg.addAll(left.join());
                msg.addAll(right.join());
                return msg;
            }
        }

        private List<JSONArray> executor(KSqlStrategy ksql, long start, long end) {
            List<JSONArray> messages = new ArrayList<>();
            Properties props = new Properties();
            props.put(ConsumerConfig.GROUP_ID_CONFIG, KConstants.Kafka.KAFKA_EAGLE_SYSTEM_GROUP);
            props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaService.getKafkaBrokerServer(ksql.getCluster()));
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, KConstants.Kafka.EARLIEST);
            if (SystemConfigUtils.getBooleanProperty(ksql.getCluster() + ".kafka.eagle.sasl.enable")) {
                kafkaService.sasl(props, ksql.getCluster());
            }
            if (SystemConfigUtils.getBooleanProperty(ksql.getCluster() + ".kafka.eagle.ssl.enable")) {
                kafkaService.ssl(props, ksql.getCluster());
            }
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
            List<TopicPartition> topics = new ArrayList<>();
            TopicPartition tp = new TopicPartition(ksql.getTopic(), ksql.getPartition());
            topics.add(tp);
            consumer.assign(topics);
            long limit = ksql.getLimit() == 0 ? KConstants.KSQL.LIMIT : ksql.getLimit();
            boolean desc = false;
            for (FieldSchemaStrategy filter : ksql.getFieldSchema()) {
                if (KConstants.KSQL.ORDER_BY.equals(filter.getType())) {
                    if (KConstants.KSQL.ORDER_BY_DESC.equals(filter.getValue())) {
                        desc = true;
                        break;
                    }
                }
            }
            if (desc) {
                consumer.seek(tp, end - limit);
            } else {
                consumer.seek(tp, start);
            }

            JSONArray datasets = new JSONArray();
            boolean flag = true;
            long counter = 0;
            long batchOffset = 0L;
            while (flag) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(TIMEOUT));
                for (ConsumerRecord<String, String> record : records) {
                    counter++;
                    String msg = record.value();
                    JSONObject object = new JSONObject(new LinkedHashMap<>());
                    object.put(org.smartloli.kafka.eagle.core.sql.schema.TopicSchema.PARTITION, record.partition());
                    object.put(org.smartloli.kafka.eagle.core.sql.schema.TopicSchema.OFFSET, record.offset());
                    object.put(org.smartloli.kafka.eagle.core.sql.schema.TopicSchema.MSG, record.value());
                    object.put(org.smartloli.kafka.eagle.core.sql.schema.TopicSchema.TIMESPAN, record.timestamp());
                    object.put(org.smartloli.kafka.eagle.core.sql.schema.TopicSchema.DATE, CalendarUtils.convertUnixTime(record.timestamp()));
                    // record offset
                    batchOffset = record.offset();
                    // filter
                    List<FieldSchemaStrategy> filters = ksql.getFieldSchema();
                    List<Boolean> matchs = new ArrayList<>();
                    for (FieldSchemaStrategy filter : filters) {
                        String key = filter.getKey();
                        String value = filter.getValue();
                        if (filter.isJsonUdf()) {// sql include json object
                            filterJSONObject(msg, filter, matchs);
                        } else if (filter.isJsonsUdf()) {// sql include json array
                            filterJSONArray(msg, filter, matchs);
                        } else {// sql include text
                            filterText(msg, filter, matchs);
                        }
                        filterTimeSpan(msg, filter, matchs, record.timestamp());
                    }
                    if (matchs.size() == filters.size()) {
                        datasets.add(object);
                    }
                    if (counter == limit) {
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
            LOG.info("ShardSubScan: " + messages.toString());
            LOG.info(this.ksql.getJobId() + ", [BatchOffset: " + batchOffset + "], [Progress:" + MathUtils.percent(batchOffset, this.ksql.getEnd()) + "%]");
            try {
                String lastestLog = CalendarUtils.getDate() + " INFO [WorkNodeServer-" + NetUtils.hostname() + "], Cluster[" + ksql.getCluster() + "], Topic[" + ksql.getTopic() + "], Partition[" + ksql.getPartition() + "], Sharding = ∑(" + start + "~" + end + ") finished.";
                if (LogCacheFactory.LOG_RECORDS.containsKey(ksql.getJobId())) {
                    String earliestLog = LogCacheFactory.LOG_RECORDS.get(ksql.getJobId()).toString();
                    LogCacheFactory.LOG_RECORDS.put(ksql.getJobId(), earliestLog + "\n" + lastestLog);
                } else {
                    LogCacheFactory.LOG_RECORDS.put(ksql.getJobId(), lastestLog);
                }
            } catch (Exception e) {
                ErrorUtils.print(this.getClass()).error("Store shard sub scan task log has error, msg is ", e);
            }
            return messages;
        }

        private void filterTimeSpan(String msg, FieldSchemaStrategy filter, List<Boolean> matchs, long timestamp) {
            if (KConstants.KSQL.ORDER_BY.equals(filter.getType())) {
                matchs.add(true);// order by default match one record
            }
            if (TopicSchema.TIMESPAN.equals(filter.getKey()) && !KConstants.KSQL.ORDER_BY.equals(filter.getType())) {
                if (KConstants.KSQL.GT.equals(filter.getType())) {
                    if (timestamp > Long.parseLong(filter.getValue())) {
                        matchs.add(true);
                    }
                } else if (KConstants.KSQL.GE.equals(filter.getType())) {
                    if (timestamp >= Long.parseLong(filter.getValue())) {
                        matchs.add(true);
                    }
                } else if (KConstants.KSQL.EQ.equals(filter.getType())) {
                    if (timestamp == Long.parseLong(filter.getValue())) {
                        matchs.add(true);
                    }
                } else if (KConstants.KSQL.LT.equals(filter.getType())) {
                    if (timestamp < Long.parseLong(filter.getValue())) {
                        matchs.add(true);
                    }
                } else if (KConstants.KSQL.LE.equals(filter.getType())) {
                    if (timestamp <= Long.parseLong(filter.getValue())) {
                        matchs.add(true);
                    }
                }
            }
        }

        private void filterJSONObject(String msg, FieldSchemaStrategy filter, List<Boolean> matchs) {
            if (KConstants.KSQL.LIKE.equals(filter.getType())) {
                if (JSON.parseObject(msg).getString(filter.getKey()).contains(filter.getValue())) {
                    matchs.add(true);
                }
            } else if (KConstants.KSQL.EQ.equals(filter.getType())) {
                if (JSON.parseObject(msg).getString(filter.getKey()).equals(filter.getValue())) {
                    matchs.add(true);
                }
            }
        }

        private void filterText(String msg, FieldSchemaStrategy filter, List<Boolean> matchs) {
            if (KConstants.KSQL.LIKE.equals(filter.getType())) {
                if (msg.contains(filter.getValue())) {
                    matchs.add(true);
                }
            } else if (KConstants.KSQL.EQ.equals(filter.getType())) {
                if (msg.equals(filter.getValue())) {
                    matchs.add(true);
                }
            }
        }

        private void filterJSONArray(String msg, FieldSchemaStrategy filter, List<Boolean> matchs) {
            if (KConstants.KSQL.LIKE.equals(filter.getType())) {
                for (Object obj : JSON.parseArray(msg)) {
                    JSONObject json = (JSONObject) obj;
                    if (json.getString(filter.getKey()).contains(filter.getValue())) {
                        matchs.add(true);
                        break;
                    }
                }
            } else if (KConstants.KSQL.EQ.equals(filter.getType())) {
                for (Object obj : JSON.parseArray(msg)) {
                    JSONObject json = (JSONObject) obj;
                    if (json.getString(filter.getKey()).equals(filter.getValue())) {
                        matchs.add(true);
                        break;
                    }
                }
            }
        }

    }

    public static List<JSONArray> query(KSqlStrategy ksql) {
        ForkJoinPool pool = new ForkJoinPool();
        ForkJoinTask<List<JSONArray>> result = pool.submit(new ShardSubScan().new SubScanTask(ksql, ksql.getStart(), ksql.getEnd()));
        pool.shutdown();
        return result.invoke();
    }
}
