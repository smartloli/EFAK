/**
 * TopicRecordPageInfo.java
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

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * <p>
 * Topic 分区分页结果类，用于存储 Topic 分区的分页查询结果。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/9/21 23:12:05
 * @version 5.0.0
 */
@Data
public class TopicPartitionPageResult {
    /**
     * 分页总数
     */
    private Integer total;

    /**
     * 分区 ID 列表，默认 10 条记录
     */
    private TreeSet<String> partitionIds;

    /**
     * Topic 记录信息列表
     */
    private List<TopicRecordInfo> records = new ArrayList<>();
}
