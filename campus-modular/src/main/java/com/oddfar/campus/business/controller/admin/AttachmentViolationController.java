package com.oddfar.campus.business.controller.admin;

import com.oddfar.campus.business.domain.vo.FileViolationVo;
import com.oddfar.campus.business.service.CampusFileService;
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

    /**
     * 标记/清除附件违规
     */
    @PreAuthorize("@ss.resourceAuth()")
    @PutMapping(value = "/flag", name = "标记附件违规")
    public R flag(@Validated @RequestBody FileViolationVo vo) {
        if (vo.getViolationStatus() == 1) {
            campusFileService.flagViolation(vo.getFileId(), vo.getViolationReason());
        } else {
            campusFileService.clearViolation(vo.getFileId());
        }
        return R.ok();
    }
}
