package com.oddfar.campus.business.mapper;

import com.oddfar.campus.business.domain.entity.CreditLogEntity;
import com.oddfar.campus.common.core.BaseMapperX;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CreditLogMapper extends BaseMapperX<CreditLogEntity> {

    /**
     * 查询用户的信用分变更日志
     */
    List<CreditLogEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 检查是否已存在相同关联类型和关联id的信用分变更记录（幂等检查）
     */
    default boolean existsByRelated(Long userId, String relatedType, Long relatedId) {
        return selectCount(new LambdaQueryWrapperX<CreditLogEntity>()
                .eq(CreditLogEntity::getUserId, userId)
                .eq(CreditLogEntity::getRelatedType, relatedType)
                .eq(CreditLogEntity::getRelatedId, relatedId)) > 0;
    }
}
