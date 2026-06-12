package com.oddfar.campus.business.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 治理批次（将相关审核操作分组）
 */
@Data
@TableName("campus_governance_batch")
public class GovernanceBatchEntity {
    private static final long serialVersionUID = 1L;

    @TableId("batch_id")
    private Long batchId;

    /** 批次类型：TAKEDOWN/REJECT/BATCH_REVIEW */
    private String batchType;

    /** 操作管理员id */
    private Long adminId;

    /** 批次原因 */
    private String reason;

    /** 涉及内容数量 */
    private Integer contentCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private Long createUser;
}
