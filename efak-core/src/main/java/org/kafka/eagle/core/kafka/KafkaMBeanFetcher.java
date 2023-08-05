/**
 * KafkaMBeanFetcher.java
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
package org.kafka.eagle.core.kafka;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.common.constants.JmxConstants;
import org.kafka.eagle.common.constants.KConstants;
import org.kafka.eagle.common.constants.KConstants.MBean;
import org.kafka.eagle.common.utils.NetUtil;
import org.kafka.eagle.common.utils.StrUtils;
import org.kafka.eagle.plugins.kafka.JMXFactoryUtil;
import org.kafka.eagle.pojo.cluster.BrokerInfo;
import org.kafka.eagle.pojo.kafka.JMXInitializeInfo;
import org.kafka.eagle.pojo.topic.MBeanInfo;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/8/5 11:48
 * @Version: 3.4.0
 */
@Slf4j
public class KafkaMBeanFetcher {

    private KafkaMBeanFetcher() {
    }

    // 1:Available, 0:Not Available
    private static short getBrokerStatus(String host, int port) {
        return NetUtil.telnet(host, port) ? (short) 1 : (short) 0;
    }

    /**
     * Get brokers topic all partitions log end offset.
     */
    public static Map<Integer, Long> logEndOffset(JMXInitializeInfo initializeInfo, String topic) {
        BrokerInfo brokerInfo = new BrokerInfo();
        JMXConnector connector = null;
        String JMX = initializeInfo.getUri();
        Map<Integer, Long> endOffsets = new HashMap<>();
        String mbean = "kafka.log:type=Log,name=LogEndOffset,topic=" + topic + ",partition=*";

        try {
            brokerInfo.setBrokerHost(initializeInfo.getHost());
            brokerInfo.setBrokerJmxPort(initializeInfo.getPort());
            brokerInfo.setBrokerJmxPortStatus(getBrokerStatus(initializeInfo.getHost(), initializeInfo.getPort()));
            if (brokerInfo.getBrokerJmxPortStatus() == 1) {
                JMXServiceURL jmxSeriverUrl = new JMXServiceURL(String.format(JMX, initializeInfo.getHost() + ":" + initializeInfo.getPort()));
                initializeInfo.setUrl(jmxSeriverUrl);
                connector = JMXFactoryUtil.connectWithTimeout(initializeInfo);
                MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();

                Set<ObjectName> objectNames = mbeanConnection.queryNames(new ObjectName(mbean), null);
                for (ObjectName objectName : objectNames) {
                    int partition = Integer.valueOf(objectName.getKeyProperty("partition"));
                    Object value = mbeanConnection.getAttribute(new ObjectName(mbean), KConstants.MBean.VALUE);
                    if (value != null) {
                        endOffsets.put(partition, Long.valueOf(value.toString()));
                    }
                }

            }
        } catch (Exception e) {
            log.error("Get kafka mbean from jmx has error, JMXInitializeInfo[{}], error msg is {}", initializeInfo, e);
        } finally {
            if (connector != null) {
                try {
                    connector.close();
                } catch (IOException e) {
                    log.error("Close mbean jmx connector has error, msg is {}", e);
                }
            }
        }

        return endOffsets;
    }

    /**
     * Get all kafka broker mbean.
     *
     * @param initializeInfo
     * @return
     */
    public static Map<String, MBeanInfo> mbean(JMXInitializeInfo initializeInfo, Map<String,String> mbeanMaps) {
        BrokerInfo brokerInfo = new BrokerInfo();
        JMXConnector connector = null;
        String JMX = initializeInfo.getUri();
        Map<String, MBeanInfo> mbeanMap = new HashMap<>();

        try {
            brokerInfo.setBrokerHost(initializeInfo.getHost());
            brokerInfo.setBrokerJmxPort(initializeInfo.getPort());
            brokerInfo.setBrokerJmxPortStatus(getBrokerStatus(initializeInfo.getHost(), initializeInfo.getPort()));
            if (brokerInfo.getBrokerJmxPortStatus() == 1) {
                JMXServiceURL jmxSeriverUrl = new JMXServiceURL(String.format(JMX, initializeInfo.getHost() + ":" + initializeInfo.getPort()));
                initializeInfo.setUrl(jmxSeriverUrl);
                connector = JMXFactoryUtil.connectWithTimeout(initializeInfo);
                MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
                for (Map.Entry<String, String> mbean : mbeanMaps.entrySet()) {
                    MBeanInfo mBeanInfo = new MBeanInfo();

                    if(MBean.OSFREEMEMORY.equals(mbean.getKey())){
                        MemoryMXBean memBean = ManagementFactory.newPlatformMXBeanProxy(mbeanConnection, ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);
                        long memUsed = memBean.getHeapMemoryUsage().getUsed();
                        long memMax = memBean.getHeapMemoryUsage().getMax();
                        double mem = StrUtils.numberic(String.valueOf(memUsed * 100.0 / memMax));
                        mBeanInfo.setFifteenMinute("0.0");
                        mBeanInfo.setFiveMinute("0.0");
                        mBeanInfo.setMeanRate("0.0");
                        mBeanInfo.setOneMinute(String.valueOf(mem));
                    }else if(MBean.CPUUSED.equals(mbean.getKey())){
                        String cpuStr = mbeanConnection.getAttribute(new ObjectName(String.format(JmxConstants.BrokerServer.JMX_PERFORMANCE_TYPE.getValue(), initializeInfo.getBrokerId())), JmxConstants.BrokerServer.PROCESS_CPU_LOAD.getValue()).toString();
                        double cpuValue = Double.parseDouble(cpuStr);
                        double cpu = StrUtils.numberic(String.valueOf(cpuValue * 100.0));
                        mBeanInfo.setFifteenMinute("0.0");
                        mBeanInfo.setFiveMinute("0.0");
                        mBeanInfo.setMeanRate("0.0");
                        mBeanInfo.setOneMinute(String.valueOf(cpu));
                    }else {
                        if (mbeanConnection.isRegistered(new ObjectName(mbean.getValue()))) {
                            Object fifteenMinuteRate = mbeanConnection.getAttribute(new ObjectName(mbean.getValue()), MBean.FIFTEEN_MINUTE_RATE);
                            Object fiveMinuteRate = mbeanConnection.getAttribute(new ObjectName(mbean.getValue()), MBean.FIVE_MINUTE_RATE);
                            Object meanRate = mbeanConnection.getAttribute(new ObjectName(mbean.getValue()), MBean.MEAN_RATE);
                            Object oneMinuteRate = mbeanConnection.getAttribute(new ObjectName(mbean.getValue()), MBean.ONE_MINUTE_RATE);
                            mBeanInfo.setFifteenMinute(fifteenMinuteRate.toString());
                            mBeanInfo.setFiveMinute(fiveMinuteRate.toString());
                            mBeanInfo.setMeanRate(meanRate.toString());
                            mBeanInfo.setOneMinute(oneMinuteRate.toString());
                        } else {
                            mBeanInfo.setFifteenMinute("0.0");
                            mBeanInfo.setFiveMinute("0.0");
                            mBeanInfo.setMeanRate("0.0");
                            mBeanInfo.setOneMinute("0.0");
                        }
                    }
                    mbeanMap.put(mbean.getKey(), mBeanInfo);
                }
            }
        } catch (Exception e) {
            log.error("Get kafka mbean set from jmx has error, JMXInitializeInfo[{}], error msg is {}", initializeInfo, e);
        } finally {
            if (connector != null) {
                try {
                    connector.close();
                } catch (IOException e) {
                    log.error("Close mbean set jmx connector has error, msg is {}", e);
                }
            }
        }

        return mbeanMap;
    }

}
