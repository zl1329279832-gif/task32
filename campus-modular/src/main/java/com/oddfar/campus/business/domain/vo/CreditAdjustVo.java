package com.oddfar.campus.business.domain.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 信用分调整VO
 */
@Data
public class CreditAdjustVo {

    /** 用户id */
    @NotNull(message = "用户id不能为空")
    private Long userId;

    /** 变更值（正数加分，负数扣分） */
    @NotNull(message = "变更值不能为空")
    private Integer delta;

    /** 原因 */
    private String reason;
}
