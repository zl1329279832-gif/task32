package com.oddfar.campus.business.mapper;

import com.oddfar.campus.business.domain.entity.UserCreditEntity;
import com.oddfar.campus.common.core.BaseMapperX;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserCreditMapper extends BaseMapperX<UserCreditEntity> {

    /**
     * 根据用户id查询信用分
     */
    default UserCreditEntity selectByUserId(Long userId) {
        return selectOne(new LambdaQueryWrapperX<UserCreditEntity>()
                .eq(UserCreditEntity::getUserId, userId));
    }
}
