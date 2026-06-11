package com.oddfar.campus.business.mapper;

import com.oddfar.campus.business.domain.entity.ModerationRecordEntity;
import com.oddfar.campus.common.core.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ModerationRecordMapper extends BaseMapperX<ModerationRecordEntity> {

    List<ModerationRecordEntity> selectRecordPage(ModerationRecordEntity record);
}
