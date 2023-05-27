/**
 * JDatabase.java
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
package org.kafka.eagle.common.constants;

/**
 * Description: TODO
 * @Author: smartloli
 * @Date: 2023/5/27 20:23
 * @Version: 3.4.0
 */
public class JDatabase {

    private String keClusterSql="CREATE TABLE ke_clusters(\n" +
            "    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary Key ClusterId',\n" +
            "    name VARCHAR(128) NOT NULL COMMENT 'Cluster Name',\n" +
            "    status SMALLINT NOT NULL COMMENT '1:Normal,0:Error',\n" +
            "    nodes INT NOT NULL COMMENT 'Cluster Nodes',\n" +
            "    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',\n" +
            "    kraft CHAR(1) NOT NULL COMMENT 'Y,N',\n" +
            "    INDEX idx_name (name)\n" +
            ") COMMENT 'Cluster Manage' CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;";

    private String brokerInfSql = "CREATE TABLE ke_brokers( \n" +
            "    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary Key',\n" +
            "    cluster_id BIGINT NOT NULL COMMENT 'Cluster ID',\n" +
            "    broker_id VARCHAR(128) NOT NULL COMMENT 'Broker ID',\n" +
            "    broker_host VARCHAR(128) NOT NULL COMMENT 'Broker Host',\n" +
            "    broker_port INT NOT NULL COMMENT 'Broker Port',\n" +
            "    broker_jmx_port INT NOT NULL COMMENT 'Broker JMX Port',\n" +
            "    broker_jmx_port_status SMALLINT NOT NULL COMMENT 'Broker JMX Port Status: 0-Not Available, 1-Available',\n" +
            "    broker_memory_used_rate DOUBLE NOT NULL COMMENT 'Broker Memory Used Rate',\n" +
            "    broker_cpu_used_rate DOUBLE NOT NULL COMMENT 'Broker CPU Used Rate',\n" +
            "    broker_startup_time DATETIME NOT NULL COMMENT 'Broker Startup Time',\n" +
            "    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',\n" +
            "    broker_version VARCHAR(128) NOT NULL COMMENT 'Broker Version',\n" +
            "    INDEX idx_cluster_id (cluster_id),\n" +
            "    INDEX idx_broker_id (broker_id),\n" +
            "    INDEX idx_broker_host (broker_host),\n" +
            "    INDEX idx_clusterid_brokerhost (cluster_id,broker_host),\n" +
            "    INDEX idx_clusterid_brokerid (cluster_id,broker_id),\n" +
            "    INDEX idx_clusterid_brokerid_brokerhost (cluster_id,broker_id,broker_host)\n" +
            ") COMMENT 'Broker Info' CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;";

}
