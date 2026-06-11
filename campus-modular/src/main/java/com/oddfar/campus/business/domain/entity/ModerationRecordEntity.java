package com.oddfar.campus.business.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.oddfar.campus.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("campus_moderation_record")
@EqualsAndHashCode(callSuper = true)
public class ModerationRecordEntity extends BaseEntity {

    @TableId("record_id")
    private Long recordId;

    /** 审核对象类型: 1=内容, 2=评论 */
    private Integer targetType;

    /** 审核对象ID */
    private Long targetId;

    /** 内容所属用户ID */
    private Long userId;

    /** 审核动作: 0=待审, 1=通过, 2=下架, 3=拒绝 */
    private Integer action;

    /** 触发类型: 0=自动, 1=人工 */
    private Integer triggerType;

    /** 审核人ID */
    private Long reviewerId;

    /** 自动审核原因 */
    private String autoReason;

    /** 人工审核备注 */
    private String manualRemark;

    /** 风险评分(0-100) */
    private Integer riskScore;
}
