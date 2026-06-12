package com.oddfar.campus.business.mapper;

import com.oddfar.campus.business.domain.entity.CreditCompensationDetailEntity;
import com.oddfar.campus.common.core.BaseMapperX;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CreditCompensationDetailMapper extends BaseMapperX<CreditCompensationDetailEntity> {

    /**
     * 根据申诉id查询补偿明细
     */
    default List<CreditCompensationDetailEntity> selectByAppealId(Long appealId) {
        return selectList(new LambdaQueryWrapperX<CreditCompensationDetailEntity>()
                .eq(CreditCompensationDetailEntity::getAppealId, appealId)
                .orderByAsc(CreditCompensationDetailEntity::getCreateTime));
    }
}
