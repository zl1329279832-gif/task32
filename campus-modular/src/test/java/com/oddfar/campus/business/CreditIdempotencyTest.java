package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.CreditLogEntity;
import com.oddfar.campus.business.domain.entity.UserCreditEntity;
import com.oddfar.campus.business.mapper.CreditLogMapper;
import com.oddfar.campus.business.mapper.UserCreditMapper;
import com.oddfar.campus.business.service.impl.UserCreditServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 信用分幂等与治理状态机测试
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
     * 相同关联类型+关联id的信用分变更应幂等跳过
     */
    @Test
    void duplicateCreditChangeShouldBeSkipped() {
        // 模拟已存在同一(userId, relatedType, relatedId)的日志
        when(creditLogMapper.existsByRelated(100L, "appeal", 5001L)).thenReturn(true);

        userCreditService.changeCredit(100L, 5, "申诉通过，信用分回补", "appeal", 5001L);

        // 不应更新信用分
        verify(userCreditMapper, never()).updateById(any());
        // 不应插入新日志
        verify(creditLogMapper, never()).insert(any());
    }

    /**
     * 首次信用分变更应正常执行
     */
    @Test
    void firstCreditChangeShouldSucceed() {
        when(creditLogMapper.existsByRelated(100L, "appeal", 5002L)).thenReturn(false);

        UserCreditEntity entity = new UserCreditEntity();
        entity.setCreditId(1L);
        entity.setUserId(100L);
        entity.setCreditScore(70);
        when(userCreditMapper.selectByUserId(100L)).thenReturn(entity);
        when(userCreditMapper.updateById(any())).thenReturn(1);
        when(creditLogMapper.insert(any(CreditLogEntity.class))).thenReturn(1);

        userCreditService.changeCredit(100L, 5, "申诉通过", "appeal", 5002L);

        // 信用分应更新为75
        assertEquals(75, entity.getCreditScore());
        verify(userCreditMapper).updateById(entity);
        verify(creditLogMapper).insert(argThat(log ->
                log.getUserId().equals(100L) &&
                        log.getChangeValue() == 5 &&
                        "appeal".equals(log.getRelatedType()) &&
                        log.getRelatedId().equals(5002L)));
    }

    /**
     * 信用分不应超过100上限
     */
    @Test
    void creditScoreShouldNotExceed100() {
        when(creditLogMapper.existsByRelated(100L, "appeal", 5003L)).thenReturn(false);

        UserCreditEntity entity = new UserCreditEntity();
        entity.setCreditId(1L);
        entity.setUserId(100L);
        entity.setCreditScore(98);
        when(userCreditMapper.selectByUserId(100L)).thenReturn(entity);
        when(userCreditMapper.updateById(any())).thenReturn(1);
        when(creditLogMapper.insert(any(CreditLogEntity.class))).thenReturn(1);

        userCreditService.changeCredit(100L, 5, "回补", "appeal", 5003L);

        assertEquals(100, entity.getCreditScore());
    }

    /**
     * 信用分不应低于0下限
     */
    @Test
    void creditScoreShouldNotGoBelowZero() {
        when(creditLogMapper.existsByRelated(100L, "content", 1001L)).thenReturn(false);

        UserCreditEntity entity = new UserCreditEntity();
        entity.setCreditId(1L);
        entity.setUserId(100L);
        entity.setCreditScore(3);
        when(userCreditMapper.selectByUserId(100L)).thenReturn(entity);
        when(userCreditMapper.updateById(any())).thenReturn(1);
        when(creditLogMapper.insert(any(CreditLogEntity.class))).thenReturn(1);

        userCreditService.changeCredit(100L, -10, "自动拦截", "content", 1001L);

        assertEquals(0, entity.getCreditScore());
    }

    /**
     * relatedType为null时不做幂等检查（允许手动调整）
     */
    @Test
    void nullRelatedTypeShouldBypassIdempotencyCheck() {
        UserCreditEntity entity = new UserCreditEntity();
        entity.setCreditId(1L);
        entity.setUserId(100L);
        entity.setCreditScore(50);
        when(userCreditMapper.selectByUserId(100L)).thenReturn(entity);
        when(userCreditMapper.updateById(any())).thenReturn(1);
        when(creditLogMapper.insert(any(CreditLogEntity.class))).thenReturn(1);

        userCreditService.changeCredit(100L, 10, "管理员手动调整", null, null);

        assertEquals(60, entity.getCreditScore());
        // 不应调用幂等检查
        verify(creditLogMapper, never()).existsByRelated(anyLong(), anyString(), anyLong());
    }
}
