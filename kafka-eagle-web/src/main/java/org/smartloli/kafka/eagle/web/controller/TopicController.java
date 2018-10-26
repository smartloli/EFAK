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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsFactory;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsService;
import org.smartloli.kafka.eagle.web.service.TopicService;

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

	/** Kafka metrics interface. */
	private KafkaMetricsService kafkaMetricsService = new KafkaMetricsFactory().create();

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

	/** Topic message viewer. */
	@RequiresPermissions("/topic/export")
	@RequestMapping(value = "/topic/export", method = RequestMethod.GET)
	public ModelAndView topicExportView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/topic/export");
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

		String metadata = topicService.metadata(clusterAlias, tname);
		JSONArray metadatas = JSON.parseArray(metadata);
		int offset = 0;
		JSONArray aaDatas = new JSONArray();
		for (Object object : metadatas) {
			JSONObject meta = (JSONObject) object;
			if (offset < (iDisplayLength + iDisplayStart) && offset >= iDisplayStart) {
				JSONObject obj = new JSONObject();
				obj.put("topic", tname);
				obj.put("partition", meta.getInteger("partitionId"));
				obj.put("leader", meta.getInteger("leader"));
				obj.put("replicas", meta.getString("replicas"));
				obj.put("isr", meta.getString("isr"));
				aaDatas.add(obj);
			}
			offset++;
		}

		JSONObject target = new JSONObject();
		target.put("sEcho", sEcho);
		target.put("iTotalRecords", metadatas.size());
		target.put("iTotalDisplayRecords", metadatas.size());
		target.put("aaData", aaDatas);
		try {
			byte[] output = target.toJSONString().getBytes();
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

	/***/
	@RequestMapping(value = "/topic/mock/send/message/{topic}/ajax", method = RequestMethod.GET)
	public void topicMockSend(@PathVariable("topic") String topic, @RequestParam("message") String message, HttpServletResponse response, HttpServletRequest request) {
		try {
			HttpSession session = request.getSession();
			String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
			JSONObject object = new JSONObject();
			object.put("status", topicService.mockSendMsg(clusterAlias, topic, message));
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

		JSONArray topics = JSON.parseArray(topicService.list(clusterAlias));
		int offset = 0;
		JSONArray aaDatas = new JSONArray();
		for (Object object : topics) {
			JSONObject topic = (JSONObject) object;
			if (search.length() > 0 && topic.getString("topic").contains(search)) {
				JSONObject obj = new JSONObject();
				obj.put("id", topic.getInteger("id"));
				obj.put("topic", "<a href='/ke/topic/meta/" + topic.getString("topic") + "/' target='_blank'>" + topic.getString("topic") + "</a>");
				obj.put("partitions", topic.getString("partitions").length() > 50 ? topic.getString("partitions").substring(0, 50) + "..." : topic.getString("partitions"));
				obj.put("partitionNumbers", topic.getInteger("partitionNumbers"));
				obj.put("topicSize", "<a class='btn btn-primary btn-xs'>" + kafkaMetricsService.topicSize(clusterAlias, topic.getString("topic")) + "</a>&nbsp");
				obj.put("created", topic.getString("created"));
				obj.put("modify", topic.getString("modify"));
				obj.put("operate", "<a name='remove' href='#" + topic.getString("topic") + "' class='btn btn-danger btn-xs'>Remove</a>&nbsp");
				aaDatas.add(obj);
			} else if (search.length() == 0) {
				if (offset < (iDisplayLength + iDisplayStart) && offset >= iDisplayStart) {
					JSONObject obj = new JSONObject();
					obj.put("id", topic.getInteger("id"));
					obj.put("topic", "<a href='/ke/topic/meta/" + topic.getString("topic") + "/' target='_blank'>" + topic.getString("topic") + "</a>");
					obj.put("partitions", topic.getString("partitions").length() > 50 ? topic.getString("partitions").substring(0, 50) + "..." : topic.getString("partitions"));
					obj.put("partitionNumbers", topic.getInteger("partitionNumbers"));
					obj.put("topicSize", "<a class='btn btn-primary btn-xs'>" + kafkaMetricsService.topicSize(clusterAlias, topic.getString("topic")) + "</a>&nbsp");
					obj.put("created", topic.getString("created"));
					obj.put("modify", topic.getString("modify"));
					obj.put("operate", "<a name='remove' href='#" + topic.getString("topic") + "' class='btn btn-danger btn-xs'>Remove</a>&nbsp");
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
		if (SystemConfigUtils.getProperty("kafka.eagle.topic.token").equals(token)) {
			String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
			Map<String, Object> respons = kafkaService.delete(clusterAlias, topicName);
			if ("success".equals(respons.get("status"))) {
				mav.setViewName("redirect:/topic/list");
			} else {
				mav.setViewName("redirect:/errors/500");
			}
		} else {
			mav.setViewName("redirect:/errors/500");
		}
		return mav;
	}

	/** Logical execute kafka sql. */
	@RequestMapping(value = "/topic/logical/commit/", method = RequestMethod.GET)
	public void topicSqlLogicalAjax(@RequestParam String sql, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
		try {
			String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
			String target = topicService.execute(clusterAlias, sql);
			JSONObject result = JSON.parseObject(target);

			byte[] output = result.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get topic message by ajax. */
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

}
