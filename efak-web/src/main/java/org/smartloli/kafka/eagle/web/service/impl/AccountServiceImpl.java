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

import java.util.List;
import java.util.Map;

import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.web.dao.UserDao;
import org.smartloli.kafka.eagle.web.pojo.Signiner;
import org.smartloli.kafka.eagle.web.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

/**
 * Handling requests for login, reset password, logout, etc.
 * 
 * @author smartloli.
 *
 *         Created by May 17, 2017
 */
@Service
public class AccountServiceImpl implements AccountService {

	@Autowired
	private UserDao userDao;

	@Override
	public Signiner login(String username, String password) {
		Signiner signin = new Signiner();
		signin.setUsername(username);
		signin.setPassword(password);

		if (userDao.login(signin) == null) {
			signin.setUsername(KConstants.Login.UNKNOW_USER);
			signin.setPassword("");
			return signin;
		}

		return userDao.login(signin);
	}

	@Override
	public Signiner findUserByRtxNo(int rtxno) {
		return userDao.findUserByRtxNo(rtxno).get(0);
	}

	@Override
	public List<Signiner> findUserBySearch(Map<String, Object> params) {
		return userDao.findUserBySearch(params);
	}

	@Override
	public int userCounts() {
		return userDao.userCounts();
	}

	@Override
	public int insertUser(Signiner signin) {
		return userDao.insertUser(signin);
	}

	@Override
	public int reset(Signiner signin) {
		return userDao.reset(signin);
	}

	@Override
	public int modify(Signiner signin) {
		return userDao.modify(signin);
	}

	@Override
	public int delete(Signiner signin) {
		return userDao.delete(signin);
	}

	@Override
	public String findUserById(int id) {
		Signiner signer = userDao.findUserById(id);
		JSONObject object = new JSONObject();
		object.put("rtxno", signer.getRtxno());
		object.put("username", signer.getUsername());
		object.put("realname", signer.getRealname());
		object.put("email", signer.getEmail());
		return object.toJSONString();
	}

	@Override
	public String getAutoUserRtxNo() {
		Signiner signer = userDao.findUserLimitOne();
		JSONObject object = new JSONObject();
		object.put("rtxno", signer.getRtxno()+1);
		return object.toJSONString();
	}

}
