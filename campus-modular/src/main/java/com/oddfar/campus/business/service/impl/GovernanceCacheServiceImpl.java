package com.oddfar.campus.business.service.impl;

import com.oddfar.campus.business.service.GovernanceCacheService;
import com.oddfar.campus.common.core.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * 治理相关Redis缓存失效服务实现
 */
@Service
public class GovernanceCacheServiceImpl implements GovernanceCacheService {

    private static final String CONTENT_DETAIL_KEY = "campus:content:detail:";
    private static final String CONTENT_LIST_KEY = "campus:content:list";
    private static final String HOT_CONTENT_KEY = "campus:content:hot";
    private static final String USER_CONTENT_KEY = "campus:content:user:";

    @Autowired
    private RedisCache redisCache;

    @Override
    public void evictContentCaches(Long contentId) {
        // 清除内容详情缓存
        redisCache.deleteObject(CONTENT_DETAIL_KEY + contentId);
        // 清除内容列表缓存（模糊匹配）
        Collection<String> listKeys = redisCache.keys(CONTENT_LIST_KEY + "*");
        if (listKeys != null && !listKeys.isEmpty()) {
            redisCache.deleteObject(listKeys);
        }
        // 清除热门内容缓存
        redisCache.deleteObject(HOT_CONTENT_KEY);
    }

    @Override
    public void evictUserCaches(Long userId) {
        redisCache.deleteObject(USER_CONTENT_KEY + userId);
    }
}
