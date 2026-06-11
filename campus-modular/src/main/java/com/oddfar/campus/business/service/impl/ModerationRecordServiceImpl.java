package com.oddfar.campus.business.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddfar.campus.business.domain.entity.ModerationRecordEntity;
import com.oddfar.campus.business.mapper.ModerationRecordMapper;
import com.oddfar.campus.business.service.ModerationRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审核记录服务实现
 */
@Service
public class ModerationRecordServiceImpl extends ServiceImpl<ModerationRecordMapper, ModerationRecordEntity>
        implements ModerationRecordService {

    @Autowired
    private ModerationRecordMapper moderationRecordMapper;

    @Override
    public ModerationRecordEntity recordAction(Long contentId, String targetType, Long targetId,
                                                 String moderationType, String action, String reason,
                                                 String matchedRules, Integer beforeStatus, Integer afterStatus) {
        ModerationRecordEntity record = new ModerationRecordEntity();
        record.setRecordId(IdWorker.getId());
        record.setContentId(contentId);
        record.setTargetType(targetType);
        record.setTargetId(targetId);
        record.setModerationType(moderationType);
        record.setAction(action);
        record.setReason(reason);
        record.setMatchedRules(matchedRules);
        record.setBeforeStatus(beforeStatus);
        record.setAfterStatus(afterStatus);
        moderationRecordMapper.insert(record);
        return record;
    }

    @Override
    public List<ModerationRecordEntity> getByContentId(Long contentId) {
        return moderationRecordMapper.selectByContentId(contentId);
    }
}
