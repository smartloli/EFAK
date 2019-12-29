/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.common.protocol.alarm;

import org.smartloli.kafka.eagle.common.protocol.BaseProtocol;

/**
 * Definition AlertConsumerInfo information.
 * 
 * @author smartloli.
 *
 *         Created by Oct 27, 2018
 */
public class AlarmConsumerInfo extends BaseProtocol {

	private int id;
	private String cluster = "";
	/** Consumer group name. */
	private String group = "";
	private String topic = "";
	private long lag = 0L;
	private String alarmGroup = "";
	private int alarmTimes;
	private int alarmMaxTimes;
	private String alarmLevel = "";
	private String isNormal = "";
	private String isEnable = "";
	private String created = "";
	private String modify = "";

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	/** Consumer group name. */
	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getCluster() {
		return cluster;
	}

	public void setCluster(String cluster) {
		this.cluster = cluster;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public long getLag() {
		return lag;
	}

	public void setLag(long lag) {
		this.lag = lag;
	}

	public String getAlarmGroup() {
		return alarmGroup;
	}

	public void setAlarmGroup(String alarmGroup) {
		this.alarmGroup = alarmGroup;
	}

	public int getAlarmTimes() {
		return alarmTimes;
	}

	public void setAlarmTimes(int alarmTimes) {
		this.alarmTimes = alarmTimes;
	}

	public int getAlarmMaxTimes() {
		return alarmMaxTimes;
	}

	public void setAlarmMaxTimes(int alarmMaxTimes) {
		this.alarmMaxTimes = alarmMaxTimes;
	}

	public String getAlarmLevel() {
		return alarmLevel;
	}

	public void setAlarmLevel(String alarmLevel) {
		this.alarmLevel = alarmLevel;
	}

	public String getIsNormal() {
		return isNormal;
	}

	public void setIsNormal(String isNormal) {
		this.isNormal = isNormal;
	}

	public String getIsEnable() {
		return isEnable;
	}

	public void setIsEnable(String isEnable) {
		this.isEnable = isEnable;
	}

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

}
