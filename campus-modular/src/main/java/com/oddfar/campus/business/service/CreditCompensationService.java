package com.oddfar.campus.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oddfar.campus.business.domain.entity.CreditCompensationDetailEntity;
import com.oddfar.campus.business.domain.vo.CreditCompensationVo;

import java.util.List;

/**
 * 信用分补偿明细服务
 */
public interface CreditCompensationService extends IService<CreditCompensationDetailEntity> {

    /**
     * 记录一条补偿明细
     */
    CreditCompensationDetailEntity recordDetail(Long appealId, Long userId,
            String compensationType, int compensationValue, String description);

    /**
     * 根据申诉id查询补偿明细
     */
    List<CreditCompensationDetailEntity> getByAppealId(Long appealId);

    /**
     * 构建补偿汇总
     */
    CreditCompensationVo buildCompensationSummary(Long appealId);
}
