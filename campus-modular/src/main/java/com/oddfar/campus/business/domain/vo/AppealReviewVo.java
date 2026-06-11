package com.oddfar.campus.business.domain.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 申诉审核VO
 */
@Data
public class AppealReviewVo {

    /** 申诉id */
    @NotNull(message = "申诉id不能为空")
    private Long appealId;

    /** 审核决策：1=通过, 2=拒绝 */
    @NotNull(message = "审核决策不能为空")
    private Integer decision;

    /** 审核意见 */
    private String reviewComment;
}
