package com.oddfar.campus.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddfar.campus.business.domain.entity.ModerationRecordEntity;
import com.oddfar.campus.business.mapper.ModerationRecordMapper;
import com.oddfar.campus.business.service.ModerationRecordService;
import com.oddfar.campus.common.domain.PageResult;
import com.oddfar.campus.common.core.page.PageUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ModerationRecordServiceImpl extends ServiceImpl<ModerationRecordMapper, ModerationRecordEntity>
        implements ModerationRecordService {

    @Resource
    private ModerationRecordMapper moderationRecordMapper;

    @Override
    public PageResult<ModerationRecordEntity> page(ModerationRecordEntity record) {
        PageUtils.startPage();
        List<ModerationRecordEntity> list = moderationRecordMapper.selectRecordPage(record);
        return PageUtils.getPageResult(list);
    }

    @Override
    public void addRecord(ModerationRecordEntity record) {
        if (record.getRecordId() == null) {
            record.setRecordId(IdWorker.getId());
        }
        moderationRecordMapper.insert(record);
    }

    @Override
    public ModerationRecordEntity getLatestRecord(Integer targetType, Long targetId) {
        LambdaQueryWrapper<ModerationRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModerationRecordEntity::getTargetType, targetType)
               .eq(ModerationRecordEntity::getTargetId, targetId)
               .orderByDesc(ModerationRecordEntity::getCreateTime)
               .last("LIMIT 1");
        return moderationRecordMapper.selectOne(wrapper);
    }
}
