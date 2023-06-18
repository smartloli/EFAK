/**
 * KafkaStoragePlugin.java
 * <p>
 * Copyright 2023 smartloli
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kafka.eagle.core.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.kafka.eagle.common.constants.KConstants;
import org.kafka.eagle.common.utils.KafkaPartitionerUtil;
import org.kafka.eagle.common.utils.StrUtils;
import org.kafka.eagle.pojo.cluster.KafkaClientInfo;

import java.util.Properties;

/**
 * Operation Kafka configuration file and asynchronous thread closing method.
 *
 * @Author: smartloli
 * @Date: 2023/6/18 19:57
 * @Version: 3.4.0
 */
@Slf4j
public class KafkaStoragePlugin {
    private final KafkaAsyncCloser closer;
    private final Properties props;


    public KafkaStoragePlugin() {
        this.props = new Properties();
        this.closer = new KafkaAsyncCloser();
    }

    public Properties getKafkaAdminClientProps(KafkaClientInfo kafkaClientInfo) {

        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaClientInfo.getBrokerServer());

        if (kafkaClientInfo.isSasl()) {
            sasl(props, kafkaClientInfo);
        }
        if (kafkaClientInfo.isSsl()) {
            ssl(props, kafkaClientInfo);
        }
        return props;
    }

    public Properties getKafkaConsumerProps(KafkaClientInfo kafkaClientInfo) {
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KConstants.Kafka.EFAK_SYSTEM_GROUP);
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaClientInfo.getBrokerServer());
        String key = kafkaClientInfo.getKeyDeserializer();
        String value = kafkaClientInfo.getValueDeserializer();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, key == null ? StringDeserializer.class.getCanonicalName() : key);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, value == null ? StringDeserializer.class.getCanonicalName() : value);

        if (kafkaClientInfo.isSasl()) {
            this.sasl(props, kafkaClientInfo);
        }
        if (kafkaClientInfo.isSsl()) {
            this.ssl(props, kafkaClientInfo);
        }

        return props;
    }

    /**
     * Set kafka broker sasl.
     */
    private void sasl(Properties props, KafkaClientInfo kafkaClientInfo) {
        // configure the following four settings for SSL Encryption
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, kafkaClientInfo.getSaslProtocol());
        if (!StrUtils.isNull(kafkaClientInfo.getSaslClientId())) {
            props.put(CommonClientConfigs.CLIENT_ID_CONFIG, kafkaClientInfo.getSaslClientId());
        }
        props.put(SaslConfigs.SASL_MECHANISM, kafkaClientInfo.getSaslMechanism());
        props.put(SaslConfigs.SASL_JAAS_CONFIG, kafkaClientInfo.getSaslJaasConfig());
    }

    /**
     * Set kafka broker ssl.
     */
    private void ssl(Properties props, KafkaClientInfo kafkaClientInfo) {
        // configure the following three settings for SSL Encryption
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, kafkaClientInfo.getSslProtocol());
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, kafkaClientInfo.getSslTruststoreLocation());
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, kafkaClientInfo.getSslTruststorePassword());

        // configure the following three settings for SSL Authentication
        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, kafkaClientInfo.getSslKeystoreLocation());
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, kafkaClientInfo.getSslKeystorePassword());
        props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, kafkaClientInfo.getSslKeyPassword());

        // ssl handshake failed
        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, kafkaClientInfo.getSslAlgorithm());

    }

    public Properties getKafkaProducerProps(KafkaClientInfo kafkaClientInfo) {
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaClientInfo.getBrokerServer());
        String key = kafkaClientInfo.getKeySerializer();
        String value = kafkaClientInfo.getValueSerializer();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, key == null ? StringSerializer.class.getCanonicalName() : key);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, value == null ? StringSerializer.class.getCanonicalName() : value);
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, KafkaPartitionerUtil.class.getName());

        if (kafkaClientInfo.isSasl()) {
            sasl(props, kafkaClientInfo);
        }
        if (kafkaClientInfo.isSsl()) {
            ssl(props, kafkaClientInfo);
        }
        return props;
    }

    public void registerToClose(AutoCloseable autoCloseable) {
        this.closer.close(autoCloseable);
    }
}
