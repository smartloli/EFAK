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
package org.smartloli.kafka.eagle.core.sql.execute;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.protocol.KafkaSqlInfo;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerFactory;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerService;
import org.smartloli.kafka.eagle.core.sql.tool.KSqlUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pre processing the SQL submitted by the client.
 *
 * @author smartloli.
 * <p>
 * Created by Feb 28, 2017
 */
public class KafkaSqlParser {

    private final static Logger LOG = LoggerFactory.getLogger(KafkaSqlParser.class);
    private static KafkaService kafkaService = new KafkaFactory().create();
    private static BrokerService brokerService = new BrokerFactory().create();

    public static String execute(String clusterAlias, String sql) {
        JSONObject status = new JSONObject();
        try {
            KafkaSqlInfo kafkaSql = kafkaService.parseSql(clusterAlias, sql);
            LOG.info("KafkaSqlParser - SQL[" + kafkaSql.getSql() + "]");
            if (kafkaSql.isStatus()) {
                if (!hasTopic(clusterAlias, kafkaSql)) {
                    status.put("error", true);
                    status.put("msg", "ERROR - Topic[" + kafkaSql.getTableName() + "] not exist.");
                } else {
                    long start = System.currentTimeMillis();
                    kafkaSql.setClusterAlias(clusterAlias);
                    List<JSONArray> dataSets = KafkaConsumerAdapter.executor(kafkaSql);
                    String results = "";
                    int rows = 0;
                    if (dataSets.size() > 0 && !dataSets.get(dataSets.size() - 1).isEmpty()) {
                        JSONObject object = KSqlUtils.query(kafkaSql.getSchema(), kafkaSql.getTableName(), dataSets, kafkaSql.getSql());
                        results = object.getString("result");
                        rows = object.getInteger("size");
                    } else {
                        List<Map<String, Object>> schemas = new ArrayList<Map<String, Object>>();
                        Map<String, Object> map = new HashMap<>();
                        map.put("NULL", "");
                        schemas.add(map);
                        results = new Gson().toJson(schemas);
                    }
                    long end = System.currentTimeMillis();
                    status.put("error", false);
                    status.put("msg", results);
                    status.put("status", "Time taken: " + (end - start) / 1000.0 + " seconds, Fetched: " + rows + " row(s)");
                    status.put("spent", end - start);
                }
            } else {
                status.put("error", true);
                status.put("msg", "ERROR - SQL[" + kafkaSql.getSql() + "] has error,please start with select.");
            }
        } catch (Exception e) {
            status.put("error", true);
            status.put("msg", e.getMessage());
            e.printStackTrace();
            LOG.error("Execute sql to query kafka topic has error,msg is " + e.getMessage());
        }
        return status.toJSONString();
    }

    private static boolean hasTopic(String clusterAlias, KafkaSqlInfo kafkaSql) {
        boolean status = brokerService.findKafkaTopic(clusterAlias, kafkaSql.getTableName());
        if (status) {
            kafkaSql.setTopic(kafkaSql.getTableName());
        }
        return status;
    }
}
