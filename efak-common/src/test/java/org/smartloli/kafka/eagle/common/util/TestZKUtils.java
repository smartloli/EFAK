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

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.data.Stat;
import org.smartloli.kafka.eagle.common.serializer.EFAKZkSerializer;

import java.util.List;

/**
 * TODO
 *
 * @author smartloli.
 * <p>
 * Created by Oct 28, 2018
 */
public class TestZKUtils {

    public static void main(String[] args) {
        for (String clusterAlias : SystemConfigUtils.getPropertyArray("efak.zk.cluster.alias", ",")) {
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
                List<String> ids = zkClient.getChildren(KConstants.Kafka.BROKER_IDS_PATH);
                for (String id : ids) {
                    String path = KConstants.Kafka.BROKER_IDS_PATH + "/" + id;
                    if (zkClient.exists(path)) {
                        Object obj = zkClient.readData(path);
                        Stat stat = new Stat();
                        Object obj2 = zkClient.readData(path, stat);
                        System.out.println("result: " + obj2);
                        System.out.println("result: " + stat);
                        // listen broker status
                        zkClient.subscribeDataChanges(path, new IZkDataListener() {
                            @Override
                            public void handleDataChange(String dataPath, Object data) throws Exception {

                            }

                            @Override
                            public void handleDataDeleted(String s) throws Exception {

                            }
                        });
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (zkClient != null) {
                    zkClient.close();
                }
            }
        }
    }

}
