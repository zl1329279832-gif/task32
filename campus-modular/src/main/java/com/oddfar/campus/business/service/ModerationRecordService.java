package com.oddfar.campus.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oddfar.campus.business.domain.entity.ModerationRecordEntity;

import com.oddfar.campus.common.domain.PageResult;

import java.util.Date;
import java.util.List;

/**
 * 审核记录服务
 */
public interface ModerationRecordService extends IService<ModerationRecordEntity> {

    /**
     * 记录审核动作
     */
    ModerationRecordEntity recordAction(Long contentId, String targetType, Long targetId,
                                         String moderationType, String action, String reason,
                                         String matchedRules, Integer beforeStatus, Integer afterStatus);

    /**
     * 查询某内容的审核历史
     */
    List<ModerationRecordEntity> getByContentId(Long contentId);

    /**
     * 根据批次id查询审核记录
     */
    List<ModerationRecordEntity> getByBatchId(Long batchId);

    /**
     * 审计查询（支持多条件筛选+分页）
     */
    PageResult<ModerationRecordEntity> queryAuditRecords(Long contentId, Long adminId,
            String action, String targetType, Long batchId, Date startTime, Date endTime);
}
