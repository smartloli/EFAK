/**
 * TestKafkaClusterFetcher.java
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
package org.kafka.eagle.core.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.kafka.eagle.core.kafka.KafkaClusterFetcher;
import org.kafka.eagle.pojo.cluster.BrokerInfo;
import org.kafka.eagle.pojo.kafka.JMXInitializeInfo;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/6/7 15:12
 * @Version: 3.4.0
 */
@Slf4j
public class TestKafkaClusterFetcher {

    @Test
    public void testKafkaJmxInfo() {
        JMXInitializeInfo initializeInfo = new JMXInitializeInfo();
        initializeInfo.setBrokerId("1");
        initializeInfo.setHost("127.0.0.1");
        initializeInfo.setPort(9999);
        BrokerInfo brokerInfo = KafkaClusterFetcher.getKafkaJmxInfo(initializeInfo);
        log.info("brokerInfo:{}", brokerInfo);
    }

    @Test
    public void kafkaAliveStatus() {
        log.info("status:{}", KafkaClusterFetcher.getKafkaAliveStatus("127.0.0.1", 19093));
    }

}
