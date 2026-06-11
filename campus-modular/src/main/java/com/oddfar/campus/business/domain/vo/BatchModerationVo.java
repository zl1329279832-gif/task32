package com.oddfar.campus.business.domain.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class BatchModerationVo {

    /** 内容ID列表 */
    @NotEmpty(message = "内容ID列表不能为空")
    private List<Long> contentIds;

    /** 审核动作: 1=通过, 2=下架, 3=拒绝, 4=恢复 */
    @NotNull(message = "审核动作不能为空")
    private Integer action;

    /** 审核备注 */
    private String remark;
}
