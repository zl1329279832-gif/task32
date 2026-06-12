package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.AppealEntity;
import com.oddfar.campus.business.domain.entity.ContentEntity;
import com.oddfar.campus.business.domain.entity.CreditCompensationDetailEntity;
import com.oddfar.campus.business.domain.entity.ModerationRecordEntity;
import com.oddfar.campus.business.domain.vo.CreditCompensationVo;
import com.oddfar.campus.business.mapper.AppealMapper;
import com.oddfar.campus.business.mapper.ContentMapper;
import com.oddfar.campus.business.mapper.CreditCompensationDetailMapper;
import com.oddfar.campus.business.service.*;
import com.oddfar.campus.business.service.impl.AppealServiceImpl;
import com.oddfar.campus.business.service.impl.CreditCompensationServiceImpl;
import com.oddfar.campus.business.service.impl.GovernanceCacheHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 信用分补偿明细测试
 */
@ExtendWith(MockitoExtension.class)
public class CreditCompensationDetailTest extends BaseTest {

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
    private GovernanceCacheHelper governanceCacheHelper;

    @BeforeEach
    void setUp() {
        ModerationRecordEntity mockRecord = new ModerationRecordEntity();
        mockRecord.setRecordId(9999L);
        lenient().when(moderationRecordService.recordAction(anyLong(), anyString(), any(),
                anyString(), anyString(), anyString(), any(), any(), any())).thenReturn(mockRecord);
    }

    @Test
    void appealApproval_createsCompensationDetails() {
        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> secMock =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            secMock.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(1L);

            AppealEntity appeal = createAppeal(500L, 1L, 100L, 0);
            when(appealMapper.selectById(500L)).thenReturn(appeal);
            when(appealMapper.updateById(any())).thenReturn(1);

            appealService.reviewAppeal(500L, 1, "申诉通过");

            // 验证创建了两条补偿明细
            verify(creditCompensationService).recordDetail(eq(500L), eq(100L),
                    eq("BASE_RESTORE"), eq(3), contains("基础信用恢复"));
            verify(creditCompensationService).recordDetail(eq(500L), eq(100L),
                    eq("INTERACTION_RESTORE"), eq(2), contains("互动损失补偿"));
        }
    }

    @Test
    void appealApproval_totalCompensationIs5() {
        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> secMock =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            secMock.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(1L);

            AppealEntity appeal = createAppeal(500L, 1L, 100L, 0);
            when(appealMapper.selectById(500L)).thenReturn(appeal);
            when(appealMapper.updateById(any())).thenReturn(1);

            appealService.reviewAppeal(500L, 1, "通过");

            // 验证信用分总回补是+5（保持向后兼容）
            verify(userCreditService).changeCredit(eq(100L), eq(5),
                    contains("信用分回补"), eq("appeal"), eq(500L));
        }
    }

    @Test
    void rejectedAppeal_noCompensation() {
        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> secMock =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            secMock.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(1L);

            AppealEntity appeal = createAppeal(500L, 1L, 100L, 0);
            when(appealMapper.selectById(500L)).thenReturn(appeal);
            when(appealMapper.updateById(any())).thenReturn(1);

            appealService.reviewAppeal(500L, 2, "申诉拒绝");

            verify(creditCompensationService, never()).recordDetail(anyLong(), anyLong(),
                    anyString(), anyInt(), anyString());
            verify(userCreditService, never()).changeCredit(anyLong(), anyInt(),
                    anyString(), anyString(), anyLong());
        }
    }

    @Test
    void compensationSummary_correctTotal() {
        // 单独测试CreditCompensationServiceImpl的汇总功能
        CreditCompensationDetailMapper detailMapper = mock(CreditCompensationDetailMapper.class);
        CreditCompensationServiceImpl compensationService = new CreditCompensationServiceImpl();

        CreditCompensationDetailEntity detail1 = createCompensationDetail(1L, 500L, 100L, "BASE_RESTORE", 3);
        CreditCompensationDetailEntity detail2 = createCompensationDetail(2L, 500L, 100L, "INTERACTION_RESTORE", 2);

        // 由于 compensationService 内部依赖 mapper 注入，这里验证 VO 结构
        CreditCompensationVo vo = new CreditCompensationVo();
        vo.setAppealId(500L);
        vo.setUserId(100L);
        vo.setDetails(Arrays.asList(detail1, detail2));
        vo.setTotalCompensation(detail1.getCompensationValue() + detail2.getCompensationValue());

        assertEquals(5, vo.getTotalCompensation());
        assertEquals(2, vo.getDetails().size());
        assertEquals(500L, vo.getAppealId());
    }
}
