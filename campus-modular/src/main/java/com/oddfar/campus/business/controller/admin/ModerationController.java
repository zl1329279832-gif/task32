package com.oddfar.campus.business.controller.admin;

import com.oddfar.campus.business.domain.entity.*;
import com.oddfar.campus.business.domain.vo.BatchModerationVo;
import com.oddfar.campus.business.enums.CampusBizCodeEnum;
import com.oddfar.campus.business.service.*;
import com.oddfar.campus.common.annotation.ApiResource;
import com.oddfar.campus.common.domain.PageResult;
import com.oddfar.campus.common.domain.R;
import com.oddfar.campus.common.enums.ResBizTypeEnum;
import com.oddfar.campus.common.exception.ServiceException;
import com.oddfar.campus.common.utils.SecurityUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 审核管理
 */
@RestController
@RequestMapping("/admin/moderation")
@ApiResource(name = "审核管理", appCode = "campus", resBizType = ResBizTypeEnum.BUSINESS)
public class ModerationController {

    @Autowired
    private ModerationRecordService moderationRecordService;
    @Autowired
    private ViolationRecordService violationRecordService;
    @Autowired
    private UserCreditService userCreditService;
    @Autowired
    private CreditLogService creditLogService;
    @Autowired
    private ContentService contentService;

    @GetMapping(value = "/records", name = "审核记录列表")
    @PreAuthorize("@ss.resourceAuth()")
    public R records(ModerationRecordEntity record) {
        PageResult<ModerationRecordEntity> page = moderationRecordService.page(record);
        return R.ok().put(page);
    }

    @GetMapping(value = "/record/{recordId}", name = "审核记录详情")
    @PreAuthorize("@ss.resourceAuth()")
    public R recordDetail(@PathVariable Long recordId) {
        return R.ok(moderationRecordService.getById(recordId));
    }

    @PostMapping(value = "/batch", name = "批量审核")
    @PreAuthorize("@ss.resourceAuth()")
    public R batchModerate(@Validated @RequestBody BatchModerationVo batchVo) {
        if (batchVo.getContentIds() == null || batchVo.getContentIds().isEmpty()) {
            throw new ServiceException(CampusBizCodeEnum.MODERATION_BATCH_EMPTY.getMsg(),
                    CampusBizCodeEnum.MODERATION_BATCH_EMPTY.getCode());
        }

        Long reviewerId = SecurityUtils.getUserId();
        for (Long contentId : batchVo.getContentIds()) {
            ContentEntity content = contentService.getById(contentId);
            if (content == null) {
                continue;
            }

            int newStatus;
            switch (batchVo.getAction()) {
                case 1: newStatus = 1; break; // 通过
                case 2: newStatus = 2; break; // 下架
                case 3: newStatus = 3; break; // 拒绝
                case 4: // 恢复
                    contentService.restoreContentWithInteractions(contentId);
                    // 创建审核记录
                    ModerationRecordEntity restoreRecord = new ModerationRecordEntity();
                    restoreRecord.setRecordId(IdWorker.getId());
                    restoreRecord.setTargetType(1);
                    restoreRecord.setTargetId(contentId);
                    restoreRecord.setUserId(content.getUserId());
                    restoreRecord.setAction(1);
                    restoreRecord.setTriggerType(1);
                    restoreRecord.setReviewerId(reviewerId);
                    restoreRecord.setManualRemark(batchVo.getRemark());
                    moderationRecordService.addRecord(restoreRecord);
                    continue;
                default:
                    throw new ServiceException(CampusBizCodeEnum.MODERATION_INVALID_ACTION.getMsg(),
                            CampusBizCodeEnum.MODERATION_INVALID_ACTION.getCode());
            }

            // 更新内容状态
            ContentEntity update = new ContentEntity();
            update.setContentId(contentId);
            update.setStatus(newStatus);
            contentService.updateContent(update);

            // 创建审核记录
            ModerationRecordEntity record = new ModerationRecordEntity();
            record.setRecordId(IdWorker.getId());
            record.setTargetType(1);
            record.setTargetId(contentId);
            record.setUserId(content.getUserId());
            record.setAction(newStatus);
            record.setTriggerType(1);
            record.setReviewerId(reviewerId);
            record.setManualRemark(batchVo.getRemark());
            moderationRecordService.addRecord(record);

            // 下架或拒绝时创建违规记录并扣分
            if (newStatus == 2 || newStatus == 3) {
                ViolationRecordEntity violation = new ViolationRecordEntity();
                violation.setUserId(content.getUserId());
                violation.setTargetType(1);
                violation.setTargetId(contentId);
                violation.setViolationType("manual");
                violation.setDescription(batchVo.getRemark());
                violation.setPenaltyType(newStatus == 2 ? 1 : 2);
                violation.setCreditDeduct(5);
                violation.setRecordId(record.getRecordId());
                violationRecordService.addViolation(violation);
            }
        }
        return R.ok();
    }

    @GetMapping(value = "/violations", name = "违规记录列表")
    @PreAuthorize("@ss.resourceAuth()")
    public R violations(ViolationRecordEntity record) {
        PageResult<ViolationRecordEntity> page = violationRecordService.page(record);
        return R.ok().put(page);
    }

    @GetMapping(value = "/credits", name = "信用分列表")
    @PreAuthorize("@ss.resourceAuth()")
    public R credits(UserCreditEntity credit) {
        return R.ok(userCreditService.list());
    }

    @GetMapping(value = "/credit-logs", name = "信用分变更日志")
    @PreAuthorize("@ss.resourceAuth()")
    public R creditLogs(CreditLogEntity creditLog) {
        PageResult<CreditLogEntity> page = creditLogService.page(creditLog);
        return R.ok().put(page);
    }

    @PostMapping(value = "/credit/adjust", name = "手动调整信用分")
    @PreAuthorize("@ss.resourceAuth()")
    public R adjustCredit(@RequestParam Long userId, @RequestParam int points,
                          @RequestParam String reason) {
        if (points > 0) {
            userCreditService.restoreCredit(userId, points, reason, "manual", null);
        } else if (points < 0) {
            userCreditService.deductCredit(userId, -points, reason, "manual", null);
        }
        return R.ok();
    }
}
