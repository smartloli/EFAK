/**
 * JmxMetricsConst.java
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
package org.kafka.eagle.core.constant;

/**
 * <p>
 * 集中管理的 JMX 常量定义，用于 Kafka 监控
 * </p>
 * 使用示例：
 * <pre>
 *   // 获取带占位符的 Kafka 日志大小指标
 *   String logSize = JmxMetricsConst.Log.SIZE.getValue();
 *
 *   // 填充占位符
 *   String formatted = String.format(logSize, "my-topic", "0");
 *
 *   // 获取代理 BytesInPerSec 指标
 *   String brokerMetric = JmxMetricsConst.Server.BYTES_IN_PER_SEC.getValue();
 * </pre>
 * @author Mr.SmartLoli
 * @since 2025/8/23 22:08:51
 * @version 5.0.0
 */
public class JmxMetricsConst {
    /** 通用常量 */
    public static final class Common {
        public static final String VALUE = "Value";
        public static final String MBEAN_KEY_SEPARATOR = "__EFAK__";

        private final String key;
        Common(String key) { this.key = key; }
        public String key() { return key; }
    }

    /** Kafka 日志指标 */
    public enum Log {
        SIZE("kafka.log:type=Log,name=Size,topic=%s,partition=%s"),
        VALUE(Common.VALUE);

        private final String key;
        Log(String key) { this.key = key; }
        public String key() { return key; }
    }

    /** 代理节点/服务器指标 */
    public enum Server {
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
        REQUEST_TIME_MS_PRODUCE("kafka.network:type=RequestMetrics,name=TotalTimeMs,request=Produce"),
        REQUEST_TIME_MS_OFFSET_FETCH("kafka.network:type=RequestMetrics,name=TotalTimeMs,request=OffsetFetch"),

        // 代理节点信息
        BROKER_APP_INFO("kafka.server:type=app-info,id=%s"),
        BROKER_VERSION_VALUE("Version"),
        BROKER_STARTTIME_VALUE("StartTimeMs"),

        // 内存和CPU指标
        BROKER_OS_MEM_FREE("OS_MEM_FREE"),
        BROKER_OS_MEM_USED("OS_MEM_USED"),
        BROKER_OS_CPU_USED("OS_CPU_USED");

        private final String key;
        Server(String key) { this.key = key; }
        public String key() { return key; }
    }

    /** 系统指标 */
    public enum System {
        JMX_PERFORMANCE_TYPE("java.lang:type=OperatingSystem"),
        TOTAL_PHYSICAL_MEMORY_SIZE("TotalPhysicalMemorySize"),
        FREE_PHYSICAL_MEMORY_SIZE("FreePhysicalMemorySize"),
        PROCESS_CPU_LOAD("ProcessCpuLoad");

        private final String key;
        System(String key) { this.key = key; }
        public String key() { return key; }
    }

    private JmxMetricsConst() {}
}
