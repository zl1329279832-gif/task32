package com.oddfar.campus.business.mapper;

import com.oddfar.campus.business.domain.entity.AppealEntity;
import com.oddfar.campus.common.core.BaseMapperX;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AppealMapper extends BaseMapperX<AppealEntity> {

    /**
     * 根据内容id和申诉状态查询
     */
    default List<AppealEntity> selectByContentIdAndStatus(Long contentId, Integer status) {
        return selectList(new LambdaQueryWrapperX<AppealEntity>()
                .eq(AppealEntity::getContentId, contentId)
                .eq(AppealEntity::getAppealStatus, status));
    }

    /**
     * 根据用户id查询申诉列表
     */
    default List<AppealEntity> selectByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<AppealEntity>()
                .eq(AppealEntity::getUserId, userId)
                .orderByDesc(AppealEntity::getCreateTime));
    }

    /**
     * 根据内容id查询是否存在待审或已通过的申诉
     */
    default Long selectPendingOrApprovedCount(Long contentId) {
        return selectCount(new LambdaQueryWrapperX<AppealEntity>()
                .eq(AppealEntity::getContentId, contentId)
                .in(AppealEntity::getAppealStatus, 0, 1));
    }

    /**
     * 查询某内容最近一次被拒绝的申诉
     */
    default AppealEntity selectLatestRejected(Long contentId) {
        return selectOne(new LambdaQueryWrapperX<AppealEntity>()
                .eq(AppealEntity::getContentId, contentId)
                .eq(AppealEntity::getAppealStatus, 2)
                .orderByDesc(AppealEntity::getCreateTime)
                .last("LIMIT 1"));
    }
}
