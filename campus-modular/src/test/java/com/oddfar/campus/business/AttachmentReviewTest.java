package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.*;
import com.oddfar.campus.business.mapper.ContentLoveMapper;
import com.oddfar.campus.business.mapper.ContentMapper;
import com.oddfar.campus.business.service.*;
import com.oddfar.campus.business.service.impl.ContentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 附件复核测试：部分附件维持违规场景
 */
@ExtendWith(MockitoExtension.class)
public class AttachmentReviewTest extends BaseTest {

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
    }

    /**
     * 恢复时仅清除复核状态非"维持违规"的附件
     * 3个附件：reviewStatus=0(未复核), 1(复核通过), 2(维持违规)
     * 预期：前2个clearViolation, 第3个跳过
     */
    @Test
    void restoreSkipsMaintainedViolationFiles() {
        Long contentId = 4001L;
        ContentEntity content = createContent(contentId, 100L, 1L, "含多附件内容", 2, 1);
        content.setLoveCount(10L);
        when(contentMapper.selectById(contentId)).thenReturn(content);

        InteractionSnapshotEntity snapshot = createSnapshot(8001L, contentId, 10L, 5L);
        when(snapshotService.getLatestSnapshot(contentId)).thenReturn(snapshot);
        when(contentLoveService.countByContentId(contentId)).thenReturn(10L);

        // 3个违规附件，不同复核状态
        CampusFileEntity file1 = createFile(5001L, contentId, 100L, 1, 0); // 未复核
        CampusFileEntity file2 = createFile(5002L, contentId, 100L, 1, 1); // 复核通过
        CampusFileEntity file3 = createFile(5003L, contentId, 100L, 1, 2); // 维持违规

        when(fileService.list(any())).thenReturn(Arrays.asList(file1, file2, file3));

        contentService.restoreContent(contentId, "申诉通过", "APPEAL");

        // 验证：前两个附件违规被清除
        verify(fileService).clearViolation(5001L);
        verify(fileService).clearViolation(5002L);
        // 验证：维持违规的附件不被清除
        verify(fileService, never()).clearViolation(5003L);
    }

    /**
     * 所有附件都是未复核状态 → 全部清除
     */
    @Test
    void restoreWithAllUnreviewedFilesClearsAll() {
        Long contentId = 4002L;
        ContentEntity content = createContent(contentId, 100L, 1L, "全部未复核", 2, 1);
        content.setLoveCount(5L);
        when(contentMapper.selectById(contentId)).thenReturn(content);
        when(snapshotService.getLatestSnapshot(contentId)).thenReturn(createSnapshot(8002L, contentId, 5L, 2L));
        when(contentLoveService.countByContentId(contentId)).thenReturn(5L);

        CampusFileEntity file1 = createFile(5004L, contentId, 100L, 1, 0);
        CampusFileEntity file2 = createFile(5005L, contentId, 100L, 1, 0);
        when(fileService.list(any())).thenReturn(Arrays.asList(file1, file2));

        contentService.restoreContent(contentId, "恢复", "ADMIN_RESTORE");

        verify(fileService).clearViolation(5004L);
        verify(fileService).clearViolation(5005L);
    }

    /**
     * 无违规附件 → 不触发clearViolation
     */
    @Test
    void restoreWithoutViolatedFilesSkipsFileClear() {
        Long contentId = 4003L;
        ContentEntity content = createContent(contentId, 100L, 1L, "无违规附件", 3, 0);
        content.setLoveCount(0L);
        when(contentMapper.selectById(contentId)).thenReturn(content);
        when(snapshotService.getLatestSnapshot(contentId)).thenReturn(null);
        when(fileService.list(any())).thenReturn(Collections.emptyList());

        contentService.restoreContent(contentId, "审核通过", "ADMIN_RESTORE");

        verify(fileService, never()).clearViolation(anyLong());
    }
}
