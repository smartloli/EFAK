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
package org.smartloli.kafka.eagle.web.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.smartloli.kafka.eagle.common.protocol.MetadataInfo;
import org.smartloli.kafka.eagle.common.protocol.PartitionsInfo;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicConfig;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicMockMessage;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicRank;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicSqlHistory;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.KConstants.Kafka;
import org.smartloli.kafka.eagle.common.util.KConstants.Role;
import org.smartloli.kafka.eagle.common.util.KConstants.Topic;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerFactory;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerService;
import org.smartloli.kafka.eagle.web.pojo.Signiner;
import org.smartloli.kafka.eagle.web.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;

/**
 * Kafka topic controller to viewer data.
 * 
 * @author smartloli.
 *
 *         Created by Sep 6, 2016.
 * 
 *         Update by hexiang 20170216
 */
@Controller
public class TopicController {

	/** Kafka topic service interface. */
	@Autowired
	private TopicService topicService;

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	/** BrokerService interface. */
	private BrokerService brokerService = new BrokerFactory().create();

	/** Topic create viewer. */
	@RequiresPermissions("/topic/create")
	@RequestMapping(value = "/topic/create", method = RequestMethod.GET)
	public ModelAndView topicCreateView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/topic/create");
		return mav;
	}

	/** Topic message viewer. */
	@RequiresPermissions("/topic/message")
	@RequestMapping(value = "/topic/message", method = RequestMethod.GET)
	public ModelAndView topicMessageView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/topic/msg");
		return mav;
	}

	/** Topic message manager. */
	@RequiresPermissions("/topic/manager")
	@RequestMapping(value = "/topic/manager", method = RequestMethod.GET)
	public ModelAndView topicManagerView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/topic/manager");
		return mav;
	}

	/** Topic mock viewer. */
	@RequiresPermissions("/topic/mock")
	@RequestMapping(value = "/topic/mock", method = RequestMethod.GET)
	public ModelAndView topicMockView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/topic/mock");
		return mav;
	}

	/** Topic list viewer. */
	@RequestMapping(value = "/topic/list", method = RequestMethod.GET)
	public ModelAndView topicListView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/topic/list");
		return mav;
	}

	/** Topic metadata viewer. */
	@RequestMapping(value = "/topic/meta/{tname}/", method = RequestMethod.GET)
	public ModelAndView topicMetaView(@PathVariable("tname") String tname, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
		if (topicService.hasTopic(clusterAlias, tname)) {
			mav.setViewName("/topic/topic_meta");
		} else {
			mav.setViewName("/error/404");
		}

		return mav;
	}

	/** Create topic success viewer. */
	@RequestMapping(value = "/topic/create/success", method = RequestMethod.GET)
	public ModelAndView successView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/topic/add_success");
		return mav;
	}

	/** Create topic failed viewer. */
	@RequestMapping(value = "/topic/create/failed", method = RequestMethod.GET)
	public ModelAndView failedView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/topic/add_failed");
		return mav;
	}

	/** Get topic metadata by ajax. */
	@RequestMapping(value = "/topic/meta/{tname}/ajax", method = RequestMethod.GET)
	public void topicMetaAjax(@PathVariable("tname") String tname, HttpServletResponse response, HttpServletRequest request) {
		String aoData = request.getParameter("aoData");
		JSONArray params = JSON.parseArray(aoData);
		int sEcho = 0, iDisplayStart = 0, iDisplayLength = 0;
		for (Object object : params) {
			JSONObject param = (JSONObject) object;
			if ("sEcho".equals(param.getString("name"))) {
				sEcho = param.getIntValue("value");
			} else if ("iDisplayStart".equals(param.getString("name"))) {
				iDisplayStart = param.getIntValue("value");
			} else if ("iDisplayLength".equals(param.getString("name"))) {
				iDisplayLength = param.getIntValue("value");
			}
		}

		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

		Map<String, Object> map = new HashMap<>();
		map.put("start", iDisplayStart);
		map.put("length", iDisplayLength);
		long count = topicService.getPartitionNumbers(clusterAlias, tname);
		List<MetadataInfo> metadatas = topicService.metadata(clusterAlias, tname, map);
		JSONArray aaDatas = new JSONArray();
		for (MetadataInfo metadata : metadatas) {
			JSONObject object = new JSONObject();
			object.put("topic", tname);
			object.put("partition", metadata.getPartitionId());
			object.put("logsize", metadata.getLogSize());
			object.put("leader", metadata.getLeader());
			object.put("replicas", metadata.getReplicas());
			object.put("isr", metadata.getIsr());
			if (metadata.isPreferredLeader()) {
				object.put("preferred_leader", "<a class='btn btn-success btn-xs'>true</a>");
			} else {
				object.put("preferred_leader", "<a class='btn btn-danger btn-xs'>false</a>");
			}
			if (metadata.isUnderReplicated()) {
				object.put("under_replicated", "<a class='btn btn-danger btn-xs'>true</a>");
			} else {
				object.put("under_replicated", "<a class='btn btn-success btn-xs'>false</a>");
			}
			aaDatas.add(object);
		}

		JSONObject target = new JSONObject();
		target.put("sEcho", sEcho);
		target.put("iTotalRecords", count);
		target.put("iTotalDisplayRecords", count);
		target.put("aaData", aaDatas);
		try {
			byte[] output = target.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get cluster data by ajax. */
	@RequestMapping(value = "/topic/meta/mbean/{tname}/ajax", method = RequestMethod.GET)
	public void topicMetaMetricsAjax(@PathVariable("tname") String tname, HttpServletResponse response, HttpServletRequest request, HttpSession session) {
		try {
			String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
			String target = topicService.getTopicMBean(clusterAlias, tname);

			byte[] output = target.getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get cluster data by ajax. */
	@RequestMapping(value = "/topic/meta/jmx/{tname}/ajax", method = RequestMethod.GET)
	public void topicMsgByJmxAjax(@PathVariable("tname") String tname, HttpServletResponse response, HttpServletRequest request, HttpSession session) {
		try {
			String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
			String target = topicService.getTopicSizeAndCapacity(clusterAlias, tname);

			byte[] output = target.getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get topic datasets by ajax. */
	@RequestMapping(value = "/topic/mock/list/ajax", method = RequestMethod.GET)
	public void topicMockAjax(HttpServletResponse response, HttpServletRequest request) {
		try {
			HttpSession session = request.getSession();
			String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
			String name = request.getParameter("name");
			JSONObject object = new JSONObject();
			object.put("items", JSON.parseArray(topicService.mockTopics(clusterAlias, name)));
			byte[] output = object.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get topic datasets by ajax. */
	@RequestMapping(value = "/topic/manager/keys/ajax", method = RequestMethod.GET)
	public void getTopicProperties(HttpServletResponse response, HttpServletRequest request) {
		try {
			HttpSession session = request.getSession();
			String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
			String name = request.getParameter("name");
			JSONObject object = new JSONObject();
			object.put("items", JSON.parseArray(topicService.getTopicProperties(clusterAlias, name)));
			byte[] output = object.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get topic datasets by ajax. */
	@RequestMapping(value = "/topic/manager/{type}/ajax", method = RequestMethod.GET)
	public void alterTopicConfigAjax(@PathVariable("type") String type, HttpServletResponse response, HttpServletRequest request) {
		try {
			HttpSession session = request.getSession();
			String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
			String topic = request.getParameter("topic");
			TopicConfig topicConfig = new TopicConfig();
			topicConfig.setName(topic);
			topicConfig.setType(type.toUpperCase());
			if (KConstants.Topic.ADD.equals(topicConfig.getType())) {
				String key = request.getParameter("key");
				String value = request.getParameter("value");
				ConfigEntry configEntry = new ConfigEntry(key, value);
				topicConfig.setConfigEntry(configEntry);
			} else if (KConstants.Topic.DELETE.equals(topicConfig.getType())) {
				String key = request.getParameter("key");
				ConfigEntry configEntry = new ConfigEntry(key, "");
				topicConfig.setConfigEntry(configEntry);
			}
			JSONObject object = new JSONObject();
			object.put("result", topicService.changeTopicConfig(clusterAlias, topicConfig));
			byte[] output = object.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Send mock data to topic. */
	@RequestMapping(value = "/topic/mock/send/message/topic/ajax", method = RequestMethod.POST)
	public void topicMockSend(@RequestBody TopicMockMessage topicMockMessage, HttpServletResponse response, HttpServletRequest request) {
		try {
			HttpSession session = request.getSession();
			String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
			JSONObject object = new JSONObject();
			object.put("status", topicService.mockSendMsg(clusterAlias, topicMockMessage.getTopic(), topicMockMessage.getMessage()));
			byte[] output = object.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get topic datasets by ajax. */
	@RequestMapping(value = "/topic/list/table/ajax", method = RequestMethod.GET)
	public void topicListAjax(HttpServletResponse response, HttpServletRequest request) {
		String aoData = request.getParameter("aoData");
		JSONArray params = JSON.parseArray(aoData);
		int sEcho = 0, iDisplayStart = 0, iDisplayLength = 0;
		String search = "";
		for (Object object : params) {
			JSONObject param = (JSONObject) object;
			if ("sEcho".equals(param.getString("name"))) {
				sEcho = param.getIntValue("value");
			} else if ("iDisplayStart".equals(param.getString("name"))) {
				iDisplayStart = param.getIntValue("value");
			} else if ("iDisplayLength".equals(param.getString("name"))) {
				iDisplayLength = param.getIntValue("value");
			} else if ("sSearch".equals(param.getString("name"))) {
				search = param.getString("value");
			}
		}

		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
		Signiner signiner = (Signiner) session.getAttribute(KConstants.Login.SESSION_USER);

		Map<String, Object> map = new HashMap<>();
		map.put("search", search);
		map.put("start", iDisplayStart);
		map.put("length", iDisplayLength);
		long count = 0L;
		if (search != null && !"".equals(search)) {
			count = topicService.getTopicNumbers(clusterAlias, search);
		} else {
			count = topicService.getTopicNumbers(clusterAlias);
		}
		List<PartitionsInfo> topics = topicService.list(clusterAlias, map);
		JSONArray aaDatas = new JSONArray();
		for (PartitionsInfo partition : topics) {
			JSONObject object = new JSONObject();
			object.put("id", partition.getId());
			object.put("topic", "<a href='/ke/topic/meta/" + partition.getTopic() + "/' target='_blank'>" + partition.getTopic() + "</a>");
			object.put("partitions", partition.getPartitionNumbers());
			try {
				long brokerSpread = partition.getBrokersSpread();
				if (brokerSpread < Topic.TOPIC_BROKER_SPREAD_ERROR) {
					object.put("brokerSpread", "<a class='btn btn-danger btn-xs'>" + brokerSpread + "%</a>");
				} else if (brokerSpread >= Topic.TOPIC_BROKER_SPREAD_ERROR && brokerSpread < Topic.TOPIC_BROKER_SPREAD_NORMAL) {
					object.put("brokerSpread", "<a class='btn btn-warning btn-xs'>" + brokerSpread + "%</a>");
				} else if (brokerSpread >= Topic.TOPIC_BROKER_SPREAD_NORMAL) {
					object.put("brokerSpread", "<a class='btn btn-success btn-xs'>" + brokerSpread + "%</a>");
				} else {
					object.put("brokerSpread", "<a class='btn btn-primary btn-xs'>" + brokerSpread + "%</a>");
				}

				long brokerSkewed = partition.getBrokersSkewed();
				if (brokerSkewed >= Topic.TOPIC_BROKER_SKEW_ERROR) {
					object.put("brokerSkewed", "<a class='btn btn-danger btn-xs'>" + brokerSkewed + "%</a>");
				} else if (brokerSkewed > Topic.TOPIC_BROKER_SKEW_NORMAL && brokerSkewed < Topic.TOPIC_BROKER_SKEW_ERROR) {
					object.put("brokerSkewed", "<a class='btn btn-warning btn-xs'>" + brokerSkewed + "%</a>");
				} else if (brokerSkewed <= Topic.TOPIC_BROKER_SKEW_NORMAL) {
					object.put("brokerSkewed", "<a class='btn btn-success btn-xs'>" + brokerSkewed + "%</a>");
				} else {
					object.put("brokerSkewed", "<a class='btn btn-primary btn-xs'>" + brokerSkewed + "%</a>");
				}

				long brokerLeaderSkewed = partition.getBrokersLeaderSkewed();
				if (brokerLeaderSkewed >= Topic.TOPIC_BROKER_LEADER_SKEW_ERROR) {
					object.put("brokerLeaderSkewed", "<a class='btn btn-danger btn-xs'>" + brokerLeaderSkewed + "%</a>");
				} else if (brokerSkewed > Topic.TOPIC_BROKER_LEADER_SKEW_NORMAL && brokerLeaderSkewed < Topic.TOPIC_BROKER_LEADER_SKEW_ERROR) {
					object.put("brokerLeaderSkewed", "<a class='btn btn-warning btn-xs'>" + brokerLeaderSkewed + "%</a>");
				} else if (brokerSkewed <= Topic.TOPIC_BROKER_LEADER_SKEW_NORMAL) {
					object.put("brokerLeaderSkewed", "<a class='btn btn-success btn-xs'>" + brokerLeaderSkewed + "%</a>");
				} else {
					object.put("brokerLeaderSkewed", "<a class='btn btn-primary btn-xs'>" + brokerLeaderSkewed + "%</a>");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			object.put("created", partition.getCreated());
			object.put("modify", partition.getModify());
			Map<String, Object> topicStateParams = new HashMap<>();
			topicStateParams.put("cluster", clusterAlias);
			topicStateParams.put("topic", partition.getTopic());
			topicStateParams.put("tkey", Topic.TRUNCATE);
			List<TopicRank> topicStates = topicService.getCleanTopicState(topicStateParams);
			if (topicStates != null && topicStates.size() > 0) {
				if (topicStates.get(0).getTvalue() == 0) {
					if (Role.ADMIN.equals(signiner.getUsername())) {
						object.put("operate", "<div class='btn-group'><button class='btn btn-primary btn-xs dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Action <span class='caret'></span></button><ul class='dropdown-menu dropdown-menu-right'><li><a name='topic_modify' href='#" + partition.getTopic()
								+ "'><i class='fa fa-fw fa-edit'></i>Alter</a></li><li><a href='#" + partition.getTopic() + "' name='topic_remove'><i class='fa fa-fw fa-minus-circle'></i>Drop</a></li><li><a href='#" + partition.getTopic() + "' name=''><i class='fa fa-fw fa-trash-o'></i>Truncating</a></li></ul></div>");
					} else {
						object.put("operate", "");
					}
				} else {
					if (Role.ADMIN.equals(signiner.getUsername())) {
						object.put("operate", "<div class='btn-group'><button class='btn btn-primary btn-xs dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Action <span class='caret'></span></button><ul class='dropdown-menu dropdown-menu-right'><li><a name='topic_modify' href='#" + partition.getTopic()
								+ "'><i class='fa fa-fw fa-edit'></i>Alter</a></li><li><a href='#" + partition.getTopic() + "' name='topic_remove'><i class='fa fa-fw fa-minus-circle'></i>Drop</a></li><li><a href='#" + partition.getTopic() + "' name='topic_clean'><i class='fa fa-fw fa-trash-o'></i>Truncated</a></li></ul></div>");
					} else {
						object.put("operate", "");
					}
				}
			} else {
				if (Role.ADMIN.equals(signiner.getUsername())) {
					object.put("operate", "<div class='btn-group'><button class='btn btn-primary btn-xs dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Action <span class='caret'></span></button><ul class='dropdown-menu dropdown-menu-right'><li><a name='topic_modify' href='#" + partition.getTopic()
							+ "'><i class='fa fa-fw fa-edit'></i>Alter</a></li><li><a href='#" + partition.getTopic() + "' name='topic_remove'><i class='fa fa-fw fa-minus-circle'></i>Drop</a></li><li><a href='#" + partition.getTopic() + "' name='topic_clean'><i class='fa fa-fw fa-trash-o'></i>Truncate</a></li></ul></div>");
				} else {
					object.put("operate", "");
				}
			}

			aaDatas.add(object);
		}

		JSONObject target = new JSONObject();
		target.put("sEcho", sEcho);
		target.put("iTotalRecords", count);
		target.put("iTotalDisplayRecords", count);
		target.put("aaData", aaDatas);
		try {
			byte[] output = target.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get select topic datasets by ajax. */
	@RequestMapping(value = "/topic/list/select/ajax", method = RequestMethod.POST)
	public void topicSelectListAjax(HttpServletResponse response, HttpServletRequest request) {
		try {
			String topic = request.getParameter("topic");
			HttpSession session = request.getSession();
			String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
			byte[] output = topicService.getSelectTopics(clusterAlias, topic).getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get select filter topic datasets by ajax. */
	@RequestMapping(value = "/topic/list/filter/select/ajax", method = RequestMethod.GET)
	public void topicSelectFilterListAjax(HttpServletResponse response, HttpServletRequest request) {
		try {
			String topics = request.getParameter("topics");
			String stime = request.getParameter("stime");
			String etime = request.getParameter("etime");
			HttpSession session = request.getSession();
			String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

			Map<String, Object> params = new HashMap<>();
			params.put("cluster", clusterAlias);
			params.put("stime", stime);
			params.put("etime", etime);
			if (!Strings.isNullOrEmpty(topics)) {
				String[] topicStrs = topics.split(",");
				Set<String> topicSets = new HashSet<>();
				for (String topic : topicStrs) {
					topicSets.add(topic);
				}
				params.put("topics", topicSets);
			}

			byte[] output = topicService.getSelectTopicsLogSize(clusterAlias, params).getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Clean topic data by ajax. */
	@RequestMapping(value = "/topic/clean/data/{topic}/", method = RequestMethod.GET)
	public ModelAndView cleanTopicDataAjax(@PathVariable("topic") String topic, HttpServletResponse response, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		try {
			HttpSession session = request.getSession();
			String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

			TopicRank tr = new TopicRank();
			tr.setCluster(clusterAlias);
			tr.setTopic(topic);
			tr.setTkey(Topic.TRUNCATE);
			tr.setTvalue(0);
			if (topicService.addCleanTopicData(Arrays.asList(tr)) > 0) {
				mav.setViewName("redirect:/topic/list");
			} else {
				mav.setViewName("redirect:/errors/500");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			mav.setViewName("redirect:/errors/500");
		}
		return mav;
	}

	/** Create topic form. */
	@RequestMapping(value = "/topic/create/form", method = RequestMethod.POST)
	public ModelAndView topicAddForm(HttpSession session, HttpServletResponse response, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		String ke_topic_name = request.getParameter("ke_topic_name");
		String ke_topic_partition = request.getParameter("ke_topic_partition");
		String ke_topic_repli = request.getParameter("ke_topic_repli");
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
		Map<String, Object> respons = kafkaService.create(clusterAlias, ke_topic_name, ke_topic_partition, ke_topic_repli);
		if ("success".equals(respons.get("status"))) {
			session.removeAttribute("Submit_Status");
			session.setAttribute("Submit_Status", respons.get("info"));
			mav.setViewName("redirect:/topic/create/success");
		} else {
			session.removeAttribute("Submit_Status");
			session.setAttribute("Submit_Status", respons.get("info"));
			mav.setViewName("redirect:/topic/create/failed");
		}
		return mav;
	}

	/** Delete topic. */
	@RequestMapping(value = "/topic/{topicName}/{token}/delete", method = RequestMethod.GET)
	public ModelAndView topicDelete(@PathVariable("topicName") String topicName, @PathVariable("token") String token, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		if (SystemConfigUtils.getProperty("kafka.eagle.topic.token").equals(token) && !Kafka.CONSUMER_OFFSET_TOPIC.equals(topicName)) {
			String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
			Map<String, Object> respons = kafkaService.delete(clusterAlias, topicName);
			if ("success".equals(respons.get("status"))) {
				mav.setViewName("redirect:/topic/list");
			} else {
				mav.setViewName("redirect:/errors/500");
			}
		} else {
			mav.setViewName("redirect:/errors/403");
		}
		return mav;
	}

	/** Modify topic partitions. */
	@RequestMapping(value = "/topic/{topicName}/{partitions}/modify", method = RequestMethod.GET)
	public ModelAndView topicModifyPartitions(@PathVariable("topicName") String topicName, @PathVariable("partitions") int token, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
		Map<String, Object> respons = brokerService.createTopicPartitions(clusterAlias, topicName, token);
		if ("success".equals(respons.get("status"))) {
			mav.setViewName("redirect:/topic/list");
		} else {
			mav.setViewName("redirect:/errors/500");
		}
		return mav;
	}

	/** Logical execute kafka sql. */
	@RequestMapping(value = "/topic/logical/commit/", method = RequestMethod.GET)
	public void topicSqlLogicalAjax(@RequestParam String sql, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
		TopicSqlHistory topicSql = new TopicSqlHistory();
		try {
			String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
			String target = topicService.execute(clusterAlias, sql);
			JSONObject result = JSON.parseObject(target);
			try {
				topicSql.setCluster(clusterAlias);
				topicSql.setCreated(CalendarUtils.getDate());
				topicSql.setHost(request.getRemoteHost());
				topicSql.setKsql(sql);
				if (result.getBoolean("error")) {
					topicSql.setStatus("FAILED");
					topicSql.setSpendTime(0);
				} else {
					topicSql.setStatus("SUCCESSED");
					topicSql.setSpendTime(result.getLongValue("spent"));
				}
				topicSql.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
				Signiner signin = (Signiner) SecurityUtils.getSubject().getSession().getAttribute(KConstants.Login.SESSION_USER);
				topicSql.setUsername(signin.getUsername());
				topicService.writeTopicSqlHistory(Arrays.asList(topicSql));
			} catch (Exception e) {
				e.printStackTrace();
			}
			byte[] output = result.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get topic page message from kafka. */
	@RequestMapping(value = "/topic/physics/commit/", method = RequestMethod.GET)
	public void topicSqlPhysicsAjax(@RequestParam String sql, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
		String aoData = request.getParameter("aoData");
		JSONArray params = JSON.parseArray(aoData);
		int sEcho = 0, iDisplayStart = 0, iDisplayLength = 0;
		for (Object object : params) {
			JSONObject param = (JSONObject) object;
			if ("sEcho".equals(param.getString("name"))) {
				sEcho = param.getIntValue("value");
			} else if ("iDisplayStart".equals(param.getString("name"))) {
				iDisplayStart = param.getIntValue("value");
			} else if ("iDisplayLength".equals(param.getString("name"))) {
				iDisplayLength = param.getIntValue("value");
			}
		}

		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

		String text = topicService.execute(clusterAlias, sql);
		JSONObject result = JSON.parseObject(text);

		JSONArray topics = JSON.parseArray(result.getString("msg"));
		JSONArray aaDatas = new JSONArray();
		int offset = 0;
		if (topics != null) {
			for (Object object : topics) {
				JSONObject topic = (JSONObject) object;
				if (offset < (iDisplayLength + iDisplayStart) && offset >= iDisplayStart) {
					JSONObject obj = new JSONObject();
					for (String key : topic.keySet()) {
						obj.put(key, topic.get(key));
					}
					aaDatas.add(obj);
				}
				offset++;
			}
		}

		JSONObject target = new JSONObject();
		target.put("sEcho", sEcho);
		target.put("iTotalRecords", topics.size());
		target.put("iTotalDisplayRecords", topics.size());
		target.put("aaData", aaDatas);
		try {
			byte[] output = target.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get topic sql history. */
	@RequestMapping(value = "/topic/sql/history/ajax", method = RequestMethod.GET)
	public void topicSqlHistoryAjax(HttpServletResponse response, HttpServletRequest request) {
		String aoData = request.getParameter("aoData");
		JSONArray params = JSON.parseArray(aoData);
		int sEcho = 0, iDisplayStart = 0, iDisplayLength = 0;
		String search = "";
		for (Object object : params) {
			JSONObject param = (JSONObject) object;
			if ("sEcho".equals(param.getString("name"))) {
				sEcho = param.getIntValue("value");
			} else if ("iDisplayStart".equals(param.getString("name"))) {
				iDisplayStart = param.getIntValue("value");
			} else if ("iDisplayLength".equals(param.getString("name"))) {
				iDisplayLength = param.getIntValue("value");
			} else if ("sSearch".equals(param.getString("name"))) {
				search = param.getString("value");
			}
		}

		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

		Signiner signin = (Signiner) SecurityUtils.getSubject().getSession().getAttribute(KConstants.Login.SESSION_USER);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("cluster", clusterAlias);
		map.put("search", search);
		map.put("username", signin.getUsername());
		map.put("start", iDisplayStart);
		map.put("size", iDisplayLength);
		long count = 0L;
		List<TopicSqlHistory> topicSqls = null;
		if (signin.getUsername().equals(KConstants.Role.ADMIN)) {
			topicSqls = topicService.readTopicSqlHistoryByAdmin(map);
			count = topicService.countTopicSqlHistoryByAdmin(map);
		} else {
			topicSqls = topicService.readTopicSqlHistory(map);
			count = topicService.countTopicSqlHistory(map);
		}

		JSONArray aaDatas = new JSONArray();
		if (topicSqls != null) {
			for (TopicSqlHistory topicSql : topicSqls) {
				JSONObject obj = new JSONObject();
				int id = topicSql.getId();
				String host = topicSql.getHost();
				String ksql = topicSql.getKsql();
				obj.put("id", id);
				obj.put("username", topicSql.getUsername());
				obj.put("host", "<a href='#" + id + "/host' name='ke_sql_query_detail'>" + (host.length() > 20 ? host.substring(0, 20) + "..." : host) + "</a>");
				obj.put("ksql", "<a href='#" + id + "/ksql' name='ke_sql_query_detail'>" + (ksql.length() > 60 ? ksql.substring(0, 60) + "..." : ksql) + "</a>");
				if (topicSql.getStatus().equals("SUCCESSED")) {
					obj.put("status", "<a class='btn btn-success btn-xs'>" + topicSql.getStatus() + "</a>");
				} else {
					obj.put("status", "<a class='btn btn-danger btn-xs'>" + topicSql.getStatus() + "</a>");
				}
				obj.put("spendTime", topicSql.getSpendTime() / 1000.0 + "s");
				obj.put("created", topicSql.getCreated());
				aaDatas.add(obj);
			}
		}
		JSONObject target = new JSONObject();
		target.put("sEcho", sEcho);
		target.put("iTotalRecords", count);
		target.put("iTotalDisplayRecords", count);
		target.put("aaData", aaDatas);
		try {
			byte[] output = target.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get ksql host or sql detail. */
	@RequestMapping(value = "/topic/ksql/detail/{type}/{id}/ajax", method = RequestMethod.GET)
	public void getKSqlDetailByIdAjax(@PathVariable("id") int id, @PathVariable("type") String type, HttpServletResponse response, HttpServletRequest request) {
		try {
			JSONObject object = new JSONObject();
			Map<String, Object> params = new HashMap<>();
			params.put("id", id);
			if ("host".equals(type)) {
				object.put("result", topicService.findTopicSqlByID(params).getHost());
			} else if ("ksql".equals(type)) {
				object.put("result", topicService.findTopicSqlByID(params).getKsql());
			}
			byte[] output = object.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get producer chart data by ajax. */
	@RequestMapping(value = "/topic/producer/chart/ajax", method = RequestMethod.GET)
	public void topicProducerChartAjax(HttpServletResponse response, HttpServletRequest request, HttpSession session) {
		try {
			String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

			Map<String, Object> param = new HashMap<>();
			param.put("cluster", clusterAlias);
			param.put("stime", request.getParameter("stime"));
			param.put("etime", request.getParameter("etime"));
			param.put("topic", request.getParameter("topic"));
			String target = topicService.queryTopicProducerChart(param);
			if (StringUtils.isEmpty(target)) {
				target = "";
			}
			byte[] output = target.getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
