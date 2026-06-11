package com.oddfar.campus.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddfar.campus.business.domain.entity.ViolationRecordEntity;
import com.oddfar.campus.business.mapper.ViolationRecordMapper;
import com.oddfar.campus.business.service.UserCreditService;
import com.oddfar.campus.business.service.ViolationRecordService;
import com.oddfar.campus.common.domain.PageResult;
import com.oddfar.campus.common.core.page.PageUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ViolationRecordServiceImpl extends ServiceImpl<ViolationRecordMapper, ViolationRecordEntity>
        implements ViolationRecordService {

    @Resource
    private ViolationRecordMapper violationRecordMapper;

    @Resource
    private UserCreditService userCreditService;

    @Override
    public PageResult<ViolationRecordEntity> page(ViolationRecordEntity record) {
        PageUtils.startPage();
        List<ViolationRecordEntity> list = violationRecordMapper.selectViolationPage(record);
        return PageUtils.getPageResult(list);
    }

    @Override
    public void addViolation(ViolationRecordEntity record) {
        if (record.getViolationId() == null) {
            record.setViolationId(IdWorker.getId());
        }
        violationRecordMapper.insert(record);

        // 扣除信用分
        if (record.getCreditDeduct() != null && record.getCreditDeduct() > 0) {
            userCreditService.deductCredit(
                    record.getUserId(),
                    record.getCreditDeduct(),
                    "违规: " + record.getDescription(),
                    "moderation",
                    record.getRecordId()
            );
        }
    }

    @Override
    public long countByUserId(Long userId) {
        LambdaQueryWrapper<ViolationRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ViolationRecordEntity::getUserId, userId);
        return violationRecordMapper.selectCount(wrapper);
    }
}
