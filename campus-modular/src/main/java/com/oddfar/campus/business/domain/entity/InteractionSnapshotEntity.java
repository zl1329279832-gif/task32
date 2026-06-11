package com.oddfar.campus.business.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 互动数据快照（用于下架后恢复）
 */
@Data
@TableName("campus_interaction_snapshot")
public class InteractionSnapshotEntity {
    private static final long serialVersionUID = 1L;

    @TableId("snapshot_id")
    private Long snapshotId;

    /** 内容id */
    private Long contentId;

    /** 快照时的点赞数 */
    private Long loveCount;

    /** 快照时的评论数 */
    private Long commentCount;

    /** 快照类型：TAKEDOWN/REJECT */
    private String snapshotType;

    /** 关联审核记录id */
    private Long moderationRecordId;

    /** 是否已消费（用于防止重复回补）：0=未消费, 1=已消费 */
    private Integer consumed;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private Long createUser;
}
