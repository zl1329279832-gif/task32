package com.oddfar.campus.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oddfar.campus.business.domain.entity.UserCreditEntity;

public interface UserCreditService extends IService<UserCreditEntity> {

    /**
     * 获取或初始化用户信用分
     */
    UserCreditEntity getOrCreateCredit(Long userId);

    /**
     * 获取信用分
     */
    int getCreditScore(Long userId);

    /**
     * 扣除信用分
     */
    void deductCredit(Long userId, int points, String reason, String refType, Long refId);

    /**
     * 恢复信用分
     */
    void restoreCredit(Long userId, int points, String reason, String refType, Long refId);
}
