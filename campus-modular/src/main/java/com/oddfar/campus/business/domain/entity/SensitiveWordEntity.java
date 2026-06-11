package com.oddfar.campus.business.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.oddfar.campus.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 敏感词库
 */
@Data
@TableName("campus_sensitive_word")
@EqualsAndHashCode(callSuper = true)
public class SensitiveWordEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId("word_id")
    private Long wordId;

    /** 敏感词 */
    private String word;

    /** 分类：politics/porn/abuse/ad/custom */
    private String category;

    /** 严重度：1=低, 2=中, 3=高 */
    private Integer severity;

    /** 状态：0=启用, 1=禁用 */
    private String status;
}
