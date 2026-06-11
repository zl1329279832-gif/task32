package com.oddfar.campus.business.controller.admin;

import com.oddfar.campus.business.domain.entity.AppealEntity;
import com.oddfar.campus.business.domain.vo.AppealReviewVo;
import com.oddfar.campus.business.service.AppealService;
import com.oddfar.campus.common.annotation.ApiResource;
import com.oddfar.campus.common.core.page.PageUtils;
import com.oddfar.campus.common.domain.PageResult;
import com.oddfar.campus.common.domain.R;
import com.oddfar.campus.common.enums.ResBizTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 申诉管理
 */
@RestController
@RequestMapping("/admin/appeal")
@ApiResource(name = "申诉管理", appCode = "campus", resBizType = ResBizTypeEnum.BUSINESS)
public class AppealManagementController {

    @Autowired
    private AppealService appealService;

    /**
     * 分页查询申诉列表
     */
    @PreAuthorize("@ss.resourceAuth()")
    @GetMapping(value = "/list", name = "申诉列表")
    public R list(AppealEntity entity) {
        PageUtils.startPage();
        PageResult<AppealEntity> page = appealService.page(entity);
        return R.ok().put(page);
    }

    /**
     * 申诉详情
     */
    @PreAuthorize("@ss.resourceAuth()")
    @GetMapping(value = "/{appealId}", name = "申诉详情")
    public R getInfo(@PathVariable Long appealId) {
        return R.ok(appealService.getById(appealId));
    }

    /**
     * 审核申诉
     */
    @PreAuthorize("@ss.resourceAuth()")
    @PutMapping(value = "/review", name = "审核申诉")
    public R review(@Validated @RequestBody AppealReviewVo vo) {
        return R.ok(appealService.reviewAppeal(vo.getAppealId(), vo.getDecision(), vo.getReviewComment()));
    }
}
