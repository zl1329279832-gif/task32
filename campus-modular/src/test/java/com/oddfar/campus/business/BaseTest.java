package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.*;
import com.oddfar.campus.business.mapper.*;
import org.mockito.Mock;

/**
 * 测试基类 - 提供Mock对象和测试数据工厂
 */
public abstract class BaseTest {

    /** 创建测试用内容实体 */
    protected ContentEntity createContent(Long contentId, Long userId, Long categoryId,
                                           String content, Integer status, Integer type) {
        ContentEntity entity = new ContentEntity();
        entity.setContentId(contentId);
        entity.setUserId(userId);
        entity.setCategoryId(categoryId);
        entity.setContent(content);
        entity.setStatus(status);
        entity.setType(type);
        entity.setLoveCount(0L);
        entity.setFileCount(0);
        return entity;
    }

    /** 创建测试用评论实体 */
    protected CommentEntity createComment(Long commentId, Long contentId, Long userId,
                                            String coContent) {
        CommentEntity entity = new CommentEntity();
        entity.setCommentId(commentId);
        entity.setContentId(contentId);
        entity.setUserId(userId);
        entity.setCoContent(coContent);
        entity.setParentId(0L);
        entity.setOneLevelId(-1L);
        entity.setFrozenStatus(0);
        return entity;
    }

    /** 创建测试用敏感词实体 */
    protected SensitiveWordEntity createSensitiveWord(Long wordId, String word,
                                                       Integer severity, String category) {
        SensitiveWordEntity entity = new SensitiveWordEntity();
        entity.setWordId(wordId);
        entity.setWord(word);
        entity.setSeverity(severity);
        entity.setCategory(category);
        entity.setStatus("0");
        return entity;
    }

    /** 创建测试用审核规则 */
    protected ModerationRuleEntity createRule(Long ruleId, String ruleType, String ruleKey,
                                               String action, String description) {
        ModerationRuleEntity entity = new ModerationRuleEntity();
        entity.setRuleId(ruleId);
        entity.setRuleType(ruleType);
        entity.setRuleKey(ruleKey);
        entity.setRuleValue("{}");
        entity.setAction(action);
        entity.setPriority(10);
        entity.setStatus("0");
        entity.setDescription(description);
        return entity;
    }

    /** 创建测试用申诉实体 */
    protected AppealEntity createAppeal(Long appealId, Long contentId, Long userId,
                                         Integer status) {
        AppealEntity entity = new AppealEntity();
        entity.setAppealId(appealId);
        entity.setContentId(contentId);
        entity.setUserId(userId);
        entity.setAppealReason("测试申诉理由");
        entity.setAppealStatus(status);
        return entity;
    }

    /** 创建管理员审核范围 */
    protected AdminModerationScopeEntity createScope(Long scopeId, Long adminUserId,
                                                      String scopeType, Long scopeValue) {
        AdminModerationScopeEntity entity = new AdminModerationScopeEntity();
        entity.setScopeId(scopeId);
        entity.setAdminUserId(adminUserId);
        entity.setScopeType(scopeType);
        entity.setScopeValue(scopeValue);
        return entity;
    }

    /** 创建治理批次 */
    protected GovernanceBatchEntity createBatch(Long batchId, String batchNo,
                                                 Long adminId, String batchType, Integer contentCount) {
        GovernanceBatchEntity entity = new GovernanceBatchEntity();
        entity.setBatchId(batchId);
        entity.setBatchNo(batchNo);
        entity.setAdminId(adminId);
        entity.setBatchType(batchType);
        entity.setContentCount(contentCount);
        return entity;
    }

    /** 创建信用分补偿明细 */
    protected CreditCompensationDetailEntity createCompensationDetail(Long detailId,
                                                                       Long appealId, Long userId,
                                                                       String type, Integer value) {
        CreditCompensationDetailEntity entity = new CreditCompensationDetailEntity();
        entity.setDetailId(detailId);
        entity.setAppealId(appealId);
        entity.setUserId(userId);
        entity.setCompensationType(type);
        entity.setCompensationValue(value);
        return entity;
    }

    /** 创建互动快照 */
    protected InteractionSnapshotEntity createSnapshot(Long snapshotId, Long contentId,
                                                        Long loveCount, Long commentCount) {
        InteractionSnapshotEntity entity = new InteractionSnapshotEntity();
        entity.setSnapshotId(snapshotId);
        entity.setContentId(contentId);
        entity.setLoveCount(loveCount);
        entity.setCommentCount(commentCount);
        entity.setRecommendCount(0L);
        entity.setFavoriteCount(0L);
        entity.setReportCount(0L);
        entity.setSearchHitCount(0L);
        entity.setSnapshotType("TAKEDOWN");
        entity.setCreateTime(new java.util.Date());
        return entity;
    }
}
