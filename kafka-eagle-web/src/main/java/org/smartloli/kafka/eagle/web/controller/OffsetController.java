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

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.smartloli.kafka.eagle.common.protocol.OffsetInfo;
import org.smartloli.kafka.eagle.common.protocol.offsets.TopicOffsetInfo;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.StrUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.web.service.OffsetService;
import org.smartloli.kafka.eagle.web.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Kafka offset controller to viewer data.
 * 
 * @author smartloli.
 *
 *         Created by Sep 6, 2016.
 * 
 *         Update by hexiang 20170216
 */
@Controller
public class OffsetController {

	/** Offsets consumer data interface. */
	@Autowired
	private OffsetService offsetService;

	/** Kafka topic service interface. */
	@Autowired
	private TopicService topicService;

	/** Consumer viewer. */
	@RequestMapping(value = "/consumers/offset/", method = RequestMethod.GET)
	public ModelAndView consumersActiveView(HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
		String formatter = SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage");
		String group = StrUtils.convertNull(request.getParameter("group"));
		String topic = StrUtils.convertNull(request.getParameter("topic"));

		try {
			group = URLDecoder.decode(group, "UTF-8");
			topic = URLDecoder.decode(topic, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (offsetService.hasGroupTopic(clusterAlias, formatter, group, topic)) {
			mav.setViewName("/consumers/offset_consumers");
		} else {
			mav.setViewName("/error/404");
		}
		return mav;
	}

	/** Get real-time offset data from Kafka by ajax. */
	@RequestMapping(value = "/consumers/offset/realtime/", method = RequestMethod.GET)
	public ModelAndView offsetRealtimeView(HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
		String formatter = SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage");
		String group = StrUtils.convertNull(request.getParameter("group"));
		String topic = StrUtils.convertNull(request.getParameter("topic"));

		try {
			group = URLDecoder.decode(group, "UTF-8");
			topic = URLDecoder.decode(topic, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (offsetService.hasGroupTopic(clusterAlias, formatter, group, topic)) {
			mav.setViewName("/consumers/offset_realtime");
		} else {
			mav.setViewName("/error/404");
		}
		return mav;
	}

	/** Get detail offset from Kafka by ajax. */
	@RequestMapping(value = "/consumer/offset/group/topic/ajax", method = RequestMethod.GET)
	public void offsetDetailAjax(HttpServletResponse response, HttpServletRequest request) {
		String aoData = request.getParameter("aoData");
		String group = StrUtils.convertNull(request.getParameter("group"));
		String topic = StrUtils.convertNull(request.getParameter("topic"));
		try {
			group = URLDecoder.decode(group, "UTF-8");
			topic = URLDecoder.decode(topic, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
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

		String formatter = SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage");

		TopicOffsetInfo topicOffset = new TopicOffsetInfo();
		topicOffset.setCluster(clusterAlias);
		topicOffset.setFormatter(formatter);
		topicOffset.setGroup(group);
		topicOffset.setPageSize(iDisplayLength);
		topicOffset.setStartPage(iDisplayStart);
		topicOffset.setTopic(topic);

		List<OffsetInfo> logSizes = offsetService.getConsumerOffsets(topicOffset);
		long count = topicService.getPartitionNumbers(clusterAlias, topic);
		JSONArray aaDatas = new JSONArray();
		for (OffsetInfo offsetInfo : logSizes) {
			JSONObject object = new JSONObject();
			object.put("partition", offsetInfo.getPartition());
			if (offsetInfo.getLogSize() == 0) {
				object.put("logsize", "<a class='btn btn-warning btn-xs'>0</a>");
			} else {
				object.put("logsize", offsetInfo.getLogSize());
			}
			if (offsetInfo.getOffset() == -1) {
				object.put("offset", "<a class='btn btn-warning btn-xs'>0</a>");
			} else {
				object.put("offset", "<a class='btn btn-success btn-xs'>" + offsetInfo.getOffset() + "</a>");
			}
			object.put("lag", "<a class='btn btn-danger btn-xs'>" + offsetInfo.getLag() + "</a>");
			object.put("owner", offsetInfo.getOwner());
			object.put("created", offsetInfo.getCreate());
			object.put("modify", offsetInfo.getModify());
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

	/** Get real-time offset graph data from Kafka by ajax. */
	@RequestMapping(value = "/consumer/offset/group/topic/realtime/ajax", method = RequestMethod.GET)
	public void offsetGraphAjax(HttpServletResponse response, HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

		try {
			String group = StrUtils.convertNull(request.getParameter("group"));
			String topic = StrUtils.convertNull(request.getParameter("topic"));
			try {
				group = URLDecoder.decode(group, "UTF-8");
				topic = URLDecoder.decode(topic, "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
			Map<String, Object> param = new HashMap<>();
			param.put("cluster", clusterAlias);
			param.put("group", group);
			param.put("topic", topic);
			param.put("stime", request.getParameter("stime"));
			param.put("etime", request.getParameter("etime"));

			byte[] output = offsetService.getOffsetsGraph(param).getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get real-time offset graph data from Kafka by ajax. */
	@RequestMapping(value = "/consumer/offset/rate/group/topic/realtime/ajax", method = RequestMethod.GET)
	public void offsetRateGraphAjax(HttpServletResponse response, HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

		try {
			String group = StrUtils.convertNull(request.getParameter("group"));
			String topic = StrUtils.convertNull(request.getParameter("topic"));
			try {
				group = URLDecoder.decode(group, "UTF-8");
				topic = URLDecoder.decode(topic, "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
			Map<String, Object> params = new HashMap<>();
			params.put("cluster", clusterAlias);
			params.put("group", group);
			params.put("topic", topic);

			byte[] output = offsetService.getOffsetRate(params).getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
