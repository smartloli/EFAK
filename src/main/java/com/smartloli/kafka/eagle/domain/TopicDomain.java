package com.smartloli.kafka.eagle.domain;

import java.util.List;

import com.google.gson.Gson;

/**
 * @Date Mar 11, 2016
 *
 * @Author dengjie
 *
 * @Note TODO
 */
public class TopicDomain {

	private String topic;
	private int partition;
	private List<PartitionInfo> partitionInfo;

	public List<PartitionInfo> getPartitionInfo() {
		return partitionInfo;
	}

	public void setPartitionInfo(List<PartitionInfo> partitionInfo) {
		this.partitionInfo = partitionInfo;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public int getPartition() {
		return partition;
	}

	public void setPartition(int partition) {
		this.partition = partition;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
