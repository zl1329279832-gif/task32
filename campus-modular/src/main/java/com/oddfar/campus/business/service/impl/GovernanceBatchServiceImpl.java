package com.oddfar.campus.business.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddfar.campus.business.domain.entity.GovernanceBatchEntity;
import com.oddfar.campus.business.mapper.GovernanceBatchMapper;
import com.oddfar.campus.business.service.GovernanceBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 治理批次服务实现
 */
@Service
public class GovernanceBatchServiceImpl extends ServiceImpl<GovernanceBatchMapper, GovernanceBatchEntity>
        implements GovernanceBatchService {

    @Autowired
    private GovernanceBatchMapper governanceBatchMapper;

    @Override
    public GovernanceBatchEntity createBatch(String batchType, Long adminId, String reason, int contentCount) {
        GovernanceBatchEntity batch = new GovernanceBatchEntity();
        batch.setBatchId(IdWorker.getId());
        batch.setBatchType(batchType);
        batch.setAdminId(adminId);
        batch.setReason(reason);
        batch.setContentCount(contentCount);
        batch.setCreateTime(new Date());
        governanceBatchMapper.insert(batch);
        return batch;
    }

    @Override
    public List<GovernanceBatchEntity> getByAdminId(Long adminId) {
        return governanceBatchMapper.selectByAdminId(adminId);
    }
}
