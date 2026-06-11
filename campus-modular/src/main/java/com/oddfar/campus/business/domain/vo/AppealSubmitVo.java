package com.oddfar.campus.business.domain.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 申诉提交VO
 */
@Data
public class AppealSubmitVo {

    /** 内容id */
    @NotNull(message = "内容id不能为空")
    private Long contentId;

    /** 申诉理由 */
    @NotBlank(message = "申诉理由不能为空")
    private String appealReason;
}
