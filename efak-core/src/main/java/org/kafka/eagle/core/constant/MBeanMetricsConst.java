/**
 * MBeanConst.java
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
 * 定义 Kafka Broker 常见的 JMX MBean 指标名称。
 * </p>
 * <p>使用示例：</p>
 * <pre>
 *   String metricKey = MBeanMetricsConst.Common.COUNT.key();
 *   System.out.println(metricKey);  // 输出: "Count"
 *
 *   String trafficMetric = MBeanMetricsConst.Traffic.MESSAGES_IN.key();
 *   System.out.println(trafficMetric);  // 输出: "msg"
 *
 *   String systemMetric = MBeanMetricsConst.System.CPU_USED.key();
 *   System.out.println(systemMetric);  // 输出: "cpu_used"
 * </pre>
 * <p>
 * @author Mr.SmartLoli
 * @since 2025/8/23 21:56:12
 * @version 5.0.0
 */
public class MBeanMetricsConst {

    private MBeanMetricsConst() {
        // 防止实例化工具类
    }

    /**
     * 通用指标属性
     */
    public enum Common {
        COUNT("Count"),
        EVENT_TYPE("EventType"),
        FIFTEEN_MINUTE_RATE("FifteenMinuteRate"),
        FIVE_MINUTE_RATE("FiveMinuteRate"),
        MEAN_RATE("MeanRate"),
        ONE_MINUTE_RATE("OneMinuteRate"),
        RATE_UNIT("RateUnit"),
        MEAN("Mean"),
        VALUE("Value");

        private final String key;

        Common(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    /**
     * Kafka 流量指标（每秒）
     */
    public enum Traffic {
        MESSAGES_IN("msg"),
        BYTES_IN("ins"),
        BYTES_OUT("out"),
        BYTES_REJECTED("rejected"),
        FAILED_FETCH_REQUEST("fetch"),
        FAILED_PRODUCE_REQUEST("produce");

        private final String key;

        Traffic(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    /**
     * 代理节点级别指标
     */
    public enum Broker {
        MESSAGE_IN("message_in"),
        BYTE_IN("byte_in"),
        BYTE_OUT("byte_out"),
        BYTE_REJECTED("byte_rejected"),
        FAILED_FETCH("failed_fetch_request"),
        FAILED_PRODUCE("failed_produce_request"),
        PRODUCE_CONVERSIONS("produce_message_conversions"),
        TOTAL_FETCH("total_fetch_requests"),
        TOTAL_PRODUCE("total_produce_requests"),
        REPL_BYTES_IN("replication_bytes_in"),
        REPL_BYTES_OUT("replication_bytes_out");

        private final String key;

        Broker(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    /**
     * 主题级别指标
     */
    public enum Topic{
        BYTE_IN("byte_in"),
        BYTE_OUT("byte_out"),
        LOG_SIZE("log_size"),
        CAPACITY("capacity");

        public String key() {
            return key;
        }

        private final String key;

        Topic(String key) {
            this.key = key;
        }
    }

    /**
     * 系统级别指标
     */
    public enum System {
        OS_USED_MEMORY("os_used_memory"),
        OS_FREE_MEMORY("os_free_memory"),
        CPU_USED("cpu_used");

        private final String key;

        System(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }
}
