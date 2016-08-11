package com.webank.cms.domain;

import com.google.gson.Gson;

/**
 * @Date Aug 8, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note TODO
 */
public class DashBoardDomain {

	private long release;
	private long unRelease;
	private long add;
	private long del;

	public long getRelease() {
		return release;
	}

	public void setRelease(long release) {
		this.release = release;
	}

	public long getUnRelease() {
		return unRelease;
	}

	public void setUnRelease(long unRelease) {
		this.unRelease = unRelease;
	}

	public long getAdd() {
		return add;
	}

	public void setAdd(long add) {
		this.add = add;
	}

	public long getDel() {
		return del;
	}

	public void setDel(long del) {
		this.del = del;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
