package com.oddfar.campus.business.mapper;

import com.oddfar.campus.business.domain.entity.GovernanceBatchEntity;
import com.oddfar.campus.common.core.BaseMapperX;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GovernanceBatchMapper extends BaseMapperX<GovernanceBatchEntity> {

    /**
     * 根据管理员id查询批次列表
     */
    default List<GovernanceBatchEntity> selectByAdminId(Long adminId) {
        return selectList(new LambdaQueryWrapperX<GovernanceBatchEntity>()
                .eq(GovernanceBatchEntity::getAdminId, adminId)
                .orderByDesc(GovernanceBatchEntity::getCreateTime));
    }
}
