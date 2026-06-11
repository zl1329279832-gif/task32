package com.oddfar.campus.business.controller.admin;

import com.oddfar.campus.business.domain.entity.ModerationRuleEntity;
import com.oddfar.campus.business.service.ModerationRuleService;
import com.oddfar.campus.common.annotation.ApiResource;
import com.oddfar.campus.common.core.page.PageUtils;
import com.oddfar.campus.common.domain.PageResult;
import com.oddfar.campus.common.domain.R;
import com.oddfar.campus.common.enums.ResBizTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 审核规则管理
 */
@RestController
@RequestMapping("/admin/moderationRule")
@ApiResource(name = "审核规则管理", appCode = "campus", resBizType = ResBizTypeEnum.BUSINESS)
public class ModerationRuleController {

    @Autowired
    private ModerationRuleService moderationRuleService;

    @PreAuthorize("@ss.resourceAuth()")
    @GetMapping(value = "/list", name = "规则列表")
    public R list(ModerationRuleEntity entity) {
        PageUtils.startPage();
        PageResult<ModerationRuleEntity> page = moderationRuleService.page(entity);
        return R.ok().put(page);
    }

    @PreAuthorize("@ss.resourceAuth()")
    @PostMapping(value = "", name = "新增规则")
    public R add(@RequestBody ModerationRuleEntity entity) {
        return R.ok(moderationRuleService.save(entity));
    }

    @PreAuthorize("@ss.resourceAuth()")
    @PutMapping(value = "", name = "修改规则")
    public R edit(@RequestBody ModerationRuleEntity entity) {
        return R.ok(moderationRuleService.updateById(entity));
    }

    @PreAuthorize("@ss.resourceAuth()")
    @DeleteMapping(value = "/{ruleIds}", name = "删除规则")
    public R remove(@PathVariable Long[] ruleIds) {
        for (Long ruleId : ruleIds) {
            moderationRuleService.removeById(ruleId);
        }
        return R.ok();
    }
}
