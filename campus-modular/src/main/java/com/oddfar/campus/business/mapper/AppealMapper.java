package com.oddfar.campus.business.mapper;

import com.oddfar.campus.business.domain.entity.AppealEntity;
import com.oddfar.campus.common.core.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AppealMapper extends BaseMapperX<AppealEntity> {

    List<AppealEntity> selectAppealPage(AppealEntity appeal);
}
