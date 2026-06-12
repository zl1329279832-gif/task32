package com.oddfar.campus.business.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddfar.campus.business.domain.entity.ModerationRecordEntity;
import com.oddfar.campus.business.mapper.ModerationRecordMapper;
import com.oddfar.campus.business.service.ModerationRecordService;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import com.oddfar.campus.common.core.page.PageUtils;
import com.oddfar.campus.common.domain.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
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

    @Override
    public List<ModerationRecordEntity> getByBatchId(Long batchId) {
        return moderationRecordMapper.selectByBatchId(batchId);
    }

    @Override
    public PageResult<ModerationRecordEntity> queryAuditRecords(Long contentId, Long adminId,
            String action, String targetType, Long batchId, Date startTime, Date endTime) {
        LambdaQueryWrapperX<ModerationRecordEntity> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eqIfPresent(ModerationRecordEntity::getContentId, contentId);
        wrapper.eqIfPresent(ModerationRecordEntity::getAdminId, adminId);
        wrapper.eqIfPresent(ModerationRecordEntity::getAction, action);
        wrapper.eqIfPresent(ModerationRecordEntity::getTargetType, targetType);
        wrapper.eqIfPresent(ModerationRecordEntity::getBatchId, batchId);
        wrapper.geIfPresent(ModerationRecordEntity::getCreateTime, startTime);
        wrapper.leIfPresent(ModerationRecordEntity::getCreateTime, endTime);
        wrapper.orderByDesc(ModerationRecordEntity::getCreateTime);
        return moderationRecordMapper.selectPage(wrapper);
    }
}
