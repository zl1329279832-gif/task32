package com.oddfar.campus.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oddfar.campus.business.domain.entity.ModerationRecordEntity;
import com.oddfar.campus.common.domain.PageResult;

public interface ModerationRecordService extends IService<ModerationRecordEntity> {

    PageResult<ModerationRecordEntity> page(ModerationRecordEntity record);

    void addRecord(ModerationRecordEntity record);

    /**
     * 获取最近一条审核记录
     */
    ModerationRecordEntity getLatestRecord(Integer targetType, Long targetId);
}
