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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.DescribeConsumerGroupsResult;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.admin.ListConsumerGroupsResult;
import org.apache.kafka.clients.admin.MemberDescription;
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
import org.smartloli.kafka.eagle.common.constant.JmxConstants.KafkaServer;
import org.smartloli.kafka.eagle.common.constant.JmxConstants.KafkaServer8;
import org.smartloli.kafka.eagle.common.protocol.*;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.CollectorType;
import org.smartloli.kafka.eagle.common.util.KConstants.Kafka;
import org.smartloli.kafka.eagle.common.util.KafkaPartitioner;
import org.smartloli.kafka.eagle.common.util.KafkaZKPoolUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import kafka.admin.RackAwareMode;
import kafka.zk.AdminZkClient;
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
	private final String DELETE_TOPICS_PATH = "/admin/delete_topics";
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
	public String getAllBrokersInfo(String clusterAlias) {
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
					if (SystemConfigUtils.getBooleanProperty("kafka.eagle.sasl.enable")) {
						String endpoints = JSON.parseObject(tupleString).getString("endpoints");
						String tmp = endpoints.split(File.separator + File.separator)[1];
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
					broker.setVersion(getKafkaVersion(broker.getHost(), broker.getJmxPort(), ids, clusterAlias));
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
		return targets.toString();
	}

	/** Get all topic info from zookeeper. */
	public String getAllPartitions(String clusterAlias) {
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		List<PartitionsInfo> targets = new ArrayList<PartitionsInfo>();
		if (zkc.pathExists(BROKER_TOPICS_PATH)) {
			Seq<String> subBrokerTopicsPaths = zkc.getChildren(BROKER_TOPICS_PATH);
			List<String> topics = JavaConversions.seqAsJavaList(subBrokerTopicsPaths);
			int id = 0;
			for (String topic : topics) {
				try {
					Tuple2<Option<byte[]>, Stat> tuple = zkc.getDataAndStat(BROKER_TOPICS_PATH + "/" + topic);
					PartitionsInfo partition = new PartitionsInfo();
					partition.setId(++id);
					partition.setCreated(CalendarUtils.convertUnixTime2Date(tuple._2.getCtime()));
					partition.setModify(CalendarUtils.convertUnixTime2Date(tuple._2.getMtime()));
					partition.setTopic(topic);
					String tupleString = new String(tuple._1.get());
					JSONObject partitionObject = JSON.parseObject(tupleString).getJSONObject("partitions");
					partition.setPartitionNumbers(partitionObject.size());
					partition.setPartitions(partitionObject.keySet());
					targets.add(partition);
				} catch (Exception ex) {
					LOG.error(ex.getMessage());
				}
			}
		}
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		// Sort topic by create time.
		Collections.sort(targets, new Comparator<PartitionsInfo>() {
			public int compare(PartitionsInfo arg0, PartitionsInfo arg1) {
				try {
					long hits0 = CalendarUtils.convertDate2UnixTime(arg0.getCreated());
					long hits1 = CalendarUtils.convertDate2UnixTime(arg1.getCreated());

					if (hits1 > hits0) {
						return 1;
					} else if (hits1 == hits0) {
						return 0;
					} else {
						return -1;
					}
				} catch (Exception e) {
					LOG.error("Convert date to unix time has error,msg is " + e.getMessage());
					return 0;
				}
			}
		});
		return targets.toString();
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

	/**
	 * According to topic and partition to obtain Replicas & Isr.
	 * 
	 * @param topic
	 * @param partitionid
	 * @return String.
	 */
	private String getReplicasIsr(String clusterAlias, String topic, int partitionid) {
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		TopicPartition tp = new TopicPartition(topic, partitionid);
		Option<Seq<Object>> repclicasAndPartition = zkc.getInSyncReplicasForPartition(tp);
		List<Object> targets = JavaConversions.seqAsJavaList(repclicasAndPartition.get());
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
		int brokers = JSON.parseArray(getAllBrokersInfo(clusterAlias)).size();
		if (Integer.parseInt(replic) > brokers) {
			targets.put("status", "error");
			targets.put("info", "replication factor: " + replic + " larger than available brokers: " + brokers);
			return targets;
		}
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		AdminZkClient adminZkCli = new AdminZkClient(zkc);
		adminZkCli.createTopic(topicName, Integer.parseInt(partitions), Integer.parseInt(replic), new Properties(), RackAwareMode.Enforced$.MODULE$);
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
			adminZkCli = null;
		}

		targets.put("status", "success");
		targets.put("info", "Create topic[" + topicName + "] has successed,partitions numbers is [" + partitions + "],replication-factor numbers is [" + replic + "]");
		return targets;
	}

	/** Delete topic to kafka cluster. */
	public Map<String, Object> delete(String clusterAlias, String topicName) {
		Map<String, Object> targets = new HashMap<String, Object>();
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		AdminZkClient adminZkCli = new AdminZkClient(zkc);
		adminZkCli.deleteTopic(topicName);
		boolean dt = zkc.deleteRecursive(DELETE_TOPICS_PATH + "/" + topicName);
		boolean bt = zkc.deleteRecursive(BROKER_TOPICS_PATH + "/" + topicName);
		if (dt && bt) {
			targets.put("status", "success");
		} else {
			targets.put("status", "failed");
		}
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return targets;
	}

	/** Get kafka brokers from zookeeper. */
	private List<HostsInfo> getBrokers(String clusterAlias) {
		List<HostsInfo> targets = new ArrayList<HostsInfo>();
		JSONArray brokers = JSON.parseArray(getAllBrokersInfo(clusterAlias));
		for (Object object : brokers) {
			JSONObject broker = (JSONObject) object;
			HostsInfo host = new HostsInfo();
			host.setHost(broker.getString("host"));
			host.setPort(broker.getInteger("port"));
			targets.add(host);
		}
		return targets;
	}

	private String parseBrokerServer(String clusterAlias) {
		String brokerServer = "";
		JSONArray brokers = JSON.parseArray(getAllBrokersInfo(clusterAlias));
		for (Object object : brokers) {
			JSONObject broker = (JSONObject) object;
			brokerServer += broker.getString("host") + ":" + broker.getInteger("port") + ",";
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

	private void sasl(Properties props, String bootstrapServers) {
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SystemConfigUtils.getProperty("kafka.eagle.sasl.protocol"));
		props.put(SaslConfigs.SASL_MECHANISM, SystemConfigUtils.getProperty("kafka.eagle.sasl.mechanism"));
	}

	private KafkaSqlInfo segments(String clusterAlias, String sql) {
		KafkaSqlInfo kafkaSql = new KafkaSqlInfo();
		kafkaSql.setMetaSql(sql);
		sql = sql.toLowerCase();
		kafkaSql.setSql(sql);
		if (sql.contains("and")) {
			sql = sql.split("and")[0];
		} else if (sql.contains("group by")) {
			sql = sql.split("group")[0];
		} else if (sql.contains("limit")) {
			sql = sql.split("limit")[0];
		}
		kafkaSql.getSchema().put("partition", "integer");
		kafkaSql.getSchema().put("offset", "bigint");
		kafkaSql.getSchema().put("msg", "varchar");
		if (!sql.startsWith("select")) {
			kafkaSql.setStatus(false);
			return kafkaSql;
		} else {
			Matcher tableName = Pattern.compile("select\\s.+from\\s(.+)where\\s(.+)").matcher(kafkaSql.getMetaSql().toLowerCase());
			if (tableName.find()) {
				kafkaSql.setStatus(true);
				kafkaSql.setTableName(tableName.group(1).trim().replaceAll("\"", ""));
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

	/** Get kafka 0.10.x activer topics. */
	public Set<String> getKafkaActiverTopics(String clusterAlias, String group) {
		JSONArray consumerGroups = getKafkaMetadata(parseBrokerServer(clusterAlias), group);
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

	/** Get kafka 0.10.x, 1.x, 2.x consumer metadata. */
	public String getKafkaConsumer(String clusterAlias) {
		Properties prop = new Properties();
		JSONArray consumerGroups = new JSONArray();
		prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, parseBrokerServer(clusterAlias));

		if (SystemConfigUtils.getBooleanProperty("kafka.eagle.sasl.enable")) {
			sasl(prop, getKafkaBrokerServer(clusterAlias));
		}

		try {
			AdminClient adminClient = AdminClient.create(prop);
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
					consumerGroup.put("meta", getKafkaMetadata(parseBrokerServer(clusterAlias), groupId));
					consumerGroups.add(consumerGroup);
				}
			}
			adminClient.close();
		} catch (Exception e) {
			LOG.error("Get kafka consumer has error,msg is " + e.getMessage());
			e.printStackTrace();
		}
		return consumerGroups.toJSONString();
	}

	/** Get kafka 0.10.x consumer metadata. */
	private JSONArray getKafkaMetadata(String bootstrapServers, String group) {
		Properties prop = new Properties();
		prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

		if (SystemConfigUtils.getBooleanProperty("kafka.eagle.sasl.enable")) {
			sasl(prop, bootstrapServers);
		}

		JSONArray consumerGroups = new JSONArray();
		try {
			AdminClient adminClient = AdminClient.create(prop);
			DescribeConsumerGroupsResult descConsumerGroup = adminClient.describeConsumerGroups(Arrays.asList(group));
			Collection<MemberDescription> consumerMetaInfos = descConsumerGroup.describedGroups().get(group).get().members();
			if (consumerMetaInfos.size() == 0) {
				ListConsumerGroupOffsetsResult noActiveTopic = adminClient.listConsumerGroupOffsets(group);
				JSONObject topicSub = new JSONObject();
				JSONArray topicSubs = new JSONArray();
				for (Entry<TopicPartition, OffsetAndMetadata> entry : noActiveTopic.partitionsToOffsetAndMetadata().get().entrySet()) {
					JSONObject object = new JSONObject();
					object.put("topic", entry.getKey().topic());
					object.put("partition", entry.getKey().partition());
					topicSubs.add(object);
				}
				topicSub.put("owner", "");
				topicSub.put("node", "-");
				topicSub.put("topicSub", topicSubs);
				consumerGroups.add(topicSub);
			} else {
				for (MemberDescription consumerMetaInfo : consumerMetaInfos) {
					JSONObject topicSub = new JSONObject();
					JSONArray topicSubs = new JSONArray();
					for (TopicPartition topic : consumerMetaInfo.assignment().topicPartitions()) {
						JSONObject object = new JSONObject();
						object.put("topic", topic.topic());
						object.put("partition", topic.partition());
						topicSubs.add(object);
					}
					topicSub.put("owner", consumerMetaInfo.consumerId());
					topicSub.put("node", consumerMetaInfo.host().replaceAll("/", ""));
					topicSub.put("topicSub", topicSubs);
					consumerGroups.add(topicSub);
				}
			}
			adminClient.close();
		} catch (Exception e) {
			LOG.error("Get kafka consumer metadata has error, msg is " + e.getMessage());
			e.printStackTrace();
		}
		return consumerGroups;
	}

	/** Get kafka 0.10.x consumer pages. */
	public String getKafkaActiverSize(String clusterAlias, String group) {
		JSONArray consumerGroups = getKafkaMetadata(parseBrokerServer(clusterAlias), group);
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

	/** Get kafka 0.10.x, 1.x, 2.x consumer groups. */
	public int getKafkaConsumerGroups(String clusterAlias) {
		Properties prop = new Properties();
		int counter = 0;
		prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, parseBrokerServer(clusterAlias));

		if (SystemConfigUtils.getBooleanProperty("kafka.eagle.sasl.enable")) {
			sasl(prop, parseBrokerServer(clusterAlias));
		}

		try {
			AdminClient adminClient = AdminClient.create(prop);
			ListConsumerGroupsResult consumerGroups = adminClient.listConsumerGroups();
			java.util.Iterator<ConsumerGroupListing> groups = consumerGroups.all().get().iterator();
			while (groups.hasNext()) {
				String groupId = groups.next().groupId();
				if (!groupId.contains("kafka.eagle")) {
					counter++;
				}
			}
			adminClient.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return counter;
	}

	/** Get kafka 0.10.x, 1.x, 2.x consumer topic information. */
	public Set<String> getKafkaConsumerTopic(String clusterAlias, String group) {
		JSONArray consumerGroups = getKafkaMetadata(parseBrokerServer(clusterAlias), group);
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
		return getKafkaMetadata(parseBrokerServer(clusterAlias), group).toJSONString();
	}

	/** Get kafka 0.10.x, 1.x, 2.x offset from topic. */
	public String getKafkaOffset(String clusterAlias) {
		Properties prop = new Properties();
		prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, parseBrokerServer(clusterAlias));

		if (SystemConfigUtils.getBooleanProperty("kafka.eagle.sasl.enable")) {
			sasl(prop, parseBrokerServer(clusterAlias));
		}
		JSONArray targets = new JSONArray();
		try {
			AdminClient adminClient = AdminClient.create(prop);
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
			adminClient.close();
		} catch (Exception e) {
			LOG.error("Get consumer offset has error, msg is " + e.getMessage());
			e.printStackTrace();
		}
		return targets.toJSONString();
	}

	/** Get kafka 0.10.x broker bootstrap server. */
	public String getKafkaBrokerServer(String clusterAlias) {
		return parseBrokerServer(clusterAlias);
	}

	/** Get kafka 0.10.x sasl logsize. */
	public long getKafkaLogSize(String clusterAlias, String topic, int partitionid) {
		Properties props = new Properties();
		props.put(ConsumerConfig.GROUP_ID_CONFIG, Kafka.KAFKA_EAGLE_SYSTEM_GROUP);
		props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getKafkaBrokerServer(clusterAlias));
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		if (SystemConfigUtils.getBooleanProperty("kafka.eagle.sasl.enable")) {
			props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SystemConfigUtils.getProperty("kafka.eagle.sasl.protocol"));
			props.put(SaslConfigs.SASL_MECHANISM, SystemConfigUtils.getProperty("kafka.eagle.sasl.mechanism"));
		}
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
		TopicPartition tp = new TopicPartition(topic, partitionid);
		consumer.assign(Collections.singleton(tp));
		java.util.Map<TopicPartition, Long> logsize = consumer.endOffsets(Collections.singleton(tp));
		consumer.close();
		return logsize.get(tp).longValue();
	}

	/** Get kafka version. */
	private String getKafkaVersion(String host, int port, String ids, String clusterAlias) {
		JMXConnector connector = null;
		String version = "";
		String JMX = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";
		try {
			JMXServiceURL jmxSeriverUrl = new JMXServiceURL(String.format(JMX, host + ":" + port));
			connector = JMXConnectorFactory.connect(jmxSeriverUrl);
			MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
			if (CollectorType.KAFKA.equals(SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage"))) {
				version = mbeanConnection.getAttribute(new ObjectName(String.format(KafkaServer.version, ids)), KafkaServer.value).toString();
			} else {
				version = mbeanConnection.getAttribute(new ObjectName(KafkaServer8.version), KafkaServer8.value).toString();
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
		if (zkc.pathExists(BROKER_TOPICS_PATH)) {
			Seq<String> subBrokerTopicsPaths = zkc.getChildren(BROKER_TOPICS_PATH);
			List<String> topics = JavaConversions.seqAsJavaList(subBrokerTopicsPaths);
			if (topics.contains(topic)) {
				Tuple2<Option<byte[]>, Stat> tuple = zkc.getDataAndStat(BROKER_TOPICS_PATH + "/" + topic);
				String tupleString = new String(tuple._1.get());
				JSONObject partitionObject = JSON.parseObject(tupleString).getJSONObject("partitions");
				for (String partition : partitionObject.keySet()) {
					String path = String.format(TOPIC_ISR, topic, Integer.valueOf(partition));
					Tuple2<Option<byte[]>, Stat> tuple2 = zkc.getDataAndStat(path);
					String tupleString2 = new String(tuple2._1.get());
					JSONObject topicMetadata = JSON.parseObject(tupleString2);
					MetadataInfo metadate = new MetadataInfo();
					metadate.setIsr(topicMetadata.getString("isr"));
					metadate.setLeader(topicMetadata.getInteger("leader"));
					metadate.setPartitionId(Integer.valueOf(partition));
					metadate.setReplicas(getReplicasIsr(clusterAlias, topic, Integer.valueOf(partition)));
					targets.add(metadate);
				}
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

		if (SystemConfigUtils.getBooleanProperty("kafka.eagle.sasl.enable")) {
			props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SystemConfigUtils.getProperty("kafka.eagle.sasl.protocol"));
			props.put(SaslConfigs.SASL_MECHANISM, SystemConfigUtils.getProperty("kafka.eagle.sasl.mechanism"));
		}

		Producer<String, String> producer = new KafkaProducer<>(props);
		JSONObject msg = new JSONObject();
		msg.put("date", CalendarUtils.getDate());
		msg.put("msg", message);

		producer.send(new ProducerRecord<String, String>(topic, new Date().getTime() + "", msg.toJSONString()));
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

		if (SystemConfigUtils.getBooleanProperty("kafka.eagle.sasl.enable")) {
			sasl(prop, parseBrokerServer(clusterAlias));
		}
		try {
			AdminClient adminClient = AdminClient.create(prop);
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
		}
		return lag;
	}

	/** Get kafka old version log size. */
	public long getLogSize(String clusterAlias, String topic, int partitionid) {
		JMXConnector connector = null;
		String JMX = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";
		JSONArray brokers = JSON.parseArray(getAllBrokersInfo(clusterAlias));
		for (Object object : brokers) {
			JSONObject broker = (JSONObject) object;
			try {
				JMXServiceURL jmxSeriverUrl = new JMXServiceURL(String.format(JMX, broker.getString("host") + ":" + broker.getInteger("jmxPort")));
				connector = JMXConnectorFactory.connect(jmxSeriverUrl);
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
			logSize = Long.parseLong(mbeanConnection.getAttribute(new ObjectName(String.format(KafkaServer8.logSize, topic, partitionid)), KafkaServer8.value).toString());
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
}
