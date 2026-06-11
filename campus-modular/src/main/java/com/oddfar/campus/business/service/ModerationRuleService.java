package com.oddfar.campus.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oddfar.campus.business.domain.entity.ModerationRuleEntity;
import com.oddfar.campus.common.domain.PageResult;

import java.util.List;

/**
 * 审核规则服务
 */
public interface ModerationRuleService extends IService<ModerationRuleEntity> {

    /**
     * 获取所有启用的审核规则
     */
    List<ModerationRuleEntity> getEnabledRules();

    /**
     * 分页查询
     */
    PageResult<ModerationRuleEntity> page(ModerationRuleEntity entity);
}
