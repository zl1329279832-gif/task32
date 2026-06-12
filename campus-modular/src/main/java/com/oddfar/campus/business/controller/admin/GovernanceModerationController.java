package com.oddfar.campus.business.controller.admin;

import com.oddfar.campus.business.domain.entity.ContentEntity;
import com.oddfar.campus.business.domain.entity.GovernanceBatchEntity;
import com.oddfar.campus.business.domain.entity.ModerationRecordEntity;
import com.oddfar.campus.business.domain.vo.BatchReviewVo;
import com.oddfar.campus.business.enums.CampusBizCodeEnum;
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
    private UserCreditService userCreditService;
    @Autowired
    private AdminModerationScopeService scopeService;
    @Autowired
    private GovernanceBatchService governanceBatchService;

    /**
     * 批量审核通过
     */
    @PreAuthorize("@ss.resourceAuth()")
    @PostMapping(value = "/batchApprove", name = "批量审核通过")
    public R batchApprove(@Validated @RequestBody BatchReviewVo vo) {
        Long adminUserId = SecurityUtils.getUserId();
        // 创建治理批次
        GovernanceBatchEntity batch = governanceBatchService.createBatch(
                "BATCH_REVIEW", adminUserId,
                vo.getReason() != null ? vo.getReason() : "管理员批量审核通过",
                vo.getContentIds().size());
        for (Long contentId : vo.getContentIds()) {
            checkScope(adminUserId, contentId);
            ContentEntity content = contentService.getById(contentId);
            if (content != null && content.getStatus() == 0) {
                // 使用统一恢复方法（包含解冻评论、清除附件违规、记录审核）
                contentService.restoreContent(contentId,
                        vo.getReason() != null ? vo.getReason() : "管理员批量审核通过",
                        "ADMIN_RESTORE");
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
        // 创建治理批次
        GovernanceBatchEntity batch = governanceBatchService.createBatch(
                "REJECT", adminUserId,
                vo.getReason() != null ? vo.getReason() : "管理员批量审核拒绝",
                vo.getContentIds().size());
        for (Long contentId : vo.getContentIds()) {
            checkScope(adminUserId, contentId);
            ContentEntity content = contentService.getById(contentId);
            if (content != null && content.getStatus() == 0) {
                // 使用统一下架方法（包含快照、冻结评论、记录审核、幂等保护）
                contentService.deleteContentById(contentId);

                // 更新状态为拒绝(3)而不是下架(2)，区分审核拒绝和管理员下架
                content.setStatus(3);
                contentService.updateById(content);

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
     * 恢复内容（统一恢复：内容状态、评论解冻、附件违规清除、点赞统计回补）
     */
    @PreAuthorize("@ss.resourceAuth()")
    @PutMapping(value = "/restore", name = "恢复内容")
    public R restore(@RequestParam Long contentId, @RequestParam(required = false) String reason) {
        checkScope(SecurityUtils.getUserId(), contentId);
        contentService.restoreContent(contentId,
                reason != null ? reason : "管理员手动恢复",
                "ADMIN_RESTORE");
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
