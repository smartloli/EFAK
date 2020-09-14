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
package org.smartloli.kafka.eagle.core.task.metrics;

import org.smartloli.kafka.eagle.common.protocol.BaseProtocol;

/**
 * Metrics WorkNodeServer memory used, max, cpu.
 *
 * @author smartloli.
 * <p>
 * Created by Sep 15, 2020
 */
public class WorkNodeMetrics extends BaseProtocol {
    private long memoryUser;
    private long memoryMax;
    private double cpu;
    private boolean isAlive = false;

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public long getMemoryUser() {
        return memoryUser;
    }

    public void setMemoryUser(long memoryUser) {
        this.memoryUser = memoryUser;
    }

    public long getMemoryMax() {
        return memoryMax;
    }

    public void setMemoryMax(long memoryMax) {
        this.memoryMax = memoryMax;
    }

    public double getCpu() {
        return cpu;
    }

    public void setCpu(double cpu) {
        this.cpu = cpu;
    }
}
