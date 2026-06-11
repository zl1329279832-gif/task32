package com.oddfar.campus.business.enums;

/**
 * 审核决策枚举
 * 与 ContentEntity.status 对齐：PASS→1, PENDING→0, BLOCK→3
 */
public enum ModerationDecision {

    PASS(1, "直接发布"),
    PENDING(0, "待审核"),
    BLOCK(3, "拦截");

    private final int status;
    private final String description;

    ModerationDecision(int status, String description) {
        this.status = status;
        this.description = description;
    }

    public int toStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 取最严格的决策（ordinal越大越严格）
     */
    public static ModerationDecision worst(ModerationDecision a, ModerationDecision b) {
        return a.ordinal() > b.ordinal() ? a : b;
    }
}
