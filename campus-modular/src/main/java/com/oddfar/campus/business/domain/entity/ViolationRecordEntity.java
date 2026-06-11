package com.oddfar.campus.business.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 违规记录
 */
@Data
@TableName("campus_violation_record")
public class ViolationRecordEntity {
    private static final long serialVersionUID = 1L;

    @TableId("violation_id")
    private Long violationId;

    /** 用户id */
    private Long userId;

    /** 内容id */
    private Long contentId;

    /** 违规类型：SENSITIVE_WORD/ATTACHMENT/REPORT/MANUAL */
    private String violationType;

    /** 描述 */
    private String description;

    /** 匹配的敏感词（JSON） */
    private String matchedWords;

    /** 关联审核记录id */
    private Long moderationRecordId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private Long createUser;
}
