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
package org.smartloli.kafka.eagle.common.protocol;

/**
 * Topic capacity such as :
 * <p>
 * 0B ~ 100MB
 * <p>
 * 100MB ~ 10GB
 * <p>
 * 10GB+
 *
 * @author smartloli.
 * <p>
 * Created by Jun 11, 2022
 */
public class TopicCapacityInfo extends BaseProtocol {

    private long mb;
    private long gb;
    private long tb;

    public long getMb() {
        return mb;
    }

    public void setMb(long mb) {
        this.mb = mb;
    }

    public long getGb() {
        return gb;
    }

    public void setGb(long gb) {
        this.gb = gb;
    }

    public long getTb() {
        return tb;
    }

    public void setTb(long tb) {
        this.tb = tb;
    }
}
