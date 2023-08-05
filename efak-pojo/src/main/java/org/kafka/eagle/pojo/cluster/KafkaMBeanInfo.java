/**
 * KafkaMetricInfo.java
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
package org.kafka.eagle.pojo.cluster;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.kafka.eagle.common.constants.KConstants;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/8/5 11:26
 * @Version: 3.4.0
 */
@Data
@TableName("ke_kafka_mbean_metrics")
public class KafkaMBeanInfo implements Serializable {
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
     * Collect kafka mbean key, such as message_in, byte_in, byte_out etc.
     */
    private String mbeanKey;

    /**
     * value by mbean, such as 9.99
     */
    private String mbeanValue;

    /**
     * Stats consumer group topic data day, such as 20230728
     */
    private String day = LocalDateTime.now().format(KConstants.getFormatter());
    ;

    /**
     * Get kafka mbean data timespan.
     */
    private Long timespan = new Date().getTime();

    /**
     * Update time.
     */
    private LocalDateTime modifyTime = LocalDateTime.now();


}
