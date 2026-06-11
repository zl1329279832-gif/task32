package com.oddfar.campus.business.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.oddfar.campus.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理员审核范围
 */
@Data
@TableName("campus_admin_moderation_scope")
@EqualsAndHashCode(callSuper = true)
public class AdminModerationScopeEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId("scope_id")
    private Long scopeId;

    /** 管理员用户id */
    private Long adminUserId;

    /** 范围类型：ALL/CATEGORY */
    private String scopeType;

    /** 范围值（categoryId，当scopeType=CATEGORY时有效） */
    private Long scopeValue;
}
