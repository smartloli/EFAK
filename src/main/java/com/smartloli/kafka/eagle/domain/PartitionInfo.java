package com.smartloli.kafka.eagle.domain;

import com.google.gson.Gson;

/**
 * @Date Mar 11, 2016
 *
 * @Author dengjie
 *
 * @Note TODO
 */
public class PartitionInfo {

	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
