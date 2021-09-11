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

import java.lang.management.*;

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
    public double getProcessCpu() {
        long totalTime = 0;
        for (long id : threadBean.getAllThreadIds()) {
            totalTime += threadBean.getThreadCpuTime(id);
        }
        long curtime = System.nanoTime();
        long usedTime = totalTime - preUsedTime;
        long totalPassedTime = curtime - preTime;
        preTime = curtime;
        preUsedTime = totalTime;
        return StrUtils.numberic((((double) usedTime) / totalPassedTime / osMxBean.getAvailableProcessors()) * 100);
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

}

