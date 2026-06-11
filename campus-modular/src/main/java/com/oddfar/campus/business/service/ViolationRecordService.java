package com.oddfar.campus.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oddfar.campus.business.domain.entity.ViolationRecordEntity;

/**
 * 违规记录服务
 */
public interface ViolationRecordService extends IService<ViolationRecordEntity> {

    /**
     * 记录违规
     */
    ViolationRecordEntity recordViolation(Long userId, Long contentId, String violationType,
                                           String description, String matchedWords, Long moderationRecordId);

    /**
     * 查询用户违规次数
     */
    Long getViolationCount(Long userId);
}
