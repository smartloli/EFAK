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

import org.smartloli.kafka.eagle.service.ClusterService;
import org.smartloli.kafka.eagle.util.GzipUtils;

/**
 * Kafka & Zookeeper controller to viewer data.
 * 
 * @author smartloli.
 *
 *         Created by Sep 6, 2016
 */
@Controller
public class ClusterController {

	@Autowired
	private ClusterService clusterService;

	/** Cluster viewer. */
	@RequestMapping(value = "/cluster/info", method = RequestMethod.GET)
	public ModelAndView clusterView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/cluster/cluster");
		return mav;
	}

	/** Zookeeper client viewer. */
	@RequestMapping(value = "/cluster/zkcli", method = RequestMethod.GET)
	public ModelAndView zkCliView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/cluster/zkcli");
		return mav;
	}

	// /** Get cluster data by ajax. */
	// @RequestMapping(value = "/cluster/info/{type}/ajax", method =
	// RequestMethod.GET)
	// public void clusterAjax(@PathVariable("type") String type,
	// HttpServletResponse response, HttpServletRequest request) {
	// response.setContentType("text/html;charset=utf-8");
	// response.setCharacterEncoding("utf-8");
	// response.setHeader("Charset", "utf-8");
	// response.setHeader("Cache-Control", "no-cache");
	// response.setHeader("Content-Encoding", "gzip");
	//
	// try {
	// byte[] output = GzipUtils.compressToByte(clusterService.get(type));
	// response.setContentLength(output == null ? "NULL".toCharArray().length :
	// output.length);
	// OutputStream out = response.getOutputStream();
	// out.write(output);
	//
	// out.flush();
	// out.close();
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// }
	// }

	/** Get cluster data by ajax. */
	@RequestMapping(value = "/cluster/info/{type}/ajax", method = RequestMethod.GET)
	public void clusterAjax(@PathVariable("type") String type, HttpServletResponse response, HttpServletRequest request) {
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

		JSONObject target = JSON.parseObject(clusterService.get(type));
		JSONArray ret = target.getJSONArray(type);
		int offset = 0;
		JSONArray retArr = new JSONArray();
		for (Object tmp : ret) {
			JSONObject tmp2 = (JSONObject) tmp;
			if (search.length() > 0 && search.equals(tmp2.getString("host"))) {
				JSONObject obj = new JSONObject();
				obj.put("id", tmp2.getInteger("id"));
				obj.put("port", tmp2.getInteger("port"));
				obj.put("ip", tmp2.getString("host"));
				if ("kafka".equals(type)) {
					obj.put("created", tmp2.getString("created"));
					obj.put("modify", tmp2.getString("modify"));
				} else if ("zk".equals(type)) {
					String mode = tmp2.getString("mode");
					if ("death".equals(mode)) {
						obj.put("mode", "<a class='btn btn-danger btn-xs'>" + tmp2.getString("mode") + "</a>");
					} else {
						obj.put("mode", "<a class='btn btn-success btn-xs'>" + tmp2.getString("mode") + "</a>");
					}
				}
				retArr.add(obj);
			} else if (search.length() == 0) {
				if (offset < (iDisplayLength + iDisplayStart) && offset >= iDisplayStart) {
					JSONObject obj = new JSONObject();
					obj.put("id", tmp2.getInteger("id"));
					obj.put("port", tmp2.getInteger("port"));
					obj.put("ip", tmp2.getString("host"));
					if ("kafka".equals(type)) {
						obj.put("created", tmp2.getString("created"));
						obj.put("modify", tmp2.getString("modify"));
					} else if ("zk".equals(type)) {
						String mode = tmp2.getString("mode");
						if ("death".equals(mode)) {
							obj.put("mode", "<a class='btn btn-danger btn-xs'>" + tmp2.getString("mode") + "</a>");
						} else {
							obj.put("mode", "<a class='btn btn-success btn-xs'>" + tmp2.getString("mode") + "</a>");
						}
					}
					retArr.add(obj);
				}
				offset++;
			}
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

	/** Get zookeeper client whether live data by ajax. */
	@RequestMapping(value = "/cluster/zk/islive/ajax", method = RequestMethod.GET)
	public void zkCliLiveAjax(HttpServletResponse response, HttpServletRequest request) {
		response.setContentType("text/html;charset=utf-8");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Charset", "utf-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Content-Encoding", "gzip");

		try {
			byte[] output = GzipUtils.compressToByte(clusterService.status().toJSONString());
			response.setContentLength(output == null ? "NULL".toCharArray().length : output.length);
			OutputStream out = response.getOutputStream();
			out.write(output);

			out.flush();
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Execute zookeeper command by ajax. */
	@RequestMapping(value = "/cluster/zk/cmd/ajax", method = RequestMethod.GET)
	public void zkCliCmdAjax(HttpServletResponse response, HttpServletRequest request) {
		response.setContentType("text/html;charset=utf-8");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Charset", "utf-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Content-Encoding", "gzip");

		String cmd = request.getParameter("cmd");
		String type = request.getParameter("type");

		try {
			byte[] output = GzipUtils.compressToByte(clusterService.execute(cmd, type));
			response.setContentLength(output == null ? "NULL".toCharArray().length : output.length);
			OutputStream out = response.getOutputStream();
			out.write(output);

			out.flush();
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
