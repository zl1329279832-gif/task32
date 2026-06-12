package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.AppealEntity;
import com.oddfar.campus.business.domain.entity.ContentEntity;
import com.oddfar.campus.business.mapper.AppealMapper;
import com.oddfar.campus.business.mapper.ContentMapper;
import com.oddfar.campus.business.service.*;
import com.oddfar.campus.business.service.impl.AppealServiceImpl;
import com.oddfar.campus.business.service.impl.GovernanceCacheHelper;
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
 * 申诉版本追踪测试
 */
@ExtendWith(MockitoExtension.class)
public class AppealVersionTest extends BaseTest {

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

    @Test
    void firstAppeal_hasVersion1() {
        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> secMock =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            secMock.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(100L);

            ContentEntity content = createContent(1L, 100L, 1L, "测试", 2, 0);
            when(contentMapper.selectById(1L)).thenReturn(content);
            when(appealMapper.selectPendingOrApprovedCount(1L)).thenReturn(0L);
            when(appealMapper.selectLatestRejected(1L)).thenReturn(null);
            when(appealMapper.insert(any())).thenReturn(1);

            appealService.submitAppeal(1L, "首次申诉");

            verify(appealMapper).insert(argThat(appeal ->
                    appeal.getAppealVersion() == 1 &&
                    appeal.getPreviousAppealId() == null));
        }
    }

    @Test
    void reAppealAfterRejection_incrementsVersion() {
        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> secMock =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            secMock.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(100L);

            ContentEntity content = createContent(1L, 100L, 1L, "测试", 2, 0);
            when(contentMapper.selectById(1L)).thenReturn(content);
            when(appealMapper.selectPendingOrApprovedCount(1L)).thenReturn(0L);

            // 有一次被拒绝的申诉
            AppealEntity rejected = createAppeal(500L, 1L, 100L, 2);
            rejected.setAppealVersion(1);
            when(appealMapper.selectLatestRejected(1L)).thenReturn(rejected);
            when(appealMapper.insert(any())).thenReturn(1);

            appealService.submitAppeal(1L, "重新申诉");

            verify(appealMapper).insert(argThat(appeal ->
                    appeal.getAppealVersion() == 2 &&
                    appeal.getPreviousAppealId().equals(500L)));
        }
    }

    @Test
    void reAppealWhilePending_blocked() {
        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> secMock =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            secMock.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(100L);

            ContentEntity content = createContent(1L, 100L, 1L, "测试", 2, 0);
            when(contentMapper.selectById(1L)).thenReturn(content);
            when(appealMapper.selectPendingOrApprovedCount(1L)).thenReturn(1L);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> appealService.submitAppeal(1L, "重复申诉"));
            assertEquals(12009, (int) ex.getCode());
        }
    }

    @Test
    void reAppealAfterApproval_blocked() {
        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> secMock =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            secMock.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(100L);

            ContentEntity content = createContent(1L, 100L, 1L, "测试", 2, 0);
            when(contentMapper.selectById(1L)).thenReturn(content);
            // 已通过的申诉也算在 pendingOrApproved 计数中
            when(appealMapper.selectPendingOrApprovedCount(1L)).thenReturn(1L);

            assertThrows(ServiceException.class,
                    () -> appealService.submitAppeal(1L, "通过后重复申诉"));
        }
    }

    @Test
    void tripleAppeal_version3() {
        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> secMock =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            secMock.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(100L);

            ContentEntity content = createContent(1L, 100L, 1L, "测试", 2, 0);
            when(contentMapper.selectById(1L)).thenReturn(content);
            when(appealMapper.selectPendingOrApprovedCount(1L)).thenReturn(0L);

            // 第二次被拒绝的申诉（版本2）
            AppealEntity rejected2 = createAppeal(600L, 1L, 100L, 2);
            rejected2.setAppealVersion(2);
            when(appealMapper.selectLatestRejected(1L)).thenReturn(rejected2);
            when(appealMapper.insert(any())).thenReturn(1);

            appealService.submitAppeal(1L, "第三次申诉");

            verify(appealMapper).insert(argThat(appeal ->
                    appeal.getAppealVersion() == 3 &&
                    appeal.getPreviousAppealId().equals(600L)));
        }
    }
}
