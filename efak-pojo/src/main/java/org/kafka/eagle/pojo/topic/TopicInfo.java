/**
 * TopicInfo.java
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

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Manage topic info.
 *
 * @Author: smartloli
 * @Date: 2023/6/20 22:27
 * @Version: 3.4.0
 */
@Data
@TableName("ke_topics")
public class TopicInfo implements Serializable {
    /**
     * ClusterId AUTO_INCREMENT
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Create Cluster id.
     */
    private String clusterId;

    /**
     * Topic name.
     */
    private String topicName;

    /**
     * Topic partitions.
     */
    private Integer partitions;

    /**
     * Topic replications.
     */
    private Integer replications;

    /**
     * The higher the coverage, the higher the resource usage of kafka broker nodes.
     */
    private Integer brokerSpread;

    /**
     * The larger the skewed, the higher the pressure on the broker node of kafka.
     */
    private Integer brokerSkewed;

    /**
     * The higher the leader skewed, the higher the pressure on the kafka broker leader node.
     */
    private Integer brokerLeaderSkewed;

    /**
     * Topic retain ms,default milliseconds.
     */
    private Long retainMs;

    /**
     * Cluster modify time.
     */
    private LocalDateTime modifyTime = LocalDateTime.now();


}
