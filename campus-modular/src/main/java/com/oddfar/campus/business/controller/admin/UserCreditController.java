package com.oddfar.campus.business.controller.admin;

import com.oddfar.campus.business.domain.vo.CreditAdjustVo;
import com.oddfar.campus.business.service.UserCreditService;
import com.oddfar.campus.common.annotation.ApiResource;
import com.oddfar.campus.common.domain.R;
import com.oddfar.campus.common.enums.ResBizTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信用分管理
 */
@RestController
@RequestMapping("/admin/userCredit")
@ApiResource(name = "用户信用分管理", appCode = "campus", resBizType = ResBizTypeEnum.BUSINESS)
public class UserCreditController {

    @Autowired
    private UserCreditService userCreditService;

    /**
     * 查询用户信用分及变更日志
     */
    @PreAuthorize("@ss.resourceAuth()")
    @GetMapping(value = "/{userId}", name = "查询用户信用分")
    public R getInfo(@PathVariable Long userId) {
        int score = userCreditService.getCreditScore(userId);
        R r = R.ok();
        r.put("creditScore", score);
        r.put("creditLogs", userCreditService.getCreditLogs(userId));
        return r;
    }

    /**
     * 手动调整信用分
     */
    @PreAuthorize("@ss.resourceAuth()")
    @PutMapping(value = "/adjust", name = "调整信用分")
    public R adjust(@Validated @RequestBody CreditAdjustVo vo) {
        userCreditService.changeCredit(vo.getUserId(), vo.getDelta(), vo.getReason(), "manual", null);
        return R.ok();
    }
}
