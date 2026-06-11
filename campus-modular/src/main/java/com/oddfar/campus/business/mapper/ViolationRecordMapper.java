package com.oddfar.campus.business.mapper;

import com.oddfar.campus.business.domain.entity.ViolationRecordEntity;
import com.oddfar.campus.common.core.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ViolationRecordMapper extends BaseMapperX<ViolationRecordEntity> {

    List<ViolationRecordEntity> selectViolationPage(ViolationRecordEntity record);
}
