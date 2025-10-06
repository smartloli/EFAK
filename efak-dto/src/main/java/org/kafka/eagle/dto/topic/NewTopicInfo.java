/**
 * NewTopicInfo.java
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
 * 新建 Topic 信息类，用于存储创建新 Topic 时的配置参数。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/8/24 01:05:05
 * @version 5.0.0
 */
@Data
public class NewTopicInfo {
    /**
     * Topic 名称
     */
    private String topicName;

    /**
     * Topic 分区数
     */
    private Integer partitions;

    /**
     * Topic 副本数
     */
    private Short replication;

    /**
     * Topic 保留时间（毫秒）
     */
    private Long retainMs;
}
