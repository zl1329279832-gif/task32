package com.oddfar.campus.business.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddfar.campus.business.domain.entity.ModerationRuleEntity;
import com.oddfar.campus.business.mapper.ModerationRuleMapper;
import com.oddfar.campus.business.service.ModerationRuleService;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import com.oddfar.campus.common.domain.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审核规则服务实现
 */
@Service
public class ModerationRuleServiceImpl extends ServiceImpl<ModerationRuleMapper, ModerationRuleEntity>
        implements ModerationRuleService {

    @Autowired
    private ModerationRuleMapper moderationRuleMapper;

    @Override
    public List<ModerationRuleEntity> getEnabledRules() {
        return moderationRuleMapper.selectEnabledRules();
    }

    @Override
    public PageResult<ModerationRuleEntity> page(ModerationRuleEntity entity) {
        LambdaQueryWrapperX<ModerationRuleEntity> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eqIfPresent(ModerationRuleEntity::getRuleType, entity.getRuleType());
        wrapper.eqIfPresent(ModerationRuleEntity::getStatus, entity.getStatus());
        wrapper.orderByDesc(ModerationRuleEntity::getPriority);
        return moderationRuleMapper.selectPage(wrapper);
    }
}
