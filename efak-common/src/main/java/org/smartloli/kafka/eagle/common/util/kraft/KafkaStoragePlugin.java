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

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.protocol.BrokersInfo;
import org.smartloli.kafka.eagle.common.protocol.cache.BrokerCache;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.KafkaPartitioner;
import org.smartloli.kafka.eagle.common.util.StrUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;

import java.util.List;
import java.util.Properties;

/**
 * Operation Kafka configuration file and asynchronous thread closing method.
 *
 * @author smartloli.
 * <p>
 * Created by Oct 07, 2021
 */
public class KafkaStoragePlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStoragePlugin.class);

    private final KafkaAsyncCloser closer;
    private final Properties props;


    public KafkaStoragePlugin() {
        this.props = new Properties();
        this.closer = new KafkaAsyncCloser();
    }

    private String parseBrokerServer(String clusterAlias) {
        String brokerServer = "";
        List<BrokersInfo> brokers = BrokerCache.META_CACHE.get(clusterAlias);
        for (BrokersInfo broker : brokers) {
            brokerServer += broker.getHost() + ":" + broker.getPort() + ",";
        }
        if ("".equals(brokerServer)) {
            return "";
        }
        return brokerServer.substring(0, brokerServer.length() - 1);
    }

    public Properties getKafkaConsumerProps(String clusterAlias) {
        String brokers = SystemConfigUtils.getProperty(clusterAlias + ".efak.bootstrap.servers");
        if (StrUtils.isNull(brokers)) {
            brokers = parseBrokerServer(clusterAlias);
        }
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KConstants.Kafka.EFAK_SYSTEM_GROUP);
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, brokers);
        String key = SystemConfigUtils.getProperty(clusterAlias + ".efak.consumer.key.deserializer");
        String value = SystemConfigUtils.getProperty(clusterAlias + ".efak.consumer.value.deserializer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, key == null ? StringDeserializer.class.getCanonicalName() : key);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, value == null ? StringDeserializer.class.getCanonicalName() : value);

        if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".efak.sasl.enable")) {
            this.sasl(props, clusterAlias);
        }
        if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".efak.ssl.enable")) {
            this.ssl(props, clusterAlias);
        }

        return props;
    }

    /**
     * Set kafka broker sasl.
     */
    private void sasl(Properties props, String clusterAlias) {
        // configure the following four settings for SSL Encryption
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SystemConfigUtils.getProperty(clusterAlias + ".efak.sasl.protocol"));
        if (!"".equals(SystemConfigUtils.getProperty(clusterAlias + ".efak.sasl.client.id"))) {
            props.put(CommonClientConfigs.CLIENT_ID_CONFIG, SystemConfigUtils.getProperty(clusterAlias + ".efak.sasl.client.id"));
        }
        props.put(SaslConfigs.SASL_MECHANISM, SystemConfigUtils.getProperty(clusterAlias + ".efak.sasl.mechanism"));
        props.put(SaslConfigs.SASL_JAAS_CONFIG, SystemConfigUtils.getProperty(clusterAlias + ".efak.sasl.jaas.config"));
    }

    /**
     * Set kafka broker ssl.
     */
    private void ssl(Properties props, String clusterAlias) {
        // configure the following three settings for SSL Encryption
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SystemConfigUtils.getProperty(clusterAlias + ".efak.ssl.protocol"));
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, SystemConfigUtils.getProperty(clusterAlias + ".efak.ssl.truststore.location"));
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, SystemConfigUtils.getProperty(clusterAlias + ".efak.ssl.truststore.password"));

        // configure the following three settings for SSL Authentication
        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, SystemConfigUtils.getProperty(clusterAlias + ".efak.ssl.keystore.location"));
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, SystemConfigUtils.getProperty(clusterAlias + ".efak.ssl.keystore.password"));
        props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, SystemConfigUtils.getProperty(clusterAlias + ".efak.ssl.key.password"));

        // ssl handshake failed
        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, SystemConfigUtils.getProperty(clusterAlias + ".efak.ssl.endpoint.identification.algorithm"));

    }

    public Properties getKafkaProducerProps(String clusterAlias) {
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, SystemConfigUtils.getProperty(clusterAlias + ".efak.bootstrap.servers"));
        String key = SystemConfigUtils.getProperty(clusterAlias + ".efak.producer.key.serializer");
        String value = SystemConfigUtils.getProperty(clusterAlias + ".efak.producer.value.serializer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, key == null ? StringSerializer.class.getCanonicalName() : key);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, value == null ? StringSerializer.class.getCanonicalName() : value);
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, KafkaPartitioner.class.getName());

        if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".efak.sasl.enable")) {
            sasl(props, clusterAlias);
        }
        if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".efak.ssl.enable")) {
            ssl(props, clusterAlias);
        }
        return props;
    }

    public void registerToClose(AutoCloseable autoCloseable) {
        this.closer.close(autoCloseable);
    }
}
