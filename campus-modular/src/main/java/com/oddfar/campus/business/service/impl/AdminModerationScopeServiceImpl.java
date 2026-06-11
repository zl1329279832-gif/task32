package com.oddfar.campus.business.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddfar.campus.business.domain.entity.AdminModerationScopeEntity;
import com.oddfar.campus.business.domain.entity.ContentEntity;
import com.oddfar.campus.business.mapper.AdminModerationScopeMapper;
import com.oddfar.campus.business.mapper.ContentMapper;
import com.oddfar.campus.business.service.AdminModerationScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理员审核范围服务实现
 */
@Service
public class AdminModerationScopeServiceImpl extends ServiceImpl<AdminModerationScopeMapper, AdminModerationScopeEntity>
        implements AdminModerationScopeService {

    @Autowired
    private AdminModerationScopeMapper adminModerationScopeMapper;
    @Autowired
    private ContentMapper contentMapper;

    @Override
    public boolean canModerate(Long adminUserId, Long contentId) {
        // userId=1 是超级管理员，拥有全部权限
        if (adminUserId != null && adminUserId == 1L) {
            return true;
        }

        List<AdminModerationScopeEntity> scopes = getScopes(adminUserId);
        if (scopes.isEmpty()) {
            return false;
        }

        // 检查是否有 ALL 范围
        boolean hasAll = scopes.stream().anyMatch(s -> "ALL".equals(s.getScopeType()));
        if (hasAll) {
            return true;
        }

        // 检查分类范围
        ContentEntity content = contentMapper.selectById(contentId);
        if (content == null) {
            return false;
        }

        Set<Long> allowedCats = getAllowedCategoryIds(adminUserId);
        return allowedCats.contains(content.getCategoryId());
    }

    @Override
    public Set<Long> getAllowedCategoryIds(Long adminUserId) {
        List<AdminModerationScopeEntity> scopes = getScopes(adminUserId);
        return scopes.stream()
                .filter(s -> "CATEGORY".equals(s.getScopeType()) && s.getScopeValue() != null)
                .map(AdminModerationScopeEntity::getScopeValue)
                .collect(Collectors.toSet());
    }

    @Override
    public List<AdminModerationScopeEntity> getScopes(Long adminUserId) {
        return adminModerationScopeMapper.selectByAdminUserId(adminUserId);
    }
}
