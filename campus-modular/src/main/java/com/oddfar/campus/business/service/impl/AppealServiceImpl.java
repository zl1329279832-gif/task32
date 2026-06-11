package com.oddfar.campus.business.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddfar.campus.business.domain.entity.*;
import com.oddfar.campus.business.enums.CampusBizCodeEnum;
import com.oddfar.campus.business.mapper.AppealMapper;
import com.oddfar.campus.business.mapper.CommentMapper;
import com.oddfar.campus.business.mapper.ContentMapper;
import com.oddfar.campus.business.service.*;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import com.oddfar.campus.common.domain.PageResult;
import com.oddfar.campus.common.exception.ServiceException;
import com.oddfar.campus.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 申诉服务实现
 */
@Service
public class AppealServiceImpl extends ServiceImpl<AppealMapper, AppealEntity>
        implements AppealService {

    @Autowired
    private AppealMapper appealMapper;
    @Autowired
    private ContentMapper contentMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private ModerationRecordService moderationRecordService;
    @Autowired
    private InteractionSnapshotService interactionSnapshotService;
    @Autowired
    private UserCreditService userCreditService;

    @Override
    public int submitAppeal(Long contentId, String reason) {
        Long userId = SecurityUtils.getUserId();

        // 验证内容存在
        ContentEntity content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new ServiceException(CampusBizCodeEnum.CONTENT_IS_NULL.getMsg(),
                    CampusBizCodeEnum.CONTENT_IS_NULL.getCode());
        }

        // 验证是否自己的内容
        if (!content.getUserId().equals(userId)) {
            throw new ServiceException(CampusBizCodeEnum.APPEAL_NOT_OWNER.getMsg(),
                    CampusBizCodeEnum.APPEAL_NOT_OWNER.getCode());
        }

        // 验证内容状态（只有下架=2或拒绝=3才能申诉）
        if (content.getStatus() != 2 && content.getStatus() != 3) {
            throw new ServiceException(CampusBizCodeEnum.APPEAL_NOT_ELIGIBLE.getMsg(),
                    CampusBizCodeEnum.APPEAL_NOT_ELIGIBLE.getCode());
        }

        // 检查是否已有待审或已通过的申诉
        Long existingCount = appealMapper.selectPendingOrApprovedCount(contentId);
        if (existingCount > 0) {
            throw new ServiceException(CampusBizCodeEnum.APPEAL_ALREADY_EXISTS.getMsg(),
                    CampusBizCodeEnum.APPEAL_ALREADY_EXISTS.getCode());
        }

        // 创建申诉
        AppealEntity appeal = new AppealEntity();
        appeal.setAppealId(IdWorker.getId());
        appeal.setContentId(contentId);
        appeal.setUserId(userId);
        appeal.setAppealReason(reason);
        appeal.setAppealStatus(0); // 待审
        return appealMapper.insert(appeal);
    }

    @Override
    @Transactional
    public int reviewAppeal(Long appealId, Integer decision, String reviewComment) {
        AppealEntity appeal = appealMapper.selectById(appealId);
        if (appeal == null) {
            throw new ServiceException("申诉不存在");
        }

        if (appeal.getAppealStatus() != 0) {
            throw new ServiceException("该申诉已处理");
        }

        // 更新申诉状态
        appeal.setAppealStatus(decision);
        appeal.setAdminId(SecurityUtils.getUserId());
        appeal.setReviewComment(reviewComment);
        appeal.setReviewTime(new Date());

        try {
            appeal.setAdminName(SecurityUtils.getLoginUser().getUser().getNickName());
        } catch (Exception e) {
            // ignore if can't get name
        }

        int rows = appealMapper.updateById(appeal);

        if (decision == 1) {
            // 申诉通过：恢复内容、解冻评论、恢复互动数据
            ContentEntity content = contentMapper.selectById(appeal.getContentId());
            if (content != null) {
                Integer beforeStatus = content.getStatus();

                // 恢复内容状态为正常
                content.setStatus(1);

                // 从快照恢复点赞数
                InteractionSnapshotEntity snapshot = interactionSnapshotService.getLatestSnapshot(appeal.getContentId());
                if (snapshot != null && snapshot.getLoveCount() != null) {
                    content.setLoveCount(snapshot.getLoveCount());
                }

                contentMapper.updateById(content);

                // 解冻评论
                unfreezeByContentId(appeal.getContentId());

                // 记录审核操作
                moderationRecordService.recordAction(
                        appeal.getContentId(), "CONTENT", null,
                        "MANUAL", "RESTORE", "申诉通过: " + reviewComment,
                        null, beforeStatus, 1);

                // 信用分回补
                userCreditService.changeCredit(appeal.getUserId(), 5,
                        "申诉通过，信用分回补", "appeal", appealId);
            }
        }
        // 如果拒绝(decision=2)，不做任何内容操作，终局

        return rows;
    }

    @Override
    public PageResult<AppealEntity> page(AppealEntity entity) {
        LambdaQueryWrapperX<AppealEntity> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eqIfPresent(AppealEntity::getAppealStatus, entity.getAppealStatus());
        wrapper.eqIfPresent(AppealEntity::getContentId, entity.getContentId());
        wrapper.eqIfPresent(AppealEntity::getUserId, entity.getUserId());
        wrapper.orderByDesc(AppealEntity::getCreateTime);
        return appealMapper.selectPage(wrapper);
    }

    @Override
    public List<AppealEntity> getMyAppeals() {
        return appealMapper.selectByUserId(SecurityUtils.getUserId());
    }

    /**
     * 解冻内容下的所有评论
     */
    private void unfreezeByContentId(Long contentId) {
        CommentEntity update = new CommentEntity();
        update.setFrozenStatus(0);
        commentMapper.update(update, new LambdaQueryWrapperX<CommentEntity>()
                .eq(CommentEntity::getContentId, contentId)
                .eq(CommentEntity::getFrozenStatus, 1));
    }
}
