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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.management.*;
import java.util.*;

/**
 * Count the percent of application resources.
 *
 * @author smartloli.
 * <p>
 * Created by Sep 15, 2020
 */
public class AppUtils {

    private static AppUtils instance = new AppUtils();

    private OperatingSystemMXBean osMxBean;
    private RuntimeMXBean runtimeMXBean;
    private ThreadMXBean threadBean;
    private MemoryMXBean memoryMXBean;
    private long preTime = System.nanoTime();
    private long preUsedTime = 0;

    private AppUtils() {
        osMxBean = ManagementFactory.getOperatingSystemMXBean();
        memoryMXBean = ManagementFactory.getMemoryMXBean();
        threadBean = ManagementFactory.getThreadMXBean();
        runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    }

    public static AppUtils getInstance() {
        return instance;
    }

    /**
     * Get current application cpu (such as 1%).
     * <p>
     * 100% -> 1 cpu cores
     */
//    public double getProcessCpu() {
//        long totalTime = 0;
//        for (long id : threadBean.getAllThreadIds()) {
//            totalTime += threadBean.getThreadCpuTime(id);
//        }
//        long curtime = System.nanoTime();
//        long usedTime = totalTime - preUsedTime;
//        long totalPassedTime = curtime - preTime;
//        preTime = curtime;
//        preUsedTime = totalTime;
//        return StrUtils.numberic((((double) usedTime) / totalPassedTime / osMxBean.getAvailableProcessors()) * 100);
//    }
    public double getProcessCpu() {
        return StrUtils.numberic(getCpuUsagePercent());
    }


    /**
     * Get application mem used.
     */
    public long getProcessMemUsed() {
        return memoryMXBean.getHeapMemoryUsage().getUsed();
    }

    /**
     * Get application mem max.
     */
    public long getProcessMemMax() {
        return memoryMXBean.getHeapMemoryUsage().getMax();
    }

    public String getStartTime() {
        return CalendarUtils.convertUnixTime2Date(runtimeMXBean.getStartTime());
    }

    /**
     * Get os cpu info.
     */
    private static Map<?, ?> getCpuInfo() {
        InputStreamReader inputs = null;
        BufferedReader buffer = null;
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            inputs = new InputStreamReader(new FileInputStream("/proc/stat"));
            buffer = new BufferedReader(inputs);
            String line = "";
            while (true) {
                line = buffer.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith("cpu")) {
                    StringTokenizer tokenizer = new StringTokenizer(line);
                    List<String> temp = new ArrayList<String>();
                    while (tokenizer.hasMoreElements()) {
                        String value = tokenizer.nextToken();
                        temp.add(value);
                    }
                    map.put("user", temp.get(1));
                    map.put("nice", temp.get(2));
                    map.put("system", temp.get(3));
                    map.put("idle", temp.get(4));
                    map.put("iowait", temp.get(5));
                    map.put("irq", temp.get(6));
                    map.put("softirq", temp.get(7));
                    map.put("stealstolen", temp.get(8));
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LoggerUtils.print(AppUtils.class).error("Get cpu info has error, msg is ", e);
            map.put("user", "-1");
            map.put("nice", "-1");
            map.put("system", "-1");
            map.put("idle", "-1");
            map.put("iowait", "-1");
            map.put("irq", "-1");
            map.put("softirq", "-1");
            map.put("stealstolen", "-1");
        } finally {
            try {
                buffer.close();
                inputs.close();
            } catch (Exception ex) {
                LoggerUtils.print(AppUtils.class).error("Close cpu stream object has error, msg is ", ex);
            }
        }
        return map;
    }

    /**
     * Get cpu usage.
     */
    private static double getCpuUsagePercent() {
        try {
            Map<?, ?> map1 = getCpuInfo();
            Thread.sleep(10);
            Map<?, ?> map2 = getCpuInfo();

            if (map1 == null || map2 == null) {
                return 0.00;
            }

            long user1 = Long.parseLong(map1.get("user").toString());
            long nice1 = Long.parseLong(map1.get("nice").toString());
            long system1 = Long.parseLong(map1.get("system").toString());
            long idle1 = Long.parseLong(map1.get("idle").toString());

            long user2 = Long.parseLong(map2.get("user").toString());
            long nice2 = Long.parseLong(map2.get("nice").toString());
            long system2 = Long.parseLong(map2.get("system").toString());
            long idle2 = Long.parseLong(map2.get("idle").toString());

            long total1 = user1 + system1 + nice1;
            long total2 = user2 + system2 + nice2;
            float total = total2 - total1;

            long totalIdle1 = user1 + nice1 + system1 + idle1;
            long totalIdle2 = user2 + nice2 + system2 + idle2;
            float totalidle = totalIdle2 - totalIdle1;
            double cpusage = 0.00;
            if (totalidle <= 0) {
                return cpusage;
            } else {
                cpusage = (total * 1.0 / totalidle) * 100;
                return cpusage;
            }
        } catch (InterruptedException e) {
            LoggerUtils.print(AppUtils.class).error("Get cpu usage percent has error, msg is ", e);
        }
        return 0.00;
    }

    /**
     * Get memory usage.
     */
    public static double getOSMemUsage() {
        Map<String, Object> map = new HashMap<String, Object>();
        InputStreamReader inputs = null;
        BufferedReader buffer = null;
        try {
            inputs = new InputStreamReader(new FileInputStream("/proc/meminfo"));
            buffer = new BufferedReader(inputs);
            String line = "";
            while (true) {
                line = buffer.readLine();
                if (line == null)
                    break;
                int beginIndex = 0;
                int endIndex = line.indexOf(":");
                if (endIndex != -1) {
                    String key = line.substring(beginIndex, endIndex);
                    beginIndex = endIndex + 1;
                    endIndex = line.length();
                    String memory = line.substring(beginIndex, endIndex);
                    String value = memory.replace("kB", "").trim();
                    map.put(key, value);
                }
            }

            long memTotal = Long.parseLong(map.get("MemTotal").toString());
            long memFree = Long.parseLong(map.get("MemFree").toString());
            long memused = memTotal - memFree;
            long buffers = Long.parseLong(map.get("Buffers").toString());
            long cached = Long.parseLong(map.get("Cached").toString());

            double usage = (double) (memused - buffers - cached) * 1.0 / memTotal * 100;
            return StrUtils.numberic(usage);
        } catch (Exception e) {
            LoggerUtils.print(AppUtils.class).error("Get os memory usage percent has error, msg is ", e);
        } finally {
            try {
                buffer.close();
                inputs.close();
            } catch (Exception ex) {
                LoggerUtils.print(AppUtils.class).error("Close os memory object has error, msg is ", ex);
            }
        }
        return 0.00;
    }
}

