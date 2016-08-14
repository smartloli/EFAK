package com.smartloli.kafka.eagle.domain;

import java.util.Set;

import com.google.gson.Gson;

/**
 * @Date Mar 30, 2016
 *
 * @Author dengjie
 *
 * @Note TODO
 */
public class PartitionsDomain {

	private int id;
	private String topic;
	private Set<Integer> partitions;
	private int partitionNumbers;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPartitionNumbers() {
		return partitionNumbers;
	}

	public void setPartitionNumbers(int partitionNumbers) {
		this.partitionNumbers = partitionNumbers;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Set<Integer> getPartitions() {
		return partitions;
	}

	public void setPartitions(Set<Integer> partitions) {
		this.partitions = partitions;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
