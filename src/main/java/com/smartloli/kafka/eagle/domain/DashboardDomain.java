package com.smartloli.kafka.eagle.domain;

import com.google.gson.Gson;

/**
 * @Date Aug 13, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note TODO
 */
public class DashboardDomain {

	private int brokers;
	private int topics;
	private int zks;
	private int consumers;

	public int getBrokers() {
		return brokers;
	}

	public void setBrokers(int brokers) {
		this.brokers = brokers;
	}

	public int getTopics() {
		return topics;
	}

	public void setTopics(int topics) {
		this.topics = topics;
	}

	public int getZks() {
		return zks;
	}

	public void setZks(int zks) {
		this.zks = zks;
	}

	public int getConsumers() {
		return consumers;
	}

	public void setConsumers(int consumers) {
		this.consumers = consumers;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
