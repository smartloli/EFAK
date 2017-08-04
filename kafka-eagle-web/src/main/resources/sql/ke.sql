/*
 Navicat MySQL Data Transfer

 Source Server         : local
 Source Server Version : 50616
 Source Host           : localhost
 Source Database       : ke

 Target Server Version : 50616
 File Encoding         : utf-8

 Date: 06/23/2017 17:02:12 PM
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `ke_p_role`
-- ----------------------------
DROP TABLE IF EXISTS `ke_p_role`;
CREATE TABLE `ke_p_role` (
  `id` tinyint(4) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) CHARACTER SET utf8 NOT NULL COMMENT 'role name',
  `seq` tinyint(4) NOT NULL COMMENT 'rank',
  `description` varchar(128) CHARACTER SET utf8 NOT NULL COMMENT 'role describe',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
--  Records of `ke_p_role`
-- ----------------------------
BEGIN;
INSERT INTO `ke_p_role` VALUES ('1', 'Administrator', '1', 'Have all permissions'), ('2', 'Devs', '2', 'Own add or delete'), ('3', 'Tourist', '3', 'Only viewer');
COMMIT;

-- ----------------------------
--  Table structure for `ke_resources`
-- ----------------------------
DROP TABLE IF EXISTS `ke_resources`;
CREATE TABLE `ke_resources` (
  `resource_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 NOT NULL COMMENT 'resource name',
  `url` varchar(255) NOT NULL,
  `parent_id` int(11) NOT NULL,
  PRIMARY KEY (`resource_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
--  Records of `ke_resources`
-- ----------------------------
BEGIN;
INSERT INTO `ke_resources` VALUES ('1', 'System', '/system', '-1'), ('2', 'User', '/system/user', '1'), ('3', 'Role', '/system/role', '1'), ('4', 'Resource', '/system/resource', '1'), ('5', 'Notice', '/system/notice', '1'), ('6', 'Topic', '/topic', '-1'), ('7', 'Message', '/topic/message', '6'), ('8', 'Create', '/topic/create', '6'), ('9', 'Alarm', '/alarm', '-1'), ('10', 'Add', '/alarm/add', '9'), ('11', 'Modify', '/alarm/modify', '9'), ('12', 'Cluster', '/cluster', '-1'), ('13', 'ZkCli', '/cluster/zkcli', '12'), ('14', 'UserDelete', '/system/user/delete', '1'), ('15', 'UserModify', '/system/user/modify', '1'), ('16', 'Mock', '/topic/mock', '6');
COMMIT;

-- ----------------------------
--  Table structure for `ke_role_resource`
-- ----------------------------
DROP TABLE IF EXISTS `ke_role_resource`;
CREATE TABLE `ke_role_resource` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `role_id` int(11) NOT NULL,
  `resource_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
--  Records of `ke_role_resource`
-- ----------------------------
BEGIN;
INSERT INTO `ke_role_resource` VALUES ('1', '1', '1'), ('2', '1', '2'), ('3', '1', '3'), ('4', '1', '4'), ('5', '1', '5'), ('6', '1', '7'), ('7', '1', '8'), ('8', '1', '10'), ('9', '1', '11'), ('10', '1', '13'), ('11', '2', '7'), ('12', '2', '8'), ('13', '2', '13'), ('14', '2', '10'), ('15', '2', '11'), ('16', '1', '14'), ('17', '1', '15'), ('18', '1', '16');
COMMIT;

-- ----------------------------
--  Table structure for `ke_trend`
-- ----------------------------
DROP TABLE IF EXISTS `ke_trend`;
CREATE TABLE `ke_trend` (
  `cluster` varchar(64) NOT NULL,
  `key` varchar(64) NOT NULL,
  `value` varchar(64) NOT NULL,
  `hour` varchar(2) NOT NULL,
  `tm` varchar(16) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
--  Table structure for `ke_user_role`
-- ----------------------------
DROP TABLE IF EXISTS `ke_user_role`;
CREATE TABLE `ke_user_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `role_id` tinyint(4) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
--  Records of `ke_user_role`
-- ----------------------------
BEGIN;
INSERT INTO `ke_user_role` VALUES ('1', '1', '1');
COMMIT;

-- ----------------------------
--  Table structure for `ke_users`
-- ----------------------------
DROP TABLE IF EXISTS `ke_users`;
CREATE TABLE `ke_users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `rtxno` int(11) NOT NULL,
  `username` varchar(64) NOT NULL,
  `password` varchar(128) NOT NULL,
  `email` varchar(64) NOT NULL,
  `realname` varchar(128) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
--  Records of `ke_users`
-- ----------------------------
BEGIN;
INSERT INTO `ke_users` VALUES ('1', '1000', 'admin', '123456', 'admin@email.com', '系统管理员');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
