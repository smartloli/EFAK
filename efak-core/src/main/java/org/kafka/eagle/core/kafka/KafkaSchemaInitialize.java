/**
 * KafkaSchemaUtil.java
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

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.kafka.eagle.common.constants.KConstants;
import org.kafka.eagle.pojo.cluster.BrokerInfo;
import org.kafka.eagle.pojo.cluster.ClusterInfo;
import org.kafka.eagle.pojo.cluster.KafkaClientInfo;

import java.util.List;

/**
 * Kafka schema utils.
 *
 * @Author: smartloli
 * @Date: 2023/6/23 19:22
 * @Version: 3.4.0
 */
@Slf4j
public class KafkaSchemaInitialize {

    private KafkaSchemaInitialize() {
    }

    /**
     * Init kafka client info.
     * @param brokerInfos
     * @param clusterInfo
     * @return
     */

    public static KafkaClientInfo init(List<BrokerInfo> brokerInfos, ClusterInfo clusterInfo) {
        KafkaClientInfo kafkaClientInfo = new KafkaClientInfo();
        kafkaClientInfo.setBrokerServer(KafkaClusterFetcher.parseBrokerServer(brokerInfos));
        kafkaClientInfo.setClusterId(clusterInfo.getClusterId());
        if (KConstants.Cluster.ENABLE_AUTH.equals(clusterInfo.getAuth())) {
            try {
                String authConfig = clusterInfo.getAuthConfig();
                if (!StrUtil.isBlank(authConfig)) {
                    JSONObject authConfigJson = JSON.parseObject(authConfig);
                    if (KConstants.Cluster.AUTH_TYPE_SASL.equals(authConfigJson.getString(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG))) {
                        kafkaClientInfo.setSasl(true);
                        kafkaClientInfo.setSaslClientId(CommonClientConfigs.CLIENT_ID_CONFIG);
                        kafkaClientInfo.setSaslProtocol(authConfigJson.getString(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
                        kafkaClientInfo.setSaslMechanism(authConfigJson.getString(SaslConfigs.SASL_MECHANISM));
                        kafkaClientInfo.setSaslJaasConfig(authConfigJson.getString(SaslConfigs.SASL_JAAS_CONFIG));
                    } else if (KConstants.Cluster.AUTH_TYPE_SSL.equals(authConfigJson.getString(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG))) {
                        kafkaClientInfo.setSsl(true);
                        kafkaClientInfo.setSslProtocol(authConfigJson.getString(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
                        kafkaClientInfo.setSslTruststoreLocation(authConfigJson.getString(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG));
                        kafkaClientInfo.setSslTruststorePassword(authConfigJson.getString(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG));
                        kafkaClientInfo.setSslKeystoreLocation(authConfigJson.getString(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG));
                        kafkaClientInfo.setSslKeystorePassword(authConfigJson.getString(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG));
                        kafkaClientInfo.setSslKeyPassword(authConfigJson.getString(SslConfigs.SSL_KEY_PASSWORD_CONFIG));
                        kafkaClientInfo.setSslAlgorithm(authConfigJson.getString(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG));
                    }
                }
            } catch (Exception e) {
                log.error("Parse auth config to json has error,msg is {}", e);
            }
        }
        return kafkaClientInfo;
    }
}
