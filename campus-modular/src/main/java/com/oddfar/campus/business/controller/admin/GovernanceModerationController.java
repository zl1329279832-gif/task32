package com.oddfar.campus.business.controller.admin;

import com.oddfar.campus.business.domain.entity.ContentEntity;
import com.oddfar.campus.business.domain.entity.InteractionSnapshotEntity;
import com.oddfar.campus.business.domain.entity.ModerationRecordEntity;
import com.oddfar.campus.business.domain.vo.BatchReviewVo;
import com.oddfar.campus.business.enums.CampusBizCodeEnum;
import com.oddfar.campus.business.enums.ModerationDecision;
import com.oddfar.campus.business.service.*;
import com.oddfar.campus.common.annotation.ApiResource;
import com.oddfar.campus.common.domain.R;
import com.oddfar.campus.common.enums.ResBizTypeEnum;
import com.oddfar.campus.common.exception.ServiceException;
import com.oddfar.campus.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 内容治理管理
 */
@RestController
@RequestMapping("/admin/governance")
@ApiResource(name = "内容治理管理", appCode = "campus", resBizType = ResBizTypeEnum.BUSINESS)
public class GovernanceModerationController {

    @Autowired
    private ContentService contentService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private ModerationRecordService moderationRecordService;
    @Autowired
    private InteractionSnapshotService snapshotService;
    @Autowired
    private UserCreditService userCreditService;
    @Autowired
    private AdminModerationScopeService scopeService;

    /**
     * 批量审核通过
     */
    @PreAuthorize("@ss.resourceAuth()")
    @PostMapping(value = "/batchApprove", name = "批量审核通过")
    public R batchApprove(@Validated @RequestBody BatchReviewVo vo) {
        Long adminUserId = SecurityUtils.getUserId();
        for (Long contentId : vo.getContentIds()) {
            checkScope(adminUserId, contentId);
            ContentEntity content = contentService.getById(contentId);
            if (content != null && content.getStatus() == 0) {
                Integer beforeStatus = content.getStatus();
                content.setStatus(1);
                contentService.updateById(content);
                // 解冻评论
                commentService.unfreezeByContentId(contentId);
                // 记录审核
                moderationRecordService.recordAction(contentId, "CONTENT", null,
                        "MANUAL", "PASS", vo.getReason(), null, beforeStatus, 1);
            }
        }
        return R.ok();
    }

    /**
     * 批量审核拒绝
     */
    @PreAuthorize("@ss.resourceAuth()")
    @PostMapping(value = "/batchReject", name = "批量审核拒绝")
    public R batchReject(@Validated @RequestBody BatchReviewVo vo) {
        Long adminUserId = SecurityUtils.getUserId();
        for (Long contentId : vo.getContentIds()) {
            checkScope(adminUserId, contentId);
            ContentEntity content = contentService.getById(contentId);
            if (content != null && content.getStatus() == 0) {
                Integer beforeStatus = content.getStatus();
                // 快照
                snapshotService.takeSnapshot(contentId, "REJECT", null);
                // 冻结评论
                commentService.freezeByContentId(contentId);
                content.setStatus(3);
                contentService.updateById(content);
                // 记录审核
                moderationRecordService.recordAction(contentId, "CONTENT", null,
                        "MANUAL", "BLOCK", vo.getReason(), null, beforeStatus, 3);
                // 扣分
                userCreditService.changeCredit(content.getUserId(), -5,
                        "内容审核拒绝", "content", contentId);
            }
        }
        return R.ok();
    }

    /**
     * 下架内容
     */
    @PreAuthorize("@ss.resourceAuth()")
    @PutMapping(value = "/takedown", name = "下架内容")
    public R takedown(@RequestParam Long contentId, @RequestParam(required = false) String reason) {
        checkScope(SecurityUtils.getUserId(), contentId);
        contentService.deleteContentById(contentId);
        return R.ok();
    }

    /**
     * 恢复内容
     */
    @PreAuthorize("@ss.resourceAuth()")
    @PutMapping(value = "/restore", name = "恢复内容")
    public R restore(@RequestParam Long contentId, @RequestParam(required = false) String reason) {
        checkScope(SecurityUtils.getUserId(), contentId);
        ContentEntity content = contentService.getById(contentId);
        if (content != null && (content.getStatus() == 2 || content.getStatus() == 3)) {
            Integer beforeStatus = content.getStatus();

            // 恢复内容状态
            content.setStatus(1);
            // 从快照恢复点赞数
            InteractionSnapshotEntity snapshot = snapshotService.getLatestSnapshot(contentId);
            if (snapshot != null && snapshot.getLoveCount() != null) {
                content.setLoveCount(snapshot.getLoveCount());
            }
            contentService.updateById(content);

            // 解冻评论
            commentService.unfreezeByContentId(contentId);

            // 记录审核
            moderationRecordService.recordAction(contentId, "CONTENT", null,
                    "MANUAL", "RESTORE", reason, null, beforeStatus, 1);
        }
        return R.ok();
    }

    /**
     * 查看审核历史
     */
    @PreAuthorize("@ss.resourceAuth()")
    @GetMapping(value = "/history/{contentId}", name = "查看审核历史")
    public R history(@PathVariable Long contentId) {
        List<ModerationRecordEntity> records = moderationRecordService.getByContentId(contentId);
        return R.ok(records);
    }

    /**
     * 检查管理员审核范围
     */
    private void checkScope(Long adminUserId, Long contentId) {
        // userId=1 是超级管理员
        if (adminUserId != null && adminUserId == 1L) {
            return;
        }
        if (!scopeService.canModerate(adminUserId, contentId)) {
            throw new ServiceException(CampusBizCodeEnum.MODERATION_SCOPE_DENIED.getMsg(),
                    CampusBizCodeEnum.MODERATION_SCOPE_DENIED.getCode());
        }
    }
}
