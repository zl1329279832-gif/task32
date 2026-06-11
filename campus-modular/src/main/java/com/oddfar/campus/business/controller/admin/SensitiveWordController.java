package com.oddfar.campus.business.controller.admin;

import com.oddfar.campus.business.domain.entity.SensitiveWordEntity;
import com.oddfar.campus.business.service.SensitiveWordService;
import com.oddfar.campus.common.annotation.ApiResource;
import com.oddfar.campus.common.domain.PageResult;
import com.oddfar.campus.common.domain.R;
import com.oddfar.campus.common.enums.ResBizTypeEnum;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 敏感词管理
 */
@RestController
@RequestMapping("/admin/sensitive-word")
@ApiResource(name = "敏感词管理", appCode = "campus", resBizType = ResBizTypeEnum.BUSINESS)
public class SensitiveWordController {

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @GetMapping(value = "list", name = "敏感词列表")
    @PreAuthorize("@ss.resourceAuth()")
    public R list(SensitiveWordEntity word) {
        PageResult<SensitiveWordEntity> page = sensitiveWordService.page(word);
        return R.ok().put(page);
    }

    @GetMapping(value = "/{wordId}", name = "敏感词详情")
    @PreAuthorize("@ss.resourceAuth()")
    public R getInfo(@PathVariable Long wordId) {
        return R.ok(sensitiveWordService.getById(wordId));
    }

    @PostMapping(value = "", name = "新增敏感词")
    @PreAuthorize("@ss.resourceAuth()")
    public R add(@Validated @RequestBody SensitiveWordEntity word) {
        word.setWordId(IdWorker.getId());
        return R.ok(sensitiveWordService.insertWord(word));
    }

    @PutMapping(value = "", name = "修改敏感词")
    @PreAuthorize("@ss.resourceAuth()")
    public R edit(@Validated @RequestBody SensitiveWordEntity word) {
        return R.ok(sensitiveWordService.updateWord(word));
    }

    @DeleteMapping(value = "/{wordIds}", name = "删除敏感词")
    @PreAuthorize("@ss.resourceAuth()")
    public R remove(@PathVariable Long[] wordIds) {
        return R.ok(sensitiveWordService.deleteWordByIds(wordIds));
    }

    @PostMapping(value = "/refreshCache", name = "刷新敏感词缓存")
    @PreAuthorize("@ss.resourceAuth()")
    public R refreshCache() {
        sensitiveWordService.refreshCache();
        return R.ok();
    }
}
