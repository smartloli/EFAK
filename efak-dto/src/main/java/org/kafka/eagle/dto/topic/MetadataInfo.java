/**
 * MetadataInfo.java
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
 * Topic 元数据信息类，用于存储 Topic 分区的详细元数据信息。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/8/23 01:05:05
 * @version 5.0.0
 */
@Data
public class MetadataInfo {

    /**
     * Topic 分区 ID
     */
    private int partitionId;

    /**
     * Topic 日志大小
     */
    private long logSize;

    /**
     * 分区 Leader 节点
     */
    private int leader;

    /**
     * 副本节点列表
     */
    private String replicas;

    /**
     * 同步副本节点列表
     */
    private String isr;

}
