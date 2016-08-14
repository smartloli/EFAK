package com.smartloli.kafka.eagle.controller;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartloli.kafka.eagle.service.TopicService;
import com.smartloli.kafka.eagle.utils.GzipUtils;

@Controller
public class TopicController {

	private final Logger LOG = LoggerFactory.getLogger(TopicController.class);

	@RequestMapping(value = "/topic/create", method = RequestMethod.GET)
	public ModelAndView topicCreateView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/topic/create");
		return mav;
	}

	@RequestMapping(value = "/topic/list", method = RequestMethod.GET)
	public ModelAndView topicListView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/topic/list");
		return mav;
	}

	@RequestMapping(value = "/topic/list/table/ajax", method = RequestMethod.GET)
	public void articleListAjax(HttpServletResponse response, HttpServletRequest request) {
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

		JSONArray ret = JSON.parseArray(TopicService.list());
		int offset = 0;
		JSONArray retArr = new JSONArray();
		for (Object tmp : ret) {
			JSONObject tmp2 = (JSONObject) tmp;
			if (offset < (iDisplayLength + iDisplayStart) && offset >= iDisplayStart) {
				JSONObject obj = new JSONObject();
				obj.put("id", tmp2.getInteger("id"));
				obj.put("topic", "<a href='#' target='_blank'>" + tmp2.getString("topic") + "</a>");
				obj.put("partitions", tmp2.getString("partitions"));
				obj.put("partitionNumbers", tmp2.getInteger("partitionNumbers"));
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
