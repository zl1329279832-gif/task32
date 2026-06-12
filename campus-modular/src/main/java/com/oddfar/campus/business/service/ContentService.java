package com.oddfar.campus.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oddfar.campus.business.domain.entity.ContentEntity;
import com.oddfar.campus.business.domain.vo.ContentVo;
import com.oddfar.campus.business.domain.vo.SendContentVo;
import com.oddfar.campus.business.domain.entity.GovernanceBatchEntity;
import com.oddfar.campus.common.domain.PageResult;

import java.util.List;

/**
 * @author ningzhiyuan
 */
public interface ContentService extends IService<ContentEntity> {

    PageResult<ContentVo> page(ContentEntity contentEntity);

    /**
     * 最新墙
     *
     * @return
     */
    PageResult<ContentVo> newestPage();

    /**
     * 热门墙
     *
     * @return
     */
    PageResult<ContentVo> hotPage();


    /**
     * 查询某用户的点赞墙列表
     *
     * @return
     */
    PageResult<ContentVo> getLoveContentByUserId(Long userId);

    /**
     * 查询自己的信息墙列表
     *
     * @return
     */
    PageResult<ContentVo> getOwnContent();

    /**
     * 查询根据id校园墙内容
     *
     * @return 校园墙内容
     */
    ContentVo selectContentByContentId(ContentEntity contentEntity);


    /**
     * 用户发表信息墙
     *
     * @param sendContentVo
     * @return
     */
    int sendContent(SendContentVo sendContentVo);

    /**
     * 更新校园墙内容
     *
     * @param content 校园墙内容
     * @return
     */
    int updateContent(ContentEntity content);


    /**
     * 查询热门信息墙内容列表（热度根据点赞高低判断）
     *
     * @return
     */
    List<ContentEntity> getSimpleHotContent();

    /**
     * 根据ContentId查询信息墙内容简单文本列表
     *
     * @return
     */
    List<ContentEntity> getSimpleContentText(List<Long> contentIdList);

    /**
     * 删除信息墙
     *
     * @param contentId
     */
    void deleteContentById(Long contentId);

    /**
     * 删除自己的信息墙
     *
     * @param contentId
     */
    void deleteOwnContent(Long contentId);

    /**
     * 检测是否自己发布的信息墙
     *
     * @param contentId
     * @return
     */
    boolean checkOwnContent(Long contentId);

    /**
     * 统一恢复内容：恢复状态、解冻评论、清除附件违规、从快照恢复点赞统计、记录审核日志。
     * 幂等设计：若内容已经是正常状态(1)则跳过，不会重复回补。
     *
     * @param contentId 内容id
     * @param reason    恢复原因（用于审核日志）
     * @param source    来源标识：APPEAL / ADMIN_RESTORE
     */
    void restoreContent(Long contentId, String reason, String source);

    /**
     * 批量下架（带治理批次追踪）
     *
     * @param contentIds 内容id列表
     * @param reason     下架原因
     * @return 治理批次实体
     */
    GovernanceBatchEntity batchTakedown(List<Long> contentIds, String reason);
}
