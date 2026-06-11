-- ============================================================
-- 内容治理与申诉闭环 DDL
-- ============================================================

-- 1. 敏感词库
CREATE TABLE IF NOT EXISTS `campus_sensitive_word` (
  `word_id`     bigint       NOT NULL,
  `word`        varchar(100) NOT NULL,
  `category`    varchar(50)  DEFAULT 'default' COMMENT '分类：politics/porn/abuse/ad/custom',
  `severity`    tinyint      DEFAULT 1         COMMENT '严重度：1=低, 2=中, 3=高',
  `status`      char(1)      DEFAULT '0'       COMMENT '0=启用, 1=禁用',
  `del_flag`    bit(1)       DEFAULT b'0',
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP,
  `create_user` bigint       DEFAULT NULL,
  `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_user` bigint       DEFAULT NULL,
  PRIMARY KEY (`word_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词库';

-- 2. 用户信用分
CREATE TABLE IF NOT EXISTS `campus_user_credit` (
  `credit_id`    bigint   NOT NULL,
  `user_id`      bigint   NOT NULL,
  `credit_score` int      DEFAULT 100 COMMENT '0-100, 默认100',
  `del_flag`     bit(1)   DEFAULT b'0',
  `create_time`  datetime DEFAULT CURRENT_TIMESTAMP,
  `create_user`  bigint   DEFAULT NULL,
  `update_time`  datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_user`  bigint   DEFAULT NULL,
  PRIMARY KEY (`credit_id`),
  UNIQUE KEY `uk_user_id` (`user_id`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信用分';

-- 3. 信用分变更日志
CREATE TABLE IF NOT EXISTS `campus_credit_log` (
  `log_id`       bigint       NOT NULL,
  `user_id`      bigint       NOT NULL,
  `change_value` int          NOT NULL COMMENT '变更值 +/-',
  `reason`       varchar(200) DEFAULT NULL,
  `related_type` varchar(30)  DEFAULT NULL COMMENT '关联类型：content/comment/appeal',
  `related_id`   bigint       DEFAULT NULL,
  `create_time`  datetime     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信用分变更日志';

-- 4. 审核规则配置
CREATE TABLE IF NOT EXISTS `campus_moderation_rule` (
  `rule_id`     bigint       NOT NULL,
  `rule_type`   varchar(20)  NOT NULL COMMENT 'CATEGORY/TAG/CREDIT_THRESHOLD/ATTACHMENT',
  `rule_key`    varchar(100) DEFAULT NULL,
  `rule_value`  varchar(200) NOT NULL COMMENT 'JSON配置',
  `action`      varchar(20)  DEFAULT 'PENDING' COMMENT 'PASS/PENDING/BLOCK',
  `priority`    int          DEFAULT 0 COMMENT '越高越先评估',
  `status`      char(1)      DEFAULT '0' COMMENT '0=启用, 1=禁用',
  `description` varchar(300) DEFAULT NULL,
  `del_flag`    bit(1)       DEFAULT b'0',
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP,
  `create_user` bigint       DEFAULT NULL,
  `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_user` bigint       DEFAULT NULL,
  PRIMARY KEY (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核规则配置';

-- 5. 审核记录（完整操作日志）
CREATE TABLE IF NOT EXISTS `campus_moderation_record` (
  `record_id`              bigint       NOT NULL,
  `content_id`             bigint       NOT NULL,
  `target_type`            varchar(20)  DEFAULT 'CONTENT' COMMENT 'CONTENT/COMMENT/FILE',
  `target_id`              bigint       DEFAULT NULL,
  `moderation_type`        varchar(20)  NOT NULL COMMENT 'AUTO/MANUAL',
  `action`                 varchar(20)  NOT NULL COMMENT 'PASS/PENDING/BLOCK/TAKEDOWN/RESTORE',
  `reason`                 varchar(500) DEFAULT NULL,
  `matched_rules`          varchar(1000) DEFAULT NULL COMMENT 'JSON匹配的规则',
  `admin_id`               bigint       DEFAULT NULL,
  `admin_name`             varchar(50)  DEFAULT NULL,
  `before_status`          tinyint      DEFAULT NULL,
  `after_status`           tinyint      DEFAULT NULL,
  `snapshot_love_count`    bigint       DEFAULT NULL,
  `snapshot_comment_count` bigint       DEFAULT NULL,
  `del_flag`               bit(1)       DEFAULT b'0',
  `create_time`            datetime     DEFAULT CURRENT_TIMESTAMP,
  `create_user`            bigint       DEFAULT NULL,
  `update_time`            datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_user`            bigint       DEFAULT NULL,
  PRIMARY KEY (`record_id`),
  KEY `idx_content_id` (`content_id`),
  KEY `idx_admin_id` (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核记录';

-- 6. 用户申诉
CREATE TABLE IF NOT EXISTS `campus_appeal` (
  `appeal_id`      bigint        NOT NULL,
  `content_id`     bigint        NOT NULL,
  `user_id`        bigint        NOT NULL,
  `appeal_reason`  varchar(1000) NOT NULL,
  `appeal_status`  tinyint       DEFAULT 0 COMMENT '0=待审, 1=通过, 2=拒绝',
  `admin_id`       bigint        DEFAULT NULL,
  `admin_name`     varchar(50)   DEFAULT NULL,
  `review_comment` varchar(500)  DEFAULT NULL,
  `review_time`    datetime      DEFAULT NULL,
  `del_flag`       bit(1)        DEFAULT b'0',
  `create_time`    datetime      DEFAULT CURRENT_TIMESTAMP,
  `create_user`    bigint        DEFAULT NULL,
  `update_time`    datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_user`    bigint        DEFAULT NULL,
  PRIMARY KEY (`appeal_id`),
  KEY `idx_content_id` (`content_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_appeal_status` (`appeal_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户申诉';

-- 7. 互动数据快照（用于恢复）
CREATE TABLE IF NOT EXISTS `campus_interaction_snapshot` (
  `snapshot_id`          bigint      NOT NULL,
  `content_id`           bigint      NOT NULL,
  `love_count`           bigint      DEFAULT 0,
  `comment_count`        bigint      DEFAULT 0,
  `snapshot_type`        varchar(20) DEFAULT 'TAKEDOWN' COMMENT 'TAKEDOWN/REJECT',
  `moderation_record_id` bigint      DEFAULT NULL,
  `consumed`             tinyint     DEFAULT 0 COMMENT '0=未消费, 1=已消费（防止重复回补）',
  `del_flag`             bit(1)      DEFAULT b'0',
  `create_time`          datetime    DEFAULT CURRENT_TIMESTAMP,
  `create_user`          bigint      DEFAULT NULL,
  PRIMARY KEY (`snapshot_id`),
  KEY `idx_content_id` (`content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='互动数据快照';

-- 8. 管理员审核范围
CREATE TABLE IF NOT EXISTS `campus_admin_moderation_scope` (
  `scope_id`      bigint      NOT NULL,
  `admin_user_id` bigint      NOT NULL,
  `scope_type`    varchar(20) DEFAULT 'CATEGORY' COMMENT 'CATEGORY/ALL',
  `scope_value`   bigint      DEFAULT NULL COMMENT 'categoryId if CATEGORY',
  `del_flag`      bit(1)      DEFAULT b'0',
  `create_time`   datetime    DEFAULT CURRENT_TIMESTAMP,
  `create_user`   bigint      DEFAULT NULL,
  `update_time`   datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_user`   bigint      DEFAULT NULL,
  PRIMARY KEY (`scope_id`),
  KEY `idx_admin_user_id` (`admin_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员审核范围';

-- 9. 违规记录
CREATE TABLE IF NOT EXISTS `campus_violation_record` (
  `violation_id`         bigint       NOT NULL,
  `user_id`              bigint       NOT NULL,
  `content_id`           bigint       DEFAULT NULL,
  `violation_type`       varchar(30)  NOT NULL COMMENT 'SENSITIVE_WORD/ATTACHMENT/REPORT/MANUAL',
  `description`          varchar(500) DEFAULT NULL,
  `matched_words`        varchar(500) DEFAULT NULL COMMENT 'JSON匹配的敏感词',
  `moderation_record_id` bigint       DEFAULT NULL,
  `del_flag`             bit(1)       DEFAULT b'0',
  `create_time`          datetime     DEFAULT CURRENT_TIMESTAMP,
  `create_user`          bigint       DEFAULT NULL,
  PRIMARY KEY (`violation_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='违规记录';

-- 10. 修改现有表
ALTER TABLE `campus_comment`
  ADD COLUMN `frozen_status` tinyint DEFAULT 0
  COMMENT '0=正常, 1=冻结(父内容被下架)' AFTER `address`;

ALTER TABLE `campus_file`
  ADD COLUMN `violation_status` tinyint DEFAULT 0
  COMMENT '0=正常, 1=标记违规' AFTER `url`,
  ADD COLUMN `violation_reason` varchar(200) DEFAULT NULL
  COMMENT '违规原因' AFTER `violation_status`;
