package com.oddfar.campus.business.domain.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量附件复核请求
 */
@Data
public class FileReviewBatchVo {

    @NotEmpty(message = "文件id列表不能为空")
    private List<Long> fileIds;

    @NotNull(message = "复核状态不能为空")
    private Integer reviewStatus;
}
