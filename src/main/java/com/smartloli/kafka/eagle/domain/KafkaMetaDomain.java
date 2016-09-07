package com.smartloli.kafka.eagle.domain;

import com.google.gson.Gson;

/**
 * @Date Aug 15, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note TODO
 */
public class KafkaMetaDomain {

	private int partitionId;
	private int leader;
	private String isr;
	private String replicas;

	public int getPartitionId() {
		return partitionId;
	}

	public void setPartitionId(int partitionId) {
		this.partitionId = partitionId;
	}

	public int getLeader() {
		return leader;
	}

	public void setLeader(int leader) {
		this.leader = leader;
	}

	public String getIsr() {
		return isr;
	}

	public void setIsr(String isr) {
		this.isr = isr;
	}

	public String getReplicas() {
		return replicas;
	}

	public void setReplicas(String replicas) {
		this.replicas = replicas;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
