/**
 * KafkaClientUtils.java
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
package org.kafka.eagle.web.util;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.kafka.eagle.dto.broker.BrokerInfo;
import org.kafka.eagle.dto.cluster.KafkaClusterInfo;
import org.kafka.eagle.dto.cluster.KafkaClientInfo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * Kafka客户端工具类，提供KafkaClientInfo构建和相关的工具方法
 * 主要用于统一管理Kafka客户端连接信息的构建逻辑
 * </p>
 * Author: Mr.SmartLoli
 * Date: 2025/9/27 00:00
 * Version: 1.0
 */
@Slf4j
public class KafkaClientUtils {

    private static String getString(Map<String, Object> map, String key) {
        // 关键逻辑：兼容 JSON 值为非 String 的情况（例如被解析为 Number/Boolean）
        if (map == null || key == null) {
            return null;
        }
        Object value = map.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private static String maskJaas(String jaas) {
        // 关键逻辑：避免日志输出敏感信息
        if (jaas == null || jaas.isBlank()) {
            return "<empty>";
        }
        return "<configured>";
    }

    /**
     * 构建KafkaClientInfo对象
     *
     * @param cluster 集群信息，包含认证配置
     * @param brokers Broker节点列表
     * @return 构建好的KafkaClientInfo对象
     */
    public static KafkaClientInfo buildKafkaClientInfo(KafkaClusterInfo cluster, List<BrokerInfo> brokers) {
        if (cluster == null) {
            throw new IllegalArgumentException("集群信息不能为空");
        }

        if (brokers == null || brokers.isEmpty()) {
            throw new IllegalArgumentException("Broker信息不能为空");
        }

        KafkaClientInfo kafkaClientInfo = new KafkaClientInfo();
        kafkaClientInfo.setClusterId(cluster.getClusterId());

        // 从数据库 ke_broker_info 获取当前集群的Broker信息
        String brokerServer = brokers.stream()
                .map(broker -> broker.getHostIp() + ":" + broker.getPort())
                .collect(Collectors.joining(","));
        kafkaClientInfo.setBrokerServer(brokerServer);

        // 从数据库ke_cluster获取当前集群是否需要认证
        if ("Y".equals(cluster.getAuth())) {
            // 解析auth_config的JSON字符串
            String authConfig = cluster.getAuthConfig();
            if (authConfig != null && !authConfig.trim().isEmpty()) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> authMap = JSON.parseObject(authConfig, Map.class);

                    String securityProtocol = getString(authMap, CommonClientConfigs.SECURITY_PROTOCOL_CONFIG);
                    if (securityProtocol == null || securityProtocol.isBlank()) {
                        throw new IllegalArgumentException("Kafka认证已启用但缺少 security.protocol");
                    }

                    // 关键逻辑：根据 security.protocol 判断启用 SASL/SSL（支持 SASL_PLAINTEXT / SASL_SSL / SSL）
                    if (securityProtocol.startsWith("SASL_")) {
                        kafkaClientInfo.setSasl(true);
                        kafkaClientInfo.setSaslProtocol(securityProtocol);

                        kafkaClientInfo.setSaslClientId(getString(authMap, CommonClientConfigs.CLIENT_ID_CONFIG));
                        kafkaClientInfo.setSaslMechanism(getString(authMap, SaslConfigs.SASL_MECHANISM));
                        kafkaClientInfo.setSaslJaasConfig(getString(authMap, SaslConfigs.SASL_JAAS_CONFIG));

                        if (kafkaClientInfo.getSaslMechanism() == null || kafkaClientInfo.getSaslMechanism().isBlank()) {
                            throw new IllegalArgumentException("Kafka认证已启用但缺少 sasl.mechanism");
                        }
                        if (kafkaClientInfo.getSaslJaasConfig() == null || kafkaClientInfo.getSaslJaasConfig().isBlank()) {
                            throw new IllegalArgumentException("Kafka认证已启用但缺少 sasl.jaas.config");
                        }

                        // SASL_SSL 场景需要 SSL 参数（证书可选：可走 JVM 默认信任库）
                        if (securityProtocol.endsWith("_SSL")) {
                            kafkaClientInfo.setSsl(true);
                            kafkaClientInfo.setSslProtocol(securityProtocol);
                            kafkaClientInfo.setSslTruststoreLocation(getString(authMap, SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG));
                            kafkaClientInfo.setSslTruststorePassword(getString(authMap, SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG));
                            kafkaClientInfo.setSslKeystoreLocation(getString(authMap, SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG));
                            kafkaClientInfo.setSslKeystorePassword(getString(authMap, SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG));
                            kafkaClientInfo.setSslKeyPassword(getString(authMap, SslConfigs.SSL_KEY_PASSWORD_CONFIG));
                            kafkaClientInfo.setSslAlgorithm(getString(authMap, SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG));
                        }
                    } else if ("SSL".equalsIgnoreCase(securityProtocol)) {
                        kafkaClientInfo.setSsl(true);
                        kafkaClientInfo.setSslProtocol("SSL");
                        kafkaClientInfo.setSslTruststoreLocation(getString(authMap, SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG));
                        kafkaClientInfo.setSslTruststorePassword(getString(authMap, SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG));
                        kafkaClientInfo.setSslKeystoreLocation(getString(authMap, SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG));
                        kafkaClientInfo.setSslKeystorePassword(getString(authMap, SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG));
                        kafkaClientInfo.setSslKeyPassword(getString(authMap, SslConfigs.SSL_KEY_PASSWORD_CONFIG));
                        kafkaClientInfo.setSslAlgorithm(getString(authMap, SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG));
                    } else {
                        // 关键逻辑：认证开关已开启，但协议不支持，直接失败以便快速暴露配置问题
                        throw new IllegalArgumentException("Kafka认证已启用但 security.protocol 不支持: " + securityProtocol);
                    }

                    log.info("构建KafkaClientInfo完成: clusterId={}, security.protocol={}, sasl={}, ssl={}, sasl.mechanism={}, sasl.jaas={}",
                            cluster.getClusterId(), securityProtocol, kafkaClientInfo.isSasl(), kafkaClientInfo.isSsl(),
                            kafkaClientInfo.getSaslMechanism(), maskJaas(kafkaClientInfo.getSaslJaasConfig()));
                } catch (Exception e) {
                    log.warn("解析集群 {} 的认证配置失败: {}", cluster.getClusterId(), e.getMessage(), e);
                    throw e;
                }
            } else {
                throw new IllegalArgumentException("Kafka认证已启用但 auth_config 为空");
            }
        }

        return kafkaClientInfo;
    }

    /**
     * 构建KafkaClientInfo对象（简化版本，只需要基础连接信息）
     *
     * @param clusterId 集群ID
     * @param brokers Broker节点列表
     * @return 构建好的KafkaClientInfo对象（无认证）
     */
    public static KafkaClientInfo buildSimpleKafkaClientInfo(String clusterId, List<BrokerInfo> brokers) {
        if (clusterId == null || clusterId.trim().isEmpty()) {
            throw new IllegalArgumentException("集群ID不能为空");
        }

        if (brokers == null || brokers.isEmpty()) {
            throw new IllegalArgumentException("Broker信息不能为空");
        }

        KafkaClientInfo kafkaClientInfo = new KafkaClientInfo();
        kafkaClientInfo.setClusterId(clusterId);

        // 构建broker服务器地址
        String brokerServer = brokers.stream()
                .map(broker -> broker.getHostIp() + ":" + broker.getPort())
                .collect(Collectors.joining(","));
        kafkaClientInfo.setBrokerServer(brokerServer);

        // 默认不使用认证
        kafkaClientInfo.setSasl(false);

        return kafkaClientInfo;
    }

    /**
     * 验证KafkaClientInfo对象是否有效
     *
     * @param kafkaClientInfo 要验证的KafkaClientInfo对象
     * @return 验证结果
     */
    public static boolean isValidKafkaClientInfo(KafkaClientInfo kafkaClientInfo) {
        if (kafkaClientInfo == null) {
            return false;
        }

        if (kafkaClientInfo.getClusterId() == null || kafkaClientInfo.getClusterId().trim().isEmpty()) {
            return false;
        }

        if (kafkaClientInfo.getBrokerServer() == null || kafkaClientInfo.getBrokerServer().trim().isEmpty()) {
            return false;
        }

        // 如果启用了SASL认证，验证认证信息
        if (kafkaClientInfo.isSasl()) {
            if (kafkaClientInfo.getSaslProtocol() == null || kafkaClientInfo.getSaslProtocol().trim().isEmpty()) {
                log.warn("SASL认证已启用但缺少安全协议配置");
                return false;
            }
            if (kafkaClientInfo.getSaslMechanism() == null || kafkaClientInfo.getSaslMechanism().trim().isEmpty()) {
                log.warn("SASL认证已启用但缺少 sasl.mechanism 配置");
                return false;
            }
            if (kafkaClientInfo.getSaslJaasConfig() == null || kafkaClientInfo.getSaslJaasConfig().trim().isEmpty()) {
                log.warn("SASL认证已启用但缺少 sasl.jaas.config 配置");
                return false;
            }
        }

        return true;
    }

    /**
     * 获取Broker服务器地址列表
     *
     * @param brokers Broker节点列表
     * @return 服务器地址列表（格式：host:port）
     */
    public static List<String> getBrokerServerAddresses(List<BrokerInfo> brokers) {
        if (brokers == null || brokers.isEmpty()) {
            throw new IllegalArgumentException("Broker信息不能为空");
        }

        return brokers.stream()
                .map(broker -> broker.getHostIp() + ":" + broker.getPort())
                .collect(Collectors.toList());
    }

    /**
     * 格式化Broker服务器地址为字符串
     *
     * @param brokers Broker节点列表
     * @return 格式化的服务器地址字符串（格式：host1:port1,host2:port2）
     */
    public static String formatBrokerServerString(List<BrokerInfo> brokers) {
        if (brokers == null || brokers.isEmpty()) {
            throw new IllegalArgumentException("Broker信息不能为空");
        }

        return brokers.stream()
                .map(broker -> broker.getHostIp() + ":" + broker.getPort())
                .collect(Collectors.joining(","));
    }
}
