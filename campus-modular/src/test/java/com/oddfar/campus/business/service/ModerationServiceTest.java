package com.oddfar.campus.business.service;

import com.oddfar.campus.business.domain.vo.ModerationResultVo;
import com.oddfar.campus.business.domain.vo.SendContentVo;
import com.oddfar.campus.business.service.impl.ModerationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 内容发布审核测试
 */
@ExtendWith(MockitoExtension.class)
public class ModerationServiceTest {

    @InjectMocks
    private ModerationServiceImpl moderationService;

    @Mock
    private SensitiveWordService sensitiveWordService;

    @Mock
    private UserCreditService userCreditService;

    @Mock
    private ViolationRecordService violationRecordService;

    private SendContentVo cleanContentVo;
    private SendContentVo riskyContentVo;

    @BeforeEach
    void setUp() {
        cleanContentVo = new SendContentVo();
        cleanContentVo.setCategoryId(1L);
        cleanContentVo.setContent("这是一条正常的校园分享");
        cleanContentVo.setType(0);
        cleanContentVo.setIsAnonymous(0);

        riskyContentVo = new SendContentVo();
        riskyContentVo.setCategoryId(1L);
        riskyContentVo.setContent("这是包含违规词汇的内容");
        riskyContentVo.setType(1);
        riskyContentVo.setIsAnonymous(0);
        riskyContentVo.setFileList(Arrays.asList(1L, 2L, 3L));
    }

    @Test
    @DisplayName("低风险内容应直接发布")
    void testAutoPassLowRiskContent() {
        when(sensitiveWordService.checkText(anyString())).thenReturn(Collections.emptyList());
        when(violationRecordService.countByUserId(1L)).thenReturn(0L);
        when(userCreditService.getCreditScore(1L)).thenReturn(100);

        ModerationResultVo result = moderationService.moderateContent(cleanContentVo, 1L);

        assertEquals(1, result.getDecision(), "低风险内容应直接发布");
        assertTrue(result.getRiskScore() < 30, "风险分应低于30");
        assertTrue(result.getHitWords().isEmpty(), "不应命中敏感词");
    }

    @Test
    @DisplayName("中风险内容应进入待审")
    void testAutoPendingMediumRiskContent() {
        // 敏感词level2(+8), 1次违规(+8), 信用分55(+12), 图片3文件(+4) = 32
        when(sensitiveWordService.checkText(anyString())).thenReturn(Arrays.asList("轻微词"));
        when(sensitiveWordService.getMaxHitLevel("轻微词")).thenReturn(2);
        when(violationRecordService.countByUserId(2L)).thenReturn(1L);
        when(userCreditService.getCreditScore(2L)).thenReturn(55);

        ModerationResultVo result = moderationService.moderateContent(riskyContentVo, 2L);

        assertEquals(0, result.getDecision(), "中风险内容应进入待审");
        assertTrue(result.getRiskScore() >= 30 && result.getRiskScore() < 70,
                "风险分应在30-70之间, 实际: " + result.getRiskScore());
    }

    @Test
    @DisplayName("高风险内容应被拦截")
    void testAutoBlockHighRiskContent() {
        when(sensitiveWordService.checkText(anyString())).thenReturn(Arrays.asList("违禁词1", "违禁词2"));
        when(sensitiveWordService.getMaxHitLevel("违禁词1")).thenReturn(3);
        when(sensitiveWordService.getMaxHitLevel("违禁词2")).thenReturn(3);
        when(violationRecordService.countByUserId(3L)).thenReturn(5L);
        when(userCreditService.getCreditScore(3L)).thenReturn(20);

        ModerationResultVo result = moderationService.moderateContent(riskyContentVo, 3L);

        assertEquals(3, result.getDecision(), "高风险内容应被拦截");
        assertTrue(result.getRiskScore() >= 70, "风险分应>=70, 实际: " + result.getRiskScore());
        assertFalse(result.getHitWords().isEmpty(), "应命中敏感词");
    }

    @Test
    @DisplayName("敏感词检测应返回命中词")
    void testSensitiveWordDetection() {
        when(sensitiveWordService.checkText("文本含有敏感词ABC"))
                .thenReturn(Arrays.asList("敏感词ABC"));
        when(sensitiveWordService.getMaxHitLevel(anyString())).thenReturn(2);
        when(violationRecordService.countByUserId(1L)).thenReturn(0L);
        when(userCreditService.getCreditScore(1L)).thenReturn(100);

        SendContentVo vo = new SendContentVo();
        vo.setCategoryId(1L);
        vo.setContent("文本含有敏感词ABC");
        vo.setType(0);
        vo.setIsAnonymous(0);

        ModerationResultVo result = moderationService.moderateContent(vo, 1L);

        assertTrue(result.getHitWords().contains("敏感词ABC"), "应返回命中的敏感词");
    }

    @Test
    @DisplayName("风险分计算应正确加权")
    void testRiskScoreCalculation() {
        // 无敏感词、3次违规(+15)、信用分50(+12)、视频内容(+5)
        when(sensitiveWordService.checkText(anyString())).thenReturn(Collections.emptyList());
        when(violationRecordService.countByUserId(4L)).thenReturn(3L);
        when(userCreditService.getCreditScore(4L)).thenReturn(50);

        int score = moderationService.calculateRiskScore(4L, "正常文本", 2, 1, 1L);

        // 期望: 0(敏感词) + 15(违规3次) + 12(信用50) + 5(视频) = 32
        assertEquals(32, score, "风险分计算应正确: 0+15+12+5=32");
    }
}
