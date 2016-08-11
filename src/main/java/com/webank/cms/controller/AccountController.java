package com.webank.cms.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.webank.cms.domain.LoginDomain;
import com.webank.cms.domain.UserDomain;
import com.webank.cms.service.UserService;
import com.webank.cms.utils.MD5Utils;

@Controller
public class AccountController {

	private final Logger LOG = LoggerFactory.getLogger(AccountController.class);

	@RequestMapping(value = "/signin", method = RequestMethod.GET)
	public ModelAndView signinView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/account/signin");
		return mav;
	}

	@RequestMapping(value = "/signout", method = RequestMethod.GET)
	public ModelAndView signoutView(HttpSession session) {
		session.removeAttribute("user");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/account/signin");
		return mav;
	}

	@ResponseBody
	@RequestMapping(value = "/signin/user", method = RequestMethod.POST)
	public Object signinForm(HttpSession session, HttpServletResponse response, HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			String ref_url = request.getParameter("ref_url").replaceAll("#", "");
			UserDomain user = UserService.get(username, MD5Utils.md5(password));
			LoginDomain lg = new LoginDomain();
			if (user == null) {
				lg.setError("user_or_pwd_error");
				lg.setRefUrl(ref_url);
			} else {
				session.removeAttribute("user");
				session.setAttribute("user", user);
				lg.setError("without");
				lg.setRefUrl(ref_url);
			}
			map.put("/login/status/", lg);
		} catch (Exception ex) {
			ex.printStackTrace();
			LOG.error("Request has error," + ex.getMessage());
			LoginDomain lg = new LoginDomain();
			lg.setError("user_or_pwd_error");
			map.put("/login/status/", lg);
		}
		return new Gson().toJson(map);
	}
}
