/**
 * TopicSummaryInfo.java
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
import org.kafka.eagle.common.constants.KConstants;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * The data level used to collect topics, divided by day.
 *
 * @Author: smartloli
 * @Date: 2023/7/1 22:57
 * @Version: 3.4.0
 */
@Data
@TableName("ke_topics_summary")
public class TopicSummaryInfo {

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

    /**
     * Get topic logsize diff val.
     */
    private Long logSizeDiffVal = 0L;

    /**
     * Get topic logsize timespan.
     */
    private Long timespan = new Date().getTime();

    /**
     * Topic day, such as 20230701
     */
    private String day = LocalDateTime.now().format(KConstants.getFormatter());


    /**
     * Cluster modify time.
     */
    private LocalDateTime modifyTime = LocalDateTime.now();


}
