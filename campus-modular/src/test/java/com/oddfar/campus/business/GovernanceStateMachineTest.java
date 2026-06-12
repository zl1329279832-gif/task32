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

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 治理状态机完整流程测试
 * 模拟真实场景：内容发布 → 自动审核 → 管理员下架 → 用户申诉 → 申诉通过 →
 * 管理员二次下架 → 再次申诉的完整链路
 */
@ExtendWith(MockitoExtension.class)
public class GovernanceStateMachineTest extends BaseTest {

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
    private GovernanceBatchService governanceBatchService;
    @Mock
    private GovernanceCacheService governanceCacheService;
    @Mock
    private ContentLoveService contentLoveService;

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
     * 场景1：发布 → 下架 → 申诉恢复 → 验证完整状态链
     *
     * 状态流转：status=1(正常) → status=2(下架) → status=1(恢复)
     * 评论状态：frozenStatus=0 → frozenStatus=1 → frozenStatus=0
     * 点赞统计：loveCount=15 → 快照保存 → loveCount=15(恢复)
     */
    @Test
    void fullCycle_publish_takedown_appealRestore() {
        Long contentId = 1001L;

        // === Phase 1: 正常内容存在 ===
        ContentEntity content = createContent(contentId, 100L, 1L, "二手物品转让", 1, 0);
        content.setLoveCount(15L);

        // === Phase 2: 管理员下架 ===
        when(contentMapper.selectById(contentId)).thenReturn(content);

        InteractionSnapshotEntity snapshot = new InteractionSnapshotEntity();
        snapshot.setLoveCount(15L);
        snapshot.setCommentCount(5L);
        when(snapshotService.takeSnapshot(eq(contentId), eq("TAKEDOWN"), isNull())).thenReturn(snapshot);

        try (MockedStatic<SecurityUtils> secMock = mockStatic(SecurityUtils.class)) {
            secMock.when(SecurityUtils::getUserId).thenReturn(1L);
            when(governanceBatchService.createBatch(anyString(), anyLong(), anyString(), anyInt()))
                    .thenReturn(createBatch(5001L, "TAKEDOWN", 1L, 1));
            contentService.deleteContentById(contentId);
        }

        // 验证下架后状态
        assertEquals(2, content.getStatus());
        verify(snapshotService).takeSnapshot(contentId, "TAKEDOWN", null);
        verify(commentService).freezeByContentId(contentId);

        // === Phase 3: 申诉通过恢复 ===
        reset(commentService, moderationRecordService, snapshotService);
        lenient().when(moderationRecordService.recordAction(anyLong(), anyString(), any(),
                anyString(), anyString(), anyString(), any(), any(), any())).thenReturn(new ModerationRecordEntity());
        lenient().when(moderationRecordService.updateById(any())).thenReturn(true);
        when(contentMapper.selectById(contentId)).thenReturn(content);
        when(snapshotService.getLatestSnapshot(contentId)).thenReturn(snapshot);

        contentService.restoreContent(contentId, "申诉通过", "APPEAL");

        // 验证恢复后状态
        assertEquals(1, content.getStatus());
        assertEquals(15L, content.getLoveCount());
        verify(commentService).unfreezeByContentIdWithReadOnly(contentId, 60);
    }

    /**
     * 场景2：管理员二次下架 → 再次申诉
     * 第一次恢复后，管理员再次下架，用户再次申诉
     * 验证状态机不会出现异常或数据不一致
     */
    @Test
    void doubleTakedown_doubleAppeal() {
        Long contentId = 1002L;

        // === 第一次下架 ===
        ContentEntity content = createContent(contentId, 100L, 1L, "争议内容", 1, 0);
        content.setLoveCount(10L);
        when(contentMapper.selectById(contentId)).thenReturn(content);

        InteractionSnapshotEntity snapshot1 = new InteractionSnapshotEntity();
        snapshot1.setLoveCount(10L);
        snapshot1.setCommentCount(3L);
        when(snapshotService.takeSnapshot(eq(contentId), eq("TAKEDOWN"), isNull())).thenReturn(snapshot1);

        try (MockedStatic<SecurityUtils> secMock = mockStatic(SecurityUtils.class)) {
            secMock.when(SecurityUtils::getUserId).thenReturn(1L);
            when(governanceBatchService.createBatch(anyString(), anyLong(), anyString(), anyInt()))
                    .thenReturn(createBatch(5001L, "TAKEDOWN", 1L, 1));
            contentService.deleteContentById(contentId);
        }
        assertEquals(2, content.getStatus());

        // === 第一次恢复 ===
        reset(commentService, moderationRecordService, snapshotService);
        lenient().when(moderationRecordService.recordAction(anyLong(), anyString(), any(),
                anyString(), anyString(), anyString(), any(), any(), any())).thenReturn(new ModerationRecordEntity());
        lenient().when(moderationRecordService.updateById(any())).thenReturn(true);
        when(contentMapper.selectById(contentId)).thenReturn(content);
        when(snapshotService.getLatestSnapshot(contentId)).thenReturn(snapshot1);

        contentService.restoreContent(contentId, "首次申诉通过", "APPEAL");
        assertEquals(1, content.getStatus());

        // === 第二次下架（管理员再次下架）===
        reset(commentService, moderationRecordService, snapshotService);
        lenient().when(moderationRecordService.recordAction(anyLong(), anyString(), any(),
                anyString(), anyString(), anyString(), any(), any(), any())).thenReturn(new ModerationRecordEntity());
        lenient().when(moderationRecordService.updateById(any())).thenReturn(true);
        when(contentMapper.selectById(contentId)).thenReturn(content);

        InteractionSnapshotEntity snapshot2 = new InteractionSnapshotEntity();
        snapshot2.setLoveCount(10L);
        snapshot2.setCommentCount(7L); // 恢复期间可能有新评论
        when(snapshotService.takeSnapshot(eq(contentId), eq("TAKEDOWN"), isNull())).thenReturn(snapshot2);

        try (MockedStatic<SecurityUtils> secMock = mockStatic(SecurityUtils.class)) {
            secMock.when(SecurityUtils::getUserId).thenReturn(1L);
            when(governanceBatchService.createBatch(anyString(), anyLong(), anyString(), anyInt()))
                    .thenReturn(createBatch(5002L, "TAKEDOWN", 1L, 1));
            contentService.deleteContentById(contentId);
        }
        assertEquals(2, content.getStatus());

        // === 第二次恢复 ===
        reset(commentService, moderationRecordService, snapshotService);
        lenient().when(moderationRecordService.recordAction(anyLong(), anyString(), any(),
                anyString(), anyString(), anyString(), any(), any(), any())).thenReturn(new ModerationRecordEntity());
        lenient().when(moderationRecordService.updateById(any())).thenReturn(true);
        when(contentMapper.selectById(contentId)).thenReturn(content);
        when(snapshotService.getLatestSnapshot(contentId)).thenReturn(snapshot2);

        contentService.restoreContent(contentId, "二次申诉通过", "APPEAL");
        assertEquals(1, content.getStatus());
        assertEquals(10L, content.getLoveCount());
    }

    /**
     * 场景3：下架 → 重复下架 → 幂等跳过
     * 管理员可能误操作多次点击下架按钮
     */
    @Test
    void duplicateTakedownIsSkipped() {
        Long contentId = 1003L;

        // 第一次下架
        ContentEntity content = createContent(contentId, 100L, 1L, "测试内容", 1, 0);
        when(contentMapper.selectById(contentId)).thenReturn(content);
        when(snapshotService.takeSnapshot(eq(contentId), anyString(), any())).thenReturn(new InteractionSnapshotEntity());

        try (MockedStatic<SecurityUtils> secMock = mockStatic(SecurityUtils.class)) {
            secMock.when(SecurityUtils::getUserId).thenReturn(1L);
            when(governanceBatchService.createBatch(anyString(), anyLong(), anyString(), anyInt()))
                    .thenReturn(createBatch(5003L, "TAKEDOWN", 1L, 1));
            contentService.deleteContentById(contentId);
        }
        assertEquals(2, content.getStatus());

        // 第二次下架（内容已经是status=2）
        reset(snapshotService, commentService, moderationRecordService);
        when(contentMapper.selectById(contentId)).thenReturn(content);

        contentService.deleteContentById(contentId);

        // 幂等：不应重复执行
        verify(snapshotService, never()).takeSnapshot(anyLong(), anyString(), any());
        verify(commentService, never()).freezeByContentId(anyLong());
        verify(moderationRecordService, never()).recordAction(anyLong(), anyString(), any(),
                anyString(), anyString(), anyString(), any(), any(), any());
    }

    /**
     * 场景4：恢复 → 重复恢复 → 幂等跳过
     * 申诉通过后管理员又点击恢复按钮
     */
    @Test
    void duplicateRestoreIsSkipped() {
        Long contentId = 1004L;

        // 下架后恢复
        ContentEntity content = createContent(contentId, 100L, 1L, "恢复测试", 2, 0);
        content.setLoveCount(0L);
        when(contentMapper.selectById(contentId)).thenReturn(content);
        InteractionSnapshotEntity snapshot = new InteractionSnapshotEntity();
        snapshot.setLoveCount(5L);
        when(snapshotService.getLatestSnapshot(contentId)).thenReturn(snapshot);

        contentService.restoreContent(contentId, "恢复", "ADMIN_RESTORE");
        assertEquals(1, content.getStatus());

        // 重复恢复
        reset(snapshotService, commentService, moderationRecordService);
        when(contentMapper.selectById(contentId)).thenReturn(content);

        contentService.restoreContent(contentId, "重复恢复", "ADMIN_RESTORE");

        verify(snapshotService, never()).getLatestSnapshot(anyLong());
        verify(commentService, never()).unfreezeByContentIdWithReadOnly(anyLong(), anyInt());
    }

    /**
     * 场景5：带附件违规的完整链路
     * 内容因附件违规被下架 → 申诉通过 → 附件违规被连带清除
     */
    @Test
    void takedownWithAttachmentViolation_fullRestore() {
        Long contentId = 1005L;

        // 下架含违规附件的内容
        ContentEntity content = createContent(contentId, 100L, 1L, "含违规图片的内容", 1, 1);
        content.setLoveCount(8L);
        when(contentMapper.selectById(contentId)).thenReturn(content);
        when(snapshotService.takeSnapshot(eq(contentId), anyString(), any()))
                .thenReturn(new InteractionSnapshotEntity());

        try (MockedStatic<SecurityUtils> secMock = mockStatic(SecurityUtils.class)) {
            secMock.when(SecurityUtils::getUserId).thenReturn(1L);
            when(governanceBatchService.createBatch(anyString(), anyLong(), anyString(), anyInt()))
                    .thenReturn(createBatch(5005L, "TAKEDOWN", 1L, 1));
            contentService.deleteContentById(contentId);
        }
        assertEquals(2, content.getStatus());

        // 申诉通过恢复
        reset(commentService, moderationRecordService, snapshotService);
        lenient().when(moderationRecordService.recordAction(anyLong(), anyString(), any(),
                anyString(), anyString(), anyString(), any(), any(), any())).thenReturn(new ModerationRecordEntity());
        lenient().when(moderationRecordService.updateById(any())).thenReturn(true);
        when(contentMapper.selectById(contentId)).thenReturn(content);

        InteractionSnapshotEntity snapshot = new InteractionSnapshotEntity();
        snapshot.setLoveCount(8L);
        when(snapshotService.getLatestSnapshot(contentId)).thenReturn(snapshot);

        // 模拟1个违规附件
        CampusFileEntity violatedFile = new CampusFileEntity();
        violatedFile.setFileId(4001L);
        violatedFile.setContentId(contentId);
        violatedFile.setViolationStatus(1);
        violatedFile.setViolationReason("图片含敏感内容");

        when(fileService.list(any()))
                .thenReturn(Arrays.asList(violatedFile));

        contentService.restoreContent(contentId, "申诉通过", "APPEAL");

        // 验证完整恢复
        assertEquals(1, content.getStatus());
        assertEquals(8L, content.getLoveCount());
        verify(commentService).unfreezeByContentIdWithReadOnly(contentId, 60);
        verify(fileService).clearViolation(4001L);
    }

    /**
     * 场景6：pending(0)状态的内容不允许恢复
     */
    @Test
    void cannotRestorePendingContent() {
        ContentEntity content = createContent(1006L, 100L, 1L, "待审核内容", 0, 0);
        when(contentMapper.selectById(1006L)).thenReturn(content);

        assertThrows(com.oddfar.campus.common.exception.ServiceException.class,
                () -> contentService.restoreContent(1006L, "不应允许", "ADMIN_RESTORE"));
    }
}
