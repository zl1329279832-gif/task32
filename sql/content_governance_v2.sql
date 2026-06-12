-- =============================================
-- 内容治理增强 V2：影响快照扩展、治理批次、申诉版本、附件复核、信用补偿明细、评论只读
-- =============================================

-- 1. 治理批次表
CREATE TABLE IF NOT EXISTS `campus_governance_batch` (
  `batch_id`      bigint        NOT NULL,
  `batch_no`      varchar(40)   NOT NULL COMMENT '批次编号, e.g. GOV-20260612-00001',
  `admin_id`      bigint        NOT NULL,
  `admin_name`    varchar(50)   DEFAULT NULL,
  `batch_type`    varchar(20)   NOT NULL COMMENT 'TAKEDOWN/APPROVE/REJECT',
  `content_count` int           DEFAULT 0,
  `reason`        varchar(500)  DEFAULT NULL,
  `del_flag`      bit(1)        DEFAULT b'0',
  `create_time`   datetime      DEFAULT CURRENT_TIMESTAMP,
  `create_user`   bigint        DEFAULT NULL,
  `update_time`   datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_user`   bigint        DEFAULT NULL,
  PRIMARY KEY (`batch_id`),
  UNIQUE KEY `uk_batch_no` (`batch_no`),
  KEY `idx_admin_id` (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='治理批次';

-- 2. 信用分补偿明细表
CREATE TABLE IF NOT EXISTS `campus_credit_compensation_detail` (
  `detail_id`          bigint        NOT NULL,
  `appeal_id`          bigint        NOT NULL,
  `user_id`            bigint        NOT NULL,
  `compensation_type`  varchar(30)   NOT NULL COMMENT 'BASE_RESTORE/INTERACTION_RESTORE/MANUAL',
  `compensation_value` int           NOT NULL COMMENT '本项补偿分值',
  `description`        varchar(200)  DEFAULT NULL,
  `credit_log_id`      bigint        DEFAULT NULL COMMENT '关联 campus_credit_log.log_id',
  `create_time`        datetime      DEFAULT CURRENT_TIMESTAMP,
  `create_user`        bigint        DEFAULT NULL,
  PRIMARY KEY (`detail_id`),
  KEY `idx_appeal_id` (`appeal_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信用分补偿明细';

-- 3. 互动快照表扩展字段（推荐/收藏/举报/搜索命中）
ALTER TABLE `campus_interaction_snapshot`
  ADD COLUMN `recommend_count`  bigint DEFAULT 0 COMMENT '被推荐数' AFTER `comment_count`,
  ADD COLUMN `favorite_count`   bigint DEFAULT 0 COMMENT '被收藏数' AFTER `recommend_count`,
  ADD COLUMN `report_count`     bigint DEFAULT 0 COMMENT '被举报数' AFTER `favorite_count`,
  ADD COLUMN `search_hit_count` bigint DEFAULT 0 COMMENT '被搜索命中数' AFTER `report_count`;

-- 4. 审核记录表关联治理批次
ALTER TABLE `campus_moderation_record`
  ADD COLUMN `batch_id` bigint DEFAULT NULL COMMENT '治理批次id' AFTER `admin_name`;
ALTER TABLE `campus_moderation_record`
  ADD KEY `idx_batch_id` (`batch_id`);

-- 5. 申诉表增加版本号和前序申诉链
ALTER TABLE `campus_appeal`
  ADD COLUMN `appeal_version`     int    DEFAULT 1 COMMENT '申诉版本号' AFTER `appeal_status`,
  ADD COLUMN `previous_appeal_id` bigint DEFAULT NULL COMMENT '上一次申诉id' AFTER `appeal_version`;

-- 6. 附件表增加复核状态
ALTER TABLE `campus_file`
  ADD COLUMN `review_status`    tinyint      DEFAULT 0 COMMENT '复核状态: 0=无需复核, 1=待复核, 2=复核通过, 3=复核不通过' AFTER `violation_reason`,
  ADD COLUMN `review_admin_id`  bigint       DEFAULT NULL COMMENT '复核管理员id' AFTER `review_status`,
  ADD COLUMN `review_time`      datetime     DEFAULT NULL COMMENT '复核时间' AFTER `review_admin_id`,
  ADD COLUMN `review_comment`   varchar(200) DEFAULT NULL COMMENT '复核意见' AFTER `review_time`;

-- 7. 评论表增加只读状态
ALTER TABLE `campus_comment`
  ADD COLUMN `read_only_status` tinyint DEFAULT 0 COMMENT '0=可编辑, 1=只读(恢复后)' AFTER `frozen_status`;
