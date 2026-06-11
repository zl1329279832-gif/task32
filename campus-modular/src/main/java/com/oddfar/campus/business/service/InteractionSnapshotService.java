package com.oddfar.campus.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oddfar.campus.business.domain.entity.InteractionSnapshotEntity;

/**
 * 互动数据快照服务
 */
public interface InteractionSnapshotService extends IService<InteractionSnapshotEntity> {

    /**
     * 拍摄快照（下架/拒绝前保存互动数据）
     */
    InteractionSnapshotEntity takeSnapshot(Long contentId, String snapshotType, Long moderationRecordId);

    /**
     * 获取某内容最新的未消费快照
     */
    InteractionSnapshotEntity getLatestSnapshot(Long contentId);

    /**
     * 标记快照为已消费（防止重复回补点赞数）
     */
    void markConsumed(Long snapshotId);
}
