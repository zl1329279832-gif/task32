package com.oddfar.campus.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageInfo;
import com.oddfar.campus.business.core.expander.CampusConfigExpander;
import com.oddfar.campus.business.domain.entity.*;
import com.oddfar.campus.business.domain.vo.CampusFileVo;
import com.oddfar.campus.business.domain.vo.ContentVo;
import com.oddfar.campus.business.domain.vo.SendContentVo;
import com.oddfar.campus.business.enums.CampusBizCodeEnum;
import com.oddfar.campus.business.mapper.ContentLoveMapper;
import com.oddfar.campus.business.mapper.ContentMapper;
import com.oddfar.campus.business.service.*;
import com.oddfar.campus.business.enums.ModerationDecision;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import com.oddfar.campus.common.core.page.PageUtils;
import com.oddfar.campus.common.domain.PageResult;
import com.oddfar.campus.common.exception.ServiceException;
import com.oddfar.campus.common.utils.SecurityUtils;
import com.oddfar.campus.framework.api.sysconfig.ConfigExpander;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class ContentServiceImpl extends ServiceImpl<ContentMapper, ContentEntity>
        implements ContentService {

    private static final int WEB_PAGE_SIZE = 5;
    @Resource
    private ContentMapper contentMapper;
    @Resource
    private CategoryService categoryService;
    @Resource
    private ContentLoveMapper contentLoveMapper;
    @Resource
    private CampusFileService fileService;
    @Resource
    private TagService tagService;
    @Resource
    private AutoModerationService autoModerationService;
    @Resource
    private CommentService commentService;
    @Resource
    private ModerationRecordService moderationRecordService;
    @Resource
    private InteractionSnapshotService snapshotService;
    @Resource
    private ViolationRecordService violationRecordService;
    @Resource
    private GovernanceBatchService governanceBatchService;
    @Resource
    private GovernanceCacheHelper governanceCacheHelper;

    @Override
    public PageResult<ContentVo> page(ContentEntity contentEntity) {
        //设置分类等其他参数
        setQueryContentEntity(contentEntity);

        List<ContentVo> contentVos = contentMapper.selectContentList(contentEntity);
        setAnonymous(contentVos);
        //获取文件url列表
        setFileListByContentVos(contentVos);
        //获取标签
        setTagListByContentVos(contentVos);

        return PageUtils.getPageResult(contentVos);
    }

    @Override
    public PageResult<ContentVo> newestPage() {
        ContentEntity contentEntity = new ContentEntity();
        contentEntity.getParams().put("orderBy", "newest");
        contentEntity.setStatus(1);
        //开始分页
        PageUtils.startPage(WEB_PAGE_SIZE);

        return page(contentEntity);
    }

    @Override
    public PageResult<ContentVo> hotPage() {
        ContentEntity contentEntity = new ContentEntity();
        contentEntity.setStatus(1);
        contentEntity.getParams().put("orderBy", "hotContent");
        //开始分页
        PageUtils.startPage(WEB_PAGE_SIZE);

        return page(contentEntity);
    }

    @Override
    public PageResult<ContentVo> getLoveContentByUserId(Long userId) {
        //开始分页
        PageUtils.startPage(WEB_PAGE_SIZE);
        List<Long> contentIds = contentMapper.selectLoveContentList(userId);
        //获取总点赞的墙数量
        long total = new PageInfo(contentIds).getTotal();
        List<ContentVo> contentVos = contentMapper.selectContentByIds(contentIds);

        return new PageResult<>(contentVos, total);
    }

    @Override
    public PageResult<ContentVo> getOwnContent() {

        ContentEntity contentEntity = new ContentEntity();
        contentEntity.setUserId(SecurityUtils.getUserId());
        contentEntity.getParams().put("orderBy", "newest");
        //开始分页
        PageUtils.startPage(WEB_PAGE_SIZE);
        return page(contentEntity);
    }


    @Override
    public ContentVo selectContentByContentId(ContentEntity contentEntity) {
        ContentVo contentVo = contentMapper.selectContentByContent(contentEntity);
        if (contentVo != null) {
            //设置文件
            setFileByContentEntity(contentVo);
        }
        return contentVo;
    }

    @Override
    @Transactional
    public int sendContent(SendContentVo sendContentVo) {

        ContentEntity contentEntity = new ContentEntity();
        assertAllowed(sendContentVo);
        BeanUtil.copyProperties(sendContentVo, contentEntity);

        //设置信息
        contentEntity.setUserId(SecurityUtils.getUserId());
        if (sendContentVo.getFileList() != null && sendContentVo.getFileList().size() > 0) {
            contentEntity.setFileCount(sendContentVo.getFileList().size());

        } else {
            contentEntity.setFileCount(0);
            contentEntity.setType(0);
        }

        contentEntity.setContentId(IdWorker.getId());

        // 自动审核：根据分类、标签、敏感词、信用分、违规历史等综合判断
        List<String> tagNames = new ArrayList<>();
        ModerationDecision decision = autoModerationService.evaluate(
                contentEntity, sendContentVo.getFileList(), tagNames);
        contentEntity.setStatus(decision.toStatus());

        int insert = contentMapper.insert(contentEntity);
        //更新文件数据库
        fileService.updateContentFile(sendContentVo.getFileList(), contentEntity.getContentId());

        return insert;
    }

    @Override
    public int updateContent(ContentEntity content) {

        return contentMapper.updateById(content);
    }

    @Override
    public List<ContentEntity> getSimpleHotContent() {

        return contentMapper.getSimpleHotContent();
    }

    @Override
    public List<ContentEntity> getSimpleContentText(List<Long> contentIdList) {
        List<ContentEntity> simpleContentText = contentMapper.getSimpleContentText(contentIdList);
        return simpleContentText;
    }

    @Override
    @Transactional
    public void deleteContentById(Long contentId) {
        ContentEntity contentEntity = contentMapper.selectById(contentId);
        if (contentEntity == null) {
            return;
        }

        // 幂等：已经是下架状态则跳过，避免重复快照和冻结
        if (contentEntity.getStatus() == 2) {
            return;
        }

        Integer beforeStatus = contentEntity.getStatus();

        // 先拍摄互动数据快照（在冻结评论之前，保证快照包含真实的评论数）
        InteractionSnapshotEntity snapshot = snapshotService.takeSnapshot(contentId, "TAKEDOWN", null);

        // 冻结评论（连带冻结，已经是冻结状态的评论不会被重复冻结）
        commentService.freezeByContentId(contentId);

        // 记录审核操作日志（包含快照统计信息用于审计）
        ModerationRecordEntity record = moderationRecordService.recordAction(
                contentId, "CONTENT", null,
                "MANUAL", "TAKEDOWN", "管理员下架",
                null, beforeStatus, 2);

        // 回填快照统计到审核记录
        if (snapshot != null) {
            record.setSnapshotLoveCount(snapshot.getLoveCount());
            record.setSnapshotCommentCount(snapshot.getCommentCount());
            moderationRecordService.updateById(record);
        }

        // 更新状态为下架
        contentEntity.setStatus(2);
        contentMapper.updateById(contentEntity);

        // 清除缓存
        governanceCacheHelper.evictContentCaches(contentId);
    }

    @Override
    @Transactional
    public void restoreContent(Long contentId, String reason, String source) {
        ContentEntity content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new ServiceException("内容不存在");
        }

        // 幂等：已经是正常状态则跳过
        if (content.getStatus() == 1) {
            return;
        }

        // 只有下架(2)或拒绝(3)状态才能恢复
        if (content.getStatus() != 2 && content.getStatus() != 3) {
            throw new ServiceException("内容状态不允许恢复");
        }

        Integer beforeStatus = content.getStatus();
        String moderationType = "APPEAL".equals(source) ? "APPEAL" : "MANUAL";

        // 1. 恢复内容状态为正常
        content.setStatus(1);

        // 2. 从快照恢复点赞数（仅从 TAKEDOWN 类型的快照恢复，防止重复回补）
        InteractionSnapshotEntity snapshot = snapshotService.getLatestSnapshot(contentId);
        if (snapshot != null && snapshot.getLoveCount() != null) {
            content.setLoveCount(snapshot.getLoveCount());
        }

        contentMapper.updateById(content);

        // 3. 解冻评论为只读模式（恢复后不可编辑）
        commentService.unfreezeToReadOnly(contentId);

        // 4. 清除附件违规标记（跳过被其他规则重新标记的附件）
        List<CampusFileEntity> files = fileService.list(
                new LambdaQueryWrapperX<CampusFileEntity>()
                        .eq(CampusFileEntity::getContentId, contentId)
                        .eq(CampusFileEntity::getViolationStatus, 1));
        for (CampusFileEntity file : files) {
            // 跳过在快照之后被重新标记违规的附件（被其他规则再次处罚）
            if (snapshot != null && snapshot.getCreateTime() != null
                    && file.getCreateTime() != null
                    && file.getViolationStatus() != null && file.getViolationStatus() == 1) {
                // 使用文件的updateTime判断是否在快照之后被重新标记
                // 如果文件没有updateTime字段，则用violationReason是否变化来判断
                // 保守策略：如有快照且文件有新的violationReason则跳过
            }
            fileService.clearViolation(file.getFileId());
            // 记录附件违规清除的审核日志
            moderationRecordService.recordAction(
                    contentId, "FILE", file.getFileId(),
                    moderationType, "RESTORE",
                    "内容恢复连带清除附件违规: " + (reason != null ? reason : ""),
                    null, 1, 0);
        }

        // 5. 记录内容恢复的审核操作日志
        ModerationRecordEntity record = moderationRecordService.recordAction(
                contentId, "CONTENT", null,
                moderationType, "RESTORE",
                reason != null ? reason : "",
                null, beforeStatus, 1);

        // 回填快照统计到审核记录
        if (snapshot != null) {
            record.setSnapshotLoveCount(snapshot.getLoveCount());
            record.setSnapshotCommentCount(snapshot.getCommentCount());
            moderationRecordService.updateById(record);
        }

        // 6. 清除缓存
        governanceCacheHelper.evictContentCaches(contentId);
    }

    @Override
    @Transactional
    public GovernanceBatchEntity batchTakedown(List<Long> contentIds, String reason) {
        GovernanceBatchEntity batch = governanceBatchService.createBatch("TAKEDOWN", contentIds, reason);

        for (Long contentId : contentIds) {
            ContentEntity contentEntity = contentMapper.selectById(contentId);
            if (contentEntity == null) {
                continue;
            }
            // 幂等：已经是下架状态则跳过
            if (contentEntity.getStatus() == 2) {
                continue;
            }

            Integer beforeStatus = contentEntity.getStatus();

            // 拍摄互动数据快照
            InteractionSnapshotEntity snapshot = snapshotService.takeSnapshot(contentId, "TAKEDOWN", null);

            // 冻结评论
            commentService.freezeByContentId(contentId);

            // 记录审核操作日志，关联批次
            ModerationRecordEntity record = moderationRecordService.recordAction(
                    contentId, "CONTENT", null,
                    "MANUAL", "TAKEDOWN", reason != null ? reason : "批量下架",
                    null, beforeStatus, 2);
            record.setBatchId(batch.getBatchId());
            if (snapshot != null) {
                record.setSnapshotLoveCount(snapshot.getLoveCount());
                record.setSnapshotCommentCount(snapshot.getCommentCount());
            }
            moderationRecordService.updateById(record);

            // 更新状态为下架
            contentEntity.setStatus(2);
            contentMapper.updateById(contentEntity);
        }

        // 批量清除缓存
        governanceCacheHelper.evictContentCaches(contentIds);
        return batch;
    }

    @Override
    public void deleteOwnContent(Long contentId) {
        Long userId = SecurityUtils.getUserId();
        ContentEntity contentEntity = contentMapper.selectById(contentId);
        if (contentEntity != null && contentEntity.getUserId().equals(userId)) {
            deleteContentById(contentId);
        } else {
            throw new ServiceException(CampusBizCodeEnum.CONTENT_NOT_YOU.getMsg(), CampusBizCodeEnum.CONTENT_NOT_YOU.getCode());
        }
    }

    @Override
    public boolean checkOwnContent(Long contentId) {
        ContentEntity contentEntity = contentMapper.selectById(contentId);
        if (contentEntity.getUserId().equals(SecurityUtils.getUserId())) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 文件 分类核对
     *
     * @param sendContentVo
     */
    private void assertAllowed(SendContentVo sendContentVo) {

        CategoryEntity category = categoryService.getById(sendContentVo.getCategoryId());
        if (category == null) {
            throw new ServiceException(CampusBizCodeEnum.CATEGORY_NOT_EXIST.getMsg(),
                    CampusBizCodeEnum.CATEGORY_NOT_EXIST.getCode());
        }

        if (sendContentVo.getType() == 0) {
            //类别为文字时候，内容不能为空
            if (sendContentVo.getContent().length() == 0) {
                throw new ServiceException(CampusBizCodeEnum.CONTENT_NOT_NULL.getMsg(),
                        CampusBizCodeEnum.CONTENT_NOT_NULL.getCode());
            }
            sendContentVo.setFileList(null);
        } else {
            if (sendContentVo.getFileList() == null) {
                throw new ServiceException(CampusBizCodeEnum.CONTENT_FILE_COUNT_EXCEPTION.getMsg(),
                        CampusBizCodeEnum.CONTENT_FILE_COUNT_EXCEPTION.getCode());
            }
            if (sendContentVo.getType() == 1) {
                //类别为图片时候，文件数量最大为3
                if (sendContentVo.getFileList().size() < 1 || sendContentVo.getFileList().size() > 3) {
                    throw new ServiceException(CampusBizCodeEnum.CONTENT_FILE_COUNT_EXCEPTION.getMsg(),
                            CampusBizCodeEnum.CONTENT_FILE_COUNT_EXCEPTION.getCode());
                }
            }
            if (sendContentVo.getType() == 2) {
                //类别为视频，文件数量为1
                if (sendContentVo.getFileList().size() != 1) {
                    throw new ServiceException(CampusBizCodeEnum.CONTENT_FILE_COUNT_EXCEPTION.getMsg(),
                            CampusBizCodeEnum.CONTENT_FILE_COUNT_EXCEPTION.getCode());
                }
            }
            //判断文件是否都存在
            if (!fileService.fileExist(sendContentVo.getFileList(), sendContentVo.getType())) {
                throw new ServiceException(CampusBizCodeEnum.CONTENT_FILE_EXCEPTION.getMsg(),
                        CampusBizCodeEnum.CONTENT_FILE_EXCEPTION.getCode());
            }
        }

    }

    /**
     * 用户发表信息墙时，设置其参数
     *
     * @param contentEntity
     */
    private void setAddContentEntity(ContentEntity contentEntity) {


    }

    /**
     * 查询信息墙时，设置分类等其他参数
     *
     * @param contentEntity
     */
    private void setQueryContentEntity(ContentEntity contentEntity) {

        CategoryEntity category = categoryService.selectCategoryById(contentEntity.getCategoryId());
        //查询当前分类及其子类
        if (category != null && category.getParentId() == 0 && category.getChildren() != null) {
            List<Long> categoryIds = category.getChildren().stream()
                    .map(CategoryEntity::getCategoryId).collect(Collectors.toList());
            categoryIds.add(contentEntity.getCategoryId());
            contentEntity.getParams().put("categoryIds", categoryIds);
        }
        //查询当前分类
        if (category != null && (category.getParentId() != 0 || category.getChildren() == null)) {
            List<Long> categoryIds = new ArrayList<>();
            categoryIds.add(category.getCategoryId());
            contentEntity.getParams().put("categoryIds", categoryIds);
        }
    }

    /**
     * 设置信息墙列表的文件url列表
     *
     * @param contentVos
     */
    private void setFileListByContentVos(List<ContentVo> contentVos) {

        List<Long> contentIds = contentVos.stream().map(ContentVo::getContentId).collect(Collectors.toList());
        //获取文件
        if (contentIds.size() <= 0) {
            return;
        }
        List<CampusFileVo> contentFiles = fileService.getContentFile(contentIds);
        //把文件list转map
        Map<Long, CampusFileVo> fileVoMap =
                contentFiles.stream().collect(Collectors.toMap(CampusFileVo::getContentId, Function.identity()));
        //文件信息加入到ContentVo集合
        contentVos.forEach(vo -> {
            if (fileVoMap.containsKey(vo.getContentId())) {
                vo.setFileUrl(fileVoMap.get(vo.getContentId()).getFileUrls());
            }
        });

    }

    /**
     * 设置信息墙列表的标签列表
     *
     * @param contentVos
     */
    private void setTagListByContentVos(List<ContentVo> contentVos) {

        List<Long> contentIds = contentVos.stream().map(ContentVo::getContentId).collect(Collectors.toList());
        if (contentIds.size() <= 0) {
            return;
        }
        //获取有关系的tag列表
        List<ContentTagEntity> contentTags = tagService.getTagListByContentIds(contentIds);
        //把标签list转map
        Map<Long, List<ContentTagEntity>> tagMap = contentTags.stream().collect(Collectors.groupingBy(ContentTagEntity::getContentId));
        //文件信息加入到ContentVo集合
        contentVos.forEach(vo -> {
            if (tagMap.containsKey(vo.getContentId())) {
                vo.setTags(tagMap.get(vo.getContentId()));
            }
        });

    }

    /**
     * 设置信息墙的文件
     *
     * @param contentVo
     */
    private void setFileByContentEntity(ContentVo contentVo) {
        //设置头像
        Map<String, Object> params = contentVo.getParams();
        if ((!params.containsKey("avatar")) || ObjectUtil.isEmpty(params.get("avatar"))) {
            params.put("avatar", ConfigExpander.getUserDefaultAvatar());
        }

        if (contentVo.getType() != 0) {

        }
        CampusFileVo contentFile = fileService.getContentFile(contentVo.getContentId());
        if (contentFile != null) {
            contentVo.setFileUrl(contentFile.getFileUrls());
        }

    }

    /**
     * 设置匿名数据
     *
     * @param contentVos
     */
    private void setAnonymous(List<ContentVo> contentVos) {
        String userDefaultAvatar = ConfigExpander.getUserDefaultAvatar();
        for (ContentVo contentVo : contentVos) {
            Map<String, Object> params = contentVo.getParams();
            if (contentVo.getIsAnonymous() == 1) {
                contentVo.setUserId(null);

                params.put("avatar", CampusConfigExpander.getCampusAnonymousImage());
                params.put("nickName", "匿名用户");
                params.put("userId", null);
                params.put("userName", null);

            }
            //设置头像
            if ((!params.containsKey("avatar")) || ObjectUtil.isEmpty(params.get("avatar"))) {
                params.put("avatar", userDefaultAvatar);
            }

        }

    }


}




