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
package org.smartloli.kafka.eagle.core.task.rpc.server;

import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.layered.TFramedTransport;
import org.smartloli.kafka.eagle.common.util.ErrorUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.task.rpc.WorkNodeService;
import org.smartloli.kafka.eagle.core.task.rpc.handler.WorkNodeServiceHandler;

/**
 * It is the object of true calculation and returns the calculation
 * result to a container of master.
 *
 * @author smartloli.
 * <p>
 * Created by Sep 16, 2020
 */
public class WorkNodeServer {
    private void start(int port) throws TTransportException {
        TNonblockingServerSocket socket = new TNonblockingServerSocket(port);
        final WorkNodeService.Processor processor = new WorkNodeService.Processor(new WorkNodeServiceHandler());
        THsHaServer.Args arg = new THsHaServer.Args(socket);
        /**
         * Binary coded format efficient, intensive data transmission, The use
         * of non blocking mode of transmission, according to the size of the
         * block, similar to the Java of NIO.
         */
        arg.protocolFactory(new TCompactProtocol.Factory());
        arg.transportFactory(new TFramedTransport.Factory());
        arg.processorFactory(new TProcessorFactory(processor));
        TServer server = new THsHaServer(arg);
        server.serve();
    }

    public static void main(String[] args) {
        try {
            int port = SystemConfigUtils.getIntProperty("kafka.eagle.sql.worknode.port");
            ErrorUtils.print(WorkNodeServer.class).info(WorkNodeServer.class.getSimpleName() + " started and listening from master task, port is [" + port + "].");
            WorkNodeServer rpc = new WorkNodeServer();
            rpc.start(port);
        } catch (Exception e) {
            ErrorUtils.print(WorkNodeServer.class).error("Start WorkNodeServer has error, msg is ", e);
        }
    }
}
