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
package org.smartloli.kafka.eagle.core.task.schedule;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.common.util.*;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.sql.tool.KSqlUtils;
import org.smartloli.kafka.eagle.core.task.metrics.WorkNodeMetrics;
import org.smartloli.kafka.eagle.core.task.parser.KSqlParser;
import org.smartloli.kafka.eagle.core.task.rpc.MasterNodeClient;
import org.smartloli.kafka.eagle.core.task.strategy.KSqlStrategy;
import org.smartloli.kafka.eagle.core.task.strategy.WorkNodeStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * The client generate the query strategy, initializes the master query task,
 * and send the schedule task to the worker through the master.
 *
 * @author smartloli.
 * <p>
 * Created by Sep 11, 2020
 */
public class JobClient {

    private ConcurrentHashMap<String, Object> taskLogs = new ConcurrentHashMap<>();
    private static KafkaService kafkaService = new KafkaFactory().create();

    public JobClient(ConcurrentHashMap<String, Object> taskLogs) {
        this.taskLogs = taskLogs;
    }

    public static void main(String[] args) {
        System.out.println(getWorkNodeMetrics());
    }

    private static void testQueryTopic() {
        String sql = "select * from kjson where `partition` in (0,1,2) and JSON(msg,'id')='1' limit 10";
        String cluster = "cluster1";
        long start = System.currentTimeMillis();
        JSONObject resultObject = query(sql, cluster);
        String results = resultObject.getString("result");
        int rows = resultObject.getInteger("size");
        long end = System.currentTimeMillis();
        JSONObject status = new JSONObject();
        status.put("error", false);
        status.put("msg", results);
        status.put("status", "Time taken: " + (end - start) / 1000.0 + " seconds, Fetched: " + rows + " row(s)");
        status.put("spent", end - start);
        System.out.println("Result: " + status);
    }

    public static List<WorkNodeMetrics> getWorkNodeMetrics() {
        List<WorkNodeMetrics> metrics = new ArrayList<>();
        for (WorkNodeStrategy workNode : getWorkNodes()) {
            WorkNodeMetrics workNodeMetrics = new WorkNodeMetrics();
            if (NetUtils.telnet(workNode.getHost(), workNode.getPort())) {
                JSONObject object = new JSONObject();
                object.put(KConstants.Protocol.KEY, KConstants.Protocol.HEART_BEAT);
                MasterNodeClient masterCli = new MasterNodeClient(workNode.getHost(), workNode.getPort(), object);
                try {
                    masterCli.start();
                } catch (Exception e) {
                    ErrorUtils.print(JobClient.class).error("Submit worknode[" + workNode.getHost() + ":" + workNode.getPort() + "] has error, msg is ", e);
                }
                List<JSONArray> results = masterCli.getResult();
                if (results.size() > 0) {
                    if (results.get(0).size() > 0) {
                        JSONObject result = (JSONObject) results.get(0).get(0);
                        workNodeMetrics.setAlive(true);
                        workNodeMetrics.setCpu(result.getDouble("cpu"));
                        workNodeMetrics.setMemoryUser(result.getLong("mem_used"));
                        workNodeMetrics.setMemoryMax(result.getLong("mem_max"));
                    }
                }
            } else {
                workNodeMetrics.setAlive(false);
            }
            metrics.add(workNodeMetrics);
        }
        return metrics;
    }

    public static JSONObject query(String sql, String cluster) {
        List<JSONArray> result = submit(sql, cluster);
        KSqlStrategy ksql = KSqlParser.parseQueryKSql(sql, cluster);
        JSONObject resultObject = new JSONObject();
        try {
            resultObject = KSqlUtils.query(getTableSchema(), ksql.getTopic(), result, sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    private static JSONObject getTableSchema() {
        JSONObject schema = new JSONObject();
        schema.put("partition", "integer");
        schema.put("offset", "bigint");
        schema.put("msg", "varchar");
        schema.put("timespan", "varchar");
        schema.put("date", "varchar");
        return schema;
    }

    public static List<JSONArray> submit(String sql, String cluster) {
        List<KSqlStrategy> tasks = getTaskStrategy(sql, cluster);

        //Stats tasks finish
        CountDownLatch countDownLatch = new CountDownLatch(tasks.size());
        MasterSchedule master = new MasterSchedule(new WorkerSchedule(), getWorkNodes(), countDownLatch);

        for (KSqlStrategy ksql : tasks) {
            master.submit(ksql);
        }

        master.execute();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return master.getResult();
    }

    private static List<KSqlStrategy> getTaskStrategy(String sql, String cluster) {
        List<KSqlStrategy> tasks = new ArrayList<>();
        KSqlStrategy ksql = KSqlParser.parseQueryKSql(sql, cluster);
        long limit = ksql.getLimit();
        int workNodeSize = getWorkNodes().size();
        for (int partitionId : ksql.getPartitions()) {
            long endLogSize = 0L;
            if ("kafka".equals(SystemConfigUtils.getProperty(cluster + ".kafka.eagle.offset.storage"))) {
                endLogSize = kafkaService.getKafkaLogSize(cluster, ksql.getTopic(), partitionId);
            } else {
                endLogSize = kafkaService.getLogSize(cluster, ksql.getTopic(), partitionId);
            }

            long numberPer = endLogSize / workNodeSize;
            for (int workNodeIndex = 0; workNodeIndex < workNodeSize; workNodeIndex++) {
                KSqlStrategy kSqlStrategy = new KSqlStrategy();
                if (workNodeIndex == (workNodeSize - 1)) {
                    kSqlStrategy.setStart(workNodeIndex * numberPer);
                    kSqlStrategy.setEnd(endLogSize);
                } else {
                    kSqlStrategy.setStart(workNodeIndex * numberPer);
                    kSqlStrategy.setEnd((numberPer * (workNodeIndex + 1)) - 1);
                }
                kSqlStrategy.setPartition(partitionId);
                kSqlStrategy.setCluster(cluster);
                kSqlStrategy.setTopic(ksql.getTopic());
                kSqlStrategy.setLimit(ksql.getLimit());
                kSqlStrategy.setFieldSchema(ksql.getFieldSchema());
                tasks.add(kSqlStrategy);
            }
        }
        return tasks;
    }

    private static List<WorkNodeStrategy> getWorkNodes() {
        List<WorkNodeStrategy> nodes = new ArrayList<>();
        List<String> hosts = WorkUtils.getWorkNodes();
        int port = SystemConfigUtils.getIntProperty("kafka.eagle.sql.worknode.port");
        for (String host : hosts) {
            WorkNodeStrategy wns = new WorkNodeStrategy();
            wns.setHost(host);
            wns.setPort(port);
            nodes.add(wns);
        }
        return nodes;
    }
}
