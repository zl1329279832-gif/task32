package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.*;
import com.oddfar.campus.business.mapper.AppealMapper;
import com.oddfar.campus.business.mapper.ContentMapper;
import com.oddfar.campus.business.service.*;
import com.oddfar.campus.business.service.impl.AppealServiceImpl;
import com.oddfar.campus.common.exception.ServiceException;
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
 * 申诉全流程级联测试
 * 覆盖：申诉通过时内容、评论、附件、点赞统计、信用分、审核记录的完整恢复链
 */
@ExtendWith(MockitoExtension.class)
public class AppealFullCascadeTest extends BaseTest {

    @InjectMocks
    private AppealServiceImpl appealService;

    @Mock
    private AppealMapper appealMapper;
    @Mock
    private ContentMapper contentMapper;
    @Mock
    private ContentService contentService;
    @Mock
    private ModerationRecordService moderationRecordService;
    @Mock
    private UserCreditService userCreditService;
    @Mock
    private CreditCompensationService creditCompensationService;
    @Mock
    private GovernanceCacheService governanceCacheService;
    @Mock
    private InteractionSnapshotService snapshotService;

    @BeforeEach
    void setUp() {
        ModerationRecordEntity mockRecord = new ModerationRecordEntity();
        mockRecord.setRecordId(9999L);
        lenient().when(moderationRecordService.recordAction(anyLong(), anyString(), any(),
                anyString(), anyString(), anyString(), any(), any(), any())).thenReturn(mockRecord);
    }

    /**
     * 申诉通过 → 触发完整级联恢复：
     * 1. contentService.restoreContent() 被调用（负责恢复内容、解冻评论、清除附件违规、恢复点赞数）
     * 2. userCreditService.changeCredit() 被调用（信用分回补）
     * 3. moderationRecordService.recordAction() 被调用（信用分回补审计）
     */
    @Test
    void approvedAppealTriggersFullCascade() {
        // 准备申诉数据
        AppealEntity appeal = createAppeal(5001L, 1001L, 100L, 0);
        when(appealMapper.selectById(5001L)).thenReturn(appeal);
        // updateById 成功
        when(appealMapper.updateById(any())).thenReturn(1);
        // mock快照用于计算补偿分
        InteractionSnapshotEntity snapshot = new InteractionSnapshotEntity();
        snapshot.setLoveCount(10L);
        snapshot.setCommentCount(5L);
        lenient().when(snapshotService.getLatestSnapshot(1001L)).thenReturn(snapshot);

        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> secMock =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            secMock.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(1L);

            int result = appealService.reviewAppeal(5001L, 1, "内容无违规，恢复发布");

            assertEquals(1, result);

            // 验证 contentService.restoreContent 被调用（统一级联恢复）
            verify(contentService).restoreContent(eq(1001L),
                    contains("申诉通过"), eq("APPEAL"));

            // 验证信用分补偿明细被创建（替代直接调用changeCredit）
            verify(creditCompensationService).compensate(
                    eq(100L), eq(5001L), eq(1001L),
                    eq(5), anyInt(), anyString());

            // 验证信用分回补的审核日志被记录
            verify(moderationRecordService).recordAction(
                    eq(1001L), eq("CREDIT"), eq(100L),
                    eq("APPEAL"), eq("CREDIT_RESTORE"),
                    anyString(), isNull(), isNull(), isNull());

            // 验证缓存被清除
            verify(governanceCacheService).evictContentCaches(1001L);
            verify(governanceCacheService).evictUserCaches(100L);
        }
    }

    /**
     * 申诉拒绝 → 不触发任何恢复操作
     */
    @Test
    void rejectedAppealSkipsRestore() {
        AppealEntity appeal = createAppeal(5002L, 1001L, 100L, 0);
        when(appealMapper.selectById(5002L)).thenReturn(appeal);
        when(appealMapper.updateById(any())).thenReturn(1);

        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> secMock =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            secMock.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(1L);

            int result = appealService.reviewAppeal(5002L, 2, "申诉理由不成立");

            assertEquals(1, result);

            // 不应该调用恢复
            verify(contentService, never()).restoreContent(anyLong(), anyString(), anyString());
            // 不应该调用信用分补偿
            verify(creditCompensationService, never()).compensate(anyLong(), anyLong(), anyLong(), anyInt(), anyInt(), anyString());
            // 不应该清除缓存
            verify(governanceCacheService, never()).evictContentCaches(anyLong());
        }
    }

    /**
     * 申诉通过 - 已处理的申诉重复审核 → 前置检查拒绝
     */
    @Test
    void duplicateApprovalBlockedByStatusCheck() {
        // 申诉已经被处理过（状态=1）
        AppealEntity appeal = createAppeal(5001L, 1001L, 100L, 1);
        when(appealMapper.selectById(5001L)).thenReturn(appeal);

        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> secMock =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            secMock.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(1L);

            assertThrows(ServiceException.class,
                    () -> appealService.reviewAppeal(5001L, 1, "通过"));

            // 恢复操作不应被调用
            verify(contentService, never()).restoreContent(anyLong(), anyString(), anyString());
            verify(creditCompensationService, never()).compensate(anyLong(), anyLong(), anyLong(), anyInt(), anyInt(), anyString());
        }
    }

    /**
     * 已处理的申诉再次审核 → 前置检查拒绝
     */
    @Test
    void alreadyProcessedAppealRejectsReview() {
        // 申诉状态=1（已通过），再次审核
        AppealEntity appeal = createAppeal(5001L, 1001L, 100L, 1);
        when(appealMapper.selectById(5001L)).thenReturn(appeal);

        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> secMock =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            secMock.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(1L);

            assertThrows(ServiceException.class,
                    () -> appealService.reviewAppeal(5001L, 1, "重复审核"));
        }
    }
}
