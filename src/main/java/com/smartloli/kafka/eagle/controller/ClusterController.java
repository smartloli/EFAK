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
import com.smartloli.kafka.eagle.service.ClusterService;
import com.smartloli.kafka.eagle.utils.GzipUtils;

@Controller
public class ClusterController {

	private final Logger LOG = LoggerFactory.getLogger(ClusterController.class);

	@RequestMapping(value = "/cluster", method = RequestMethod.GET)
	public ModelAndView clusterView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/cluster/cluster");
		return mav;
	}

	@RequestMapping(value = "/cluster/info/ajax", method = RequestMethod.GET)
	public void clusterAjax(HttpServletResponse response, HttpServletRequest request) {
		response.setContentType("text/html;charset=utf-8");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Charset", "utf-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Content-Encoding", "gzip");

		String ip = request.getHeader("x-forwarded-for");
		LOG.info("IP:" + (ip == null ? request.getRemoteAddr() : ip));

		try {
			byte[] output = GzipUtils.compressToByte(ClusterService.getCluster());
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
		JSONArray zk = new JSONArray();
		for (int i = 0; i < 3; i++) {
			JSONObject tmp = new JSONObject();
			tmp.put("id", i + 1);
			tmp.put("ip", "zk" + i);
			tmp.put("port", "2181");
			tmp.put("version", "3.4.6");
			zk.add(tmp);
		}
		JSONObject obj = new JSONObject();
		obj.put("zk", zk);
		JSONArray kafka = new JSONArray();
		for (int i = 0; i < 3; i++) {
			JSONObject tmp = new JSONObject();
			tmp.put("id", i + 1);
			tmp.put("ip", "slave" + i);
			tmp.put("port", "9092");
			tmp.put("version", "0.8.2.2");
			kafka.add(tmp);
		}
		obj.put("kafka", kafka);
		return obj.toJSONString();
	}

}
