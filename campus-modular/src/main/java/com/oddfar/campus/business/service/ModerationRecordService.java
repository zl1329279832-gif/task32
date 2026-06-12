package com.oddfar.campus.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oddfar.campus.business.domain.entity.ModerationRecordEntity;

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
     * 查询某管理员的审核记录
     */
    List<ModerationRecordEntity> getByAdminId(Long adminId);

    /**
     * 查询某批次的审核记录
     */
    List<ModerationRecordEntity> getByBatchId(Long batchId);
}
