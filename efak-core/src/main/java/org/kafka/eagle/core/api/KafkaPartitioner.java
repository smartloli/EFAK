/**
 * KafkaPartitioner.java
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
package org.kafka.eagle.core.api;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.Map;

/**
 * <p>
 * Kafka 分区器：根据 key 的哈希值对分区数量取模，确保相同 key 路由到相同分区。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/8/23 22:44:40
 * @version 5.0.0
 */
public class KafkaPartitioner implements Partitioner {

    /**
     * 使用 key 的 hashCode 与分区数取模，得到分区编号。
     */
    public int partition(Object key, int numPartitions) {
        int partition = 0;
        String k = (String) key;
        int hashCode = k.hashCode();
        partition = Math.abs(hashCode) % numPartitions;
        return partition;
    }

    @Override
    public void configure(Map<String, ?> configs) {
        // 无需配置
    }

    /**
     * 新版 Producer API 分区方法。
     * 使用 key 的 hashCode 与 topic 的分区数取模，得到分区编号。
     */
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        int partition = 0;
        String k = (String) key;
        int hashCode = k.hashCode();
        partition = Math.abs(hashCode) % cluster.partitionCountForTopic(topic);
        return partition;
    }

    @Override
    public void close() {
        // 无需处理
    }

}
