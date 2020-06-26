/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.web.controller;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.web.pojo.Signiner;
import org.smartloli.kafka.eagle.web.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

/**
 * Control user login, logout, reset password and other operations.
 * 
 * @author smartloli.
 *
 *         Created by May 26, 2017.
 */
@Controller
@RequestMapping("/account")
public class AccountController {

	@Autowired
	private AccountService accountService;

	/** Signin viewer. */
	@RequestMapping(value = "/signin", method = RequestMethod.GET)
	public ModelAndView signinView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/account/signin");
		return mav;
	}

	/** Login action and checked username&password. */
	@RequestMapping(value = "/signin/action/", method = RequestMethod.POST)
	public String login(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String refUrl = request.getParameter("ref_url");
		UsernamePasswordToken token = new UsernamePasswordToken(username, password);
		token.setRememberMe(true);
		Subject subject = SecurityUtils.getSubject();
		if (subject.isAuthenticated()) {
			setKafkaAlias(subject);
			if ("zh_CN".equals(SystemConfigUtils.getProperty("kafka.eagle.i18n.language"))) {
				Locale locale = new Locale("zh", "CN");
				request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, locale);
			} else if ("en_US".equals(SystemConfigUtils.getProperty("kafka.eagle.i18n.language"))) {
				Locale locale = new Locale("en", "US");
				request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, locale);
			} else {
				request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, LocaleContextHolder.getLocale());
			}
			return "redirect:" + refUrl;
		} else {
			subject.getSession().setAttribute(KConstants.Login.ERROR_LOGIN, "<div class='alert alert-danger'>Account or password is error .</div>");
		}
		token.clear();
		return "/account/signin";
	}

	/** If validation passes, set the kafka default cluster. */
	private void setKafkaAlias(Subject subject) {
		Object object = subject.getSession().getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS);
		if (object == null) {
			String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
			String defaultClusterAlias = clusterAliass[0];
			subject.getSession().setAttribute(KConstants.SessionAlias.CLUSTER_ALIAS, defaultClusterAlias);
			String dropList = "<div class='dropdown-menu dropdown-menu-right' aria-labelledby='clusterDropdown'>";
			int i = 0;
			for (String clusterAlias : clusterAliass) {
				if (!clusterAlias.equals(defaultClusterAlias) && i < KConstants.SessionAlias.CLUSTER_ALIAS_LIST_LIMIT) {
					dropList += "<a class='dropdown-item' href='/cluster/info/" + clusterAlias + "/change'><i class='fas fa-feather-alt fa-sm fa-fw mr-1'></i>" + clusterAlias + "</a>";
					i++;
				}
			}
			dropList += "<a class='dropdown-item' href='/cluster/multi'><i class='fas fa-server fa-sm fa-fw mr-1'></i>More...</a></div>";
			subject.getSession().setAttribute(KConstants.SessionAlias.CLUSTER_ALIAS_LIST, dropList);
		}
	}

	/** Reset password. */
	@RequestMapping(value = "/reset/", method = RequestMethod.POST)
	public String reset(HttpSession session, HttpServletRequest request) {
		String password = request.getParameter("ke_new_password_name");
		Signiner signin = (Signiner) SecurityUtils.getSubject().getSession().getAttribute(KConstants.Login.SESSION_USER);
		signin.setPassword(password);
		int code = accountService.reset(signin);
		if (code > 0) {
			return "redirect:/account/signout";
		} else {
			return "redirect:/errors/500";
		}

	}

	/** Signout system. */
	@RequestMapping(value = "/signout", method = RequestMethod.GET)
	public String logout() {
		Subject subject = SecurityUtils.getSubject();
		if (subject.isAuthenticated()) {
			subject.getSession().removeAttribute(KConstants.Login.SESSION_USER);
			subject.getSession().removeAttribute(KConstants.Login.SESSION_USER_TIME);
			subject.getSession().removeAttribute(KConstants.Login.ERROR_LOGIN);
			subject.getSession().removeAttribute(KConstants.Role.WHETHER_SYSTEM_ADMIN);
		}
		return "redirect:/account/signin";
	}

}
