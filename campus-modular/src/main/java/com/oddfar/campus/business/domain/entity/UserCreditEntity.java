package com.oddfar.campus.business.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.oddfar.campus.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户信用分
 */
@Data
@TableName("campus_user_credit")
@EqualsAndHashCode(callSuper = true)
public class UserCreditEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId("credit_id")
    private Long creditId;

    /** 用户id */
    private Long userId;

    /** 信用分 0-100，默认100 */
    private Integer creditScore;
}
