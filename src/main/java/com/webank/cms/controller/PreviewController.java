package com.webank.cms.controller;

import java.io.OutputStream;

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
import com.webank.cms.service.TextDetailService;
import com.webank.cms.utils.GzipUtils;

@Controller
public class PreviewController {

	private final Logger LOG = LoggerFactory.getLogger(PreviewController.class);

	@RequestMapping(value = "/article/preview/{articleId}", method = RequestMethod.GET)
	public ModelAndView indexView(@PathVariable("articleId") long articleId, HttpSession session) {
		ModelAndView mav = new ModelAndView();
		JSONArray arr = JSON.parseArray(TextDetailService.getArticleByID(articleId));
		if (arr.size() == 0) {
			mav.setViewName("/error/404");
		} else {
			mav.setViewName("/article/preview");
		}
		return mav;
	}

	@RequestMapping(value = "/article/p/{articleId}", method = RequestMethod.GET)
	public void articleContextAjax(@PathVariable("articleId") long articleId, HttpServletResponse response, HttpServletRequest request) {
		response.setContentType("text/html;charset=utf-8");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Charset", "utf-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Content-Encoding", "gzip");

		String ip = request.getHeader("x-forwarded-for");
		LOG.info("IP:" + (ip == null ? request.getRemoteAddr() : ip));
		JSONArray arr = JSON.parseArray(TextDetailService.getArticleByID(articleId));
		JSONObject tmp = (JSONObject) arr.get(0);
		JSONObject obj = new JSONObject();
		obj.put("title", tmp.getString("title"));
		obj.put("type", tmp.getString("type"));
		obj.put("chanle", tmp.getString("chanle"));
		obj.put("content", tmp.getString("content").replaceAll("\r|\n|\t", ""));
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
