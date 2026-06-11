package com.oddfar.campus.business.service;

import com.oddfar.campus.business.domain.entity.ContentEntity;
import com.oddfar.campus.business.domain.entity.CommentEntity;
import com.oddfar.campus.business.enums.ModerationDecision;

import java.util.List;

/**
 * 自动审核服务（核心引擎）
 */
public interface AutoModerationService {

    /**
     * 评估内容发布
     *
     * @param content  内容实体
     * @param fileIds  附件id列表
     * @param tagNames 标签名列表
     * @return 审核决策
     */
    ModerationDecision evaluate(ContentEntity content, List<Long> fileIds, List<String> tagNames);

    /**
     * 评估评论发布
     *
     * @param comment 评论实体
     * @return 审核决策
     */
    ModerationDecision evaluateComment(CommentEntity comment);
}
