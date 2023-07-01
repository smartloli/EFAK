/**
 * TopicSequenceInfo.java
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Used to collect the magnitude of Topic data and record it as temporal data based on timestamp.
 *
 * @Author: smartloli
 * @Date: 2023/7/1 23:01
 * @Version: 3.4.0
 */
@Data
@TableName("ke_topics_sequence")
public class TopicSequenceInfo {

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

    /**
     * Topic LogSize
     */
    private Long logSize = 0L;

    private Long logSizeDiffVal = 0L;

    private Long timespan = new Date().getTime();

    /**
     * Define the time format.
     */
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    /**
     * Topic day, such as 20230701
     */
    private String day = LocalDateTime.now().format(formatter);



    /**
     * Cluster modify time.
     */
    private LocalDateTime modifyTime = LocalDateTime.now();

}
