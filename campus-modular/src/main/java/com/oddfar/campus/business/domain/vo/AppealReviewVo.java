package com.oddfar.campus.business.domain.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AppealReviewVo {

    /** 申诉ID */
    @NotNull(message = "申诉ID不能为空")
    private Long appealId;

    /** 处理状态: 2=通过, 3=驳回 */
    @NotNull(message = "处理状态不能为空")
    private Integer status;

    /** 处理备注 */
    private String reviewRemark;
}
