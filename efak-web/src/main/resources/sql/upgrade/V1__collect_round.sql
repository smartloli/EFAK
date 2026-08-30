-- Collector round ledger and round stamps on metric tables.
CREATE TABLE IF NOT EXISTS `ke_collect_round` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `round_id` varchar(128) NOT NULL,
  `task_type` varchar(64) NOT NULL,
  `node_id` varchar(128) NOT NULL,
  `cluster_id` varchar(64) DEFAULT NULL,
  `assigned_count` int NOT NULL DEFAULT 0,
  `success_count` int NOT NULL DEFAULT 0,
  `skipped_count` int NOT NULL DEFAULT 0,
  `error_message` varchar(1024) DEFAULT NULL,
  `started_at` datetime NOT NULL,
  `finished_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_round_id` (`round_id`),
  KEY `idx_task_started` (`task_type`, `started_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Collector round ledger';

ALTER TABLE `ke_topics_metrics` ADD COLUMN `collect_round` varchar(128) DEFAULT NULL COMMENT '采集轮次';
ALTER TABLE `ke_broker_metrics` ADD COLUMN `collect_round` varchar(128) DEFAULT NULL COMMENT '采集轮次';
ALTER TABLE `ke_topic_instant_metrics` ADD COLUMN `collect_round` varchar(128) DEFAULT NULL COMMENT '采集轮次';
