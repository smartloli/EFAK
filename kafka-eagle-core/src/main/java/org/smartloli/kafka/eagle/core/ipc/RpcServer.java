/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.core.ipc;

import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;

/**
 * Providing rpc services inside access to offset in kafka.
 * 
 * @author smartloli.
 *
 *         Created by Jan 5, 2017
 */
public class RpcServer {
	private static Logger LOG = LoggerFactory.getLogger(RpcServer.class);

	private void start(int port) throws TTransportException {
		TNonblockingServerSocket socket = new TNonblockingServerSocket(port);
		final OffsetMetadataServer.Processor processor = new OffsetMetadataServer.Processor(new OffsetMetadataServerImpl());
		THsHaServer.Args arg = new THsHaServer.Args(socket);
		/**
		 * Binary coded format efficient, intensive data transmission, The use
		 * of non blocking mode of transmission, according to the size of the
		 * block, similar to the Java of NIO
		 */
		arg.protocolFactory(new TCompactProtocol.Factory());
		arg.transportFactory(new TFramedTransport.Factory());
		arg.processorFactory(new TProcessorFactory(processor));
		TServer server = new THsHaServer(arg);
		server.serve();
	}

	public static void main(String[] args) {
		String formatter = SystemConfigUtils.getProperty("kafka.eagle.offset.storage");
		if ("kafka".equals(formatter)) {
			try {
				KafkaOffsetGetter.getInstance();
				LOG.info("Start KafkaOffsetGetter thrift server.");
				int port = SystemConfigUtils.getIntProperty("kafka.eagle.offset.rpc.port");
				RpcServer rpc = new RpcServer();
				rpc.start(port);
			} catch (Exception ex) {
				LOG.error("Initialize KafkaOffsetGetter thrift server has error,msg is " + ex.getMessage());
			}
		}
	}
}
