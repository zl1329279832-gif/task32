package com.oddfar.campus.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oddfar.campus.business.domain.entity.GovernanceBatchEntity;

import java.util.List;

/**
 * 治理批次服务
 */
public interface GovernanceBatchService extends IService<GovernanceBatchEntity> {

    /**
     * 创建治理批次
     */
    GovernanceBatchEntity createBatch(String batchType, Long adminId, String reason, int contentCount);

    /**
     * 查询管理员创建的批次列表
     */
    List<GovernanceBatchEntity> getByAdminId(Long adminId);
}
