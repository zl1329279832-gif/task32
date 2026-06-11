package com.oddfar.campus.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oddfar.campus.business.domain.entity.SensitiveWordEntity;
import com.oddfar.campus.common.domain.PageResult;

import java.util.List;

public interface SensitiveWordService extends IService<SensitiveWordEntity> {

    PageResult<SensitiveWordEntity> page(SensitiveWordEntity word);

    int insertWord(SensitiveWordEntity word);

    int updateWord(SensitiveWordEntity word);

    int deleteWordByIds(Long[] wordIds);

    /**
     * 检测文本中的敏感词
     * @return 命中的敏感词列表
     */
    List<String> checkText(String text);

    /**
     * 获取命中敏感词的最高等级
     */
    int getMaxHitLevel(String text);

    /**
     * 刷新敏感词缓存
     */
    void refreshCache();
}
