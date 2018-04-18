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
package org.smartloli.kafka.eagle.common.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.util.KConstants.ServerDevice;

/**
 * Check whether the corresponding port on the server can be accessed.
 * 
 * @author smartloli.
 *
 *         Created by Mar 16, 2018
 */
public class NetUtils {

	private static final Logger LOG = LoggerFactory.getLogger(NetUtils.class);

	/** Check server host & port whether normal. */
	public static boolean telnet(String host, int port) {
		Socket socket = new Socket();
		try {
			socket.setReceiveBufferSize(ServerDevice.BUFFER_SIZE);
			socket.setSoTimeout(ServerDevice.TIME_OUT);
		} catch (Exception ex) {
			LOG.error("Socket create failed.");
		}
		SocketAddress address = new InetSocketAddress(host, port);
		try {
			socket.connect(address, ServerDevice.TIME_OUT);
			return true;
		} catch (IOException e) {
			LOG.error("Telnet [" + host + ":" + port + "] has crash,please check it.");
			return false;
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					LOG.error("Close socket [" + host + ":" + port + "] has error.");
				}
			}
		}
	}

	/** Ping server whether alive. */
	public static boolean ping(String host) {
		try {
			InetAddress address = InetAddress.getByName(host);
			return address.isReachable(ServerDevice.TIME_OUT);
		} catch (Exception e) {
			LOG.error("Ping [" + host + "] server has crash or not exist.");
			return false;
		}
	}

}
