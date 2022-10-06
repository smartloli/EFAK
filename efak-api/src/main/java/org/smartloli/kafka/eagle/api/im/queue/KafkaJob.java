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
package org.smartloli.kafka.eagle.api.im.queue;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.protocol.BrokersInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.queue.BaseJobContext;
import org.smartloli.kafka.eagle.common.protocol.cache.BrokerCache;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.KConstants.AlarmQueue;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;

import java.util.List;
import java.util.Properties;

/**
 * Add alarm message to wechat job queue.
 *
 * @author smartloli.
 * <p>
 * Created by Oct 27, 2019
 */
public class KafkaJob implements Job {
    private final Logger LOG = LoggerFactory.getLogger(KafkaJob.class);
    /**
     * Send alarm information by mail or webhook.
     */
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        BaseJobContext bjc = (BaseJobContext) jobContext.getJobDetail().getJobDataMap().get(AlarmQueue.JOB_PARAMS);
        sendMsg(bjc.getData(), bjc.getUrl());
    }

    private int sendMsg(String data, String url) {
        sendKafkaMsg("cluster1",url,data);
        return 1;
    }


    private void sendKafkaMsg(String clusterAlias, String topic, String msg) {
        Properties props = new Properties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KConstants.Kafka.EFAK_SYSTEM_GROUP);
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, parseBrokerServer(clusterAlias));
        props.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".efak.sasl.enable")) {
            sasl(props, clusterAlias);
        }
        if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".efak.ssl.enable")) {
            ssl(props, clusterAlias);
        }
        new org.apache.kafka.clients.producer.KafkaProducer<>(props).send(new ProducerRecord<>(topic,msg));
        LOG.info("sendMsg ->topic:" + topic+",msg="+msg);
    }

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
     * Set topic ssl.
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
}
