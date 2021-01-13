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
package org.smartloli.kafka.eagle.web.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.common.protocol.plugins.ConnectConfigInfo;
import org.smartloli.kafka.eagle.common.util.ErrorUtils;
import org.smartloli.kafka.eagle.common.util.HttpClientUtils;
import org.smartloli.kafka.eagle.common.util.NetUtils;
import org.smartloli.kafka.eagle.common.util.StrUtils;
import org.smartloli.kafka.eagle.web.dao.BrokerDao;
import org.smartloli.kafka.eagle.web.service.ConnectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handle the logic of the action kafka connect.
 *
 * @author smartloli.
 * <p>
 * Created by Aug 30, 2020
 */
@Service
public class ConnectServiceImpl implements ConnectService {

    @Autowired
    private BrokerDao brokerDao;

    @Override
    public int insertOrUpdateConnectConfig(ConnectConfigInfo connectConfig) {
        return brokerDao.insertOrUpdateConnectConfig(connectConfig);
    }

    @Override
    public List<ConnectConfigInfo> getConnectConfigList(Map<String, Object> params) {
        return brokerDao.getConnectConfigList(params);
    }

    @Override
    public int connectConfigCount(Map<String, Object> params) {
        return brokerDao.connectConfigCount(params);
    }

    @Override
    public int deleteConnectConfigById(Map<String, Object> params) {
        return brokerDao.deleteConnectConfigById(params);
    }

    @Override
    public int modifyConnectConfigById(ConnectConfigInfo connectConfig) {
        return brokerDao.modifyConnectConfigById(connectConfig);
    }

    @Override
    public ConnectConfigInfo findConnectUriById(int id) {
        return brokerDao.findConnectUriById(id);
    }

    @Override
    public List<String> getConnectorsTableList(String uri, String search) {
        String uriStr = "";
        if (!uri.endsWith("/")) {
            uriStr = uri + "/";
        } else {
            uriStr = uri;
        }
        List<String> connectors = StrUtils.stringListConvertListStrings(HttpClientUtils.doGet(uriStr + "connectors"));
        List<String> result = new ArrayList<>();
        if (!StrUtils.isNull(search)) {
            for (String connector : connectors) {
                if (connector.contains(search)) {
                    result.add(connector);
                }
            }
        } else {
            result = connectors;
        }
        return result;
    }

    @Override
    public boolean connectorHasAlive(String uri) {
        return NetUtils.uri(uri);
    }

    @Override
    public String getConnectorPluginsSummary(String uri, String connector) {
        String uriStr = "";
        if (!uri.endsWith("/")) {
            uriStr = uri + "/";
        } else {
            uriStr = uri;
        }
        JSONObject object = new JSONObject();
        String status = "";
        String config = "";
        String tasks = "";
        try {
            status = HttpClientUtils.doGet(uriStr + "connectors/" + connector + "/status");
        } catch (Exception e) {
            ErrorUtils.print(this.getClass()).error("Get connector[" + uriStr + connector + "] status has error, msg is ", e);
        }
        try {
            config = HttpClientUtils.doGet(uriStr + "connectors/" + connector + "/config");
        } catch (Exception e) {
            ErrorUtils.print(this.getClass()).error("Get connector[" + uriStr + connector + "] config has error, msg is ", e);
        }
        try {
            tasks = HttpClientUtils.doGet(uriStr + "connectors/" + connector + "/tasks");
        } catch (Exception e) {
            ErrorUtils.print(this.getClass()).error("Get connector[" + uriStr + connector + "] tasks has error, msg is ", e);
        }
        object.put("status", status);
        object.put("config", config);
        object.put("tasks", tasks);
        return object.toString();
    }
}
