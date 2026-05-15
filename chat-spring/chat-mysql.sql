/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50726
Source Host           : localhost:3306
Source Database       : chat

Target Server Type    : MYSQL
Target Server Version : 50726
File Encoding         : 65001

Date: 2021-09-24 11:39:22
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for im_channel
-- ----------------------------
DROP TABLE IF EXISTS `im_channel`;
CREATE TABLE `im_channel` (
  `id` bigint(20) NOT NULL COMMENT '频道主键',
  `type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '类型:0-私聊 1-群聊  2-客服通道 3-服务消息通道',
  `channel_config` longtext COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通讯频道设置',
  `is_need_access` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否需要客服接入:1-是 0-否',
  `join_subscriber_ids` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '加入的用户ID',
  `msg_count` tinyint(4) NOT NULL DEFAULT '0' COMMENT '消息数',
  `create_time` bigint(20) NOT NULL COMMENT '创建时间',
  `update_time` bigint(20) NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `TYPE_IDS` (`type`,`join_subscriber_ids`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通讯频道';

-- ----------------------------
-- Table structure for im_channel_msg
-- ----------------------------
DROP TABLE IF EXISTS `im_channel_msg`;
CREATE TABLE `im_channel_msg` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日志ID',
  `subscriber_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '发送消息的用户ID',
  `channel_id` bigint(20) NOT NULL COMMENT '频道ID',
  `content` longtext COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息JSON内容',
  `time` bigint(20) NOT NULL COMMENT '时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通讯频道消息';

-- ----------------------------
-- Table structure for im_dialogue
-- ----------------------------
DROP TABLE IF EXISTS `im_dialogue`;
CREATE TABLE `im_dialogue` (
  `id` bigint(20) NOT NULL,
  `subscriber_id` bigint(20) NOT NULL COMMENT '订阅者ID',
  `channel_id` bigint(20) NOT NULL COMMENT '通讯频道ID',
  `type` int(11) NOT NULL DEFAULT '0' COMMENT '对话类型: 0-私聊 1-群聊 2-系统助手 3-服务号 4-折叠组',
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对话名称',
  `avatar` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对话头像',
  `brief` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对话简要',
  `last_time` bigint(20) NOT NULL COMMENT '最新消息时间',
  `last_read_time` bigint(20) NOT NULL COMMENT '最后读取消息时间',
  `unread_count` int(11) NOT NULL DEFAULT '0' COMMENT '未读消息数',
  `is_visible` int(11) NOT NULL DEFAULT '1' COMMENT '用户是否可见:1-是 0-否',
  `show_outer` int(11) NOT NULL DEFAULT '1' COMMENT '是否在外层显示:1-是 2-否',
  PRIMARY KEY (`id`),
  UNIQUE KEY `SCT` (`subscriber_id`,`channel_id`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对话';

-- ----------------------------
-- Table structure for im_subscriber
-- ----------------------------
DROP TABLE IF EXISTS `im_subscriber`;
CREATE TABLE `im_subscriber` (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0-普通用户 1-VIP(可以群聊) 2-系统客服',
  `app_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'native' COMMENT '应用ID,native为本系统',
  `unique_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '开放ID,或者本系统手机号',
  `nickname` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '昵称',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '头像',
  `intro` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '简介',
  `create_time` bigint(20) NOT NULL COMMENT '注册时间',
  `update_time` bigint(20) NOT NULL COMMENT '更新时间',
  `last_login_time` bigint(20) NOT NULL COMMENT '最近登录时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `AUQ` (`app_id`,`unique_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订阅者';
