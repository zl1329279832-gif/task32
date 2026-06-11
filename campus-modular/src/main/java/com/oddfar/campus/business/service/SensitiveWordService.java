package com.oddfar.campus.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oddfar.campus.business.domain.entity.SensitiveWordEntity;
import com.oddfar.campus.common.domain.PageResult;

import java.util.List;

/**
 * 敏感词服务
 */
public interface SensitiveWordService extends IService<SensitiveWordEntity> {

    /**
     * 检测文本中的敏感词
     *
     * @param text 待检测文本
     * @return 匹配到的敏感词及其严重度
     */
    List<SensitiveWordMatch> detectSensitiveWords(String text);

    /**
     * 重新加载敏感词缓存
     */
    void reloadCache();

    /**
     * 分页查询
     */
    PageResult<SensitiveWordEntity> page(SensitiveWordEntity entity);

    /**
     * 敏感词匹配结果
     */
    class SensitiveWordMatch {
        private String word;
        private Integer severity;
        private String category;

        public SensitiveWordMatch(String word, Integer severity, String category) {
            this.word = word;
            this.severity = severity;
            this.category = category;
        }

        public String getWord() { return word; }
        public Integer getSeverity() { return severity; }
        public String getCategory() { return category; }
    }
}
