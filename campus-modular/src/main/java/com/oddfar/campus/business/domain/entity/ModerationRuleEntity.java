package com.oddfar.campus.business.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.oddfar.campus.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审核规则配置
 */
@Data
@TableName("campus_moderation_rule")
@EqualsAndHashCode(callSuper = true)
public class ModerationRuleEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId("rule_id")
    private Long ruleId;

    /** 规则类型：CATEGORY/TAG/CREDIT_THRESHOLD/ATTACHMENT */
    private String ruleType;

    /** 规则键 */
    private String ruleKey;

    /** 规则值（JSON配置） */
    private String ruleValue;

    /** 触发动作：PASS/PENDING/BLOCK */
    private String action;

    /** 优先级，越高越先评估 */
    private Integer priority;

    /** 状态：0=启用, 1=禁用 */
    private String status;

    /** 描述 */
    private String description;
}
