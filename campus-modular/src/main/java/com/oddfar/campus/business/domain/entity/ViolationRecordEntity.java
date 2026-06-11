package com.oddfar.campus.business.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.oddfar.campus.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("campus_violation_record")
@EqualsAndHashCode(callSuper = true)
public class ViolationRecordEntity extends BaseEntity {

    @TableId("violation_id")
    private Long violationId;

    /** 违规用户ID */
    private Long userId;

    /** 违规对象类型: 1=内容, 2=评论 */
    private Integer targetType;

    /** 违规对象ID */
    private Long targetId;

    /** 违规类型(sensitive_word/image_risk/manual) */
    private String violationType;

    /** 违规描述 */
    private String description;

    /** 处罚类型: 0=警告, 1=内容下架, 2=内容拒绝 */
    private Integer penaltyType;

    /** 扣除信用分 */
    private Integer creditDeduct;

    /** 关联审核记录ID */
    private Long recordId;
}
