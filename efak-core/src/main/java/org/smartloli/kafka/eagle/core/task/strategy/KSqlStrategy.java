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

import org.smartloli.kafka.eagle.common.protocol.BaseProtocol;

import java.util.ArrayList;
import java.util.List;

/**
 * Build kafka sql task policy.
 *
 * @author smartloli.
 * <p>
 * Created by Sep 11, 2020
 */
public class KSqlStrategy extends BaseProtocol {

    private String jobId;
    private String cluster;
    private String sql;
    private String topic;
    private int partition;
    private List<Integer> partitions = new ArrayList<>();
    private List<String> columns = new ArrayList<>();
    private long start;
    private long end;
    private long limit = 0L;

    /**
     * KSQL combined filters of multiple fields.
     *
     * <code>
     * select * from topic where `partition` in (0) and msg like 'kafka-eagle%' and msg like 'smartloli%' and timespan > 1599754181586 limit 10;
     * </code>
     * <p>
     * So, filters include [{"like":"kafka-eagle",">":"1599754181586"},{"like":"smartloli"}]
     */
    private List<FieldSchemaStrategy> fieldSchema = new ArrayList<>();

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

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

    public List<Integer> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<Integer> partitions) {
        this.partitions = partitions;
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

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public List<FieldSchemaStrategy> getFieldSchema() {
        return fieldSchema;
    }

    public void setFieldSchema(List<FieldSchemaStrategy> fieldSchema) {
        this.fieldSchema = fieldSchema;
    }
}
