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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import org.smartloli.kafka.eagle.common.util.*;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerFactory;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerService;
import org.smartloli.kafka.eagle.core.sql.tool.KSqlUtils;
import org.smartloli.kafka.eagle.core.task.metrics.WorkNodeMetrics;
import org.smartloli.kafka.eagle.core.task.parser.KSqlParser;
import org.smartloli.kafka.eagle.core.task.rpc.MasterNodeClient;
import org.smartloli.kafka.eagle.core.task.strategy.KSqlStrategy;
import org.smartloli.kafka.eagle.core.task.strategy.WorkNodeStrategy;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    private static BrokerService brokerService = new BrokerFactory().create();
    private static LRUCacheUtils cache = new LRUCacheUtils(1024);

    public JobClient(ConcurrentHashMap<String, Object> taskLogs) {
        this.taskLogs = taskLogs;
    }

    public static String physicsSubmit(String cluster, String sql, String jobId) {
        JSONObject status = new JSONObject();
        ErrorUtils.print(JobClient.class).info("JobClient - Physics KSQL[" + sql + "]");
        long start = System.currentTimeMillis();
        JSONObject resultObject = query(jobId, sql, cluster);
        String results = resultObject.getString("result");
        int rows = resultObject.getInteger("size");
        long end = System.currentTimeMillis();
        status.put("error", false);
        status.put("msg", results);
        status.put("spent", end - start);
        cache.put(jobId, "Time taken: " + (end - start) / 1000.0 + " seconds, Fetched: " + rows + " row(s)");
        return status.toString();
    }

    public static String logicalSubmit(String cluster, String sql, String jobId) {
        JSONObject status = new JSONObject();
        ErrorUtils.print(JobClient.class).info("JobClient - Logical KSQL[" + sql + "]");
        if (validForSql(sql)) {
            KSqlStrategy ksql = KSqlParser.parseQueryKSql(sql, cluster);
            if (!validForTopic(cluster, ksql.getTopic())) {
                status.put("error", true);
                status.put("msg", "ERROR - Topic[" + ksql.getTopic() + "] not exist.");
            } else {
                status.put("error", false);
                status.put("spent", 0);
                status.put("columns", ksql.getColumns());
            }
        } else {
            status.put("error", true);
            status.put("msg", "ERROR - KSQL[" + sql + "] has error, please start with select.");
        }

        return status.toString();
    }

    private static boolean validForSql(String sql) {
        if (sql.toLowerCase().contains("select")) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean validForTopic(String clusterAlias, String topic) {
        return brokerService.findKafkaTopic(clusterAlias, topic);
    }

    public static List<WorkNodeMetrics> getWorkNodeMetrics() {
        List<WorkNodeMetrics> metrics = new ArrayList<>();
        int id = 1;
        for (WorkNodeStrategy workNode : getWorkNodes()) {
            WorkNodeMetrics workNodeMetrics = new WorkNodeMetrics();
            workNodeMetrics.setId(id);
            workNodeMetrics.setHost(workNode.getHost());
            workNodeMetrics.setPort(workNode.getPort());
            if (NetUtils.telnet(workNode.getHost(), workNode.getPort())) {
                JSONObject object = new JSONObject();
                object.put(KConstants.Protocol.KEY, KConstants.Protocol.HEART_BEAT);
                List<JSONArray> results = new ArrayList<>();
                String resultStr = MasterNodeClient.getResult(workNode.getHost(), workNode.getPort(), object);
                try {
                    if (!StrUtils.isNull(resultStr)) {
                        results = JSON.parseArray(resultStr, JSONArray.class);
                    }
                } catch (Exception e) {
                    ErrorUtils.print(JobClient.class).error("Deserialize result by [" + workNode.getHost() + ":" + workNode.getPort() + "] has error, msg is ", e);
                }
                if (results.size() > 0) {
                    if (results.get(0).size() > 0) {
                        JSONObject result = (JSONObject) results.get(0).get(0);
                        workNodeMetrics.setAlive(true);
                        workNodeMetrics.setCpu(result.getString("cpu"));
                        workNodeMetrics.setMemory(result.getString("memory"));
                        workNodeMetrics.setStartTime(result.getString("created"));
                    }
                }
            } else {
                workNodeMetrics.setAlive(false);
            }
            id++;
            metrics.add(workNodeMetrics);
        }
        return metrics;
    }

    public static String getWorkNodeTaskLogs(String jobId) {
        String logs = "";
        for (WorkNodeStrategy workNode : getWorkNodesAlive()) {
            if (NetUtils.telnet(workNode.getHost(), workNode.getPort())) {
                JSONObject object = new JSONObject();
                object.put(KConstants.Protocol.KEY, KConstants.Protocol.KSQL_QUERY_LOG);
                object.put(KConstants.Protocol.JOB_ID, jobId);
                List<JSONArray> results = new ArrayList<>();
                String resultStr = MasterNodeClient.getResult(workNode.getHost(), workNode.getPort(), object);
                try {
                    if (!StrUtils.isNull(resultStr)) {
                        results = JSON.parseArray(resultStr, JSONArray.class);
                    }
                } catch (Exception e) {
                    ErrorUtils.print(JobClient.class).error("Deserialize result by [" + workNode.getHost() + ":" + workNode.getPort() + "] has error, msg is ", e);
                }
                if (results.size() > 0) {
                    if (results.get(0).size() > 0) {
                        JSONObject result = (JSONObject) results.get(0).get(0);
                        logs = result.getString("log") + "\n" + cache.get(jobId) + "\n";
                    }
                }
            }
        }
        return logs;
    }

    private static JSONObject query(String jobId, String sql, String cluster) {
        List<JSONArray> result = submit(jobId, sql, cluster);
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

    public static List<JSONArray> submit(String jobId, String sql, String cluster) {
        Map<WorkNodeStrategy, List<KSqlStrategy>> tasks = getTaskStrategy(sql, cluster);
        ErrorUtils.print(JobClient.class).info("KSqlStrategy: " + new Gson().toJson(tasks));
        List<JSONArray> parentResult = new ArrayList<>();
        for (Map.Entry<WorkNodeStrategy, List<KSqlStrategy>> task : tasks.entrySet()) {
            for (KSqlStrategy ksql : task.getValue()) {
                ksql.setJobId(jobId);
                List<JSONArray> daughterResult = WorkerScheduleTask.evaluate(ksql, task.getKey());
                if (daughterResult != null && daughterResult.size() > 0) {
                    parentResult.addAll(daughterResult);
                }
            }
        }
        return parentResult;
    }

    private static Map<WorkNodeStrategy, List<KSqlStrategy>> getTaskStrategy(String sql, String cluster) {
        // List<KSqlStrategy> tasks = new ArrayList<>();
        Map<WorkNodeStrategy, List<KSqlStrategy>> workNodeTasks = new HashMap<>();
        KSqlStrategy ksql = KSqlParser.parseQueryKSql(sql, cluster);
        List<WorkNodeStrategy> workNodes = getWorkNodesAlive();
        if (workNodes.size() == 0) {
            return workNodeTasks;
        }
        for (int partitionId : ksql.getPartitions()) {
            long endLogSize = 0L;
            long endRealLogSize = 0L;
            if ("kafka".equals(SystemConfigUtils.getProperty(cluster + ".kafka.eagle.offset.storage"))) {
                endLogSize = kafkaService.getKafkaLogSize(cluster, ksql.getTopic(), partitionId);
                endRealLogSize = kafkaService.getKafkaRealLogSize(cluster, ksql.getTopic(), partitionId);
            } else {
                endLogSize = kafkaService.getLogSize(cluster, ksql.getTopic(), partitionId);
                endRealLogSize = kafkaService.getRealLogSize(cluster, ksql.getTopic(), partitionId);
            }

            long startLogSize = endLogSize - endRealLogSize;
            long numberPer = endRealLogSize / workNodes.size();
            for (int workNodeIndex = 0; workNodeIndex < workNodes.size(); workNodeIndex++) {
                KSqlStrategy kSqlStrategy = new KSqlStrategy();
                if (workNodeIndex == (workNodes.size() - 1)) {
                    kSqlStrategy.setStart(workNodeIndex * numberPer + startLogSize + 1);
                    kSqlStrategy.setEnd(endLogSize);
                } else {
                    if (workNodeIndex == 0) {
                        kSqlStrategy.setStart(numberPer * workNodeIndex + startLogSize);
                    } else {
                        kSqlStrategy.setStart(numberPer * workNodeIndex + startLogSize + 1);
                    }
                    kSqlStrategy.setEnd(numberPer * (workNodeIndex + 1) + startLogSize);
                }
                kSqlStrategy.setPartition(partitionId);
                kSqlStrategy.setCluster(cluster);
                kSqlStrategy.setTopic(ksql.getTopic());
                kSqlStrategy.setLimit(ksql.getLimit());
                kSqlStrategy.setFieldSchema(ksql.getFieldSchema());
                WorkNodeStrategy worknode = new WorkNodeStrategy();
                worknode.setHost(workNodes.get(workNodeIndex).getHost());
                worknode.setPort(workNodes.get(workNodeIndex).getPort());
                if (workNodeTasks.containsKey(worknode)) {
                    workNodeTasks.get(worknode).add(kSqlStrategy);
                } else {
                    workNodeTasks.put(worknode, Arrays.asList(kSqlStrategy));
                }
            }
        }
        return workNodeTasks;
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

    private static List<WorkNodeStrategy> getWorkNodesAlive() {
        List<WorkNodeStrategy> nodes = new ArrayList<>();
        List<String> hosts = WorkUtils.getWorkNodes();
        int port = SystemConfigUtils.getIntProperty("kafka.eagle.sql.worknode.port");
        for (String host : hosts) {
            WorkNodeStrategy wns = new WorkNodeStrategy();
            wns.setHost(host);
            wns.setPort(port);
            if (NetUtils.telnet(host, port)) {
                nodes.add(wns); // alive node
            }
        }
        return nodes;
    }

}
