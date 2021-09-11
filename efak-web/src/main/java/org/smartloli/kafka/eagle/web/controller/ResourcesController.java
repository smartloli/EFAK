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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.smartloli.kafka.eagle.web.service.ResourceService;
import org.smartloli.kafka.eagle.web.sso.filter.SSORealm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * System resource management is used to control access to each module.
 * 
 * @author smartloli.
 *
 *         Created by May 26, 2017.
 */
@Controller
@RequestMapping("/system")
public class ResourcesController {

	@Autowired
	private ResourceService resourceService;
	@Autowired
	private SSORealm ssoRealm;

	/** Resource viewer. */
	@RequiresPermissions("/system/resource")
	@RequestMapping(value = "/resource", method = RequestMethod.GET)
	public ModelAndView resourceView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/system/resource");
		return mav;
	}

	/** Resource graph. */
	@RequestMapping(value = "/resource/graph/ajax", method = RequestMethod.GET)
	public void resGraphAjax(HttpServletResponse response, HttpServletRequest request) {
		try {
			byte[] output = resourceService.getResourcesTree().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Add home resource form. */
	@RequestMapping(value = "/resource/add/home/", method = RequestMethod.POST)
	public String addHome(HttpSession session, HttpServletRequest request) {
		String name = request.getParameter("ke_resource_home_name");
		String url = request.getParameter("ke_resource_home_url");
		Map<String, Object> map = new HashMap<>();
		map.put("name", name);
		map.put("url", url);
		map.put("parentId", -1);
		resourceService.insertResource(map);

		return "redirect:/system/resource";
	}

	/** Add children resource form. */
	@RequestMapping(value = "/resource/add/children/", method = RequestMethod.POST)
	public String addChildren(HttpSession session, HttpServletRequest request) {
		String name = request.getParameter("ke_resource_child_name");
		String url = request.getParameter("ke_resource_child_url");
		String parentId = request.getParameter("res_parent_id");
		Map<String, Object> map = new HashMap<>();
		map.put("name", name);
		map.put("url", url);
		map.put("parentId", parentId);
		int code = resourceService.insertResource(map);
		if (code > 0) {
			ssoRealm.clearAllCached();
			return "redirect:/system/resource";
		} else {
			return "redirect:/errors/500";
		}

	}

	/** Delete resource parent or children. */
	@RequestMapping(value = "/resource/delete/parent/or/children/", method = RequestMethod.POST)
	public String deleteParentOrChildren(HttpSession session, HttpServletRequest request) {
		String res_child_root_id = request.getParameter("res_child_root_id");
		String res_child_id = request.getParameter("res_child_id");
		int resId = 0;
		Map<String, Object> map = new HashMap<>();
		if (res_child_id == null) {
			resId = Integer.parseInt(res_child_root_id);
			map.put("parentId", resId);
		} else {
			resId = Integer.parseInt(res_child_id);
			map.put("resourceId", resId);
		}
		int code = resourceService.deleteParentOrChildByResId(map);
		if (code > 0) {
			return "redirect:/system/resource";
		} else {
			return "redirect:/errors/500";
		}

	}

	/** Get parent resource. */
	@RequestMapping(value = "/resource/parent/ajax", method = RequestMethod.GET)
	public void resParentAjax(HttpServletResponse response, HttpServletRequest request) {
		try {
			byte[] output = resourceService.getResourceParent().toString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Get children resource by parent id. */
	@RequestMapping(value = "/resource/child/{parentId}/ajax", method = RequestMethod.GET)
	public void resChildAjax(@PathVariable("parentId") int parentId, HttpServletResponse response, HttpServletRequest request) {
		try {
			byte[] output = resourceService.findResourceByParentId(parentId).toString().getBytes();
			BaseController.response(output, response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
