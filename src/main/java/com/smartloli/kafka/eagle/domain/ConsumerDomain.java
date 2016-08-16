package com.smartloli.kafka.eagle.domain;

import java.util.List;

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
public class ConsumerDomain {

	private int id;
	private String group;
	private List<String> topic;
	private int consumerNumber;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public List<String> getTopic() {
		return topic;
	}

	public void setTopic(List<String> topic) {
		this.topic = topic;
	}

	public int getConsumerNumber() {
		return consumerNumber;
	}

	public void setConsumerNumber(int consumerNumber) {
		this.consumerNumber = consumerNumber;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
