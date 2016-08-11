package com.webank.cms.domain;

import com.google.gson.Gson;

/**
 * @Date Aug 1, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note TODO
 */
public class UserDomain {

	private String username;
	private String password;
	private int adminType;// 0:admin,1:custormer

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getAdminType() {
		return adminType;
	}

	public void setAdminType(int adminType) {
		this.adminType = adminType;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
