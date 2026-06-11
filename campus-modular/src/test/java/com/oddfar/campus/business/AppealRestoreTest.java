package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.AppealEntity;
import com.oddfar.campus.business.domain.entity.ContentEntity;
import com.oddfar.campus.business.domain.entity.InteractionSnapshotEntity;
import com.oddfar.campus.business.domain.entity.ModerationRecordEntity;
import com.oddfar.campus.business.enums.CampusBizCodeEnum;
import com.oddfar.campus.business.mapper.AppealMapper;
import com.oddfar.campus.business.mapper.CommentMapper;
import com.oddfar.campus.business.mapper.ContentMapper;
import com.oddfar.campus.business.service.*;
import com.oddfar.campus.business.service.impl.AppealServiceImpl;
import com.oddfar.campus.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 申诉恢复测试
 */
@ExtendWith(MockitoExtension.class)
public class AppealRestoreTest extends BaseTest {

    @InjectMocks
    private AppealServiceImpl appealService;

    @Mock
    private AppealMapper appealMapper;
    @Mock
    private ContentMapper contentMapper;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private ModerationRecordService moderationRecordService;
    @Mock
    private InteractionSnapshotService interactionSnapshotService;
    @Mock
    private UserCreditService userCreditService;
    @Mock
    private CampusFileService campusFileService;
    @Mock
    private CommentService commentService;

    /**
     * 提交申诉 - 正常情况
     */
    @Test
    void submitAppealForRejectedContent() {
        ContentEntity content = createContent(1002L, 100L, 1L, "被拒绝的内容", 3, 0);
        when(contentMapper.selectById(1002L)).thenReturn(content);
        when(appealMapper.selectPendingOrApprovedCount(1002L)).thenReturn(0L);
        when(appealMapper.insert(any(AppealEntity.class))).thenReturn(1);

        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> secMock =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            secMock.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(100L);

            int result = appealService.submitAppeal(1002L, "我认为内容没有违规");

            assertEquals(1, result);
            verify(appealMapper).insert(argThat(appeal ->
                    appeal.getContentId().equals(1002L) &&
                            appeal.getUserId().equals(100L) &&
                            appeal.getAppealStatus() == 0
            ));
        }
    }

    /**
     * 申诉通过 - 恢复内容状态、解冻评论、清除附件违规、标记快照已消费
     */
    @Test
    void approvedAppealRestoresContentAndClearsViolations() {
        AppealEntity appeal = createAppeal(5001L, 1002L, 100L, 0);
        when(appealMapper.selectById(5001L)).thenReturn(appeal);
        when(appealMapper.updateById(any(AppealEntity.class))).thenReturn(1);

        ContentEntity content = createContent(1002L, 100L, 1L, "被拒绝的内容", 3, 0);
        content.setLoveCount(0L);
        when(contentMapper.selectById(1002L)).thenReturn(content);

        InteractionSnapshotEntity snapshot = new InteractionSnapshotEntity();
        snapshot.setSnapshotId(8001L);
        snapshot.setLoveCount(10L);
        snapshot.setCommentCount(5L);
        snapshot.setConsumed(0);
        when(interactionSnapshotService.getLatestSnapshot(1002L)).thenReturn(snapshot);

        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> secMock =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            secMock.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(1L);

            int result = appealService.reviewAppeal(5001L, 1, "申诉通过");

            assertEquals(1, result);
            // 验证内容状态恢复为1（正常）
            assertEquals(1, content.getStatus());
            // 验证点赞数从快照恢复
            assertEquals(10L, content.getLoveCount());
            // 验证快照被标记为已消费
            verify(interactionSnapshotService).markConsumed(8001L);
            // 验证评论被解冻（通过CommentService而非直接CommentMapper）
            verify(commentService).unfreezeByContentId(1002L);
            // 验证附件违规标记被清除
            verify(campusFileService).clearViolationByContentId(1002L);
            // 验证信用分回补
            verify(userCreditService).changeCredit(eq(100L), eq(5), anyString(), eq("appeal"), eq(5001L));
            // 验证审核记录
            verify(moderationRecordService).recordAction(eq(1002L), eq("CONTENT"), isNull(),
                    eq("MANUAL"), eq("RESTORE"), anyString(), isNull(), eq(3), eq(1));
        }
    }

    /**
     * 申诉拒绝 - 记录审计但不改变内容状态
     */
    @Test
    void rejectedAppealRecordsAuditAndLeavesUnchanged() {
        AppealEntity appeal = createAppeal(5002L, 1002L, 100L, 0);
        when(appealMapper.selectById(5002L)).thenReturn(appeal);
        when(appealMapper.updateById(any(AppealEntity.class))).thenReturn(1);

        ContentEntity content = createContent(1002L, 100L, 1L, "被拒绝的内容", 3, 0);
        when(contentMapper.selectById(1002L)).thenReturn(content);

        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> secMock =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            secMock.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(1L);

            int result = appealService.reviewAppeal(5002L, 2, "申诉理由不成立");

            assertEquals(1, result);
            // 不应该更新内容
            verify(contentMapper, never()).updateById(any());
            // 不应该调用信用分回补
            verify(userCreditService, never()).changeCredit(anyLong(), anyInt(), anyString(), anyString(), anyLong());
            // 不应该解冻评论
            verify(commentService, never()).unfreezeByContentId(anyLong());
            // 不应该清除附件违规
            verify(campusFileService, never()).clearViolationByContentId(anyLong());
            // 但应该记录审核操作（审计痕迹）
            verify(moderationRecordService).recordAction(eq(1002L), eq("CONTENT"), isNull(),
                    eq("MANUAL"), eq("APPEAL_REJECT"), contains("申诉驳回"), isNull(), eq(3), eq(3));
        }
    }

    /**
     * 重复提交申诉 - 应抛出异常
     */
    @Test
    void cannotSubmitDuplicateAppeal() {
        ContentEntity content = createContent(1002L, 100L, 1L, "被拒绝的内容", 3, 0);
        when(contentMapper.selectById(1002L)).thenReturn(content);
        when(appealMapper.selectPendingOrApprovedCount(1002L)).thenReturn(1L);

        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> secMock =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            secMock.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(100L);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> appealService.submitAppeal(1002L, "重复申诉"));
            assertEquals(CampusBizCodeEnum.APPEAL_ALREADY_EXISTS.getCode(), ex.getCode());
        }
    }

    /**
     * 已处理的申诉不能重复审核（幂等守卫）
     */
    @Test
    void cannotReviewAlreadyProcessedAppeal() {
        AppealEntity appeal = createAppeal(5003L, 1002L, 100L, 1); // 已通过
        when(appealMapper.selectById(5003L)).thenReturn(appeal);

        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> secMock =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            secMock.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(1L);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> appealService.reviewAppeal(5003L, 1, "再次通过"));
            assertEquals("该申诉已处理", ex.getMessage());
            // 不应调用任何下游服务
            verify(contentMapper, never()).updateById(any());
            verify(moderationRecordService, never()).recordAction(
                    anyLong(), anyString(), any(), anyString(), anyString(),
                    anyString(), any(), any(), any());
        }
    }

    /**
     * 申诉通过但无快照时不回补点赞数
     */
    @Test
    void approvedAppealWithoutSnapshotSkipsLoveRestore() {
        AppealEntity appeal = createAppeal(5004L, 1003L, 100L, 0);
        when(appealMapper.selectById(5004L)).thenReturn(appeal);
        when(appealMapper.updateById(any(AppealEntity.class))).thenReturn(1);

        ContentEntity content = createContent(1003L, 100L, 1L, "内容", 2, 0);
        content.setLoveCount(3L);
        when(contentMapper.selectById(1003L)).thenReturn(content);
        when(interactionSnapshotService.getLatestSnapshot(1003L)).thenReturn(null);

        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> secMock =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            secMock.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(1L);

            appealService.reviewAppeal(5004L, 1, "通过");

            // 点赞数不变
            assertEquals(3L, content.getLoveCount());
            // 不标记快照消费
            verify(interactionSnapshotService, never()).markConsumed(anyLong());
            // 仍然解冻评论和清除违规
            verify(commentService).unfreezeByContentId(1003L);
            verify(campusFileService).clearViolationByContentId(1003L);
        }
    }
}
