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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.web.service.OffsetService;

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

	/** Consumer viewer. */
	@RequestMapping(value = "/consumers/offset/{group}/{topic}/", method = RequestMethod.GET)
	public ModelAndView consumersActiveView(@PathVariable("group") String group, @PathVariable("topic") String topic, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
		String formatter = SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage");
		if (offsetService.hasGroupTopic(clusterAlias, formatter, group, topic)) {
			mav.setViewName("/consumers/offset_consumers");
		} else {
			mav.setViewName("/error/404");
		}
		return mav;
	}

	/** Get real-time offset data from Kafka by ajax. */
	@RequestMapping(value = "/consumers/offset/{group}/{topic}/realtime", method = RequestMethod.GET)
	public ModelAndView offsetRealtimeView(@PathVariable("group") String group, @PathVariable("topic") String topic, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
		String formatter = SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage");
		if (offsetService.hasGroupTopic(clusterAlias, formatter, group, topic)) {
			mav.setViewName("/consumers/offset_realtime");
		} else {
			mav.setViewName("/error/404");
		}
		return mav;
	}

	/** Get detail offset from Kafka by ajax. */
	@RequestMapping(value = "/consumer/offset/{group}/{topic}/ajax", method = RequestMethod.GET)
	public void offsetDetailAjax(@PathVariable("group") String group, @PathVariable("topic") String topic, HttpServletResponse response, HttpServletRequest request) {
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

		String formatter = SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage");
		JSONArray logSizes = JSON.parseArray(offsetService.getLogSize(clusterAlias, formatter, topic, group));
		int offset = 0;
		JSONArray aaDatas = new JSONArray();
		for (Object object : logSizes) {
			JSONObject logSize = (JSONObject) object;
			if (offset < (iDisplayLength + iDisplayStart) && offset >= iDisplayStart) {
				JSONObject obj = new JSONObject();
				obj.put("partition", logSize.getInteger("partition"));
				if (logSize.getLong("logSize") == 0) {
					obj.put("logsize", "<a class='btn btn-warning btn-xs'>0</a>");
				} else {
					obj.put("logsize", logSize.getLong("logSize"));
				}
				if (logSize.getLong("offset") == -1) {
					obj.put("offset", "<a class='btn btn-warning btn-xs'>0</a>");
				} else {
					obj.put("offset", "<a class='btn btn-success btn-xs'>" + logSize.getLong("offset") + "</a>");
				}
				obj.put("lag", "<a class='btn btn-danger btn-xs'>" + logSize.getLong("lag") + "</a>");
				obj.put("owner", logSize.getString("owner"));
				obj.put("node", logSize.getString("node"));
				obj.put("created", logSize.getString("create"));
				obj.put("modify", logSize.getString("modify"));
				aaDatas.add(obj);
			}
			offset++;
		}

		JSONObject target = new JSONObject();
		target.put("sEcho", sEcho);
		target.put("iTotalRecords", logSizes.size());
		target.put("iTotalDisplayRecords", logSizes.size());
		target.put("aaData", aaDatas);
		try {
			byte[] output = target.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get real-time offset graph data from Kafka by ajax. */
	@RequestMapping(value = "/consumer/offset/{group}/{topic}/realtime/ajax", method = RequestMethod.GET)
	public void offsetGraphAjax(@PathVariable("group") String group, @PathVariable("topic") String topic, HttpServletResponse response, HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

		try {
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
	@RequestMapping(value = "/consumer/offset/rate/{topic}/realtime/ajax", method = RequestMethod.GET)
	public void offsetRateGraphAjax(@PathVariable("topic") String topic, HttpServletResponse response, HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

		try {
			byte[] output = offsetService.getOffsetRate(clusterAlias, topic).getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
