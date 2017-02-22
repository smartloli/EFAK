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
package org.smartloli.kafka.eagle.controller.aop;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import org.smartloli.kafka.eagle.util.ConstantUtils;
import org.smartloli.kafka.eagle.util.SystemConfigUtils;

/**
 * Handler url request and check whether has session .
 *
 * @author smartloli.
 *
 *         Created by Feb 20, 2017
 */
public class AccountInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		try {
			Object object = request.getSession().getAttribute(ConstantUtils.SessionAlias.CLUSTER_ALIAS);
			if (object == null) {
				String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
				String defaultClusterAlias = clusterAliass[0];
				request.getSession().setAttribute(ConstantUtils.SessionAlias.CLUSTER_ALIAS, defaultClusterAlias);
			}
		} catch (Exception e) {
			String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
			String defaultClusterAlias = clusterAliass[0];
			request.getSession().setAttribute(ConstantUtils.SessionAlias.CLUSTER_ALIAS, defaultClusterAlias);
			e.printStackTrace();
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
