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
import com.webank.cms.domain.ArticleDomain;
import com.webank.cms.domain.TextDetailDomain;
import com.webank.cms.service.ArticleService;
import com.webank.cms.service.TextDetailService;
import com.webank.cms.utils.CalendarUtils;
import com.webank.cms.utils.GzipUtils;

@Controller
public class EditController {

	private final Logger LOG = LoggerFactory.getLogger(EditController.class);
	private final String STATUS = "未发布";

	@RequestMapping(value = "/article/edit/{articleId}", method = RequestMethod.GET)
	public ModelAndView indexView(@PathVariable("articleId") long articleId, HttpSession session) {
		ModelAndView mav = new ModelAndView();
		JSONArray arr = JSON.parseArray(TextDetailService.getArticleByID(articleId));
		if (arr.size() == 0) {
			mav.setViewName("/error/404");
		} else {
			mav.setViewName("/article/edit");
		}
		return mav;
	}

	@RequestMapping(value = "/article/e/{articleId}", method = RequestMethod.GET)
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
		obj.put("sDate", tmp.getString("sDate"));
		obj.put("eDate", tmp.getString("eDate"));
		obj.put("chanle", tmp.getString("chanle"));
		obj.put("isTop", tmp.getString("isTop"));
		obj.put("content", tmp.getString("content").replaceAll("\r|\n|\t", ""));
		obj.put("isValid", tmp.getString("isValid"));
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

	@RequestMapping(value = "/article/edit/content", method = RequestMethod.POST)
	public ModelAndView articleEditForm(HttpSession session, HttpServletResponse response, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		try {
			String author = "zmm";
			String article_title = new String(request.getParameter("article_title").getBytes("ISO-8859-1"), "UTF-8");
			String article_type = new String(request.getParameter("article_type").getBytes("ISO-8859-1"), "UTF-8");
			String article_range_date = request.getParameter("article_valid_date");
			String valid = request.getParameter("article_valid");
			String article_valid = valid == null ? "-1" : new String(valid.getBytes("ISO-8859-1"), "UTF-8");
			String article_chanle = new String(request.getParameter("article_chanle").getBytes("ISO-8859-1"), "UTF-8");
			String article_is_top = new String(request.getParameter("article_is_top").getBytes("ISO-8859-1"), "UTF-8");
			String article_content = new String(request.getParameter("article_content").getBytes("ISO-8859-1"), "UTF-8");
			long articleID = Long.parseLong(request.getParameter("article_id"));
			ArticleDomain arts = new ArticleDomain();// Article title table
			arts.setArticleID(articleID);
			arts.setAuthor(author);
			arts.setChanle(article_chanle);
			arts.setCreateDate(CalendarUtils.getCurrentDate());
			arts.setStatus(STATUS);
			arts.setTitle(article_title);
			arts.setIsTop(article_is_top);

			TextDetailDomain text = new TextDetailDomain();
			text.setArticleID(articleID);
			text.setChanle(article_chanle);
			text.setContent(article_content);
			text.setsDate(article_range_date.split(",")[0].replaceAll("-", ""));
			text.seteDate(article_range_date.split(",")[1].replaceAll("-", ""));
			text.setTitle(article_title);
			text.setIsTop(article_is_top);
			text.setType(article_type);
			text.setIsValid(article_valid);

			boolean artStatus = ArticleService.edit(arts);
			boolean textStatus = TextDetailService.edit(text);
			session.removeAttribute("Submit_Status");
			if (artStatus && textStatus) {
				session.setAttribute("Submit_Status", "修改成功");
				mav.setViewName("redirect:/article/success");
			} else {
				session.setAttribute("Submit_Status", "修改失败");
				mav.setViewName("redirect:/article/failed");
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			LOG.error("Request has error," + ex.getMessage());
			session.setAttribute("Submit_Status", "修改失败");
			mav.setViewName("redirect:/article/failed");
		}

		return mav;
	}

}
