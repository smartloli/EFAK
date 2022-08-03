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
package org.smartloli.kafka.eagle.common.util.kraft;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.protocol.BrokersInfo;
import org.smartloli.kafka.eagle.common.protocol.MetadataInfo;
import org.smartloli.kafka.eagle.common.protocol.cache.BrokerCache;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.KafkaCacheUtils;
import org.smartloli.kafka.eagle.common.util.LoggerUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;

import java.util.*;

/**
 * Get the Kafka topic metadata information through the broker address.
 *
 * @author smartloli.
 * <p>
 * Created by Oct 07, 2021
 */
public class KafkaSchemaFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSchemaFactory.class);
    private final KafkaStoragePlugin plugin;
    private Set<String> tableNames;

    public KafkaSchemaFactory(final KafkaStoragePlugin plugin) {
        this.plugin = plugin;
    }

    public Set<String> getTableNames(String clusterAlias) {
        if (tableNames == null) {
            KafkaConsumer<?, ?> kafkaConsumer = null;
            try {
                kafkaConsumer = new KafkaConsumer<>(plugin.getKafkaConsumerProps(clusterAlias));
                tableNames = kafkaConsumer.listTopics().keySet();
            } catch (Exception e) {
                LoggerUtils.print(this.getClass()).error("Failure while loading table names for database '{}': {}", clusterAlias, e);
            } finally {
                plugin.registerToClose(kafkaConsumer);
            }
        }
        return tableNames;
    }

    public void getTopicMetaData(String clusterAlias, List<String> topics) {
        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(plugin.getKafkaAdminClientProps(clusterAlias));
            DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(topics);
            System.out.println(describeTopicsResult.all().get());
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Failure while loading topic meta for kafka '{}': {}", clusterAlias, e);
        } finally {
            plugin.registerToClose(adminClient);
        }
    }

    public List<String> getTopicPartitionsOfString(String clusterAlias, String topic) {
        List<String> partitions = new ArrayList<>();
        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(plugin.getKafkaAdminClientProps(clusterAlias));
            DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Arrays.asList(topic));
            for (TopicPartitionInfo tp : describeTopicsResult.all().get().get(topic).partitions()) {
                partitions.add(String.valueOf(tp.partition()));
            }
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Failure while loading topic '{}' meta for kafka '{}': {}", topic, clusterAlias, e);
        } finally {
            plugin.registerToClose(adminClient);
        }
        return partitions;
    }

    public Set<Integer> getTopicPartitionsOfInt(String clusterAlias, String topic) {
        Set<Integer> partitions = new HashSet<>();
        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(plugin.getKafkaAdminClientProps(clusterAlias));
            DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Arrays.asList(topic));
            for (TopicPartitionInfo tp : describeTopicsResult.all().get().get(topic).partitions()) {
                partitions.add(tp.partition());
            }
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Failure while loading topic '{}' meta for kafka '{}': {}", topic, clusterAlias, e);
        } finally {
            plugin.registerToClose(adminClient);
        }
        return partitions;
    }

    public List<MetadataInfo> getTopicPartitionsLeader(String clusterAlias, String topic) {
        List<MetadataInfo> partitions = new ArrayList<>();
        AdminClient adminClient = null;
        try {
            adminClient = AdminClient.create(plugin.getKafkaAdminClientProps(clusterAlias));
            DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Arrays.asList(topic));
            for (TopicPartitionInfo tp : describeTopicsResult.all().get().get(topic).partitions()) {
                MetadataInfo metadata = new MetadataInfo();
                metadata.setPartitionId(tp.partition());
                metadata.setLeader(tp.leader().id());
                partitions.add(metadata);
            }
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Failure while loading topic '{}' meta for kafka '{}': {}", topic, clusterAlias, e);
        } finally {
            plugin.registerToClose(adminClient);
        }
        return partitions;
    }

    public String getBrokerJMXFromIds(String clusterAlias, int ids) {
        String jni = "";
        try {
            String env = SystemConfigUtils.getProperty("efak.runtime.env", KConstants.EFAK.RUNTIME_ENV_PRD);
            if (env.equals(KConstants.EFAK.RUNTIME_ENV_DEV)) {
                KafkaCacheUtils.initKafkaMetaData();
            }

            for (BrokersInfo brokersInfo : BrokerCache.META_CACHE.get(clusterAlias)) {
                if (String.valueOf(ids).equals(brokersInfo.getIds())) {
                    jni = brokersInfo.getHost() + ":" + brokersInfo.getJmxPort();
                    break;
                }
            }
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Failure while loading meta for kafka '{}': {}", clusterAlias, e);
        }
        return jni;
    }

    public static void main(String[] args) {
        KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());
        String jni = ksf.getBrokerJMXFromIds("cluster1", 0);
        System.out.println(jni);
    }
}
