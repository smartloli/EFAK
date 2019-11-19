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
 * Alarm message info.
 * 
 * @author smartloli.
 *
 *         Created by Oct 7, 2019
 */
public class AlarmMessageInfo extends BaseProtocol {

	/**
	 * AlarmID : [ 1 ]
	 * 
	 * AlarmStatus : [ PROBLEM ]
	 * 
	 * AlarmLevel : [ P0 ]
	 * 
	 * AlarmProject : [ Kafka ]
	 * 
	 * AlarmTimes : [ current(1), max(7) ]
	 * 
	 * AlarmDate : [ 2019-10-07 21:43:22 ]
	 * 
	 * AlarmContent : [ node.shutdown [ localhost:9092 ] ]
	 * 
	 */

	private String title;
	private String alarmStatus;
	private String alarmProject;
	private String alarmLevel;
	private String alarmTimes;
	private String alarmContent;
	private String alarmDate;
	private int alarmId;

	public String toDingDingMarkDown() {
		return title + " \n\n>#### AlarmID : [ **" + alarmId + "** ]\n> #### AlarmStatus : [ **" + alarmStatus + "** ]\n> #### AlarmLevel : [ " + alarmLevel + " ]\n" + "> #### AlarmProject : [ " + alarmProject + " ]\n"
				+ "> #### AlarmTimes : [ " + alarmTimes + " ]\n" + "> #### AlarmDate : [ " + alarmDate + " ]\n" + "> #### AlarmContent : [ " + alarmContent + " ]";
	}

	public String toWeChatMarkDown() {
		return title + " \n\n>AlarmID : [ **" + alarmId + "** ]\n> AlarmStatus : [ **" + alarmStatus + "** ]\n" + "> AlarmLevel : [ " + alarmLevel + " ]\n" + "> AlarmProject : [ " + alarmProject + " ]\n" + "> AlarmTimes : [ " + alarmTimes
				+ " ]\n" + "> AlarmDate : [ " + alarmDate + " ]\n" + "> AlarmContent : [ " + alarmContent + " ]";
	}
	
	public String toMail() {
		return title + " \n AlarmID : [ " + alarmId + " ]\n AlarmStatus : [ " + alarmStatus + " ]\n" + " AlarmLevel : [ " + alarmLevel + " ]\n" + " AlarmProject : [ " + alarmProject + " ]\n" + " AlarmTimes : [ " + alarmTimes
				+ " ]\n AlarmDate : [ " + alarmDate + " ]\n" + " AlarmContent : [ " + alarmContent + " ]"; 
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAlarmStatus() {
		return alarmStatus;
	}

	public void setAlarmStatus(String alarmStatus) {
		this.alarmStatus = alarmStatus;
	}

	public String getAlarmProject() {
		return alarmProject;
	}

	public void setAlarmProject(String alarmProject) {
		this.alarmProject = alarmProject;
	}

	public String getAlarmLevel() {
		return alarmLevel;
	}

	public void setAlarmLevel(String alarmLevel) {
		this.alarmLevel = alarmLevel;
	}

	public String getAlarmTimes() {
		return alarmTimes;
	}

	public void setAlarmTimes(String alarmTimes) {
		this.alarmTimes = alarmTimes;
	}

	public String getAlarmContent() {
		return alarmContent;
	}

	public void setAlarmContent(String alarmContent) {
		this.alarmContent = alarmContent;
	}

	public String getAlarmDate() {
		return alarmDate;
	}

	public void setAlarmDate(String alarmDate) {
		this.alarmDate = alarmDate;
	}

	public int getAlarmId() {
		return alarmId;
	}

	public void setAlarmId(int alarmId) {
		this.alarmId = alarmId;
	}

}
