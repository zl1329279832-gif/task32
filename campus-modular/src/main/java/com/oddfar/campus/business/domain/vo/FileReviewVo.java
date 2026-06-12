package com.oddfar.campus.business.domain.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 附件复核请求
 */
@Data
public class FileReviewVo {

    @NotNull(message = "文件id不能为空")
    private Long fileId;

    /** 复核结果：2=通过, 3=不通过 */
    @NotNull(message = "复核结果不能为空")
    private Integer reviewStatus;

    private String reviewComment;
}
