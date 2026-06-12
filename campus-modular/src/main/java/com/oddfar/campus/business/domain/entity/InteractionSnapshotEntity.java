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

    /** 被推荐数 */
    private Long recommendCount;

    /** 被收藏数 */
    private Long favoriteCount;

    /** 被举报数 */
    private Long reportCount;

    /** 被搜索命中数 */
    private Long searchHitCount;

    /** 快照类型：TAKEDOWN/REJECT */
    private String snapshotType;

    /** 关联审核记录id */
    private Long moderationRecordId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private Long createUser;
}
