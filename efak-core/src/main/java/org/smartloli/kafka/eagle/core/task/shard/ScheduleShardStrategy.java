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
package org.smartloli.kafka.eagle.core.task.shard;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.common.constant.ThreadConstants;
import org.smartloli.kafka.eagle.common.util.NetUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.common.util.WorkUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.task.strategy.WorkNodeStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generate thread fragmentation task.
 *
 * @author smartloli.
 * <p>
 * Created by Jul 28, 2022
 */
public class ScheduleShardStrategy {

    private final static KafkaService kafkaService = new KafkaFactory().create();

    @Deprecated
    public static Map<String, String> getScheduleShardSuperTask(String cluster) {
        Map<String, String> subShardMaps = new HashMap<>();
        List<String> hosts = WorkUtils.getWorkNodes();
        int port = SystemConfigUtils.getIntProperty("efak.worknode.port");
        List<WorkNodeStrategy> nodes = new ArrayList<>();
        for (String host : hosts) {
            if (NetUtils.telnet(host, port)) {
                WorkNodeStrategy wns = new WorkNodeStrategy();
                wns.setPort(port);
                wns.setHost(host);
                String masterHost = SystemConfigUtils.getProperty("efak.worknode.master.host");
                if (!masterHost.equals(host)) {
                    nodes.add(wns);
                }
            }
        }

        JSONArray consumerGroups = JSON.parseArray(kafkaService.getKafkaConsumer(cluster));

        JSONArray consumerGroupShard = consumerGroups;
        if (nodes.size() > 0 && consumerGroupShard != null) {
            int balanceNum = (consumerGroupShard.size() / nodes.size()) + 1;
            JSONArray tmpConsumerGroup = new JSONArray();
            int consumerGroupIndex = 0;
            int nodeIndex = 0;
            for (Object consumerGroup : consumerGroupShard) {
                consumerGroupIndex++;
                JSONObject object = (JSONObject) consumerGroup;
                if (tmpConsumerGroup.size() <= balanceNum) {
                    tmpConsumerGroup.add(object);
                    if (tmpConsumerGroup.size() == balanceNum) {
                        subShardMaps.put(nodes.get(nodeIndex).getHost(), tmpConsumerGroup.toString());
                        nodeIndex++;
                        tmpConsumerGroup.clear();
                    }

                    // final result dataset
                    if (consumerGroupIndex == consumerGroupShard.size()) {
                        subShardMaps.put(nodes.get(nodeIndex).getHost(), tmpConsumerGroup.toString());
                        nodeIndex = 0;
                        tmpConsumerGroup.clear();
                    }
                }
            }

        }

        return subShardMaps;
    }

    public static Map<String, List<String>> getScheduleShardTask() {
        List<String> hosts = WorkUtils.getWorkNodes();
        int port = SystemConfigUtils.getIntProperty("efak.worknode.port");
        List<WorkNodeStrategy> nodes = new ArrayList<>();
        for (String host : hosts) {
            if (NetUtils.telnet(host, port)) {
                WorkNodeStrategy wns = new WorkNodeStrategy();
                wns.setPort(port);
                wns.setHost(host);
                String masterHost = SystemConfigUtils.getProperty("efak.worknode.master.host");
                if (!masterHost.equals(host)) {
                    nodes.add(wns);
                }
            }
        }

        Map<String, List<String>> strategyMaps = new HashMap<>();
        List<String> vip1Task = new ArrayList<>();
        List<String> vip2Task = new ArrayList<>();

        // split vip
        for (Map.Entry<String, Integer> entry : ThreadConstants.SUB_TASK_MAP.entrySet()) {
            if (entry.getValue() == ThreadConstants.WEIGHT_VIP1) {
                // vip1
                vip1Task.add(entry.getKey());
            } else if (entry.getValue() == ThreadConstants.WEIGHT_VIP2) {
                // vip2
                vip2Task.add(entry.getKey());
            }
        }
        int counter = 1;
        if (nodes.size() > 0) {
            String hostVip1 = nodes.get(0).getHost();
            strategyMaps.put(hostVip1, vip1Task);

            int nodeSize = nodes.size();
            int taskIndex = 0;
            for (int i = 1; i < nodeSize; i++) {
                String hostVip2 = nodes.get(i).getHost();
                if (strategyMaps.containsKey(hostVip2)) {
                    strategyMaps.get(hostVip2).add(vip2Task.get(taskIndex));
                } else {
                    List<String> tmpTask = new ArrayList<>();
                    tmpTask.add(vip2Task.get(taskIndex));
                    strategyMaps.put(hostVip2, tmpTask);
                }
                counter++;
                taskIndex++;
                if (vip2Task.size() - nodes.size() >= 0) {
                    if (counter <= vip2Task.size() && counter >= nodes.size()) {
                        i = 0;
                        counter = 1;
                        // nodeSize = vip2Task.size() - nodes.size() + 2;
                        nodeSize = vip2Task.size() - taskIndex + 1;
                    }
                } else {
                    if (counter > vip2Task.size()) {
                        break;
                    }
                }
            }
        }
        return strategyMaps;
    }
}
