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

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.KConstants.Kafka;
import org.smartloli.kafka.eagle.web.service.ClusterService;

/**
 * Kafka & Zookeeper controller to viewer data.
 * 
 * @author smartloli.
 *
 *         Created by Sep 6, 2016.
 * 
 *         Update by hexiang 20170216
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

	/** Cluster viewer. */
	@RequestMapping(value = "/cluster/multi", method = RequestMethod.GET)
	public ModelAndView clustersView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/cluster/multicluster");
		return mav;
	}

	/** Zookeeper client viewer. */
	@RequiresPermissions("/cluster/zkcli")
	@RequestMapping(value = "/cluster/zkcli", method = RequestMethod.GET)
	public ModelAndView zkCliView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/cluster/zkcli");
		return mav;
	}

	/** Get cluster data by ajax. */
	@RequestMapping(value = "/cluster/info/{type}/ajax", method = RequestMethod.GET)
	public void clusterAjax(@PathVariable("type") String type, HttpServletResponse response, HttpServletRequest request) {
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

		JSONObject deserializeClusters = JSON.parseObject(clusterService.get(clusterAlias, type));
		JSONArray clusters = deserializeClusters.getJSONArray(type);
		int offset = 0;
		JSONArray aaDatas = new JSONArray();
		for (Object object : clusters) {
			JSONObject cluster = (JSONObject) object;
			if (search.length() > 0 && search.equals(cluster.getString("host"))) {
				JSONObject obj = new JSONObject();
				obj.put("id", cluster.getInteger("id"));
				obj.put("port", cluster.getInteger("port"));
				obj.put("ip", cluster.getString("host"));
				if ("kafka".equals(type)) {
					obj.put("created", cluster.getString("created"));
					obj.put("modify", cluster.getString("modify"));
					String version = cluster.getString("version") == "" ? Kafka.UNKOWN : cluster.getString("version");
					if (Kafka.UNKOWN.equals(version)) {
						obj.put("version", "<a class='btn btn-danger btn-xs'>" + version + "</a>");
					} else {
						obj.put("version", "<a class='btn btn-success btn-xs'>" + version + "</a>");
					}
				} else if ("zk".equals(type)) {
					String mode = cluster.getString("mode");
					if ("death".equals(mode)) {
						obj.put("mode", "<a class='btn btn-danger btn-xs'>" + mode + "</a>");
					} else {
						obj.put("mode", "<a class='btn btn-success btn-xs'>" + mode + "</a>");
					}
				}
				aaDatas.add(obj);
			} else if (search.length() == 0) {
				if (offset < (iDisplayLength + iDisplayStart) && offset >= iDisplayStart) {
					JSONObject obj = new JSONObject();
					obj.put("id", cluster.getInteger("id"));
					obj.put("port", cluster.getInteger("port"));
					obj.put("ip", cluster.getString("host"));
					if ("kafka".equals(type)) {
						obj.put("created", cluster.getString("created"));
						obj.put("modify", cluster.getString("modify"));
						String version = cluster.getString("version") == "" ? Kafka.UNKOWN : cluster.getString("version");
						if (Kafka.UNKOWN.equals(version)) {
							obj.put("version", "<a class='btn btn-danger btn-xs'>" + version + "</a>");
						} else {
							obj.put("version", "<a class='btn btn-success btn-xs'>" + version + "</a>");
						}
					} else if ("zk".equals(type)) {
						String mode = cluster.getString("mode");
						if ("death".equals(mode)) {
							obj.put("mode", "<a class='btn btn-danger btn-xs'>" + mode + "</a>");
						} else {
							obj.put("mode", "<a class='btn btn-success btn-xs'>" + mode + "</a>");
						}
					}
					aaDatas.add(obj);
				}
				offset++;
			}
		}

		JSONObject target = new JSONObject();
		target.put("sEcho", sEcho);
		target.put("iTotalRecords", clusters.size());
		target.put("iTotalDisplayRecords", clusters.size());
		target.put("aaData", aaDatas);
		try {
			byte[] output = target.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Change cluster viewer address. */
	@RequestMapping(value = "/cluster/info/{clusterAlias}/change", method = RequestMethod.GET)
	public ModelAndView clusterChangeAjax(@PathVariable("clusterAlias") String clusterAlias, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
		if (!clusterService.hasClusterAlias(clusterAlias)) {
			return new ModelAndView("redirect:/error/404");
		} else {
			session.removeAttribute(KConstants.SessionAlias.CLUSTER_ALIAS);
			session.setAttribute(KConstants.SessionAlias.CLUSTER_ALIAS, clusterAlias);
			return new ModelAndView("redirect:/");
		}
	}

	/** Get multicluster information. */
	@RequestMapping(value = "/cluster/info/multicluster/ajax", method = RequestMethod.GET)
	public void multiClusterAjax(HttpServletResponse response, HttpServletRequest request) {
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

		JSONArray clusterAliass = clusterService.clusterAliass();
		int offset = 0;
		JSONArray aaDatas = new JSONArray();
		for (Object object : clusterAliass) {
			JSONObject cluster = (JSONObject) object;
			if (search.length() > 0 && cluster.getString("clusterAlias").contains(search)) {
				JSONObject target = new JSONObject();
				target.put("id", cluster.getInteger("id"));
				target.put("clusterAlias", cluster.getString("clusterAlias"));
				target.put("zkhost", cluster.getString("zkhost"));
				target.put("operate", "<a name='change' href='#" + cluster.getString("clusterAlias") + "' class='btn btn-primary btn-xs'>Change</a>");
				aaDatas.add(target);
			} else if (search.length() == 0) {
				if (offset < (iDisplayLength + iDisplayStart) && offset >= iDisplayStart) {
					JSONObject target = new JSONObject();
					target.put("id", cluster.getInteger("id"));
					target.put("clusterAlias", cluster.getString("clusterAlias"));
					target.put("zkhost", cluster.getString("zkhost"));
					target.put("operate", "<a name='change' href='#" + cluster.getString("clusterAlias") + "' class='btn btn-primary btn-xs'>Change</a>");
					aaDatas.add(target);
				}
				offset++;
			}
		}

		JSONObject target = new JSONObject();
		target.put("sEcho", sEcho);
		target.put("iTotalRecords", clusterAliass.size());
		target.put("iTotalDisplayRecords", clusterAliass.size());
		target.put("aaData", aaDatas);
		try {
			byte[] output = target.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get zookeeper client whether live data by ajax. */
	@RequestMapping(value = "/cluster/zk/islive/ajax", method = RequestMethod.GET)
	public void zkCliLiveAjax(HttpServletResponse response, HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

		try {
			byte[] output = clusterService.status(clusterAlias).toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Execute zookeeper command by ajax. */
	@RequestMapping(value = "/cluster/zk/cmd/ajax", method = RequestMethod.GET)
	public void zkCliCmdAjax(HttpServletResponse response, HttpServletRequest request) {
		String cmd = request.getParameter("cmd");
		String type = request.getParameter("type");

		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

		try {
			byte[] output = clusterService.execute(clusterAlias, cmd, type).getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
