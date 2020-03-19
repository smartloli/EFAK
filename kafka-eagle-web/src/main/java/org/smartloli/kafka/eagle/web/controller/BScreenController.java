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

import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.KConstants.Topic;
import org.smartloli.kafka.eagle.web.service.BScreenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Big screen controller to viewer data.
 * 
 * @author smartloli.
 *
 *         Created by Aug 28, 2019.
 * 
 */
@Controller
public class BScreenController {

	@Autowired
	private BScreenService bscreen;

	/** Big screen viewer. */
	@RequestMapping(value = "/bs", method = RequestMethod.GET)
	public ModelAndView indexView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/bscreen/bscreen");
		return mav;
	}

	/** Get producer and consumer real rate data by ajax. */
	@RequestMapping(value = "/bs/brokers/ins/outs/realrate/ajax", method = RequestMethod.GET)
	public void getProducerAndConsumerRealRateAjax(HttpServletResponse response, HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
		try {
			byte[] output = bscreen.getProducerAndConsumerRate(clusterAlias).getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get producer and consumer real rate data by ajax. */
	@RequestMapping(value = "/bs/topic/total/logsize/ajax", method = RequestMethod.GET)
	public void getTopicTotalLogSizeAjax(HttpServletResponse response, HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
		try {
			byte[] output = bscreen.getTopicTotalLogSize(clusterAlias).getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get producer history bar data by ajax. */
	@RequestMapping(value = "/bs/{type}/history/ajax", method = RequestMethod.GET)
	public void getProducerOrConsumerHistoryAjax(@PathVariable("type") String type, HttpServletResponse response, HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
		try {
			byte[] output = bscreen.getProducerOrConsumerHistory(clusterAlias, type).getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get today consumer and producer data and lag data by ajax. */
	@RequestMapping(value = "/bs/{dtype}/day/ajax", method = RequestMethod.GET)
	public void getTodayOrHistoryConsumerProducerAjax(@PathVariable("dtype") String dtype, HttpServletResponse response, HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
		try {
			byte[] output = bscreen.getTodayOrHistoryConsumerProducer(clusterAlias, dtype).getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get today consumer and producer data and lag data by ajax. */
	@RequestMapping(value = "/bs/topic/total/capacity/ajax", method = RequestMethod.GET)
	public void getTopicTotalCapacityAjax(HttpServletResponse response, HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("cluster", clusterAlias);
			params.put("tkey", Topic.CAPACITY);
			byte[] output = bscreen.getTopicCapacity(params).getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
