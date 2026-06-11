package com.oddfar.campus.business.mapper;

import com.oddfar.campus.business.domain.entity.CreditLogEntity;
import com.oddfar.campus.common.core.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CreditLogMapper extends BaseMapperX<CreditLogEntity> {

    /**
     * 查询用户的信用分变更日志
     */
    List<CreditLogEntity> selectByUserId(@Param("userId") Long userId);
}
