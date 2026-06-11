package com.oddfar.campus.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oddfar.campus.business.domain.entity.AppealEntity;
import com.oddfar.campus.business.domain.vo.AppealReviewVo;
import com.oddfar.campus.business.domain.vo.AppealSubmitVo;
import com.oddfar.campus.common.domain.PageResult;

import java.util.List;

public interface AppealService extends IService<AppealEntity> {

    PageResult<AppealEntity> page(AppealEntity appeal);

    /**
     * 用户提交申诉
     */
    void submitAppeal(AppealSubmitVo vo);

    /**
     * 管理员审核申诉
     */
    void reviewAppeal(AppealReviewVo vo);

    /**
     * 查询用户自己的申诉列表
     */
    List<AppealEntity> getOwnAppeals(Long userId);

    /**
     * 是否存在进行中的申诉
     */
    boolean hasActiveAppeal(Integer targetType, Long targetId);
}
