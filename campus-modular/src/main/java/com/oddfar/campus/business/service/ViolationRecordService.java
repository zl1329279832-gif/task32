package com.oddfar.campus.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oddfar.campus.business.domain.entity.ViolationRecordEntity;
import com.oddfar.campus.common.domain.PageResult;

public interface ViolationRecordService extends IService<ViolationRecordEntity> {

    PageResult<ViolationRecordEntity> page(ViolationRecordEntity record);

    void addViolation(ViolationRecordEntity record);

    /**
     * 根据用户ID统计违规次数
     */
    long countByUserId(Long userId);
}
