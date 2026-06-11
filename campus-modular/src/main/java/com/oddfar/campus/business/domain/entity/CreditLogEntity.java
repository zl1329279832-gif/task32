package com.oddfar.campus.business.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("campus_credit_log")
public class CreditLogEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("log_id")
    private Long logId;

    /** 用户ID */
    private Long userId;

    /** 变更值(正=加分,负=扣分) */
    private Integer changeValue;

    /** 变更前分数 */
    private Integer scoreBefore;

    /** 变更后分数 */
    private Integer scoreAfter;

    /** 变更原因 */
    private String reason;

    /** 关联类型(moderation/appeal/manual) */
    private String refType;

    /** 关联ID */
    private Long refId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /** 操作人ID */
    private Long createUser;
}
