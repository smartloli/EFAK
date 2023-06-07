/**
 * NetUtils.java
 * <p>
 * Copyright 2023 smartloli
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kafka.eagle.common.utils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.common.constants.KConstants;

import java.io.IOException;
import java.net.*;

/**
 * Network detection tools.
 *
 * @Author: smartloli
 * @Date: 2023/6/7 15:19
 * @Version: 3.4.0
 */
@Slf4j
public class NetUtil {
    /**
     * Check server host & port whether normal.
     */
    public static boolean telnet(String host, int port) {
        Socket socket = new Socket();
        try {
            socket.setReceiveBufferSize(KConstants.ServerDevice.BUFFER_SIZE);
            socket.setSoTimeout(KConstants.ServerDevice.TIME_OUT);
        } catch (Exception e) {
            log.error("Socket create failed, msg is {}", e);
        }
        SocketAddress address = new InetSocketAddress(host, port);
        try {
            socket.connect(address, KConstants.ServerDevice.TIME_OUT);
            return true;
        } catch (IOException e) {
            log.error("Telnet [" + host + ":" + port + "] has crash, please check it.");
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    log.error("Close socket [" + host + ":" + port + "] has error.");
                }
            }
        }
    }

    public static boolean uri(String uriStr) {
        URI uri = URI.create(uriStr);
        if (uri == null || StrUtil.isBlank(uri.getHost())) {
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
            return address.isReachable(KConstants.ServerDevice.TIME_OUT);
        } catch (Exception e) {
            log.error("Ping [" + host + "] server has crash or not exist.");
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
            log.error("Get local server ip has error, msg is {}", e);
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
            log.error("Get local server ip has error, msg is {}", e);
        }
        return ip;
    }
}
