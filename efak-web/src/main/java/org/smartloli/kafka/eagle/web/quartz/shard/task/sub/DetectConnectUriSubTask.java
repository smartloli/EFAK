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
package org.smartloli.kafka.eagle.web.quartz.shard.task.sub;

import org.smartloli.kafka.eagle.common.protocol.plugins.ConnectConfigInfo;
import org.smartloli.kafka.eagle.common.util.*;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.MetricsServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collect kafka connect rest service dataset.
 *
 * @author smartloli.
 * <p>
 * Created by Dec 09, 2021
 */
public class DetectConnectUriSubTask extends Thread {

    @Override
    public synchronized void run() {
        try {
            if (SystemConfigUtils.getBooleanProperty("efak.metrics.charts")) {
                String[] clusterAliass = SystemConfigUtils.getPropertyArray("efak.zk.cluster.alias", ",");
                for (String clusterAlias : clusterAliass) {
                    this.detectConnectUri(clusterAlias);
                }
            }
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).error("Get kafka connect uri has error, msg is ", e);
        }
    }

    /**
     * Whether kafka connect uri is alive by detected.
     */
    private void detectConnectUri(String clusterAlias) {
        MetricsServiceImpl metrics = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
        Map<String, Object> params = new HashMap<>();
        params.put("cluster", clusterAlias);
        List<ConnectConfigInfo> connectUris = metrics.detectConnectConfigList(params);
        for (ConnectConfigInfo configInfo : connectUris) {
            try {
                if (NetUtils.uri(configInfo.getConnectUri())) {
                    configInfo.setAlive(KConstants.BrokerSever.CONNECT_URI_ALIVE);
                } else {
                    configInfo.setAlive(KConstants.BrokerSever.CONNECT_URI_SHUTDOWN);
                }
            } catch (Exception e) {
                LoggerUtils.print(this.getClass()).error("Get kafka connect uri alive or shutdown has error, msg is ", e);
            }
            configInfo.setModify(CalendarUtils.getDate());
            try {
                metrics.modifyConnectConfigStatusById(configInfo);
            } catch (Exception e) {
                LoggerUtils.print(this.getClass()).error("Update kafka connect uri alive or shutdown has error, msg is ", e);
            }
        }
    }
}
