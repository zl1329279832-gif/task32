package com.oddfar.campus.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oddfar.campus.business.domain.entity.AppealEntity;
import com.oddfar.campus.common.domain.PageResult;

import java.util.List;

/**
 * 申诉服务
 */
public interface AppealService extends IService<AppealEntity> {

    /**
     * 提交申诉
     */
    int submitAppeal(Long contentId, String reason);

    /**
     * 审核申诉
     *
     * @param appealId      申诉id
     * @param decision      1=通过, 2=拒绝
     * @param reviewComment 审核意见
     * @return 影响行数
     */
    int reviewAppeal(Long appealId, Integer decision, String reviewComment);

    /**
     * 分页查询申诉列表
     */
    PageResult<AppealEntity> page(AppealEntity entity);

    /**
     * 查询用户的申诉列表
     */
    List<AppealEntity> getMyAppeals();
}
