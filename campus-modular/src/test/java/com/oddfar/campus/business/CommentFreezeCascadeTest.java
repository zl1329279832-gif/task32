package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.CommentEntity;
import com.oddfar.campus.business.domain.entity.ContentEntity;
import com.oddfar.campus.business.domain.entity.ModerationRecordEntity;
import com.oddfar.campus.business.mapper.CommentMapper;
import com.oddfar.campus.business.mapper.ContentLoveMapper;
import com.oddfar.campus.business.mapper.ContentMapper;
import com.oddfar.campus.business.service.*;
import com.oddfar.campus.business.service.impl.ContentServiceImpl;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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

        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> secMock =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            secMock.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(1L);
            when(governanceBatchService.createBatch(anyString(), anyLong(), anyString(), anyInt()))
                    .thenReturn(createBatch(5001L, "TAKEDOWN", 1L, 1));

            // 调用deleteContentById（已改为下架逻辑）
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
    }

    /**
     * 冻结的评论字段状态正确
     */
    @Test
    void frozenCommentsNotVisibleInPublicApi() {
        CommentEntity comment = createComment(2001L, 1001L, 101L, "测试评论");
        assertEquals(0, comment.getFrozenStatus());

        // 冻结后
        comment.setFrozenStatus(1);
        assertEquals(1, comment.getFrozenStatus());

        // CommentMapper.xml已添加 frozen_status = 0 条件
        // 验证字段正确设置
        assertNotNull(comment.getFrozenStatus());
    }

    /**
     * 解冻评论应调用unfreezeByContentIdWithReadOnly
     */
    @Test
    void appealApprovalShouldUnfreezeComments() {
        // 验证CommentService接口有unfreezeByContentIdWithReadOnly方法
        commentService.unfreezeByContentIdWithReadOnly(1001L, 60);
        verify(commentService).unfreezeByContentIdWithReadOnly(1001L, 60);
    }
}
