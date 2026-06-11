-- ============================================================
-- 内容治理与申诉闭环 - 数据库迁移脚本
-- ============================================================

-- 1. 敏感词表
CREATE TABLE `campus_sensitive_word` (
  `word_id`     bigint       NOT NULL COMMENT '敏感词主键',
  `word`        varchar(100) NOT NULL COMMENT '敏感词',
  `category`    varchar(50)  DEFAULT 'default' COMMENT '分类(politics/porn/abuse/ad/default)',
  `level`       tinyint(1)   DEFAULT 1 COMMENT '严重等级: 1=低(需审核), 2=中(需审核), 3=高(直接拦截)',
  `status`      char(1)      DEFAULT '0' COMMENT '状态(0正常 1停用)',
  `del_flag`    bit(1)       DEFAULT b'0' COMMENT '逻辑删除',
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP,
  `create_user` bigint       DEFAULT NULL,
  `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_user` bigint       DEFAULT NULL,
  PRIMARY KEY (`word_id`),
  UNIQUE KEY `uk_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='敏感词表';

-- 2. 用户信用分表
CREATE TABLE `campus_user_credit` (
  `user_id`         bigint   NOT NULL COMMENT '用户ID',
  `credit_score`    int      DEFAULT 100 COMMENT '信用分(0-100, 初始100)',
  `violation_count` int      DEFAULT 0 COMMENT '累计违规次数',
  `create_time`     datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time`     datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='用户信用分表';

-- 3. 信用分变更日志
CREATE TABLE `campus_credit_log` (
  `log_id`       bigint       NOT NULL COMMENT '日志主键',
  `user_id`      bigint       NOT NULL COMMENT '用户ID',
  `change_value` int          NOT NULL COMMENT '变更值(正=加分,负=扣分)',
  `score_before` int          NOT NULL COMMENT '变更前分数',
  `score_after`  int          NOT NULL COMMENT '变更后分数',
  `reason`       varchar(200) DEFAULT NULL COMMENT '变更原因',
  `ref_type`     varchar(30)  DEFAULT NULL COMMENT '关联类型(moderation/appeal/manual)',
  `ref_id`       bigint       DEFAULT NULL COMMENT '关联ID',
  `create_time`  datetime     DEFAULT CURRENT_TIMESTAMP,
  `create_user`  bigint       DEFAULT NULL,
  PRIMARY KEY (`log_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='信用分变更日志';

-- 4. 审核记录表
CREATE TABLE `campus_moderation_record` (
  `record_id`     bigint       NOT NULL COMMENT '审核记录主键',
  `target_type`   tinyint(1)   NOT NULL COMMENT '审核对象类型: 1=内容, 2=评论',
  `target_id`     bigint       NOT NULL COMMENT '审核对象ID',
  `user_id`       bigint       NOT NULL COMMENT '内容所属用户ID',
  `action`        tinyint(1)   NOT NULL COMMENT '审核动作: 0=待审, 1=通过, 2=下架, 3=拒绝',
  `trigger_type`  tinyint(1)   DEFAULT 0 COMMENT '触发类型: 0=自动, 1=人工',
  `reviewer_id`   bigint       DEFAULT NULL COMMENT '审核人ID',
  `auto_reason`   varchar(500) DEFAULT NULL COMMENT '自动审核原因',
  `manual_remark` varchar(500) DEFAULT NULL COMMENT '人工审核备注',
  `risk_score`    int          DEFAULT 0 COMMENT '风险评分(0-100)',
  `del_flag`      bit(1)       DEFAULT b'0' COMMENT '逻辑删除',
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP,
  `create_user`   bigint       DEFAULT NULL,
  `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_user`   bigint       DEFAULT NULL,
  PRIMARY KEY (`record_id`),
  KEY `idx_target` (`target_type`, `target_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='审核记录表';

-- 5. 违规记录表
CREATE TABLE `campus_violation_record` (
  `violation_id`   bigint       NOT NULL COMMENT '违规记录主键',
  `user_id`        bigint       NOT NULL COMMENT '违规用户ID',
  `target_type`    tinyint(1)   NOT NULL COMMENT '违规对象类型: 1=内容, 2=评论',
  `target_id`      bigint       NOT NULL COMMENT '违规对象ID',
  `violation_type` varchar(50)  NOT NULL COMMENT '违规类型(sensitive_word/image_risk/manual)',
  `description`    varchar(500) DEFAULT NULL COMMENT '违规描述',
  `penalty_type`   tinyint(1)   DEFAULT 0 COMMENT '处罚类型: 0=警告, 1=内容下架, 2=内容拒绝',
  `credit_deduct`  int          DEFAULT 0 COMMENT '扣除信用分',
  `record_id`      bigint       DEFAULT NULL COMMENT '关联审核记录ID',
  `del_flag`       bit(1)       DEFAULT b'0' COMMENT '逻辑删除',
  `create_time`    datetime     DEFAULT CURRENT_TIMESTAMP,
  `create_user`    bigint       DEFAULT NULL,
  `update_time`    datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_user`    bigint       DEFAULT NULL,
  PRIMARY KEY (`violation_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='违规记录表';

-- 6. 申诉表
CREATE TABLE `campus_appeal` (
  `appeal_id`     bigint       NOT NULL COMMENT '申诉主键',
  `user_id`       bigint       NOT NULL COMMENT '申诉用户ID',
  `target_type`   tinyint(1)   NOT NULL COMMENT '申诉对象类型: 1=内容, 2=评论',
  `target_id`     bigint       NOT NULL COMMENT '申诉对象ID',
  `reason`        varchar(500) NOT NULL COMMENT '申诉理由',
  `evidence_urls` varchar(1000) DEFAULT NULL COMMENT '申诉证据URL(逗号分隔)',
  `status`        tinyint(1)   DEFAULT 0 COMMENT '申诉状态: 0=待处理, 1=审核中, 2=通过, 3=驳回',
  `reviewer_id`   bigint       DEFAULT NULL COMMENT '处理人ID',
  `review_remark` varchar(500) DEFAULT NULL COMMENT '处理备注',
  `review_time`   datetime     DEFAULT NULL COMMENT '处理时间',
  `del_flag`      bit(1)       DEFAULT b'0' COMMENT '逻辑删除',
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP,
  `create_user`   bigint       DEFAULT NULL,
  `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_user`   bigint       DEFAULT NULL,
  PRIMARY KEY (`appeal_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='申诉表';

-- 7. 评论表添加状态字段
ALTER TABLE `campus_comment` ADD COLUMN `status` tinyint(1) DEFAULT 1 COMMENT '状态: 0=待审, 1=正常, 2=冻结' AFTER `co_content`;
