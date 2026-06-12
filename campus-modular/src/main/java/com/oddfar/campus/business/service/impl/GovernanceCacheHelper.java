package com.oddfar.campus.business.service.impl;

import com.oddfar.campus.common.constant.CacheConstants;
import com.oddfar.campus.common.core.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 治理操作后的缓存失效助手
 */
@Component
public class GovernanceCacheHelper {

    @Autowired
    private RedisCache redisCache;

    /**
     * 清除单个内容相关缓存
     */
    public void evictContentCaches(Long contentId) {
        redisCache.deleteObject(CacheConstants.CONTENT_DETAIL_KEY + contentId);
        redisCache.deleteObject(CacheConstants.CONTENT_COMMENTS_KEY + contentId);
        // 列表缓存也需要失效（内容可见性变化）
        redisCache.deleteObject(CacheConstants.CONTENT_HOT_KEY);
        redisCache.deleteObject(CacheConstants.CONTENT_NEWEST_KEY);
    }

    /**
     * 批量清除内容相关缓存
     */
    public void evictContentCaches(List<Long> contentIds) {
        for (Long contentId : contentIds) {
            redisCache.deleteObject(CacheConstants.CONTENT_DETAIL_KEY + contentId);
            redisCache.deleteObject(CacheConstants.CONTENT_COMMENTS_KEY + contentId);
        }
        // 列表缓存只清一次
        redisCache.deleteObject(CacheConstants.CONTENT_HOT_KEY);
        redisCache.deleteObject(CacheConstants.CONTENT_NEWEST_KEY);
    }
}
