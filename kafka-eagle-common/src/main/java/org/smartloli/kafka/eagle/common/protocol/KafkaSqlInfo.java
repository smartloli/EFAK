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
package org.smartloli.kafka.eagle.common.protocol;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

/**
 * KSQL topic metadata management.
 * 
 * @author smartloli.
 *
 *         Created by Feb 28, 2017
 */
public class KafkaSqlInfo extends BaseProtocol {

	private List<Integer> partition = new ArrayList<>();
	private String sql;
	private String metaSql;
	private JSONObject schema = new JSONObject(new LinkedHashMap<>());
	private String tableName;
	private long limit = 0L;
	private String topic;
	private boolean status;
	private List<HostsInfo> seeds = new ArrayList<>();
	private String clusterAlias;

	public long getLimit() {
		return limit;
	}

	public void setLimit(long limit) {
		this.limit = limit;
	}

	public String getClusterAlias() {
		return clusterAlias;
	}

	public void setClusterAlias(String clusterAlias) {
		this.clusterAlias = clusterAlias;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getMetaSql() {
		return metaSql;
	}

	public void setMetaSql(String metaSql) {
		this.metaSql = metaSql;
	}

	public List<HostsInfo> getSeeds() {
		return seeds;
	}

	public void setSeeds(List<HostsInfo> seeds) {
		this.seeds = seeds;
	}

	public JSONObject getSchema() {
		return schema;
	}

	public void setSchema(JSONObject schema) {
		this.schema = schema;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public List<Integer> getPartition() {
		return partition;
	}

	public void setPartition(List<Integer> partition) {
		this.partition = partition;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

}
