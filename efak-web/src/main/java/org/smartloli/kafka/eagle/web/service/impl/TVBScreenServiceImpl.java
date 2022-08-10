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
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.web.service.TVBScreenService;

/**
 * EFAK TV Monitor.
 *
 * @author smartloli.
 * <p>
 * Created by Aug 07, 2022
 */
public class TVBScreenServiceImpl implements TVBScreenService {

    /**
     * Kafka service interface.
     */
    private KafkaService kafkaService = new KafkaFactory().create();

    @Override
    public String getTVMidOfKafka(String clusterAlias) {
        JSONObject object = new JSONObject();

        object.put("cluster", clusterAlias);
        object.put("groups", kafkaService.getKafkaConsumerGroups(clusterAlias));


        return object.toJSONString();
    }

    @Override
    public String getCpuAndMem(String clusterAlias) {
        return null;
    }
}
