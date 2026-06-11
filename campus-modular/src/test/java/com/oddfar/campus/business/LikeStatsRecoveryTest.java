package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.ContentEntity;
import com.oddfar.campus.business.domain.entity.InteractionSnapshotEntity;
import com.oddfar.campus.business.domain.entity.CommentEntity;
import com.oddfar.campus.business.mapper.CommentMapper;
import com.oddfar.campus.business.mapper.ContentMapper;
import com.oddfar.campus.business.mapper.InteractionSnapshotMapper;
import com.oddfar.campus.business.service.impl.InteractionSnapshotServiceImpl;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 点赞统计回补测试
 */
@ExtendWith(MockitoExtension.class)
public class LikeStatsRecoveryTest extends BaseTest {

    @InjectMocks
    private InteractionSnapshotServiceImpl snapshotService;

    @Mock
    private InteractionSnapshotMapper interactionSnapshotMapper;
    @Mock
    private ContentMapper contentMapper;
    @Mock
    private CommentMapper commentMapper;

    /**
     * 下架时快照loveCount，申诉通过后恢复
     */
    @Test
    void loveCountRecoveredAfterAppeal() {
        // 1. 模拟下架时拍摄快照
        ContentEntity content = createContent(1001L, 100L, 1L, "测试内容", 1, 0);
        content.setLoveCount(15L);

        when(contentMapper.selectById(1001L)).thenReturn(content);
        when(commentMapper.selectCount(any(LambdaQueryWrapperX.class))).thenReturn(8L);
        when(interactionSnapshotMapper.insert(any(InteractionSnapshotEntity.class))).thenReturn(1);

        InteractionSnapshotEntity snapshot = snapshotService.takeSnapshot(1001L, "TAKEDOWN", null);

        assertNotNull(snapshot);
        assertEquals(15L, snapshot.getLoveCount());
        assertEquals(8L, snapshot.getCommentCount());
        assertEquals("TAKEDOWN", snapshot.getSnapshotType());

        // 2. 模拟申诉通过后恢复
        when(interactionSnapshotMapper.selectLatestByContentId(1001L)).thenReturn(snapshot);

        InteractionSnapshotEntity restored = snapshotService.getLatestSnapshot(1001L);
        assertNotNull(restored);
        assertEquals(15L, restored.getLoveCount());
    }

    /**
     * 评论数在下架时被快照，申诉通过后解冻恢复
     */
    @Test
    void commentCountRecoveredAfterAppeal() {
        ContentEntity content = createContent(1001L, 100L, 1L, "测试内容", 1, 0);
        content.setLoveCount(5L);

        when(contentMapper.selectById(1001L)).thenReturn(content);
        when(commentMapper.selectCount(any(LambdaQueryWrapperX.class))).thenReturn(12L);
        when(interactionSnapshotMapper.insert(any(InteractionSnapshotEntity.class))).thenReturn(1);

        InteractionSnapshotEntity snapshot = snapshotService.takeSnapshot(1001L, "TAKEDOWN", null);

        assertNotNull(snapshot);
        assertEquals(12L, snapshot.getCommentCount());
        assertEquals(1001L, snapshot.getContentId());
    }
}
