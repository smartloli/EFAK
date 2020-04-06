/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.plugin.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC constants.
 * 
 * @author smartloli.
 *
 *         Created by Jul 7, 2017
 */
public interface JConstants {

	/** Get databases. */
	public static final String SHOW_DATABASES = "SHOW DATABASES";

	/** Get tables. */
	public static final String SHOW_TABLES = "SHOW TABLES";

	/** Get tables from sqlite db. */
	public static final String SQLITE_TABLES = "select name from sqlite_master";

	/** MySql type. */
	public static final String MYSQL = "mysql";

	/** MySql driver name. */
	public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";

	/** Sqlite type. */
	public static final String SQLITE = "sqlite";

	/** Sqlite driver name. */
	public static final String SQLITE_DRIVER = "org.sqlite.JDBC";

	/** Create database script. */
	public static String CREATE_DB_SQL = "CREATE DATABASE IF NOT EXISTS %s";

	/** Automatically create databases and tables. */
	public static final List<String> TBLS = Arrays.asList("ke_p_role", "ke_resources", "ke_role_resource", "ke_metrics", "ke_metrics_offline", "ke_alarm_consumer", "ke_alarm_clusters", "ke_user_role", "ke_users", "ke_topic_rank", "ke_sql_history", "ke_logsize", "ke_consumer_bscreen", "ke_alarm_config", "ke_consumer_group", "ke_consumer_group_summary");

	static String CREATE_TABLE_KE_P_ROLE = "CREATE TABLE IF NOT EXISTS `ke_p_role` (`id` tinyint(4) NOT NULL AUTO_INCREMENT,`name` varchar(64) CHARACTER SET utf8 NOT NULL COMMENT 'role name',`seq` tinyint(4) NOT NULL COMMENT 'rank',`description` varchar(128) CHARACTER SET utf8 NOT NULL COMMENT 'role describe',PRIMARY KEY (`id`)) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4";
	static String CREATE_TABLE_KE_P_ROLE_INSERT = "INSERT INTO `ke_p_role` VALUES ('1', 'Administrator', '1', 'Have all permissions'), ('2', 'Devs', '2', 'Own add or delete'), ('3', 'Tourist', '3', 'Only viewer')";

	static String CREATE_TABLE_KE_RESOURCES = "CREATE TABLE IF NOT EXISTS `ke_resources` (`resource_id` int(11) NOT NULL AUTO_INCREMENT,`name` varchar(255) CHARACTER SET utf8 NOT NULL COMMENT 'resource name',`url` varchar(255) NOT NULL,`parent_id` int(11) NOT NULL,PRIMARY KEY (`resource_id`)) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4";
	static String CREATE_TABLE_KE_RESOURCES_INSERT = "INSERT INTO `ke_resources` VALUES ('1', 'System', '/system', '-1'), ('2', 'User', '/system/user', '1'), ('3', 'Role', '/system/role', '1'), ('4', 'Resource', '/system/resource', '1'), ('5', 'Notice', '/system/notice', '1'), ('6', 'Topic', '/topic', '-1'), ('7', 'Message', '/topic/message', '6'), ('8', 'Create', '/topic/create', '6'), ('9', 'Alarm', '/alarm', '-1'), ('10', 'Add', '/alarm/add', '9'), ('11', 'Modify', '/alarm/modify', '9'), ('12', 'Cluster', '/cluster', '-1'), ('13', 'ZkCli', '/cluster/zkcli', '12'), ('14', 'UserDelete', '/system/user/delete', '1'), ('15', 'UserModify', '/system/user/modify', '1'), ('16', 'Mock', '/topic/mock', '6'), ('18', 'Create', '/alarm/create', '9'), ('19', 'History', '/alarm/history', '9'), ('20', 'Manager', '/topic/manager', '6'), ('21', 'PasswdReset', '/system/user/reset', '1'), ('22', 'Config', '/alarm/config', '9'), ('23', 'List', '/alarm/list', '9')";

	static String CREATE_TABLE_KE_ROLE_RESOURCE = "CREATE TABLE IF NOT EXISTS `ke_role_resource` (`id` int(11) NOT NULL AUTO_INCREMENT,`role_id` int(11) NOT NULL,`resource_id` int(11) NOT NULL,PRIMARY KEY (`id`)) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4";
	static String CREATE_TABLE_KE_ROLE_RESOURCE_INSERT = "INSERT INTO `ke_role_resource` VALUES ('1', '1', '1'), ('2', '1', '2'), ('3', '1', '3'), ('4', '1', '4'), ('5', '1', '5'), ('6', '1', '7'), ('7', '1', '8'), ('8', '1', '10'), ('9', '1', '11'), ('10', '1', '13'), ('11', '2', '7'), ('12', '2', '8'), ('13', '2', '13'), ('14', '2', '10'), ('15', '2', '11'), ('16', '1', '14'), ('17', '1', '15'), ('18', '1', '16'), ('19', '1', '18'), ('20', '1', '19'), ('21', '1', '20'), ('22', '1', '21'), ('23', '1', '22'), ('24', '1', '23')";

	static String CREATE_TABLE_KE_METRICS = "CREATE TABLE IF NOT EXISTS `ke_metrics` (`cluster` varchar(64) DEFAULT NULL,`broker` text DEFAULT NULL,`type` varchar(32) DEFAULT NULL,`key` varchar(64) DEFAULT NULL,`value` varchar(128) DEFAULT NULL,`timespan` bigint(20) DEFAULT NULL,`tm` varchar(16) DEFAULT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

	static String CREATE_TABLE_KE_METRICS_OFFLINE = "CREATE TABLE IF NOT EXISTS `ke_metrics_offline` (`cluster` varchar(64) NOT NULL,`key` varchar(128) NOT NULL,`one` varchar(128) DEFAULT NULL,`mean` varchar(128) DEFAULT NULL,`five` varchar(128) DEFAULT NULL,`fifteen` varchar(128) DEFAULT NULL, PRIMARY KEY (`cluster`,`key`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

	static String CREATE_TABLE_KE_ALARM_CONSUMER = "CREATE TABLE IF NOT EXISTS `ke_alarm_consumer` (`id` int(11) NOT NULL AUTO_INCREMENT,`cluster` varchar(64) DEFAULT NULL,`group` varchar(128) DEFAULT NULL,`topic` varchar(128) DEFAULT NULL,`lag` bigint(20) DEFAULT NULL,`alarm_group` varchar(128) DEFAULT NULL,`alarm_times` int(11),`alarm_max_times` int(11) , `alarm_level` varchar(4),`is_normal` varchar(2) DEFAULT 'Y', `is_enable` varchar(2) DEFAULT 'Y', `created` varchar(32) DEFAULT NULL,`modify` varchar(32) DEFAULT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

	static String CREATE_TABLE_KE_ALARM_CONFIG = "CREATE TABLE IF NOT EXISTS `ke_alarm_config` (`cluster` varchar(64) NOT NULL,`alarm_group` varchar(128) NOT NULL,`alarm_type` varchar(16) DEFAULT NULL,`alarm_url` text DEFAULT NULL,`http_method` varchar(16) DEFAULT NULL,`alarm_address` text DEFAULT NULL,`created` varchar(32) DEFAULT NULL,`modify` varchar(32) DEFAULT NULL, PRIMARY KEY (`cluster`,`alarm_group`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

	static String CREATE_TABLE_KE_ALARM_CLUSTERS = "CREATE TABLE IF NOT EXISTS `ke_alarm_clusters` (`id` int(11) NOT NULL AUTO_INCREMENT,`type` varchar(32) DEFAULT NULL,`cluster` varchar(64) DEFAULT NULL,`server` text DEFAULT NULL,`alarm_group` varchar(128) DEFAULT NULL,`alarm_times` int(11),`alarm_max_times` int(11) , `alarm_level` varchar(4),`is_normal` varchar(2) DEFAULT 'Y',`is_enable` varchar(2) DEFAULT 'Y', `created` varchar(32) DEFAULT NULL,`modify` varchar(32) DEFAULT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

	static String CREATE_TABLE_KE_TOPIC_RANK = "CREATE TABLE IF NOT EXISTS `ke_topic_rank` (`cluster` varchar(64) NOT NULL,`topic` varchar(64) NOT NULL,`tkey` varchar(64) NOT NULL,`tvalue` bigint(20) DEFAULT NULL,PRIMARY KEY (`cluster`,`topic`,`tkey`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

	static String CREATE_TABLE_KE_SQL_HISTORY = "CREATE TABLE IF NOT EXISTS `ke_sql_history` (`id` int(11) NOT NULL AUTO_INCREMENT, `cluster` varchar(64) DEFAULT NULL,`username` varchar(64) DEFAULT NULL,`host` varchar(128) DEFAULT NULL,`ksql` text DEFAULT NULL,`status` varchar(16) DEFAULT NULL,`spend_time` bigint(20) DEFAULT NULL,`created` varchar(32) DEFAULT NULL,`tm` varchar(16) DEFAULT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

	static String CREATE_TABLE_KE_LOGSIZE = "CREATE TABLE IF NOT EXISTS `ke_logsize` (`cluster` varchar(64) DEFAULT NULL,`topic` varchar(64) DEFAULT NULL,`logsize` bigint(20) DEFAULT NULL,`diffval` bigint(20) DEFAULT NULL,`timespan` bigint(20) DEFAULT NULL,`tm` varchar(16) DEFAULT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

	static String CREATE_TABLE_KE_CONSUMER_BSCREEN = "CREATE TABLE IF NOT EXISTS `ke_consumer_bscreen` (`cluster` varchar(64) DEFAULT NULL,`group` varchar(128) DEFAULT NULL,`topic` varchar(64) DEFAULT NULL,`logsize` bigint(20) DEFAULT NULL,`difflogsize` bigint(20) DEFAULT NULL,`offsets` bigint(20) DEFAULT NULL,`diffoffsets` bigint(20) DEFAULT NULL,`lag` bigint(20) DEFAULT NULL,`timespan` bigint(20) DEFAULT NULL,`tm` varchar(16) DEFAULT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

	static String CREATE_TABLE_KE_CONSUMER_GROUP = "CREATE TABLE IF NOT EXISTS `ke_consumer_group` (`cluster` varchar(64) NOT NULL,`group` varchar(128) NOT NULL,`topic` varchar(128) NOT NULL,`status` int(11) DEFAULT NULL, PRIMARY KEY (`cluster`,`group`,`topic`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

	static String CREATE_TABLE_KE_CONSUMER_GROUP_SUMMARY = "CREATE TABLE IF NOT EXISTS `ke_consumer_group_summary` (`cluster` varchar(64) NOT NULL,`group` varchar(128) NOT NULL,`topic_number` varchar(128) NOT NULL,`coordinator`varchar(128) DEFAULT NULL,`active_topic` int(11) DEFAULT NULL,`active_thread_total` int(11) DEFAULT NULL, PRIMARY KEY (`cluster`,`group`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

	static String CREATE_TABLE_KE_USER_ROLE = "CREATE TABLE IF NOT EXISTS `ke_user_role` (`id` int(11) NOT NULL AUTO_INCREMENT,`user_id` int(11) NOT NULL,`role_id` tinyint(4) NOT NULL,PRIMARY KEY (`id`)) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4";
	static String CREATE_TABLE_KE_USER_ROLE_INSERT = "INSERT INTO `ke_user_role` VALUES ('1', '1', '1');";

	static String CREATE_TABLE_KE_USERS = "CREATE TABLE IF NOT EXISTS `ke_users` (`id` int(11) NOT NULL AUTO_INCREMENT,`rtxno` int(11) NOT NULL,`username` varchar(64) NOT NULL,`password` varchar(128) NOT NULL,`email` varchar(64) NOT NULL,`realname` varchar(128) NOT NULL,PRIMARY KEY (`id`)) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4";
	static String CREATE_TABLE_KE_USERS_INSERT = "INSERT INTO `ke_users` VALUES ('1', '1000', 'admin', '123456', 'admin@email.com', 'Administrator');";

	/** Sqlite Sql. */
	static String CREATE_TABLE_SQLITE_KE_P_ROLE = "CREATE TABLE IF NOT EXISTS `ke_p_role` (`id` integer primary key autoincrement,`name` varchar(64) NOT NULL,`seq` tinyint(4) NOT NULL,`description` varchar(128) NOT NULL)";
	static String CREATE_TABLE_SQLITE_KE_P_ROLE_INSERT = "INSERT INTO `ke_p_role` VALUES ('1', 'Administrator', '1', 'Have all permissions'), ('2', 'Devs', '2', 'Own add or delete'), ('3', 'Tourist', '3', 'Only viewer')";

	static String CREATE_TABLE_SQLITE_KE_RESOURCES = "CREATE TABLE IF NOT EXISTS `ke_resources` (`resource_id` integer primary key autoincrement,`name` varchar(255),`url` varchar(255),`parent_id` int(11))";
	static String CREATE_TABLE_SQLITE_KE_RESOURCES_INSERT = "INSERT INTO `ke_resources` VALUES ('1', 'System', '/system', '-1'), ('2', 'User', '/system/user', '1'), ('3', 'Role', '/system/role', '1'), ('4', 'Resource', '/system/resource', '1'), ('5', 'Notice', '/system/notice', '1'), ('6', 'Topic', '/topic', '-1'), ('7', 'Message', '/topic/message', '6'), ('8', 'Create', '/topic/create', '6'), ('9', 'Alarm', '/alarm', '-1'), ('10', 'Add', '/alarm/add', '9'), ('11', 'Modify', '/alarm/modify', '9'), ('12', 'Cluster', '/cluster', '-1'), ('13', 'ZkCli', '/cluster/zkcli', '12'), ('14', 'UserDelete', '/system/user/delete', '1'), ('15', 'UserModify', '/system/user/modify', '1'), ('16', 'Mock', '/topic/mock', '6'), ('18', 'Create', '/alarm/create', '9'), ('19', 'History', '/alarm/history', '9'), ('20', 'Manager', '/topic/manager', '6'), ('21', 'PasswdReset', '/system/user/reset', '1'), ('22', 'Config', '/alarm/config', '9'), ('23', 'List', '/alarm/list', '9')";

	static String CREATE_TABLE_SQLITE_KE_ROLE_RESOURCE = "CREATE TABLE IF NOT EXISTS `ke_role_resource` (`id` integer primary key autoincrement,`role_id` int(11),`resource_id` int(11))";
	static String CREATE_TABLE_SQLITE_KE_ROLE_RESOURCE_INSERT = "INSERT INTO `ke_role_resource` VALUES ('1', '1', '1'), ('2', '1', '2'), ('3', '1', '3'), ('4', '1', '4'), ('5', '1', '5'), ('6', '1', '7'), ('7', '1', '8'), ('8', '1', '10'), ('9', '1', '11'), ('10', '1', '13'), ('11', '2', '7'), ('12', '2', '8'), ('13', '2', '13'), ('14', '2', '10'), ('15', '2', '11'), ('16', '1', '14'), ('17', '1', '15'), ('18', '1', '16'), ('19', '1', '18'), ('20', '1', '19'), ('21', '1', '20'), ('22', '1', '21'), ('23', '1', '22'), ('24', '1', '23')";

	static String CREATE_TABLE_SQLITE_KE_METRICS = "CREATE TABLE IF NOT EXISTS `ke_metrics` (`cluster` varchar(64),`broker` text,`type` varchar(32),`key` varchar(64),`value` varchar(128),`timespan` bigint(20),`tm` varchar(16))";

	static String CREATE_TABLE_SQLITE_KE_METRICS_OFFLINE = "CREATE TABLE IF NOT EXISTS `ke_metrics_offline` (`cluster` varchar(64) ,`key` varchar(128) ,`one` varchar(128) ,`mean` varchar(128) ,`five` varchar(128) ,`fifteen` varchar(128) , PRIMARY KEY (`cluster`,`key`))";

	static String CREATE_TABLE_SQLITE_KE_ALARM_CONSUMER = "CREATE TABLE IF NOT EXISTS `ke_alarm_consumer` (`id` integer primary key autoincrement,`cluster` varchar(64),`group` varchar(128),`topic` varchar(128),`lag` bigint(20),`alarm_group` varchar(128) ,`alarm_times` int(11),`alarm_max_times` int(11) , `alarm_level` varchar(4), `is_normal` varchar(2) DEFAULT 'Y', `is_enable` varchar(2) DEFAULT 'Y',`created` varchar(32),`modify` varchar(32))";

	static String CREATE_TABLE_SQLITE_KE_ALARM_CONFIG = "CREATE TABLE IF NOT EXISTS `ke_alarm_config` (`cluster` varchar(64) ,`alarm_group` varchar(128) ,`alarm_type` varchar(16) ,`alarm_url` text ,`http_method` varchar(16) ,`alarm_address` text ,`created` varchar(32) ,`modify` varchar(32) , PRIMARY KEY (`cluster`,`alarm_group`))";

	static String CREATE_TABLE_SQLITE_KE_ALARM_CLUSTERS = "CREATE TABLE IF NOT EXISTS `ke_alarm_clusters` (`id` integer primary key autoincrement,`type` varchar(32),`cluster` varchar(64),`server` text,`alarm_group` text ,`alarm_times` int(11),`alarm_max_times` int(11) , `alarm_level` varchar(4),`is_normal` varchar(2) DEFAULT 'Y', `is_enable` varchar(2) DEFAULT 'Y',`created` varchar(32),`modify` varchar(32))";

	static String CREATE_TABLE_SQLITE_KE_TOPIC_RANK = "CREATE TABLE IF NOT EXISTS `ke_topic_rank` (`cluster` varchar(64) ,`topic` varchar(128) ,`tkey` varchar(128) ,`tvalue` bigint(20),primary key (`cluster`,`topic`,`tkey`))";

	static String CREATE_TABLE_SQLITE_KE_SQL_HISTORY = "CREATE TABLE IF NOT EXISTS `ke_sql_history` (`id` integer primary key autoincrement,`cluster` varchar(64) ,`username` varchar(64),`host` varchar(128) ,`ksql` text ,`status` varchar(16) ,`spend_time` bigint(20) ,`created` varchar(32),`tm` varchar(16))";

	static String CREATE_TABLE_SQLITE_KE_LOGSIZE = "CREATE TABLE IF NOT EXISTS `ke_logsize` (`cluster` varchar(64) ,`topic` varchar(64) ,`logsize` bigint(20) ,`diffval` bigint(20) ,`timespan` bigint(20) ,`tm` varchar(16))";

	static String CREATE_TABLE_SQLITE_KE_CONSUMER_BSCREEN = "CREATE TABLE IF NOT EXISTS `ke_consumer_bscreen` (`cluster` varchar(64) ,`group` varchar(128) ,`topic` varchar(64) ,`logsize` bigint(20) ,`difflogsize` bigint(20) ,`offsets` bigint(20) ,`diffoffsets` bigint(20) ,`lag` bigint(20) ,`timespan` bigint(20) ,`tm` varchar(16))";

	static String CREATE_TABLE_SQLITE_KE_CONSUMER_GROUP = "CREATE TABLE IF NOT EXISTS `ke_consumer_group` (`cluster` varchar(64) ,`group` varchar(128) ,`topic` varchar(128) ,`status` int(11), primary key (`cluster`,`group`,`topic`))";

	static String CREATE_TABLE_SQLITE_KE_CONSUMER_GROUP_SUMMARY = "CREATE TABLE IF NOT EXISTS `ke_consumer_group_summary` (`cluster` varchar(64) ,`group` varchar(128) ,`topic_number` varchar(128),`coordinator`varchar(128),`active_topic` int(11),`active_thread_total` int(11), primary key (`cluster`,`group`))";

	static String CREATE_TABLE_SQLITE_KE_USER_ROLE = "CREATE TABLE IF NOT EXISTS `ke_user_role` (`id` integer primary key autoincrement,`user_id` int(11),`role_id` tinyint(4))";
	static String CREATE_TABLE_SQLITE_KE_USER_ROLE_INSERT = "INSERT INTO `ke_user_role` VALUES ('1', '1', '1')";

	static String CREATE_TABLE_SQLITE_KE_USERS = "CREATE TABLE IF NOT EXISTS `ke_users` (`id` integer primary key autoincrement,`rtxno` int(11) NOT NULL,`username` varchar(64) NOT NULL,`password` varchar(128) NOT NULL,`email` varchar(64) NOT NULL,`realname` varchar(128) NOT NULL)";
	static String CREATE_TABLE_SQLITE_KE_USERS_INSERT = "INSERT INTO `ke_users` VALUES ('1', '1000', 'admin', '123456', 'admin@email.com', 'Administrator')";

	/** Create tables script. */
	public static final Map<String, String> KEYS = new HashMap<String, String>() {

		private static final long serialVersionUID = 1L;

		{
			// Mysql
			put("CREATE_TABLE_KE_P_ROLE", CREATE_TABLE_KE_P_ROLE);
			put("CREATE_TABLE_KE_P_ROLE_INSERT", CREATE_TABLE_KE_P_ROLE_INSERT);
			put("CREATE_TABLE_KE_RESOURCES", CREATE_TABLE_KE_RESOURCES);
			put("CREATE_TABLE_KE_RESOURCES_INSERT", CREATE_TABLE_KE_RESOURCES_INSERT);
			put("CREATE_TABLE_KE_ROLE_RESOURCE", CREATE_TABLE_KE_ROLE_RESOURCE);
			put("CREATE_TABLE_KE_ROLE_RESOURCE_INSERT", CREATE_TABLE_KE_ROLE_RESOURCE_INSERT);
			put("CREATE_TABLE_KE_METRICS", CREATE_TABLE_KE_METRICS);
			put("CREATE_TABLE_KE_METRICS_OFFLINE", CREATE_TABLE_KE_METRICS_OFFLINE);
			put("CREATE_TABLE_KE_ALARM_CONSUMER", CREATE_TABLE_KE_ALARM_CONSUMER);
			put("CREATE_TABLE_KE_ALARM_CONFIG", CREATE_TABLE_KE_ALARM_CONFIG);
			put("CREATE_TABLE_KE_ALARM_CLUSTERS", CREATE_TABLE_KE_ALARM_CLUSTERS);
			put("CREATE_TABLE_KE_CONSUMER_GROUP", CREATE_TABLE_KE_CONSUMER_GROUP);
			put("CREATE_TABLE_KE_CONSUMER_GROUP_SUMMARY", CREATE_TABLE_KE_CONSUMER_GROUP_SUMMARY);
			put("CREATE_TABLE_KE_TOPIC_RANK", CREATE_TABLE_KE_TOPIC_RANK);
			put("CREATE_TABLE_KE_SQL_HISTORY", CREATE_TABLE_KE_SQL_HISTORY);
			put("CREATE_TABLE_KE_LOGSIZE", CREATE_TABLE_KE_LOGSIZE);
			put("CREATE_TABLE_KE_CONSUMER_BSCREEN", CREATE_TABLE_KE_CONSUMER_BSCREEN);
			put("CREATE_TABLE_KE_USER_ROLE", CREATE_TABLE_KE_USER_ROLE);
			put("CREATE_TABLE_KE_USER_ROLE_INSERT", CREATE_TABLE_KE_USER_ROLE_INSERT);
			put("CREATE_TABLE_KE_USERS", CREATE_TABLE_KE_USERS);
			put("CREATE_TABLE_KE_USERS_INSERT", CREATE_TABLE_KE_USERS_INSERT);
			// Sqlite
			put("CREATE_TABLE_SQLITE_KE_P_ROLE", CREATE_TABLE_SQLITE_KE_P_ROLE);
			put("CREATE_TABLE_SQLITE_KE_P_ROLE_INSERT", CREATE_TABLE_SQLITE_KE_P_ROLE_INSERT);
			put("CREATE_TABLE_SQLITE_KE_RESOURCES", CREATE_TABLE_SQLITE_KE_RESOURCES);
			put("CREATE_TABLE_SQLITE_KE_RESOURCES_INSERT", CREATE_TABLE_SQLITE_KE_RESOURCES_INSERT);
			put("CREATE_TABLE_SQLITE_KE_ROLE_RESOURCE", CREATE_TABLE_SQLITE_KE_ROLE_RESOURCE);
			put("CREATE_TABLE_SQLITE_KE_ROLE_RESOURCE_INSERT", CREATE_TABLE_SQLITE_KE_ROLE_RESOURCE_INSERT);
			put("CREATE_TABLE_SQLITE_KE_METRICS", CREATE_TABLE_SQLITE_KE_METRICS);
			put("CREATE_TABLE_SQLITE_KE_METRICS_OFFLINE", CREATE_TABLE_SQLITE_KE_METRICS_OFFLINE);
			put("CREATE_TABLE_SQLITE_KE_ALARM_CONSUMER", CREATE_TABLE_SQLITE_KE_ALARM_CONSUMER);
			put("CREATE_TABLE_SQLITE_KE_ALARM_CONFIG", CREATE_TABLE_SQLITE_KE_ALARM_CONFIG);
			put("CREATE_TABLE_SQLITE_KE_ALARM_CLUSTERS", CREATE_TABLE_SQLITE_KE_ALARM_CLUSTERS);
			put("CREATE_TABLE_SQLITE_KE_CONSUMER_GROUP", CREATE_TABLE_SQLITE_KE_CONSUMER_GROUP);
			put("CREATE_TABLE_SQLITE_KE_CONSUMER_GROUP_SUMMARY", CREATE_TABLE_SQLITE_KE_CONSUMER_GROUP_SUMMARY);
			put("CREATE_TABLE_SQLITE_KE_TOPIC_RANK", CREATE_TABLE_SQLITE_KE_TOPIC_RANK);
			put("CREATE_TABLE_SQLITE_KE_SQL_HISTORY", CREATE_TABLE_SQLITE_KE_SQL_HISTORY);
			put("CREATE_TABLE_SQLITE_KE_LOGSIZE", CREATE_TABLE_SQLITE_KE_LOGSIZE);
			put("CREATE_TABLE_SQLITE_KE_CONSUMER_BSCREEN", CREATE_TABLE_SQLITE_KE_CONSUMER_BSCREEN);
			put("CREATE_TABLE_SQLITE_KE_USER_ROLE", CREATE_TABLE_SQLITE_KE_USER_ROLE);
			put("CREATE_TABLE_SQLITE_KE_USER_ROLE_INSERT", CREATE_TABLE_SQLITE_KE_USER_ROLE_INSERT);
			put("CREATE_TABLE_SQLITE_KE_USERS", CREATE_TABLE_SQLITE_KE_USERS);
			put("CREATE_TABLE_SQLITE_KE_USERS_INSERT", CREATE_TABLE_SQLITE_KE_USERS_INSERT);
		}

	};

	/** Mac system . */
	public static final String MAC = "Mac";

	/** Windows system. */
	public static final String WIN = "Win";

}
