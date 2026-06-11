package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.*;
import com.oddfar.campus.business.mapper.*;
import com.oddfar.campus.business.service.*;
import com.oddfar.campus.business.service.impl.ContentServiceImpl;
import com.oddfar.campus.business.service.impl.UserCreditServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 重复操作幂等性测试
 * 覆盖：重复下架、重复恢复、重复信用分变更、重复附件违规标记/清除
 */
@ExtendWith(MockitoExtension.class)
public class DuplicateOperationTest extends BaseTest {

    // ==================== ContentServiceImpl 幂等测试 ====================

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

    @BeforeEach
    void setUp() {
        ModerationRecordEntity mockRecord = new ModerationRecordEntity();
        mockRecord.setRecordId(9999L);
        lenient().when(moderationRecordService.recordAction(anyLong(), anyString(), any(),
                anyString(), anyString(), anyString(), any(), any(), any())).thenReturn(mockRecord);
        lenient().when(moderationRecordService.updateById(any())).thenReturn(true);
    }

    /**
     * 重复下架同一内容 → 第二次跳过，不产生重复快照或冻结
     */
    @Test
    void duplicateTakedownIsIdempotent() {
        // 内容已处于下架状态(status=2)
        ContentEntity content = createContent(1001L, 100L, 1L, "已下架内容", 2, 0);
        when(contentMapper.selectById(1001L)).thenReturn(content);

        contentService.deleteContentById(1001L);

        // 不应再次拍摄快照
        verify(snapshotService, never()).takeSnapshot(anyLong(), anyString(), any());
        // 不应再次冻结评论
        verify(commentService, never()).freezeByContentId(anyLong());
        // 不应再次记录审核
        verify(moderationRecordService, never()).recordAction(anyLong(), anyString(), any(),
                anyString(), anyString(), anyString(), any(), any(), any());
        // 不应再次更新内容
        verify(contentMapper, never()).updateById(any());
    }

    /**
     * 恢复已正常的内容 → 跳过，不重复回补点赞数
     */
    @Test
    void duplicateRestoreIsIdempotent() {
        // 内容已处于正常状态(status=1)
        ContentEntity content = createContent(1001L, 100L, 1L, "正常内容", 1, 0);
        content.setLoveCount(10L);
        when(contentMapper.selectById(1001L)).thenReturn(content);

        contentService.restoreContent(1001L, "管理员恢复", "ADMIN_RESTORE");

        // 不应查询快照
        verify(snapshotService, never()).getLatestSnapshot(anyLong());
        // 不应解冻评论
        verify(commentService, never()).unfreezeByContentId(anyLong());
        // 不应记录审核
        verify(moderationRecordService, never()).recordAction(anyLong(), anyString(), any(),
                anyString(), anyString(), anyString(), any(), any(), any());
    }

    /**
     * 下架 → 恢复 → 再次恢复：第二次恢复幂等跳过
     */
    @Test
    void restoreAfterRestoreIsNoOp() {
        // 第一次：内容处于下架状态(2)，应正常恢复
        ContentEntity content = createContent(1001L, 100L, 1L, "下架内容", 2, 0);
        content.setLoveCount(0L);
        when(contentMapper.selectById(1001L)).thenReturn(content);
        when(contentMapper.updateById(any())).thenReturn(1);

        InteractionSnapshotEntity snapshot = new InteractionSnapshotEntity();
        snapshot.setLoveCount(15L);
        snapshot.setCommentCount(8L);
        when(snapshotService.getLatestSnapshot(1001L)).thenReturn(snapshot);
        when(fileService.list(any())).thenReturn(Collections.emptyList());

        contentService.restoreContent(1001L, "申诉恢复", "APPEAL");

        // 验证第一次恢复生效
        assertEquals(1, content.getStatus());
        assertEquals(15L, content.getLoveCount());
        verify(commentService).unfreezeByContentId(1001L);

        // 第二次：内容已恢复正常(1)，应跳过
        reset(snapshotService, commentService, moderationRecordService, fileService);
        when(contentMapper.selectById(1001L)).thenReturn(content);

        contentService.restoreContent(1001L, "重复恢复", "APPEAL");

        // 验证第二次恢复没有执行任何操作
        verify(snapshotService, never()).getLatestSnapshot(anyLong());
        verify(commentService, never()).unfreezeByContentId(anyLong());
    }
}
