/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.core.factory.hub;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map.Entry;

import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.protocol.topic.ReassignPartitionInfo;
import org.smartloli.kafka.eagle.common.util.KafkaZKPoolUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import kafka.admin.ReassignPartitionsCommand;
import kafka.zk.KafkaZkClient;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.JavaConverters;
import scala.collection.Map;
import scala.collection.Seq;

/**
 * Implements {@link KafkaHubService} all method.
 * 
 * @author smartloli.
 *
 *         Created by May 21, 2020
 */
public class KafkaHubServiceImpl implements KafkaHubService {

	private final Logger LOG = LoggerFactory.getLogger(KafkaHubServiceImpl.class);

	/** Instance Kafka Zookeeper client pool. */
	private KafkaZKPoolUtils kafkaZKPool = KafkaZKPoolUtils.getInstance();

	/**
	 * Reassign partitions.
	 * 
	 * @param reassignTopics
	 *            {"topics":[{"topic":"k20200326"}],"version":1}
	 * 
	 * @param brokerIdList
	 *            0,1,2 ...
	 * 
	 * 
	 */
	public void reassignPartitions(String clusterAlias, String reassignTopicsJson, List<Object> brokerIdList) {
		Seq<Object> seq = JavaConverters.asScalaIteratorConverter(brokerIdList.iterator()).asScala().toSeq();
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		Tuple2<Map<TopicPartition, Seq<Object>>, Map<TopicPartition, Seq<Object>>> tuple = ReassignPartitionsCommand.generateAssignment(zkc, seq, reassignTopicsJson, true);
		LOG.info("Proposed partition reassignment configuartion");
		try {
			LOG.info(Files.readLines(createKafkaTempJson(tuple._1).getAbsoluteFile(), Charsets.UTF_8).toString());
		} catch (Exception e) {
			LOG.error("Read proposed partition reassignment configuartion has error,msg is ", e);
		}
		LOG.info("Current partition replica assignment");
		try {
			LOG.info(Files.readLines(createKafkaTempJson(tuple._2).getAbsoluteFile(), Charsets.UTF_8).toString());
		} catch (Exception e) {
			LOG.error("Read current partition replica assignment has error,msg is ", e);
		}
		ReassignPartitionInfo rpi = new ReassignPartitionInfo();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream pStream = new PrintStream(baos);
		PrintStream oldPStream = System.out;
		System.setOut(pStream);
		try {
			ReassignPartitionsCommand.main(new String[] { "--zookeeper", "127.0.0.1:2181", "--reassignment-json-file", createKafkaTempJson(tuple._2).getAbsolutePath(), "--verify" });
		} catch (Exception e) {
			LOG.error("Execute command has error, msg is ", e);
		}
		System.out.flush();
		System.setOut(oldPStream);
		LOG.info("Result:\n" + baos.toString());
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
	}

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
		File f = File.createTempFile("ke_reassignmengt_", ".json");
		FileWriter out = new FileWriter(f);
		out.write(object.toJSONString());
		out.close();
		f.deleteOnExit();
		return f;
	}

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
				object.put("proposed", Files.readLines(createKafkaTempJson(tuple._1).getAbsoluteFile(), Charsets.UTF_8).toString());
				object.put("proposed_status", true);
			} catch (Exception e) {
				LOG.error("Read proposed partition reassignment configuartion has error,msg is ", e);
				object.put("error_proposed", "Read proposed partition reassignment configuartion has error,msg is " + e.getCause().getMessage());
			}
			try {
				object.put("current", Files.readLines(createKafkaTempJson(tuple._2).getAbsoluteFile(), Charsets.UTF_8).toString());
				object.put("current_status", true);
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

}
