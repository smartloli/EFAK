/**
 * KafkaStoragePlugin.java
 * <p>
 * Copyright 2025 smartloli
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
package org.kafka.eagle.core.api;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.kafka.eagle.core.constant.ClusterMetricsConst;
import org.kafka.eagle.dto.cluster.KafkaClientInfo;

import java.util.Properties;

/**
 * <p>
 * 用于创建客户端配置的 Kafka 存储插件。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/8/23 22:29:19
 * @version 5.0.0
 */
@Slf4j
public class KafkaStoragePlugin {
    private final KafkaAsyncCloser closer;
    private final Properties props;

    public KafkaStoragePlugin() {
        this.props = new Properties();
        this.closer = new KafkaAsyncCloser();
    }

    /* ======================= CLIENT CONFIGURATION ======================= */

    /** 获取 Kafka AdminClient 配置 */
    public Properties buildAdminClientProps(KafkaClientInfo clientInfo) {
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, clientInfo.getBrokerServer());

        if (clientInfo.isSasl()) {
            applySaslConfig(props, clientInfo);
        }
        if (clientInfo.isSsl()) {
            applySslConfig(props, clientInfo);
        }
        return props;
    }

    /** 获取 Kafka Consumer 配置 */
    public Properties buildConsumerProps(KafkaClientInfo clientInfo) {
        props.put(ConsumerConfig.GROUP_ID_CONFIG, ClusterMetricsConst.Cluster.EFAK_SYSTEM_GROUP.key());
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, clientInfo.getBrokerServer());

        String keyDeserializer = clientInfo.getKeyDeserializer();
        String valueDeserializer = clientInfo.getValueDeserializer();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                keyDeserializer == null ? StringDeserializer.class.getCanonicalName() : keyDeserializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                valueDeserializer == null ? StringDeserializer.class.getCanonicalName() : valueDeserializer);

        if (clientInfo.isSasl()) {
            applySaslConfig(props, clientInfo);
        }
        if (clientInfo.isSsl()) {
            applySslConfig(props, clientInfo);
        }

        return props;
    }

    /** 获取 Kafka Producer 配置 */
    public Properties buildProducerProps(KafkaClientInfo clientInfo) {
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, clientInfo.getBrokerServer());

        String keySerializer = clientInfo.getKeySerializer();
        String valueSerializer = clientInfo.getValueSerializer();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                keySerializer == null ? StringSerializer.class.getCanonicalName() : keySerializer);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                valueSerializer == null ? StringSerializer.class.getCanonicalName() : valueSerializer);

        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, KafkaPartitioner.class.getName());

        if (clientInfo.isSasl()) {
            applySaslConfig(props, clientInfo);
        }
        if (clientInfo.isSsl()) {
            applySslConfig(props, clientInfo);
        }

        return props;
    }

    /* ======================= SASL & SSL CONFIG ======================= */

    /** 应用 SASL 身份验证设置 */
    private void applySaslConfig(Properties props, KafkaClientInfo clientInfo) {
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, clientInfo.getSaslProtocol());

        if (StrUtil.isNotBlank(clientInfo.getSaslClientId())) {
            props.put(CommonClientConfigs.CLIENT_ID_CONFIG, clientInfo.getSaslClientId());
        }

        props.put(SaslConfigs.SASL_MECHANISM, clientInfo.getSaslMechanism());
        props.put(SaslConfigs.SASL_JAAS_CONFIG, clientInfo.getSaslJaasConfig());
    }

    /** 应用 SSL 加密设置 */
    private void applySslConfig(Properties props, KafkaClientInfo clientInfo) {
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, clientInfo.getSslProtocol());

        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, clientInfo.getSslTruststoreLocation());
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, clientInfo.getSslTruststorePassword());

        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, clientInfo.getSslKeystoreLocation());
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, clientInfo.getSslKeystorePassword());
        props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, clientInfo.getSslKeyPassword());

        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, clientInfo.getSslAlgorithm());
    }

    /* ======================= RESOURCE MANAGEMENT ======================= */

    /** 注册资源以进行异步关闭 */
    public void registerResourceForClose(AutoCloseable resource) {
        this.closer.close(resource);
    }
}
