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

    public KafkaStoragePlugin() {
        this.closer = new KafkaAsyncCloser();
    }

    /* ======================= CLIENT CONFIGURATION ======================= */

    /** 获取 Kafka AdminClient 配置 */
    public Properties buildAdminClientProps(KafkaClientInfo clientInfo) {
        Properties props = new Properties();
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, clientInfo.getBrokerServer());

        // 关键逻辑：每次构建都用新的 Properties，避免并发请求/多集群互相污染配置
        applySecurityConfig(props, clientInfo);
        return props;
    }

    /** 获取 Kafka Consumer 配置 */
    public Properties buildConsumerProps(KafkaClientInfo clientInfo) {
        Properties props = new Properties();
        // 关键逻辑：不设置 group.id，避免在开启 ACL 时触发 ConsumerGroup 权限校验导致“无结果”
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, clientInfo.getBrokerServer());

        String keyDeserializer = clientInfo.getKeyDeserializer();
        String valueDeserializer = clientInfo.getValueDeserializer();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                keyDeserializer == null ? StringDeserializer.class.getCanonicalName() : keyDeserializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                valueDeserializer == null ? StringDeserializer.class.getCanonicalName() : valueDeserializer);

        applySecurityConfig(props, clientInfo);
        return props;
    }

    /** 获取 Kafka Producer 配置 */
    public Properties buildProducerProps(KafkaClientInfo clientInfo) {
        Properties props = new Properties();
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, clientInfo.getBrokerServer());

        String keySerializer = clientInfo.getKeySerializer();
        String valueSerializer = clientInfo.getValueSerializer();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                keySerializer == null ? StringSerializer.class.getCanonicalName() : keySerializer);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                valueSerializer == null ? StringSerializer.class.getCanonicalName() : valueSerializer);

        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, KafkaPartitioner.class.getName());

        applySecurityConfig(props, clientInfo);
        return props;
    }

    /* ======================= SASL & SSL CONFIG ======================= */

    private void applySecurityConfig(Properties props, KafkaClientInfo clientInfo) {
        if (clientInfo == null) {
            throw new IllegalArgumentException("KafkaClientInfo不能为空");
        }

        // 关键逻辑：先确定 security.protocol，避免 SASL/SSL 重复覆盖或写入 null 导致 NPE
        String saslProtocol = clientInfo.getSaslProtocol();
        String sslProtocol = clientInfo.getSslProtocol();

        String securityProtocol = null;
        if (clientInfo.isSasl()) {
            if (StrUtil.isBlank(saslProtocol)) {
                throw new IllegalArgumentException("已启用SASL认证但缺少 security.protocol");
            }
            securityProtocol = saslProtocol;
        }
        if (clientInfo.isSsl()) {
            if (StrUtil.isBlank(sslProtocol)) {
                throw new IllegalArgumentException("已启用SSL但缺少 security.protocol");
            }
            if (securityProtocol == null) {
                securityProtocol = sslProtocol;
            } else if (!securityProtocol.equals(sslProtocol)) {
                // SASL_SSL 场景下建议两者一致；若不一致，优先使用 SASL 的 security.protocol
                log.warn("检测到SASL/SSL的security.protocol不一致，优先使用SASL协议: sasl={}, ssl={}",
                        saslProtocol, sslProtocol);
            }
        }

        if (StrUtil.isNotBlank(securityProtocol)) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
        }

        if (clientInfo.isSasl()) {
            applySaslConfig(props, clientInfo);
        }
        if (clientInfo.isSsl()) {
            applySslConfig(props, clientInfo);
        }
    }

    /** 应用 SASL 身份验证设置 */
    private void applySaslConfig(Properties props, KafkaClientInfo clientInfo) {
        // 关键逻辑：Properties 不允许 null value，这里做必填校验避免 NPE
        if (StrUtil.isBlank(clientInfo.getSaslMechanism())) {
            throw new IllegalArgumentException("已启用SASL认证但缺少 sasl.mechanism");
        }
        if (StrUtil.isBlank(clientInfo.getSaslJaasConfig())) {
            throw new IllegalArgumentException("已启用SASL认证但缺少 sasl.jaas.config");
        }

        if (StrUtil.isNotBlank(clientInfo.getSaslClientId())) {
            props.put(CommonClientConfigs.CLIENT_ID_CONFIG, clientInfo.getSaslClientId());
        }

        props.put(SaslConfigs.SASL_MECHANISM, clientInfo.getSaslMechanism());
        props.put(SaslConfigs.SASL_JAAS_CONFIG, clientInfo.getSaslJaasConfig());
    }

    /** 应用 SSL 加密设置 */
    private void applySslConfig(Properties props, KafkaClientInfo clientInfo) {
        // 关键逻辑：SSL 配置中很多字段是可选的（例如单向TLS不需要 keystore），只在有值时写入
        if (StrUtil.isNotBlank(clientInfo.getSslTruststoreLocation())) {
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, clientInfo.getSslTruststoreLocation());
            if (clientInfo.getSslTruststorePassword() != null) {
                props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, clientInfo.getSslTruststorePassword());
            }
        }

        if (StrUtil.isNotBlank(clientInfo.getSslKeystoreLocation())) {
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, clientInfo.getSslKeystoreLocation());
            if (clientInfo.getSslKeystorePassword() != null) {
                props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, clientInfo.getSslKeystorePassword());
            }
        }
        if (clientInfo.getSslKeyPassword() != null) {
            props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, clientInfo.getSslKeyPassword());
        }

        // 允许配置为空字符串以关闭主机名校验
        if (clientInfo.getSslAlgorithm() != null) {
            props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, clientInfo.getSslAlgorithm());
        }
    }

    /* ======================= RESOURCE MANAGEMENT ======================= */

    /** 注册资源以进行异步关闭 */
    public void registerResourceForClose(AutoCloseable resource) {
        this.closer.close(resource);
    }
}
