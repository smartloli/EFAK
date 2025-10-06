/**
 * KafkaClusterInfo.java
 * <p>
 * Copyright 2025 smartloli
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
package org.kafka.eagle.dto.cluster;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.kafka.eagle.dto.broker.BrokerInfo;
import java.util.List;

/**
 * <p>
 * Kafka 集群信息 DTO，包含集群 ID、名称、状态和认证设置。
 * 该类表示用于管理和监控的 Kafka 集群核心信息。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/8/23 21:12:25
 * @version 5.0.0
 */
@Data
public class KafkaClusterInfo {
    /**
     * 数据库存储的主键 ID
     */
    private Long id;

    /**
     * Kafka 集群的唯一标识符
     */
    private String clusterId;

    /**
     * Kafka 集群的可读名称
     */
    private String name;

    /**
     * 集群类型（环境/部署类型）
     */
    private String clusterType;

    /**
     * 是否启用认证（Y/N）
     */
    private String auth;

    /**
     * 认证配置（JSON 格式）
     */
    private String authConfig;

    /**
     * 集群可用性百分比
     */
    private BigDecimal availability;

    // 临时字段，用于前端显示
    private transient Integer onlineNodes;
    private transient Integer totalNodes;

    // 临时字段，用于编辑模态框的 Broker 节点信息
    private transient List<BrokerInfo> brokers; // 编辑模态框的 Broker 节点
    private transient String type;              // 前端 clusterType 的别名
    private transient Boolean securityEnabled;  // 转换为认证状态的别名（Y/N）
    private transient String securityConfig;    // 前端 authConfig 的别名

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
