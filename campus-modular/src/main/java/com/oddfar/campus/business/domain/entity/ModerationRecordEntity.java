package com.oddfar.campus.business.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.oddfar.campus.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审核记录（完整操作日志）
 */
@Data
@TableName("campus_moderation_record")
@EqualsAndHashCode(callSuper = true)
public class ModerationRecordEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId("record_id")
    private Long recordId;

    /** 内容id */
    private Long contentId;

    /** 目标类型：CONTENT/COMMENT/FILE */
    private String targetType;

    /** 目标id */
    private Long targetId;

    /** 审核类型：AUTO/MANUAL */
    private String moderationType;

    /** 动作：PASS/PENDING/BLOCK/TAKEDOWN/RESTORE */
    private String action;

    /** 原因 */
    private String reason;

    /** 匹配的规则（JSON） */
    private String matchedRules;

    /** 管理员id */
    private Long adminId;

    /** 管理员名称 */
    private String adminName;

    /** 操作前状态 */
    private Integer beforeStatus;

    /** 操作后状态 */
    private Integer afterStatus;

    /** 快照点赞数 */
    private Long snapshotLoveCount;

    /** 快照评论数 */
    private Long snapshotCommentCount;
}
