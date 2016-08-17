package com.smartloli.kafka.eagle.domain;

import java.util.HashSet;
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

	private int id = 0;
	private String topic = "";
	private Set<String> partitions = new HashSet<String>();
	private int partitionNumbers = 0;
	private String created = "";
	private String modify = "";

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getModify() {
		return modify;
	}

	public void setModify(String modify) {
		this.modify = modify;
	}

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

	public Set<String> getPartitions() {
		return partitions;
	}

	public void setPartitions(Set<String> partitions) {
		this.partitions = partitions;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
