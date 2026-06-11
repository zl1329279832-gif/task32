-- ============================================================
-- 测试数据库 schema (H2 MySQL兼容模式)
-- ============================================================

-- 系统表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `user_id` bigint NOT NULL,
  `user_name` varchar(30) NOT NULL,
  `nick_name` varchar(30) NOT NULL,
  `email` varchar(50) DEFAULT '',
  `phonenumber` varchar(11) DEFAULT '',
  `sex` char(1) DEFAULT '0',
  `avatar` varchar(100) DEFAULT '',
  `password` varchar(100) DEFAULT '',
  `status` char(1) DEFAULT '0',
  `del_flag` bit(1) DEFAULT b'0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_user` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_user` bigint DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
);

CREATE TABLE IF NOT EXISTS `sys_role` (
  `role_id` bigint NOT NULL,
  `role_name` varchar(30) NOT NULL,
  `role_key` varchar(100) NOT NULL,
  `role_sort` int DEFAULT 0,
  `status` char(1) DEFAULT '0',
  `del_flag` bit(1) DEFAULT b'0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_user` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_user` bigint DEFAULT NULL,
  PRIMARY KEY (`role_id`)
);

CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`)
);

CREATE TABLE IF NOT EXISTS `sys_role_menu` (
  `role_id` bigint NOT NULL,
  `menu_id` bigint NOT NULL,
  PRIMARY KEY (`role_id`, `menu_id`)
);

CREATE TABLE IF NOT EXISTS `sys_role_resource` (
  `role_id` bigint NOT NULL,
  `resource_id` bigint NOT NULL,
  PRIMARY KEY (`role_id`, `resource_id`)
);

CREATE TABLE IF NOT EXISTS `sys_menu` (
  `menu_id` bigint NOT NULL,
  `menu_name` varchar(50) NOT NULL,
  `parent_id` bigint DEFAULT 0,
  `order_num` int DEFAULT 0,
  `path` varchar(200) DEFAULT '',
  `component` varchar(255) DEFAULT NULL,
  `menu_type` char(1) DEFAULT '',
  `perms` varchar(100) DEFAULT NULL,
  `icon` varchar(100) DEFAULT '#',
  `status` char(1) DEFAULT '0',
  `del_flag` bit(1) DEFAULT b'0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_user` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_user` bigint DEFAULT NULL,
  PRIMARY KEY (`menu_id`)
);

CREATE TABLE IF NOT EXISTS `sys_resource` (
  `resource_id` bigint NOT NULL,
  `resource_code` varchar(200) DEFAULT NULL,
  `resource_name` varchar(200) DEFAULT NULL,
  `app_code` varchar(100) DEFAULT NULL,
  `class_name` varchar(200) DEFAULT NULL,
  `method_name` varchar(200) DEFAULT NULL,
  `modular_type` varchar(50) DEFAULT NULL,
  `modular_code` varchar(100) DEFAULT NULL,
  `modular_name` varchar(100) DEFAULT NULL,
  `required_flag` tinyint DEFAULT 0,
  `status` char(1) DEFAULT '0',
  `del_flag` bit(1) DEFAULT b'0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_user` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_user` bigint DEFAULT NULL,
  PRIMARY KEY (`resource_id`)
);

CREATE TABLE IF NOT EXISTS `sys_config` (
  `config_id` bigint NOT NULL,
  `config_name` varchar(100) DEFAULT '',
  `config_key` varchar(100) DEFAULT '',
  `config_value` varchar(500) DEFAULT '',
  `config_type` char(1) DEFAULT 'N',
  `del_flag` bit(1) DEFAULT b'0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_user` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_user` bigint DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`config_id`)
);

-- 业务表
CREATE TABLE IF NOT EXISTS `campus_category` (
  `category_id` bigint NOT NULL,
  `category_name` varchar(50) NOT NULL,
  `parent_id` bigint DEFAULT 0,
  `order_num` int DEFAULT 0,
  `type` int DEFAULT 0,
  `description` varchar(500) DEFAULT NULL,
  `slug` varchar(100) DEFAULT NULL,
  `status` char(1) DEFAULT '0',
  `icon` varchar(100) DEFAULT NULL,
  `del_flag` bit(1) DEFAULT b'0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_user` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_user` bigint DEFAULT NULL,
  PRIMARY KEY (`category_id`)
);

CREATE TABLE IF NOT EXISTS `campus_content` (
  `content_id` bigint NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  `content` varchar(1000) DEFAULT NULL,
  `status` int DEFAULT 0,
  `type` int DEFAULT 0,
  `file_count` int DEFAULT 0,
  `love_count` bigint DEFAULT 0,
  `is_anonymous` int DEFAULT 0,
  `remark` varchar(500) DEFAULT NULL,
  `del_flag` bit(1) DEFAULT b'0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_user` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_user` bigint DEFAULT NULL,
  PRIMARY KEY (`content_id`)
);

CREATE TABLE IF NOT EXISTS `campus_comment` (
  `comment_id` bigint NOT NULL,
  `parent_id` bigint DEFAULT 0,
  `user_id` bigint DEFAULT NULL,
  `to_user_id` bigint DEFAULT NULL,
  `one_level_id` bigint DEFAULT -1,
  `content_id` bigint DEFAULT NULL,
  `co_content` varchar(500) DEFAULT NULL,
  `ip` varchar(50) DEFAULT NULL,
  `address` varchar(100) DEFAULT NULL,
  `frozen_status` int DEFAULT 0,
  `del_flag` bit(1) DEFAULT b'0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_user` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_user` bigint DEFAULT NULL,
  PRIMARY KEY (`comment_id`)
);

CREATE TABLE IF NOT EXISTS `campus_content_love` (
  `user_id` bigint NOT NULL,
  `content_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `content_id`)
);

CREATE TABLE IF NOT EXISTS `campus_file` (
  `file_id` bigint NOT NULL,
  `content_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `url` varchar(500) DEFAULT NULL,
  `violation_status` int DEFAULT 0,
  `violation_reason` varchar(200) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`file_id`)
);

CREATE TABLE IF NOT EXISTS `campus_tag` (
  `tag_id` bigint NOT NULL,
  `tag_name` varchar(50) NOT NULL,
  `description` varchar(200) DEFAULT NULL,
  `status` char(1) DEFAULT '0',
  `del_flag` bit(1) DEFAULT b'0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_user` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_user` bigint DEFAULT NULL,
  PRIMARY KEY (`tag_id`)
);

CREATE TABLE IF NOT EXISTS `campus_content_tag` (
  `content_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  `tag_name` varchar(50) DEFAULT NULL
);

-- 治理表
CREATE TABLE IF NOT EXISTS `campus_sensitive_word` (
  `word_id` bigint NOT NULL,
  `word` varchar(100) NOT NULL,
  `category` varchar(50) DEFAULT 'default',
  `severity` int DEFAULT 1,
  `status` char(1) DEFAULT '0',
  `del_flag` bit(1) DEFAULT b'0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_user` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_user` bigint DEFAULT NULL,
  PRIMARY KEY (`word_id`)
);

CREATE TABLE IF NOT EXISTS `campus_user_credit` (
  `credit_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `credit_score` int DEFAULT 100,
  `del_flag` bit(1) DEFAULT b'0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_user` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_user` bigint DEFAULT NULL,
  PRIMARY KEY (`credit_id`)
);

CREATE TABLE IF NOT EXISTS `campus_credit_log` (
  `log_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `change_value` int NOT NULL,
  `reason` varchar(200) DEFAULT NULL,
  `related_type` varchar(30) DEFAULT NULL,
  `related_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`)
);

CREATE TABLE IF NOT EXISTS `campus_moderation_rule` (
  `rule_id` bigint NOT NULL,
  `rule_type` varchar(20) NOT NULL,
  `rule_key` varchar(100) DEFAULT NULL,
  `rule_value` varchar(200) NOT NULL,
  `action` varchar(20) DEFAULT 'PENDING',
  `priority` int DEFAULT 0,
  `status` char(1) DEFAULT '0',
  `description` varchar(300) DEFAULT NULL,
  `del_flag` bit(1) DEFAULT b'0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_user` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_user` bigint DEFAULT NULL,
  PRIMARY KEY (`rule_id`)
);

CREATE TABLE IF NOT EXISTS `campus_moderation_record` (
  `record_id` bigint NOT NULL,
  `content_id` bigint NOT NULL,
  `target_type` varchar(20) DEFAULT 'CONTENT',
  `target_id` bigint DEFAULT NULL,
  `moderation_type` varchar(20) NOT NULL,
  `action` varchar(20) NOT NULL,
  `reason` varchar(500) DEFAULT NULL,
  `matched_rules` varchar(1000) DEFAULT NULL,
  `admin_id` bigint DEFAULT NULL,
  `admin_name` varchar(50) DEFAULT NULL,
  `before_status` int DEFAULT NULL,
  `after_status` int DEFAULT NULL,
  `snapshot_love_count` bigint DEFAULT NULL,
  `snapshot_comment_count` bigint DEFAULT NULL,
  `del_flag` bit(1) DEFAULT b'0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_user` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_user` bigint DEFAULT NULL,
  PRIMARY KEY (`record_id`)
);

CREATE TABLE IF NOT EXISTS `campus_appeal` (
  `appeal_id` bigint NOT NULL,
  `content_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `appeal_reason` varchar(1000) NOT NULL,
  `appeal_status` int DEFAULT 0,
  `admin_id` bigint DEFAULT NULL,
  `admin_name` varchar(50) DEFAULT NULL,
  `review_comment` varchar(500) DEFAULT NULL,
  `review_time` datetime DEFAULT NULL,
  `del_flag` bit(1) DEFAULT b'0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_user` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_user` bigint DEFAULT NULL,
  PRIMARY KEY (`appeal_id`)
);

CREATE TABLE IF NOT EXISTS `campus_interaction_snapshot` (
  `snapshot_id` bigint NOT NULL,
  `content_id` bigint NOT NULL,
  `love_count` bigint DEFAULT 0,
  `comment_count` bigint DEFAULT 0,
  `snapshot_type` varchar(20) DEFAULT 'TAKEDOWN',
  `moderation_record_id` bigint DEFAULT NULL,
  `del_flag` bit(1) DEFAULT b'0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_user` bigint DEFAULT NULL,
  PRIMARY KEY (`snapshot_id`)
);

CREATE TABLE IF NOT EXISTS `campus_admin_moderation_scope` (
  `scope_id` bigint NOT NULL,
  `admin_user_id` bigint NOT NULL,
  `scope_type` varchar(20) DEFAULT 'CATEGORY',
  `scope_value` bigint DEFAULT NULL,
  `del_flag` bit(1) DEFAULT b'0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_user` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_user` bigint DEFAULT NULL,
  PRIMARY KEY (`scope_id`)
);

CREATE TABLE IF NOT EXISTS `campus_violation_record` (
  `violation_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `content_id` bigint DEFAULT NULL,
  `violation_type` varchar(30) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `matched_words` varchar(500) DEFAULT NULL,
  `moderation_record_id` bigint DEFAULT NULL,
  `del_flag` bit(1) DEFAULT b'0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_user` bigint DEFAULT NULL,
  PRIMARY KEY (`violation_id`)
);

CREATE TABLE IF NOT EXISTS `sys_login_log` (
  `info_id` bigint NOT NULL,
  `user_name` varchar(50) DEFAULT '',
  `ipaddr` varchar(128) DEFAULT '',
  `login_location` varchar(255) DEFAULT '',
  `browser` varchar(50) DEFAULT '',
  `os` varchar(50) DEFAULT '',
  `status` char(1) DEFAULT '0',
  `msg` varchar(255) DEFAULT '',
  `login_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`info_id`)
);

CREATE TABLE IF NOT EXISTS `sys_oper_log` (
  `oper_id` bigint NOT NULL,
  `log_content` varchar(500) DEFAULT NULL,
  `app_name` varchar(100) DEFAULT NULL,
  `oper_url` varchar(255) DEFAULT '',
  `oper_param` varchar(2000) DEFAULT '',
  `json_result` varchar(2000) DEFAULT '',
  `status` int DEFAULT 0,
  `error_msg` varchar(2000) DEFAULT '',
  `oper_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `oper_user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`oper_id`)
);

CREATE TABLE IF NOT EXISTS `sys_dict_type` (
  `dict_id` bigint NOT NULL,
  `dict_name` varchar(100) DEFAULT '',
  `dict_type` varchar(100) DEFAULT '',
  `status` char(1) DEFAULT '0',
  `del_flag` bit(1) DEFAULT b'0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_user` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_user` bigint DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`dict_id`)
);

CREATE TABLE IF NOT EXISTS `sys_dict_data` (
  `dict_code` bigint NOT NULL,
  `dict_sort` int DEFAULT 0,
  `dict_label` varchar(100) DEFAULT '',
  `dict_value` varchar(100) DEFAULT '',
  `dict_type` varchar(100) DEFAULT '',
  `css_class` varchar(100) DEFAULT NULL,
  `list_class` varchar(100) DEFAULT NULL,
  `is_default` char(1) DEFAULT 'N',
  `status` char(1) DEFAULT '0',
  `del_flag` bit(1) DEFAULT b'0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_user` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_user` bigint DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`dict_code`)
);

CREATE TABLE IF NOT EXISTS `social_user` (
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`)
);

CREATE TABLE IF NOT EXISTS `social_user_auth` (
  `id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `identity` varchar(100) DEFAULT NULL,
  `source` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);
