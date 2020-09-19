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

import org.smartloli.kafka.eagle.common.protocol.plugins.ConnectConfigInfo;

import java.util.List;
import java.util.Map;

/**
 * Define a kafka connect interface to operate.
 *
 * @author smartloli.
 * <p>
 * Created by Aug 30, 2020
 */
public interface ConnectService {

    /**
     * Storage or modify kafka connect uri schema.
     */
    public int insertOrUpdateConnectConfig(ConnectConfigInfo connectConfig);

    /**
     * Get kafka connect uri schema from database table.
     */
    public List<ConnectConfigInfo> getConnectConfigList(Map<String, Object> params);

    /**
     * Get kafka connect uri schema numbers from database table for page.
     */
    public int connectConfigCount(Map<String, Object> params);

    /**
     * Delete kafka connect uri schema from database table.
     */
    public int deleteConnectConfigById(Map<String, Object> params);

    /**
     * Modify kafka connect uri config by id.
     */
    public int modifyConnectConfigById(ConnectConfigInfo connectConfig);

    /**
     * Find kafka connect uri schema by id.
     */
    public ConnectConfigInfo findConnectUriById(int id);

    /**
     * Get connectors table list.
     */
    public List<String> getConnectorsTableList(String uri, String search);

    /**
     * Connector alive status.
     */
    public boolean connectorHasAlive(String uri);

    /**
     * Get connector plugins summary and return JSONObject result.
     */
    public String getConnectorPluginsSummary(String uri, String connector);

}
