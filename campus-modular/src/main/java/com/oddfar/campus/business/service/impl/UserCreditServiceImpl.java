package com.oddfar.campus.business.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddfar.campus.business.domain.entity.CreditLogEntity;
import com.oddfar.campus.business.domain.entity.UserCreditEntity;
import com.oddfar.campus.business.mapper.UserCreditMapper;
import com.oddfar.campus.business.service.CreditLogService;
import com.oddfar.campus.business.service.UserCreditService;
import com.oddfar.campus.common.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class UserCreditServiceImpl extends ServiceImpl<UserCreditMapper, UserCreditEntity>
        implements UserCreditService {

    @Resource
    private UserCreditMapper userCreditMapper;

    @Resource
    private CreditLogService creditLogService;

    @Override
    public UserCreditEntity getOrCreateCredit(Long userId) {
        UserCreditEntity entity = userCreditMapper.selectById(userId);
        if (entity == null) {
            entity = new UserCreditEntity();
            entity.setUserId(userId);
            entity.setCreditScore(100);
            entity.setViolationCount(0);
            entity.setCreateTime(new Date());
            entity.setUpdateTime(new Date());
            userCreditMapper.insert(entity);
        }
        return entity;
    }

    @Override
    public int getCreditScore(Long userId) {
        UserCreditEntity entity = getOrCreateCredit(userId);
        return entity.getCreditScore();
    }

    @Override
    @Transactional
    public void deductCredit(Long userId, int points, String reason, String refType, Long refId) {
        UserCreditEntity credit = getOrCreateCredit(userId);
        int scoreBefore = credit.getCreditScore();
        int scoreAfter = Math.max(0, scoreBefore - points);

        credit.setCreditScore(scoreAfter);
        credit.setViolationCount(credit.getViolationCount() + 1);
        credit.setUpdateTime(new Date());
        userCreditMapper.updateById(credit);

        CreditLogEntity log = new CreditLogEntity();
        log.setLogId(IdWorker.getId());
        log.setUserId(userId);
        log.setChangeValue(-points);
        log.setScoreBefore(scoreBefore);
        log.setScoreAfter(scoreAfter);
        log.setReason(reason);
        log.setRefType(refType);
        log.setRefId(refId);
        log.setCreateTime(new Date());
        try {
            log.setCreateUser(SecurityUtils.getUserId());
        } catch (Exception ignored) {
        }
        creditLogService.addLog(log);
    }

    @Override
    @Transactional
    public void restoreCredit(Long userId, int points, String reason, String refType, Long refId) {
        UserCreditEntity credit = getOrCreateCredit(userId);
        int scoreBefore = credit.getCreditScore();
        int scoreAfter = Math.min(100, scoreBefore + points);

        credit.setCreditScore(scoreAfter);
        credit.setUpdateTime(new Date());
        userCreditMapper.updateById(credit);

        CreditLogEntity log = new CreditLogEntity();
        log.setLogId(IdWorker.getId());
        log.setUserId(userId);
        log.setChangeValue(points);
        log.setScoreBefore(scoreBefore);
        log.setScoreAfter(scoreAfter);
        log.setReason(reason);
        log.setRefType(refType);
        log.setRefId(refId);
        log.setCreateTime(new Date());
        try {
            log.setCreateUser(SecurityUtils.getUserId());
        } catch (Exception ignored) {
        }
        creditLogService.addLog(log);
    }
}
