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
package org.smartloli.kafka.eagle.web.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Kafka & Zookeeper service api.
 *
 * @author smartloli.
 * <p>
 * Created by Jan 17, 2017.
 * <p>
 * Update by hexiang 20170216
 */
public interface ClusterService {

    /**
     * Execute zookeeper comand interface
     */
    public String execute(String clusterAlias, String cmd, String type);

    /**
     * Get Kafka & Zookeeper interface.
     */
    public String get(String clusterAlias, String type);

    /**
     * Get kafka version.
     */
    public String getKafkaVersion(String host, int jmxPort, String ids, String clusterAlias);

    /**
     * Get Zookkeeper status interface.
     */
    public JSONObject status(String clusterAlias);

    /**
     * Get multi cluster aliass interface.
     */
    public JSONArray clusterAliass();

    /**
     * Checked cluster alias is exist interface.
     */
    public boolean hasClusterAlias(String clusterAlias);

    /**
     * Get kafka used memory.
     */
    public String getUsedMemory(String clusterAlias, String host, int jmxPort);

    /**
     * Get kafka used cpu.
     */
    public String getUsedCpu(String clusterAlias, String host, int jmxPort);

}
