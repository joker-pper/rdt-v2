/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80016
 Source Host           : localhost:3306
 Source Schema         : rdt_v2_jpa

 Target Server Type    : MySQL
 Target Server Version : 80016
 File Encoding         : 65001

 Date: 30/06/2019 22:41:17
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
  `id` varchar(255) COLLATE utf8_bin NOT NULL,
  `name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `price` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` bigint(20) NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `status` int(11) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of role
-- ----------------------------
BEGIN;
INSERT INTO `role` VALUES (2, '2019-06-30 14:33:50', 'ADMIN', 1);
INSERT INTO `role` VALUES (1, '2019-06-30 14:33:50', 'SA', 1);
COMMIT;

-- ----------------------------
-- Table structure for t_order
-- ----------------------------
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order` (
  `id` varchar(255) COLLATE utf8_bin NOT NULL,
  `goods_id` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `price` int(11) DEFAULT NULL,
  `price2` int(11) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL,
  `account_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `create_by_id` bigint(20) DEFAULT NULL,
  `create_by_id2` bigint(20) DEFAULT NULL,
  `create_by_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `create_by_name2` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `role_create_time` datetime DEFAULT NULL,
  `role_id` bigint(20) DEFAULT NULL,
  `role_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `username` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `status` int(11) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
BEGIN;
INSERT INTO `user` VALUES (4, 'USER', 2, 1, '用户2', '用户1', '2019-06-30 14:33:50', '2019-06-30 14:33:50', 2, 'ADMIN', 1, '用户4', 1);
INSERT INTO `user` VALUES (5, 'ROLE', 1, 1, 'SA', 'SA', '2019-06-30 14:33:50', '2019-06-30 14:33:50', 1, 'SA', 2, '用户5', 1);
INSERT INTO `user` VALUES (3, 'ROLE', 2, 2, 'ADMIN', 'ADMIN', '2019-06-30 14:33:50', '2019-06-30 14:33:50', 1, 'SA', 2, '用户3', 1);
INSERT INTO `user` VALUES (1, NULL, 1, NULL, 'SA', NULL, '2019-06-30 14:33:50', '2019-06-30 14:33:50', 1, 'SA', 2, '用户1', 1);
INSERT INTO `user` VALUES (2, 'USER', 1, 1, '用户1', '用户1', '2019-06-30 14:33:50', '2019-06-30 14:33:50', 2, 'ADMIN', 1, '用户2', 1);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
