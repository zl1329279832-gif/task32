package com.oddfar.campus.business.mapper;

import com.oddfar.campus.business.domain.entity.ModerationRecordEntity;
import com.oddfar.campus.common.core.BaseMapperX;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ModerationRecordMapper extends BaseMapperX<ModerationRecordEntity> {

    /**
     * 根据内容id查询审核记录
     */
    default List<ModerationRecordEntity> selectByContentId(Long contentId) {
        return selectList(new LambdaQueryWrapperX<ModerationRecordEntity>()
                .eq(ModerationRecordEntity::getContentId, contentId)
                .orderByDesc(ModerationRecordEntity::getCreateTime));
    }
}
