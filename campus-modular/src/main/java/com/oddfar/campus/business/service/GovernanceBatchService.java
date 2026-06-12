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
    GovernanceBatchEntity createBatch(String batchType, List<Long> contentIds, String reason);

    /**
     * 根据id查询批次
     */
    GovernanceBatchEntity getBatchById(Long batchId);
}
