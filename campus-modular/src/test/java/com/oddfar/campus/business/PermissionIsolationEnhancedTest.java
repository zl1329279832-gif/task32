package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.*;
import com.oddfar.campus.business.mapper.ContentLoveMapper;
import com.oddfar.campus.business.mapper.ContentMapper;
import com.oddfar.campus.business.service.*;
import com.oddfar.campus.business.service.impl.ContentServiceImpl;
import com.oddfar.campus.common.exception.ServiceException;
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
 * 增强权限隔离测试：批次操作逐条检查scope
 */
@ExtendWith(MockitoExtension.class)
public class PermissionIsolationEnhancedTest extends BaseTest {

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
     * 超级管理员(userId=1)下架任何内容均成功
     */
    @Test
    void superAdminCanTakedownAnyContent() {
        Long contentId = 9001L;
        ContentEntity content = createContent(contentId, 200L, 5L, "超管测试", 1, 0);

        try (MockedStatic<SecurityUtils> secMock = mockStatic(SecurityUtils.class)) {
            secMock.when(SecurityUtils::getUserId).thenReturn(1L);
            when(contentMapper.selectById(contentId)).thenReturn(content);
            when(governanceBatchService.createBatch(anyString(), anyLong(), anyString(), anyInt()))
                    .thenReturn(createBatch(5001L, "TAKEDOWN", 1L, 1));
            when(snapshotService.takeSnapshot(eq(contentId), anyString(), any()))
                    .thenReturn(new InteractionSnapshotEntity());

            contentService.deleteContentById(contentId);

            assertEquals(2, content.getStatus());
            verify(governanceBatchService).createBatch("TAKEDOWN", 1L, "管理员下架", 1);
        }
    }

    /**
     * 恢复任何状态正常的内容
     */
    @Test
    void restoreContentWorks() {
        Long contentId = 9002L;
        ContentEntity content = createContent(contentId, 200L, 5L, "恢复测试", 2, 0);
        content.setLoveCount(3L);
        when(contentMapper.selectById(contentId)).thenReturn(content);
        when(snapshotService.getLatestSnapshot(contentId)).thenReturn(createSnapshot(8001L, contentId, 3L, 1L));
        when(contentLoveService.countByContentId(contentId)).thenReturn(3L);

        contentService.restoreContent(contentId, "恢复", "ADMIN_RESTORE");

        assertEquals(1, content.getStatus());
        assertEquals(3L, content.getLoveCount());
        verify(commentService).unfreezeByContentIdWithReadOnly(contentId, 60);
    }

    /**
     * 点赞对账：实际点赞 > 快照时保留实际值
     */
    @Test
    void likeReconciliationKeepsHigherActualCount() {
        Long contentId = 9003L;
        ContentEntity content = createContent(contentId, 200L, 5L, "对账测试", 2, 0);
        content.setLoveCount(0L);
        when(contentMapper.selectById(contentId)).thenReturn(content);

        InteractionSnapshotEntity snapshot = createSnapshot(8002L, contentId, 10L, 5L);
        when(snapshotService.getLatestSnapshot(contentId)).thenReturn(snapshot);
        // 实际点赞15 > 快照10
        when(contentLoveService.countByContentId(contentId)).thenReturn(15L);

        contentService.restoreContent(contentId, "恢复", "ADMIN_RESTORE");

        // 应保留实际点赞数15而非快照的10
        assertEquals(15L, content.getLoveCount());
    }

    /**
     * 点赞对账：快照点赞 > 实际时恢复快照值
     */
    @Test
    void likeReconciliationRestoresSnapshotWhenHigher() {
        Long contentId = 9004L;
        ContentEntity content = createContent(contentId, 200L, 5L, "快照恢复测试", 2, 0);
        content.setLoveCount(0L);
        when(contentMapper.selectById(contentId)).thenReturn(content);

        InteractionSnapshotEntity snapshot = createSnapshot(8003L, contentId, 20L, 5L);
        when(snapshotService.getLatestSnapshot(contentId)).thenReturn(snapshot);
        // 实际点赞3 < 快照20
        when(contentLoveService.countByContentId(contentId)).thenReturn(3L);

        contentService.restoreContent(contentId, "恢复", "ADMIN_RESTORE");

        // 应恢复快照值20
        assertEquals(20L, content.getLoveCount());
    }
}
