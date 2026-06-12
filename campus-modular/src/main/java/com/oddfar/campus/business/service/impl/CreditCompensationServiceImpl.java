package com.oddfar.campus.business.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddfar.campus.business.domain.entity.CreditCompensationDetailEntity;
import com.oddfar.campus.business.domain.vo.CreditCompensationVo;
import com.oddfar.campus.business.mapper.CreditCompensationDetailMapper;
import com.oddfar.campus.business.service.CreditCompensationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 信用分补偿明细服务实现
 */
@Service
public class CreditCompensationServiceImpl extends ServiceImpl<CreditCompensationDetailMapper, CreditCompensationDetailEntity>
        implements CreditCompensationService {

    @Autowired
    private CreditCompensationDetailMapper compensationDetailMapper;

    @Override
    public CreditCompensationDetailEntity recordDetail(Long appealId, Long userId,
            String compensationType, int compensationValue, String description) {
        CreditCompensationDetailEntity detail = new CreditCompensationDetailEntity();
        detail.setDetailId(IdWorker.getId());
        detail.setAppealId(appealId);
        detail.setUserId(userId);
        detail.setCompensationType(compensationType);
        detail.setCompensationValue(compensationValue);
        detail.setDescription(description);
        detail.setCreateTime(new Date());
        compensationDetailMapper.insert(detail);
        return detail;
    }

    @Override
    public List<CreditCompensationDetailEntity> getByAppealId(Long appealId) {
        return compensationDetailMapper.selectByAppealId(appealId);
    }

    @Override
    public CreditCompensationVo buildCompensationSummary(Long appealId) {
        List<CreditCompensationDetailEntity> details = getByAppealId(appealId);
        int total = 0;
        Long userId = null;
        for (CreditCompensationDetailEntity detail : details) {
            total += detail.getCompensationValue();
            if (userId == null) {
                userId = detail.getUserId();
            }
        }

        CreditCompensationVo vo = new CreditCompensationVo();
        vo.setAppealId(appealId);
        vo.setUserId(userId);
        vo.setTotalCompensation(total);
        vo.setDetails(details);
        return vo;
    }
}
