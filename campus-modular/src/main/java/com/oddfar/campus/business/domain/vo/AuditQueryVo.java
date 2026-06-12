package com.oddfar.campus.business.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 审计查询请求
 */
@Data
public class AuditQueryVo {

    private Long contentId;

    private Long adminId;

    /** 动作：PASS/TAKEDOWN/RESTORE/BLOCK 等 */
    private String action;

    /** 目标类型：CONTENT/COMMENT/FILE/CREDIT */
    private String targetType;

    /** 治理批次id */
    private Long batchId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
}
