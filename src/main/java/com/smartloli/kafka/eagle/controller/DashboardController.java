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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartloli.kafka.eagle.service.DashboardService;
import com.smartloli.kafka.eagle.utils.GzipUtils;

@Controller
public class DashboardController {

	private final Logger LOG = LoggerFactory.getLogger(DashboardController.class);

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView indexView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/main/index");
		return mav;
	}

	@RequestMapping(value = "/dash/kafka/ajax", method = RequestMethod.GET)
	public void dashboardAjax(HttpServletResponse response, HttpServletRequest request) {
		response.setContentType("text/html;charset=utf-8");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Charset", "utf-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Content-Encoding", "gzip");

		String ip = request.getHeader("x-forwarded-for");
		LOG.info("IP:" + (ip == null ? request.getRemoteAddr() : ip));

		try {
			byte[] output = GzipUtils.compressToByte(DashboardService.getDashboard());
//			byte[] output = GzipUtils.compressToByte(kafkaCluster());
			response.setContentLength(output.length);
			OutputStream out = response.getOutputStream();
			out.write(output);

			out.flush();
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static String kafkaCluster() {
		JSONObject obj = new JSONObject();
		obj.put("name", "Kafka Brokers");
		JSONArray arr = new JSONArray();
		for (int i = 0; i < 2; i++) {
			JSONObject tmp = new JSONObject();
			tmp.put("name", "slave" + i + ":9092");
			arr.add(tmp);
		}
		obj.put("children", arr);
		return obj.toJSONString();
	}

}
