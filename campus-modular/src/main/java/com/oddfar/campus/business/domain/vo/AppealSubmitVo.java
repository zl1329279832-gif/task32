package com.oddfar.campus.business.domain.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class AppealSubmitVo {

    /** 申诉对象类型: 1=内容, 2=评论 */
    @NotNull(message = "申诉类型不能为空")
    private Integer targetType;

    /** 申诉对象ID */
    @NotNull(message = "申诉对象ID不能为空")
    private Long targetId;

    /** 申诉理由 */
    @NotBlank(message = "申诉理由不能为空")
    @Size(max = 500, message = "申诉理由不能超过500个字符")
    private String reason;

    /** 申诉证据URL(逗号分隔) */
    private String evidenceUrls;
}
