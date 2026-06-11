package com.oddfar.campus.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddfar.campus.business.domain.entity.*;
import com.oddfar.campus.business.domain.vo.AppealReviewVo;
import com.oddfar.campus.business.domain.vo.AppealSubmitVo;
import com.oddfar.campus.business.enums.CampusBizCodeEnum;
import com.oddfar.campus.business.mapper.AppealMapper;
import com.oddfar.campus.business.service.*;
import com.oddfar.campus.common.domain.PageResult;
import com.oddfar.campus.common.exception.ServiceException;
import com.oddfar.campus.common.core.page.PageUtils;
import com.oddfar.campus.common.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class AppealServiceImpl extends ServiceImpl<AppealMapper, AppealEntity>
        implements AppealService {

    @Resource
    private AppealMapper appealMapper;

    @Resource
    private ContentService contentService;

    @Resource
    private UserCreditService userCreditService;

    @Resource
    private ModerationRecordService moderationRecordService;

    @Resource
    private ViolationRecordService violationRecordService;

    @Override
    public PageResult<AppealEntity> page(AppealEntity appeal) {
        PageUtils.startPage();
        List<AppealEntity> list = appealMapper.selectAppealPage(appeal);
        return PageUtils.getPageResult(list);
    }

    @Override
    @Transactional
    public void submitAppeal(AppealSubmitVo vo) {
        Long userId = SecurityUtils.getUserId();

        // 校验：是否存在进行中的申诉
        if (hasActiveAppeal(vo.getTargetType(), vo.getTargetId())) {
            throw new ServiceException(CampusBizCodeEnum.APPEAL_ALREADY_EXISTS.getMsg(), CampusBizCodeEnum.APPEAL_ALREADY_EXISTS.getCode());
        }

        // 校验：内容必须是被下架或被拒绝的状态
        if (vo.getTargetType() == 1) {
            ContentEntity content = contentService.getById(vo.getTargetId());
            if (content == null) {
                throw new ServiceException(CampusBizCodeEnum.CONTENT_IS_NULL.getMsg(), CampusBizCodeEnum.CONTENT_IS_NULL.getCode());
            }
            // 只有内容所属人才能申诉
            if (!content.getUserId().equals(userId)) {
                throw new ServiceException(CampusBizCodeEnum.APPEAL_NOT_OWN.getMsg(), CampusBizCodeEnum.APPEAL_NOT_OWN.getCode());
            }
            // 只有下架(2)或拒绝(3)状态才能申诉
            if (content.getStatus() != 2 && content.getStatus() != 3) {
                throw new ServiceException(CampusBizCodeEnum.APPEAL_TARGET_NOT_ELIGIBLE.getMsg(), CampusBizCodeEnum.APPEAL_TARGET_NOT_ELIGIBLE.getCode());
            }
        }

        AppealEntity appeal = new AppealEntity();
        appeal.setAppealId(IdWorker.getId());
        appeal.setUserId(userId);
        appeal.setTargetType(vo.getTargetType());
        appeal.setTargetId(vo.getTargetId());
        appeal.setReason(vo.getReason());
        appeal.setEvidenceUrls(vo.getEvidenceUrls());
        appeal.setStatus(0); // 待处理
        appealMapper.insert(appeal);
    }

    @Override
    @Transactional
    public void reviewAppeal(AppealReviewVo vo) {
        AppealEntity appeal = appealMapper.selectById(vo.getAppealId());
        if (appeal == null) {
            throw new ServiceException(CampusBizCodeEnum.APPEAL_NOT_EXIST.getMsg(), CampusBizCodeEnum.APPEAL_NOT_EXIST.getCode());
        }
        // 只有待处理(0)或审核中(1)的申诉才能审核
        if (appeal.getStatus() != 0 && appeal.getStatus() != 1) {
            throw new ServiceException(CampusBizCodeEnum.APPEAL_ALREADY_REVIEWED.getMsg(), CampusBizCodeEnum.APPEAL_ALREADY_REVIEWED.getCode());
        }

        Long reviewerId = SecurityUtils.getUserId();
        appeal.setReviewerId(reviewerId);
        appeal.setReviewRemark(vo.getReviewRemark());
        appeal.setReviewTime(new Date());

        if (vo.getStatus() == 2) {
            // 通过：恢复内容
            appeal.setStatus(2);
            appealMapper.updateById(appeal);

            if (appeal.getTargetType() == 1) {
                // 恢复内容并回补互动数据
                contentService.restoreContentWithInteractions(appeal.getTargetId());
            }

            // 恢复信用分 - 查找关联的违规记录
            LambdaQueryWrapper<ViolationRecordEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ViolationRecordEntity::getTargetType, appeal.getTargetType())
                   .eq(ViolationRecordEntity::getTargetId, appeal.getTargetId())
                   .eq(ViolationRecordEntity::getUserId, appeal.getUserId());
            List<ViolationRecordEntity> violations = violationRecordService.list(wrapper);
            int totalDeducted = 0;
            for (ViolationRecordEntity v : violations) {
                if (v.getCreditDeduct() != null) {
                    totalDeducted += v.getCreditDeduct();
                }
            }
            if (totalDeducted > 0) {
                userCreditService.restoreCredit(appeal.getUserId(), totalDeducted,
                        "申诉通过恢复", "appeal", appeal.getAppealId());
            }

            // 创建审核记录
            ModerationRecordEntity record = new ModerationRecordEntity();
            record.setRecordId(IdWorker.getId());
            record.setTargetType(appeal.getTargetType());
            record.setTargetId(appeal.getTargetId());
            record.setUserId(appeal.getUserId());
            record.setAction(1); // 通过
            record.setTriggerType(1); // 人工
            record.setReviewerId(reviewerId);
            record.setManualRemark("申诉通过: " + vo.getReviewRemark());
            moderationRecordService.addRecord(record);

        } else if (vo.getStatus() == 3) {
            // 驳回
            appeal.setStatus(3);
            appealMapper.updateById(appeal);
        }
    }

    @Override
    public List<AppealEntity> getOwnAppeals(Long userId) {
        LambdaQueryWrapper<AppealEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppealEntity::getUserId, userId)
               .orderByDesc(AppealEntity::getCreateTime);
        return appealMapper.selectList(wrapper);
    }

    @Override
    public boolean hasActiveAppeal(Integer targetType, Long targetId) {
        LambdaQueryWrapper<AppealEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppealEntity::getTargetType, targetType)
               .eq(AppealEntity::getTargetId, targetId)
               .in(AppealEntity::getStatus, 0, 1);
        return appealMapper.selectCount(wrapper) > 0;
    }
}
