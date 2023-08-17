/**
 * JmxConstants.java
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
package org.kafka.eagle.common.constants;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/6/7 15:07
 * @Version: 3.4.0
 */
public class JmxConstants {
    private static final String KAFKA_COMMON_VALUE = "Value";

    public static final String MBEAN_KEY_SEPARATOR = "__EFAK__";

    public enum KafkaLog {
        SIZE("kafka.log:type=Log,name=Size,topic=%s,partition=%s"),
        VALUE(KAFKA_COMMON_VALUE);
        private String value;

        private KafkaLog(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum BrokerServer {
        BYTES_IN_PER_SEC("kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec"),
        BYTES_OUT_PER_SEC("kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec"),
        BYTES_REJECTED_PER_SEC("kafka.server:type=BrokerTopicMetrics,name=BytesRejectedPerSec"),
        FAILED_FETCH_REQUESTS_PER_SEC("kafka.server:type=BrokerTopicMetrics,name=FailedFetchRequestsPerSec"),
        FAILED_PRODUCE_REQUESTS_PER_SEC("kafka.server:type=BrokerTopicMetrics,name=FailedProduceRequestsPerSec"),
        MESSAGES_IN_PER_SEC("kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec"),
        PRODUCE_MESSAGE_CONVERSIONS_PER_SEC("kafka.server:type=BrokerTopicMetrics,name=ProduceMessageConversionsPerSec"),
        REPLICATION_BYTES_IN_PER_SEC("kafka.server:type=BrokerTopicMetrics,name=ReplicationBytesInPerSec"),
        REPLICATION_BYTES_OUT_PER_SEC("kafka.server:type=BrokerTopicMetrics,name=ReplicationBytesOutPerSec"),
        TOTAL_FETCH_REQUESTS_PER_SEC("kafka.server:type=BrokerTopicMetrics,name=TotalFetchRequestsPerSec"),
        TOTAL_PRODUCE_REQUESTS_PER_SEC("kafka.server:type=BrokerTopicMetrics,name=TotalProduceRequestsPerSec"),
        BYTES_IN_PER_SEC_TOPIC("kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec,topic=%s"),
        BYTES_OUT_PER_SEC_TOPIC("kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec,topic=%s"),
        JMX_PERFORMANCE_TYPE("java.lang:type=OperatingSystem"),
        TOTAL_PHYSICAL_MEMORY_SIZE("TotalPhysicalMemorySize"),
        FREE_PHYSICAL_MEMORY_SIZE("FreePhysicalMemorySize"),
        PROCESS_CPU_LOAD("ProcessCpuLoad"),
        BROKER_APP_INFO("kafka.server:type=app-info,id=%s"),
        BROKER_VERSION_VALUE("Version"),
        BROKER_OS_MEM_FREE("OS_MEM_FREE"),
        BROKER_OS_MEM_USED("OS_MEM_USED"),
        BROKER_OS_CPU_USED("OS_CPU_USED"),
        BROKER_STARTTIME_VALUE("StartTimeMs");

        private String value;

        public String getValue() {
            return value;
        }

        private BrokerServer(String value) {
            this.value = value;
        }
    }
}
