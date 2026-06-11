package com.oddfar.campus.business.mapper;

import com.oddfar.campus.business.domain.entity.ViolationRecordEntity;
import com.oddfar.campus.common.core.BaseMapperX;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ViolationRecordMapper extends BaseMapperX<ViolationRecordEntity> {

    /**
     * 查询用户的违规次数
     */
    default Long selectViolationCount(Long userId) {
        return selectCount(new LambdaQueryWrapperX<ViolationRecordEntity>()
                .eq(ViolationRecordEntity::getUserId, userId));
    }
}
