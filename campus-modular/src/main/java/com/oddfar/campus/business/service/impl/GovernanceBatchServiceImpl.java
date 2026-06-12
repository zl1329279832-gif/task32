package com.oddfar.campus.business.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddfar.campus.business.domain.entity.GovernanceBatchEntity;
import com.oddfar.campus.business.enums.CampusBizCodeEnum;
import com.oddfar.campus.business.mapper.GovernanceBatchMapper;
import com.oddfar.campus.business.service.GovernanceBatchService;
import com.oddfar.campus.common.exception.ServiceException;
import com.oddfar.campus.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 治理批次服务实现
 */
@Service
public class GovernanceBatchServiceImpl extends ServiceImpl<GovernanceBatchMapper, GovernanceBatchEntity>
        implements GovernanceBatchService {

    @Autowired
    private GovernanceBatchMapper governanceBatchMapper;

    private static final AtomicLong BATCH_SEQ = new AtomicLong(0);

    @Override
    public GovernanceBatchEntity createBatch(String batchType, List<Long> contentIds, String reason) {
        GovernanceBatchEntity batch = new GovernanceBatchEntity();
        batch.setBatchId(IdWorker.getId());
        batch.setBatchNo(generateBatchNo());
        batch.setBatchType(batchType);
        batch.setContentCount(contentIds.size());
        batch.setReason(reason);

        try {
            batch.setAdminId(SecurityUtils.getUserId());
            batch.setAdminName(SecurityUtils.getLoginUser().getUser().getNickName());
        } catch (Exception e) {
            // ignore if can't get admin info
        }

        governanceBatchMapper.insert(batch);
        return batch;
    }

    @Override
    public GovernanceBatchEntity getBatchById(Long batchId) {
        GovernanceBatchEntity batch = governanceBatchMapper.selectById(batchId);
        if (batch == null) {
            throw new ServiceException(CampusBizCodeEnum.BATCH_NOT_FOUND.getMsg(),
                    CampusBizCodeEnum.BATCH_NOT_FOUND.getCode());
        }
        return batch;
    }

    private String generateBatchNo() {
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        long seq = BATCH_SEQ.incrementAndGet();
        return String.format("GOV-%s-%05d", dateStr, seq);
    }
}
