package com.webank.cms.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.webank.cms.domain.UserDomain;
import com.webank.cms.factory.MongoDBFactory;

/**
 * @Date Aug 1, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note TODO
 */
public class UserService {

	private final static String TAB_NAME = "user_list";

	public static UserDomain get(String username, String password) {
		String ret = MongoDBFactory.login(username, password, TAB_NAME);
		if ("".equals(ret)) {
			return null;
		} else {
			UserDomain user = new UserDomain();
			JSONObject obj = JSON.parseObject(ret);
			user.setUsername(obj.getString("username"));
			user.setAdminType(obj.getIntValue("adminType"));
			return user;
		}
	}

}
