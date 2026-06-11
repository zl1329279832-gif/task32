package com.oddfar.campus.business.domain.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量审核VO
 */
@Data
public class BatchReviewVo {

    /** 内容id列表 */
    @NotEmpty(message = "内容id列表不能为空")
    private List<Long> contentIds;

    /** 审核动作（通过/拒绝），由Controller方法决定 */
    private String action;

    /** 审核原因 */
    private String reason;
}
