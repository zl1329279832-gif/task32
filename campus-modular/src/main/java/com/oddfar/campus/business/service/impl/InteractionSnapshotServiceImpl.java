package com.oddfar.campus.business.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddfar.campus.business.domain.entity.ContentEntity;
import com.oddfar.campus.business.domain.entity.InteractionSnapshotEntity;
import com.oddfar.campus.business.mapper.CommentMapper;
import com.oddfar.campus.business.mapper.ContentMapper;
import com.oddfar.campus.business.mapper.InteractionSnapshotMapper;
import com.oddfar.campus.business.service.InteractionSnapshotService;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import com.oddfar.campus.business.domain.entity.CommentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 互动数据快照服务实现
 */
@Service
public class InteractionSnapshotServiceImpl extends ServiceImpl<InteractionSnapshotMapper, InteractionSnapshotEntity>
        implements InteractionSnapshotService {

    @Autowired
    private InteractionSnapshotMapper interactionSnapshotMapper;
    @Autowired
    private ContentMapper contentMapper;
    @Autowired
    private CommentMapper commentMapper;

    @Override
    public InteractionSnapshotEntity takeSnapshot(Long contentId, String snapshotType, Long moderationRecordId) {
        ContentEntity content = contentMapper.selectById(contentId);
        if (content == null) {
            return null;
        }

        // 统计当前评论数
        Long commentCount = commentMapper.selectCount(
                new LambdaQueryWrapperX<CommentEntity>()
                        .eq(CommentEntity::getContentId, contentId));

        InteractionSnapshotEntity snapshot = new InteractionSnapshotEntity();
        snapshot.setSnapshotId(IdWorker.getId());
        snapshot.setContentId(contentId);
        snapshot.setLoveCount(content.getLoveCount() != null ? content.getLoveCount() : 0L);
        snapshot.setCommentCount(commentCount != null ? commentCount : 0L);
        snapshot.setRecommendCount(0L);
        snapshot.setFavoriteCount(0L);
        snapshot.setReportCount(0L);
        snapshot.setSearchHitCount(0L);
        snapshot.setSnapshotType(snapshotType);
        snapshot.setModerationRecordId(moderationRecordId);
        snapshot.setCreateTime(new Date());

        interactionSnapshotMapper.insert(snapshot);
        return snapshot;
    }

    @Override
    public InteractionSnapshotEntity getLatestSnapshot(Long contentId) {
        return interactionSnapshotMapper.selectLatestByContentId(contentId);
    }
}
