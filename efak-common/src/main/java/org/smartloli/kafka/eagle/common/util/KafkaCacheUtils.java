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
package org.smartloli.kafka.eagle.common.util;

import com.alibaba.fastjson.JSON;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.data.Stat;
import org.smartloli.kafka.eagle.common.protocol.BrokersInfo;
import org.smartloli.kafka.eagle.common.protocol.cache.BrokerCache;
import org.smartloli.kafka.eagle.common.serializer.EFAKZkSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Cache the Kafka metadata information in zookeeper into memory.
 *
 * @author smartloli.
 * <p>
 * Created by Jun 02, 2022
 */
public class KafkaCacheUtils {

    public static void initKafkaMetaData() {

        try {
            for (String clusterAlias : SystemConfigUtils.getPropertyArray("efak.zk.cluster.alias", ",")) {
                List<BrokersInfo> targets = new ArrayList<BrokersInfo>();
                String zkServers = SystemConfigUtils.getProperty(clusterAlias + ".zk.list");
                ZkClient zkClient = null;
                try {
                    zkClient = new ZkClient(zkServers);
                    zkClient.setZkSerializer(new EFAKZkSerializer());
                    if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".zk.acl.enable")) {
                        String schema = SystemConfigUtils.getProperty(clusterAlias + ".zk.acl.schema");
                        String username = SystemConfigUtils.getProperty(clusterAlias + ".zk.acl.username");
                        String password = SystemConfigUtils.getProperty(clusterAlias + ".zk.acl.password");
                        zkClient.addAuthInfo(schema, (username + ":" + password).getBytes());
                    }
                    // load broker cache
                    List<String> brokerIdss = zkClient.getChildren(KConstants.Kafka.BROKER_IDS_PATH);
                    for (String ids : brokerIdss) {
                        String path = KConstants.Kafka.BROKER_IDS_PATH + "/" + ids;
                        if (zkClient.exists(path)) {
                            Stat stat = new Stat();
                            String result = zkClient.readData(path, stat);
                            // load
                            refreshKafkaMetaData(targets, stat, clusterAlias, result, ids);
                            // listen broker status
                            zkClient.subscribeDataChanges(path, new IZkDataListener() {
                                @Override
                                public void handleDataChange(String dataPath, Object data) throws Exception {
                                    // refresh
                                    refreshKafkaMetaData(targets, stat, clusterAlias, data.toString(), ids);
                                }

                                @Override
                                public void handleDataDeleted(String path) throws Exception {
                                    // delete data
                                    String[] tmpZnode = path.split("/");
                                    String ids = tmpZnode[tmpZnode.length - 1];
                                    // remove offline broker
                                    removeOfflineBrokerMetaData(clusterAlias, ids);

                                }
                            });
                        }
                    }

                } catch (Exception ex) {
                    LoggerUtils.print(KafkaCacheUtils.class).error("Get broker info for zookeeper has error, msg is ", ex);
                } finally {
                    if (zkClient != null) {
                        zkClient.close();
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtils.print(KafkaCacheUtils.class).error("Load kafka metadata into cache has error, msg is ", e);
        }
    }

    private static void removeOfflineBrokerMetaData(String clusterAlias, String ids) {
        List<BrokersInfo> brokersInfos = BrokerCache.META_CACHE.get(clusterAlias);
        IntStream.range(0, brokersInfos.size()).filter(i ->
                brokersInfos.get(i).getIds().equals(ids)).
                boxed().findFirst().map(i -> brokersInfos.remove((int) i));
    }

    private static void refreshKafkaMetaData(List<BrokersInfo> targets, Stat stat, String clusterAlias, String data, String ids) {
        BrokersInfo broker = new BrokersInfo();
        broker.setCreated(CalendarUtils.convertUnixTime2Date(stat.getCtime()));
        broker.setModify(CalendarUtils.convertUnixTime2Date(stat.getMtime()));
        if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".efak.sasl.enable") || SystemConfigUtils.getBooleanProperty(clusterAlias + ".efak.ssl.enable")) {
            String endpoints = JSON.parseObject(data).getString("endpoints");
            List<String> endpointsList = JSON.parseArray(endpoints, String.class);
            String host = "";
            int port = 0;
            if (endpointsList.size() > 1) {
                String protocol = "";
                if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".efak.sasl.enable")) {
                    protocol = KConstants.Kafka.SASL_PLAINTEXT;
                }
                if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".efak.ssl.enable")) {
                    protocol = KConstants.Kafka.SSL;
                }
                for (String endpointsStr : endpointsList) {
                    /**
                     * add support format demo：
                     * listener.security.protocol.map=CLIENT:SASL_PLAINTEXT,EXTERNAL:SASL_PLAINTEXT
                     * advertised.listeners=CLIENT://192.168.56.11:9092,EXTERNAL://192.168.55.11:9093
                     * sasl.mechanism.inter.broker.protocol=plain
                     * sasl_mechanism_inter_broker_protocol=plain
                     * inter.broker.listener.name=CLIENT
                     */
                    if (endpointsStr.contains(protocol) || endpointsStr.contains("INTERNAL") || endpointsStr.contains("CLIENT")) {
                        String tmp = endpointsStr.split("//")[1];
                        host = tmp.split(":")[0];
                        port = Integer.parseInt(tmp.split(":")[1]);
                        break;
                    }
                }
            } else {
                if (endpointsList.size() > 0) {
                    String tmp = endpointsList.get(0).split("//")[1];
                    host = tmp.split(":")[0];
                    port = Integer.parseInt(tmp.split(":")[1]);
                }
            }
            broker.setHost(host);
            broker.setPort(port);
        } else {
            String host = JSON.parseObject(data).getString("host");
            int port = JSON.parseObject(data).getInteger("port");
            broker.setHost(host);
            broker.setPort(port);
        }
        broker.setJmxPort(JSON.parseObject(data).getInteger("jmx_port"));
        broker.setIds(ids);
        try {
            broker.setJmxPortStatus(NetUtils.telnet(broker.getHost(), broker.getJmxPort()));
        } catch (Exception e) {
            LoggerUtils.print(KafkaCacheUtils.class).error("Telnet [" + broker.getHost() + ":" + broker.getJmxPort() + "] has error, msg is ", e);
        }

        // lrucache key: clusterAlias
        targets.add(broker);
        BrokerCache.META_CACHE.put(clusterAlias, targets);
    }

}
