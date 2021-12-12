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
package org.smartloli.kafka.eagle.common.util;

/**
 * Kafka zkclient object instance.
 *
 * @author smartloli.
 * <p>
 * Created by Oct 07, 2021
 */
public class KafkaZKSingletonUtils {

    private static class ZkClientHolder {
        private static KafkaZKPoolUtils kafkaZKPool = null;

        static {
            try {
                if (kafkaZKPool == null) {
                    kafkaZKPool = KafkaZKPoolUtils.getInstance();
                }
            } catch (Exception e) {
                LoggerUtils.print(KafkaZKSingletonUtils.class).error("Failure to initialize zkclient pool object, msg is {}", e);
            }
        }
    }

    public static KafkaZKPoolUtils create() {
        return ZkClientHolder.kafkaZKPool;
    }

    public static int getZkCliPoolSize(String cluster) {
        return KafkaZKPoolUtils.getZkCliPoolSize(cluster);
    }
    
}
