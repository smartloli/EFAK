-- Active: 1683968819703@@127.0.0.1@3306@ke

-- Cluster: 1683968819703@@
CREATE TABLE IF NOT EXISTS ke_clusters(
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary Key ClusterId',
    name VARCHAR(128) NOT NULL COMMENT 'Cluster Name',
    status SMALLINT NOT NULL COMMENT '1:Normal,0:Error',
    nodes INT NOT NULL COMMENT 'Cluster Nodes',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    kraft CHAR(1) NOT NULL COMMENT 'Y,N',
    INDEX idx_name (name)
) COMMENT 'Cluster Manage' CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Broker: 1683968819703@@
CREATE TABLE IF NOT EXISTS ke_brokers(
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary Key',
    cluster_id BIGINT NOT NULL COMMENT 'Cluster ID',
    broker_id VARCHAR(128) NOT NULL COMMENT 'Broker ID',
    broker_host VARCHAR(128) NOT NULL COMMENT 'Broker Host',
    broker_port INT NOT NULL COMMENT 'Broker Port',
    broker_jmx_port INT NOT NULL COMMENT 'Broker JMX Port',
    broker_jmx_port_status SMALLINT NOT NULL COMMENT 'Broker JMX Port Status: 0-Not Available, 1-Available',
    broker_memory_used_rate DOUBLE NOT NULL COMMENT 'Broker Memory Used Rate',
    broker_cpu_used_rate DOUBLE NOT NULL COMMENT 'Broker CPU Used Rate',
    broker_startup_time DATETIME NOT NULL COMMENT 'Broker Startup Time',
    modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    broker_version VARCHAR(128) NOT NULL COMMENT 'Broker Version',
    INDEX idx_cluster_id (cluster_id),
    INDEX idx_broker_id (broker_id),
    INDEX idx_broker_host (broker_host),
    INDEX idx_clusterid_brokerhost (cluster_id,broker_host),
    INDEX idx_clusterid_brokerid (cluster_id,broker_id),
    INDEX idx_clusterid_brokerid_brokerhost (cluster_id,broker_id,broker_host)
) COMMENT 'Broker Info' CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;