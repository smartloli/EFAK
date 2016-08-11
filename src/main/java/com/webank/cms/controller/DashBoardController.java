package com.webank.cms.controller;

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
import com.alibaba.fastjson.JSONObject;
import com.webank.cms.domain.DashBoardDomain;
import com.webank.cms.service.DashBoardService;
import com.webank.cms.utils.GzipUtils;

@Controller
public class DashBoardController {

	private final Logger LOG = LoggerFactory.getLogger(DashBoardController.class);

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView indexView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/main/index");
		return mav;
	}

	@RequestMapping(value = "/dash/ajax", method = RequestMethod.GET)
	public void dashBoardAjax(HttpSession session, HttpServletResponse response, HttpServletRequest request) {
		response.setContentType("text/html;charset=utf-8");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Charset", "utf-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Content-Encoding", "gzip");

		String ip = request.getHeader("x-forwarded-for");
		LOG.info("IP:" + (ip == null ? request.getRemoteAddr() : ip));

		JSONObject obj = JSON.parseObject(session.getAttribute("user").toString());
		String username = obj.getString("username");
		DashBoardDomain dash = DashBoardService.getRelease(username);
		try {
			byte[] output = GzipUtils.compressToByte(dash.toString());
			response.setContentLength(output.length);
			OutputStream out = response.getOutputStream();
			out.write(output);

			out.flush();
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		DashBoardDomain dash = DashBoardService.getRelease("zmm");
		System.out.println(dash);
	}

}
