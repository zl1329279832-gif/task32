package com.oddfar.campus.business.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.oddfar.campus.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 用户申诉
 */
@Data
@TableName("campus_appeal")
@EqualsAndHashCode(callSuper = true)
public class AppealEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId("appeal_id")
    private Long appealId;

    /** 内容id */
    private Long contentId;

    /** 用户id */
    private Long userId;

    /** 申诉理由 */
    private String appealReason;

    /** 申诉状态：0=待审, 1=通过, 2=拒绝 */
    private Integer appealStatus;

    /** 审核管理员id */
    private Long adminId;

    /** 审核管理员名称 */
    private String adminName;

    /** 审核意见 */
    private String reviewComment;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date reviewTime;
}
