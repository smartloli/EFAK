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
public class ConsumerDetailDomain {

	private int id;
	private String topic;
	private boolean isConsumering;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public boolean isConsumering() {
		return isConsumering;
	}

	public void setConsumering(boolean isConsumering) {
		this.isConsumering = isConsumering;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
