/**
 * KafkaClientInfo.java
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
package org.kafka.eagle.dto.cluster;

import lombok.Data;

/**
 * <p>
 * Kafka 客户端配置信息 DTO，包含 Broker 地址、SASL 和 SSL 认证设置。
 * 该类封装了建立 Kafka 集群连接所需的所有属性，包括 SASL 和 SSL 协议的安全配置。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/8/23 20:38
 * @version 5.0.0
 */
@Data
public class KafkaClientInfo {

    /**
     * Kafka 集群 ID
     */
    private String clusterId;

    /**
     * Kafka Broker 地址
     */
    private String brokerServer;

    /**
     * 启用 SASL 认证
     */
    private boolean sasl = false;

    /**
     * SASL 协议
     */
    private String saslProtocol;

    /**
     * SASL 机制
     */
    private String saslMechanism;

    /**
     * SASL 客户端 ID
     */
    private String saslClientId;

    /**
     * SASL JAAS 配置
     */
    private String saslJaasConfig;

    /**
     * 启用 SSL
     */
    private boolean ssl = false;

    /**
     * SSL 协议
     */
    private String sslProtocol;

    /**
     * SSL 信任库位置
     */
    private String sslTruststoreLocation;

    /**
     * SSL 信任库密码
     */
    private String sslTruststorePassword;

    /**
     * SSL 密钥库位置
     */
    private String sslKeystoreLocation;

    /**
     * SSL 密钥库密码
     */
    private String sslKeystorePassword;

    /**
     * SSL 密钥密码
     */
    private String sslKeyPassword;

    /**
     * SSL 算法
     */
    private String sslAlgorithm;

    /**
     * 键反序列化器
     */
    private String keyDeserializer;

    /**
     * 值反序列化器
     */
    private String valueDeserializer;

    /**
     * 键序列化器
     */
    private  String keySerializer;

    /**
     * 值序列化器
     */
    private String valueSerializer;
}
