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
package org.smartloli.kafka.eagle.web.service;

import java.util.List;
import java.util.Map;

import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmClusterInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmConfigInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmConsumerInfo;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicLogSize;

/**
 * Alarm service interface.
 * 
 * @author smartloli.
 *
 *         Created by Jan 17, 2017.
 * 
 *         Update by hexiang 20170216
 */
public interface AlertService {

	/** Add alerter interface. */
	public int insertAlarmConsumer(AlarmConsumerInfo alarmConsumer);

	/** Get consumer group alarm. */
	public String getAlarmConsumerGroup(String clusterAlias, String formatter, String search);

	/** Get consumer topic alarm. */
	public String getAlarmConsumerTopic(String clusterAlias, String formatter, String group, String search);

	/** List alarmer information. */
	public List<AlarmConsumerInfo> getAlarmConsumerAppList(Map<String, Object> params);

	/** Count alarm consumer application size. */
	public int alertConsumerAppCount(Map<String, Object> params);

	/** Modify consumer alarm switch. */
	public int modifyConsumerAlertSwitchById(AlarmConsumerInfo alarmConsumer);

	/** Find alarm consumer info by id. */
	public AlarmConsumerInfo findAlarmConsumerAlertById(int id);

	/** Get all alarm consumer tasks. */
	public List<AlarmConsumerInfo> getAllAlarmConsumerTasks();

	/** Delete alarm consumer by id. */
	public int deleteAlarmConsumerById(int id);

	/** Modify alarm consumer info by id. */
	public int modifyAlarmConsumerById(AlarmConsumerInfo alarmConsumer);

	/** Modify alert consumer(alarmtimes,isnormal) info by id. */
	public int modifyConsumerStatusAlertById(AlarmConsumerInfo alarmConsumer);

	/** Storage or update alarm cluster,such as kafka or zookeeper. */
	public int create(AlarmClusterInfo clusterInfo);

	/** List cluster information from alert. */
	public List<AlarmClusterInfo> getAlarmClusterList(Map<String, Object> params);

	/** Get all alarm cluster tasks. */
	public List<AlarmClusterInfo> getAllAlarmClusterTasks();

	public int getAlarmClusterCount(Map<String, Object> params);

	/** Delete alert by id. */
	public int deleteAlarmClusterAlertById(int id);

	/** Find alert cluster info by id. */
	public AlarmClusterInfo findAlarmClusterAlertById(int id);

	/** Modify alarm cluster switch by id. */
	public int modifyClusterAlertSwitchById(AlarmClusterInfo clusterInfo);

	/** Modify alert cluster(server,alarm group,alarm level) info by id. */
	public int modifyClusterAlertById(AlarmClusterInfo cluster);

	/** Modify alert cluster(alarmtimes,isnormal) info by id. */
	public int modifyClusterStatusAlertById(AlarmClusterInfo cluster);

	/** Get alert type list. */
	public String getAlertTypeList();

	/** Get alert cluster type list. */
	public String getAlertClusterTypeList(String type, Map<String, Object> params);

	/** Storage or update alarm config info. */
	public int insertOrUpdateAlarmConfig(AlarmConfigInfo alarmConfig);

	/** Find alarm config by group name. */
	public boolean findAlarmConfigByGroupName(Map<String, Object> params);

	/** Get alarm config list. */
	public List<AlarmConfigInfo> getAlarmConfigList(Map<String, Object> params);

	/** Get alarm config count. */
	public int alarmConfigCount(Map<String, Object> params);

	/** Delete alarm config by group name. */
	public int deleteAlertByGroupName(Map<String, Object> params);

	/** Get alarm config by group name. */
	public AlarmConfigInfo getAlarmConfigByGroupName(Map<String, Object> params);

	/** Get lastest lag used to alarm consumer. */
	public long queryLastestLag(Map<String, Object> params);

	/** Get topic producer logsize by alarm. */
	public List<TopicLogSize> queryTopicProducerByAlarm(Map<String, Object> params);
}
