package com.oddfar.campus.business.controller.admin;

import com.oddfar.campus.business.domain.entity.CampusFileEntity;
import com.oddfar.campus.business.domain.vo.FileViolationVo;
import com.oddfar.campus.business.service.CampusFileService;
import com.oddfar.campus.business.service.ModerationRecordService;
import com.oddfar.campus.common.annotation.ApiResource;
import com.oddfar.campus.common.domain.R;
import com.oddfar.campus.common.enums.ResBizTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 附件违规管理
 */
@RestController
@RequestMapping("/admin/fileViolation")
@ApiResource(name = "附件违规管理", appCode = "campus", resBizType = ResBizTypeEnum.BUSINESS)
public class AttachmentViolationController {

    @Autowired
    private CampusFileService campusFileService;
    @Autowired
    private ModerationRecordService moderationRecordService;

    /**
     * 标记/清除附件违规
     */
    @PreAuthorize("@ss.resourceAuth()")
    @PutMapping(value = "/flag", name = "标记附件违规")
    public R flag(@Validated @RequestBody FileViolationVo vo) {
        CampusFileEntity file = campusFileService.getById(vo.getFileId());
        Long contentId = file != null ? file.getContentId() : null;

        if (vo.getViolationStatus() == 1) {
            // 幂等：已标记违规的不重复操作
            if (file != null && file.getViolationStatus() != null && file.getViolationStatus() == 1) {
                return R.ok();
            }
            campusFileService.flagViolation(vo.getFileId(), vo.getViolationReason());
            // 审计记录
            moderationRecordService.recordAction(
                    contentId, "FILE", vo.getFileId(),
                    "MANUAL", "BLOCK", "附件标记违规: " + vo.getViolationReason(),
                    null, 0, 1);
        } else {
            // 幂等：未标记违规的不重复操作
            if (file != null && (file.getViolationStatus() == null || file.getViolationStatus() == 0)) {
                return R.ok();
            }
            campusFileService.clearViolation(vo.getFileId());
            // 审计记录
            moderationRecordService.recordAction(
                    contentId, "FILE", vo.getFileId(),
                    "MANUAL", "RESTORE", "附件违规复核通过",
                    null, 1, 0);
        }
        return R.ok();
    }
}
