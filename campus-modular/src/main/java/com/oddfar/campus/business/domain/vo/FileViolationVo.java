package com.oddfar.campus.business.domain.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 附件违规标记VO
 */
@Data
public class FileViolationVo {

    /** 文件id */
    @NotNull(message = "文件id不能为空")
    private Long fileId;

    /** 违规状态：0=正常, 1=违规 */
    @NotNull(message = "违规状态不能为空")
    private Integer violationStatus;

    /** 违规原因 */
    private String violationReason;
}
