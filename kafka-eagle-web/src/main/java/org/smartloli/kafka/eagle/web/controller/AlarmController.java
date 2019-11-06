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
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmClusterInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmConfigInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmConsumerInfo;
import org.smartloli.kafka.eagle.common.util.AlertUtils;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.KConstants.AlarmType;
import org.smartloli.kafka.eagle.common.util.StrUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.web.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Alarm controller to viewer data.
 * 
 * @author smartloli.
 *
 *         Created by Sep 6, 2016.
 * 
 *         Update by hexiang 20170216
 */
@Controller
public class AlarmController {

	/** Alert Service interface to operate this method. */
	@Autowired
	private AlertService alertService;

	/** Add alarmer viewer. */
	@RequiresPermissions("/alarm/add")
	@RequestMapping(value = "/alarm/add", method = RequestMethod.GET)
	public ModelAndView addView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/alarm/add");
		return mav;
	}

	/** Add alarmer config group. */
	@RequiresPermissions("/alarm/config")
	@RequestMapping(value = "/alarm/config", method = RequestMethod.GET)
	public ModelAndView configView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/alarm/config");
		return mav;
	}

	/** Add alarmer config group. */
	@RequiresPermissions("/alarm/list")
	@RequestMapping(value = "/alarm/list", method = RequestMethod.GET)
	public ModelAndView listView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/alarm/list");
		return mav;
	}

	/** Create cluster alarmer viewer. */
	@RequiresPermissions("/alarm/create")
	@RequestMapping(value = "/alarm/create", method = RequestMethod.GET)
	public ModelAndView createView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/alarm/create");
		return mav;
	}

	/** Modify alarmer viewer. */
	@RequiresPermissions("/alarm/modify")
	@RequestMapping(value = "/alarm/modify", method = RequestMethod.GET)
	public ModelAndView modifyView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/alarm/modify");
		return mav;
	}

	/** Modify alarmer viewer. */
	@RequiresPermissions("/alarm/history")
	@RequestMapping(value = "/alarm/history", method = RequestMethod.GET)
	public ModelAndView historyView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/alarm/history");
		return mav;
	}

	/** Create alarmer success viewer. */
	@RequestMapping(value = "/alarm/add/success", method = RequestMethod.GET)
	public ModelAndView addSuccessView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/alarm/add_success");
		return mav;
	}

	/** Create alarmer failed viewer. */
	@RequestMapping(value = "/alarm/add/failed", method = RequestMethod.GET)
	public ModelAndView addFailedView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/alarm/add_failed");
		return mav;
	}

	/** Config alarmer success viewer. */
	@RequestMapping(value = "/alarm/config/success", method = RequestMethod.GET)
	public ModelAndView configSuccessView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/alarm/config_success");
		return mav;
	}

	/** Config alarmer failed viewer. */
	@RequestMapping(value = "/alarm/config/failed", method = RequestMethod.GET)
	public ModelAndView configFailedView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/alarm/config_failed");
		return mav;
	}

	/** Create alarmer success viewer. */
	@RequestMapping(value = "/alarm/create/success", method = RequestMethod.GET)
	public ModelAndView createSuccessView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/alarm/create_success");
		return mav;
	}

	/** Create alarmer failed viewer. */
	@RequestMapping(value = "/alarm/create/failed", method = RequestMethod.GET)
	public ModelAndView createFailedView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/alarm/create_failed");
		return mav;
	}

	/** Get alarmer consumer group by ajax. */
	@RequestMapping(value = "/alarm/consumer/group/ajax", method = RequestMethod.GET)
	public void alarmConsumerGroupAjax(HttpServletResponse response, HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
		String search = StrUtils.convertNull(request.getParameter("name"));

		String formatter = SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage");
		try {
			JSONObject object = new JSONObject();
			object.put("items", JSON.parseArray(alertService.getAlarmConsumerGroup(clusterAlias, formatter, search)));
			byte[] output = object.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get alarmer consumer group by ajax. */
	@RequestMapping(value = "/alarm/consumer/{group}/topic/ajax", method = RequestMethod.GET)
	public void alarmConsumerTopicAjax(@PathVariable("group") String group, HttpServletResponse response, HttpServletRequest request) {

		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
		String search = StrUtils.convertNull(request.getParameter("name"));

		String formatter = SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage");
		try {
			JSONObject object = new JSONObject();
			object.put("items", JSON.parseArray(alertService.getAlarmConsumerTopic(clusterAlias, formatter, group, search)));
			byte[] output = object.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Add alarmer form. */
	@RequestMapping(value = "/alarm/add/form", method = RequestMethod.POST)
	public ModelAndView alarmAddForm(HttpSession session, HttpServletResponse response, HttpServletRequest request) {
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

		ModelAndView mav = new ModelAndView();
		String group = request.getParameter("ke_alarm_consumer_group");
		String topic = request.getParameter("ke_alarm_consumer_topic");
		String lag = request.getParameter("ke_topic_lag");
		String alarmLevel = request.getParameter("ke_alarm_cluster_level");
		String alarmMaxTimes = request.getParameter("ke_alarm_cluster_maxtimes");
		String alarmGroup = request.getParameter("ke_alarm_cluster_group");
		AlarmConsumerInfo alarmConsumer = new AlarmConsumerInfo();
		alarmConsumer.setGroup(group);
		alarmConsumer.setAlarmGroup(alarmGroup);
		alarmConsumer.setAlarmLevel(alarmLevel);
		alarmConsumer.setAlarmMaxTimes(Integer.parseInt(alarmMaxTimes));
		alarmConsumer.setAlarmTimes(0);
		alarmConsumer.setCluster(clusterAlias);
		alarmConsumer.setCreated(CalendarUtils.getDate());
		alarmConsumer.setIsEnable("Y");
		alarmConsumer.setIsNormal("Y");
		alarmConsumer.setLag(Long.parseLong(lag));
		alarmConsumer.setModify(CalendarUtils.getDate());
		alarmConsumer.setTopic(topic);

		Map<String, Object> map = new HashMap<>();
		map.put("cluster", clusterAlias);
		map.put("group", group);
		map.put("topic", topic);
		int findCode = alertService.isExistAlertByCGT(map);

		if (findCode > 0) {
			session.removeAttribute("Alarm_Submit_Status");
			session.setAttribute("Alarm_Submit_Status", "Insert failed,msg is alarm group[" + group + "] and topic[" + topic + "] has exist.");
			mav.setViewName("redirect:/alarm/add/failed");
		} else {
			int code = alertService.insertAlarmConsumer(alarmConsumer);
			if (code > 0) {
				session.removeAttribute("Alarm_Submit_Status");
				session.setAttribute("Alarm_Submit_Status", "Insert success.");
				mav.setViewName("redirect:/alarm/add/success");
			} else {
				session.removeAttribute("Alarm_Submit_Status");
				session.setAttribute("Alarm_Submit_Status", "Insert failed.");
				mav.setViewName("redirect:/alarm/add/failed");
			}
		}

		return mav;
	}

	/** Get alarmer datasets by ajax. */
	@RequestMapping(value = "/alarm/list/table/ajax", method = RequestMethod.GET)
	public void alarmConsumerListAjax(HttpServletResponse response, HttpServletRequest request) {
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

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("cluster", clusterAlias);
		map.put("search", search);
		map.put("start", iDisplayStart);
		map.put("size", iDisplayLength);

		List<AlarmConsumerInfo> alarmConsumers = alertService.getAlarmConsumerAppList(map);
		JSONArray aaDatas = new JSONArray();
		for (AlarmConsumerInfo alertConsumer : alarmConsumers) {
			JSONObject obj = new JSONObject();
			int id = alertConsumer.getId();
			String group = alertConsumer.getGroup();
			String topic = alertConsumer.getTopic();
			String alarmGroup = alertConsumer.getAlarmGroup();
			obj.put("id", id);
			obj.put("group", "<a href='#" + id + "/cgroup' name='ke_alarm_consumer_detail'>" + (group.length() > 8 ? group.substring(0, 8) + "..." : group) + "</a>");
			obj.put("topic", "<a href='#" + id + "/topic' name='ke_alarm_consumer_detail'>" + (topic.length() > 8 ? topic.substring(0, 8) + "..." : topic) + "</a>");
			obj.put("lag", alertConsumer.getLag());
			obj.put("alarmGroup", "<a href='#" + id + "/agroup' name='ke_alarm_consumer_detail'>" + (alarmGroup.length() > 8 ? alarmGroup.substring(0, 8) + "..." : alarmGroup) + "</a>");
			obj.put("alarmTimes", alertConsumer.getAlarmTimes());
			obj.put("alarmMaxTimes", alertConsumer.getAlarmMaxTimes());
			if (alertConsumer.getAlarmLevel().equals("P0")) {
				obj.put("alarmLevel", "<a class='btn btn-danger btn-xs'>" + alertConsumer.getAlarmLevel() + "</a>");
			} else if (alertConsumer.getAlarmLevel().equals("P1")) {
				obj.put("alarmLevel", "<a class='btn btn-warning btn-xs'>" + alertConsumer.getAlarmLevel() + "</a>");
			} else if (alertConsumer.getAlarmLevel().equals("P2")) {
				obj.put("alarmLevel", "<a class='btn btn-info btn-xs'>" + alertConsumer.getAlarmLevel() + "</a>");
			} else {
				obj.put("alarmLevel", "<a class='btn btn-primary btn-xs'>" + alertConsumer.getAlarmLevel() + "</a>");
			}
			if (alertConsumer.getIsNormal().equals("Y")) {
				obj.put("alarmIsNormal", "<a class='btn btn-success btn-xs'>Y</a>");
			} else {
				obj.put("alarmIsNormal", "<a class='btn btn-danger btn-xs'>N</a>");
			}
			if (alertConsumer.getIsEnable().equals("Y")) {
				obj.put("alarmIsEnable", "<input type='checkbox' name='is_enable_chk' id='alarm_config_is_enable_" + id + "' checked class='chooseBtn' /><label id='is_enable_label_id' val=" + id
						+ " name='is_enable_label' for='alarm_config_is_enable_" + id + "' class='choose-label'></label>");
			} else {
				obj.put("alarmIsEnable", "<input type='checkbox' name='is_enable_chk' id='alarm_config_is_enable_" + id + "' class='chooseBtn' /><label id='is_enable_label_id' val=" + id
						+ " name='is_enable_label' for='alarm_config_is_enable_" + id + "' class='choose-label'></label>");
			}
			obj.put("created", alertConsumer.getCreated());
			obj.put("modify", alertConsumer.getModify());
			obj.put("operate",
					"<div class='btn-group'><button class='btn btn-primary btn-xs dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Action <span class='caret'></span></button><ul class='dropdown-menu dropdown-menu-right'><li><a name='alarm_consumer_modify' href='#"
							+ id + "/modify'><i class='fa fa-fw fa-edit'></i>Modify</a></li><li><a href='#" + id + "' name='alarm_consumer_remove'><i class='fa fa-fw fa-trash-o'></i>Delete</a></li></ul></div>");
			aaDatas.add(obj);
		}

		JSONObject target = new JSONObject();
		target.put("sEcho", sEcho);
		target.put("iTotalRecords", alertService.alertConsumerAppCount(map));
		target.put("iTotalDisplayRecords", alertService.alertConsumerAppCount(map));
		target.put("aaData", aaDatas);
		try {
			byte[] output = target.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Switch cluster alert info. */
	@RequestMapping(value = "/alarm/list/modify/switch/{id}/ajax", method = RequestMethod.GET)
	public void modifyConsumerAlertSwitchByIdAjax(@PathVariable("id") int id, HttpServletResponse response, HttpServletRequest request) {
		try {
			AlarmConsumerInfo alarmConsumer = alertService.findAlarmConsumerAlertById(id);
			if (alarmConsumer.getIsEnable().equals("Y")) {
				alarmConsumer.setIsEnable("N");
			} else {
				alarmConsumer.setIsEnable("Y");
			}
			alarmConsumer.setModify(CalendarUtils.getDate());
			int code = alertService.modifyConsumerAlertSwitchById(alarmConsumer);
			JSONObject object = new JSONObject();
			if (code > 0) {
				object.put("code", code);
				object.put("status", "Success");
			} else {
				object.put("code", code);
				object.put("status", "Failed");
			}
			byte[] output = object.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Get alarm consumer detail, such consumer group(cgroup) or topic, and
	 * alarm group(agroup).
	 */
	@RequestMapping(value = "/alarm/consumer/detail/{type}/{id}/ajax", method = RequestMethod.GET)
	public void getAlarmConsumerDetailByIdAjax(@PathVariable("id") int id, @PathVariable("type") String type, HttpServletResponse response, HttpServletRequest request) {
		try {
			JSONObject object = new JSONObject();
			if ("cgroup".equals(type)) {
				object.put("result", alertService.findAlarmConsumerAlertById(id).getGroup());
			} else if ("topic".equals(type)) {
				object.put("result", alertService.findAlarmConsumerAlertById(id).getTopic());
			} else if ("agroup".equals(type)) {
				object.put("result", alertService.findAlarmConsumerAlertById(id).getAlarmGroup());
			}
			byte[] output = object.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Delete alarm consumer application. */
	@RequestMapping(value = "/alarm/consumer/{id}/del", method = RequestMethod.GET)
	public ModelAndView alarmConsumerDelete(@PathVariable("id") int id, HttpServletRequest request) {
		int code = alertService.deleteAlarmConsumerById(id);
		if (code > 0) {
			return new ModelAndView("redirect:/alarm/modify");
		} else {
			return new ModelAndView("redirect:/errors/500");
		}
	}

	/** Get alert info. */
	@RequestMapping(value = "/alarm/consumer/modify/{id}/ajax", method = RequestMethod.GET)
	public void findAlarmConsumerByIdAjax(@PathVariable("id") int id, HttpServletResponse response, HttpServletRequest request) {
		try {
			byte[] output = alertService.findAlarmConsumerAlertById(id).toString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Modify consumer topic alert info. */
	@RequestMapping(value = "/alarm/consumer/modify/", method = RequestMethod.POST)
	public String modifyAlarmConsumerInfo(HttpSession session, HttpServletRequest request) {
		String id = request.getParameter("ke_consumer_id_lag");
		String lag = request.getParameter("ke_consumer_name_lag");
		String agroup = request.getParameter("ke_alarm_consumer_group");
		String maxtimes = request.getParameter("ke_alarm_consumer_maxtimes");
		String level = request.getParameter("ke_alarm_consumer_level");

		AlarmConsumerInfo alertConsumer = new AlarmConsumerInfo();
		// JavaScript has already judged.
		alertConsumer.setId(Integer.parseInt(id));
		alertConsumer.setLag(Long.parseLong(lag));
		alertConsumer.setAlarmGroup(agroup);
		alertConsumer.setAlarmMaxTimes(Integer.parseInt(maxtimes));
		alertConsumer.setAlarmLevel(level);
		alertConsumer.setModify(CalendarUtils.getDate());

		if (alertService.modifyAlarmConsumerById(alertConsumer) > 0) {
			return "redirect:/alarm/modify";
		} else {
			return "redirect:/errors/500";
		}
	}

	/** Create alarmer form. */
	@RequestMapping(value = "/alarm/create/form", method = RequestMethod.POST)
	public ModelAndView alarmCreateForm(HttpSession session, HttpServletResponse response, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		String type = request.getParameter("ke_alarm_cluster_type");
		String servers = request.getParameter("ke_server_alarm");
		String level = request.getParameter("ke_alarm_cluster_level");
		String alarmGroup = request.getParameter("ke_alarm_cluster_group");
		int maxTimes = 10;
		try {
			maxTimes = Integer.parseInt(request.getParameter("ke_alarm_cluster_maxtimes"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
		AlarmClusterInfo alarmClusterInfo = new AlarmClusterInfo();
		alarmClusterInfo.setAlarmGroup(alarmGroup);
		alarmClusterInfo.setAlarmLevel(level);
		alarmClusterInfo.setAlarmMaxTimes(maxTimes);
		alarmClusterInfo.setAlarmTimes(0);
		alarmClusterInfo.setCluster(clusterAlias);
		alarmClusterInfo.setCreated(CalendarUtils.getDate());
		alarmClusterInfo.setIsEnable("Y");
		alarmClusterInfo.setIsNormal("Y");
		alarmClusterInfo.setModify(CalendarUtils.getDate());
		alarmClusterInfo.setServer(servers);
		alarmClusterInfo.setType(type);

		int code = alertService.create(alarmClusterInfo);
		if (code > 0) {
			session.removeAttribute("Alarm_Submit_Status");
			session.setAttribute("Alarm_Submit_Status", "Insert success.");
			mav.setViewName("redirect:/alarm/create/success");
		} else {
			session.removeAttribute("Alarm_Submit_Status");
			session.setAttribute("Alarm_Submit_Status", "Insert failed.");
			mav.setViewName("redirect:/alarm/create/failed");
		}

		return mav;
	}

	/** Get alarmer cluster history datasets by ajax. */
	@RequestMapping(value = "/alarm/history/table/ajax", method = RequestMethod.GET)
	public void alarmClusterHistoryAjax(HttpServletResponse response, HttpServletRequest request) {
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

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("cluster", clusterAlias);
		map.put("search", search);
		map.put("start", iDisplayStart);
		map.put("size", iDisplayLength);

		List<AlarmClusterInfo> alarmClusters = alertService.getAlarmClusterList(map);
		JSONArray aaDatas = new JSONArray();
		for (AlarmClusterInfo clustersInfo : alarmClusters) {
			JSONObject obj = new JSONObject();
			int id = clustersInfo.getId();
			String server = StrUtils.convertNull(clustersInfo.getServer());
			String group = StrUtils.convertNull(clustersInfo.getAlarmGroup());
			obj.put("id", id);
			obj.put("type", clustersInfo.getType());
			obj.put("server", "<a href='#" + id + "/server' name='ke_alarm_cluster_detail'>" + (server.length() > 8 ? server.substring(0, 8) + "..." : server) + "</a>");
			obj.put("alarmGroup", "<a href='#" + id + "/group' name='ke_alarm_cluster_detail'>" + (group.length() > 8 ? group.substring(0, 8) + "..." : group) + "</a>");
			obj.put("alarmTimes", clustersInfo.getAlarmTimes());
			obj.put("alarmMaxTimes", clustersInfo.getAlarmMaxTimes());
			if (clustersInfo.getAlarmLevel().equals("P0")) {
				obj.put("alarmLevel", "<a class='btn btn-danger btn-xs'>" + clustersInfo.getAlarmLevel() + "</a>");
			} else if (clustersInfo.getAlarmLevel().equals("P1")) {
				obj.put("alarmLevel", "<a class='btn btn-warning btn-xs'>" + clustersInfo.getAlarmLevel() + "</a>");
			} else if (clustersInfo.getAlarmLevel().equals("P2")) {
				obj.put("alarmLevel", "<a class='btn btn-info btn-xs'>" + clustersInfo.getAlarmLevel() + "</a>");
			} else {
				obj.put("alarmLevel", "<a class='btn btn-primary btn-xs'>" + clustersInfo.getAlarmLevel() + "</a>");
			}
			if (clustersInfo.getIsNormal().equals("Y")) {
				obj.put("alarmIsNormal", "<a class='btn btn-success btn-xs'>Y</a>");
			} else {
				obj.put("alarmIsNormal", "<a class='btn btn-danger btn-xs'>N</a>");
			}
			if (clustersInfo.getIsEnable().equals("Y")) {
				obj.put("alarmIsEnable", "<input type='checkbox' name='is_enable_chk' id='alarm_config_is_enable_" + id + "' checked class='chooseBtn' /><label id='is_enable_label_id' val=" + id
						+ " name='is_enable_label' for='alarm_config_is_enable_" + id + "' class='choose-label'></label>");
			} else {
				obj.put("alarmIsEnable", "<input type='checkbox' name='is_enable_chk' id='alarm_config_is_enable_" + id + "' class='chooseBtn' /><label id='is_enable_label_id' val=" + id
						+ " name='is_enable_label' for='alarm_config_is_enable_" + id + "' class='choose-label'></label>");
			}
			obj.put("created", clustersInfo.getCreated());
			obj.put("modify", clustersInfo.getModify());
			obj.put("operate",
					"<div class='btn-group'><button class='btn btn-primary btn-xs dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Action <span class='caret'></span></button><ul class='dropdown-menu dropdown-menu-right'><li><a name='alarm_cluster_modify' href='#"
							+ id + "/modify'><i class='fa fa-fw fa-edit'></i>Modify</a></li><li><a href='#" + id + "' name='alarm_cluster_remove'><i class='fa fa-fw fa-trash-o'></i>Delete</a></li></ul></div>");
			aaDatas.add(obj);
		}

		JSONObject target = new JSONObject();
		target.put("sEcho", sEcho);
		target.put("iTotalRecords", alertService.getAlarmClusterCount(map));
		target.put("iTotalDisplayRecords", alertService.getAlarmClusterCount(map));
		target.put("aaData", aaDatas);
		try {
			byte[] output = target.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get alarm cluster detail, such server or alarm group. */
	@RequestMapping(value = "/alarm/cluster/detail/{type}/{id}/ajax", method = RequestMethod.GET)
	public void getAlarmClusterDetailByIdAjax(@PathVariable("id") int id, @PathVariable("type") String type, HttpServletResponse response, HttpServletRequest request) {
		try {
			JSONObject object = new JSONObject();
			if ("server".equals(type)) {
				object.put("result", alertService.findAlarmClusterAlertById(id).getServer());
			} else if ("group".equals(type)) {
				object.put("result", alertService.findAlarmClusterAlertById(id).getAlarmGroup());
			}
			byte[] output = object.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Delete history alarmer. */
	@RequestMapping(value = "/alarm/history/{id}/del", method = RequestMethod.GET)
	public ModelAndView alarmHistoryClusterDelete(@PathVariable("id") int id, HttpServletRequest request) {
		int code = alertService.deleteAlarmClusterAlertById(id);
		if (code > 0) {
			return new ModelAndView("redirect:/alarm/history");
		} else {
			return new ModelAndView("redirect:/errors/500");
		}
	}

	/** Get alert info. */
	@RequestMapping(value = "/alarm/history/modify/{id}/ajax", method = RequestMethod.GET)
	public void findClusterAlertByIdAjax(@PathVariable("id") int id, HttpServletResponse response, HttpServletRequest request) {
		try {
			byte[] output = alertService.findAlarmClusterAlertById(id).toString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Switch cluster alert info. */
	@RequestMapping(value = "/alarm/history/modify/switch/{id}/ajax", method = RequestMethod.GET)
	public void modifyClusterAlertSwitchByIdAjax(@PathVariable("id") int id, HttpServletResponse response, HttpServletRequest request) {
		try {
			AlarmClusterInfo alarmCluster = alertService.findAlarmClusterAlertById(id);
			if (alarmCluster.getIsEnable().equals("Y")) {
				alarmCluster.setIsEnable("N");
			} else {
				alarmCluster.setIsEnable("Y");
			}
			alarmCluster.setModify(CalendarUtils.getDate());
			int code = alertService.modifyClusterAlertSwitchById(alarmCluster);
			JSONObject object = new JSONObject();
			if (code > 0) {
				object.put("code", code);
				object.put("status", "Success");
			} else {
				object.put("code", code);
				object.put("status", "Failed");
			}
			byte[] output = object.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Modify consumer topic alert info. */
	@RequestMapping(value = "/alarm/history/modify/", method = RequestMethod.POST)
	public String modifyClusterAlertInfo(HttpSession session, HttpServletRequest request) {
		String id = request.getParameter("ke_alarm_cluster_id_server");
		String server = request.getParameter("ke_alarm_cluster_name_server");
		String group = request.getParameter("ke_alarm_cluster_group");
		String maxtimes = request.getParameter("ke_alarm_cluster_maxtimes");
		String level = request.getParameter("ke_alarm_cluster_level");

		AlarmClusterInfo cluster = new AlarmClusterInfo();
		cluster.setId(Integer.parseInt(id));
		cluster.setServer(server);
		cluster.setAlarmGroup(group);
		cluster.setAlarmMaxTimes(Integer.parseInt(maxtimes));
		cluster.setAlarmLevel(level);
		cluster.setModify(CalendarUtils.getDate());
		if (alertService.modifyClusterAlertById(cluster) > 0) {
			return "redirect:/alarm/history";
		} else {
			return "redirect:/errors/500";
		}
	}

	/** Get alarm type list, such as email, dingding, wechat and so on. */
	@RequestMapping(value = "/alarm/type/list/ajax", method = RequestMethod.GET)
	public void alarmTypeListAjax(HttpServletResponse response, HttpServletRequest request) {
		try {
			JSONObject object = new JSONObject();
			object.put("items", JSON.parseArray(alertService.getAlertTypeList()));
			byte[] output = object.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get alarm cluster type list, such as kafka, zookeeper and so on. */
	@RequestMapping(value = "/alarm/cluster/{type}/list/ajax", method = RequestMethod.GET)
	public void alarmClusterTypeListAjax(@PathVariable("type") String type, HttpServletResponse response, HttpServletRequest request) {
		try {
			JSONObject object = new JSONObject();
			String search = "";
			Map<String, Object> map = new HashMap<>();
			if ("group".equals(type)) {
				HttpSession session = request.getSession();
				String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
				search = StrUtils.convertNull(request.getParameter("name"));
				map.put("search", "%" + search + "%");
				map.put("start", 0);
				map.put("size", 10);
				map.put("cluster", clusterAlias);
			} else {
				search = StrUtils.convertNull(request.getParameter("name"));
				map.put("search", search);
			}
			object.put("items", JSON.parseArray(alertService.getAlertClusterTypeList(type, map)));
			byte[] output = object.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Check alarm group name. */
	@RequestMapping(value = "/alarm/check/{group}/ajax", method = RequestMethod.GET)
	public void alarmGroupCheckAjax(@PathVariable("group") String group, HttpServletResponse response, HttpServletRequest request) {
		try {
			HttpSession session = request.getSession();
			String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
			Map<String, Object> params = new HashMap<>();
			params.put("cluster", clusterAlias);
			params.put("alarmGroup", group);
			JSONObject object = new JSONObject();
			object.put("result", alertService.findAlarmConfigByGroupName(params));
			byte[] output = object.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Add alarm config . */
	@RequestMapping(value = "/alarm/config/storage/form", method = RequestMethod.POST)
	public ModelAndView alarmAddConfigForm(HttpSession session, HttpServletResponse response, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		String group = request.getParameter("ke_alarm_group_name");
		String type = request.getParameter("ke_alarm_type");
		String url = request.getParameter("ke_alarm_url");
		String http = request.getParameter("ke_alarm_http");
		String address = request.getParameter("ke_alarm_address");
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

		AlarmConfigInfo alarmConfig = new AlarmConfigInfo();
		alarmConfig.setCluster(clusterAlias);
		alarmConfig.setAlarmGroup(group);
		alarmConfig.setAlarmType(type);
		alarmConfig.setAlarmUrl(url);
		alarmConfig.setHttpMethod(http);
		alarmConfig.setAlarmAddress(address);
		alarmConfig.setCreated(CalendarUtils.getDate());
		alarmConfig.setModify(CalendarUtils.getDate());

		Map<String, Object> params = new HashMap<>();
		params.put("cluster", clusterAlias);
		params.put("alarmGroup", group);
		boolean findCode = alertService.findAlarmConfigByGroupName(params);

		if (findCode) {
			session.removeAttribute("Alarm_Config_Status");
			session.setAttribute("Alarm_Config_Status", "Insert failed alarm group[" + alarmConfig.getAlarmGroup() + "] has exist.");
			mav.setViewName("redirect:/alarm/config/failed");
		} else {
			int resultCode = alertService.insertOrUpdateAlarmConfig(alarmConfig);
			if (resultCode > 0) {
				session.removeAttribute("Alarm_Config_Status");
				session.setAttribute("Alarm_Config_Status", "Insert success.");
				mav.setViewName("redirect:/alarm/config/success");
			} else {
				session.removeAttribute("Alarm_Config_Status");
				session.setAttribute("Alarm_Config_Status", "Insert failed.");
				mav.setViewName("redirect:/alarm/config/failed");
			}
		}

		return mav;
	}

	/** Get alarm config list. */
	@RequestMapping(value = "/alarm/config/table/ajax", method = RequestMethod.GET)
	public void getAlarmConfigTableAjax(HttpServletResponse response, HttpServletRequest request) {
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

		Map<String, Object> map = new HashMap<>();
		map.put("search", "%" + search + "%");
		map.put("start", iDisplayStart);
		map.put("size", iDisplayLength);
		map.put("cluster", clusterAlias);

		JSONArray configList = JSON.parseArray(alertService.getAlarmConfigList(map).toString());
		JSONArray aaDatas = new JSONArray();

		for (Object object : configList) {
			JSONObject config = (JSONObject) object;
			JSONObject obj = new JSONObject();
			String alarmGroup = StrUtils.convertNull(config.getString("alarmGroup"));
			String url = StrUtils.convertNull(config.getString("alarmUrl"));
			String address = StrUtils.convertNull(config.getString("alarmAddress"));
			obj.put("cluster", config.getString("cluster"));
			obj.put("alarmGroup", alarmGroup.length() > 16 ? alarmGroup.substring(0, 16) + "..." : alarmGroup);
			obj.put("alarmType", config.getString("alarmType"));
			obj.put("alarmUrl", "<a name='ke_alarm_config_detail' href='#" + alarmGroup + "/url'>" + (url.length() > 16 ? url.substring(0, 16) + "..." : url) + "</a>");
			obj.put("httpMethod", config.getString("httpMethod"));
			obj.put("alarmAddress", "<a name='ke_alarm_config_detail' href='#" + alarmGroup + "/address'>" + (address.length() > 16 ? address.substring(0, 16) + "..." : address) + "</a>");
			obj.put("created", config.getString("created"));
			obj.put("modify", config.getString("modify"));
			obj.put("operate",
					"<div class='btn-group'><button class='btn btn-primary btn-xs dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Action <span class='caret'></span></button><ul class='dropdown-menu dropdown-menu-right'><li><a name='ke_alarm_config_modify' href='#"
							+ alarmGroup + "/modify'><i class='fa fa-fw fa-edit'></i>Modify</a></li><li><a href='#" + alarmGroup + "' name='ke_alarm_config_remove'><i class='fa fa-fw fa-trash-o'></i>Delete</a></li></ul></div>");
			aaDatas.add(obj);
		}

		int count = alertService.alarmConfigCount(map); // only need cluster
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

	/** Delete alarm config. */
	@RequestMapping(value = "/alarm/config/{group}/del", method = RequestMethod.GET)
	public ModelAndView alarmConfigDelete(@PathVariable("group") String group, HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
		Map<String, Object> map = new HashMap<>();
		map.put("cluster", clusterAlias);
		map.put("alarmGroup", group);

		int code = alertService.deleteAlertByGroupName(map);
		if (code > 0) {
			return new ModelAndView("redirect:/alarm/list");
		} else {
			return new ModelAndView("redirect:/errors/500");
		}
	}

	/** Get alarm config by group name. */
	@RequestMapping(value = "/alarm/config/get/{type}/{group}/ajax", method = RequestMethod.GET)
	public void getAlarmConfigDetailByGroupAjax(@PathVariable("type") String type, @PathVariable("group") String group, HttpServletResponse response, HttpServletRequest request) {
		try {
			HttpSession session = request.getSession();
			String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
			Map<String, Object> params = new HashMap<>();
			params.put("cluster", clusterAlias);
			params.put("alarmGroup", group);
			JSONObject object = new JSONObject();
			if ("url".equals(type)) {
				object.put("result", alertService.getAlarmConfigByGroupName(params).getAlarmUrl());
			} else if ("address".equals(type)) {
				object.put("result", alertService.getAlarmConfigByGroupName(params).getAlarmAddress());
			} else if ("modify".equals(type)) {
				object.put("url", alertService.getAlarmConfigByGroupName(params).getAlarmUrl());
				object.put("address", alertService.getAlarmConfigByGroupName(params).getAlarmAddress());
			}
			byte[] output = object.toJSONString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Modify alarm config. */
	@RequestMapping(value = "/alarm/config/modify/form/", method = RequestMethod.POST)
	public ModelAndView alarmModifyConfigForm(HttpSession session, HttpServletResponse response, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		String group = request.getParameter("ke_alarm_group_m_name");
		String url = request.getParameter("ke_alarm_config_m_url");
		String address = request.getParameter("ke_alarm_config_m_address");
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

		Map<String, Object> params = new HashMap<>();
		params.put("cluster", clusterAlias);
		params.put("alarmGroup", group);

		AlarmConfigInfo alarmConfig = alertService.getAlarmConfigByGroupName(params);

		alarmConfig.setAlarmUrl(url);
		alarmConfig.setAlarmAddress(address);
		alarmConfig.setModify(CalendarUtils.getDate());

		int resultCode = alertService.insertOrUpdateAlarmConfig(alarmConfig);
		if (resultCode > 0) {
			mav.setViewName("redirect:/alarm/list");
		} else {
			mav.setViewName("redirect:/errors/500");
		}
		return mav;
	}

	/** Send test message by alarm config . */
	@RequestMapping(value = "/alarm/config/test/send/ajax", method = RequestMethod.GET)
	public void sendTestMsgAlarmConfig(HttpServletResponse response, HttpServletRequest request) {
		try {
			String type = request.getParameter("type");
			String url = request.getParameter("url");
			String http = request.getParameter("http");
			String msg = request.getParameter("msg");
			String result = "";
			if (AlarmType.EMAIL.equals(type)) {
				
			} else if (AlarmType.DingDing.equals(type)) {
				result = AlertUtils.sendTestMsgByDingDing(url, msg);
			} else if (AlarmType.WebHook.equals(type)) {
				if (AlarmType.HTTP_GET.equals(http)) {
					// send get request
				} else if (AlarmType.HTTP_POST.equals(http)) {
					// send post request
				}
			} else if (AlarmType.WeChat.equals(type)) {
				result = AlertUtils.sendTestMsgByWeChat(url, msg);
			}
			byte[] output = result.getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
