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
package org.smartloli.kafka.eagle.web.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.smartloli.kafka.eagle.common.protocol.BrokersInfo;
import org.smartloli.kafka.eagle.common.protocol.MBeanInfo;
import org.smartloli.kafka.eagle.common.protocol.MetadataInfo;
import org.smartloli.kafka.eagle.common.protocol.PartitionsInfo;
import org.smartloli.kafka.eagle.common.protocol.bscreen.BScreenBarInfo;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicConfig;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicLogSize;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicRank;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicSqlHistory;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.Kafka;
import org.smartloli.kafka.eagle.common.util.KConstants.MBean;
import org.smartloli.kafka.eagle.common.util.KConstants.Topic;
import org.smartloli.kafka.eagle.common.util.StrUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.Mx4jFactory;
import org.smartloli.kafka.eagle.core.factory.Mx4jService;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerFactory;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerService;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsFactory;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsService;
import org.smartloli.kafka.eagle.core.sql.execute.KafkaSqlParser;
import org.smartloli.kafka.eagle.web.dao.TopicDao;
import org.smartloli.kafka.eagle.web.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

/**
 * Kafka topic implements service interface.
 * 
 * @author smartloli.
 *
 *         Created by Aug 14, 2016.
 * 
 *         Update by hexiang 20170216
 */
@Service
public class TopicServiceImpl implements TopicService {

	@Autowired
	private TopicDao topicDao;

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	/** Kafka topic config service interface. */
	private KafkaMetricsService kafkaMetricsService = new KafkaMetricsFactory().create();

	/** Broker service interface. */
	private static BrokerService brokerService = new BrokerFactory().create();

	/** Mx4j service interface. */
	private Mx4jService mx4jService = new Mx4jFactory().create();

	/** Find topic name in all topics. */
	public boolean hasTopic(String clusterAlias, String topicName) {
		return brokerService.findKafkaTopic(clusterAlias, topicName);
	}

	/** Get metadata in topic. */
	public List<MetadataInfo> metadata(String clusterAlias, String topicName, Map<String, Object> params) {
		return brokerService.topicMetadataRecords(clusterAlias, topicName, params);
	}

	/** Execute kafka execute query sql and viewer topic message. */
	public String execute(String clusterAlias, String sql) {
		return KafkaSqlParser.execute(clusterAlias, sql);
	}

	/** Get kafka 0.10.x mock topics. */
	public String mockTopics(String clusterAlias, String name) {
		List<String> topicList = brokerService.topicList(clusterAlias);
		int offset = 0;
		JSONArray topics = new JSONArray();
		for (String topicName : topicList) {
			if (name != null) {
				JSONObject topic = new JSONObject();
				if (topicName.contains(name) && !topicName.equals(Kafka.CONSUMER_OFFSET_TOPIC)) {
					topic.put("text", topicName);
					topic.put("id", offset);
				}
				topics.add(topic);
			} else {
				JSONObject topic = new JSONObject();
				if (!topicName.equals(Kafka.CONSUMER_OFFSET_TOPIC)) {
					topic.put("text", topicName);
					topic.put("id", offset);
				}
				topics.add(topic);
			}

			offset++;
		}
		return topics.toJSONString();
	}

	/** Send mock message to kafka topic . */
	public boolean mockSendMsg(String clusterAlias, String topic, String message) {
		return kafkaService.mockMessage(clusterAlias, topic, message);
	}

	/** Get topic property keys */
	public String getTopicProperties(String clusterAlias, String name) {
		JSONArray topics = new JSONArray();
		int offset = 0;
		for (String key : Topic.getTopicConfigKeys()) {
			if (name != null) {
				JSONObject topic = new JSONObject();
				if (key.contains(name)) {
					topic.put("text", key);
					topic.put("id", offset);
				}
				topics.add(topic);
			} else {
				JSONObject topic = new JSONObject();
				topic.put("text", key);
				topic.put("id", offset);
				topics.add(topic);
			}
			offset++;
		}
		return topics.toJSONString();
	}

	/** Alter topic config. */
	public String changeTopicConfig(String clusterAlias, TopicConfig topicConfig) {
		return kafkaMetricsService.changeTopicConfig(clusterAlias, topicConfig.getName(), topicConfig.getType(), topicConfig.getConfigEntry());
	}

	/** Get topic numbers. */
	public long getTopicNumbers(String clusterAlias) {
		return brokerService.topicNumbers(clusterAlias);
	}

	@Override
	public long getTopicNumbers(String clusterAlias, String topic) {
		return brokerService.topicNumbers(clusterAlias, topic);
	}

	/** Get topic list. */
	public List<PartitionsInfo> list(String clusterAlias, Map<String, Object> params) {
		List<PartitionsInfo> topicRecords = brokerService.topicRecords(clusterAlias, params);
		for (PartitionsInfo partitionInfo : topicRecords) {
			Map<String, Object> spread = new HashMap<>();
			spread.put("cluster", clusterAlias);
			spread.put("topic", partitionInfo.getTopic());
			spread.put("tkey", Topic.BROKER_SPREAD);
			partitionInfo.setBrokersSpread(topicDao.readBrokerPerformance(spread) == null ? 0 : topicDao.readBrokerPerformance(spread).getTvalue());
			spread.put("tkey", Topic.BROKER_SKEWED);
			partitionInfo.setBrokersSkewed(topicDao.readBrokerPerformance(spread) == null ? 0 : topicDao.readBrokerPerformance(spread).getTvalue());
			spread.put("tkey", Topic.BROKER_LEADER_SKEWED);
			partitionInfo.setBrokersLeaderSkewed(topicDao.readBrokerPerformance(spread) == null ? 0 : topicDao.readBrokerPerformance(spread).getTvalue());
		}
		return topicRecords;
	}

	/** Get topic partition numbers. */
	public long getPartitionNumbers(String clusterAlias, String topic) {
		return brokerService.partitionNumbers(clusterAlias, topic);
	}

	@Override
	public String getTopicMBean(String clusterAlias, String topic) {
		List<BrokersInfo> brokers = kafkaService.getAllBrokersInfo(clusterAlias);
		Map<String, MBeanInfo> mbeans = new HashMap<>();
		for (BrokersInfo broker : brokers) {
			String uri = broker.getHost() + ":" + broker.getJmxPort();
			MBeanInfo bytesIn = mx4jService.bytesInPerSec(uri, topic);
			MBeanInfo bytesOut = mx4jService.bytesOutPerSec(uri, topic);
			MBeanInfo bytesRejected = mx4jService.bytesRejectedPerSec(uri, topic);
			MBeanInfo failedFetchRequest = mx4jService.failedFetchRequestsPerSec(uri, topic);
			MBeanInfo failedProduceRequest = mx4jService.failedProduceRequestsPerSec(uri, topic);
			MBeanInfo messageIn = mx4jService.messagesInPerSec(uri, topic);
			MBeanInfo produceMessageConversions = mx4jService.produceMessageConversionsPerSec(uri, topic);
			MBeanInfo totalFetchRequests = mx4jService.totalFetchRequestsPerSec(uri, topic);
			MBeanInfo totalProduceRequests = mx4jService.totalProduceRequestsPerSec(uri, topic);

			assembleMBeanInfo(mbeans, MBean.MESSAGES_IN, messageIn);
			assembleMBeanInfo(mbeans, MBean.BYTES_IN, bytesIn);
			assembleMBeanInfo(mbeans, MBean.BYTES_OUT, bytesOut);
			assembleMBeanInfo(mbeans, MBean.BYTES_REJECTED, bytesRejected);
			assembleMBeanInfo(mbeans, MBean.FAILED_FETCH_REQUEST, failedFetchRequest);
			assembleMBeanInfo(mbeans, MBean.FAILED_PRODUCE_REQUEST, failedProduceRequest);
			assembleMBeanInfo(mbeans, MBean.PRODUCEMESSAGECONVERSIONS, produceMessageConversions);
			assembleMBeanInfo(mbeans, MBean.TOTALFETCHREQUESTSPERSEC, totalFetchRequests);
			assembleMBeanInfo(mbeans, MBean.TOTALPRODUCEREQUESTSPERSEC, totalProduceRequests);
		}
		for (Entry<String, MBeanInfo> entry : mbeans.entrySet()) {
			if (entry == null || entry.getValue() == null) {
				continue;
			}
			entry.getValue().setFifteenMinute(StrUtils.assembly(entry.getValue().getFifteenMinute()));
			entry.getValue().setFiveMinute(StrUtils.assembly(entry.getValue().getFiveMinute()));
			entry.getValue().setMeanRate(StrUtils.assembly(entry.getValue().getMeanRate()));
			entry.getValue().setOneMinute(StrUtils.assembly(entry.getValue().getOneMinute()));
		}
		return new Gson().toJson(mbeans);
	}

	private void assembleMBeanInfo(Map<String, MBeanInfo> mbeans, String mBeanInfoKey, MBeanInfo mBeanInfo) {
		if (mbeans.containsKey(mBeanInfoKey) && mBeanInfo != null) {
			MBeanInfo mbeanInfo = mbeans.get(mBeanInfoKey);
			String fifteenMinuteOld = mbeanInfo.getFifteenMinute() == null ? "0.0" : mbeanInfo.getFifteenMinute();
			String fifteenMinuteLastest = mBeanInfo.getFifteenMinute() == null ? "0.0" : mBeanInfo.getFifteenMinute();
			String fiveMinuteOld = mbeanInfo.getFiveMinute() == null ? "0.0" : mbeanInfo.getFiveMinute();
			String fiveMinuteLastest = mBeanInfo.getFiveMinute() == null ? "0.0" : mBeanInfo.getFiveMinute();
			String meanRateOld = mbeanInfo.getMeanRate() == null ? "0.0" : mbeanInfo.getMeanRate();
			String meanRateLastest = mBeanInfo.getMeanRate() == null ? "0.0" : mBeanInfo.getMeanRate();
			String oneMinuteOld = mbeanInfo.getOneMinute() == null ? "0.0" : mbeanInfo.getOneMinute();
			String oneMinuteLastest = mBeanInfo.getOneMinute() == null ? "0.0" : mBeanInfo.getOneMinute();
			long fifteenMinute = Math.round(StrUtils.numberic(fifteenMinuteOld)) + Math.round(StrUtils.numberic(fifteenMinuteLastest));
			long fiveMinute = Math.round(StrUtils.numberic(fiveMinuteOld)) + Math.round(StrUtils.numberic(fiveMinuteLastest));
			long meanRate = Math.round(StrUtils.numberic(meanRateOld)) + Math.round(StrUtils.numberic(meanRateLastest));
			long oneMinute = Math.round(StrUtils.numberic(oneMinuteOld)) + Math.round(StrUtils.numberic(oneMinuteLastest));
			mbeanInfo.setFifteenMinute(String.valueOf(fifteenMinute));
			mbeanInfo.setFiveMinute(String.valueOf(fiveMinute));
			mbeanInfo.setMeanRate(String.valueOf(meanRate));
			mbeanInfo.setOneMinute(String.valueOf(oneMinute));
		} else {
			mbeans.put(mBeanInfoKey, mBeanInfo);
		}
	}

	/** Get topic logsize, topicsize from jmx data. */
	public String getTopicSizeAndCapacity(String clusterAlias, String topic) {
		JSONObject object = new JSONObject();
		long logSize = brokerService.getTopicRealLogSize(clusterAlias, topic);
		JSONObject topicSize;
		if ("kafka".equals(SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage"))) {
			topicSize = kafkaMetricsService.topicSize(clusterAlias, topic);
		} else {
			topicSize = kafkaMetricsService.topicSize(clusterAlias, topic);
		}
		object.put("logsize", logSize);
		object.put("topicsize", topicSize.getString("size"));
		object.put("sizetype", topicSize.getString("type"));
		return object.toJSONString();
	}

	/** Get topic producer logsize chart datasets. */
	public String queryTopicProducerChart(Map<String, Object> params) {
		List<TopicLogSize> topicLogSizes = topicDao.queryTopicProducerChart(params);
		JSONArray arrays = new JSONArray();
		for (TopicLogSize topicLogSize : topicLogSizes) {
			JSONObject object = new JSONObject();
			object.put("x", CalendarUtils.convertUnixTime(topicLogSize.getTimespan(), "yyyy-MM-dd HH:mm"));
			object.put("y", topicLogSize.getDiffval());
			arrays.add(object);
		}
		return arrays.toJSONString();
	}

	@Override
	public String getSelectTopics(String clusterAlias, String prefixTopic) {
		return brokerService.topicListParams(clusterAlias, prefixTopic);
	}

	@Override
	public String getSelectTopicsLogSize(String clusterAlias, Map<String, Object> params) {
		JSONArray array = new JSONArray();
		List<BScreenBarInfo> bsProducers = topicDao.queryProducerHistoryBar(params);
		Map<String, Object> bsMaps = new HashMap<>();
		for (BScreenBarInfo bsProducer : bsProducers) {
			if (bsProducer != null) {
				bsMaps.put(bsProducer.getTm(), bsProducer.getValue());
			}
		}
		int index = 0;
		try {
			index = CalendarUtils.getDiffDay(params.get("stime").toString(), params.get("etime").toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i = index; i >= 0; i--) {
			String tm = CalendarUtils.getCustomLastDay(i);
			if (bsMaps.containsKey(tm)) {
				JSONObject object = new JSONObject();
				object.put("x", CalendarUtils.getCustomLastDay("yyyy-MM-dd", i));
				object.put("y", bsMaps.get(tm).toString());
				array.add(object);
			} else {
				JSONObject object = new JSONObject();
				object.put("x", CalendarUtils.getCustomLastDay("MM-dd", i));
				object.put("y", 0);
				array.add(object);
			}
		}
		return array.toJSONString();
	}

	@Override
	public int writeTopicSqlHistory(List<TopicSqlHistory> topicSqlHistorys) {
		return topicDao.writeTopicSqlHistory(topicSqlHistorys);
	}

	@Override
	public List<TopicSqlHistory> readTopicSqlHistory(Map<String, Object> params) {
		return topicDao.readTopicSqlHistory(params);
	}

	@Override
	public List<TopicSqlHistory> readTopicSqlHistoryByAdmin(Map<String, Object> params) {
		return topicDao.readTopicSqlHistoryByAdmin(params);
	}

	@Override
	public long countTopicSqlHistory(Map<String, Object> params) {
		return topicDao.countTopicSqlHistory(params);
	}

	@Override
	public long countTopicSqlHistoryByAdmin(Map<String, Object> params) {
		return topicDao.countTopicSqlHistoryByAdmin(params);
	}

	@Override
	public TopicSqlHistory findTopicSqlByID(Map<String, Object> params) {
		return topicDao.findTopicSqlByID(params);
	}

	@Override
	public int addCleanTopicData(List<TopicRank> topicRanks) {
		return topicDao.writeTopicRank(topicRanks);
	}

	@Override
	public List<TopicRank> getCleanTopicState(Map<String, Object> params) {
		return topicDao.getCleanTopicState(params);
	}

}
