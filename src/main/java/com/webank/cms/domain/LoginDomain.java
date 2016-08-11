package com.webank.cms.domain;

import com.google.gson.Gson;

/**
 * @Date Aug 6, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note TODO
 */
public class LoginDomain {

	private String error;
	private String refUrl;

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getRefUrl() {
		return refUrl;
	}

	public void setRefUrl(String refUrl) {
		this.refUrl = refUrl;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
