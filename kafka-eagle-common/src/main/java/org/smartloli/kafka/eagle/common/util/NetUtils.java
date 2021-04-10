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

import org.smartloli.kafka.eagle.common.util.KConstants.ServerDevice;

import java.io.IOException;
import java.net.*;

/**
 * Check whether the corresponding port on the server can be accessed.
 *
 * @author smartloli.
 * <p>
 * Created by Mar 16, 2018
 */
public class NetUtils {

    /**
     * Check server host & port whether normal.
     */
    public static boolean telnet(String host, int port) {
        Socket socket = new Socket();
        try {
            socket.setReceiveBufferSize(ServerDevice.BUFFER_SIZE);
            socket.setSoTimeout(ServerDevice.TIME_OUT);
        } catch (Exception ex) {
            ErrorUtils.print(NetUtils.class).error("Socket create failed.");
        }
        SocketAddress address = new InetSocketAddress(host, port);
        try {
            socket.connect(address, ServerDevice.TIME_OUT);
            return true;
        } catch (IOException e) {
            ErrorUtils.print(NetUtils.class).error("Telnet [" + host + ":" + port + "] has crash, please check it.");
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    ErrorUtils.print(NetUtils.class).error("Close socket [" + host + ":" + port + "] has error.");
                }
            }
        }
    }

    public static boolean uri(String uriStr) {
        URI uri = URI.create(uriStr);
        if (uri == null || StrUtils.isNull(uri.getHost())) {
            return false;
        }
        return telnet(uri.getHost(), uri.getPort());
    }

    /**
     * Ping server whether alive.
     */
    public static boolean ping(String host) {
        try {
            InetAddress address = InetAddress.getByName(host);
            return address.isReachable(ServerDevice.TIME_OUT);
        } catch (Exception e) {
            ErrorUtils.print(NetUtils.class).error("Ping [" + host + "] server has crash or not exist.");
            return false;
        }
    }

    /**
     * Get server ip.
     */
    public static String ip() {
        String ip = "";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            ErrorUtils.print(NetUtils.class).error("Get local server ip has error, msg is " + e.getMessage());
        }
        return ip;
    }

    /**
     * Get server hostname.
     */
    public static String hostname() {
        String ip = "";
        try {
            ip = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            ErrorUtils.print(NetUtils.class).error("Get local server ip has error, msg is " + e.getMessage());
        }
        return ip;
    }

}
