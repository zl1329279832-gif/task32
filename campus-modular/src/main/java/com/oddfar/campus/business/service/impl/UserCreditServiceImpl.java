package com.oddfar.campus.business.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddfar.campus.business.domain.entity.CreditLogEntity;
import com.oddfar.campus.business.domain.entity.UserCreditEntity;
import com.oddfar.campus.business.mapper.CreditLogMapper;
import com.oddfar.campus.business.mapper.UserCreditMapper;
import com.oddfar.campus.business.service.UserCreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 用户信用分服务实现
 */
@Service
public class UserCreditServiceImpl extends ServiceImpl<UserCreditMapper, UserCreditEntity>
        implements UserCreditService {

    @Autowired
    private UserCreditMapper userCreditMapper;
    @Autowired
    private CreditLogMapper creditLogMapper;

    @Override
    public int getCreditScore(Long userId) {
        UserCreditEntity entity = userCreditMapper.selectByUserId(userId);
        if (entity == null) {
            // 首次查询，创建默认信用分
            entity = new UserCreditEntity();
            entity.setCreditId(IdWorker.getId());
            entity.setUserId(userId);
            entity.setCreditScore(100);
            userCreditMapper.insert(entity);
        }
        return entity.getCreditScore();
    }

    @Override
    @Transactional
    public void changeCredit(Long userId, int delta, String reason, String relatedType, Long relatedId) {
        UserCreditEntity entity = userCreditMapper.selectByUserId(userId);
        if (entity == null) {
            entity = new UserCreditEntity();
            entity.setCreditId(IdWorker.getId());
            entity.setUserId(userId);
            entity.setCreditScore(100);
            userCreditMapper.insert(entity);
        }

        int newScore = Math.max(0, Math.min(100, entity.getCreditScore() + delta));
        entity.setCreditScore(newScore);
        userCreditMapper.updateById(entity);

        // 记录变更日志
        CreditLogEntity log = new CreditLogEntity();
        log.setLogId(IdWorker.getId());
        log.setUserId(userId);
        log.setChangeValue(delta);
        log.setReason(reason);
        log.setRelatedType(relatedType);
        log.setRelatedId(relatedId);
        log.setCreateTime(new Date());
        creditLogMapper.insert(log);
    }

    @Override
    public String evaluateCreditAction(Long userId) {
        int score = getCreditScore(userId);
        if (score >= 80) {
            return "PASS";
        } else if (score >= 40) {
            return "PENDING";
        } else {
            return "BLOCK";
        }
    }

    @Override
    public List<CreditLogEntity> getCreditLogs(Long userId) {
        return creditLogMapper.selectByUserId(userId);
    }
}
