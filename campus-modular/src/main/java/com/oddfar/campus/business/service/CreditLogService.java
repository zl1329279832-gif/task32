package com.oddfar.campus.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oddfar.campus.business.domain.entity.CreditLogEntity;
import com.oddfar.campus.common.domain.PageResult;

public interface CreditLogService extends IService<CreditLogEntity> {

    PageResult<CreditLogEntity> page(CreditLogEntity creditLog);

    void addLog(CreditLogEntity creditLog);
}
