package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.ContentEntity;
import com.oddfar.campus.business.domain.entity.ModerationRecordEntity;
import com.oddfar.campus.business.domain.entity.ModerationRuleEntity;
import com.oddfar.campus.business.domain.entity.SensitiveWordEntity;
import com.oddfar.campus.business.enums.ModerationDecision;
import com.oddfar.campus.business.service.*;
import com.oddfar.campus.business.service.impl.AutoModerationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 内容发布审核测试
 */
@ExtendWith(MockitoExtension.class)
public class ContentPublishReviewTest extends BaseTest {

    @InjectMocks
    private AutoModerationServiceImpl autoModerationService;

    @Mock
    private SensitiveWordService sensitiveWordService;
    @Mock
    private ModerationRuleService moderationRuleService;
    @Mock
    private ModerationRecordService moderationRecordService;
    @Mock
    private ViolationRecordService violationRecordService;
    @Mock
    private UserCreditService userCreditService;

    @BeforeEach
    void setUp() {
        // 默认无审核规则
        lenient().when(moderationRuleService.getEnabledRules()).thenReturn(Collections.emptyList());
        lenient().when(violationRecordService.getViolationCount(anyLong())).thenReturn(0L);
        // recordAction默认返回一个Mock审核记录
        ModerationRecordEntity mockRecord = new ModerationRecordEntity();
        mockRecord.setRecordId(9999L);
        lenient().when(moderationRecordService.recordAction(anyLong(), anyString(), any(), anyString(), anyString(),
                anyString(), anyString(), any(), any())).thenReturn(mockRecord);
    }

    /**
     * 干净内容且信用分高 → 自动通过 PASS
     */
    @Test
    void cleanContentShouldAutoApprove() {
        ContentEntity content = createContent(1L, 100L, 1L, "这是一条正常的内容", null, 0);

        when(sensitiveWordService.detectSensitiveWords(anyString())).thenReturn(Collections.emptyList());
        when(userCreditService.evaluateCreditAction(100L)).thenReturn("PASS");

        ModerationDecision decision = autoModerationService.evaluate(content, null, Collections.emptyList());

        assertEquals(ModerationDecision.PASS, decision);
        assertEquals(1, decision.toStatus());
        verify(moderationRecordService).recordAction(eq(1L), eq("CONTENT"), isNull(),
                eq("AUTO"), eq("PASS"), anyString(), anyString(), isNull(), eq(1));
    }

    /**
     * 内容含中低严重度敏感词 → PENDING
     */
    @Test
    void contentWithSensitiveWordShouldBePending() {
        ContentEntity content = createContent(2L, 100L, 1L, "这条内容有脏话", null, 0);

        SensitiveWordService.SensitiveWordMatch match =
                new SensitiveWordService.SensitiveWordMatch("脏话", 2, "abuse");
        when(sensitiveWordService.detectSensitiveWords(anyString()))
                .thenReturn(Arrays.asList(match));
        when(userCreditService.evaluateCreditAction(100L)).thenReturn("PASS");

        ModerationDecision decision = autoModerationService.evaluate(content, null, Collections.emptyList());

        assertEquals(ModerationDecision.PENDING, decision);
        assertEquals(0, decision.toStatus());
    }

    /**
     * 内容含高严重度敏感词 → BLOCK + 记录违规 + 扣分
     */
    @Test
    void contentWithHighSeverityWordShouldBeBlocked() {
        ContentEntity content = createContent(3L, 100L, 1L, "这条内容有违禁品", null, 0);

        SensitiveWordService.SensitiveWordMatch match =
                new SensitiveWordService.SensitiveWordMatch("违禁品", 3, "custom");
        when(sensitiveWordService.detectSensitiveWords(anyString()))
                .thenReturn(Arrays.asList(match));
        when(userCreditService.evaluateCreditAction(100L)).thenReturn("PASS");

        ModerationDecision decision = autoModerationService.evaluate(content, null, Collections.emptyList());

        assertEquals(ModerationDecision.BLOCK, decision);
        assertEquals(3, decision.toStatus());
        // 验证记录了违规
        verify(violationRecordService).recordViolation(eq(100L), eq(3L), eq("SENSITIVE_WORD"),
                anyString(), anyString(), anyLong());
        // 验证扣了信用分
        verify(userCreditService).changeCredit(eq(100L), eq(-10), anyString(), eq("content"), eq(3L));
    }

    /**
     * 内容在需要审核的分类下 → PENDING
     */
    @Test
    void contentInModeratedCategoryShouldBePending() {
        ContentEntity content = createContent(4L, 100L, 2L, "二手交易内容", null, 0);

        when(sensitiveWordService.detectSensitiveWords(anyString())).thenReturn(Collections.emptyList());
        when(userCreditService.evaluateCreditAction(100L)).thenReturn("PASS");

        ModerationRuleEntity rule = createRule(1L, "CATEGORY", "2", "PENDING", "二手交易需要审核");
        when(moderationRuleService.getEnabledRules()).thenReturn(Arrays.asList(rule));

        ModerationDecision decision = autoModerationService.evaluate(content, null, Collections.emptyList());

        assertEquals(ModerationDecision.PENDING, decision);
    }
}
