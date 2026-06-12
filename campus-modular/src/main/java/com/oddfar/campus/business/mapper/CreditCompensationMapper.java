package com.oddfar.campus.business.mapper;

import com.oddfar.campus.business.domain.entity.CreditCompensationEntity;
import com.oddfar.campus.common.core.BaseMapperX;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CreditCompensationMapper extends BaseMapperX<CreditCompensationEntity> {

    /**
     * 根据申诉id查询补偿明细
     */
    default List<CreditCompensationEntity> selectByAppealId(Long appealId) {
        return selectList(new LambdaQueryWrapperX<CreditCompensationEntity>()
                .eq(CreditCompensationEntity::getAppealId, appealId));
    }

    /**
     * 根据用户id查询补偿明细
     */
    default List<CreditCompensationEntity> selectByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<CreditCompensationEntity>()
                .eq(CreditCompensationEntity::getUserId, userId)
                .orderByDesc(CreditCompensationEntity::getCreateTime));
    }
}
