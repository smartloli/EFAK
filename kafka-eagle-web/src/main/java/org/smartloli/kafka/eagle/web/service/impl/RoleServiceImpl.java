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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.core.ipc.KafkaOffsetGetter;
import org.smartloli.kafka.eagle.web.dao.ResourcesDao;
import org.smartloli.kafka.eagle.web.dao.RoleDao;
import org.smartloli.kafka.eagle.web.pojo.Role;
import org.smartloli.kafka.eagle.web.pojo.RoleResource;
import org.smartloli.kafka.eagle.web.pojo.UserRole;
import org.smartloli.kafka.eagle.web.service.RoleService;
import org.smartloli.kafka.eagle.web.sso.pojo.Resources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Handle actions such as adding, deleting, modifying, etc.
 * 
 * @author smartloli.
 *
 *         Created by May 24, 2017
 */
@Service
public class RoleServiceImpl implements RoleService {

	@Autowired
	private RoleDao roleDao;
	@Autowired
	private ResourcesDao resDao;

	@Override
	public List<Role> getRoles() {
		return roleDao.getRoles();
	}

	@Override
	public String getRoleTree(int roleId) {
		List<RoleResource> roleResources = roleDao.findRoleResourcesByRoleId(roleId);
		List<Resources> resources = resDao.getResourcesTree();
		Map<String, List<Resources>> map = new HashMap<>();
		Map<Integer, String> parent = new HashMap<>();
		for (Resources resource : resources) {
			if (resource.getParentId() == -1) {
				parent.put(resource.getResourceId(), resource.getName());
			}
		}
		for (Resources resource : resources) {
			if (resource.getParentId() != -1) {
				if (map.containsKey(parent.get(resource.getParentId()).toString())) {
					map.get(parent.get(resource.getParentId())).add(resource);
				} else {
					List<Resources> subName = new ArrayList<>();
					subName.add(resource);
					map.put(parent.get(resource.getParentId()).toString(), subName);
				}
			}
		}
		JSONArray targets = new JSONArray();
		for (Entry<String, List<Resources>> entry : map.entrySet()) {
			JSONObject subTarget = new JSONObject();
			JSONArray subTargets = new JSONArray();
			subTarget.put("text", entry.getKey());
			for (Resources resource : entry.getValue()) {
				JSONObject subInSubTarget = new JSONObject();
				subInSubTarget.put("text", resource.getName());
				subInSubTarget.put("href", resource.getResourceId());
				if (roleResources != null && roleResources.size() > 0) {
					for (RoleResource roleResource : roleResources) {
						if (roleResource.getResourceId() == resource.getResourceId()) {
							JSONObject state = new JSONObject();
							state.put("checked", true);
							subInSubTarget.put("state", state);
							break;
						}
					}
				}
				subTargets.add(subInSubTarget);
			}
			subTarget.put("nodes", subTargets);
			targets.add(subTarget);
		}
		return targets.toJSONString();
	}

	@Override
	public int insertRoleResource(RoleResource roleResource) {
		return roleDao.insertRoleResource(roleResource);
	}

	@Override
	public int deleteRoleResource(RoleResource roleResource) {
		return roleDao.deleteRoleResource(roleResource);
	}

	@Override
	public List<UserRole> findRoleByUserId(int userId) {
		return roleDao.findRoleByUserId(userId);
	}

	@Override
	public int insertUserRole(UserRole userRole) {
		return roleDao.insertUserRole(userRole);
	}

	@Override
	public int deleteUserRole(UserRole userRole) {
		return roleDao.deleteUserRole(userRole);
	}

	@Override
	public String getConsoleCache() {
		JSONObject object = new JSONObject();
		object.put("cache", KafkaOffsetGetter.multiKafkaConsumerOffsets);
		object.put("date", CalendarUtils.getDate());
		System.out.println(object.toJSONString());
		return object.toJSONString();
	}

}
