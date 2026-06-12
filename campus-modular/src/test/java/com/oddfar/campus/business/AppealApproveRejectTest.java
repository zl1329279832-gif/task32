package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.*;
import com.oddfar.campus.business.mapper.AppealMapper;
import com.oddfar.campus.business.mapper.ContentMapper;
import com.oddfar.campus.business.service.*;
import com.oddfar.campus.business.service.impl.AppealServiceImpl;
import com.oddfar.campus.common.utils.SecurityUtils;
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
 * 申诉通过/拒绝测试（含部分附件维持违规、补偿明细、版本追踪）
 */
@ExtendWith(MockitoExtension.class)
public class AppealApproveRejectTest extends BaseTest {

    @InjectMocks
    private AppealServiceImpl appealService;

    @Mock private AppealMapper appealMapper;
    @Mock private ContentMapper contentMapper;
    @Mock private ContentService contentService;
    @Mock private ModerationRecordService moderationRecordService;
    @Mock private UserCreditService userCreditService;
    @Mock private CreditCompensationService creditCompensationService;
    @Mock private GovernanceCacheService governanceCacheService;
    @Mock private InteractionSnapshotService snapshotService;

    @BeforeEach
    void setUp() {
        lenient().when(appealMapper.updateById(any())).thenReturn(1);
        lenient().when(moderationRecordService.recordAction(anyLong(), anyString(), any(),
                anyString(), anyString(), anyString(), any(), any(), any()))
                .thenReturn(new ModerationRecordEntity());
    }

    /**
     * 申诉通过 → 创建补偿明细 + 缓存清除
     */
    @Test
    void approveCreatesCreditCompensation() {
        AppealEntity appeal = createAppeal(3001L, 1001L, 100L, 0);
        appeal.setProcessingVersion(1);
        when(appealMapper.selectById(3001L)).thenReturn(appeal);

        InteractionSnapshotEntity snapshot = createSnapshot(8001L, 1001L, 20L, 10L);
        when(snapshotService.getLatestSnapshot(1001L)).thenReturn(snapshot);

        CreditCompensationEntity comp = createCompensation(9001L, 100L, 3001L, 1001L, 5, 0);
        when(creditCompensationService.compensate(eq(100L), eq(3001L), eq(1001L),
                eq(5), eq(0), anyString())).thenReturn(comp);

        try (MockedStatic<SecurityUtils> secMock = mockStatic(SecurityUtils.class)) {
            secMock.when(SecurityUtils::getUserId).thenReturn(1L);

            appealService.reviewAppeal(3001L, 1, "同意恢复");

            // 验证补偿被创建
            verify(creditCompensationService).compensate(eq(100L), eq(3001L), eq(1001L),
                    eq(5), eq(0), anyString());
            // 验证缓存被清除
            verify(governanceCacheService).evictContentCaches(1001L);
            verify(governanceCacheService).evictUserCaches(100L);
        }
    }

    /**
     * 高影响力内容获得额外补偿
     */
    @Test
    void highImpactContentGetsBonusCompensation() {
        AppealEntity appeal = createAppeal(3002L, 1002L, 100L, 0);
        appeal.setProcessingVersion(1);
        when(appealMapper.selectById(3002L)).thenReturn(appeal);

        // loveCount=80, commentCount=30 → totalImpact = 80 + 60 = 140 >= 100 → bonus=10
        InteractionSnapshotEntity snapshot = createSnapshot(8002L, 1002L, 80L, 30L);
        when(snapshotService.getLatestSnapshot(1002L)).thenReturn(snapshot);

        try (MockedStatic<SecurityUtils> secMock = mockStatic(SecurityUtils.class)) {
            secMock.when(SecurityUtils::getUserId).thenReturn(1L);

            appealService.reviewAppeal(3002L, 1, "高影响力内容");

            verify(creditCompensationService).compensate(eq(100L), eq(3002L), eq(1002L),
                    eq(5), eq(10), anyString());
        }
    }

    /**
     * 拒绝不创建补偿
     */
    @Test
    void rejectDoesNotCreateCompensation() {
        AppealEntity appeal = createAppeal(3003L, 1003L, 100L, 0);
        appeal.setProcessingVersion(1);
        when(appealMapper.selectById(3003L)).thenReturn(appeal);

        try (MockedStatic<SecurityUtils> secMock = mockStatic(SecurityUtils.class)) {
            secMock.when(SecurityUtils::getUserId).thenReturn(1L);

            appealService.reviewAppeal(3003L, 2, "理由不充分");

            verify(creditCompensationService, never()).compensate(anyLong(), anyLong(), anyLong(),
                    anyInt(), anyInt(), anyString());
            verify(contentService, never()).restoreContent(anyLong(), anyString(), anyString());
        }
    }

    /**
     * 重复审核幂等：已处理的申诉不能再处理
     */
    @Test
    void alreadyProcessedAppealThrowsException() {
        AppealEntity appeal = createAppeal(3004L, 1004L, 100L, 1); // 已通过
        when(appealMapper.selectById(3004L)).thenReturn(appeal);

        assertThrows(Exception.class, () -> appealService.reviewAppeal(3004L, 1, "重复"));
    }
}
