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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.smartloli.kafka.eagle.common.protocol.ConsumerInfo;
import org.smartloli.kafka.eagle.common.protocol.DisplayInfo;
import org.smartloli.kafka.eagle.common.protocol.OwnerInfo;
import org.smartloli.kafka.eagle.common.protocol.TopicConsumerInfo;
import org.smartloli.kafka.eagle.common.protocol.consumer.ConsumerGroupsInfo;
import org.smartloli.kafka.eagle.common.protocol.consumer.ConsumerSummaryInfo;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicOffsetsInfo;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.KConstants.D3;
import org.smartloli.kafka.eagle.common.util.KConstants.Topic;
import org.smartloli.kafka.eagle.common.util.StrUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.web.dao.MBeanDao;
import org.smartloli.kafka.eagle.web.dao.TopicDao;
import org.smartloli.kafka.eagle.web.service.ConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Kafka consumer data interface, and set up the return data set.
 *
 * @author smartloli.
 *
 *         Created by Aug 15, 2016.
 * 
 *         Update by hexiang 20170216
 */
@Service
public class ConsumerServiceImpl implements ConsumerService {

	@Autowired
	private MBeanDao mbeanDao;

	@Autowired
	private TopicDao topicDao;

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	/** Get active topic graph data from kafka cluster. */
	public String getActiveGraph(String clusterAlias) {
		JSONObject target = new JSONObject();
		target.put("active", getActiveGraphDatasets(clusterAlias));
		return target.toJSONString();
	}

	/** Get active graph from zookeeper. */
	private String getActiveGraphDatasets(String clusterAlias) {
		Map<String, List<String>> activeTopics = kafkaService.getActiveTopic(clusterAlias);
		JSONObject target = new JSONObject();
		JSONArray targets = new JSONArray();
		target.put("name", "Active Topics");
		int count = 0;
		for (Entry<String, List<String>> entry : activeTopics.entrySet()) {
			JSONObject subTarget = new JSONObject();
			JSONArray subTargets = new JSONArray();
			if (count > KConstants.D3.SIZE) {
				subTarget.put("name", "...");
				JSONObject subInSubTarget = new JSONObject();
				subInSubTarget.put("name", "...");
				subTargets.add(subInSubTarget);
				subTarget.put("children", subTargets);
				targets.add(subTarget);
				break;
			} else {
				subTarget.put("name", entry.getKey());
				for (String str : entry.getValue()) {
					JSONObject subInSubTarget = new JSONObject();
					if (subTargets.size() > D3.CHILD_SIZE) {
						subInSubTarget.put("name", "...");
						subTargets.add(subInSubTarget);
						break;
					} else {
						subInSubTarget.put("name", str);
						subTargets.add(subInSubTarget);
					}
				}
			}
			count++;
			subTarget.put("children", subTargets);
			targets.add(subTarget);
		}
		target.put("children", targets);
		return target.toJSONString();
	}

	/** Get kafka active number & storage offset in zookeeper. */
	private int getActiveNumber(String clusterAlias, String group, List<String> topics) {
		Map<String, List<String>> activeTopics = kafkaService.getActiveTopic(clusterAlias);
		int sum = 0;
		for (String topic : topics) {
			if (activeTopics.containsKey(group + "_" + topic)) {
				sum++;
			}
		}
		return sum;
	}

	/** Storage offset in kafka or zookeeper. */
	public String getActiveTopic(String clusterAlias, String formatter) {
		if ("kafka".equals(formatter)) {
			return getKafkaActiveTopic(clusterAlias);
		} else {
			return getActiveGraph(clusterAlias);
		}
	}

	/** Get consumers from zookeeper. */
	private String getConsumer(String clusterAlias, DisplayInfo page) {
		Map<String, List<String>> consumers = kafkaService.getConsumers(clusterAlias, page);
		List<ConsumerInfo> consumerPages = new ArrayList<ConsumerInfo>();
		int id = 0;
		for (Entry<String, List<String>> entry : consumers.entrySet()) {
			ConsumerInfo consumer = new ConsumerInfo();
			consumer.setGroup(entry.getKey());
			consumer.setNode("");
			consumer.setTopics(entry.getValue().size());
			consumer.setId(++id);
			consumer.setActiveThreads(getActiveNumber(clusterAlias, entry.getKey(), entry.getValue()));
			consumer.setActiveTopics(getActiveNumber(clusterAlias, entry.getKey(), entry.getValue()));
			consumerPages.add(consumer);
		}
		return consumerPages.toString();
	}

	/** Judge consumers storage offset in kafka or zookeeper. */
	public String getConsumer(String clusterAlias, String formatter, DisplayInfo page) {
		if ("kafka".equals(formatter)) {
			return getKafkaConsumer(page, clusterAlias);
		} else {
			return getConsumer(clusterAlias, page);
		}
	}

	/** Get consumer size from kafka topic. */
	public int getConsumerCount(String clusterAlias, String formatter) {
		if ("kafka".equals(formatter)) {
			return kafkaService.getKafkaConsumerGroups(clusterAlias);
		} else {
			return kafkaService.getConsumers(clusterAlias).size();
		}
	}

	/** List the name of the topic in the consumer detail information. */
	private String getConsumerDetail(String clusterAlias, String group, String search) {
		Map<String, List<String>> consumers = kafkaService.getConsumers(clusterAlias);
		Map<String, List<String>> actvTopics = kafkaService.getActiveTopic(clusterAlias);
		List<TopicConsumerInfo> kafkaConsumerDetails = new ArrayList<TopicConsumerInfo>();
		int id = 0;
		for (String topic : consumers.get(group)) {
			if (StrUtils.isNull(search)) {
				TopicConsumerInfo consumerDetail = new TopicConsumerInfo();
				consumerDetail.setId(++id);
				consumerDetail.setTopic(topic);
				if (actvTopics.containsKey(group + "_" + topic)) {
					consumerDetail.setConsumering(Topic.RUNNING);
				} else {
					consumerDetail.setConsumering(Topic.SHUTDOWN);
				}
				kafkaConsumerDetails.add(consumerDetail);
			} else {
				if (search.contains(topic) || topic.contains(search)) {
					TopicConsumerInfo consumerDetail = new TopicConsumerInfo();
					consumerDetail.setId(++id);
					consumerDetail.setTopic(topic);
					if (actvTopics.containsKey(group + "_" + topic)) {
						consumerDetail.setConsumering(Topic.RUNNING);
					} else {
						consumerDetail.setConsumering(Topic.SHUTDOWN);
					}
					kafkaConsumerDetails.add(consumerDetail);
				}
			}
		}
		return kafkaConsumerDetails.toString();
	}

	/** Judge consumer storage offset in kafka or zookeeper. */
	public String getConsumerDetail(String clusterAlias, String formatter, String group, String search) {
		if ("kafka".equals(formatter)) {
			return getKafkaConsumerDetail(clusterAlias, group, search);
		} else {
			return getConsumerDetail(clusterAlias, group, search);
		}
	}

	/** Get active grahp data & storage offset in kafka topic. */
	private Object getKafkaActive(String clusterAlias) {
		JSONArray consumerGroups = JSON.parseArray(kafkaService.getKafkaConsumer(clusterAlias));
		JSONObject target = new JSONObject();
		JSONArray targets = new JSONArray();
		target.put("name", "Active Topics");
		int count = 0;
		for (Object object : consumerGroups) {
			JSONObject consumerGroup = (JSONObject) object;
			JSONObject subTarget = new JSONObject();
			JSONArray subTargets = new JSONArray();
			if (count > KConstants.D3.SIZE) {
				subTarget.put("name", "...");
				JSONObject subInSubTarget = new JSONObject();
				subInSubTarget.put("name", "...");
				subTargets.add(subInSubTarget);
				subTarget.put("children", subTargets);
				targets.add(subTarget);
				break;
			} else {
				subTarget.put("name", consumerGroup.getString("group"));
				for (String str : getKafkaTopicSets(clusterAlias, consumerGroup.getString("group"))) {
					JSONObject subInSubTarget = new JSONObject();
					if (subTargets.size() > D3.CHILD_SIZE) {
						subInSubTarget.put("name", "...");
						subTargets.add(subInSubTarget);
						break;
					} else {
						subInSubTarget.put("name", str);
						subTargets.add(subInSubTarget);
					}
				}
			}
			count++;
			subTarget.put("children", subTargets);
			targets.add(subTarget);
		}
		target.put("children", targets);
		return target.toJSONString();
	}

	/** Get active topic from kafka cluster & storage offset in kafka topic. */
	private String getKafkaActiveTopic(String clusterAlias) {
		JSONObject target = new JSONObject();
		target.put("active", getKafkaActive(clusterAlias));
		return target.toJSONString();
	}

	/** Get kafka consumer & storage offset in kafka topic. */
	private String getKafkaConsumer(DisplayInfo page, String clusterAlias) {
		List<ConsumerInfo> kafkaConsumerPages = new ArrayList<ConsumerInfo>();
		JSONArray consumerGroups = JSON.parseArray(kafkaService.getKafkaConsumer(clusterAlias, page));
		int id = page.getiDisplayStart();
		for (Object object : consumerGroups) {
			JSONObject consumerGroup = (JSONObject) object;
			String group = consumerGroup.getString("group");
			ConsumerInfo consumer = new ConsumerInfo();
			consumer.setGroup(group);
			consumer.setId(++id);
			consumer.setNode(consumerGroup.getString("node"));
			OwnerInfo ownerInfo = kafkaService.getKafkaActiverNotOwners(clusterAlias, group);
			consumer.setTopics(ownerInfo.getTopicSets().size());
			consumer.setActiveTopics(getKafkaActiveTopicNumbers(clusterAlias, group));
			consumer.setActiveThreads(ownerInfo.getActiveSize());
			kafkaConsumerPages.add(consumer);
		}
		return kafkaConsumerPages.toString();

	}

	/** Get kafka active topic by active graph. */
	private Set<String> getKafkaTopicSets(String clusterAlias, String group) {
		Set<String> consumerTopics = kafkaService.getKafkaConsumerTopic(clusterAlias, group);
		Set<String> activerTopics = kafkaService.getKafkaActiverTopics(clusterAlias, group);
		for (String topic : consumerTopics) {
			if (isConsumering(clusterAlias, group, topic) == Topic.RUNNING) {
				activerTopics.add(topic);
			}
		}
		Set<String> activeTopicSets = new HashSet<>();
		for (String topic : consumerTopics) {
			if (activerTopics.contains(topic)) {
				activeTopicSets.add(topic);
			} else {
				if (isConsumering(clusterAlias, group, topic) == Topic.RUNNING) {
					activeTopicSets.add(topic);
				}
			}
		}
		return activeTopicSets;
	}

	/** Get kafka active topic total. */
	private int getKafkaActiveTopicNumbers(String clusterAlias, String group) {
		Set<String> consumerTopics = kafkaService.getKafkaConsumerTopic(clusterAlias, group);
		Set<String> activerTopics = kafkaService.getKafkaActiverTopics(clusterAlias, group);
		for (String topic : consumerTopics) {
			if (isConsumering(clusterAlias, group, topic) == Topic.RUNNING) {
				activerTopics.add(topic);
			}
		}
		int active = 0;
		for (String topic : consumerTopics) {
			if (activerTopics.contains(topic)) {
				active++;
			} else {
				if (isConsumering(clusterAlias, group, topic) == Topic.RUNNING) {
					active++;
				}
			}
		}
		return active;
	}

	/** Get consumer detail from kafka topic. */
	private String getKafkaConsumerDetail(String clusterAlias, String group, String search) {
		Set<String> consumerTopics = kafkaService.getKafkaConsumerTopic(clusterAlias, group);
		Set<String> activerTopics = kafkaService.getKafkaActiverTopics(clusterAlias, group);
		for (String topic : consumerTopics) {
			if (isConsumering(clusterAlias, group, topic) == Topic.RUNNING) {
				activerTopics.add(topic);
			}
		}
		List<TopicConsumerInfo> kafkaConsumerPages = new ArrayList<TopicConsumerInfo>();
		int id = 0;
		for (String topic : consumerTopics) {
			if (StrUtils.isNull(search)) {
				TopicConsumerInfo consumerDetail = new TopicConsumerInfo();
				consumerDetail.setId(++id);
				consumerDetail.setTopic(topic);
				if (activerTopics.contains(topic)) {
					consumerDetail.setConsumering(Topic.RUNNING);
				} else {
					consumerDetail.setConsumering(isConsumering(clusterAlias, group, topic));
				}
				kafkaConsumerPages.add(consumerDetail);
			} else {
				if (search.contains(topic) || topic.contains(search)) {
					TopicConsumerInfo consumerDetail = new TopicConsumerInfo();
					consumerDetail.setId(++id);
					consumerDetail.setTopic(topic);
					if (activerTopics.contains(topic)) {
						consumerDetail.setConsumering(Topic.RUNNING);
					} else {
						consumerDetail.setConsumering(isConsumering(clusterAlias, group, topic));
					}
					kafkaConsumerPages.add(consumerDetail);
				}
			}

		}
		return kafkaConsumerPages.toString();
	}

	/** Check if the application is consuming. */
	public int isConsumering(String clusterAlias, String group, String topic) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("cluster", clusterAlias);
		params.put("group", group);
		params.put("topic", topic);
		List<TopicOffsetsInfo> topicOffsets = mbeanDao.getConsumerRateTopic(params);
		if (topicOffsets.size() == 2) {
			try {
				long resultOffsets = Math.abs(Long.parseLong(topicOffsets.get(0).getOffsets()) - Long.parseLong(topicOffsets.get(1).getOffsets()));
				long resultLogSize = Math.abs(Long.parseLong(topicOffsets.get(0).getLogsize()) - Long.parseLong(topicOffsets.get(0).getOffsets()));

				/**
				 * offset equal offset,maybe producer rate equal consumer rate.
				 */
				if (resultOffsets == 0) {
					/**
					 * logsize equal offsets,follow two states.<br>
					 * 1. maybe application shutdown.<br>
					 * 2. maybe application run, but producer rate equal
					 * consumer rate.<br>
					 */
					if (resultLogSize == 0) {
						return Topic.PENDING;
					} else {
						return Topic.SHUTDOWN;
					}
				} else {
					return Topic.RUNNING;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (topicOffsets.size() == 1) {
			long resultLogSize = Math.abs(Long.parseLong(topicOffsets.get(0).getLogsize()) - Long.parseLong(topicOffsets.get(0).getOffsets()));
			if (resultLogSize == 0) {
				return Topic.PENDING;
			} else {
				return Topic.SHUTDOWN;
			}
		}
		return Topic.SHUTDOWN;
	}

	@Override
	public long countConsumerGroupPages(Map<String, Object> params) {
		return topicDao.countConsumerGroupPages(params);
	}

	@Override
	public long countConsumerSummaryPages(Map<String, Object> params) {
		return topicDao.countConsumerSummaryPages(params);
	}

	@Override
	public List<ConsumerGroupsInfo> getConsumerGroupPages(String clusterAlias, String group, DisplayInfo page) {
		Map<String, Object> params = new HashMap<>();
		params.put("cluster", clusterAlias);
		params.put("group", group);
		params.put("start", page.getiDisplayStart());
		params.put("size", page.getiDisplayLength());
		params.put("search", page.getSearch());
		return topicDao.getConsumerGroupPages(params);
	}

	@Override
	public List<ConsumerSummaryInfo> getConsumerSummaryPages(String clusterAlias, DisplayInfo page) {
		Map<String, Object> params = new HashMap<>();
		params.put("cluster", clusterAlias);
		params.put("start", page.getiDisplayStart());
		params.put("size", page.getiDisplayLength());
		params.put("search", page.getSearch());
		return topicDao.getConsumerSummaryPages(params);
	}

	@Override
	public String getKafkaConsumerGraph(String clusterAlias) {
		Map<String, Object> params = new HashMap<>();
		params.put("cluster", clusterAlias);
		params.put("start", 0);
		params.put("size", D3.SIZE + 1);
		List<ConsumerSummaryInfo> consumerSummarys = topicDao.getConsumerSummaryPages(params);
		if (consumerSummarys != null) {
			JSONObject target = new JSONObject();
			JSONArray targets = new JSONArray();
			target.put("name", "Active Topics");
			int count = 1;
			for (ConsumerSummaryInfo consumerSummary : consumerSummarys) {
				JSONObject subTarget = new JSONObject();
				JSONArray subTargets = new JSONArray();
				if (count > KConstants.D3.SIZE) {
					subTarget.put("name", "...");
					JSONObject subInSubTarget = new JSONObject();
					subInSubTarget.put("name", "...");
					subTargets.add(subInSubTarget);
					subTarget.put("children", subTargets);
					targets.add(subTarget);
					break;
				} else {
					subTarget.put("name", consumerSummary.getGroup());
					Map<String, Object> paramChilds = new HashMap<>();
					paramChilds.put("cluster", clusterAlias);
					paramChilds.put("group", consumerSummary.getGroup());
					paramChilds.put("start", 0);
					paramChilds.put("size", D3.CHILD_SIZE + 1);
					paramChilds.put("status", "0");// running
					List<ConsumerGroupsInfo> consumerGroups = topicDao.getConsumerGroupPages(paramChilds);
					int child = 1;
					for (ConsumerGroupsInfo consumerGroup : consumerGroups) {
						JSONObject subInSubTarget = new JSONObject();
						if (child > D3.CHILD_SIZE) {
							subInSubTarget.put("name", "...");
							subTargets.add(subInSubTarget);
							break;
						} else {
							subInSubTarget.put("name", consumerGroup.getTopic());
							subTargets.add(subInSubTarget);
						}
						child++;
					}
				}
				count++;
				subTarget.put("children", subTargets);
				targets.add(subTarget);
			}
			target.put("children", targets);
			JSONObject result = new JSONObject();
			result.put("active", target.toJSONString());
			return result.toJSONString();
		} else {
			return "";
		}
	}

}
