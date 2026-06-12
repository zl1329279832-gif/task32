package com.oddfar.campus.business.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddfar.campus.business.domain.entity.CommentEntity;
import com.oddfar.campus.business.domain.entity.ContentEntity;
import com.oddfar.campus.business.domain.vo.CommentVo;
import com.oddfar.campus.business.enums.CampusBizCodeEnum;
import com.oddfar.campus.business.mapper.CommentMapper;
import com.oddfar.campus.business.mapper.ContentMapper;
import com.oddfar.campus.business.service.AutoModerationService;
import com.oddfar.campus.business.service.CommentService;
import com.oddfar.campus.business.enums.ModerationDecision;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import com.oddfar.campus.common.core.page.PageUtils;
import com.oddfar.campus.common.domain.PageResult;
import com.oddfar.campus.common.exception.ServiceException;
import com.oddfar.campus.common.utils.SecurityUtils;
import com.oddfar.campus.common.utils.ServletUtils;
import com.oddfar.campus.common.utils.StringUtils;
import com.oddfar.campus.common.utils.ip.AddressUtils;
import com.oddfar.campus.common.utils.ip.IpUtils;
import com.oddfar.campus.framework.api.sysconfig.ConfigExpander;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, CommentEntity>
        implements CommentService {

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private ContentMapper contentMapper;
    @Autowired
    private AutoModerationService autoModerationService;

    @Override
    public PageResult<CommentEntity> page(CommentEntity comment) {
        return commentMapper.selectPage(comment);
    }

    @Override
    public PageResult<CommentVo> selectOneLevel(CommentEntity comment) {
        //设置分页
//        int firstIndex = (comment.getPageNum() - 1) * comment.getPageSize();
//        comment.setPageNum(firstIndex);

        ContentEntity content = contentMapper.selectById(comment.getContentId());

        if (content != null) {
            comment.setCommentId(content.getContentId());
            //开始分页，固定大小5
            PageUtils.startPage(5);
            //获取一级评论
            List<CommentVo> oneLevel = commentMapper.getOneLevel(comment);
            //查询一级评论中的子评论有作者的
            if (oneLevel != null && oneLevel.size() > 0) {
                List<Long> commentIdList = oneLevel.stream().map(CommentVo::getCommentId).collect(Collectors.toList());
                List<CommentVo> oneLevelChild = commentMapper.getOneLevelChildHaveAuthor(commentIdList, content.getUserId());
                oneLevel.addAll(oneLevelChild);
            }

            setCommentOther(oneLevel);

            //封装分页数据
//            return new PageResult<CommentVo>(oneLevel, selectCommentCount(content.getContentId()));
            return PageUtils.getPageResult(oneLevel);
        } else {
            return null;
        }

    }

    @Override
    public PageResult<CommentVo> selectOneLevelChild(CommentEntity comment) {
        //开始分页，固定大小5
        PageUtils.startPage(5);
        List<CommentVo> oneLevelChild = commentMapper.getOneLevelChild(comment);
        setCommentOther(oneLevelChild);
        //封装分页数据
        return PageUtils.getPageResult(oneLevelChild);
    }

    @Override
    public List<CommentVo> selectOneLevelChildList(Long commentId) {
        CommentEntity commentEntity = commentMapper.selectById(commentId);
        if (commentEntity == null) {
            throw new ServiceException(CampusBizCodeEnum.COMMENT_IS_NULL.getMsg(), CampusBizCodeEnum.COMMENT_IS_NULL.getCode());
        }
        List<CommentVo> oneLevelChild = null;
        if (commentEntity.getOneLevelId() == -1) {
            oneLevelChild = commentMapper.getOneLevelChildList(commentEntity.getCommentId(), commentId);
        } else {
            oneLevelChild = commentMapper.getOneLevelChildList(commentId, commentEntity.getOneLevelId());
        }
        setCommentOther(oneLevelChild);

        return oneLevelChild;
    }

    @Override
    public CommentVo selectCommentVo(Long commentId) {
        return commentMapper.selectCommentVo(commentId);
    }

    @Override
    public List<CommentVo> selectOwnComment() {

        List<CommentVo> commentVos = commentMapper.selectOwnComment(SecurityUtils.getUserId());

        return commentVos;
    }

    @Override
    public Long selectCommentCount(Long contentId) {
        return commentMapper.getCommentCount( contentId);
    }

    @Override
    public Long insertComment(CommentEntity comment) {
        Long userId = SecurityUtils.getUserId();
        if (comment.getContentId() != null) {
            //添加一级评论
            ContentEntity contentEntity = contentMapper.selectById(comment.getContentId());
            if (contentEntity == null || contentEntity.getStatus() != 1) {
                throw new ServiceException(CampusBizCodeEnum.CONTENT_OPERATION_PROHIBITED.getMsg(),
                        CampusBizCodeEnum.CONTENT_OPERATION_PROHIBITED.getCode());
            }

            // 自动审核评论内容
            ModerationDecision commentDecision = autoModerationService.evaluateComment(comment);
            if (commentDecision == ModerationDecision.BLOCK) {
                throw new ServiceException(CampusBizCodeEnum.COMMENT_BLOCKED.getMsg(),
                        CampusBizCodeEnum.COMMENT_BLOCKED.getCode());
            }

        } else {
            //给评论添加评论
            CommentEntity commentEntity = commentMapper.selectById(comment.getCommentId());
            if (commentEntity == null) {
                throw new ServiceException(CampusBizCodeEnum.COMMENT_IS_NULL.getMsg(),
                        CampusBizCodeEnum.COMMENT_IS_NULL.getCode());
            }

            // 只读评论不允许回复
            if (commentEntity.getReadOnlyStatus() != null && commentEntity.getReadOnlyStatus() == 1) {
                throw new ServiceException(CampusBizCodeEnum.COMMENT_READ_ONLY.getMsg(),
                        CampusBizCodeEnum.COMMENT_READ_ONLY.getCode());
            }

            comment.setParentId(commentEntity.getCommentId());
            //设置一级评论id
            if (commentEntity.getParentId() == 0) {
                comment.setOneLevelId(commentEntity.getCommentId());
                comment.setToUserId(null);
            } else {
                comment.setOneLevelId(commentEntity.getOneLevelId());
                comment.setToUserId(commentEntity.getUserId());
            }

            comment.setContentId(commentEntity.getContentId());
        }
        comment.setCommentId(IdWorker.getId());
        comment.setCommentId(IdWorker.getId());
        comment.setUserId(userId);
        comment.setIp(IpUtils.getIpAddr(ServletUtils.getRequest()));
        comment.setAddress(AddressUtils.getRealAddressByIP(comment.getIp()));
        commentMapper.insert(comment);
        return comment.getCommentId();
    }

    @Override
    public int updateComment(CommentEntity comment) {
        // 只读评论不允许编辑
        CommentEntity existing = commentMapper.selectById(comment.getCommentId());
        if (existing != null && existing.getReadOnlyStatus() != null && existing.getReadOnlyStatus() == 1) {
            throw new ServiceException(CampusBizCodeEnum.COMMENT_READ_ONLY.getMsg(),
                    CampusBizCodeEnum.COMMENT_READ_ONLY.getCode());
        }
        return commentMapper.updateById(comment);
    }

    @Override
    public int delOwnComment(Long commentId) {
        CommentEntity commentEntity = commentMapper.selectById(commentId);
        if (commentEntity != null && commentEntity.getUserId().equals(SecurityUtils.getUserId())) {
            return commentMapper.deleteById(commentId);
        } else {
            throw new ServiceException(CampusBizCodeEnum.COMMENT_DEL_ERR.getMsg(), CampusBizCodeEnum.COMMENT_DEL_ERR.getCode());
        }
    }

    private void setCommentOther(List<CommentVo> commentVos) {
        String userDefaultAvatar = ConfigExpander.getUserDefaultAvatar();
        commentVos.stream().forEach(commentVo -> {
            if (StringUtils.isEmpty(commentVo.getAvatar())) {
                commentVo.setAvatar(userDefaultAvatar);
            }
        });

    }

    @Override
    public int freezeByContentId(Long contentId) {
        CommentEntity update = new CommentEntity();
        update.setFrozenStatus(1);
        return commentMapper.update(update, new LambdaQueryWrapperX<CommentEntity>()
                .eq(CommentEntity::getContentId, contentId)
                .eq(CommentEntity::getFrozenStatus, 0));
    }

    @Override
    public int unfreezeByContentId(Long contentId) {
        CommentEntity update = new CommentEntity();
        update.setFrozenStatus(0);
        return commentMapper.update(update, new LambdaQueryWrapperX<CommentEntity>()
                .eq(CommentEntity::getContentId, contentId)
                .eq(CommentEntity::getFrozenStatus, 1));
    }

    @Override
    public int unfreezeToReadOnly(Long contentId) {
        CommentEntity update = new CommentEntity();
        update.setFrozenStatus(0);
        update.setReadOnlyStatus(1);
        return commentMapper.update(update, new LambdaQueryWrapperX<CommentEntity>()
                .eq(CommentEntity::getContentId, contentId)
                .eq(CommentEntity::getFrozenStatus, 1));
    }
}




