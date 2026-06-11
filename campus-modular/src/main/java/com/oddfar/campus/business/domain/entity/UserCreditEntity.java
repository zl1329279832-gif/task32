package com.oddfar.campus.business.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("campus_user_credit")
public class UserCreditEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("user_id")
    private Long userId;

    /** 信用分(0-100, 初始100) */
    private Integer creditScore;

    /** 累计违规次数 */
    private Integer violationCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
