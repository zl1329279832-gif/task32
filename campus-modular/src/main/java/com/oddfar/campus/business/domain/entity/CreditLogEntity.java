package com.oddfar.campus.business.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 信用分变更日志
 */
@Data
@TableName("campus_credit_log")
public class CreditLogEntity {
    private static final long serialVersionUID = 1L;

    @TableId("log_id")
    private Long logId;

    /** 用户id */
    private Long userId;

    /** 变更值 +/- */
    private Integer changeValue;

    /** 原因 */
    private String reason;

    /** 关联类型：content/comment/appeal */
    private String relatedType;

    /** 关联id */
    private Long relatedId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
