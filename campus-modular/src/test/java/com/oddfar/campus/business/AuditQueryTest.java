package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.ModerationRecordEntity;
import com.oddfar.campus.business.mapper.ModerationRecordMapper;
import com.oddfar.campus.business.service.impl.ModerationRecordServiceImpl;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import com.oddfar.campus.common.domain.PageResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 审计查询测试
 */
@ExtendWith(MockitoExtension.class)
public class AuditQueryTest extends BaseTest {

    @InjectMocks
    private ModerationRecordServiceImpl moderationRecordService;

    @Mock
    private ModerationRecordMapper moderationRecordMapper;

    @Test
    void queryByContentId_returnsRecords() {
        ModerationRecordEntity record = new ModerationRecordEntity();
        record.setRecordId(1L);
        record.setContentId(100L);
        record.setAction("TAKEDOWN");
        PageResult<ModerationRecordEntity> page = new PageResult<>(Arrays.asList(record), 1);
        when(moderationRecordMapper.selectPage(any(LambdaQueryWrapperX.class))).thenReturn(page);

        PageResult<ModerationRecordEntity> result = moderationRecordService.queryAuditRecords(
                100L, null, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        assertEquals(100L, result.getRows().get(0).getContentId());
    }

    @Test
    void queryByBatchId_returnsRecords() {
        ModerationRecordEntity record = new ModerationRecordEntity();
        record.setRecordId(1L);
        record.setBatchId(50L);
        when(moderationRecordMapper.selectByBatchId(50L)).thenReturn(Arrays.asList(record));

        List<ModerationRecordEntity> results = moderationRecordService.getByBatchId(50L);

        assertEquals(1, results.size());
        assertEquals(50L, results.get(0).getBatchId());
    }

    @Test
    void queryByAction_returnsRecords() {
        ModerationRecordEntity record = new ModerationRecordEntity();
        record.setRecordId(1L);
        record.setAction("RESTORE");
        PageResult<ModerationRecordEntity> page = new PageResult<>(Arrays.asList(record), 1);
        when(moderationRecordMapper.selectPage(any(LambdaQueryWrapperX.class))).thenReturn(page);

        PageResult<ModerationRecordEntity> result = moderationRecordService.queryAuditRecords(
                null, null, "RESTORE", null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getRows().size());
    }

    @Test
    void queryByDateRange_returnsRecords() {
        Date startTime = new Date(System.currentTimeMillis() - 86400000);
        Date endTime = new Date();
        PageResult<ModerationRecordEntity> page = new PageResult<>(Collections.emptyList(), 0);
        when(moderationRecordMapper.selectPage(any(LambdaQueryWrapperX.class))).thenReturn(page);

        PageResult<ModerationRecordEntity> result = moderationRecordService.queryAuditRecords(
                null, null, null, null, null, startTime, endTime);

        assertNotNull(result);
        assertEquals(0, result.getRows().size());
    }
}
