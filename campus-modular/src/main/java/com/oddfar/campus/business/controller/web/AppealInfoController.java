package com.oddfar.campus.business.controller.web;

import com.oddfar.campus.business.domain.entity.AppealEntity;
import com.oddfar.campus.business.domain.vo.AppealSubmitVo;
import com.oddfar.campus.business.enums.CampusBizCodeEnum;
import com.oddfar.campus.business.service.AppealService;
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
 * 用户申诉
 */
@RestController
@RequestMapping("/campus")
@ApiResource(name = "申诉api", appCode = "campus", resBizType = ResBizTypeEnum.BUSINESS)
public class AppealInfoController {

    @Autowired
    private AppealService appealService;

    @PostMapping(value = "/submitAppeal", name = "提交申诉")
    @PreAuthorize("@ss.resourceAuth()")
    public R submitAppeal(@Validated @RequestBody AppealSubmitVo vo) {
        appealService.submitAppeal(vo);
        return R.ok();
    }

    @GetMapping(value = "/getOwnAppeals", name = "查询自己的申诉")
    @PreAuthorize("@ss.resourceAuth()")
    public R getOwnAppeals() {
        Long userId = SecurityUtils.getUserId();
        List<AppealEntity> list = appealService.getOwnAppeals(userId);
        return R.ok(list);
    }

    @GetMapping(value = "/getAppealDetail", name = "查询申诉详情")
    @PreAuthorize("@ss.resourceAuth()")
    public R getAppealDetail(@RequestParam Long appealId) {
        AppealEntity appeal = appealService.getById(appealId);
        if (appeal == null) {
            throw new ServiceException(CampusBizCodeEnum.APPEAL_NOT_EXIST.getMsg(),
                    CampusBizCodeEnum.APPEAL_NOT_EXIST.getCode());
        }
        // 验证是否是自己的申诉
        if (!appeal.getUserId().equals(SecurityUtils.getUserId())) {
            throw new ServiceException(CampusBizCodeEnum.APPEAL_NOT_OWN.getMsg(),
                    CampusBizCodeEnum.APPEAL_NOT_OWN.getCode());
        }
        return R.ok(appeal);
    }
}
