package com.oddfar.campus.business.domain.vo;

import com.oddfar.campus.business.domain.entity.CreditCompensationDetailEntity;
import lombok.Data;

import java.util.List;

/**
 * 信用分补偿汇总
 */
@Data
public class CreditCompensationVo {

    private Long appealId;

    private Long userId;

    /** 总补偿分值 */
    private Integer totalCompensation;

    /** 明细列表 */
    private List<CreditCompensationDetailEntity> details;
}
