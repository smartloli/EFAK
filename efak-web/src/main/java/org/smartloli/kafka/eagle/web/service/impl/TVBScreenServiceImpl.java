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
package org.smartloli.kafka.eagle.web.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicRank;
import org.smartloli.kafka.eagle.common.util.*;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerFactory;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerService;
import org.smartloli.kafka.eagle.core.task.schedule.JobClient;
import org.smartloli.kafka.eagle.web.dao.TopicDao;
import org.smartloli.kafka.eagle.web.service.TVBScreenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * EFAK TV Monitor.
 *
 * @author smartloli.
 * <p>
 * Created by Aug 07, 2022
 */
@Service
public class TVBScreenServiceImpl implements TVBScreenService {

    @Autowired
    private TopicDao topicDao;

    /**
     * Kafka service interface.
     */
    private KafkaService kafkaService = new KafkaFactory().create();
    private static BrokerService brokerService = new BrokerFactory().create();

    @Override
    public String getClusterInfo(String clusterAlias) {
        JSONObject object = new JSONObject();

        object.put("cluster", clusterAlias);
        object.put("version", KConstants.Common.EFAK_VERSION);

        Map<String, Object> params = new HashMap<>();
        params.put("cluster", clusterAlias);
        params.put("topics", brokerService.topicList(clusterAlias));
        params.put("size", brokerService.topicList(clusterAlias).size());
        params.put("tday", CalendarUtils.getCustomDate("yyyyMMdd"));
        long totalRecords = topicDao.getBScreenTotalRecords(params);

        if (SystemConfigUtils.getBooleanProperty("efak.distributed.enable")) {
            object.put("mode", "Distribute");
        } else {
            object.put("mode", "Standalone");
        }

        Map<String, Object> producerParams = new HashMap<>();
        producerParams.put("cluster", clusterAlias);
        producerParams.put("topic", KConstants.Topic.PRODUCER_THREADS_KEY);
        producerParams.put("tkey", KConstants.Topic.PRODUCER_THREADS);
        TopicRank tr = topicDao.readProducerThreads(producerParams);
        object.put("app", tr == null ? 0L : tr.getTvalue());
        Map<String, Object> capacityParams = new HashMap<>();
        capacityParams.put("cluster", clusterAlias);
        capacityParams.put("tkey", KConstants.Topic.CAPACITY);
        JSONObject capacity = StrUtils.stringifyByObject(topicDao.getTopicCapacity(capacityParams));
        object.put("capacity", capacity.getString("size"));
        object.put("capacityType", capacity.getString("type"));

        return object.toString();
    }

    @Override
    public String getClusterOfWorkNodeInfo(String clusterAlias) {
        JSONObject object = new JSONObject();
        int worknode = 0;
        try {
            worknode = JobClient.getWorkNodeMetrics(clusterAlias).size();
        } catch (Exception e) {
            e.printStackTrace();
            LoggerUtils.print(this.getClass()).error(e.toString());
        }


        object.put("worknode", worknode);
        return object.toString();

    }
}
