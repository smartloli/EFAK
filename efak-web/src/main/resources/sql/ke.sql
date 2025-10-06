-- 创建数据库
CREATE DATABASE IF NOT EXISTS efak_ai;

-- 创建告警渠道表
CREATE TABLE `ke_alert_channels` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `cluster_id` varchar(50) NOT NULL DEFAULT '' COMMENT '集群ID',
  `name` varchar(255) NOT NULL COMMENT '渠道名称',
  `type` varchar(50) NOT NULL COMMENT '渠道类型',
  `api_url` varchar(500) NOT NULL COMMENT 'API地址',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` varchar(100) DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_cluster_id` (`cluster_id`),
  KEY `idx_cluster_type` (`cluster_id`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='告警渠道表';

-- 创建告警类型配置表
CREATE TABLE `ke_alert_type_configs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `cluster_id` varchar(50) NOT NULL DEFAULT '' COMMENT '集群ID',
  `type` varchar(50) NOT NULL COMMENT '告警类型',
  `name` varchar(255) NOT NULL COMMENT '类型名称',
  `description` text COMMENT '类型描述',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `threshold` decimal(10,2) DEFAULT NULL COMMENT '阈值',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `target` varchar(255) DEFAULT NULL COMMENT '监控目标',
  `channel_id` text COMMENT '关联渠道ID列表(JSON格式)',
  `created_by` varchar(100) DEFAULT NULL COMMENT '创建人',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_target` (`target`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_cluster_id` (`cluster_id`),
  KEY `idx_cluster_type` (`cluster_id`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='告警类型配置表'

-- 创建告警任务表
CREATE TABLE `ke_alerts` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增ID字段',
  `alert_task_id` bigint NOT NULL COMMENT '告警任务ID（关联 ke_alert_type_configs 表的 ID）',
  `cluster_id` varchar(255) NOT NULL COMMENT '集群ID',
  `title` varchar(255) NOT NULL COMMENT '告警标题',
  `description` text COMMENT '告警描述',
  `channel_id` bigint NOT NULL COMMENT '告警渠道ID（关联 ke_alert_channels 表）',
  `duration` varchar(50) DEFAULT NULL COMMENT '持续时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '告警状态：0-未处理，1-已处理，2-已忽略，3-已解决',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_alert_task_cluster` (`alert_task_id`,`cluster_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='告警信息表'

-- 创建Broker信息表
CREATE TABLE `ke_broker_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `cluster_id` varchar(255) NOT NULL COMMENT '唯一集群ID',
  `broker_id` int NOT NULL COMMENT 'Broker ID',
  `host_ip` varchar(50) NOT NULL COMMENT '主机IP',
  `port` int NOT NULL DEFAULT '9092' COMMENT '端口',
  `jmx_port` int DEFAULT NULL COMMENT 'JMX端口',
  `status` varchar(20) NOT NULL DEFAULT 'offline' COMMENT '状态(online/offline)',
  `cpu_usage` decimal(5,2) DEFAULT NULL COMMENT 'CPU使用率(%)',
  `memory_usage` decimal(5,2) DEFAULT NULL COMMENT '内存使用率(%)',
  `startup_time` timestamp NULL DEFAULT NULL COMMENT '启动时间',
  `version` varchar(50) DEFAULT NULL COMMENT '版本号',
  `created_by` varchar(100) DEFAULT NULL COMMENT '创建人',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_broker_host_port` (`broker_id`,`host_ip`,`port`),
  KEY `idx_status` (`status`),
  KEY `idx_host_ip` (`host_ip`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Kafka Broker信息表'

-- 创建Broker性能指标历史数据表
CREATE TABLE `ke_broker_metrics` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `broker_id` int NOT NULL COMMENT 'Broker ID',
  `host_ip` varchar(50) NOT NULL COMMENT '主机IP',
  `port` int NOT NULL COMMENT '端口',
  `cpu_usage` decimal(5,2) NOT NULL COMMENT 'CPU使用率(%)',
  `memory_usage` decimal(5,2) NOT NULL COMMENT '内存使用率(%)',
  `collect_time` datetime NOT NULL COMMENT '采集时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `cluster_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_broker_id` (`broker_id`),
  KEY `idx_collect_time` (`collect_time`),
  KEY `idx_broker_collect_time` (`broker_id`,`collect_time`),
  KEY `idx_host_port` (`host_ip`,`port`),
  KEY `idx_cluster_id` (`cluster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Broker性能指标历史数据表'

-- AI对话消息表
CREATE TABLE `ke_chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` varchar(64) NOT NULL COMMENT '会话ID',
  `username` varchar(100) NOT NULL COMMENT '用户名',
  `sender` varchar(20) NOT NULL COMMENT '发送者：user/assistant',
  `content` longtext NOT NULL COMMENT '消息内容',
  `model_name` varchar(100) DEFAULT NULL COMMENT 'AI模型名称',
  `message_type` tinyint(1) DEFAULT '1' COMMENT '消息类型：1-文本，2-图片，3-文件',
  `enable_markdown` tinyint(1) DEFAULT '1' COMMENT '是否启用Markdown：1-启用，0-禁用',
  `enable_charts` tinyint(1) DEFAULT '1' COMMENT '是否启用图表：1-启用，0-禁用',
  `enable_highlight` tinyint(1) DEFAULT '1' COMMENT '是否启用代码高亮：1-启用，0-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：1-正常，0-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_username` (`username`),
  KEY `idx_sender` (`sender`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI对话消息表'

--  AI对话会话表
CREATE TABLE `ke_chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(100) NOT NULL COMMENT '用户名',
  `session_id` varchar(64) NOT NULL COMMENT '会话ID',
  `title` varchar(255) NOT NULL COMMENT '会话标题',
  `model_name` varchar(100) DEFAULT NULL COMMENT 'AI模型名称',
  `message_count` int DEFAULT '0' COMMENT '消息数量',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：1-活跃，0-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_id` (`session_id`),
  KEY `idx_username` (`username`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI对话会话表'

-- 集群信息表
CREATE TABLE `ke_cluster` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cluster_id` varchar(255) NOT NULL,
  `cluster_name` varchar(255) NOT NULL,
  `cluster_type` varchar(50) DEFAULT '开发集群',
  `cluster_number` int DEFAULT '0',
  `auth` char(1) DEFAULT 'N',
  `auth_config` text,
  `online_nodes` int DEFAULT '0',
  `total_nodes` int DEFAULT '0',
  `availability` decimal(5,2) DEFAULT '0.00',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `cluster_id` (`cluster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='集群信息表'

-- 消费者组主题信息表
CREATE TABLE `ke_consumer_group_topic` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `cluster_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '集群ID',
  `group_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消费者组ID',
  `topic_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主题名称',
  `state` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'UNKNOWN' COMMENT '消费者组状态(STABLE,PREPARING_REBALANCE,COMPLETING_REBALANCE,EMPTY,DEAD,UNKNOWN)',
  `logsize` bigint DEFAULT '0' COMMENT '主题日志总大小',
  `offsets` bigint DEFAULT '0' COMMENT '消费者组当前偏移量',
  `lags` bigint DEFAULT '0' COMMENT '消费延迟数量(logsize-offsets)',
  `collect_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '数据采集时间',
  `collect_date` date NOT NULL COMMENT '采集日期(格式:YYYY-MM-DD)',
  PRIMARY KEY (`id`),
  KEY `idx_cluster_group` (`cluster_id`,`group_id`),
  KEY `idx_cluster_topic` (`cluster_id`,`topic_name`),
  KEY `idx_cluster_date` (`cluster_id`,`collect_date`),
  KEY `idx_cluster_group_topic` (`cluster_id`,`group_id`,`topic_name`),
  KEY `idx_cluster_group_date` (`cluster_id`,`group_id`,`collect_date`),
  KEY `idx_cluster_topic_date` (`cluster_id`,`topic_name`,`collect_date`),
  KEY `idx_cluster_state_date` (`cluster_id`,`state`,`collect_date`),
  KEY `idx_cluster_group_topic_date` (`cluster_id`,`group_id`,`topic_name`,`collect_date`),
  KEY `idx_cluster_time_covering` (`cluster_id`,`collect_time`,`group_id`,`topic_name`),
  KEY `idx_date_time_covering` (`collect_date`,`collect_time`,`cluster_id`,`group_id`),
  KEY `idx_lags_analysis` (`cluster_id`,`lags`,`collect_date`),
  KEY `idx_state_analysis` (`state`,`cluster_id`,`collect_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='消费者组主题延迟数据采集表'

-- 大模型配置表
CREATE TABLE `ke_model_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `model_name` varchar(100) NOT NULL COMMENT '模型名称',
  `api_type` varchar(50) NOT NULL COMMENT 'API类型',
  `endpoint` varchar(500) NOT NULL COMMENT '接口地址',
  `api_key` varchar(500) DEFAULT NULL COMMENT 'API密钥',
  `system_prompt` text DEFAULT NULL COMMENT '系统提示词',
  `timeout` int DEFAULT '30' COMMENT '超时时间(秒)',
  `description` text COMMENT '描述信息',
  `enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用(0:禁用,1:启用)',
  `status` tinyint(1) DEFAULT '0' COMMENT '状态(0:离线,1:在线,2:错误)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_model_name` (`model_name`),
  KEY `idx_api_type` (`api_type`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='大模型配置表'

-- 性能监控表
CREATE TABLE `ke_performance_monitor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `cluster_id` varchar(255) NOT NULL COMMENT '集群ID',
  `kafka_host` varchar(255) NOT NULL DEFAULT '' COMMENT 'Kafka节点HOST',
  `message_in` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '消息每秒写入记录数',
  `byte_in` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '每秒写入字节数',
  `byte_out` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '每秒读取字节数',
  `time_ms_produce` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '写入耗时(毫秒)',
  `time_ms_consumer` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '消费耗时(毫秒)',
  `memory_usage` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '内存使用率(%)',
  `cpu_usage` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT 'CPU使用率(%)',
  `collect_time` datetime NOT NULL COMMENT '采集时间',
  `collect_date` date NOT NULL COMMENT '日期(格式:2025-09-24)',
  PRIMARY KEY (`id`),
  KEY `idx_cluster_date` (`cluster_id`,`collect_date`),
  KEY `idx_cluster_time` (`cluster_id`,`collect_time`),
  KEY `idx_cluster_collect_time_desc` (`cluster_id`,`collect_time` DESC),
  KEY `idx_cluster_host_time` (`cluster_id`,`collect_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='性能监控表'

-- 任务执行历史表
CREATE TABLE `ke_task_execution_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `task_type` varchar(50) NOT NULL COMMENT '任务类型',
  `execution_status` varchar(20) NOT NULL COMMENT '执行状态：RUNNING-执行中，SUCCESS-成功，FAILED-失败，CANCELLED-已取消',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration` bigint DEFAULT NULL COMMENT '执行时长(毫秒)',
  `result_message` text COMMENT '执行结果消息',
  `error_message` text COMMENT '错误信息',
  `executor_node` varchar(100) DEFAULT NULL COMMENT '执行节点',
  `trigger_type` varchar(20) DEFAULT 'SCHEDULED' COMMENT '触发类型：SCHEDULED-定时触发，MANUAL-手动触发',
  `trigger_user` varchar(50) DEFAULT NULL COMMENT '触发用户',
  `input_params` text COMMENT '输入参数(JSON格式)',
  `output_result` text COMMENT '输出结果(JSON格式)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_execution_status` (`execution_status`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_task_type` (`task_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='任务执行历史表'

-- 任务调度表
CREATE TABLE `ke_task_scheduler` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `task_type` varchar(50) NOT NULL COMMENT '任务类型',
  `cron_expression` varchar(100) NOT NULL COMMENT 'Cron表达式',
  `description` text COMMENT '任务描述',
  `status` varchar(20) NOT NULL DEFAULT 'enabled' COMMENT '任务状态',
  `last_execute_time` varchar(30) DEFAULT NULL COMMENT '上次执行时间',
  `next_execute_time` varchar(30) DEFAULT NULL COMMENT '下次执行时间',
  `execute_count` int NOT NULL DEFAULT '0' COMMENT '执行次数',
  `success_count` int NOT NULL DEFAULT '0' COMMENT '成功次数',
  `fail_count` int NOT NULL DEFAULT '0' COMMENT '失败次数',
  `last_execute_result` text COMMENT '上次执行结果',
  `error_message` text COMMENT '错误信息',
  `created_by` varchar(50) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `updated_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `config` text COMMENT '任务配置(JSON格式)',
  `timeout` int DEFAULT '300' COMMENT '超时时间(秒)',
  `node_id` varchar(100) DEFAULT NULL COMMENT '执行节点ID',
  `cluster_name` varchar(100) DEFAULT NULL COMMENT '集群名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_name` (`task_name`),
  KEY `idx_task_type` (`task_type`),
  KEY `idx_status` (`status`),
  KEY `idx_cluster_name` (`cluster_name`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='任务调度表'

-- Topic信息表
CREATE TABLE `ke_topic_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `topic_name` varchar(255) NOT NULL COMMENT 'Topic名称',
  `partitions` int NOT NULL DEFAULT '0' COMMENT '分区数',
  `replicas` int NOT NULL DEFAULT '0' COMMENT '副本数',
  `broker_spread` varchar(50) DEFAULT NULL COMMENT 'Broker分布状态',
  `broker_skewed` varchar(50) DEFAULT NULL COMMENT 'Broker倾斜状态',
  `leader_skewed` varchar(50) DEFAULT NULL COMMENT 'Leader倾斜状态',
  `retention_time` varchar(50) DEFAULT NULL COMMENT '保留时间',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(100) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(100) DEFAULT NULL COMMENT '更新人',
  `icon` varchar(100) DEFAULT 'database',
  `cluster_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cid_topic` (`cluster_id`,`topic_name`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Topic信息表'

-- Topic实例指标表
CREATE TABLE `ke_topic_instant_metrics` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `cluster_id` varchar(64) NOT NULL COMMENT '集群ID',
  `topic_name` varchar(255) NOT NULL COMMENT 'Topic名称',
  `metric_type` varchar(32) NOT NULL COMMENT '指标类型: byte_out, capacity, logsize, byte_in',
  `metric_value` varchar(32) NOT NULL DEFAULT '0' COMMENT '指标数值',
  `last_updated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_unique_cluster_topic_metric` (`cluster_id`,`topic_name`,`metric_type`) USING BTREE,
  KEY `idx_cluster_id` (`cluster_id`),
  KEY `idx_cluster_topic` (`cluster_id`,`topic_name`),
  KEY `idx_metric_type` (`metric_type`),
  KEY `idx_last_updated` (`last_updated`),
  KEY `idx_cluster_metric_type` (`cluster_id`,`metric_type`),
  KEY `idx_cluster_topic_updated` (`cluster_id`,`topic_name`,`last_updated` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Topic当前实时指标表'

-- Topic历史指标表
CREATE TABLE `ke_topics_metrics` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `topic_name` varchar(255) NOT NULL COMMENT 'Topic名称',
  `record_count` bigint NOT NULL DEFAULT '0' COMMENT '记录数',
  `record_count_diff` bigint NOT NULL DEFAULT '0' COMMENT '记录数增量',
  `capacity` bigint NOT NULL DEFAULT '0' COMMENT '容量(字节)',
  `capacity_diff` bigint NOT NULL DEFAULT '0' COMMENT '容量增量',
  `write_speed` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '写入速度(消息/秒)',
  `read_speed` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '读取速度(消息/秒)',
  `collect_time` datetime NOT NULL COMMENT '采集时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `cluster_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_collect_time_desc` (`collect_time` DESC),
  KEY `idx_cid_topic_time_desc` (`cluster_id`,`topic_name`,`collect_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Topic指标采集明细表'

-- 用户信息表
CREATE TABLE `ke_users_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(20) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `origin_password` varchar(100) DEFAULT NULL COMMENT '原始密码',
  `roles` varchar(100) DEFAULT 'ROLE_USER' COMMENT '角色',
  `status` varchar(20) DEFAULT 'ACTIVE' COMMENT '用户状态：ACTIVE-活跃，INACTIVE-非活跃，LOCKED-锁定',
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户信息表'

-- 登录信息表
CREATE TABLE `persistent_logins` (
  `username` varchar(64) NOT NULL,
  `series` varchar(64) NOT NULL,
  `token` varchar(64) NOT NULL,
  `last_used` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`series`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='登录信息表';

-- 插入默认管理员用户
INSERT INTO `ke_users_info` (`username`, `password`, `origin_password`, `roles`, `status`) VALUES
('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'admin', 'ROLE_ADMIN', 'ACTIVE')
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`), `status` = VALUES(`status`);

-- 插入默认任务调度配置
INSERT INTO `ke_task_scheduler` (
    task_name, task_type, cron_expression, description, status,
    last_execute_time, next_execute_time, execute_count, success_count, fail_count,
    last_execute_result, error_message, created_by, create_time, updated_by, update_time,
    config, timeout, node_id, cluster_name
) VALUES
('Topic监控任务', 'topic_monitor', '0 */1 * * * ?', '监控Kafka主题状态，检查分区数量、副本数量等指标',
 'disabled', NULL, NULL, 0, 0, 0, '执行成功',
 NULL, 'admin', NOW(), NULL, NOW(), NULL, 300, NULL, 'cluster-1'),

('消费者组监控任务', 'consumer_monitor', '0 */1 * * * ?', '监控消费者组状态，检查消费延迟、消费速率等指标',
 'disabled', NULL, NULL, 0, 0, 0, '执行成功',
 NULL, 'admin', NOW(), NULL, NOW(), NULL, 300, NULL, 'cluster-1'),

('集群健康检查任务', 'cluster_monitor', '0 */1 * * * ?', '检查Kafka集群健康状态，包括Broker状态、Zookeeper连接等',
 'disabled', NULL, NULL, 0, 0, 0, '执行成功',
 NULL, 'admin', NOW(), NULL, NOW(), NULL, 300, NULL, 'cluster-1'),

('告警监控任务', 'alert_monitor', '0 */1 * * * ?', '监控系统告警状态，实时检测异常情况',
 'disabled', NULL, NULL, 0, 0, 0, '执行成功',
 NULL, 'admin', NOW(), NULL, NOW(), NULL, 600, NULL, 'cluster-1'),

('数据清理任务', 'data_cleanup', '0 */1 * * * ?', '清理过期的监控数据，释放存储空间',
 'disabled', NULL, NULL, 0, 0, 0, '执行成功',
 NULL, 'admin', NOW(), NULL, NOW(), NULL, 1800, NULL, 'cluster-1'),

('性能统计任务', 'performance_stats', '0 */1 * * * ?', '收集系统性能统计数据，包括CPU、内存、网络等指标',
 'disabled', NULL, NULL, 0, 0, 0, '执行成功',
 NULL, 'admin', NOW(), NULL, NOW(), NULL, 300, NULL, 'cluster-1')
ON DUPLICATE KEY UPDATE status = VALUES(status);

