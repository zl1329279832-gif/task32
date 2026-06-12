package com.oddfar.campus.business.controller.admin;

import com.oddfar.campus.business.domain.vo.FileReviewBatchVo;
import com.oddfar.campus.business.service.CampusFileService;
import com.oddfar.campus.common.annotation.ApiResource;
import com.oddfar.campus.common.domain.R;
import com.oddfar.campus.common.enums.ResBizTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 附件复核管理
 */
@RestController
@RequestMapping("/admin/attachment/review")
@ApiResource(name = "附件复核管理", appCode = "campus", resBizType = ResBizTypeEnum.BUSINESS)
public class AttachmentReviewController {

    @Autowired
    private CampusFileService fileService;

    /**
     * 设置附件复核状态
     */
    @PreAuthorize("@ss.resourceAuth()")
    @PutMapping(value = "/status", name = "设置附件复核状态")
    public R setReviewStatus(@RequestParam Long fileId, @RequestParam Integer reviewStatus) {
        fileService.setReviewStatus(fileId, reviewStatus);
        return R.ok();
    }

    /**
     * 批量设置附件复核状态
     */
    @PreAuthorize("@ss.resourceAuth()")
    @PutMapping(value = "/batch-status", name = "批量设置附件复核状态")
    public R batchSetReviewStatus(@Validated @RequestBody FileReviewBatchVo vo) {
        for (Long fileId : vo.getFileIds()) {
            fileService.setReviewStatus(fileId, vo.getReviewStatus());
        }
        return R.ok();
    }
}
