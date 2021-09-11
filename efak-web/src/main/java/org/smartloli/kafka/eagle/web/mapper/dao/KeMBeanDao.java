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
package org.smartloli.kafka.eagle.web.mapper.dao;

import org.smartloli.kafka.eagle.common.protocol.KpiInfo;
import org.smartloli.kafka.eagle.common.protocol.MBeanOfflineInfo;
import org.smartloli.kafka.eagle.common.protocol.bscreen.BScreenConsumerInfo;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicOffsetsInfo;

import java.util.List;
import java.util.Map;

/**
 * MBeanDao interface definition
 *
 * @author smartloli.
 * <p>
 * Created by Apr 11, 2021
 */
public interface KeMBeanDao extends KeBaseMapper {

    /**
     * Collection statistics data from kafka jmx & insert into table.
     */
    public int insert(List<KpiInfo> kpi);

    /**
     * Collection statistics offline data from kafka jmx & insert into table.
     */
    public int mbeanOfflineInsert(List<MBeanOfflineInfo> kpi);

    /**
     * Query collector data.
     */
    public List<KpiInfo> query(Map<String, Object> params);

    /**
     * Query collector data.
     */
    public List<MBeanOfflineInfo> getMBeanOffline(Map<String, Object> params);

    /**
     * Get broker kpi, such bytein or byteout etc.
     */
    public KpiInfo getBrokersKpi(Map<String, Object> params);

    /**
     * Crontab clean data.
     */
    public void remove(int tm);

    /**
     * Get consumer offset topic metrics.
     */
    public List<BScreenConsumerInfo> getConsumerOffsetsTopic(Map<String, Object> params);

    /**
     * Get consumer rate topic metrics.
     */
    public List<TopicOffsetsInfo> getConsumerRateTopic(Map<String, Object> params);

    /**
     * Query os memory data.
     */

    public List<KpiInfo> getOsMem(Map<String, Object> params);

    /**
     * Query used cpu data.
     */
    public List<KpiInfo> getUsedCPU(Map<String, Object> params);

}
