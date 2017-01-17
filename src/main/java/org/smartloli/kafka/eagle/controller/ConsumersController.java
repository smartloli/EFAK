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
package org.smartloli.kafka.eagle.controller;

import java.io.OutputStream;

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

import org.smartloli.kafka.eagle.domain.ConsumerPageDomain;
import org.smartloli.kafka.eagle.service.ConsumerService;
import org.smartloli.kafka.eagle.util.GzipUtils;
import org.smartloli.kafka.eagle.util.SystemConfigUtils;

/**
 * Kafka consumer controller to viewer data.
 * 
 * @author smartloli.
 *
 *         Created by Sep 6, 2016
 */
@Controller
public class ConsumersController {

	/** Kafka consumer service interface. */
	@Autowired
	private ConsumerService consumerService;

	/** Consumer viewer. */
	@RequestMapping(value = "/consumers", method = RequestMethod.GET)
	public ModelAndView consumersView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/consumers/consumers");
		return mav;
	}

	/** Get consumer data by ajax. */
	@RequestMapping(value = "/consumers/info/ajax", method = RequestMethod.GET)
	public void consumersGraphAjax(HttpServletResponse response, HttpServletRequest request) {
		response.setContentType("text/html;charset=utf-8");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Charset", "utf-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Content-Encoding", "gzip");

		try {
			String formatter = SystemConfigUtils.getProperty("kafka.eagle.offset.storage");
			byte[] output = GzipUtils.compressToByte(consumerService.getActiveTopic(formatter));
			response.setContentLength(output.length);
			OutputStream out = response.getOutputStream();
			out.write(output);

			out.flush();
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get consumer datasets by ajax. */
	@RequestMapping(value = "/consumer/list/table/ajax", method = RequestMethod.GET)
	public void consumerTableAjax(HttpServletResponse response, HttpServletRequest request) {
		response.setContentType("text/html;charset=utf-8");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Charset", "utf-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Content-Encoding", "gzip");

		String aoData = request.getParameter("aoData");
		JSONArray jsonArray = JSON.parseArray(aoData);
		int sEcho = 0, iDisplayStart = 0, iDisplayLength = 0;
		String search = "";
		for (Object obj : jsonArray) {
			JSONObject jsonObj = (JSONObject) obj;
			if ("sEcho".equals(jsonObj.getString("name"))) {
				sEcho = jsonObj.getIntValue("value");
			} else if ("iDisplayStart".equals(jsonObj.getString("name"))) {
				iDisplayStart = jsonObj.getIntValue("value");
			} else if ("iDisplayLength".equals(jsonObj.getString("name"))) {
				iDisplayLength = jsonObj.getIntValue("value");
			} else if ("sSearch".equals(jsonObj.getString("name"))) {
				search = jsonObj.getString("value");
			}
		}

		ConsumerPageDomain page = new ConsumerPageDomain();
		page.setSearch(search);
		page.setiDisplayLength(iDisplayLength);
		page.setiDisplayStart(iDisplayStart);

		String formatter = SystemConfigUtils.getProperty("kafka.eagle.offset.storage");
		int count = consumerService.getConsumerCount(formatter);
		JSONArray ret = JSON.parseArray(consumerService.getConsumer(formatter, page));
		JSONArray retArr = new JSONArray();
		for (Object tmp : ret) {
			JSONObject tmp2 = (JSONObject) tmp;
			JSONObject obj = new JSONObject();
			obj.put("id", tmp2.getInteger("id"));
			obj.put("group", "<a class='link' href='#" + tmp2.getString("group") + "'>" + tmp2.getString("group") + "</a>");
			obj.put("topic", tmp2.getString("topic").length() > 50 ? tmp2.getString("topic").substring(0, 50) + "..." : tmp2.getString("topic"));
			obj.put("consumerNumber", tmp2.getInteger("consumerNumber"));
			int activerNumber = tmp2.getInteger("activeNumber");
			if (activerNumber > 0) {
				obj.put("activeNumber", "<a class='btn btn-success btn-xs'>" + tmp2.getInteger("activeNumber") + "</a>");
			} else {
				obj.put("activeNumber", "<a class='btn btn-danger btn-xs'>" + tmp2.getInteger("activeNumber") + "</a>");
			}
			retArr.add(obj);
		}

		JSONObject obj = new JSONObject();
		obj.put("sEcho", sEcho);
		obj.put("iTotalRecords", count);
		obj.put("iTotalDisplayRecords", count);
		obj.put("aaData", retArr);
		try {
			byte[] output = GzipUtils.compressToByte(obj.toJSONString());
			response.setContentLength(output.length);
			OutputStream out = response.getOutputStream();
			out.write(output);

			out.flush();
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get consumer data through group by ajax. */
	@RequestMapping(value = "/consumer/{group}/table/ajax", method = RequestMethod.GET)
	public void consumerTableListAjax(@PathVariable("group") String group, HttpServletResponse response, HttpServletRequest request) {
		response.setContentType("text/html;charset=utf-8");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Charset", "utf-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Content-Encoding", "gzip");

		String aoData = request.getParameter("aoData");
		JSONArray jsonArray = JSON.parseArray(aoData);
		int sEcho = 0, iDisplayStart = 0, iDisplayLength = 0;
		for (Object obj : jsonArray) {
			JSONObject jsonObj = (JSONObject) obj;
			if ("sEcho".equals(jsonObj.getString("name"))) {
				sEcho = jsonObj.getIntValue("value");
			} else if ("iDisplayStart".equals(jsonObj.getString("name"))) {
				iDisplayStart = jsonObj.getIntValue("value");
			} else if ("iDisplayLength".equals(jsonObj.getString("name"))) {
				iDisplayLength = jsonObj.getIntValue("value");
			}
		}

		String formatter = SystemConfigUtils.getProperty("kafka.eagle.offset.storage");
		JSONArray ret = JSON.parseArray(consumerService.getConsumerDetail(formatter, group));
		int offset = 0;
		JSONArray retArr = new JSONArray();
		for (Object tmp : ret) {
			JSONObject tmp2 = (JSONObject) tmp;
			if (offset < (iDisplayLength + iDisplayStart) && offset >= iDisplayStart) {
				JSONObject obj = new JSONObject();
				String topic = tmp2.getString("topic");
				obj.put("id", tmp2.getInteger("id"));
				obj.put("topic", topic);
				if (tmp2.getBoolean("isConsumering")) {
					obj.put("isConsumering", "<a href='/ke/consumers/offset/" + group + "/" + topic + "/' target='_blank' class='btn btn-success btn-xs'>Running</a>");
				} else {
					obj.put("isConsumering", "<a href='/ke/consumers/offset/" + group + "/" + topic + "/' target='_blank' class='btn btn-danger btn-xs'>Pending</a>");
				}
				retArr.add(obj);
			}
			offset++;
		}

		JSONObject obj = new JSONObject();
		obj.put("sEcho", sEcho);
		obj.put("iTotalRecords", ret.size());
		obj.put("iTotalDisplayRecords", ret.size());
		obj.put("aaData", retArr);
		try {
			byte[] output = GzipUtils.compressToByte(obj.toJSONString());
			response.setContentLength(output.length);
			OutputStream out = response.getOutputStream();
			out.write(output);

			out.flush();
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
