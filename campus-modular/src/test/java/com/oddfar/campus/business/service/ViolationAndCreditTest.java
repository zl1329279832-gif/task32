package com.oddfar.campus.business.service;

import com.oddfar.campus.business.domain.entity.CreditLogEntity;
import com.oddfar.campus.business.domain.entity.UserCreditEntity;
import com.oddfar.campus.business.domain.entity.ViolationRecordEntity;
import com.oddfar.campus.business.mapper.UserCreditMapper;
import com.oddfar.campus.business.mapper.ViolationRecordMapper;
import com.oddfar.campus.business.service.impl.UserCreditServiceImpl;
import com.oddfar.campus.business.service.impl.ViolationRecordServiceImpl;
import org.junit.jupiter.api.DisplayName;
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
 * 违规记录与信用分测试
 */
@ExtendWith(MockitoExtension.class)
public class ViolationAndCreditTest {

    @InjectMocks
    private UserCreditServiceImpl userCreditService;

    @InjectMocks
    private ViolationRecordServiceImpl violationRecordService;

    @Mock
    private UserCreditMapper userCreditMapper;

    @Mock
    private ViolationRecordMapper violationRecordMapper;

    @Mock
    private CreditLogService creditLogService;

    @Mock
    private UserCreditService userCreditServiceMock;

    @Test
    @DisplayName("扣除信用分应创建日志记录")
    void testViolationDeductsCredit() {
        UserCreditEntity credit = new UserCreditEntity();
        credit.setUserId(1L);
        credit.setCreditScore(100);
        credit.setViolationCount(0);

        when(userCreditMapper.selectById(1L)).thenReturn(credit);
        when(userCreditMapper.updateById(any(UserCreditEntity.class))).thenReturn(1);
        doNothing().when(creditLogService).addLog(any(CreditLogEntity.class));

        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> securityUtils =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            securityUtils.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(99L);

            userCreditService.deductCredit(1L, 10, "测试扣分", "moderation", 1L);

            verify(userCreditMapper).updateById(argThat(c -> {
                UserCreditEntity updated = (UserCreditEntity) c;
                return updated.getCreditScore() == 90 && updated.getViolationCount() == 1;
            }));
            verify(creditLogService).addLog(argThat(log -> {
                CreditLogEntity l = (CreditLogEntity) log;
                return l.getChangeValue() == -10 && l.getScoreBefore() == 100 && l.getScoreAfter() == 90;
            }));
        }
    }

    @Test
    @DisplayName("信用分不应低于0")
    void testCreditFloorAtZero() {
        UserCreditEntity credit = new UserCreditEntity();
        credit.setUserId(2L);
        credit.setCreditScore(5);
        credit.setViolationCount(3);

        when(userCreditMapper.selectById(2L)).thenReturn(credit);
        when(userCreditMapper.updateById(any(UserCreditEntity.class))).thenReturn(1);
        doNothing().when(creditLogService).addLog(any(CreditLogEntity.class));

        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> securityUtils =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            securityUtils.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(99L);

            userCreditService.deductCredit(2L, 20, "大额扣分", "moderation", 2L);

            verify(userCreditMapper).updateById(argThat(c -> {
                UserCreditEntity updated = (UserCreditEntity) c;
                return updated.getCreditScore() == 0;
            }));
        }
    }

    @Test
    @DisplayName("信用分不应超过100")
    void testCreditCeilingAtHundred() {
        UserCreditEntity credit = new UserCreditEntity();
        credit.setUserId(3L);
        credit.setCreditScore(95);
        credit.setViolationCount(1);

        when(userCreditMapper.selectById(3L)).thenReturn(credit);
        when(userCreditMapper.updateById(any(UserCreditEntity.class))).thenReturn(1);
        doNothing().when(creditLogService).addLog(any(CreditLogEntity.class));

        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> securityUtils =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            securityUtils.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(99L);

            userCreditService.restoreCredit(3L, 20, "申诉恢复", "appeal", 3L);

            verify(userCreditMapper).updateById(argThat(c -> {
                UserCreditEntity updated = (UserCreditEntity) c;
                return updated.getCreditScore() == 100;
            }));
        }
    }

    @Test
    @DisplayName("新用户应自动初始化100信用分")
    void testNewUserCredit() {
        when(userCreditMapper.selectById(10L)).thenReturn(null);
        when(userCreditMapper.insert(any(UserCreditEntity.class))).thenReturn(1);

        UserCreditEntity credit = userCreditService.getOrCreateCredit(10L);

        assertNotNull(credit);
        assertEquals(100, credit.getCreditScore());
        assertEquals(0, credit.getViolationCount());
        verify(userCreditMapper).insert(any(UserCreditEntity.class));
    }

    @Test
    @DisplayName("违规次数应累加")
    void testViolationCountIncrement() {
        UserCreditEntity credit = new UserCreditEntity();
        credit.setUserId(5L);
        credit.setCreditScore(80);
        credit.setViolationCount(2);

        when(userCreditMapper.selectById(5L)).thenReturn(credit);
        when(userCreditMapper.updateById(any(UserCreditEntity.class))).thenReturn(1);
        doNothing().when(creditLogService).addLog(any(CreditLogEntity.class));

        try (MockedStatic<com.oddfar.campus.common.utils.SecurityUtils> securityUtils =
                     mockStatic(com.oddfar.campus.common.utils.SecurityUtils.class)) {
            securityUtils.when(com.oddfar.campus.common.utils.SecurityUtils::getUserId).thenReturn(99L);

            userCreditService.deductCredit(5L, 5, "再次违规", "moderation", 5L);

            verify(userCreditMapper).updateById(argThat(c -> {
                UserCreditEntity updated = (UserCreditEntity) c;
                return updated.getViolationCount() == 3;
            }));
        }
    }
}
