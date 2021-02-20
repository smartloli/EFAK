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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.web.service.AclService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONArray;

/**
 * Kafka acl controller to viewer data.
 * 
 * @author jeff
 *
 */
@Controller
public class AclController {

	/** Kafka acl service interface. */
	@Autowired
	private AclService aclService;

	/** acls viewer. */
	@RequestMapping(value = "/acls", method = RequestMethod.GET)
	public ModelAndView aclsView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/acls/acls");
		return mav;
	}
	
	
	/** Get acl data by ajax. */
	@RequestMapping(value = "/acls/list", method = RequestMethod.GET)
	@ResponseBody
	public JSONArray aclslist(HttpServletResponse response, HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
		JSONArray result = aclService.getAcls(clusterAlias);
		
		return result;
	}
	
	
    @RequestMapping(value = "/acls/topic/{tname}", method = RequestMethod.GET)
    @ResponseBody
    public JSONArray aclstopic(@PathVariable("tname") String tname, HttpServletResponse response, HttpServletRequest request, HttpSession session) {
		String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
		JSONArray result = aclService.getTopicAcls(clusterAlias, tname);

		return result;
    }	
	

}
