package com.smartloli.kafka.eagle.controller;

import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartloli.kafka.eagle.domain.AlarmDomain;
import com.smartloli.kafka.eagle.service.AlarmService;
import com.smartloli.kafka.eagle.utils.CalendarUtils;
import com.smartloli.kafka.eagle.utils.GzipUtils;

@Controller
public class AlarmController {

	private final static Logger LOG = LoggerFactory.getLogger(AlarmController.class);

	@RequestMapping(value = "/alarm/add", method = RequestMethod.GET)
	public ModelAndView addView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/alarm/add");
		return mav;
	}

	@RequestMapping(value = "/alarm/modify", method = RequestMethod.GET)
	public ModelAndView indexView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/alarm/modify");
		return mav;
	}

	@RequestMapping(value = "/alarm/create/success", method = RequestMethod.GET)
	public ModelAndView successView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/alarm/add_success");
		return mav;
	}

	@RequestMapping(value = "/alarm/create/failed", method = RequestMethod.GET)
	public ModelAndView failedView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/alarm/add_failed");
		return mav;
	}

	@RequestMapping(value = "/alarm/topic/ajax", method = RequestMethod.GET)
	public void alarmTopicAjax(HttpServletResponse response, HttpServletRequest request) {
		response.setContentType("text/html;charset=utf-8");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Charset", "utf-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Content-Encoding", "gzip");

		String ip = request.getHeader("x-forwarded-for");
		LOG.info("IP:" + (ip == null ? request.getRemoteAddr() : ip));

		try {
			byte[] output = GzipUtils.compressToByte(AlarmService.getTopics(ip));
			response.setContentLength(output == null ? "NULL".toCharArray().length : output.length);
			OutputStream out = response.getOutputStream();
			out.write(output);

			out.flush();
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@RequestMapping(value = "/alarm/add/form", method = RequestMethod.POST)
	public ModelAndView alarmAddForm(HttpSession session, HttpServletResponse response, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		String ke_group_alarms = request.getParameter("ke_group_alarms");
		String ke_topic_alarms = request.getParameter("ke_topic_alarms");
		String ke_topic_lag = request.getParameter("ke_topic_lag");
		String ke_topic_email = request.getParameter("ke_topic_email");
		JSONArray topics = JSON.parseArray(ke_topic_alarms);
		JSONArray groups = JSON.parseArray(ke_group_alarms);
		AlarmDomain alarm = new AlarmDomain();
		for (Object object : groups) {
			JSONObject obj = (JSONObject) object;
			alarm.setGroup(obj.getString("name"));
		}
		for (Object object : topics) {
			JSONObject obj = (JSONObject) object;
			alarm.setTopics(obj.getString("name"));
		}
		try {
			alarm.setLag(Long.parseLong(ke_topic_lag));
		} catch (Exception ex) {
			LOG.error("Parse long has error,msg is " + ex.getMessage());
		}
		alarm.setModifyDate(CalendarUtils.getNormalDate());
		alarm.setOwners(ke_topic_email);

		Map<String, Object> map = AlarmService.addAlarm(alarm);
		if ("success".equals(map.get("status"))) {
			session.removeAttribute("Alarm_Submit_Status");
			session.setAttribute("Alarm_Submit_Status", map.get("info"));
			mav.setViewName("redirect:/alarm/create/success");
		} else {
			session.removeAttribute("Alarm_Submit_Status");
			session.setAttribute("Alarm_Submit_Status", map.get("info"));
			mav.setViewName("redirect:/alarm/create/failed");
		}
		return mav;
	}

	@RequestMapping(value = "/alarm/list/table/ajax", method = RequestMethod.GET)
	public void alarmTopicListAjax(HttpServletResponse response, HttpServletRequest request) {
		response.setContentType("text/html;charset=utf-8");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Charset", "utf-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Content-Encoding", "gzip");

		String ip = request.getHeader("x-forwarded-for");
		LOG.info("IP:" + (ip == null ? request.getRemoteAddr() : ip));

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

		JSONArray ret = JSON.parseArray(AlarmService.list());
		int offset = 0;
		JSONArray retArr = new JSONArray();
		for (Object tmp : ret) {
			JSONObject tmp2 = (JSONObject) tmp;
			if (search.length() > 0 && search.equals(tmp2.getString("topic"))) {
				JSONObject obj = new JSONObject();
				obj.put("group", tmp2.getString("group"));
				obj.put("topic", tmp2.getString("topic"));
				obj.put("lag", tmp2.getLong("lag"));
				obj.put("owner", tmp2.getString("owner"));
				obj.put("created", tmp2.getString("created"));
				obj.put("modify", tmp2.getString("modify"));
				obj.put("operate", "<a name='remove' href='#" + tmp2.getString("group") + "/" + tmp2.getString("topic") + "' class='btn btn-danger btn-xs'>Remove</a>&nbsp");
				retArr.add(obj);
			} else if (search.length() == 0) {
				if (offset < (iDisplayLength + iDisplayStart) && offset >= iDisplayStart) {
					JSONObject obj = new JSONObject();
					obj.put("group", tmp2.getString("group"));
					obj.put("topic", tmp2.getString("topic"));
					obj.put("lag", tmp2.getLong("lag"));
					obj.put("owner", tmp2.getString("owner"));
					obj.put("created", tmp2.getString("created"));
					obj.put("modify", tmp2.getString("modify"));
					obj.put("operate", "<a name='remove' href='#" + tmp2.getString("group") + "/" + tmp2.getString("topic") + "' class='btn btn-danger btn-xs'>Remove</a>&nbsp");
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

	@RequestMapping(value = "/alarm/{group}/{topic}/del", method = RequestMethod.GET)
	public ModelAndView alarmDelete(@PathVariable("group") String group, @PathVariable("topic") String topic) {
		AlarmService.delete(group, topic);
		return new ModelAndView("redirect:/alarm/modify");
	}
}
