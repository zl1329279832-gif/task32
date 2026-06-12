package com.oddfar.campus.business.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.oddfar.campus.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 治理批次
 */
@Data
@TableName("campus_governance_batch")
@EqualsAndHashCode(callSuper = true)
public class GovernanceBatchEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId("batch_id")
    private Long batchId;

    /** 批次编号 */
    private String batchNo;

    /** 管理员id */
    private Long adminId;

    /** 管理员名称 */
    private String adminName;

    /** 批次类型：TAKEDOWN/APPROVE/REJECT */
    private String batchType;

    /** 内容数量 */
    private Integer contentCount;

    /** 原因 */
    private String reason;
}
