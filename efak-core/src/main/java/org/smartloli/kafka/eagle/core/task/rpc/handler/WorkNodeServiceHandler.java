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
package org.smartloli.kafka.eagle.core.task.rpc.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.thrift.TException;
import org.smartloli.kafka.eagle.common.util.*;
import org.smartloli.kafka.eagle.core.task.cache.LogCacheFactory;
import org.smartloli.kafka.eagle.core.task.rpc.WorkNodeService;
import org.smartloli.kafka.eagle.core.task.shard.ScheduleShardStrategy;
import org.smartloli.kafka.eagle.core.task.shard.ShardSubScan;
import org.smartloli.kafka.eagle.core.task.strategy.KSqlStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Receive and execute the assigned tasks of master.
 *
 * @author smartloli.
 * <p>
 * Created by Sep 16, 2020
 */
public class WorkNodeServiceHandler implements WorkNodeService.Iface {

    private KSqlStrategy ksql;
    private String type;
    private String jobId;
    private String key; // used by quartz ip key
    private String cluster;

    @Override
    public String getResult(String jsonObject) throws TException {
        if (JSONUtils.isJsonObject(jsonObject)) {
            JSONObject object = JSON.parseObject(jsonObject);
            if (object.getString(KConstants.Protocol.KEY).equals(KConstants.Protocol.HEART_BEAT)) {
                this.type = KConstants.Protocol.HEART_BEAT;
                this.cluster = object.getString(KConstants.Protocol.CLUSTER_NAME);
            } else if (object.getString(KConstants.Protocol.KEY).equals(KConstants.Protocol.KSQL_QUERY)) {
                this.type = KConstants.Protocol.KSQL_QUERY;
                this.ksql = object.getObject(KConstants.Protocol.VALUE, KSqlStrategy.class);
            } else if (object.getString(KConstants.Protocol.KEY).equals(KConstants.Protocol.KSQL_QUERY_LOG)) {
                this.type = KConstants.Protocol.KSQL_QUERY_LOG;
                this.jobId = object.getString(KConstants.Protocol.JOB_ID);
            } else if (object.getString(KConstants.Protocol.KEY).equals(KConstants.Protocol.SHARD_TASK)) {
                this.type = KConstants.Protocol.SHARD_TASK;
                this.key = object.getString(KConstants.Protocol.KEY_BY_IP);
            }
            return handler();
        }
        return "";
    }

    public String handler() {
        String result = "";
        if (KConstants.Protocol.HEART_BEAT.equals(this.type)) {//
            JSONObject object = new JSONObject();
            String memory = "<span class='badge bg-light-danger text-danger'>NULL</span>";
            long used = AppUtils.getInstance().getProcessMemUsed();
            long max = AppUtils.getInstance().getProcessMemMax();
            String percent = StrUtils.stringify(used) + " (" + StrUtils.numberic((used * 100.0 / max) + "") + "%)";
            if ((used * 100.0) / max < KConstants.BrokerSever.MEM_NORMAL) {
                memory = "<span class='badge bg-light-success text-success'>" + percent + "</span>";
            } else if ((used * 100.0) / max >= KConstants.BrokerSever.MEM_NORMAL && (used * 100.0) / max < KConstants.BrokerSever.MEM_DANGER) {
                memory = "<span class='badge bg-light-warning text-warning'>" + percent + "</span>";
            } else if ((used * 100.0) / max >= KConstants.BrokerSever.MEM_DANGER) {
                memory = "<span class='badge bg-light-danger text-danger'>" + percent + "</span>";
            }
            object.put("memory", memory);

            // get zkclient pool size
            int zkLimitSize = SystemConfigUtils.getIntProperty("kafka.zk.limit.size");
            int zkCliPoolSize = KafkaZKSingletonUtils.getZkCliPoolSize(this.cluster);
            int zkCliIdle = zkLimitSize - zkCliPoolSize;
            String zkCliSize = "";
            String zkCliStr = zkLimitSize + " | " + zkCliPoolSize;
            if (zkCliIdle < 0) {
                zkCliSize = "<span class='badge bg-light-danger text-danger'>" + zkCliStr + "</span>";
            } else {
                if ((zkCliIdle * 100.0) / zkLimitSize < KConstants.BrokerSever.MEM_NORMAL) {
                    zkCliSize = "<span class='badge bg-light-success text-success'>" + zkCliStr + "</span>";
                } else if ((zkCliIdle * 100.0) / zkLimitSize >= KConstants.BrokerSever.MEM_NORMAL && (zkCliIdle * 100.0) / zkLimitSize < KConstants.BrokerSever.MEM_DANGER) {
                    zkCliSize = "<span class='badge bg-light-warning text-warning'>" + zkCliStr + "</span>";
                } else if ((zkCliIdle * 100.0) / zkLimitSize >= KConstants.BrokerSever.MEM_DANGER) {
                    zkCliSize = "<span class='badge bg-light-danger text-danger'>" + zkCliStr + "</span>";
                }
            }

            object.put("zkcli", zkCliSize);

            object.put("cpu", "<span class='badge bg-secondary'>" + AppUtils.getInstance().getProcessCpu() + "%</span>");
            object.put("created", AppUtils.getInstance().getStartTime());
            JSONArray array = new JSONArray();
            array.add(object);
            List<JSONArray> results = new ArrayList<>();
            results.add(array);
            result = results.toString();
        } else if (KConstants.Protocol.KSQL_QUERY.equals(this.type)) {
            if (this.ksql != null) {
                result = ShardSubScan.query(ksql).toString();
            }
        } else if (KConstants.Protocol.KSQL_QUERY_LOG.equals(this.type)) {
            if (!StrUtils.isNull(this.jobId)) {
                if (LogCacheFactory.LOG_RECORDS.containsKey(this.jobId)) {
                    String log = LogCacheFactory.LOG_RECORDS.get(this.jobId).toString();
                    JSONObject object = new JSONObject();
                    object.put("log", log);
                    JSONArray array = new JSONArray();
                    array.add(object);
                    List<JSONArray> results = new ArrayList<>();
                    results.add(array);
                    result = results.toString();
                }
            }
        } else if (KConstants.Protocol.SHARD_TASK.equals(this.type)) {
            long stime = System.currentTimeMillis();
            Map<String, List<String>> shardTasks = ScheduleShardStrategy.getScheduleShardTask();
            LoggerUtils.print(this.getClass()).info("All shard task strategy, result: " + JSON.toJSONString(shardTasks));
            if (shardTasks.containsKey(this.key)) {
                result = JSON.toJSONString(shardTasks.get(this.key));
            }
            LoggerUtils.print(this.getClass()).info("Spent time [" + (System.currentTimeMillis() - stime) + "]ms, worknode[" + this.key + "] get task: " + result);
        }
        return result;
    }
}
