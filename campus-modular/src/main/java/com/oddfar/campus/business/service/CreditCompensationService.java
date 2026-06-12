package com.oddfar.campus.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oddfar.campus.business.domain.entity.CreditCompensationEntity;

import java.util.List;

/**
 * 信用分补偿明细服务
 */
public interface CreditCompensationService extends IService<CreditCompensationEntity> {

    /**
     * 创建补偿明细并执行信用分回补
     */
    CreditCompensationEntity compensate(Long userId, Long appealId, Long contentId,
                                         int baseComp, int bonusComp, String reason);

    /**
     * 查询某申诉的补偿明细
     */
    List<CreditCompensationEntity> getByAppealId(Long appealId);
}
