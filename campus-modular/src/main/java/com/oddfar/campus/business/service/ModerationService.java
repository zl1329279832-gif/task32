package com.oddfar.campus.business.service;

import com.oddfar.campus.business.domain.entity.CommentEntity;
import com.oddfar.campus.business.domain.vo.ModerationResultVo;
import com.oddfar.campus.business.domain.vo.SendContentVo;

public interface ModerationService {

    /**
     * 审核内容
     */
    ModerationResultVo moderateContent(SendContentVo sendContentVo, Long userId);

    /**
     * 审核评论
     */
    ModerationResultVo moderateComment(CommentEntity comment);

    /**
     * 计算风险分
     */
    int calculateRiskScore(Long userId, String text, Integer contentType, Integer fileCount, Long categoryId);
}
