/**
 * KafkaClusterFetcher.java
 * <p>
 * Copyright 2025 smartloli
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
package org.kafka.eagle.core.api;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.core.constant.JmxMetricsConst;
import org.kafka.eagle.core.util.NetUtils;
import org.kafka.eagle.core.util.StrUtils;
import org.kafka.eagle.dto.broker.BrokerInfo;
import org.kafka.eagle.dto.jmx.JMXInitializeInfo;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.StringJoiner;

/**
 * <p>
 * 通过 JMX 获取 Kafka 集群中 Broker 与 Topic 的关键指标与详情信息，支持统一的连接与资源管理。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/27 14:57:17
 * @version 5.0.0
 */
@Slf4j
public class KafkaClusterFetcher {

    private KafkaClusterFetcher() {
    }

    /**
     * 将 BrokerInfo 列表转换为以逗号分隔的 "host:port" 字符串。
     */
    public static String formatBrokerList(List<BrokerInfo> brokerInfos) {
        if (brokerInfos == null || brokerInfos.isEmpty()) {
            return "";
        }
        StringJoiner joiner = new StringJoiner(",");
        for (BrokerInfo brokerInfo : brokerInfos) {
            joiner.add(brokerInfo.getHostIp() + ":" + brokerInfo.getPort());
        }
        return joiner.toString();
    }

    /**
     * 通过 JMX 获取 Broker 详细信息。
     */
    public static BrokerInfo fetchBrokerDetails(JMXInitializeInfo initializeInfo) {
        BrokerInfo brokerInfo = new BrokerInfo();
        executeJmxOperation(initializeInfo, mbeanConnection -> {
            try {
                brokerInfo.setHostIp(initializeInfo.getHost());
                brokerInfo.setJmxPort(initializeInfo.getPort());

                // 获取 Broker 版本与启动时间
                String version = mbeanConnection.getAttribute(
                        new ObjectName(String.format(JmxMetricsConst.Server.BROKER_APP_INFO.key(), initializeInfo.getBrokerId())),
                        JmxMetricsConst.Server.BROKER_VERSION_VALUE.key()
                ).toString();

                String startTimeMs = mbeanConnection.getAttribute(
                        new ObjectName(String.format(JmxMetricsConst.Server.BROKER_APP_INFO.key(), initializeInfo.getBrokerId())),
                        JmxMetricsConst.Server.BROKER_STARTTIME_VALUE.key()
                ).toString();

                // 获取 CPU 使用率
                String cpuStr = mbeanConnection.getAttribute(
                        new ObjectName(String.format(JmxMetricsConst.System.JMX_PERFORMANCE_TYPE.key(), initializeInfo.getBrokerId())),
                        JmxMetricsConst.System.PROCESS_CPU_LOAD.key()
                ).toString();

                // 获取内存使用情况
                MemoryMXBean memBean = ManagementFactory.newPlatformMXBeanProxy(
                        mbeanConnection, ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class
                );
                long memUsed = memBean.getHeapMemoryUsage().getUsed();
                long memMax = memBean.getHeapMemoryUsage().getMax();

                brokerInfo.setVersion(version);
                brokerInfo.setStartupTime(convertEpochToLocalDateTime(Long.parseLong(startTimeMs)));
                brokerInfo.setCpuUsage(BigDecimal.valueOf(StrUtils.numberic(String.valueOf(Double.parseDouble(cpuStr) * 100))));
                brokerInfo.setMemoryUsage(BigDecimal.valueOf(StrUtils.numberic(String.valueOf(memUsed * 100.0 / memMax))));
            } catch (Exception e) {
                log.error("获取 Kafka JMX Broker 详情失败：{}", initializeInfo, e);
            }
        });
        return brokerInfo;
    }

    /**
     * 通过 JMX 获取 Topic 的记录条数。
     */
    public static Long fetchTopicRecordCount(JMXInitializeInfo initializeInfo) {
        final long[] recordCount = {0L};
        executeJmxOperation(initializeInfo, mbeanConnection -> {
            try {
                Object size = mbeanConnection.getAttribute(
                        new ObjectName(initializeInfo.getObjectName()),
                        JmxMetricsConst.Log.VALUE.key()
                );
                recordCount[0] = Long.parseLong(size.toString());
            } catch (Exception e) {
                log.error("获取 Kafka Topic 记录条数失败：{}", initializeInfo, e);
            }
        });
        return recordCount[0];
    }

    /**
     * 安全地执行 JMX 操作并自动管理连接与资源关闭。
     */
    private static void executeJmxOperation(JMXInitializeInfo initializeInfo, JMXOperation operation) {
        if (!NetUtils.telnet(initializeInfo.getHost(), initializeInfo.getPort())) {
            return;
        }

        JMXConnector connector = null;
        try {
            JMXServiceURL jmxUrl = new JMXServiceURL(String.format(initializeInfo.getUri(), initializeInfo.getHost() + ":" + initializeInfo.getPort()));
            initializeInfo.setUrl(jmxUrl);
            connector = JmxConnectionManager.connectWithTimeout(initializeInfo);
            operation.execute(connector.getMBeanServerConnection());
        } catch (Exception e) {
            log.error("执行 JMX 操作出错：{}", initializeInfo, e);
        } finally {
            if (connector != null) {
                try {
                    connector.close();
                } catch (IOException e) {
                    log.error("关闭 JMX 连接器失败：{}", e.getMessage());
                }
            }
        }
    }

    /**
     * 将时间戳（毫秒）转换为 LocalDateTime。
     */
    private static LocalDateTime convertEpochToLocalDateTime(long timestamp) {
        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    @FunctionalInterface
    private interface JMXOperation {
        void execute(MBeanServerConnection mbeanConnection) throws Exception;
    }
}
