package com.oddfar.campus.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oddfar.campus.business.domain.entity.UserCreditEntity;
import com.oddfar.campus.business.domain.entity.CreditLogEntity;

import java.util.List;

/**
 * 用户信用分服务
 */
public interface UserCreditService extends IService<UserCreditEntity> {

    /**
     * 获取用户信用分（不存在则创建，默认100）
     */
    int getCreditScore(Long userId);

    /**
     * 变更信用分
     *
     * @param userId      用户id
     * @param delta       变更值（正数加分，负数扣分）
     * @param reason      原因
     * @param relatedType 关联类型
     * @param relatedId   关联id
     */
    void changeCredit(Long userId, int delta, String reason, String relatedType, Long relatedId);

    /**
     * 根据信用分评估动作
     *
     * @return PASS/PENDING/BLOCK
     */
    String evaluateCreditAction(Long userId);

    /**
     * 查询信用分变更日志
     */
    List<CreditLogEntity> getCreditLogs(Long userId);
}
