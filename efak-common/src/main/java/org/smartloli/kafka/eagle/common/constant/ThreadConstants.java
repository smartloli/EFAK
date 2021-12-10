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
package org.smartloli.kafka.eagle.common.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * Load all thread task class name into map.
 *
 * @author smartloli.
 * <p>
 * Created by Dec 10, 2021
 */
public class ThreadConstants {

    public static final Map<String, Integer> SUB_TASK_MAP = new HashMap<String, Integer>() {
        {
            // key: thread class name, value: weight
            put("org.smartloli.kafka.eagle.web.quartz.shard.task.sub.AlertClusterSubTask", 1);
            put("org.smartloli.kafka.eagle.web.quartz.shard.task.sub.AlertConsumerSubTask", 1);
            put("org.smartloli.kafka.eagle.web.quartz.shard.task.sub.CapacityStatsSubTask", 2);
            put("org.smartloli.kafka.eagle.web.quartz.shard.task.sub.CleanTopicSubTask", 2);
            put("org.smartloli.kafka.eagle.web.quartz.shard.task.sub.DetectConnectUriSubTask", 1);
            put("org.smartloli.kafka.eagle.web.quartz.shard.task.sub.KafkaClusterSubTask", 2);
            put("org.smartloli.kafka.eagle.web.quartz.shard.task.sub.LogsizeStatsSubTask", 2);
            put("org.smartloli.kafka.eagle.web.quartz.shard.task.sub.MbeanOfflineSubTask", 2);
            put("org.smartloli.kafka.eagle.web.quartz.shard.task.sub.MetricsConsumerSubTask", 2);
            put("org.smartloli.kafka.eagle.web.quartz.shard.task.sub.PerformanceByTopicStatsSubTask", 2);
            put("org.smartloli.kafka.eagle.web.quartz.shard.task.sub.ProducerLogSizeStatsSubTask", 2);
            put("org.smartloli.kafka.eagle.web.quartz.shard.task.sub.TopicThroughputByteInTask", 2);
            put("org.smartloli.kafka.eagle.web.quartz.shard.task.sub.TopicThroughputByteOutTask", 2);
            put("org.smartloli.kafka.eagle.web.quartz.shard.task.sub.ZookeeperClusterSubTask", 1);
        }
    };
}
