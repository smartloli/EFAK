/**
 * TopicRecordPageInfo.java
 * <p>
 * Copyright 2023 smartloli
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
package org.kafka.eagle.pojo.topic;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Topic metadata partition page information.
 *
 * @Author: smartloli
 * @Date: 2023/6/24 19:46
 * @Version: 3.4.0
 */
@Data
public class TopicRecordPageInfo {

    /**
     * page total
     */
    private Integer total;

    /**
     * partition id list, default 10 records.
     */
    private TreeSet<String> partitionIds;

    private List<TopicRecordInfo> records = new ArrayList<>();

}
