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
package org.smartloli.kafka.eagle.core.factory.hub;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import kafka.admin.PreferredReplicaLeaderElectionCommand;
import kafka.admin.ReassignPartitionsCommand;
import kafka.zk.KafkaZkClient;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.protocol.BrokersInfo;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.KafkaZKPoolUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.JavaConverters;
import scala.collection.Map;
import scala.collection.Seq;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Implements {@link KafkaHubService} all method.
 *
 * @author smartloli.
 * <p>
 * Created by May 21, 2020
 */
public class KafkaHubServiceImpl implements KafkaHubService {

    private final Logger LOG = LoggerFactory.getLogger(KafkaHubServiceImpl.class);
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private final PrintStream pStream = new PrintStream(baos);
    private final static String KE_REASSIGNMENT_TOPIC = "ke_reassignment_topic_";
    private final static String KE_ELECTION_TOPIC = "ke_election_topic";

    /**
     * Instance Kafka Zookeeper client pool.
     */
    private KafkaZKPoolUtils kafkaZKPool = KafkaZKPoolUtils.getInstance();

    /**
     * Kafka service interface.
     */
    private KafkaService kafkaService = new KafkaFactory().create();

    private File createKafkaTempJson(Map<TopicPartition, Seq<Object>> tuple) throws IOException {
        JSONObject object = new JSONObject();
        object.put("version", 1);
        JSONArray array = new JSONArray();
        for (Entry<TopicPartition, Seq<Object>> entry : JavaConversions.mapAsJavaMap(tuple).entrySet()) {
            List<Object> replicas = JavaConversions.seqAsJavaList(entry.getValue());
            JSONObject tpObject = new JSONObject();
            tpObject.put("topic", entry.getKey().topic());
            tpObject.put("partition", entry.getKey().partition());
            tpObject.put("replicas", replicas);
            array.add(tpObject);
        }
        object.put("partitions", array);
        File f = File.createTempFile("ke_reassignment_", ".json");
        FileWriter out = new FileWriter(f);
        out.write(object.toJSONString());
        out.close();
        f.deleteOnExit();
        return f;
    }

    private File createKafkaTopicTempJson(String reassignTopicsJson, String filePrefix) throws IOException {
        File f = File.createTempFile(filePrefix, ".json");
        FileWriter out = new FileWriter(f);
        out.write(JSON.parseObject(reassignTopicsJson).toJSONString());
        out.close();
        f.deleteOnExit();
        return f;
    }

    /**
     * Generate reassign topics json
     *
     * @param reassignTopicsJson {"topics":[{"topic":"k20200326"}],"version":1}
     * @param brokerIdList       0,1,2 ...
     */
    public JSONObject generate(String clusterAlias, String reassignTopicsJson, List<Object> brokerIdList) {
        JSONObject object = new JSONObject();
        KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
        Seq<Object> seq = JavaConverters.asScalaIteratorConverter(brokerIdList.iterator()).asScala().toSeq();
        Tuple2<Map<TopicPartition, Seq<Object>>, Map<TopicPartition, Seq<Object>>> tuple = null;
        try {
            tuple = ReassignPartitionsCommand.generateAssignment(zkc, seq, reassignTopicsJson, true);
        } catch (Exception e) {
            LOG.error("Execute command has error, msg is ", e);
            object.put("error_result", "Execute command has error, msg is " + e);
        }
        if (tuple != null) {
            try {
                List<String> jsons = Files.readLines(createKafkaTempJson(tuple._1).getAbsoluteFile(), Charsets.UTF_8);
                if (jsons.size() > 0) {
                    object.put("proposed", jsons.get(0).toString());
                    object.put("proposed_status", true);
                } else {
                    object.put("proposed_status", false);
                }
            } catch (Exception e) {
                LOG.error("Read proposed partition reassignment configuartion has error,msg is ", e);
                object.put("error_proposed", "Read proposed partition reassignment configuartion has error,msg is " + e.getCause().getMessage());
            }
            try {
                List<String> jsons = Files.readLines(createKafkaTempJson(tuple._2).getAbsoluteFile(), Charsets.UTF_8);
                if (jsons.size() > 0) {
                    object.put("current", jsons.get(0).toString());
                    object.put("current_status", true);
                } else {
                    object.put("current_status", false);
                }
            } catch (Exception e) {
                LOG.error("Read current partition replica assignment has error,msg is ", e);
                object.put("error_current", "Read current partition replica assignment has error,msg is " + e.getCause().getMessage());
            }
        }
        if (zkc != null) {
            kafkaZKPool.release(clusterAlias, zkc);
            zkc = null;
        }
        return object;
    }

    /**
     * Execute reassign topics json.
     */
    public String execute(String clusterAlias, String reassignTopicsJson) {
        JSONObject object = new JSONObject();
        String zkServerAddress = SystemConfigUtils.getProperty(clusterAlias + ".zk.list");
        PrintStream oldPStream = System.out;
        System.setOut(pStream);
        try {
            ReassignPartitionsCommand.main(new String[]{"--zookeeper", zkServerAddress, "--reassignment-json-file", createKafkaTopicTempJson(reassignTopicsJson, KE_REASSIGNMENT_TOPIC).getAbsolutePath(), "--execute"});
            object.put("success", true);
        } catch (Exception e) {
            object.put("error", "Execute command has error, msg is " + e.getCause().getMessage());
            LOG.error("Execute command has error, msg is ", e);
        }
        System.out.flush();
        System.setOut(oldPStream);
        object.put("result", baos.toString());
        return object.toJSONString();
    }

    /**
     * Verify reassign topics json.
     */
    public String verify(String clusterAlias, String reassignTopicsJson) {
        JSONObject object = new JSONObject();
        String zkServerAddress = SystemConfigUtils.getProperty(clusterAlias + ".zk.list");
        PrintStream oldPStream = System.out;
        System.setOut(pStream);
        try {
            ReassignPartitionsCommand.main(new String[]{"--zookeeper", zkServerAddress, "--reassignment-json-file", createKafkaTopicTempJson(reassignTopicsJson, KE_REASSIGNMENT_TOPIC).getAbsolutePath(), "--verify"});
        } catch (Exception e) {
            object.put("error", "Verify command has error, msg is " + e.getCause().getMessage());
            LOG.error("Verify command has error, msg is ", e);
        }
        System.out.flush();
        System.setOut(oldPStream);
        object.put("result", baos.toString());
        return object.toJSONString();
    }

    @Override
    public String prefReplicaElection(String clusterAlias, String topic) {
        JSONObject object = new JSONObject();
        object.put("type", KConstants.Topic.ELECTION);
        String zkServerAddress = SystemConfigUtils.getProperty(clusterAlias + ".zk.list");

        Properties prop = new Properties();
        prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, parseBrokerServer(clusterAlias));

        if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.sasl.enable")) {
            kafkaService.sasl(prop, clusterAlias);
        }
        if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.ssl.enable")) {
            kafkaService.ssl(prop, clusterAlias);
        }
        AdminClient adminClient = null;

        HashMap<String, List<HashMap<String, Object>>> totalTopics = new HashMap<>();
        ArrayList<HashMap<String, Object>> totalPartitions = new ArrayList<>();
        try {
            adminClient = AdminClient.create(prop);
            List<Integer> partitionsList = adminClient.describeTopics(Collections.singleton(topic))
                    .all().get().get(topic).partitions().stream().map(p -> p.partition()).collect(Collectors.toList());
            for (int i = 0; i < partitionsList.size(); i++) {
                HashMap<String, Object> partition = new HashMap<>();
                partition.put("topic", topic);
                partition.put("partition", partitionsList.get(i));
                totalPartitions.add(partition);
            }
            totalTopics.put("partitions", totalPartitions);
            String totalPartitionsForPrefElection = JSON.toJSON(totalTopics).toString();
            PreferredReplicaLeaderElectionCommand.main(new String[]{"--zookeeper", zkServerAddress, "--path-to-json-file", createKafkaTopicTempJson(totalPartitionsForPrefElection, KE_ELECTION_TOPIC).getAbsolutePath()});
            object.put("value", "Execute preferred replica leader election command successfully for topic " + topic);
        } catch (Exception e) {
            object.put("value", "Execute preferred replica leader election command has error, msg is " + e.getCause().getMessage());
            LOG.error("Execute preferred replica leader election command for topic {} has error, msg is {}", topic, e);
        }

        if (adminClient != null) {
            adminClient.close();
        }

        return object.toJSONString();
    }

    private String parseBrokerServer(String clusterAlias) {
        String brokerServer = "";
        List<BrokersInfo> brokers = kafkaService.getAllBrokersInfo(clusterAlias);
        for (BrokersInfo broker : brokers) {
            brokerServer += broker.getHost() + ":" + broker.getPort() + ",";
        }
        if ("".equals(brokerServer)) {
            return "";
        }
        return brokerServer.substring(0, brokerServer.length() - 1);
    }

}
