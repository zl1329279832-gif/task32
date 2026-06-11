package com.oddfar.campus.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oddfar.campus.business.domain.entity.AdminModerationScopeEntity;

import java.util.List;
import java.util.Set;

/**
 * 管理员审核范围服务
 */
public interface AdminModerationScopeService extends IService<AdminModerationScopeEntity> {

    /**
     * 检查管理员是否有权审核该内容
     */
    boolean canModerate(Long adminUserId, Long contentId);

    /**
     * 获取管理员可管理的分类id集合
     */
    Set<Long> getAllowedCategoryIds(Long adminUserId);

    /**
     * 查询管理员的审核范围
     */
    List<AdminModerationScopeEntity> getScopes(Long adminUserId);
}
