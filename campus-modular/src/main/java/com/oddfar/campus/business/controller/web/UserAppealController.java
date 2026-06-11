package com.oddfar.campus.business.controller.web;

import com.oddfar.campus.business.domain.vo.AppealSubmitVo;
import com.oddfar.campus.business.service.AppealService;
import com.oddfar.campus.common.annotation.ApiResource;
import com.oddfar.campus.common.domain.R;
import com.oddfar.campus.common.enums.ResBizTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户申诉接口
 */
@RestController
@RequestMapping("/campus/appeal")
@ApiResource(name = "用户申诉", appCode = "campus", resBizType = ResBizTypeEnum.BUSINESS)
public class UserAppealController {

    @Autowired
    private AppealService appealService;

    /**
     * 提交申诉
     */
    @PreAuthorize("@ss.resourceAuth()")
    @PostMapping(value = "/submit", name = "提交申诉")
    public R submit(@Validated @RequestBody AppealSubmitVo vo) {
        return R.ok(appealService.submitAppeal(vo.getContentId(), vo.getAppealReason()));
    }

    /**
     * 我的申诉列表
     */
    @PreAuthorize("@ss.resourceAuth()")
    @GetMapping(value = "/myAppeals", name = "我的申诉列表")
    public R myAppeals() {
        return R.ok(appealService.getMyAppeals());
    }
}
