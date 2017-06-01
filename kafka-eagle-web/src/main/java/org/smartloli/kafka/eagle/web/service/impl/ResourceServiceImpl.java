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
package org.smartloli.kafka.eagle.web.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.smartloli.kafka.eagle.web.dao.ResourcesDao;
import org.smartloli.kafka.eagle.web.service.ResourceService;
import org.smartloli.kafka.eagle.web.sso.pojo.Resources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Handle, add, delete, modify, and other operations.
 * 
 * @author smartloli.
 *
 *         Created by May 18, 2017
 */
@Service
public class ResourceServiceImpl implements ResourceService {

	@Autowired
	private ResourcesDao resDao;

	@Override
	public List<Integer> findRoleIdByUserId(int userId) {
		return resDao.findRoleIdByUserId(userId);
	}

	@Override
	public List<Integer> findResourceIdByRole(int roleId) {
		return resDao.findResourceIdByRole(roleId);
	}

	@Override
	public List<Resources> getUserResources(int userId) {

		List<Integer> roleIds = resDao.findRoleIdByUserId(userId);
		List<Integer> resourceIds = new ArrayList<>();
		for (Integer roleId : roleIds) {
			resourceIds.addAll(resDao.findResourceIdByRole(roleId));
		}
		// Duplicate removal
		Set<Integer> onlyResourceIds = new HashSet<>();
		for (Integer resource : resourceIds) {
			onlyResourceIds.add(resource);
		}

		List<Resources> resources = new ArrayList<>();
		for (Integer onlyResourceId : onlyResourceIds) {
			resources.add(resDao.getUserResources(onlyResourceId));
		}
		return resources;
	}

	@Override
	public String getResourcesTree() {
		List<Resources> resources = resDao.getResourcesTree();
		Map<String, List<String>> map = new HashMap<>();
		Map<Integer, String> parent = new HashMap<>();
		for (Resources resource : resources) {
			if (resource.getParentId() == -1) {
				parent.put(resource.getResourceId(), resource.getName());
			}
		}
		for (Resources resource : resources) {
			if (resource.getParentId() != -1) {
				if (map.containsKey(parent.get(resource.getParentId()).toString())) {
					map.get(parent.get(resource.getParentId())).add(resource.getName());
				} else {
					List<String> subName = new ArrayList<>();
					subName.add(resource.getName());
					map.put(parent.get(resource.getParentId()).toString(), subName);
				}
			}
		}

		JSONObject target = new JSONObject();
		JSONArray targets = new JSONArray();
		target.put("name", "Home");
		for (Entry<String, List<String>> entry : map.entrySet()) {
			JSONObject subTarget = new JSONObject();
			JSONArray subTargets = new JSONArray();
			subTarget.put("name", entry.getKey());
			for (String str : entry.getValue()) {
				JSONObject subInSubTarget = new JSONObject();
				subInSubTarget.put("name", str);
				subTargets.add(subInSubTarget);
			}
			subTarget.put("children", subTargets);
			targets.add(subTarget);
		}
		target.put("children", targets);
		return target.toJSONString();
	}

	@Override
	public int insertResource(Map<String, Object> params) {
		return resDao.insertResource(params);
	}

	@Override
	public List<Resources> getResourceParent() {
		return resDao.getResourceParent();
	}

	@Override
	public List<Resources> findResourceByParentId(int parentId) {
		return resDao.findResourceByParentId(parentId);
	}

	@Override
	public int deleteParentOrChildByResId(Map<String, Object> params) {
		return resDao.deleteParentOrChildByResId(params);
	}

}
