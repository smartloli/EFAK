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
package org.smartloli.kafka.eagle.web.sso.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.StrUtils;
import org.smartloli.kafka.eagle.web.pojo.Signiner;
import org.smartloli.kafka.eagle.web.service.AccountService;
import org.smartloli.kafka.eagle.web.sso.pojo.SSOAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Custom filter access request.
 * 
 * @author smartloli.
 *
 *         Created by May 17, 2017
 */
public class SSOFilter implements Filter {

	@Autowired
	private AccountService accountService;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;

		String requestUri = req.getRequestURI();
		String query = StrUtils.convertNull(req.getQueryString());
		if (requestUri.contains("/account/signin/action")) {
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			Signiner signinerChk = accountService.login(username, password);
			if (!signinerChk.getUsername().equals(KConstants.Login.UNKNOW_USER)) {
				SSOAuthenticationToken token = new SSOAuthenticationToken(signinerChk.getRtxno(), signinerChk.getRealname(), signinerChk.getUsername(), signinerChk.getEmail());
				SecurityUtils.getSubject().login(token);
			}
		} else {
			Signiner signiner = (Signiner) getSession().getAttribute(KConstants.Login.SESSION_USER);
			if (signiner == null) {
				if (req.getHeader("x-requested-with") != null && req.getHeader("x-requested-with").equalsIgnoreCase("XMLHttpRequest")) {
					resp.setHeader("sessionstatus", "timeout");
					return;
				}
				if (!StrUtils.isNull(query)) {
					requestUri += "?" + query;
				}
				resp.sendRedirect("/ke/account/signin?" + requestUri);
				return;
			}
		}
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {

	}

	private Session getSession() {
		Subject subject = SecurityUtils.getSubject();
		return subject.getSession();
	}

}
