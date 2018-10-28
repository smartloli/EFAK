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
package org.smartloli.kafka.eagle.web.dao;

import java.util.List;
import java.util.Map;

import org.smartloli.kafka.eagle.common.protocol.AlertInfo;
import org.smartloli.kafka.eagle.common.protocol.ClustersInfo;

/**
 * Store kafka alert data & metrics data into database.
 * 
 * @author smartloli.
 *
 *         Created by Oct 27, 2018
 */
public interface AlertDao {

	/** Insert alert data into db. */
	public int insertAlert(AlertInfo alert);

	/** Query collector data. */
	public List<AlertInfo> query(Map<String, Object> params);

	public int alertCount(Map<String, Object> params);

	/** Exist alert by cluster_group_topic from table. */
	public int isExistAlertByCGT(Map<String, Object> params);

	/** Find alert by cluster_group_topic from table. */
	public AlertInfo findAlertByCGT(Map<String, Object> params);

	/** Delete alert by id. */
	public int deleteAlertById(int id);

	/** Find alert info by id. */
	public AlertInfo findAlertById(int id);

	/** Modify alert info by id. */
	public int modifyAlertById(AlertInfo alert);

	/** Insert alert data into db. */
	public int insertKafkaOrZK(ClustersInfo clusterInfo);
	
	/** Query cluster collector data. */
	public List<ClustersInfo> history(Map<String, Object> params);
	
	public int alertHistoryCount(Map<String, Object> params);
	
	/** Delete alert by id. */
	public int deleteClusterAlertById(int id);
	
	/** Find alert info by id. */
	public ClustersInfo findClusterAlertById(int id);
	
	/** Modify alert info by id. */
	public int modifyClusterAlertById(ClustersInfo cluster);
	
	/** Query clusters collector data. */
	public List<ClustersInfo> historys();
	
}
