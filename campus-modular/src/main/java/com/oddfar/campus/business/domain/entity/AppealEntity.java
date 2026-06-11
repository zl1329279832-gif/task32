package com.oddfar.campus.business.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.oddfar.campus.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@TableName("campus_appeal")
@EqualsAndHashCode(callSuper = true)
public class AppealEntity extends BaseEntity {

    @TableId("appeal_id")
    private Long appealId;

    /** 申诉用户ID */
    private Long userId;

    /** 申诉对象类型: 1=内容, 2=评论 */
    private Integer targetType;

    /** 申诉对象ID */
    private Long targetId;

    /** 申诉理由 */
    private String reason;

    /** 申诉证据URL(逗号分隔) */
    private String evidenceUrls;

    /** 申诉状态: 0=待处理, 1=审核中, 2=通过, 3=驳回 */
    private Integer status;

    /** 处理人ID */
    private Long reviewerId;

    /** 处理备注 */
    private String reviewRemark;

    /** 处理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date reviewTime;
}
