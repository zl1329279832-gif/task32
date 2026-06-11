package com.oddfar.campus.business.controller.admin;

import com.oddfar.campus.business.domain.entity.SensitiveWordEntity;
import com.oddfar.campus.business.service.SensitiveWordService;
import com.oddfar.campus.common.annotation.ApiResource;
import com.oddfar.campus.common.core.page.PageUtils;
import com.oddfar.campus.common.domain.PageResult;
import com.oddfar.campus.common.domain.R;
import com.oddfar.campus.common.enums.ResBizTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 敏感词管理
 */
@RestController
@RequestMapping("/admin/sensitiveWord")
@ApiResource(name = "敏感词管理", appCode = "campus", resBizType = ResBizTypeEnum.BUSINESS)
public class SensitiveWordController {

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @PreAuthorize("@ss.resourceAuth()")
    @GetMapping(value = "/list", name = "敏感词列表")
    public R list(SensitiveWordEntity entity) {
        PageUtils.startPage();
        PageResult<SensitiveWordEntity> page = sensitiveWordService.page(entity);
        return R.ok().put(page);
    }

    @PreAuthorize("@ss.resourceAuth()")
    @PostMapping(value = "", name = "新增敏感词")
    public R add(@RequestBody SensitiveWordEntity entity) {
        return R.ok(sensitiveWordService.save(entity));
    }

    @PreAuthorize("@ss.resourceAuth()")
    @PutMapping(value = "", name = "修改敏感词")
    public R edit(@RequestBody SensitiveWordEntity entity) {
        return R.ok(sensitiveWordService.updateById(entity));
    }

    @PreAuthorize("@ss.resourceAuth()")
    @DeleteMapping(value = "/{wordIds}", name = "删除敏感词")
    public R remove(@PathVariable Long[] wordIds) {
        for (Long wordId : wordIds) {
            sensitiveWordService.removeById(wordId);
        }
        // 删除后刷新缓存
        sensitiveWordService.reloadCache();
        return R.ok();
    }

    /**
     * 刷新敏感词缓存
     */
    @PreAuthorize("@ss.resourceAuth()")
    @PostMapping(value = "/reload", name = "刷新敏感词缓存")
    public R reload() {
        sensitiveWordService.reloadCache();
        return R.ok();
    }

    /**
     * 测试文本敏感词检测
     */
    @PreAuthorize("@ss.resourceAuth()")
    @PostMapping(value = "/test", name = "测试敏感词检测")
    public R testText(@RequestParam String text) {
        List<SensitiveWordService.SensitiveWordMatch> matches = sensitiveWordService.detectSensitiveWords(text);
        return R.ok(matches);
    }
}
