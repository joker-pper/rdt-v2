/*
 Navicat Premium Data Transfer

 Source Server         : mysql_local
 Source Server Type    : MySQL
 Source Server Version : 50717
 Source Host           : localhost:3306
 Source Schema         : rdt-v2-jpa

 Target Server Type    : MySQL
 Target Server Version : 50717
 File Encoding         : 65001

 Date: 21/10/2018 22:36:41
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `id` bigint(20) NOT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (1, '2018-10-21 22:35:47', 'SA');
INSERT INTO `role` VALUES (2, '2018-10-21 22:35:47', 'ADMIN');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint(20) NOT NULL,
  `account_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_by_id` bigint(20) NULL DEFAULT NULL,
  `create_by_id2` bigint(20) NULL DEFAULT NULL,
  `create_by_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_by_name2` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `role_create_time` datetime(0) NULL DEFAULT NULL,
  `role_id` bigint(20) NULL DEFAULT NULL,
  `role_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `type` int(11) NULL DEFAULT NULL,
  `username` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, NULL, 2, NULL, 'ADMIN', NULL, '2018-11-09 16:09:07', '2018-11-09 16:09:06', 2, 'ADMIN', 2, '用户1');
INSERT INTO `user` VALUES (2, 'USER', 1, 1, '用户1', '用户1', '2018-11-09 16:09:07', '2018-11-09 16:09:06', 1, 'SA', 1, '用户2');
INSERT INTO `user` VALUES (3, 'USER', 2, 1, 'ADMIN', '用户1', '2018-11-09 16:09:07', '2018-11-09 16:09:06', 2, 'ADMIN', 2, '用户3');
INSERT INTO `user` VALUES (4, 'USER', 3, 2, '用户3', '用户2', '2018-11-09 16:09:07', '2018-11-09 16:09:06', 2, 'ADMIN', 1, '用户4');
INSERT INTO `user` VALUES (5, 'USER', 1, 4, 'SA', '用户4', '2018-11-09 16:09:07', '2018-11-09 16:09:06', 2, 'ADMIN', 2, '用户5');

SET FOREIGN_KEY_CHECKS = 1;
