package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.AppealEntity;
import com.oddfar.campus.business.domain.entity.ContentEntity;
import com.oddfar.campus.business.domain.entity.InteractionSnapshotEntity;
import com.oddfar.campus.business.domain.entity.ModerationRecordEntity;
import com.oddfar.campus.business.enums.CampusBizCodeEnum;
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
     * 提交申诉 - 正常情况
     */
    @Test
    void submitAppealForRejectedContent() {
        ContentEntity content = createContent(1002L, 100L, 1L, "被拒绝的内容", 3, 0);
        when(contentMapper.selectById(1002L)).thenReturn(content);
        when(appealMapper.selectMaxVersionByContentId(1002L)).thenReturn(null);
        when(appealMapper.selectPendingOrApprovedCountByVersion(1002L, 1)).thenReturn(0L);
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
     * 申诉通过 - 通过 contentService.restoreContent 统一恢复
     */
    @Test
    void approvedAppealRestoresContent() {
        AppealEntity appeal = createAppeal(5001L, 1002L, 100L, 0);
        when(appealMapper.selectById(5001L)).thenReturn(appeal);
        when(appealMapper.updateById(any())).thenReturn(1);
        // mock快照用于计算补偿分
        InteractionSnapshotEntity snapshot = new InteractionSnapshotEntity();
        snapshot.setLoveCount(10L);
        snapshot.setCommentCount(5L);
        lenient().when(snapshotService.getLatestSnapshot(1002L)).thenReturn(snapshot);

        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> secMock =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            secMock.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(1L);

            int result = appealService.reviewAppeal(5001L, 1, "申诉通过");

            assertEquals(1, result);
            // 验证通过 contentService.restoreContent 统一恢复
            verify(contentService).restoreContent(eq(1002L),
                    contains("申诉通过"), eq("APPEAL"));
            // 验证信用分补偿明细被创建
            verify(creditCompensationService).compensate(
                    eq(100L), eq(5001L), eq(1002L),
                    eq(5), anyInt(), anyString());
            // 验证审核记录
            verify(moderationRecordService).recordAction(eq(1002L), eq("CREDIT"), eq(100L),
                    eq("APPEAL"), eq("CREDIT_RESTORE"), anyString(), isNull(), isNull(), isNull());
        }
    }

    /**
     * 申诉拒绝 - 不改变内容状态
     */
    @Test
    void rejectedAppealLeavesUnchanged() {
        AppealEntity appeal = createAppeal(5002L, 1002L, 100L, 0);
        when(appealMapper.selectById(5002L)).thenReturn(appeal);
        when(appealMapper.updateById(any())).thenReturn(1);

        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> secMock =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            secMock.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(1L);

            int result = appealService.reviewAppeal(5002L, 2, "申诉理由不成立");

            assertEquals(1, result);
            // 不应该调用内容恢复
            verify(contentService, never()).restoreContent(anyLong(), anyString(), anyString());
            // 不应该调用信用分补偿
            verify(creditCompensationService, never()).compensate(anyLong(), anyLong(), anyLong(), anyInt(), anyInt(), anyString());
        }
    }

    /**
     * 重复提交申诉 - 应抛出异常
     */
    @Test
    void cannotSubmitDuplicateAppeal() {
        ContentEntity content = createContent(1002L, 100L, 1L, "被拒绝的内容", 3, 0);
        when(contentMapper.selectById(1002L)).thenReturn(content);
        when(appealMapper.selectMaxVersionByContentId(1002L)).thenReturn(null);
        when(appealMapper.selectPendingOrApprovedCountByVersion(1002L, 1)).thenReturn(1L);

        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> secMock =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            secMock.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(100L);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> appealService.submitAppeal(1002L, "重复申诉"));
            assertEquals(CampusBizCodeEnum.APPEAL_ALREADY_EXISTS.getCode(), ex.getCode());
        }
    }
}
