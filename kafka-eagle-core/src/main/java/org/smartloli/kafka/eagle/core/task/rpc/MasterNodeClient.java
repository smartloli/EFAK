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
package org.smartloli.kafka.eagle.core.task.rpc;

import com.alibaba.fastjson.JSONObject;
import org.apache.thrift.TConfiguration;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.layered.TFramedTransport;
import org.smartloli.kafka.eagle.common.util.ErrorUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;

/**
 * It is used to receive the tasks sent by the client,
 * assign the tasks to the specified workers,
 * and summarize the calculation results of the workers.
 * It is the general controller of a task.
 *
 * @author smartloli.
 * <p>
 * Created by Sep 16, 2020
 */
public class MasterNodeClient {
    private MasterNodeClient() {

    }

    /**
     * Get worknode server metrics result.
     */
    public static String getResult(String host, int port, JSONObject object) {
        int timeout = SystemConfigUtils.getIntProperty("kafka.eagle.sql.worknode.rpc.timeout");
        TTransport transport = null;
        try {
            transport = new TFramedTransport(new TSocket(new TConfiguration(), host, port, timeout));
        } catch (TTransportException e) {
            e.printStackTrace();
            ErrorUtils.print(MasterNodeClient.class).error("TSocket connect from worknode[" + host + ":" + port + "] has error, msg is ", e);
        }
        TProtocol protocol = new TCompactProtocol(transport);
        WorkNodeService.Client client = new WorkNodeService.Client(protocol);
        String result = "";
        try {
            transport.open();
            result = client.getResult(object.toJSONString());
        } catch (Exception e) {
            ErrorUtils.print(MasterNodeClient.class).error("Get result from worknode[" + host + ":" + port + "] has error, msg is ", e);
        } finally {
            transport.close();
        }
        return result;
    }
}
