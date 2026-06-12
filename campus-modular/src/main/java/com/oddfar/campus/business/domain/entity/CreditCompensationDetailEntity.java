package com.oddfar.campus.business.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 信用分补偿明细
 */
@Data
@TableName("campus_credit_compensation_detail")
public class CreditCompensationDetailEntity {
    private static final long serialVersionUID = 1L;

    @TableId("detail_id")
    private Long detailId;

    /** 申诉id */
    private Long appealId;

    /** 用户id */
    private Long userId;

    /** 补偿类型：BASE_RESTORE/INTERACTION_RESTORE/MANUAL */
    private String compensationType;

    /** 补偿分值 */
    private Integer compensationValue;

    /** 描述 */
    private String description;

    /** 关联信用日志id */
    private Long creditLogId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private Long createUser;
}
