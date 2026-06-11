package com.oddfar.campus.business.mapper;

import com.oddfar.campus.business.domain.entity.AdminModerationScopeEntity;
import com.oddfar.campus.common.core.BaseMapperX;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AdminModerationScopeMapper extends BaseMapperX<AdminModerationScopeEntity> {

    /**
     * 查询管理员的审核范围
     */
    default List<AdminModerationScopeEntity> selectByAdminUserId(Long adminUserId) {
        return selectList(new LambdaQueryWrapperX<AdminModerationScopeEntity>()
                .eq(AdminModerationScopeEntity::getAdminUserId, adminUserId));
    }
}
