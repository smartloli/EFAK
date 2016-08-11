package com.webank.cms.aop;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.webank.cms.domain.UserDomain;

/**
 * @Date Dec 16, 2015
 *
 * @Author dengjie
 *
 * @Note Handler url request and check whether has logined
 */
public class AccountInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String method = request.getMethod();
		if ("POST".equals(method)) {
			return true;
		}
		UserDomain user = (UserDomain) request.getSession().getAttribute("user");

		if (user == null) {
			request.getRequestDispatcher("/WEB-INF/views/account/signin.jsp").forward(request, response);
			return false;
		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		super.postHandle(request, response, handler, modelAndView);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		super.afterCompletion(request, response, handler, ex);
	}

}
