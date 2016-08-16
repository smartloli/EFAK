package com.smartloli.kafka.eagle.domain;

import com.google.gson.Gson;

/**
 * @Date Aug 16, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note TODO
 */
public class OffsetZkDomain {

	private long offset;
	private String create;
	private String modify;
	private String owners;

	public String getOwners() {
		return owners;
	}

	public void setOwners(String owners) {
		this.owners = owners;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public String getCreate() {
		return create;
	}

	public void setCreate(String create) {
		this.create = create;
	}

	public String getModify() {
		return modify;
	}

	public void setModify(String modify) {
		this.modify = modify;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
