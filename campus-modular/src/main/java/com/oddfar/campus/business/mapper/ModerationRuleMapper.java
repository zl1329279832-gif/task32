package com.oddfar.campus.business.mapper;

import com.oddfar.campus.business.domain.entity.ModerationRuleEntity;
import com.oddfar.campus.common.core.BaseMapperX;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ModerationRuleMapper extends BaseMapperX<ModerationRuleEntity> {

    /**
     * 查询所有启用的审核规则，按优先级降序
     */
    default List<ModerationRuleEntity> selectEnabledRules() {
        return selectList(new LambdaQueryWrapperX<ModerationRuleEntity>()
                .eq(ModerationRuleEntity::getStatus, "0")
                .orderByDesc(ModerationRuleEntity::getPriority));
    }
}
