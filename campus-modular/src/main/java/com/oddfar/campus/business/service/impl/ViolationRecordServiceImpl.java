package com.oddfar.campus.business.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddfar.campus.business.domain.entity.ViolationRecordEntity;
import com.oddfar.campus.business.mapper.ViolationRecordMapper;
import com.oddfar.campus.business.service.ViolationRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 违规记录服务实现
 */
@Service
public class ViolationRecordServiceImpl extends ServiceImpl<ViolationRecordMapper, ViolationRecordEntity>
        implements ViolationRecordService {

    @Autowired
    private ViolationRecordMapper violationRecordMapper;

    @Override
    public ViolationRecordEntity recordViolation(Long userId, Long contentId, String violationType,
                                                   String description, String matchedWords, Long moderationRecordId) {
        ViolationRecordEntity record = new ViolationRecordEntity();
        record.setViolationId(IdWorker.getId());
        record.setUserId(userId);
        record.setContentId(contentId);
        record.setViolationType(violationType);
        record.setDescription(description);
        record.setMatchedWords(matchedWords);
        record.setModerationRecordId(moderationRecordId);
        record.setCreateTime(new Date());
        violationRecordMapper.insert(record);
        return record;
    }

    @Override
    public Long getViolationCount(Long userId) {
        return violationRecordMapper.selectViolationCount(userId);
    }
}
