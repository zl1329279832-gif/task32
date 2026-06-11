package com.oddfar.campus.business.service;

import com.oddfar.campus.business.domain.entity.AppealEntity;
import com.oddfar.campus.business.domain.entity.ContentEntity;
import com.oddfar.campus.business.domain.vo.AppealReviewVo;
import com.oddfar.campus.business.domain.vo.AppealSubmitVo;
import com.oddfar.campus.business.enums.CampusBizCodeEnum;
import com.oddfar.campus.business.mapper.AppealMapper;
import com.oddfar.campus.business.service.impl.AppealServiceImpl;
import com.oddfar.campus.common.exception.ServiceException;
import com.oddfar.campus.common.utils.SecurityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 申诉恢复测试
 */
@ExtendWith(MockitoExtension.class)
public class AppealServiceTest {

    @InjectMocks
    private AppealServiceImpl appealService;

    @Mock
    private AppealMapper appealMapper;

    @Mock
    private ContentService contentService;

    @Mock
    private UserCreditService userCreditService;

    @Mock
    private ModerationRecordService moderationRecordService;

    @Mock
    private ViolationRecordService violationRecordService;

    @Test
    @DisplayName("对被拒绝的内容提交申诉应成功")
    void testSubmitAppealOnRejectedContent() {
        ContentEntity content = new ContentEntity();
        content.setContentId(100L);
        content.setUserId(1L);
        content.setStatus(3);

        when(appealMapper.selectCount(any())).thenReturn(0L);
        when(contentService.getById(100L)).thenReturn(content);
        when(appealMapper.insert(any(AppealEntity.class))).thenReturn(1);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getUserId).thenReturn(1L);

            AppealSubmitVo vo = new AppealSubmitVo();
            vo.setTargetType(1);
            vo.setTargetId(100L);
            vo.setReason("内容不违规，请求恢复");

            assertDoesNotThrow(() -> appealService.submitAppeal(vo));
            verify(appealMapper).insert(any(AppealEntity.class));
        }
    }

    @Test
    @DisplayName("对正常内容提交申诉应失败")
    void testSubmitAppealOnNormalContent() {
        ContentEntity content = new ContentEntity();
        content.setContentId(200L);
        content.setUserId(1L);
        content.setStatus(1);

        when(appealMapper.selectCount(any())).thenReturn(0L);
        when(contentService.getById(200L)).thenReturn(content);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getUserId).thenReturn(1L);

            AppealSubmitVo vo = new AppealSubmitVo();
            vo.setTargetType(1);
            vo.setTargetId(200L);
            vo.setReason("内容已正常");

            ServiceException exception = assertThrows(ServiceException.class,
                    () -> appealService.submitAppeal(vo));
            assertEquals(CampusBizCodeEnum.APPEAL_TARGET_NOT_ELIGIBLE.getCode(),
                    exception.getCode());
        }
    }

    @Test
    @DisplayName("重复申诉应失败")
    void testDuplicateAppeal() {
        when(appealMapper.selectCount(any())).thenReturn(1L);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getUserId).thenReturn(1L);

            AppealSubmitVo vo = new AppealSubmitVo();
            vo.setTargetType(1);
            vo.setTargetId(100L);
            vo.setReason("重复申诉");

            ServiceException exception = assertThrows(ServiceException.class,
                    () -> appealService.submitAppeal(vo));
            assertEquals(CampusBizCodeEnum.APPEAL_ALREADY_EXISTS.getCode(),
                    exception.getCode());
        }
    }

    @Test
    @DisplayName("通过申诉应恢复内容和信用分")
    void testApproveAppeal() {
        AppealEntity appeal = new AppealEntity();
        appeal.setAppealId(1L);
        appeal.setUserId(1L);
        appeal.setTargetType(1);
        appeal.setTargetId(100L);
        appeal.setStatus(0);

        when(appealMapper.selectById(1L)).thenReturn(appeal);
        when(appealMapper.updateById(any(AppealEntity.class))).thenReturn(1);
        when(violationRecordService.list(any())).thenReturn(Collections.emptyList());
        doNothing().when(contentService).restoreContentWithInteractions(100L);
        doNothing().when(moderationRecordService).addRecord(any());

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getUserId).thenReturn(99L);

            AppealReviewVo vo = new AppealReviewVo();
            vo.setAppealId(1L);
            vo.setStatus(2);
            vo.setReviewRemark("经核实，内容不违规");

            assertDoesNotThrow(() -> appealService.reviewAppeal(vo));
            verify(contentService).restoreContentWithInteractions(100L);
        }
    }

    @Test
    @DisplayName("驳回申诉应不恢复内容")
    void testRejectAppeal() {
        AppealEntity appeal = new AppealEntity();
        appeal.setAppealId(2L);
        appeal.setUserId(1L);
        appeal.setTargetType(1);
        appeal.setTargetId(200L);
        appeal.setStatus(0);

        when(appealMapper.selectById(2L)).thenReturn(appeal);
        when(appealMapper.updateById(any(AppealEntity.class))).thenReturn(1);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getUserId).thenReturn(99L);

            AppealReviewVo vo = new AppealReviewVo();
            vo.setAppealId(2L);
            vo.setStatus(3);
            vo.setReviewRemark("确实违规");

            assertDoesNotThrow(() -> appealService.reviewAppeal(vo));
            verify(contentService, never()).restoreContentWithInteractions(anyLong());
        }
    }
}
