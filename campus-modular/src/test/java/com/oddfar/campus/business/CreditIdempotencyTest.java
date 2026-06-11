package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.CreditLogEntity;
import com.oddfar.campus.business.domain.entity.UserCreditEntity;
import com.oddfar.campus.business.mapper.CreditLogMapper;
import com.oddfar.campus.business.mapper.UserCreditMapper;
import com.oddfar.campus.business.service.impl.UserCreditServiceImpl;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 信用分变更幂等性测试
 * 核心场景：同一 (relatedType, relatedId) 的信用分回补只执行一次
 */
@ExtendWith(MockitoExtension.class)
public class CreditIdempotencyTest extends BaseTest {

    @InjectMocks
    private UserCreditServiceImpl userCreditService;

    @Mock
    private UserCreditMapper userCreditMapper;
    @Mock
    private CreditLogMapper creditLogMapper;

    /**
     * 首次信用分回补 → 正常执行
     */
    @Test
    void firstCreditChangeSucceeds() {
        UserCreditEntity credit = new UserCreditEntity();
        credit.setCreditId(1L);
        credit.setUserId(100L);
        credit.setCreditScore(90);
        when(userCreditMapper.selectByUserId(100L)).thenReturn(credit);
        when(userCreditMapper.updateById(any())).thenReturn(1);
        // 没有已存在的日志 → 允许执行
        when(creditLogMapper.selectCount(any(LambdaQueryWrapperX.class))).thenReturn(0L);
        when(creditLogMapper.insert(any())).thenReturn(1);

        userCreditService.changeCredit(100L, 5, "申诉通过回补", "appeal", 5001L);

        // 验证信用分更新
        assertEquals(95, credit.getCreditScore());
        verify(userCreditMapper).updateById(credit);
        // 验证日志记录
        verify(creditLogMapper).insert(argThat(log ->
                log.getUserId().equals(100L) &&
                        log.getChangeValue() == 5 &&
                        "appeal".equals(log.getRelatedType()) &&
                        log.getRelatedId().equals(5001L)
        ));
    }

    /**
     * 重复信用分回补（相同 relatedType + relatedId）→ 幂等跳过
     */
    @Test
    void duplicateCreditChangeIsSkipped() {
        // 已存在一条相同 (relatedType, relatedId) 的日志
        when(creditLogMapper.selectCount(any(LambdaQueryWrapperX.class))).thenReturn(1L);

        userCreditService.changeCredit(100L, 5, "申诉通过回补", "appeal", 5001L);

        // 不应查询用户信用
        verify(userCreditMapper, never()).selectByUserId(anyLong());
        // 不应更新信用分
        verify(userCreditMapper, never()).updateById(any());
        // 不应插入新日志
        verify(creditLogMapper, never()).insert(any());
    }

    /**
     * 不同 relatedId 的信用分变更 → 正常执行（不幂等跳过）
     */
    @Test
    void differentRelatedIdAllowsChange() {
        UserCreditEntity credit = new UserCreditEntity();
        credit.setCreditId(1L);
        credit.setUserId(100L);
        credit.setCreditScore(85);
        when(userCreditMapper.selectByUserId(100L)).thenReturn(credit);
        when(userCreditMapper.updateById(any())).thenReturn(1);
        when(creditLogMapper.selectCount(any(LambdaQueryWrapperX.class))).thenReturn(0L);
        when(creditLogMapper.insert(any())).thenReturn(1);

        // 不同的 appeal id
        userCreditService.changeCredit(100L, 5, "另一次申诉回补", "appeal", 5002L);

        verify(userCreditMapper).updateById(credit);
        assertEquals(90, credit.getCreditScore());
    }

    /**
     * relatedType 为 null 时 → 不做幂等检查，始终执行
     */
    @Test
    void nullRelatedTypeAlwaysExecutes() {
        UserCreditEntity credit = new UserCreditEntity();
        credit.setCreditId(1L);
        credit.setUserId(100L);
        credit.setCreditScore(70);
        when(userCreditMapper.selectByUserId(100L)).thenReturn(credit);
        when(userCreditMapper.updateById(any())).thenReturn(1);
        when(creditLogMapper.insert(any())).thenReturn(1);

        userCreditService.changeCredit(100L, -10, "内容审核拒绝", null, null);

        // 不应进行幂等查询
        verify(creditLogMapper, never()).selectCount(any());
        // 应正常执行
        verify(userCreditMapper).updateById(credit);
        assertEquals(60, credit.getCreditScore());
    }

    /**
     * 信用分上限100和下限0的边界测试
     */
    @Test
    void creditScoreCappedAtBounds() {
        UserCreditEntity credit = new UserCreditEntity();
        credit.setCreditId(1L);
        credit.setUserId(100L);
        credit.setCreditScore(98);
        when(userCreditMapper.selectByUserId(100L)).thenReturn(credit);
        when(userCreditMapper.updateById(any())).thenReturn(1);
        when(creditLogMapper.insert(any())).thenReturn(1);

        // 加5分，但不超过100
        userCreditService.changeCredit(100L, 5, "测试上限", "test", 1L);
        assertEquals(100, credit.getCreditScore());

        // 重置，测试下限
        credit.setCreditScore(3);
        userCreditService.changeCredit(100L, -10, "测试下限", "test", 2L);
        assertEquals(0, credit.getCreditScore());
    }
}
