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

import org.smartloli.kafka.eagle.common.protocol.AlertInfo;
import org.smartloli.kafka.eagle.common.protocol.ClustersInfo;

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
	public int add(AlertInfo alert);

	/** Get alarmer interface. */
	public String get(String clusterAlias, String formatter);

	/** List alarmer information. */
	public List<AlertInfo> list(Map<String, Object> params);

	/** Count alert size. */
	public int alertCount(Map<String, Object> params);

	/** Exist alert by cluster_group_topic from table. */
	public int isExistAlertByCGT(Map<String, Object> params);

	/** Find alert by cluster_group_topic from table. */
	public AlertInfo findAlertByCGT(Map<String, Object> params);

	/** Delete alert by id. */
	public int deleteAlertById(int id);

	/** Find alert info by id. */
	public String findAlertById(int id);

	/** Find alert info by id. */
	public int modifyAlertById(AlertInfo alert);

	/** Insert alert data into db. */
	public int create(ClustersInfo clusterInfo);

	/** List cluster information from alert. */
	public List<ClustersInfo> history(Map<String, Object> params);
	
	public int alertHistoryCount(Map<String, Object> params);
	
	/** Delete alert by id. */
	public int deleteClusterAlertById(int id);
	
	/** Find alert info by id. */
	public String findClusterAlertById(int id);
	
	/** Modify alert info by id. */
	public int modifyClusterAlertById(ClustersInfo cluster);
	
	/** Query clusters collector data. */
	public List<ClustersInfo> historys();

}
