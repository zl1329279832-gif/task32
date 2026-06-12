package com.oddfar.campus.business.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddfar.campus.business.domain.entity.CreditCompensationEntity;
import com.oddfar.campus.business.mapper.CreditCompensationMapper;
import com.oddfar.campus.business.service.CreditCompensationService;
import com.oddfar.campus.business.service.UserCreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 信用分补偿明细服务实现
 */
@Service
public class CreditCompensationServiceImpl extends ServiceImpl<CreditCompensationMapper, CreditCompensationEntity>
        implements CreditCompensationService {

    @Autowired
    private CreditCompensationMapper creditCompensationMapper;
    @Autowired
    private UserCreditService userCreditService;

    @Override
    public CreditCompensationEntity compensate(Long userId, Long appealId, Long contentId,
                                                int baseComp, int bonusComp, String reason) {
        CreditCompensationEntity entity = new CreditCompensationEntity();
        entity.setCompensationId(IdWorker.getId());
        entity.setUserId(userId);
        entity.setAppealId(appealId);
        entity.setContentId(contentId);
        entity.setBaseCompensation(baseComp);
        entity.setBonusCompensation(bonusComp);
        entity.setReason(reason);
        entity.setCreateTime(new Date());
        creditCompensationMapper.insert(entity);

        // 执行信用分回补（changeCredit 内部有幂等保护）
        int totalComp = baseComp + bonusComp;
        userCreditService.changeCredit(userId, totalComp, reason, "compensation", entity.getCompensationId());

        return entity;
    }

    @Override
    public List<CreditCompensationEntity> getByAppealId(Long appealId) {
        return creditCompensationMapper.selectByAppealId(appealId);
    }
}
