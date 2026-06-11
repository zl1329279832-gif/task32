package com.oddfar.campus.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddfar.campus.business.domain.entity.CreditLogEntity;
import com.oddfar.campus.business.mapper.CreditLogMapper;
import com.oddfar.campus.business.service.CreditLogService;
import com.oddfar.campus.common.domain.PageResult;
import com.oddfar.campus.common.core.page.PageUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class CreditLogServiceImpl extends ServiceImpl<CreditLogMapper, CreditLogEntity>
        implements CreditLogService {

    @Resource
    private CreditLogMapper creditLogMapper;

    @Override
    public PageResult<CreditLogEntity> page(CreditLogEntity creditLog) {
        PageUtils.startPage();
        LambdaQueryWrapper<CreditLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(creditLog.getUserId() != null, CreditLogEntity::getUserId, creditLog.getUserId())
               .eq(creditLog.getRefType() != null, CreditLogEntity::getRefType, creditLog.getRefType())
               .orderByDesc(CreditLogEntity::getCreateTime);
        List<CreditLogEntity> list = creditLogMapper.selectList(wrapper);
        return PageUtils.getPageResult(list);
    }

    @Override
    public void addLog(CreditLogEntity creditLog) {
        if (creditLog.getLogId() == null) {
            creditLog.setLogId(IdWorker.getId());
        }
        if (creditLog.getCreateTime() == null) {
            creditLog.setCreateTime(new Date());
        }
        creditLogMapper.insert(creditLog);
    }
}
