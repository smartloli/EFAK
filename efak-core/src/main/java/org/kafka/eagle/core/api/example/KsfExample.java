/**
 * KsfExample.java
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
package org.kafka.eagle.core.api.example;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.core.api.KafkaSchemaFactory;
import org.kafka.eagle.core.api.KafkaStoragePlugin;
import org.kafka.eagle.dto.cluster.KafkaClientInfo;
import org.kafka.eagle.dto.jmx.JMXInitializeInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * KafkaSchemaFactory使用示例，演示如何使用KSF进行Kafka操作
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/8/24 00:30:15
 * @version 5.0.0
 */
@Slf4j
public class KsfExample {
    public static void main(String[] args) {
        KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());

        KafkaClientInfo kafkaClientInfo = new KafkaClientInfo();
        // 从数据库 ke_broker_info 获取当前集群的Broker信息
        kafkaClientInfo.setBrokerServer("127.0.0.1:9092");

        // 从数据库ke_cluster（auth=N不需要，auth=Y需要认证，auth_config解析JSON字符串）获取当前集群是否需要认证
        // auth_config的JSON字符串如下：
        // {
        //  "security.protocol": "SASL_PLAINTEXT",
        //  "sasl.mechanism": "PLAIN",
        //  "sasl.jaas.config": "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"user\" password=\"password\";"
        //}
//        kafkaClientInfo.setSasl(); // 从数据库ke_cluster获取当前集群是否需要认证
//        kafkaClientInfo.setSaslJaasConfig();
//        kafkaClientInfo.setSaslMechanism();
//        kafkaClientInfo.setSaslProtocol();
        // 比如获取所有的主题名称
        // ksf.listTopicNames(kafkaClientInfo);// 获取当前集群所有主题名称

        Set<String> groupIds = new HashSet<>();
        groupIds.add("my-group-1");
//         System.out.println("topic_consumer:"+JSON.toJSONString(ksf.getKafkaConsumerGroupTopic(kafkaClientInfo, groupIds)));
//         System.out.println("groug_id:"+JSON.toJSONString(ksf.getKafkaConsumerGroups(kafkaClientInfo)));
//         System.out.println("groug_ids:"+JSON.toJSONString(ksf.getConsumerGroupIds(kafkaClientInfo)));

       // System.out.println("state:"+JSON.toJSONString(ksf.getConsumerGroups(kafkaClientInfo)));
        Set<String> setIds = new HashSet<>();
        setIds.add("my-group-1");
        setIds.add("my-group-2");
        //System.out.println("state_group_id:"+JSON.toJSONString(ksf.getConsumerGroups(kafkaClientInfo, setIds)));
         // System.out.println(ksf.listTopicNames(kafkaClientInfo));
        Map<String,Object> params = new HashMap<>();
        params.put("page",1);
        params.put("size",10);

         // System.out.println(JSON.toJSONString(ksf.getTopicPartitionPage(kafkaClientInfo, "ke28",params)));

        // System.out.println(JSON.toJSONString(ksf.fetchLatestMessages(kafkaClientInfo,"ke28",0)));
        // System.out.println(JSON.toJSONString(ksf.getPartitionCount(kafkaClientInfo,"price_test")));
        // System.out.println(JSON.toJSONString(ksf.listTopicNames(kafkaClientInfo)));
        // System.out.println(JSON.toJSONString(ksf.getTopicConfig(kafkaClientInfo,"ke28")));
         // System.out.println(ksf.getClusterBrokers(kafkaClientInfo));
        // 循环100次，每次随机发送5～10条消息，并随机延迟1～5秒
        for (int i = 0; i < 100; i++) {
            int numMessages = (int) (Math.random() * 6 + 5);
            for (int j = 0; j < numMessages; j++) {
                ksf.sendMessage(kafkaClientInfo, "ke28", "你好 kafka-eagle");
            }
            try {
                Thread.sleep((long) (Math.random() * 4000 + 30000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        JMXInitializeInfo initializeInfo = new JMXInitializeInfo();
        initializeInfo.setBrokerId("1");
        initializeInfo.setHost("127.0.0.1");
        initializeInfo.setPort(9988);
        // BrokerInfo brokerInfo = KafkaClusterFetcher.fetchBrokerDetails(initializeInfo);
        // log.info("代理信息:{}", brokerInfo);
    }
}
