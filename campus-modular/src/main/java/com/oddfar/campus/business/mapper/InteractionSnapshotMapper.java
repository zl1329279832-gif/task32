package com.oddfar.campus.business.mapper;

import com.oddfar.campus.business.domain.entity.InteractionSnapshotEntity;
import com.oddfar.campus.common.core.BaseMapperX;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InteractionSnapshotMapper extends BaseMapperX<InteractionSnapshotEntity> {

    /**
     * 查询某内容最新的未消费快照
     */
    default InteractionSnapshotEntity selectLatestByContentId(Long contentId) {
        return selectOne(new LambdaQueryWrapperX<InteractionSnapshotEntity>()
                .eq(InteractionSnapshotEntity::getContentId, contentId)
                .and(w -> w.eq(InteractionSnapshotEntity::getConsumed, 0)
                        .or().isNull(InteractionSnapshotEntity::getConsumed))
                .orderByDesc(InteractionSnapshotEntity::getCreateTime)
                .last("LIMIT 1"));
    }
}
