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
package org.smartloli.kafka.eagle.core.factory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.DescribeConsumerGroupsResult;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsOptions;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.admin.ListConsumerGroupsResult;
import org.apache.kafka.clients.admin.MemberDescription;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.constant.JmxConstants.BrokerServer;
import org.smartloli.kafka.eagle.common.constant.JmxConstants.KafkaServer8;
import org.smartloli.kafka.eagle.common.constant.OdpsSqlParser;
import org.smartloli.kafka.eagle.common.protocol.BrokersInfo;
import org.smartloli.kafka.eagle.common.protocol.DisplayInfo;
import org.smartloli.kafka.eagle.common.protocol.HostsInfo;
import org.smartloli.kafka.eagle.common.protocol.KafkaSqlInfo;
import org.smartloli.kafka.eagle.common.protocol.MetadataInfo;
import org.smartloli.kafka.eagle.common.protocol.OffsetZkInfo;
import org.smartloli.kafka.eagle.common.protocol.OwnerInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.JMXFactoryUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.CollectorType;
import org.smartloli.kafka.eagle.common.util.KConstants.Kafka;
import org.smartloli.kafka.eagle.common.util.KafkaPartitioner;
import org.smartloli.kafka.eagle.common.util.KafkaZKPoolUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import kafka.zk.KafkaZkClient;
import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.Seq;

/**
 * Implements KafkaService all method.
 * 
 * @author smartloli.
 *
 *         Created by Jan 18, 2017.
 * 
 *         Update by hexiang 20170216
 * 
 * @see org.smartloli.kafka.eagle.core.factory.KafkaService
 */
public class KafkaServiceImpl implements KafkaService {

	private final String BROKER_IDS_PATH = "/brokers/ids";
	private final String BROKER_TOPICS_PATH = "/brokers/topics";
	private final String CONSUMERS_PATH = "/consumers";
	private final String OWNERS = "/owners";
	private final String TOPIC_ISR = "/brokers/topics/%s/partitions/%s/state";
	private final Logger LOG = LoggerFactory.getLogger(KafkaServiceImpl.class);

	/** Instance Kafka Zookeeper client pool. */
	private KafkaZKPoolUtils kafkaZKPool = KafkaZKPoolUtils.getInstance();

	/** Zookeeper service interface. */
	private ZkService zkService = new ZkFactory().create();

	/**
	 * Find topic and group exist in zookeeper.
	 * 
	 * @param topic
	 *            Filter topic.
	 * @param group
	 *            Filter group
	 * @return Boolean.
	 */
	public boolean findTopicAndGroupExist(String clusterAlias, String topic, String group) {
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		String ownersPath = CONSUMERS_PATH + "/" + group + "/owners/" + topic;
		boolean status = zkc.pathExists(ownersPath);
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return status;
	}

	/**
	 * Obtaining metadata in zookeeper by topic.
	 * 
	 * @param topic
	 *            Selected condition.
	 * @return List.
	 */
	public List<String> findTopicPartition(String clusterAlias, String topic) {
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		Seq<String> brokerTopicsPaths = zkc.getChildren(BROKER_TOPICS_PATH + "/" + topic + "/partitions");
		List<String> topicAndPartitions = JavaConversions.seqAsJavaList(brokerTopicsPaths);
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
			brokerTopicsPaths = null;
		}
		return topicAndPartitions;
	}

	/** Get kafka active consumer topic. */
	public Map<String, List<String>> getActiveTopic(String clusterAlias) {
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		Map<String, List<String>> actvTopics = new HashMap<String, List<String>>();
		try {
			Seq<String> subConsumerPaths = zkc.getChildren(CONSUMERS_PATH);
			List<String> groups = JavaConversions.seqAsJavaList(subConsumerPaths);
			JSONArray groupsAndTopics = new JSONArray();
			for (String group : groups) {
				Seq<String> topics = zkc.getChildren(CONSUMERS_PATH + "/" + group + OWNERS);
				for (String topic : JavaConversions.seqAsJavaList(topics)) {
					Seq<String> partitionIds = zkc.getChildren(CONSUMERS_PATH + "/" + group + OWNERS + "/" + topic);
					if (JavaConversions.seqAsJavaList(partitionIds).size() > 0) {
						JSONObject groupAndTopic = new JSONObject();
						groupAndTopic.put("topic", topic);
						groupAndTopic.put("group", group);
						groupsAndTopics.add(groupAndTopic);
					}
				}
			}
			for (Object object : groupsAndTopics) {
				JSONObject groupAndTopic = (JSONObject) object;
				String group = groupAndTopic.getString("group");
				String topic = groupAndTopic.getString("topic");
				if (actvTopics.containsKey(group + "_" + topic)) {
					actvTopics.get(group + "_" + topic).add(topic);
				} else {
					List<String> topics = new ArrayList<String>();
					topics.add(topic);
					actvTopics.put(group + "_" + topic, topics);
				}
			}
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
		} finally {
			if (zkc != null) {
				kafkaZKPool.release(clusterAlias, zkc);
				zkc = null;
			}
		}
		return actvTopics;
	}

	/** Get kafka active consumer topic. */
	public Set<String> getActiveTopic(String clusterAlias, String group) {
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		Set<String> activeTopics = new HashSet<>();
		try {
			Seq<String> topics = zkc.getChildren(CONSUMERS_PATH + "/" + group + OWNERS);
			for (String topic : JavaConversions.seqAsJavaList(topics)) {
				activeTopics.add(topic);
			}
		} catch (Exception ex) {
			LOG.error("Get kafka active topic has error, msg is " + ex.getMessage());
			LOG.error(ex.getMessage());
		} finally {
			if (zkc != null) {
				kafkaZKPool.release(clusterAlias, zkc);
				zkc = null;
			}
		}
		return activeTopics;
	}

	/** Get all broker list from zookeeper. */
	public List<BrokersInfo> getAllBrokersInfo(String clusterAlias) {
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		List<BrokersInfo> targets = new ArrayList<BrokersInfo>();
		if (zkc.pathExists(BROKER_IDS_PATH)) {
			Seq<String> subBrokerIdsPaths = zkc.getChildren(BROKER_IDS_PATH);
			List<String> brokerIdss = JavaConversions.seqAsJavaList(subBrokerIdsPaths);
			int id = 0;
			for (String ids : brokerIdss) {
				try {
					Tuple2<Option<byte[]>, Stat> tuple = zkc.getDataAndStat(BROKER_IDS_PATH + "/" + ids);
					BrokersInfo broker = new BrokersInfo();
					broker.setCreated(CalendarUtils.convertUnixTime2Date(tuple._2.getCtime()));
					broker.setModify(CalendarUtils.convertUnixTime2Date(tuple._2.getMtime()));
					String tupleString = new String(tuple._1.get());
					if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.sasl.enable")) {
						String endpoints = JSON.parseObject(tupleString).getString("endpoints");
						String tmp = endpoints.split("//")[1];
						broker.setHost(tmp.substring(0, tmp.length() - 2).split(":")[0]);
						broker.setPort(Integer.valueOf(tmp.substring(0, tmp.length() - 2).split(":")[1]));
					} else {
						String host = JSON.parseObject(tupleString).getString("host");
						int port = JSON.parseObject(tupleString).getInteger("port");
						broker.setHost(host);
						broker.setPort(port);
					}
					broker.setJmxPort(JSON.parseObject(tupleString).getInteger("jmx_port"));
					broker.setId(++id);
					broker.setIds(ids);
					targets.add(broker);
				} catch (Exception ex) {
					LOG.error(ex.getMessage());
				}
			}
		}
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return targets;
	}

	/** Obtaining kafka consumer information from zookeeper. */
	public Map<String, List<String>> getConsumers(String clusterAlias) {
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		Map<String, List<String>> consumers = new HashMap<String, List<String>>();
		try {
			Seq<String> subConsumerPaths = zkc.getChildren(CONSUMERS_PATH);
			List<String> groups = JavaConversions.seqAsJavaList(subConsumerPaths);
			for (String group : groups) {
				String path = CONSUMERS_PATH + "/" + group + "/owners";
				if (zkc.pathExists(path)) {
					Seq<String> owners = zkc.getChildren(path);
					List<String> ownersSerialize = JavaConversions.seqAsJavaList(owners);
					consumers.put(group, ownersSerialize);
				} else {
					LOG.error("Consumer Path[" + path + "] is not exist.");
				}
			}
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
		} finally {
			if (zkc != null) {
				kafkaZKPool.release(clusterAlias, zkc);
				zkc = null;
			}
		}
		return consumers;
	}

	/** Obtaining kafka consumer page information from zookeeper. */
	public Map<String, List<String>> getConsumers(String clusterAlias, DisplayInfo page) {
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		Map<String, List<String>> consumers = new HashMap<String, List<String>>();
		try {
			if (page.getSearch().length() > 0) {
				String path = CONSUMERS_PATH + "/" + page.getSearch() + "/owners";
				if (zkc.pathExists(path)) {
					Seq<String> owners = zkc.getChildren(path);
					List<String> ownersSerialize = JavaConversions.seqAsJavaList(owners);
					consumers.put(page.getSearch(), ownersSerialize);
				} else {
					LOG.error("Consumer Path[" + path + "] is not exist.");
				}
			} else {
				Seq<String> subConsumersPaths = zkc.getChildren(CONSUMERS_PATH);
				List<String> groups = JavaConversions.seqAsJavaList(subConsumersPaths);
				int offset = 0;
				for (String group : groups) {
					if (offset < (page.getiDisplayLength() + page.getiDisplayStart()) && offset >= page.getiDisplayStart()) {
						String path = CONSUMERS_PATH + "/" + group + "/owners";
						if (zkc.pathExists(path)) {
							Seq<String> owners = zkc.getChildren(path);
							List<String> ownersSerialize = JavaConversions.seqAsJavaList(owners);
							consumers.put(group, ownersSerialize);
						} else {
							LOG.error("Consumer Path[" + path + "] is not exist.");
						}
					}
					offset++;
				}
			}
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
		} finally {
			if (zkc != null) {
				kafkaZKPool.release(clusterAlias, zkc);
				zkc = null;
			}
		}
		return consumers;
	}

	/**
	 * According to group, topic and partition to get offset from zookeeper.
	 * 
	 * @param topic
	 *            Filter topic.
	 * @param group
	 *            Filter group.
	 * @param partition
	 *            Filter partition.
	 * @return OffsetZkInfo.
	 * 
	 * @see org.smartloli.kafka.eagle.domain.OffsetZkInfo
	 */
	public OffsetZkInfo getOffset(String clusterAlias, String topic, String group, int partition) {
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		OffsetZkInfo offsetZk = new OffsetZkInfo();
		String offsetPath = CONSUMERS_PATH + "/" + group + "/offsets/" + topic + "/" + partition;
		String ownersPath = CONSUMERS_PATH + "/" + group + "/owners/" + topic + "/" + partition;
		Tuple2<Option<byte[]>, Stat> tuple = null;
		try {
			if (zkc.pathExists(offsetPath)) {
				tuple = zkc.getDataAndStat(offsetPath);
			} else {
				LOG.info("Partition[" + partition + "],OffsetPath[" + offsetPath + "] is not exist!");
				if (zkc != null) {
					kafkaZKPool.release(clusterAlias, zkc);
					zkc = null;
				}
				return offsetZk;
			}
		} catch (Exception ex) {
			LOG.error("Partition[" + partition + "],get offset has error,msg is " + ex.getMessage());
			if (zkc != null) {
				kafkaZKPool.release(clusterAlias, zkc);
				zkc = null;
			}
			return offsetZk;
		}
		String tupleString = new String(tuple._1.get());
		long offsetSize = Long.parseLong(tupleString);
		if (zkc.pathExists(ownersPath)) {
			Tuple2<Option<byte[]>, Stat> tuple2 = zkc.getDataAndStat(ownersPath);
			String tupleString2 = new String(tuple2._1.get());
			offsetZk.setOwners(tupleString2 == null ? "" : tupleString2);
		} else {
			offsetZk.setOwners("");
		}
		offsetZk.setOffset(offsetSize);
		offsetZk.setCreate(CalendarUtils.convertUnixTime2Date(tuple._2.getCtime()));
		offsetZk.setModify(CalendarUtils.convertUnixTime2Date(tuple._2.getMtime()));
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return offsetZk;
	}

	/** According to topic and partition to obtain Replicas & Isr. */
	public String getReplicasIsr(String clusterAlias, String topic, int partitionid) {
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		TopicPartition tp = new TopicPartition(topic, partitionid);
		Seq<Object> replis = zkc.getReplicasForPartition(tp);
		List<Object> targets = JavaConversions.seqAsJavaList(replis);
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return targets.toString();
	}

	/** Get zookeeper cluster information. */
	public String zkCluster(String clusterAlias) {
		String[] zks = SystemConfigUtils.getPropertyArray(clusterAlias + ".zk.list", ",");
		JSONArray targets = new JSONArray();
		int id = 1;
		for (String zk : zks) {
			JSONObject object = new JSONObject();
			object.put("id", id++);
			object.put("ip", zk.split(":")[0]);
			object.put("port", zk.split(":")[1]);
			object.put("mode", zkService.status(zk.split(":")[0], zk.split(":")[1]));
			targets.add(object);
		}
		return targets.toJSONString();
	}

	/** Judge whether the zkcli is active. */
	public JSONObject zkCliStatus(String clusterAlias) {
		JSONObject target = new JSONObject();
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		if (zkc != null) {
			target.put("live", true);
			target.put("list", SystemConfigUtils.getProperty(clusterAlias + ".zk.list"));
		} else {
			target.put("live", false);
			target.put("list", SystemConfigUtils.getProperty(clusterAlias + ".zk.list"));
		}
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return target;
	}

	/**
	 * Create topic to kafka cluster, it is worth noting that the backup number
	 * must be less than or equal to brokers data.
	 * 
	 * @param topicName
	 *            Create topic name.
	 * @param partitions
	 *            Create topic partitions.
	 * @param replic
	 *            Replic numbers.
	 * @return Map.
	 */
	public Map<String, Object> create(String clusterAlias, String topicName, String partitions, String replic) {
		Map<String, Object> targets = new HashMap<String, Object>();
		int brokers = getAllBrokersInfo(clusterAlias).size();
		if (Integer.parseInt(replic) > brokers) {
			targets.put("status", "error");
			targets.put("info", "replication factor: " + replic + " larger than available brokers: " + brokers);
			return targets;
		}
		Properties prop = new Properties();
		prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, parseBrokerServer(clusterAlias));

		if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.sasl.enable")) {
			sasl(prop, clusterAlias);
		}

		AdminClient adminClient = null;
		try {
			adminClient = AdminClient.create(prop);
			NewTopic newTopic = new NewTopic(topicName, Integer.valueOf(partitions), Short.valueOf(replic));
			adminClient.createTopics(Collections.singleton(newTopic)).all().get();
		} catch (Exception e) {
			LOG.info("Create kafka topic has error, msg is " + e.getMessage());
			e.printStackTrace();
		} finally {
			adminClient.close();
		}

		targets.put("status", "success");
		targets.put("info", "Create topic[" + topicName + "] has successed,partitions numbers is [" + partitions + "],replication-factor numbers is [" + replic + "]");
		return targets;
	}

	/** Delete topic to kafka cluster. */
	public Map<String, Object> delete(String clusterAlias, String topicName) {
		Map<String, Object> targets = new HashMap<String, Object>();
		Properties prop = new Properties();
		prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, parseBrokerServer(clusterAlias));

		if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.sasl.enable")) {
			sasl(prop, clusterAlias);
		}

		AdminClient adminClient = null;
		try {
			adminClient = AdminClient.create(prop);
			adminClient.deleteTopics(Collections.singleton(topicName)).all().get();
			targets.put("status", "success");
		} catch (Exception e) {
			LOG.info("Delete kafka topic has error, msg is " + e.getMessage());
			e.printStackTrace();
			targets.put("status", "failed");
		} finally {
			adminClient.close();
		}
		return targets;
	}

	/** Get kafka brokers from zookeeper. */
	private List<HostsInfo> getBrokers(String clusterAlias) {
		List<HostsInfo> targets = new ArrayList<HostsInfo>();
		List<BrokersInfo> brokers = getAllBrokersInfo(clusterAlias);
		for (BrokersInfo broker : brokers) {
			HostsInfo host = new HostsInfo();
			host.setHost(broker.getHost());
			host.setPort(broker.getPort());
			targets.add(host);
		}
		return targets;
	}

	private String parseBrokerServer(String clusterAlias) {
		String brokerServer = "";
		List<BrokersInfo> brokers = getAllBrokersInfo(clusterAlias);
		for (BrokersInfo broker : brokers) {
			brokerServer += broker.getHost() + ":" + broker.getPort() + ",";
		}
		if ("".equals(brokerServer)) {
			return "";
		}
		return brokerServer.substring(0, brokerServer.length() - 1);
	}

	/** Convert query sql to object. */
	public KafkaSqlInfo parseSql(String clusterAlias, String sql) {
		return segments(clusterAlias, prepare(sql));
	}

	private String prepare(String sql) {
		sql = sql.trim();
		sql = sql.replaceAll("\\s+", " ");
		return sql;
	}

	/** Set topic sasl. */
	public void sasl(Properties props, String clusterAlias) {
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.sasl.protocol"));
		if (!"".equals(SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.sasl.client.id"))) {
			props.put(CommonClientConfigs.CLIENT_ID_CONFIG, SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.sasl.client.id"));
		}
		props.put(SaslConfigs.SASL_MECHANISM, SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.sasl.mechanism"));
		props.put(SaslConfigs.SASL_JAAS_CONFIG, SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.sasl.jaas.config"));
	}

	private KafkaSqlInfo segments(String clusterAlias, String sql) {
		KafkaSqlInfo kafkaSql = new KafkaSqlInfo();
		kafkaSql.setSql(sql);
		kafkaSql.getSchema().put("partition", "integer");
		kafkaSql.getSchema().put("offset", "bigint");
		kafkaSql.getSchema().put("msg", "varchar");
		if (!sql.startsWith("select") && !sql.startsWith("SELECT")) {
			kafkaSql.setStatus(false);
			return kafkaSql;
		} else {
			String tableName = OdpsSqlParser.parserTopic(sql);
			if (!"".equals(tableName)) {
				kafkaSql.setStatus(true);
				kafkaSql.setTableName(tableName);
			}
			sql = sql.toLowerCase();
			if (sql.contains("and")) {
				sql = sql.split("and")[0];
			} else if (sql.contains("group by")) {
				sql = sql.split("group")[0];
			} else if (sql.contains("limit")) {
				sql = sql.split("limit")[0];
			}

			Matcher matcher = Pattern.compile("select\\s.+from\\s(.+)where\\s(.+)").matcher(sql);
			if (matcher.find()) {
				if (matcher.group(2).trim().startsWith("\"partition\"")) {
					String[] columns = matcher.group(2).trim().split("in")[1].replace("(", "").replace(")", "").trim().split(",");
					for (String column : columns) {
						try {
							kafkaSql.getPartition().add(Integer.parseInt(column));
						} catch (Exception e) {
							LOG.error("Parse parition[" + column + "] has error,msg is " + e.getMessage());
						}
					}
				}
				kafkaSql.setSeeds(getBrokers(clusterAlias));
			}
		}
		return kafkaSql;
	}

	/** Get kafka 0.10.x after activer topics. */
	public Set<String> getKafkaActiverTopics(String clusterAlias, String group) {
		JSONArray consumerGroups = getKafkaMetadata(parseBrokerServer(clusterAlias), group, clusterAlias);
		Set<String> topics = new HashSet<>();
		for (Object object : consumerGroups) {
			JSONObject consumerGroup = (JSONObject) object;
			for (Object topicObject : consumerGroup.getJSONArray("topicSub")) {
				JSONObject topic = (JSONObject) topicObject;
				if (!"".equals(consumerGroup.getString("owner")) && consumerGroup.getString("owner") != null) {
					topics.add(topic.getString("topic"));
				}
			}
		}
		return topics;
	}

	public Set<String> getKafkaConsumerTopics(String clusterAlias, String group) {
		JSONArray consumerGroups = getKafkaMetadata(parseBrokerServer(clusterAlias), group, clusterAlias);
		Set<String> topics = new HashSet<>();
		for (Object object : consumerGroups) {
			JSONObject consumerGroup = (JSONObject) object;
			for (Object topicObject : consumerGroup.getJSONArray("topicSub")) {
				JSONObject topic = (JSONObject) topicObject;
				topics.add(topic.getString("topic"));
			}
		}
		return topics;
	}

	/** Get kafka 0.10.x, 1.x, 2.x consumer metadata. */
	public String getKafkaConsumer(String clusterAlias) {
		Properties prop = new Properties();
		JSONArray consumerGroups = new JSONArray();
		prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, parseBrokerServer(clusterAlias));

		if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.sasl.enable")) {
			sasl(prop, clusterAlias);
		}

		AdminClient adminClient = null;
		try {
			adminClient = AdminClient.create(prop);
			ListConsumerGroupsResult cgrs = adminClient.listConsumerGroups();
			java.util.Iterator<ConsumerGroupListing> itor = cgrs.all().get().iterator();
			while (itor.hasNext()) {
				ConsumerGroupListing gs = itor.next();
				JSONObject consumerGroup = new JSONObject();
				String groupId = gs.groupId();
				DescribeConsumerGroupsResult descConsumerGroup = adminClient.describeConsumerGroups(Arrays.asList(groupId));
				if (!groupId.contains("kafka.eagle")) {
					consumerGroup.put("group", groupId);
					try {
						Node node = descConsumerGroup.all().get().get(groupId).coordinator();
						consumerGroup.put("node", node.host() + ":" + node.port());
					} catch (Exception e) {
						LOG.error("Get coordinator node has error, msg is " + e.getMessage());
						e.printStackTrace();
					}
					consumerGroup.put("meta", getKafkaMetadata(parseBrokerServer(clusterAlias), groupId, clusterAlias));
					consumerGroups.add(consumerGroup);
				}
			}
		} catch (Exception e) {
			LOG.error("Get kafka consumer has error,msg is " + e.getMessage());
			e.printStackTrace();
		} finally {
			adminClient.close();
		}
		return consumerGroups.toJSONString();
	}

	/** Get kafka 0.10.x consumer group & topic information used for page. */
	public String getKafkaConsumer(String clusterAlias, DisplayInfo page) {
		Properties prop = new Properties();
		JSONArray consumerGroups = new JSONArray();
		prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, parseBrokerServer(clusterAlias));

		if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.sasl.enable")) {
			sasl(prop, clusterAlias);
		}

		AdminClient adminClient = null;
		try {
			adminClient = AdminClient.create(prop);
			ListConsumerGroupsResult cgrs = adminClient.listConsumerGroups();
			java.util.Iterator<ConsumerGroupListing> itor = cgrs.all().get().iterator();
			if (page.getSearch().length() > 0) {
				int offset = 0;
				boolean flag = itor.hasNext();
				while (flag) {
					ConsumerGroupListing gs = itor.next();
					JSONObject consumerGroup = new JSONObject();
					String groupId = gs.groupId();
					DescribeConsumerGroupsResult descConsumerGroup = adminClient.describeConsumerGroups(Arrays.asList(groupId));
					if (!groupId.contains("kafka.eagle") && groupId.contains(page.getSearch())) {
						if (offset < (page.getiDisplayLength() + page.getiDisplayStart()) && offset >= page.getiDisplayStart()) {
							consumerGroup.put("group", groupId);
							try {
								Node node = descConsumerGroup.all().get().get(groupId).coordinator();
								consumerGroup.put("node", node.host() + ":" + node.port());
							} catch (Exception e) {
								LOG.error("Get coordinator node has error, msg is " + e.getMessage());
								e.printStackTrace();
							}
							consumerGroup.put("meta", getKafkaMetadata(parseBrokerServer(clusterAlias), groupId, clusterAlias));
							consumerGroups.add(consumerGroup);
						}
						offset++;
					}
					flag = itor.hasNext();
					if (offset >= page.getiDisplayLength() + page.getiDisplayStart()) {
						flag = false;
					}
				}
			} else {
				int offset = 0;
				boolean flag = itor.hasNext();
				while (flag) {
					ConsumerGroupListing gs = itor.next();
					JSONObject consumerGroup = new JSONObject();
					String groupId = gs.groupId();
					DescribeConsumerGroupsResult descConsumerGroup = adminClient.describeConsumerGroups(Arrays.asList(groupId));
					if (!groupId.contains("kafka.eagle")) {
						if (offset < (page.getiDisplayLength() + page.getiDisplayStart()) && offset >= page.getiDisplayStart()) {
							consumerGroup.put("group", groupId);
							try {
								Node node = descConsumerGroup.all().get().get(groupId).coordinator();
								consumerGroup.put("node", node.host() + ":" + node.port());
							} catch (Exception e) {
								LOG.error("Get coordinator node has error, msg is " + e.getMessage());
								e.printStackTrace();
							}
							consumerGroup.put("meta", getKafkaMetadata(parseBrokerServer(clusterAlias), groupId, clusterAlias));
							consumerGroups.add(consumerGroup);
						}
						offset++;
					}
					flag = itor.hasNext();
					if (offset >= page.getiDisplayLength() + page.getiDisplayStart()) {
						flag = false;
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Get kafka consumer has error,msg is " + e.getMessage());
			e.printStackTrace();
		} finally {
			adminClient.close();
		}
		return consumerGroups.toJSONString();
	}

	/** Get kafka 0.10.x consumer metadata. */
	private JSONArray getKafkaMetadata(String bootstrapServers, String group, String clusterAlias) {
		Properties prop = new Properties();
		prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

		if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.sasl.enable")) {
			sasl(prop, clusterAlias);
		}

		JSONArray consumerGroups = new JSONArray();
		AdminClient adminClient = null;
		try {
			adminClient = AdminClient.create(prop);
			DescribeConsumerGroupsResult descConsumerGroup = adminClient.describeConsumerGroups(Arrays.asList(group));
			Collection<MemberDescription> consumerMetaInfos = descConsumerGroup.describedGroups().get(group).get().members();
			Set<String> hasOwnerTopics = new HashSet<>();
			if (consumerMetaInfos.size() > 0) {
				for (MemberDescription consumerMetaInfo : consumerMetaInfos) {
					JSONObject topicSub = new JSONObject();
					JSONArray topicSubs = new JSONArray();
					for (TopicPartition topic : consumerMetaInfo.assignment().topicPartitions()) {
						JSONObject object = new JSONObject();
						object.put("topic", topic.topic());
						object.put("partition", topic.partition());
						topicSubs.add(object);
						hasOwnerTopics.add(topic.topic());
					}
					topicSub.put("owner", consumerMetaInfo.consumerId());
					topicSub.put("node", consumerMetaInfo.host().replaceAll("/", ""));
					topicSub.put("topicSub", topicSubs);
					consumerGroups.add(topicSub);
				}
			}

			ListConsumerGroupOffsetsResult noActiveTopic = adminClient.listConsumerGroupOffsets(group);
			JSONObject topicSub = new JSONObject();
			JSONArray topicSubs = new JSONArray();
			for (Entry<TopicPartition, OffsetAndMetadata> entry : noActiveTopic.partitionsToOffsetAndMetadata().get().entrySet()) {
				JSONObject object = new JSONObject();
				object.put("topic", entry.getKey().topic());
				object.put("partition", entry.getKey().partition());
				if (!hasOwnerTopics.contains(entry.getKey().topic())) {
					topicSubs.add(object);
				}
			}
			topicSub.put("owner", "");
			topicSub.put("node", "-");
			topicSub.put("topicSub", topicSubs);
			consumerGroups.add(topicSub);
		} catch (Exception e) {
			LOG.error("Get kafka consumer metadata has error, msg is " + e.getMessage());
			e.printStackTrace();
		} finally {
			adminClient.close();
		}
		return consumerGroups;
	}

	/** Get kafka 0.10.x consumer pages. */
	public String getKafkaActiverSize(String clusterAlias, String group) {
		JSONArray consumerGroups = getKafkaMetadata(parseBrokerServer(clusterAlias), group, clusterAlias);
		int activerCounter = 0;
		Set<String> topics = new HashSet<>();
		for (Object object : consumerGroups) {
			JSONObject consumerGroup = (JSONObject) object;
			if (!"".equals(consumerGroup.getString("owner")) && consumerGroup.getString("owner") != null) {
				activerCounter++;
			}
			for (Object topicObject : consumerGroup.getJSONArray("topicSub")) {
				JSONObject topic = (JSONObject) topicObject;
				topics.add(topic.getString("topic"));
			}
		}
		JSONObject activerAndTopics = new JSONObject();
		activerAndTopics.put("activers", activerCounter);
		activerAndTopics.put("topics", topics.size());
		return activerAndTopics.toJSONString();
	}

	/** Get kafka consumer information pages not owners. */
	public OwnerInfo getKafkaActiverNotOwners(String clusterAlias, String group) {
		OwnerInfo ownerInfo = new OwnerInfo();
		JSONArray consumerGroups = getKafkaMetadata(parseBrokerServer(clusterAlias), group, clusterAlias);
		int activerCounter = 0;
		Set<String> topics = new HashSet<>();
		for (Object object : consumerGroups) {
			JSONObject consumerGroup = (JSONObject) object;
			if (!"".equals(consumerGroup.getString("owner")) && consumerGroup.getString("owner") != null) {
				activerCounter++;
			}
			for (Object topicObject : consumerGroup.getJSONArray("topicSub")) {
				JSONObject topic = (JSONObject) topicObject;
				topics.add(topic.getString("topic"));
			}
		}
		ownerInfo.setActiveSize(activerCounter);
		ownerInfo.setTopicSets(topics);
		return ownerInfo;
	}

	/** Get kafka 0.10.x, 1.x, 2.x consumer groups. */
	public int getKafkaConsumerGroups(String clusterAlias) {
		Properties prop = new Properties();
		int counter = 0;
		prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, parseBrokerServer(clusterAlias));

		if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.sasl.enable")) {
			sasl(prop, clusterAlias);
		}

		AdminClient adminClient = null;
		try {
			adminClient = AdminClient.create(prop);
			ListConsumerGroupsResult consumerGroups = adminClient.listConsumerGroups();
			java.util.Iterator<ConsumerGroupListing> groups = consumerGroups.all().get().iterator();
			while (groups.hasNext()) {
				String groupId = groups.next().groupId();
				if (!groupId.contains("kafka.eagle")) {
					counter++;
				}
			}
		} catch (Exception e) {
			LOG.info("Get kafka consumer group has error, msg is " + e.getMessage());
			e.printStackTrace();
		} finally {
			adminClient.close();
		}
		return counter;
	}

	/** Get kafka 0.10.x, 1.x, 2.x consumer topic information. */
	public Set<String> getKafkaConsumerTopic(String clusterAlias, String group) {
		JSONArray consumerGroups = getKafkaMetadata(parseBrokerServer(clusterAlias), group, clusterAlias);
		Set<String> topics = new HashSet<>();
		for (Object object : consumerGroups) {
			JSONObject consumerGroup = (JSONObject) object;
			for (Object topicObject : consumerGroup.getJSONArray("topicSub")) {
				JSONObject topic = (JSONObject) topicObject;
				topics.add(topic.getString("topic"));
			}
		}
		return topics;
	}

	/** Get kafka 0.10.x consumer group and topic. */
	public String getKafkaConsumerGroupTopic(String clusterAlias, String group) {
		return getKafkaMetadata(parseBrokerServer(clusterAlias), group, clusterAlias).toJSONString();
	}

	/** Get kafka 0.10.x, 1.x, 2.x offset from topic. */
	public String getKafkaOffset(String clusterAlias) {
		Properties prop = new Properties();
		prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, parseBrokerServer(clusterAlias));

		if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.sasl.enable")) {
			sasl(prop, clusterAlias);
		}
		JSONArray targets = new JSONArray();
		AdminClient adminClient = null;
		try {
			adminClient = AdminClient.create(prop);
			ListConsumerGroupsResult consumerGroups = adminClient.listConsumerGroups();
			java.util.Iterator<ConsumerGroupListing> groups = consumerGroups.all().get().iterator();
			while (groups.hasNext()) {
				String groupId = groups.next().groupId();
				if (!groupId.contains("kafka.eagle")) {
					ListConsumerGroupOffsetsResult offsets = adminClient.listConsumerGroupOffsets(groupId);
					for (Entry<TopicPartition, OffsetAndMetadata> entry : offsets.partitionsToOffsetAndMetadata().get().entrySet()) {
						JSONObject object = new JSONObject();
						object.put("group", groupId);
						object.put("topic", entry.getKey().topic());
						object.put("partition", entry.getKey().partition());
						object.put("offset", entry.getValue().offset());
						object.put("timestamp", CalendarUtils.getDate());
						targets.add(object);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Get consumer offset has error, msg is " + e.getMessage());
			e.printStackTrace();
		} finally {
			adminClient.close();
		}
		return targets.toJSONString();
	}

	/** Get the data for the topic partition in the specified consumer group */
	public Map<Integer, Long> getKafkaOffset(String clusterAlias, String group, String topic, Set<Integer> partitionids) {
		Map<Integer, Long> partitionOffset = new HashMap<>();
		Properties prop = new Properties();
		prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, parseBrokerServer(clusterAlias));

		if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.sasl.enable")) {
			sasl(prop, clusterAlias);
		}
		AdminClient adminClient = null;
		try {
			adminClient = AdminClient.create(prop);
			List<TopicPartition> tps = new ArrayList<>();
			for (int partitionid : partitionids) {
				TopicPartition tp = new TopicPartition(topic, partitionid);
				tps.add(tp);
			}

			ListConsumerGroupOffsetsOptions consumerOffsetOptions = new ListConsumerGroupOffsetsOptions();
			consumerOffsetOptions.topicPartitions(tps);

			ListConsumerGroupOffsetsResult offsets = adminClient.listConsumerGroupOffsets(group, consumerOffsetOptions);
			for (Entry<TopicPartition, OffsetAndMetadata> entry : offsets.partitionsToOffsetAndMetadata().get().entrySet()) {
				if (topic.equals(entry.getKey().topic())) {
					partitionOffset.put(entry.getKey().partition(), entry.getValue().offset());
				}
			}
		} catch (Exception e) {
			LOG.error("Get consumer offset has error, msg is " + e.getMessage());
			e.printStackTrace();
		} finally {
			adminClient.close();
		}
		return partitionOffset;
	}

	/** Get kafka 0.10.x broker bootstrap server. */
	public String getKafkaBrokerServer(String clusterAlias) {
		return parseBrokerServer(clusterAlias);
	}

	/** Get kafka 0.10.x topic history logsize. */
	public long getKafkaLogSize(String clusterAlias, String topic, int partitionid) {
		long histyLogSize = 0L;
		Properties props = new Properties();
		props.put(ConsumerConfig.GROUP_ID_CONFIG, Kafka.KAFKA_EAGLE_SYSTEM_GROUP);
		props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getKafkaBrokerServer(clusterAlias));
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.sasl.enable")) {
			sasl(props, clusterAlias);
		}
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
		TopicPartition tp = new TopicPartition(topic, partitionid);
		consumer.assign(Collections.singleton(tp));
		java.util.Map<TopicPartition, Long> logsize = consumer.endOffsets(Collections.singleton(tp));
		try {
			histyLogSize = logsize.get(tp).longValue();
		} catch (Exception e) {
			LOG.error("Get history topic logsize has error, msg is " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (consumer != null) {
				consumer.close();
			}
		}
		return histyLogSize;
	}

	/** Get kafka 0.10.x topic history logsize. */
	public Map<TopicPartition, Long> getKafkaLogSize(String clusterAlias, String topic, Set<Integer> partitionids) {
		Properties props = new Properties();
		props.put(ConsumerConfig.GROUP_ID_CONFIG, Kafka.KAFKA_EAGLE_SYSTEM_GROUP);
		props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getKafkaBrokerServer(clusterAlias));
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.sasl.enable")) {
			sasl(props, clusterAlias);
		}
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
		Set<TopicPartition> tps = new HashSet<>();
		for (int partitionid : partitionids) {
			TopicPartition tp = new TopicPartition(topic, partitionid);
			tps.add(tp);
		}

		consumer.assign(tps);
		java.util.Map<TopicPartition, Long> endLogSize = consumer.endOffsets(tps);
		if (consumer != null) {
			consumer.close();
		}
		return endLogSize;
	}

	/** Get kafka 0.10.x topic real logsize by partitionid. */
	public long getKafkaRealLogSize(String clusterAlias, String topic, int partitionid) {
		long realLogSize = 0L;
		Properties props = new Properties();
		props.put(ConsumerConfig.GROUP_ID_CONFIG, Kafka.KAFKA_EAGLE_SYSTEM_GROUP);
		props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getKafkaBrokerServer(clusterAlias));
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.sasl.enable")) {
			sasl(props, clusterAlias);
		}
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
		TopicPartition tp = new TopicPartition(topic, partitionid);
		consumer.assign(Collections.singleton(tp));
		java.util.Map<TopicPartition, Long> endLogSize = consumer.endOffsets(Collections.singleton(tp));
		java.util.Map<TopicPartition, Long> startLogSize = consumer.beginningOffsets(Collections.singleton(tp));
		try {
			realLogSize = endLogSize.get(tp).longValue() - startLogSize.get(tp).longValue();
		} catch (Exception e) {
			LOG.error("Get real topic logsize by partition list has error, msg is " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (consumer != null) {
				consumer.close();
			}
		}
		return realLogSize;
	}

	/** Get kafka 0.10.x topic real logsize by partitionid set. */
	public long getKafkaRealLogSize(String clusterAlias, String topic, Set<Integer> partitionids) {
		long realLogSize = 0L;
		Properties props = new Properties();
		props.put(ConsumerConfig.GROUP_ID_CONFIG, Kafka.KAFKA_EAGLE_SYSTEM_GROUP);
		props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getKafkaBrokerServer(clusterAlias));
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.sasl.enable")) {
			sasl(props, clusterAlias);
		}
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
		Set<TopicPartition> tps = new HashSet<>();
		for (int partitionid : partitionids) {
			TopicPartition tp = new TopicPartition(topic, partitionid);
			tps.add(tp);
		}
		consumer.assign(tps);
		java.util.Map<TopicPartition, Long> endLogSize = consumer.endOffsets(tps);
		java.util.Map<TopicPartition, Long> startLogSize = consumer.beginningOffsets(tps);
		try {
			long endSumLogSize = 0L;
			long startSumLogSize = 0L;
			for (Entry<TopicPartition, Long> entry : endLogSize.entrySet()) {
				endSumLogSize += entry.getValue();
			}
			for (Entry<TopicPartition, Long> entry : startLogSize.entrySet()) {
				startSumLogSize += entry.getValue();
			}
			realLogSize = endSumLogSize - startSumLogSize;
		} catch (Exception e) {
			LOG.error("Get real topic logsize has error, msg is " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (consumer != null) {
				consumer.close();
			}
		}
		return realLogSize;
	}

	/** Get topic producer send logsize records. */
	public long getKafkaProducerLogSize(String clusterAlias, String topic, Set<Integer> partitionids) {
		long producerLogSize = 0L;
		Properties props = new Properties();
		props.put(ConsumerConfig.GROUP_ID_CONFIG, Kafka.KAFKA_EAGLE_SYSTEM_GROUP);
		props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getKafkaBrokerServer(clusterAlias));
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.sasl.enable")) {
			sasl(props, clusterAlias);
		}
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
		Set<TopicPartition> tps = new HashSet<>();
		for (int partitionid : partitionids) {
			TopicPartition tp = new TopicPartition(topic, partitionid);
			tps.add(tp);
		}
		consumer.assign(tps);
		java.util.Map<TopicPartition, Long> endLogSize = consumer.endOffsets(tps);
		try {
			for (Entry<TopicPartition, Long> entry : endLogSize.entrySet()) {
				producerLogSize += entry.getValue();
			}
		} catch (Exception e) {
			LOG.error("Get producer topic logsize has error, msg is " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (consumer != null) {
				consumer.close();
			}
		}
		return producerLogSize;
	}

	/** Get kafka version. */
	public String getKafkaVersion(String host, int port, String ids, String clusterAlias) {
		JMXConnector connector = null;
		String version = "-";
		String JMX = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";
		try {
			JMXServiceURL jmxSeriverUrl = new JMXServiceURL(String.format(JMX, host + ":" + port));
			connector = JMXFactoryUtils.connectWithTimeout(jmxSeriverUrl, 30, TimeUnit.SECONDS);
			MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
			if (CollectorType.KAFKA.equals(SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage"))) {
				version = mbeanConnection.getAttribute(new ObjectName(String.format(BrokerServer.BROKER_VERSION.getValue(), ids)), BrokerServer.BROKER_VERSION_VALUE.getValue()).toString();
			} else {
				version = mbeanConnection.getAttribute(new ObjectName(KafkaServer8.VERSION.getValue()), KafkaServer8.VALUE.getValue()).toString();
			}
		} catch (Exception ex) {
			LOG.error("Get kafka version from jmx has error, msg is " + ex.getMessage());
		} finally {
			if (connector != null) {
				try {
					connector.close();
				} catch (IOException e) {
					LOG.error("Close jmx connector has error, msg is " + e.getMessage());
				}
			}
		}
		return version;
	}

	/** Get kafka 0.10.x sasl topic metadata. */
	public List<MetadataInfo> findKafkaLeader(String clusterAlias, String topic) {
		List<MetadataInfo> targets = new ArrayList<>();
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		if (zkc.pathExists(BROKER_TOPICS_PATH + "/" + topic)) {
			Tuple2<Option<byte[]>, Stat> tuple = zkc.getDataAndStat(BROKER_TOPICS_PATH + "/" + topic);
			String tupleString = new String(tuple._1.get());
			JSONObject partitionObject = JSON.parseObject(tupleString).getJSONObject("partitions");
			for (String partition : partitionObject.keySet()) {
				String path = String.format(TOPIC_ISR, topic, Integer.valueOf(partition));
				Tuple2<Option<byte[]>, Stat> tuple2 = zkc.getDataAndStat(path);
				String tupleString2 = new String(tuple2._1.get());
				JSONObject topicMetadata = JSON.parseObject(tupleString2);
				MetadataInfo metadate = new MetadataInfo();
				try {
					metadate.setLeader(topicMetadata.getInteger("leader"));
				} catch (Exception e) {
					LOG.error("Parse string brokerid to int has error, brokerid[" + topicMetadata.getString("leader") + "]");
					e.printStackTrace();
				}
				metadate.setPartitionId(Integer.valueOf(partition));
				targets.add(metadate);
			}
		}
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return targets;
	}

	/** Send mock message to kafka topic . */
	public boolean mockMessage(String clusterAlias, String topic, String message) {
		Properties props = new Properties();
		props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getKafkaBrokerServer(clusterAlias));
		props.put(Kafka.KEY_SERIALIZER, StringSerializer.class.getCanonicalName());
		props.put(Kafka.VALUE_SERIALIZER, StringSerializer.class.getCanonicalName());
		props.put(Kafka.PARTITION_CLASS, KafkaPartitioner.class.getName());

		if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.sasl.enable")) {
			sasl(props, clusterAlias);
		}

		Producer<String, String> producer = new KafkaProducer<>(props);
		producer.send(new ProducerRecord<String, String>(topic, new Date().getTime() + "", message));
		producer.close();

		return true;
	}

	/** Get group consumer all topics lags. */
	public long getLag(String clusterAlias, String group, String topic) {
		long lag = 0L;
		try {
			List<String> partitions = findTopicPartition(clusterAlias, topic);
			for (String partition : partitions) {
				int partitionInt = Integer.parseInt(partition);
				OffsetZkInfo offsetZk = getOffset(clusterAlias, topic, group, partitionInt);
				long logSize = getLogSize(clusterAlias, topic, partitionInt);
				lag += logSize - offsetZk.getOffset();
			}
		} catch (Exception e) {
			LOG.error("Get cluser[" + clusterAlias + "] active group[" + group + "] topic[" + topic + "] lag has error, msg is " + e.getMessage());
			e.printStackTrace();
		}
		return lag;
	}

	/** Get kafka group consumer all topics lags. */
	public long getKafkaLag(String clusterAlias, String group, String ketopic) {
		long lag = 0L;

		Properties prop = new Properties();
		prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, parseBrokerServer(clusterAlias));

		if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.sasl.enable")) {
			sasl(prop, clusterAlias);
		}
		AdminClient adminClient = null;
		try {
			adminClient = AdminClient.create(prop);
			ListConsumerGroupOffsetsResult offsets = adminClient.listConsumerGroupOffsets(group);
			for (Entry<TopicPartition, OffsetAndMetadata> entry : offsets.partitionsToOffsetAndMetadata().get().entrySet()) {
				if (ketopic.equals(entry.getKey().topic())) {
					long logSize = getKafkaLogSize(clusterAlias, entry.getKey().topic(), entry.getKey().partition());
					lag += logSize - entry.getValue().offset();
				}
			}
		} catch (Exception e) {
			LOG.error("Get cluster[" + clusterAlias + "] group[" + group + "] topic[" + ketopic + "] consumer lag has error, msg is " + e.getMessage());
			e.printStackTrace();
		} finally {
			adminClient.close();
		}
		return lag;
	}

	/** Get kafka old version topic history logsize. */
	public long getLogSize(String clusterAlias, String topic, int partitionid) {
		JMXConnector connector = null;
		String JMX = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";
		List<BrokersInfo> brokers = getAllBrokersInfo(clusterAlias);
		for (BrokersInfo broker : brokers) {
			try {
				JMXServiceURL jmxSeriverUrl = new JMXServiceURL(String.format(JMX, broker.getHost() + ":" + broker.getJmxPort()));
				connector = JMXFactoryUtils.connectWithTimeout(jmxSeriverUrl, 30, TimeUnit.SECONDS);
				if (connector != null) {
					break;
				}
			} catch (Exception e) {
				LOG.error("Get kafka old version logsize has error, msg is " + e.getMessage());
				e.printStackTrace();
			}
		}
		long logSize = 0L;
		try {
			MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
			logSize = Long.parseLong(mbeanConnection.getAttribute(new ObjectName(String.format(KafkaServer8.END_LOG_SIZE.getValue(), topic, partitionid)), KafkaServer8.VALUE.getValue()).toString());
		} catch (Exception ex) {
			LOG.error("Get kafka old version logsize & parse has error, msg is " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			if (connector != null) {
				try {
					connector.close();
				} catch (IOException e) {
					LOG.error("Close jmx connector has error, msg is " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
		return logSize;
	}

	/** Get kafka old version topic history logsize by partition set. */
	public long getLogSize(String clusterAlias, String topic, Set<Integer> partitionids) {
		JMXConnector connector = null;
		String JMX = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";
		List<BrokersInfo> brokers = getAllBrokersInfo(clusterAlias);
		for (BrokersInfo broker : brokers) {
			try {
				JMXServiceURL jmxSeriverUrl = new JMXServiceURL(String.format(JMX, broker.getHost() + ":" + broker.getJmxPort()));
				connector = JMXFactoryUtils.connectWithTimeout(jmxSeriverUrl, 30, TimeUnit.SECONDS);
				if (connector != null) {
					break;
				}
			} catch (Exception e) {
				LOG.error("Get kafka old version logsize has error, msg is " + e.getMessage());
				e.printStackTrace();
			}
		}
		long logSize = 0L;
		try {
			MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
			for (int partitionid : partitionids) {
				logSize += Long.parseLong(mbeanConnection.getAttribute(new ObjectName(String.format(KafkaServer8.END_LOG_SIZE.getValue(), topic, partitionid)), KafkaServer8.VALUE.getValue()).toString());
			}
		} catch (Exception ex) {
			LOG.error("Get kafka old version logsize & parse has error, msg is " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			if (connector != null) {
				try {
					connector.close();
				} catch (IOException e) {
					LOG.error("Close jmx connector has error, msg is " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
		return logSize;
	}

	/** Get kafka old version real topic logsize. */
	public long getRealLogSize(String clusterAlias, String topic, int partitionid) {
		JMXConnector connector = null;
		String JMX = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";
		List<BrokersInfo> brokers = getAllBrokersInfo(clusterAlias);
		for (BrokersInfo broker : brokers) {
			try {
				JMXServiceURL jmxSeriverUrl = new JMXServiceURL(String.format(JMX, broker.getHost() + ":" + broker.getJmxPort()));
				connector = JMXFactoryUtils.connectWithTimeout(jmxSeriverUrl, 30, TimeUnit.SECONDS);
				if (connector != null) {
					break;
				}
			} catch (Exception e) {
				LOG.error("Get kafka old version logsize has error, msg is " + e.getMessage());
				e.printStackTrace();
			}
		}
		long logSize = 0L;
		try {
			MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
			long endLogSize = Long.parseLong(mbeanConnection.getAttribute(new ObjectName(String.format(KafkaServer8.END_LOG_SIZE.getValue(), topic, partitionid)), KafkaServer8.VALUE.getValue()).toString());
			long startLogSize = Long.parseLong(mbeanConnection.getAttribute(new ObjectName(String.format(KafkaServer8.START_LOG_SIZE.getValue(), topic, partitionid)), KafkaServer8.VALUE.getValue()).toString());
			logSize = endLogSize - startLogSize;
		} catch (Exception ex) {
			LOG.error("Get kafka old version logsize & parse has error, msg is " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			if (connector != null) {
				try {
					connector.close();
				} catch (IOException e) {
					LOG.error("Close jmx connector has error, msg is " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
		return logSize;
	}

	/** Get broker host and jmx_port info from ids. */
	public String getBrokerJMXFromIds(String clusterAlias, int ids) {
		String jni = "";
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		if (zkc.pathExists(BROKER_IDS_PATH)) {
			try {
				Tuple2<Option<byte[]>, Stat> tuple = zkc.getDataAndStat(BROKER_IDS_PATH + "/" + ids);
				String tupleString = new String(tuple._1.get());
				String host = "";
				if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.sasl.enable")) {
					String endpoints = JSON.parseObject(tupleString).getString("endpoints");
					String tmp = endpoints.split("//")[1];
					host = tmp.substring(0, tmp.length() - 2).split(":")[0];
				} else {
					host = JSON.parseObject(tupleString).getString("host");
				}
				int jmxPort = JSON.parseObject(tupleString).getInteger("jmx_port");
				jni = host + ":" + jmxPort;
			} catch (Exception ex) {
				LOG.error("Get broker from ids has error, msg is " + ex.getCause().getMessage());
				ex.printStackTrace();
			}
		}
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return jni;
	}

	/** Get kafka os memory. */
	public long getOSMemory(String host, int port, String property) {
		JMXConnector connector = null;
		long memory = 0L;
		String JMX = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";
		try {
			JMXServiceURL jmxSeriverUrl = new JMXServiceURL(String.format(JMX, host + ":" + port));
			connector = JMXFactoryUtils.connectWithTimeout(jmxSeriverUrl, 30, TimeUnit.SECONDS);
			MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
			String memorySize = mbeanConnection.getAttribute(new ObjectName(BrokerServer.JMX_PERFORMANCE_TYPE.getValue()), property).toString();
			memory = Long.parseLong(memorySize);
		} catch (Exception ex) {
			LOG.error("Get kafka os memory from jmx has error, msg is " + ex.getMessage());
		} finally {
			if (connector != null) {
				try {
					connector.close();
				} catch (IOException e) {
					LOG.error("Close kafka os memory jmx connector has error, msg is " + e.getMessage());
				}
			}
		}
		return memory;
	}

}
