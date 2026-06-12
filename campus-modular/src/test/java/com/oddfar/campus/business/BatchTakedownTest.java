package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.*;
import com.oddfar.campus.business.mapper.ContentMapper;
import com.oddfar.campus.business.service.*;
import com.oddfar.campus.business.service.impl.ContentServiceImpl;
import com.oddfar.campus.business.service.impl.GovernanceCacheHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 批量下架测试
 */
@ExtendWith(MockitoExtension.class)
public class BatchTakedownTest extends BaseTest {

    @InjectMocks
    private ContentServiceImpl contentService;

    @Mock
    private ContentMapper contentMapper;
    @Mock
    private CommentService commentService;
    @Mock
    private ModerationRecordService moderationRecordService;
    @Mock
    private InteractionSnapshotService snapshotService;
    @Mock
    private GovernanceBatchService governanceBatchService;
    @Mock
    private GovernanceCacheHelper governanceCacheHelper;
    @Mock
    private CampusFileService fileService;
    @Mock
    private ViolationRecordService violationRecordService;

    @BeforeEach
    void setUp() {
        ModerationRecordEntity mockRecord = new ModerationRecordEntity();
        mockRecord.setRecordId(9999L);
        lenient().when(moderationRecordService.recordAction(anyLong(), anyString(), any(),
                anyString(), anyString(), anyString(), any(), any(), any())).thenReturn(mockRecord);
        lenient().when(moderationRecordService.updateById(any())).thenReturn(true);
        lenient().when(contentMapper.updateById(any())).thenReturn(1);
    }

    @Test
    void batchTakedown_createsGovernanceBatch() {
        List<Long> contentIds = Arrays.asList(100L, 200L);
        GovernanceBatchEntity batch = createBatch(1L, "GOV-20260612-00001", 1L, "TAKEDOWN", 2);
        when(governanceBatchService.createBatch(eq("TAKEDOWN"), eq(contentIds), eq("违规内容"))).thenReturn(batch);

        ContentEntity content1 = createContent(100L, 10L, 1L, "内容1", 1, 0);
        ContentEntity content2 = createContent(200L, 20L, 1L, "内容2", 1, 0);
        when(contentMapper.selectById(100L)).thenReturn(content1);
        when(contentMapper.selectById(200L)).thenReturn(content2);

        GovernanceBatchEntity result = contentService.batchTakedown(contentIds, "违规内容");

        assertNotNull(result);
        assertEquals("GOV-20260612-00001", result.getBatchNo());
        assertEquals("TAKEDOWN", result.getBatchType());
        assertEquals(2, result.getContentCount());
        verify(governanceBatchService).createBatch("TAKEDOWN", contentIds, "违规内容");
    }

    @Test
    void batchTakedown_linksRecordsToBatch() {
        List<Long> contentIds = Arrays.asList(100L, 200L);
        GovernanceBatchEntity batch = createBatch(1L, "GOV-20260612-00002", 1L, "TAKEDOWN", 2);
        when(governanceBatchService.createBatch(anyString(), anyList(), anyString())).thenReturn(batch);

        ContentEntity content1 = createContent(100L, 10L, 1L, "内容1", 1, 0);
        ContentEntity content2 = createContent(200L, 20L, 1L, "内容2", 1, 0);
        when(contentMapper.selectById(100L)).thenReturn(content1);
        when(contentMapper.selectById(200L)).thenReturn(content2);

        contentService.batchTakedown(contentIds, "批量违规");

        // 验证每条记录都关联了批次
        verify(moderationRecordService, times(2)).updateById(argThat(record ->
                record instanceof ModerationRecordEntity &&
                ((ModerationRecordEntity) record).getBatchId() != null &&
                ((ModerationRecordEntity) record).getBatchId().equals(1L)));
    }

    @Test
    void batchTakedown_skipsAlreadyTakenDown() {
        List<Long> contentIds = Arrays.asList(100L, 200L);
        GovernanceBatchEntity batch = createBatch(1L, "GOV-20260612-00003", 1L, "TAKEDOWN", 2);
        when(governanceBatchService.createBatch(anyString(), anyList(), anyString())).thenReturn(batch);

        ContentEntity content1 = createContent(100L, 10L, 1L, "内容1", 2, 0); // 已下架
        ContentEntity content2 = createContent(200L, 20L, 1L, "内容2", 1, 0);
        when(contentMapper.selectById(100L)).thenReturn(content1);
        when(contentMapper.selectById(200L)).thenReturn(content2);

        contentService.batchTakedown(contentIds, "批量下架");

        // content1已下架不产生快照，只有content2产生快照
        verify(snapshotService, times(1)).takeSnapshot(eq(200L), eq("TAKEDOWN"), any());
        verify(snapshotService, never()).takeSnapshot(eq(100L), anyString(), any());
    }

    @Test
    void batchTakedown_evictsCache() {
        List<Long> contentIds = Arrays.asList(100L, 200L);
        GovernanceBatchEntity batch = createBatch(1L, "GOV-20260612-00004", 1L, "TAKEDOWN", 2);
        when(governanceBatchService.createBatch(anyString(), anyList(), anyString())).thenReturn(batch);

        ContentEntity content1 = createContent(100L, 10L, 1L, "内容1", 1, 0);
        ContentEntity content2 = createContent(200L, 20L, 1L, "内容2", 1, 0);
        when(contentMapper.selectById(100L)).thenReturn(content1);
        when(contentMapper.selectById(200L)).thenReturn(content2);

        contentService.batchTakedown(contentIds, "批量下架");

        verify(governanceCacheHelper).evictContentCaches(contentIds);
    }
}
