package com.oddfar.campus.business.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.oddfar.campus.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("campus_sensitive_word")
@EqualsAndHashCode(callSuper = true)
public class SensitiveWordEntity extends BaseEntity {

    @TableId("word_id")
    private Long wordId;

    /** 敏感词 */
    private String word;

    /** 分类(politics/porn/abuse/ad/default) */
    private String category;

    /** 严重等级: 1=低, 2=中, 3=高(直接拦截) */
    private Integer level;

    /** 状态(0正常 1停用) */
    private String status;
}
