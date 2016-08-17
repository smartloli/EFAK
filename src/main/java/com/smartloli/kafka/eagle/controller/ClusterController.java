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

}
