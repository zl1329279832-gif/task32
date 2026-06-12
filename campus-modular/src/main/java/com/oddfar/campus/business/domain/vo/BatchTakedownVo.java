package com.oddfar.campus.business.domain.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量下架请求
 */
@Data
public class BatchTakedownVo {

    @NotEmpty(message = "内容id列表不能为空")
    private List<Long> contentIds;

    private String reason;
}
