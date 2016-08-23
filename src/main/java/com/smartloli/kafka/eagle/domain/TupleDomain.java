package com.smartloli.kafka.eagle.domain;

import com.google.gson.Gson;

/**
 * @Date May 5, 2016
 *
 * @Author dengjie
 *
 * @Note TODO
 */
public class TupleDomain {
	private long timespan;
	private String ret = "";
	private boolean status;

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public long getTimespan() {
		return timespan;
	}

	public void setTimespan(long timespan) {
		this.timespan = timespan;
	}

	public String getRet() {
		return ret;
	}

	public void setRet(String ret) {
		this.ret = ret;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
