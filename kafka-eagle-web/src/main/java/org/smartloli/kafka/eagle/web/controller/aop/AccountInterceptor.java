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
package org.smartloli.kafka.eagle.web.controller.aop;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.smartloli.kafka.eagle.common.util.ConstantUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;

/**
 * Handler url request and check whether has session .
 *
 * @author smartloli.
 *
 *         Created by Feb 20, 2017
 */
public class AccountInterceptor extends HandlerInterceptorAdapter {

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();
	private int count = 0;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		try {
			Object object = request.getSession().getAttribute(ConstantUtils.SessionAlias.CLUSTER_ALIAS);
			if (object == null) {
				String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
				String defaultClusterAlias = clusterAliass[0];
				request.getSession().setAttribute(ConstantUtils.SessionAlias.CLUSTER_ALIAS, defaultClusterAlias);
			}
			String formatter = SystemConfigUtils.getProperty("kafka.eagle.offset.storage");
			if ("kafka".equals(formatter) && count == 0) {
				HttpSession session = request.getSession();
				String clusterAlias = session.getAttribute(ConstantUtils.SessionAlias.CLUSTER_ALIAS).toString();
				String brokers = kafkaService.getAllBrokersInfo(clusterAlias);
				JSONArray kafkaBrokers = JSON.parseArray(brokers);
				String bootstrapServers = "";
				for (Object subObject : kafkaBrokers) {
					JSONObject kafkaBroker = (JSONObject) subObject;
					String host = kafkaBroker.getString("host");
					int port = kafkaBroker.getInteger("port");
					bootstrapServers += host + ":" + port + ",";
				}
				bootstrapServers = bootstrapServers.substring(0, bootstrapServers.length() - 1);
				count++;
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
