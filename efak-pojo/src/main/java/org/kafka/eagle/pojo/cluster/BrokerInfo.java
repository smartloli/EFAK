/**
 * BrokerInfo.java
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
import org.springframework.context.annotation.Description;

import java.time.LocalDateTime;

/**
 * Kafka broker info.
 *
 * @Author: smartloli
 * @Date: 2023/5/27 20:59
 * @Version: 3.4.0
 */
@Data
@Description("Database: ke.ke_brokers")
@TableName("ke_brokers")
public class BrokerInfo {

    /**
     * BrokerId AUTO_INCREMENT
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * ClusterId.
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
     * Kafka broker port status,1:Online, 0:Offline
     */
    private short brokerPortStatus;

    /**
     * Kafka broker jmx port.
     */
    private int brokerJmxPort;

    /**
     * Kafka broker jmx port status,1:Online, 0:Offline
     */
    private short brokerJmxPortStatus;

    /**
     * Kafka broker memory used rate.
     */
    private double brokerMemoryUsedRate;

    /**
     * Kafka broker cpu used rate.
     */
    private double brokerCpuUsedRate;

    private LocalDateTime brokerStartupTime;

    /**
     * Modify time.
     */
    private LocalDateTime modifyTime = LocalDateTime.now();

    /**
     * Kafka broker version.
     */
    private String brokerVersion = "-";


}
