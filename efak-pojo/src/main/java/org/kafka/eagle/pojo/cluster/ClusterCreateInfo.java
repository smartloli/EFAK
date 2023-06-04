/**
 * ClusterCreateInfo.java
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

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Cluster create info.
 * @Author: smartloli
 * @Date: 2023/5/31 21:53
 * @Version: 3.4.0
 */
@Data
@TableName("ke_clusters_create")
public class ClusterCreateInfo {

    /**
     * BrokerId AUTO_INCREMENT
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /**
     * Create Cluster id.
     */
    private String clusterId;

    /**
     * Kafka broker id.
     */
    private String brokerId;

    /**
     * Kafka broker host or ip.
     */
    private String brokerHost;

    /**
     * Kafka broker port.
     */
    private int brokerPort;

    /**
     * Kafka broker jmx port.
     */
    private int brokerJmxPort;

    /**
     * Modify time.
     */
    private LocalDateTime modifyTime = LocalDateTime.now();


}
