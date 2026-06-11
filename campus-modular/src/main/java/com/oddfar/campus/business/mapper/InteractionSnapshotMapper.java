package com.oddfar.campus.business.mapper;

import com.oddfar.campus.business.domain.entity.InteractionSnapshotEntity;
import com.oddfar.campus.common.core.BaseMapperX;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InteractionSnapshotMapper extends BaseMapperX<InteractionSnapshotEntity> {

    /**
     * 查询某内容最新的互动快照
     */
    default InteractionSnapshotEntity selectLatestByContentId(Long contentId) {
        return selectOne(new LambdaQueryWrapperX<InteractionSnapshotEntity>()
                .eq(InteractionSnapshotEntity::getContentId, contentId)
                .orderByDesc(InteractionSnapshotEntity::getCreateTime)
                .last("LIMIT 1"));
    }
}
