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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 批量下架治理批次测试
 */
@ExtendWith(MockitoExtension.class)
public class BatchTakedownTest extends BaseTest {

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
     * 单次下架创建治理批次
     */
    @Test
    void takedownCreatesGovernanceBatch() {
        Long contentId = 2001L;
        ContentEntity content = createContent(contentId, 100L, 1L, "测试内容", 1, 0);

        try (MockedStatic<SecurityUtils> secMock = mockStatic(SecurityUtils.class)) {
            secMock.when(SecurityUtils::getUserId).thenReturn(1L);

            when(contentMapper.selectById(contentId)).thenReturn(content);
            GovernanceBatchEntity batch = createBatch(5001L, "TAKEDOWN", 1L, 1);
            when(governanceBatchService.createBatch(eq("TAKEDOWN"), eq(1L), eq("管理员下架"), eq(1)))
                    .thenReturn(batch);
            when(snapshotService.takeSnapshot(eq(contentId), anyString(), any()))
                    .thenReturn(new InteractionSnapshotEntity());

            contentService.deleteContentById(contentId);

            // 验证批次被创建
            verify(governanceBatchService).createBatch("TAKEDOWN", 1L, "管理员下架", 1);
            // 验证审核记录关联了批次id
            verify(moderationRecordService).updateById(argThat(record ->
                    record.getBatchId() != null && record.getBatchId().equals(5001L)));
            // 验证缓存被清除
            verify(governanceCacheService).evictContentCaches(contentId);
        }
    }

    /**
     * 幂等：重复下架不创建新批次
     */
    @Test
    void duplicateTakedownSkipsBatchCreation() {
        Long contentId = 2002L;
        ContentEntity content = createContent(contentId, 100L, 1L, "已下架内容", 2, 0);

        try (MockedStatic<SecurityUtils> secMock = mockStatic(SecurityUtils.class)) {
            secMock.when(SecurityUtils::getUserId).thenReturn(1L);
            when(contentMapper.selectById(contentId)).thenReturn(content);

            contentService.deleteContentById(contentId);

            verify(governanceBatchService, never()).createBatch(anyString(), anyLong(), anyString(), anyInt());
            verify(governanceCacheService, never()).evictContentCaches(anyLong());
        }
    }

    /**
     * 恢复内容也触发缓存清除
     */
    @Test
    void restoreEvictsCaches() {
        Long contentId = 2003L;
        ContentEntity content = createContent(contentId, 100L, 1L, "待恢复", 2, 0);
        content.setLoveCount(5L);
        when(contentMapper.selectById(contentId)).thenReturn(content);
        InteractionSnapshotEntity snapshot = createSnapshot(8001L, contentId, 5L, 3L);
        when(snapshotService.getLatestSnapshot(contentId)).thenReturn(snapshot);
        when(contentLoveService.countByContentId(contentId)).thenReturn(5L);

        contentService.restoreContent(contentId, "恢复", "ADMIN_RESTORE");

        verify(governanceCacheService).evictContentCaches(contentId);
    }
}
