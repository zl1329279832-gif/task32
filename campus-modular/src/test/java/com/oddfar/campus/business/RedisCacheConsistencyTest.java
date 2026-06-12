package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.*;
import com.oddfar.campus.business.mapper.ContentLoveMapper;
import com.oddfar.campus.business.mapper.ContentMapper;
import com.oddfar.campus.business.service.*;
import com.oddfar.campus.business.service.impl.ContentServiceImpl;
import com.oddfar.campus.common.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * Redis缓存一致性测试：下架/恢复/申诉均触发缓存失效
 */
@ExtendWith(MockitoExtension.class)
public class RedisCacheConsistencyTest extends BaseTest {

    @InjectMocks
    private ContentServiceImpl contentService;

    @Mock private ContentMapper contentMapper;
    @Mock private CommentService commentService;
    @Mock private ModerationRecordService moderationRecordService;
    @Mock private InteractionSnapshotService snapshotService;
    @Mock private AutoModerationService autoModerationService;
    @Mock private ContentLoveMapper contentLoveMapper;
    @Mock private CampusFileService fileService;
    @Mock private TagService tagService;
    @Mock private CategoryService categoryService;
    @Mock private ViolationRecordService violationRecordService;
    @Mock private GovernanceBatchService governanceBatchService;
    @Mock private GovernanceCacheService governanceCacheService;
    @Mock private ContentLoveService contentLoveService;

    @BeforeEach
    void setUp() {
        ModerationRecordEntity mockRecord = new ModerationRecordEntity();
        mockRecord.setRecordId(9999L);
        lenient().when(moderationRecordService.recordAction(anyLong(), anyString(), any(),
                anyString(), anyString(), anyString(), any(), any(), any())).thenReturn(mockRecord);
        lenient().when(moderationRecordService.updateById(any())).thenReturn(true);
        lenient().when(contentMapper.updateById(any())).thenReturn(1);
        lenient().when(fileService.list(any())).thenReturn(Collections.emptyList());
    }

    /**
     * 下架触发缓存清除
     */
    @Test
    void takedownEvictsContentCaches() {
        Long contentId = 7001L;
        ContentEntity content = createContent(contentId, 100L, 1L, "缓存测试", 1, 0);

        try (MockedStatic<SecurityUtils> secMock = mockStatic(SecurityUtils.class)) {
            secMock.when(SecurityUtils::getUserId).thenReturn(1L);
            when(contentMapper.selectById(contentId)).thenReturn(content);
            when(governanceBatchService.createBatch(anyString(), anyLong(), anyString(), anyInt()))
                    .thenReturn(createBatch(5001L, "TAKEDOWN", 1L, 1));
            when(snapshotService.takeSnapshot(eq(contentId), anyString(), any()))
                    .thenReturn(new InteractionSnapshotEntity());

            contentService.deleteContentById(contentId);

            verify(governanceCacheService).evictContentCaches(contentId);
        }
    }

    /**
     * 恢复触发缓存清除
     */
    @Test
    void restoreEvictsContentCaches() {
        Long contentId = 7002L;
        ContentEntity content = createContent(contentId, 100L, 1L, "恢复缓存", 2, 0);
        content.setLoveCount(5L);
        when(contentMapper.selectById(contentId)).thenReturn(content);
        when(snapshotService.getLatestSnapshot(contentId)).thenReturn(createSnapshot(8001L, contentId, 5L, 3L));
        when(contentLoveService.countByContentId(contentId)).thenReturn(5L);

        contentService.restoreContent(contentId, "恢复", "ADMIN_RESTORE");

        verify(governanceCacheService).evictContentCaches(contentId);
    }

    /**
     * 幂等下架不触发缓存清除（内容已下架）
     */
    @Test
    void idempotentTakedownSkipsCacheEviction() {
        Long contentId = 7003L;
        ContentEntity content = createContent(contentId, 100L, 1L, "已下架", 2, 0);
        when(contentMapper.selectById(contentId)).thenReturn(content);

        contentService.deleteContentById(contentId);

        verify(governanceCacheService, never()).evictContentCaches(anyLong());
    }

    /**
     * 幂等恢复不触发缓存清除（内容已正常）
     */
    @Test
    void idempotentRestoreSkipsCacheEviction() {
        Long contentId = 7004L;
        ContentEntity content = createContent(contentId, 100L, 1L, "已正常", 1, 0);
        when(contentMapper.selectById(contentId)).thenReturn(content);

        contentService.restoreContent(contentId, "重复恢复", "ADMIN_RESTORE");

        verify(governanceCacheService, never()).evictContentCaches(anyLong());
    }
}
