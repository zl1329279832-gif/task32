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
@TableName("campus_credit_compensation")
public class CreditCompensationEntity {
    private static final long serialVersionUID = 1L;

    @TableId("compensation_id")
    private Long compensationId;

    /** 用户id */
    private Long userId;

    /** 关联申诉id */
    private Long appealId;

    /** 关联内容id */
    private Long contentId;

    /** 基础补偿分（标准+5） */
    private Integer baseCompensation;

    /** 额外补偿分（高影响力内容） */
    private Integer bonusCompensation;

    /** 补偿原因 */
    private String reason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private Long createUser;
}
