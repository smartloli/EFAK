/**
 * TopicMetadataInfo.java
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

import java.util.List;

/**
 * <p>
 * Topic 元数据信息类，用于存储 Topic 的元数据信息。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/8/23 03:15:05
 * @version 5.0.0
 */
@Data
public class TopicMetadataInfo {

    /**
     * Topic 保留时间
     */
    private String retainMs;

    /**
     * Topic 名称
     */
    private String topic;

    /**
     * Topic 分区元数据信息列表
     */
    private List<MetadataInfo> metadataInfos;

}
