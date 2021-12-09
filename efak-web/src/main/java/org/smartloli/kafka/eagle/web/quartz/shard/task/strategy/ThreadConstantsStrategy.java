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
package org.smartloli.kafka.eagle.web.quartz.shard.task.strategy;

import org.smartloli.kafka.eagle.web.quartz.shard.task.sub.AlertClusterSubTask;

import java.util.HashMap;
import java.util.Map;

/**
 * Load all thread task class name into map.
 *
 * @author smartloli.
 * <p>
 * Created by Dec 10, 2021
 */
public class ThreadConstantsStrategy {

    public static final Map<Class<?>, Integer> SUB_TASK_MAP = new HashMap<Class<?>, Integer>() {
        {
            // key: thread class name, value: weight
            put(AlertClusterSubTask.class, 1);
//            put(AlertConsumerSubTask.class, 1);
//            put(CapacityStatsSubTask.class, 2);
//            put(CleanTopicSubTask.class, 2);
////            put(new DetectConnectUriSubTask.class, 1);
//            put(new KafkaClusterSubTask().getClass(), 2);
//            put(new LogsizeStatsSubTask().getClass(), 2);
//            put(new MbeanOfflineSubTask().getClass(), 2);
//            put(new MetricsConsumerSubTask().getClass(), 2);
//            put(new PerformanceByTopicStatsSubTask().getClass(), 2);
//            put(new ProducerLogSizeStatsSubTask().getClass(), 2);
//            put(new TopicThroughputByteInTask().getClass(), 2);
//            put(new TopicThroughputByteOutTask().getClass(), 2);
//            put(new ZookeeperClusterSubTask().getClass(), 1);
        }
    };
}
