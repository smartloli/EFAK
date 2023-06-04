/**
 * ClusterInfo.java
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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Manage kafka cluster info.
 * @Author: smartloli
 * @Date: 2023/5/27 20:21
 * @Version: 3.4.0
 */
@Data
@TableName("ke_clusters")
public class ClusterInfo implements Serializable {

    /**
     * ClusterId AUTO_INCREMENT
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /**
     * Create Cluster id.
     */
    private String clusterId;

    /**
     * Cluster Name
     */
    @NotNull
    @Size(min=1, max=128)
    private String name;

    /**
     * Cluster Status,1:Normal, 0:Error
     */
    private int status;

    /**
     * Kafka Brokers Number
     */
    private int nodes;

    /**
     * Whether enable kraft, Y or N
     */
    private String auth;

    private String authConfig;

    /**
     * Cluster modify time.
     */
    private LocalDateTime modifyTime;


}
