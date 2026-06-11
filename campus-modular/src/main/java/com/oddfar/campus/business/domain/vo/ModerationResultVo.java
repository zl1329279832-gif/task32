package com.oddfar.campus.business.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ModerationResultVo {

    /** 审核决策: 0=待审, 1=通过, 3=拦截 */
    private int decision;

    /** 风险评分(0-100) */
    private int riskScore;

    /** 触发原因列表 */
    private List<String> reasons = new ArrayList<>();

    /** 命中的敏感词列表 */
    private List<String> hitWords = new ArrayList<>();
}
