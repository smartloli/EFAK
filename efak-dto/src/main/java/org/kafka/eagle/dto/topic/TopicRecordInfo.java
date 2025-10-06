/**
 * TopicRecordInfo.java
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
package org.kafka.eagle.dto.topic;

import lombok.Data;

/**
 * <p>
 * Topic 记录信息类，用于存储 Topic 分区的记录信息。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/9/21 23:15:05
 * @version 5.0.0
 */
@Data
public class TopicRecordInfo {
    /**
     * Topic 分区 ID
     */
    public Integer partitionId;

    /**
     * Topic 分区记录数
     */
    private Long logSize;

    /**
     * Topic 分区 Leader
     */
    private String leader;

    /**
     * Topic 分区副本
     */
    private String replicas;

    /**
     * Topic 分区同步副本
     */
    private String isr;

    /**
     * Topic 分区首选 Leader
     */
    private Boolean preferredLeader;

    /**
     * Topic 分区副本不足
     */
    private Boolean underReplicated;
}
