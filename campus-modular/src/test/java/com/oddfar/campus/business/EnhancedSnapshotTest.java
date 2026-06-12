package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.*;
import com.oddfar.campus.business.mapper.CommentMapper;
import com.oddfar.campus.business.mapper.ContentMapper;
import com.oddfar.campus.business.mapper.InteractionSnapshotMapper;
import com.oddfar.campus.business.service.impl.InteractionSnapshotServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 增强快照字段测试
 */
@ExtendWith(MockitoExtension.class)
public class EnhancedSnapshotTest extends BaseTest {

    @InjectMocks
    private InteractionSnapshotServiceImpl snapshotService;

    @Mock private InteractionSnapshotMapper interactionSnapshotMapper;
    @Mock private ContentMapper contentMapper;
    @Mock private CommentMapper commentMapper;

    /**
     * 快照包含新增字段且默认为0
     */
    @Test
    void snapshotCapturesNewFieldsWithDefaults() {
        Long contentId = 8001L;
        ContentEntity content = createContent(contentId, 100L, 1L, "快照测试", 1, 0);
        content.setLoveCount(25L);

        when(contentMapper.selectById(contentId)).thenReturn(content);
        when(commentMapper.selectCount(any())).thenReturn(10L);
        when(interactionSnapshotMapper.insert(any())).thenReturn(1);

        InteractionSnapshotEntity snapshot = snapshotService.takeSnapshot(contentId, "TAKEDOWN", null);

        assertNotNull(snapshot);
        assertEquals(25L, snapshot.getLoveCount());
        assertEquals(10L, snapshot.getCommentCount());
        // 新增字段默认为0
        assertEquals(0L, snapshot.getRecommendCount());
        assertEquals(0L, snapshot.getBookmarkCount());
        assertEquals(0L, snapshot.getReportCount());
        assertEquals(0L, snapshot.getSearchHitCount());
        assertEquals("TAKEDOWN", snapshot.getSnapshotType());

        verify(interactionSnapshotMapper).insert(argThat(s ->
                s.getRecommendCount() != null && s.getRecommendCount() == 0L
                        && s.getBookmarkCount() != null && s.getBookmarkCount() == 0L
                        && s.getReportCount() != null && s.getReportCount() == 0L
                        && s.getSearchHitCount() != null && s.getSearchHitCount() == 0L));
    }

    /**
     * 内容不存在时快照返回null
     */
    @Test
    void snapshotReturnsNullForNonExistentContent() {
        when(contentMapper.selectById(9999L)).thenReturn(null);

        InteractionSnapshotEntity snapshot = snapshotService.takeSnapshot(9999L, "TAKEDOWN", null);

        assertNull(snapshot);
        verify(interactionSnapshotMapper, never()).insert(any());
    }
}
