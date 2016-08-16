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
public class OffsetDomain {

	private int partition;
	private long logSize;
	private long offset;
	private long lag;
	private String owner;
	private String create;
	private String modify;

	public int getPartition() {
		return partition;
	}

	public void setPartition(int partition) {
		this.partition = partition;
	}

	public long getLogSize() {
		return logSize;
	}

	public void setLogSize(long logSize) {
		this.logSize = logSize;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public long getLag() {
		return lag;
	}

	public void setLag(long lag) {
		this.lag = lag;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
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
