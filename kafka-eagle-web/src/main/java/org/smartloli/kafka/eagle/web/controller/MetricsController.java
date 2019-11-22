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

import org.apache.commons.lang.StringUtils;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.web.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Metrics mbean controller to viewer data.
 * 
 * @author smartloli.
 *
 *         Created by Sep 6, 2016.
 */
@Controller
public class MetricsController {

	@Autowired
	private MetricsService metricsService;

	/** Brokers viewer. */
	@RequestMapping(value = "/metrics/brokers", method = RequestMethod.GET)
	public ModelAndView clusterView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/metrics/brokers");
		return mav;
	}

	/** Trend viewer. */
	@RequestMapping(value = "/metrics/kafka", method = RequestMethod.GET)
	public ModelAndView trendView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/metrics/kafka");
		return mav;
	}
	
	/** Trend viewer. */
	@RequestMapping(value = "/metrics/zk", method = RequestMethod.GET)
	public ModelAndView zkView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/metrics/zk");
		return mav;
	}

	/** Get cluster data by ajax. */
	@RequestMapping(value = "/metrics/brokers/mbean/ajax", method = RequestMethod.GET)
	public void clusterAjax(HttpServletResponse response, HttpServletRequest request, HttpSession session) {
		try {
			String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
			String target = metricsService.getAllBrokersMBean(clusterAlias);

			byte[] output = target.getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get trend data by ajax. */
	@RequestMapping(value = "/metrics/trend/mbean/ajax", method = RequestMethod.GET)
	public void trendAjax(HttpServletResponse response, HttpServletRequest request, HttpSession session) {
		try {
			String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

			Map<String, Object> param = new HashMap<>();
			param.put("cluster", clusterAlias);
			param.put("stime", request.getParameter("stime"));
			param.put("etime", request.getParameter("etime"));
			param.put("type", request.getParameter("type"));
            String modules = request.getParameter("modules");
            if (StringUtils.isNotBlank(modules)) {
                param.put("modules", Arrays.asList(modules.split(",")));
            }
			String target = metricsService.query(param);
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
