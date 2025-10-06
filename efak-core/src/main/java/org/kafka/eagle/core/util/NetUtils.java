package org.kafka.eagle.core.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

/**
 * <p>
 * 网络工具类，用于 Kafka 代理连接测试。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/6/22 18:48
 * @version 5.0.0
 */
@Slf4j
public class NetUtils {

    /**
     * 测试到 host:port 的 TCP 连接
     *
     * @param host 主机地址
     * @param port 端口号
     * @return 连接成功返回 true，否则返回 false
     */
    public static boolean telnet(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 5000); // 5秒超时
            log.debug("成功连接到 {}:{}", host, port);
            return true;
        } catch (IOException e) {
            log.debug("连接 {}:{} 失败 - {}", host, port, e.getMessage());
            return false;
        }
    }

    /**
     * 使用自定义超时测试到 host:port 的 TCP 连接
     *
     * @param host    主机地址
     * @param port    端口号
     * @param timeout 超时时间（毫秒）
     * @return 连接成功返回 true，否则返回 false
     */
    public static boolean telnet(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            log.debug("成功连接到 {}:{} 超时时间 {}", host, port, timeout);
            return true;
        } catch (IOException e) {
            log.debug("连接 {}:{} 失败，超时时间 {} - {}", host, port, timeout, e.getMessage());
            return false;
        }
    }

    /**
     * 获取本地服务器 IP 地址
     *
     * @return 本地 IP 地址的字符串表示，如果未找到则返回 null
     */
    public static String getLocalAddress() {
        try {
            // 尝试获取首选 IP 地址
            InetAddress inetAddress = getFirstNonLoopbackAddress();
            if (inetAddress != null) {
                return inetAddress.getHostAddress();
            }

            // 回退到本地主机
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("获取本地地址失败: ", e);
        } catch (SocketException e) {
            log.error("获取网络接口失败: ", e);
        }
        return null;
    }

    /**
     * 获取第一个非回环地址
     *
     * @return 第一个非回环地址的 InetAddress，如果未找到则返回 null
     * @throws SocketException
     */
    private static InetAddress getFirstNonLoopbackAddress() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && !address.isLinkLocalAddress() && address.isSiteLocalAddress()) {
                        return address;
                    }
                }
            }
        }
        return null;
    }

}