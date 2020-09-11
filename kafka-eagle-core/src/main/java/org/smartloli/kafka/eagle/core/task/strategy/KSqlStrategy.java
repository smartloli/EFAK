/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.core.task.strategy;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.common.protocol.BaseProtocol;

/**
 * Build kafka sql task policy.
 *
 * @author smartloli.
 * <p>
 * Created by Sep 11, 2020
 */
public class KSqlStrategy extends BaseProtocol {

    private String cluster;
    private String sql;
    private String topic;
    private int partition;
    private long start;
    private long end;

    /**
     * KSQL combined filters of multiple fields.
     *
     * <code>
     * select * from topic where `partition` in (0) and msg like 'kafka-eagle%' and msg like 'smartloli%' and timespan > 1599754181586 limit 10;
     * </code>
     * <p>
     * So, filters include [{"like":"kafka-eagle",">":"1599754181586"},{"like":"smartloli"}]
     */
    private JSONArray filters = new JSONArray();
    /**
     * <code>
     * select * from topic where `partition` in (0) and msg like 'kafka-eagle' and timespan > 1599754181586 limit 10;
     * </code>
     * So, filter include {"like":"kafka-eagle",">":"1599754181586"}
     */
    private JSONObject filter = new JSONObject();
    private long limit;

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
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

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public JSONArray getFilters() {
        return filters;
    }

    public void setFilters(JSONArray filters) {
        this.filters = filters;
    }

    public JSONObject getFilter() {
        return filter;
    }

    public void setFilter(JSONObject filter) {
        this.filter = filter;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }
}
