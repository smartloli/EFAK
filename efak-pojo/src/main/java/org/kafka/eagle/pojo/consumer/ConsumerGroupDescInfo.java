/**
 * ConsumerGroupDescInfo.java
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
package org.kafka.eagle.pojo.consumer;

import lombok.Data;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Description: TODO
 * @Author: smartloli
 * @Date: 2023/7/16 19:14
 * @Version: 3.4.0
 */
@Data
public class ConsumerGroupDescInfo {

    /**
     * Get a valid consumer group from kafka.
     */
    private Set<String> groupIds = new HashSet<>();

    private Map<String, ConsumerGroupDescription> descConsumerGroup = new HashMap<>();

}
