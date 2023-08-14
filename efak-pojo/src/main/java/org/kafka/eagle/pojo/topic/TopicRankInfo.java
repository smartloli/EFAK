/**
 * TopicRankInfo.java
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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Description: TODO
 * @Author: smartloli
 * @Date: 2023/8/14 16:10
 * @Version: 3.4.0
 */
@Data
@TableName("ke_topic_rank")
public class TopicRankInfo {
    /**
     * ClusterId AUTO_INCREMENT
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Cluster Id
     */
    private String clusterId;

    /**
     * Kafka Topic Name.
     */
    private String topicName;

    private String topicKey;

    private String topicValue;
}
