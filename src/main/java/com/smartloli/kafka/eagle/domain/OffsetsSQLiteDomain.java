package com.smartloli.kafka.eagle.domain;

/**
 * @Date Aug 18, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note TODO
 */
public class OffsetsSQLiteDomain {

	private String topic = "";
	private long logSize = 0L;
	private long offsets = 0L;
	private long lag = 0L;
	private long consumer = 0L;
	private long producer = 0L;
	private String created = "";

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public long getLogSize() {
		return logSize;
	}

	public void setLogSize(long logSize) {
		this.logSize = logSize;
	}

	public long getOffsets() {
		return offsets;
	}

	public void setOffsets(long offsets) {
		this.offsets = offsets;
	}

	public long getLag() {
		return lag;
	}

	public void setLag(long lag) {
		this.lag = lag;
	}

	public long getConsumer() {
		return consumer;
	}

	public void setConsumer(long consumer) {
		this.consumer = consumer;
	}

	public long getProducer() {
		return producer;
	}

	public void setProducer(long producer) {
		this.producer = producer;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

}
