package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.*;
import com.oddfar.campus.business.mapper.ContentLoveMapper;
import com.oddfar.campus.business.mapper.ContentMapper;
import com.oddfar.campus.business.service.*;
import com.oddfar.campus.business.service.impl.ContentServiceImpl;
import com.oddfar.campus.business.service.impl.GovernanceCacheHelper;
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
 * 内容恢复全流程测试
 * 覆盖：restoreContent 统一恢复方法的内容状态、评论解冻、附件违规清除、点赞统计回补、审核记录
 */
@ExtendWith(MockitoExtension.class)
public class ContentRestoreFullTest extends BaseTest {

    @InjectMocks
    private ContentServiceImpl contentService;

    @Mock
    private ContentMapper contentMapper;
    @Mock
    private CommentService commentService;
    @Mock
    private ModerationRecordService moderationRecordService;
    @Mock
    private InteractionSnapshotService snapshotService;
    @Mock
    private AutoModerationService autoModerationService;
    @Mock
    private ContentLoveMapper contentLoveMapper;
    @Mock
    private CampusFileService fileService;
    @Mock
    private TagService tagService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private ViolationRecordService violationRecordService;
    @Mock
    private GovernanceCacheHelper governanceCacheHelper;

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
     * 申诉通过恢复下架内容 → 完整级联：
     * 1. 内容状态 2→1
     * 2. 评论解冻
     * 3. 附件违规清除（关键修复点）
     * 4. 点赞数从快照恢复
     * 5. 审核记录包含 FILE 类型的 CLEAR 记录
     */
    @Test
    void restoreTakedownContentClearsFileViolations() {
        // 内容处于下架状态
        ContentEntity content = createContent(1001L, 100L, 1L, "被下架的内容", 2, 1);
        content.setLoveCount(0L);
        when(contentMapper.selectById(1001L)).thenReturn(content);

        // 快照有15个点赞
        InteractionSnapshotEntity snapshot = new InteractionSnapshotEntity();
        snapshot.setLoveCount(15L);
        snapshot.setCommentCount(8L);
        when(snapshotService.getLatestSnapshot(1001L)).thenReturn(snapshot);

        // 有2个附件被标记为违规
        CampusFileEntity file1 = new CampusFileEntity();
        file1.setFileId(2001L);
        file1.setContentId(1001L);
        file1.setViolationStatus(1);
        file1.setViolationReason("图片含违规内容");

        CampusFileEntity file2 = new CampusFileEntity();
        file2.setFileId(2002L);
        file2.setContentId(1001L);
        file2.setViolationStatus(1);
        file2.setViolationReason("图片含广告");

        when(fileService.list(any()))
                .thenReturn(Arrays.asList(file1, file2));

        // 执行恢复
        contentService.restoreContent(1001L, "申诉通过恢复", "APPEAL");

        // 验证1: 内容状态恢复为正常(1)
        assertEquals(1, content.getStatus());

        // 验证2: 点赞数从快照恢复
        assertEquals(15L, content.getLoveCount());

        // 验证3: 评论被解冻
        verify(commentService).unfreezeToReadOnly(1001L);

        // 验证4: 两个附件违规被清除
        verify(fileService).clearViolation(2001L);
        verify(fileService).clearViolation(2002L);

        // 验证5: 审核记录包含附件级别的RESTORE记录
        verify(moderationRecordService, atLeast(2)).recordAction(
                eq(1001L), eq("FILE"), anyLong(),
                anyString(), eq("RESTORE"),
                contains("附件违规"), isNull(), eq(1), eq(0));

        // 验证6: 审核记录包含内容级别的RESTORE记录
        verify(moderationRecordService).recordAction(
                eq(1001L), eq("CONTENT"), isNull(),
                anyString(), eq("RESTORE"),
                anyString(), isNull(), eq(2), eq(1));
    }

    /**
     * 恢复无附件违规的内容 → 不触发 clearViolation
     */
    @Test
    void restoreContentWithoutViolationsSkipsFileClearing() {
        ContentEntity content = createContent(1001L, 100L, 1L, "无违规附件", 3, 0);
        content.setLoveCount(5L);
        when(contentMapper.selectById(1001L)).thenReturn(content);

        InteractionSnapshotEntity snapshot = new InteractionSnapshotEntity();
        snapshot.setLoveCount(5L);
        when(snapshotService.getLatestSnapshot(1001L)).thenReturn(snapshot);

        // 没有违规附件
        when(fileService.list(any()))
                .thenReturn(Collections.emptyList());

        contentService.restoreContent(1001L, "审核通过", "ADMIN_RESTORE");

        // 验证：内容恢复
        assertEquals(1, content.getStatus());
        // 验证：评论解冻
        verify(commentService).unfreezeToReadOnly(1001L);
        // 验证：没有调用 clearViolation
        verify(fileService, never()).clearViolation(anyLong());
    }

    /**
     * 恢复内容时审核记录包含快照统计信息
     */
    @Test
    void restoreRecordsSnapshotStats() {
        ContentEntity content = createContent(1001L, 100L, 1L, "恢复内容", 2, 0);
        content.setLoveCount(0L);
        when(contentMapper.selectById(1001L)).thenReturn(content);

        InteractionSnapshotEntity snapshot = new InteractionSnapshotEntity();
        snapshot.setLoveCount(20L);
        snapshot.setCommentCount(12L);
        when(snapshotService.getLatestSnapshot(1001L)).thenReturn(snapshot);
        when(fileService.list(any()))
                .thenReturn(Collections.emptyList());

        contentService.restoreContent(1001L, "恢复", "APPEAL");

        // 验证审核记录被更新（包含快照统计）
        verify(moderationRecordService).updateById(argThat(record ->
                record.getSnapshotLoveCount() != null &&
                        record.getSnapshotLoveCount().equals(20L) &&
                        record.getSnapshotCommentCount() != null &&
                        record.getSnapshotCommentCount().equals(12L)
        ));
    }

    /**
     * 恢复拒绝状态(3)的内容 → 正常恢复
     */
    @Test
    void restoreRejectedContent() {
        ContentEntity content = createContent(1001L, 100L, 1L, "被拒绝的内容", 3, 0);
        content.setLoveCount(0L);
        when(contentMapper.selectById(1001L)).thenReturn(content);
        when(snapshotService.getLatestSnapshot(1001L)).thenReturn(null);
        when(fileService.list(any()))
                .thenReturn(Collections.emptyList());

        contentService.restoreContent(1001L, "申诉通过", "APPEAL");

        assertEquals(1, content.getStatus());
        verify(commentService).unfreezeToReadOnly(1001L);
    }
}
