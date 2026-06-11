package com.oddfar.campus.business.mapper;

import com.oddfar.campus.business.domain.entity.SensitiveWordEntity;
import com.oddfar.campus.common.core.BaseMapperX;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SensitiveWordMapper extends BaseMapperX<SensitiveWordEntity> {

    /**
     * 查询所有启用的敏感词
     */
    default List<SensitiveWordEntity> selectEnabledWords() {
        return selectList(new LambdaQueryWrapperX<SensitiveWordEntity>()
                .eq(SensitiveWordEntity::getStatus, "0"));
    }
}
