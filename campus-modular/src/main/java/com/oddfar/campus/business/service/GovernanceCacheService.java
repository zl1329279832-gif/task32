package com.oddfar.campus.business.service;

/**
 * 治理相关Redis缓存失效服务
 */
public interface GovernanceCacheService {

    /**
     * 下架/恢复时清除内容相关缓存
     */
    void evictContentCaches(Long contentId);

    /**
     * 清除用户维度的缓存
     */
    void evictUserCaches(Long userId);
}
