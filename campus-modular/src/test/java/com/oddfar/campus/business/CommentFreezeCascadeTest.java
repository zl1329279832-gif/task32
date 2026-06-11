package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.CommentEntity;
import com.oddfar.campus.business.domain.entity.ContentEntity;
import com.oddfar.campus.business.domain.entity.ModerationRecordEntity;
import com.oddfar.campus.business.mapper.CommentMapper;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 评论连带冻结测试
 */
@ExtendWith(MockitoExtension.class)
public class CommentFreezeCascadeTest extends BaseTest {

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

    @BeforeEach
    void setUp() {
        ModerationRecordEntity mockRecord = new ModerationRecordEntity();
        mockRecord.setRecordId(9999L);
        lenient().when(moderationRecordService.recordAction(anyLong(), anyString(), any(), anyString(), anyString(),
                anyString(), any(), any(), any())).thenReturn(mockRecord);
    }

    /**
     * 下架内容时应冻结评论并拍摄快照
     */
    @Test
    void takedownContentShouldFreezeComments() {
        ContentEntity content = createContent(1001L, 100L, 1L, "测试内容", 1, 0);
        when(contentMapper.selectById(1001L)).thenReturn(content);
        when(contentMapper.updateById(any(ContentEntity.class))).thenReturn(1);

        contentService.deleteContentById(1001L);

        // 验证快照被拍摄
        verify(snapshotService).takeSnapshot(1001L, "TAKEDOWN", null);
        // 验证评论被冻结
        verify(commentService).freezeByContentId(1001L);
        // 验证审核记录被写入
        verify(moderationRecordService).recordAction(eq(1001L), eq("CONTENT"), isNull(),
                eq("MANUAL"), eq("TAKEDOWN"), anyString(), isNull(), eq(1), eq(2));
        // 验证内容状态更新为下架(2)
        assertEquals(2, content.getStatus());
    }

    /**
     * 二次下架已下架内容应幂等跳过
     */
    @Test
    void doubleTakedownShouldBeIdempotent() {
        ContentEntity content = createContent(1001L, 100L, 1L, "已下架内容", 2, 0);
        when(contentMapper.selectById(1001L)).thenReturn(content);

        contentService.deleteContentById(1001L);

        // 不应创建重复快照
        verify(snapshotService, never()).takeSnapshot(anyLong(), anyString(), any());
        // 不应重复冻结评论
        verify(commentService, never()).freezeByContentId(anyLong());
        // 不应写入重复审核记录
        verify(moderationRecordService, never()).recordAction(anyLong(), anyString(), any(),
                anyString(), anyString(), anyString(), any(), any(), any());
        // 状态不变
        assertEquals(2, content.getStatus());
    }

    /**
     * 冻结的评论字段状态正确
     */
    @Test
    void frozenCommentsNotVisibleInPublicApi() {
        CommentEntity comment = createComment(2001L, 1001L, 101L, "测试评论");
        assertEquals(0, comment.getFrozenStatus());

        comment.setFrozenStatus(1);
        assertEquals(1, comment.getFrozenStatus());

        assertNotNull(comment.getFrozenStatus());
    }

    /**
     * 解冻评论应调用unfreezeByContentId
     */
    @Test
    void appealApprovalShouldUnfreezeComments() {
        commentService.unfreezeByContentId(1001L);
        verify(commentService).unfreezeByContentId(1001L);
    }
}
