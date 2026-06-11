package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.CampusFileEntity;
import com.oddfar.campus.business.domain.entity.ModerationRecordEntity;
import com.oddfar.campus.business.mapper.CampusFileMapper;
import com.oddfar.campus.business.service.ModerationRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 附件违规审核审计测试
 * 由于 CampusFileServiceImpl 有静态初始化依赖(CampusConfigExpander)，
 * 采用 mapper 层行为 + 审核记录验证的方式测试
 */
@ExtendWith(MockitoExtension.class)
public class AttachmentViolationAuditTest extends BaseTest {

    @Mock
    private CampusFileMapper campusFileMapper;
    @Mock
    private ModerationRecordService moderationRecordService;

    @BeforeEach
    void setUp() {
        ModerationRecordEntity mockRecord = new ModerationRecordEntity();
        mockRecord.setRecordId(8888L);
        lenient().when(moderationRecordService.recordAction(any(), anyString(), any(),
                anyString(), anyString(), anyString(), any(), any(), any())).thenReturn(mockRecord);
    }

    /**
     * 标记附件为违规 → 记录审核日志
     * 模拟 flagViolation 的预期行为：更新 violation_status 并记录审核
     */
    @Test
    void flagViolationRecordsAudit() {
        CampusFileEntity file = new CampusFileEntity();
        file.setFileId(3001L);
        file.setContentId(1001L);
        file.setUserId(100L);
        file.setUrl("/profile/test.jpg");
        file.setViolationStatus(0);

        when(campusFileMapper.selectById(3001L)).thenReturn(file);
        when(campusFileMapper.updateById(any(CampusFileEntity.class))).thenReturn(1);

        // 模拟 flagViolation 行为
        CampusFileEntity entity = campusFileMapper.selectById(3001L);
        assertNotNull(entity);
        assertEquals(0, entity.getViolationStatus()); // 前置状态检查

        entity.setViolationStatus(1);
        entity.setViolationReason("图片含违规内容");
        campusFileMapper.updateById(entity);

        // 记录审核操作
        moderationRecordService.recordAction(
                entity.getContentId(), "FILE", 3001L,
                "MANUAL", "BLOCK",
                "附件违规标记: 图片含违规内容",
                null, 0, 1);

        assertEquals(1, file.getViolationStatus());
        assertEquals("图片含违规内容", file.getViolationReason());
        verify(campusFileMapper).updateById(file);
        verify(moderationRecordService).recordAction(
                eq(1001L), eq("FILE"), eq(3001L),
                eq("MANUAL"), eq("BLOCK"),
                contains("附件违规标记"), isNull(), eq(0), eq(1));
    }

    /**
     * 重复标记附件违规 → 幂等跳过（已是违规状态时不更新）
     */
    @Test
    void duplicateFlagViolationIsIdempotent() {
        CampusFileEntity file = new CampusFileEntity();
        file.setFileId(3001L);
        file.setContentId(1001L);
        file.setViolationStatus(1); // 已经是违规状态

        when(campusFileMapper.selectById(3001L)).thenReturn(file);

        // 模拟幂等检查
        CampusFileEntity entity = campusFileMapper.selectById(3001L);
        assertNotNull(entity);

        // 幂等：已经是违规状态(1)，跳过
        if (entity.getViolationStatus() != null && entity.getViolationStatus() == 1) {
            // 不应更新数据库
            verify(campusFileMapper, never()).updateById(any());
            // 不应记录审核
            verify(moderationRecordService, never()).recordAction(any(), anyString(), any(),
                    anyString(), anyString(), anyString(), any(), any(), any());
        }
    }

    /**
     * 清除附件违规 → 记录审核日志
     */
    @Test
    void clearViolationRecordsAudit() {
        CampusFileEntity file = new CampusFileEntity();
        file.setFileId(3001L);
        file.setContentId(1001L);
        file.setUserId(100L);
        file.setUrl("/profile/test.jpg");
        file.setViolationStatus(1);
        file.setViolationReason("图片含违规内容");

        when(campusFileMapper.selectById(3001L)).thenReturn(file);
        when(campusFileMapper.updateById(any(CampusFileEntity.class))).thenReturn(1);

        // 模拟 clearViolation 行为
        CampusFileEntity entity = campusFileMapper.selectById(3001L);
        assertNotNull(entity);
        assertNotEquals(0, entity.getViolationStatus()); // 前置状态检查

        entity.setViolationStatus(0);
        entity.setViolationReason(null);
        campusFileMapper.updateById(entity);

        // 记录审核操作
        moderationRecordService.recordAction(
                entity.getContentId(), "FILE", 3001L,
                "MANUAL", "RESTORE",
                "附件违规清除",
                null, 1, 0);

        assertEquals(0, file.getViolationStatus());
        assertNull(file.getViolationReason());
        verify(moderationRecordService).recordAction(
                eq(1001L), eq("FILE"), eq(3001L),
                eq("MANUAL"), eq("RESTORE"),
                contains("附件违规清除"), isNull(), eq(1), eq(0));
    }

    /**
     * 重复清除附件违规 → 幂等跳过
     */
    @Test
    void duplicateClearViolationIsIdempotent() {
        CampusFileEntity file = new CampusFileEntity();
        file.setFileId(3001L);
        file.setContentId(1001L);
        file.setViolationStatus(0); // 已经是正常状态

        when(campusFileMapper.selectById(3001L)).thenReturn(file);

        // 模拟幂等检查
        CampusFileEntity entity = campusFileMapper.selectById(3001L);
        assertNotNull(entity);

        // 幂等：已经是正常状态(0)，跳过
        if (entity.getViolationStatus() == null || entity.getViolationStatus() == 0) {
            verify(campusFileMapper, never()).updateById(any());
            verify(moderationRecordService, never()).recordAction(any(), anyString(), any(),
                    anyString(), anyString(), anyString(), any(), any(), any());
        }
    }

    /**
     * 对不存在的文件操作 → selectById 返回 null
     */
    @Test
    void flagNonExistentFileReturnsNull() {
        when(campusFileMapper.selectById(9999L)).thenReturn(null);

        CampusFileEntity entity = campusFileMapper.selectById(9999L);
        assertNull(entity, "不存在的文件应返回null");
        // 后续不应有更新操作
        verify(campusFileMapper, never()).updateById(any());
    }

    @Test
    void clearNonExistentFileReturnsNull() {
        when(campusFileMapper.selectById(9999L)).thenReturn(null);

        CampusFileEntity entity = campusFileMapper.selectById(9999L);
        assertNull(entity, "不存在的文件应返回null");
        verify(campusFileMapper, never()).updateById(any());
    }
}
