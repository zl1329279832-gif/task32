package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.*;
import com.oddfar.campus.business.mapper.CommentMapper;
import com.oddfar.campus.business.mapper.ContentMapper;
import com.oddfar.campus.business.service.AutoModerationService;
import com.oddfar.campus.business.service.impl.CommentServiceImpl;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 评论冻结/只读恢复测试
 */
@ExtendWith(MockitoExtension.class)
public class CommentFreezeRestoreTest extends BaseTest {

    @InjectMocks
    private CommentServiceImpl commentService;

    @Mock private CommentMapper commentMapper;
    @Mock private ContentMapper contentMapper;
    @Mock private AutoModerationService autoModerationService;

    /**
     * 解冻评论时设置只读截止时间
     */
    @Test
    void unfreezeSetsReadOnlyUntil() {
        Long contentId = 6001L;
        when(commentMapper.update(any(CommentEntity.class), any(LambdaQueryWrapperX.class)))
                .thenReturn(5);

        int result = commentService.unfreezeByContentIdWithReadOnly(contentId, 60);

        assertEquals(5, result);
        verify(commentMapper).update(argThat(entity ->
                entity.getFrozenStatus() == 0 && entity.getReadOnlyUntil() != null),
                any(LambdaQueryWrapperX.class));
    }

    /**
     * 不同只读时长产生不同的截止时间
     */
    @Test
    void readOnlyPeriodConfigurable() {
        Long contentId = 6002L;
        when(commentMapper.update(any(CommentEntity.class), any(LambdaQueryWrapperX.class)))
                .thenReturn(3);

        commentService.unfreezeByContentIdWithReadOnly(contentId, 120);

        verify(commentMapper).update(argThat(entity ->
                entity.getReadOnlyUntil() != null),
                any(LambdaQueryWrapperX.class));
    }

    /**
     * 普通解冻（不带只读期）仍然正常工作
     */
    @Test
    void normalUnfreezeStillWorks() {
        Long contentId = 6003L;
        when(commentMapper.update(any(CommentEntity.class), any(LambdaQueryWrapperX.class)))
                .thenReturn(2);

        int result = commentService.unfreezeByContentId(contentId);

        assertEquals(2, result);
    }

    /**
     * 冻结评论正常工作
     */
    @Test
    void freezeWorks() {
        Long contentId = 6004L;
        when(commentMapper.update(any(CommentEntity.class), any(LambdaQueryWrapperX.class)))
                .thenReturn(10);

        int result = commentService.freezeByContentId(contentId);

        assertEquals(10, result);
        verify(commentMapper).update(argThat(entity ->
                entity.getFrozenStatus() == 1),
                any(LambdaQueryWrapperX.class));
    }
}
