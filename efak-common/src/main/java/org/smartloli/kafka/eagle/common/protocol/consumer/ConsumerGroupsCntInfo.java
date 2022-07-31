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
package org.smartloli.kafka.eagle.common.protocol.consumer;

import org.smartloli.kafka.eagle.common.protocol.BaseProtocol;

/**
 * Count active and inactive consumer groups.
 *
 * @author smartloli.
 * <p>
 * Created by Jun 25, 2022
 */
public class ConsumerGroupsCntInfo extends BaseProtocol {

    private long activeCnt;
    private long standbyCnt;

    public long getActiveCnt() {
        return activeCnt;
    }

    public void setActiveCnt(long activeCnt) {
        this.activeCnt = activeCnt;
    }

    public long getStandbyCnt() {
        return standbyCnt;
    }

    public void setStandbyCnt(long standbyCnt) {
        this.standbyCnt = standbyCnt;
    }
}
