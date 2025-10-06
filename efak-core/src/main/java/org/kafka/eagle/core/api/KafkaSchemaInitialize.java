/**
 * KafkaSchemaInitialize.java
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
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.kafka.eagle.core.constant.ClusterMetricsConst;
import org.kafka.eagle.dto.broker.BrokerInfo;
import org.kafka.eagle.dto.cluster.KafkaClientInfo;
import org.kafka.eagle.dto.cluster.KafkaClusterInfo;

import java.util.List;

/**
 * <p>
 * 初始化支持认证的 Kafka 客户端配置。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/8/23 20:58:59
 * @version 5.0.0
 */
@Slf4j
public class KafkaSchemaInitialize {
    private KafkaSchemaInitialize() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 初始化 Kafka 客户端配置
     *
     * @param brokerInfos Broker 信息列表
     * @param clusterInfo 集群信息
     * @return Kafka 客户端信息
     */
    public static KafkaClientInfo init(List<BrokerInfo> brokerInfos, KafkaClusterInfo clusterInfo) {
        KafkaClientInfo clientInfo = new KafkaClientInfo();
        clientInfo.setBrokerServer(KafkaClusterFetcher.formatBrokerList(brokerInfos));
        clientInfo.setClusterId(clusterInfo.getClusterId());

        if (ClusterMetricsConst.Cluster.DISABLE_AUTH.key().equals(clusterInfo.getAuth())) {
            return clientInfo;
        }

        try {
            configureAuthentication(clientInfo, clusterInfo.getAuthConfig());
        } catch (Exception e) {
            log.error("解析认证配置失败: {}", e.getMessage(), e);
        }

        return clientInfo;
    }

    /**
     * 配置身份验证设置
     */
    private static void configureAuthentication(KafkaClientInfo clientInfo, String authConfig) {
        if (StrUtil.isBlank(authConfig)) {
            return;
        }

        JSONObject configJson = JSON.parseObject(authConfig);
        String securityProtocol = configJson.getString(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG);

        if (ClusterMetricsConst.Cluster.AUTH_TYPE_SASL.key().equals(securityProtocol)) {
            configureSasl(clientInfo, configJson);
        } else if (ClusterMetricsConst.Cluster.AUTH_TYPE_SSL.key().equals(securityProtocol)) {
            configureSsl(clientInfo, configJson);
        }
    }

    /**
     * 配置 SASL 身份验证
     */
    private static void configureSasl(KafkaClientInfo clientInfo, JSONObject config) {
        clientInfo.setSasl(true);
        clientInfo.setSaslClientId(config.getString(CommonClientConfigs.CLIENT_ID_CONFIG));
        clientInfo.setSaslProtocol(config.getString(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
        clientInfo.setSaslMechanism(config.getString(SaslConfigs.SASL_MECHANISM));
        clientInfo.setSaslJaasConfig(config.getString(SaslConfigs.SASL_JAAS_CONFIG));
    }

    /**
     * 配置 SSL 身份验证
     */
    private static void configureSsl(KafkaClientInfo clientInfo, JSONObject config) {
        clientInfo.setSsl(true);
        clientInfo.setSslProtocol(config.getString(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
        clientInfo.setSslTruststoreLocation(config.getString(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG));
        clientInfo.setSslTruststorePassword(config.getString(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG));
        clientInfo.setSslKeystoreLocation(config.getString(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG));
        clientInfo.setSslKeystorePassword(config.getString(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG));
        clientInfo.setSslKeyPassword(config.getString(SslConfigs.SSL_KEY_PASSWORD_CONFIG));
        clientInfo.setSslAlgorithm(config.getString(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG));
    }
}
