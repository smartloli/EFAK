/**
 * KafkaClientInfo.java
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
package org.kafka.eagle.pojo.cluster;

import lombok.Data;

/**
 * Used to operate the view corresponding to the kafka client.
 *
 * @Author: smartloli
 * @Date: 2023/6/18 20:20
 * @Version: 3.4.0
 */
@Data
public class KafkaClientInfo {

    /**
     * Kafka cluster id
     */
    private String clusterId;

    /**
     * kafka broker address
     */
    private String brokerServer;

    /**
     * enable sasl, such as scram-sha-256, scram-sha-512
     */
    private boolean sasl = false;

    private String saslProtocol;
    private String saslMechanism;
    private String saslClientId;
    private String saslJaasConfig;

    /**
     * enable ssl ,such as ssl://
     */
    private boolean ssl = false;

    private String sslProtocol;

    private String sslTruststoreLocation;
    private String sslTruststorePassword;
    private String sslKeystoreLocation;
    private String sslKeystorePassword;
    private String sslKeyPassword;
    private String sslAlgorithm;

    /**
     * key.deserializer by consumer
     */
    private String keyDeserializer;

    /**
     * value.deserializer by consumer
     */
    private String valueDeserializer;

    /**
     * key.serializer by producer
     */
    private  String keySerializer;

    /**
     * value.serializer by producer
     */
    private String valueSerializer;


}
