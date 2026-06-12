package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.CommentEntity;
import com.oddfar.campus.business.domain.entity.ContentEntity;
import com.oddfar.campus.business.domain.entity.ModerationRecordEntity;
import com.oddfar.campus.business.enums.CampusBizCodeEnum;
import com.oddfar.campus.business.mapper.CommentMapper;
import com.oddfar.campus.business.mapper.ContentMapper;
import com.oddfar.campus.business.service.AutoModerationService;
import com.oddfar.campus.business.service.impl.CommentServiceImpl;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import com.oddfar.campus.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 评论只读恢复测试
 */
@ExtendWith(MockitoExtension.class)
public class CommentReadOnlyRestoreTest extends BaseTest {

    @InjectMocks
    private CommentServiceImpl commentService;

    @Mock
    private CommentMapper commentMapper;
    @Mock
    private ContentMapper contentMapper;
    @Mock
    private AutoModerationService autoModerationService;

    @Test
    void unfreezeToReadOnly_setsBothFields() {
        when(commentMapper.update(any(), any(LambdaQueryWrapperX.class))).thenReturn(3);

        int rows = commentService.unfreezeToReadOnly(1L);

        assertEquals(3, rows);
        verify(commentMapper).update(argThat(entity -> {
            CommentEntity comment = (CommentEntity) entity;
            return comment.getFrozenStatus() == 0 && comment.getReadOnlyStatus() == 1;
        }), any(LambdaQueryWrapperX.class));
    }

    @Test
    void updateComment_readOnlyThrowsException() {
        CommentEntity existing = createComment(100L, 1L, 10L, "原内容");
        existing.setReadOnlyStatus(1);
        when(commentMapper.selectById(100L)).thenReturn(existing);

        CommentEntity update = new CommentEntity();
        update.setCommentId(100L);
        update.setCoContent("修改内容");

        ServiceException ex = assertThrows(ServiceException.class,
                () -> commentService.updateComment(update));
        assertEquals(CampusBizCodeEnum.COMMENT_READ_ONLY.getCode(), ex.getCode());
    }

    @Test
    void updateComment_normalCommentAllowed() {
        CommentEntity existing = createComment(100L, 1L, 10L, "原内容");
        existing.setReadOnlyStatus(0);
        when(commentMapper.selectById(100L)).thenReturn(existing);
        when(commentMapper.updateById(any())).thenReturn(1);

        CommentEntity update = new CommentEntity();
        update.setCommentId(100L);
        update.setCoContent("修改内容");

        int rows = commentService.updateComment(update);
        assertEquals(1, rows);
    }

    @Test
    void insertComment_replyToReadOnlyBlocked() {
        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> secMock =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            secMock.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(10L);

            // 回复一个只读评论
            CommentEntity readOnlyComment = createComment(100L, 1L, 20L, "只读评论");
            readOnlyComment.setReadOnlyStatus(1);
            when(commentMapper.selectById(100L)).thenReturn(readOnlyComment);

            CommentEntity reply = new CommentEntity();
            reply.setCommentId(100L); // 回复目标
            reply.setCoContent("回复只读评论");

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> commentService.insertComment(reply));
            assertEquals(CampusBizCodeEnum.COMMENT_READ_ONLY.getCode(), ex.getCode());
        }
    }
}
