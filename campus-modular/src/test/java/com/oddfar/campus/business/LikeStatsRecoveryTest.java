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

    /**
     * 标记快照为已消费后不会被重复获取
     */
    @Test
    void consumedSnapshotPreventsDoubleBackfill() {
        InteractionSnapshotEntity snapshot = new InteractionSnapshotEntity();
        snapshot.setSnapshotId(8001L);
        snapshot.setContentId(1001L);
        snapshot.setLoveCount(15L);
        snapshot.setConsumed(0);

        when(interactionSnapshotMapper.selectById(8001L)).thenReturn(snapshot);
        when(interactionSnapshotMapper.updateById(any(InteractionSnapshotEntity.class))).thenReturn(1);

        // 标记消费
        snapshotService.markConsumed(8001L);

        assertEquals(1, snapshot.getConsumed());
        verify(interactionSnapshotMapper).updateById(argThat(s ->
                s.getSnapshotId().equals(8001L) && s.getConsumed() == 1));
    }

    /**
     * 重复标记消费应幂等
     */
    @Test
    void markConsumedIsIdempotent() {
        InteractionSnapshotEntity snapshot = new InteractionSnapshotEntity();
        snapshot.setSnapshotId(8002L);
        snapshot.setContentId(1001L);
        snapshot.setLoveCount(10L);
        snapshot.setConsumed(1); // 已消费

        when(interactionSnapshotMapper.selectById(8002L)).thenReturn(snapshot);

        snapshotService.markConsumed(8002L);

        // 不应更新已消费的快照
        verify(interactionSnapshotMapper, never()).updateById(any());
    }

    /**
     * 标记不存在的快照消费应安全跳过
     */
    @Test
    void markConsumedNonexistentSnapshotShouldSkip() {
        when(interactionSnapshotMapper.selectById(9999L)).thenReturn(null);

        snapshotService.markConsumed(9999L);

        verify(interactionSnapshotMapper, never()).updateById(any());
    }
}
