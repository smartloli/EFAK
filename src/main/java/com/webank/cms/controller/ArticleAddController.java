package com.webank.cms.controller;

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
import com.webank.cms.domain.ArticleDomain;
import com.webank.cms.domain.TextDetailDomain;
import com.webank.cms.service.ArticleService;
import com.webank.cms.service.TextDetailService;
import com.webank.cms.utils.CalendarUtils;

@Controller
public class ArticleAddController {

	private final Logger LOG = LoggerFactory.getLogger(ArticleAddController.class);
	private final String STATUS = "未发布";

	@RequestMapping(value = "/article/add", method = RequestMethod.GET)
	public ModelAndView indexView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/article/add");
		return mav;
	}

	@RequestMapping(value = "/article/success", method = RequestMethod.GET)
	public ModelAndView successView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/article/add_success");
		return mav;
	}

	@RequestMapping(value = "/article/failed", method = RequestMethod.GET)
	public ModelAndView failedView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/article/add_failed");
		return mav;
	}

	@RequestMapping(value = "/article/add/content", method = RequestMethod.POST)
	public ModelAndView articleAddForm(HttpSession session, HttpServletResponse response, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		try {
			JSONObject obj = JSON.parseObject(session.getAttribute("user").toString());
			String author = obj.getString("username");
			String article_title = new String(request.getParameter("article_title").getBytes("ISO-8859-1"), "UTF-8");
			String article_type = new String(request.getParameter("article_type").getBytes("ISO-8859-1"), "UTF-8");
			String article_range_date = request.getParameter("article_valid_date");
			String valid = request.getParameter("article_valid");
			String article_valid = valid == null ? "-1" : new String(valid.getBytes("ISO-8859-1"), "UTF-8");
			String article_chanle = new String(request.getParameter("article_chanle").getBytes("ISO-8859-1"), "UTF-8");
			String article_is_top = new String(request.getParameter("article_is_top").getBytes("ISO-8859-1"), "UTF-8");
			String article_content = new String(request.getParameter("article_content").getBytes("ISO-8859-1"), "UTF-8");
			long articleID = CalendarUtils.getTime();
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

			ArticleService.addArticle(arts);
			TextDetailService.addTextDetail(text);

			session.removeAttribute("Submit_Status");
			session.setAttribute("Submit_Status", "添加成功");
			mav.setViewName("redirect:/article/success");
		} catch (Exception ex) {
			ex.printStackTrace();
			LOG.error("Request has error," + ex.getMessage());
			session.setAttribute("Submit_Status", "添加失败");
			mav.setViewName("redirect:/article/failed");
		}
		return mav;
	}
	
}
