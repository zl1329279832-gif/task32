package com.oddfar.campus.business.service.impl;

import com.oddfar.campus.business.domain.entity.CommentEntity;
import com.oddfar.campus.business.domain.vo.ModerationResultVo;
import com.oddfar.campus.business.domain.vo.SendContentVo;
import com.oddfar.campus.business.service.ModerationService;
import com.oddfar.campus.business.service.SensitiveWordService;
import com.oddfar.campus.business.service.UserCreditService;
import com.oddfar.campus.business.service.ViolationRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class ModerationServiceImpl implements ModerationService {

    @Resource
    private SensitiveWordService sensitiveWordService;

    @Resource
    private UserCreditService userCreditService;

    @Resource
    private ViolationRecordService violationRecordService;

    @Override
    public ModerationResultVo moderateContent(SendContentVo vo, Long userId) {
        String text = vo.getContent() != null ? vo.getContent() : "";
        Integer contentType = vo.getType();
        Integer fileCount = (vo.getFileList() != null) ? vo.getFileList().size() : 0;
        Long categoryId = vo.getCategoryId();

        int riskScore = calculateRiskScore(userId, text, contentType, fileCount, categoryId);

        ModerationResultVo result = new ModerationResultVo();
        result.setRiskScore(riskScore);

        // 检测敏感词
        List<String> hitWords = sensitiveWordService.checkText(text);
        result.setHitWords(hitWords);

        // 构建原因
        List<String> reasons = new ArrayList<>();
        if (!hitWords.isEmpty()) {
            reasons.add("命中敏感词: " + String.join(", ", hitWords));
        }

        long violationCount = violationRecordService.countByUserId(userId);
        if (violationCount >= 1) {
            reasons.add("历史违规" + violationCount + "次");
        }

        int creditScore = userCreditService.getCreditScore(userId);
        if (creditScore < 80) {
            reasons.add("信用分偏低: " + creditScore);
        }

        if (contentType != null && contentType == 2) {
            reasons.add("视频内容需审核");
        }

        result.setReasons(reasons);

        // 决策
        if (riskScore < 30) {
            result.setDecision(1); // 直接发布
        } else if (riskScore < 70) {
            result.setDecision(0); // 待审核
        } else {
            result.setDecision(3); // 拦截
        }

        return result;
    }

    @Override
    public ModerationResultVo moderateComment(CommentEntity comment) {
        String text = comment.getCoContent() != null ? comment.getCoContent() : "";
        Long userId = comment.getUserId();

        int riskScore = calculateRiskScore(userId, text, 0, 0, null);

        ModerationResultVo result = new ModerationResultVo();
        result.setRiskScore(riskScore);

        List<String> hitWords = sensitiveWordService.checkText(text);
        result.setHitWords(hitWords);

        List<String> reasons = new ArrayList<>();
        if (!hitWords.isEmpty()) {
            reasons.add("命中敏感词: " + String.join(", ", hitWords));
        }
        result.setReasons(reasons);

        if (riskScore < 30) {
            result.setDecision(1);
        } else if (riskScore < 70) {
            result.setDecision(0);
        } else {
            result.setDecision(3);
        }

        return result;
    }

    @Override
    public int calculateRiskScore(Long userId, String text, Integer contentType, Integer fileCount, Long categoryId) {
        int score = 0;

        // 1. 敏感词评分 (0-40)
        int sensitiveScore = 0;
        if (text != null && !text.isEmpty()) {
            List<String> hitWords = sensitiveWordService.checkText(text);
            for (String word : hitWords) {
                int level = sensitiveWordService.getMaxHitLevel(word);
                if (level >= 3) {
                    sensitiveScore += 15;
                } else if (level >= 2) {
                    sensitiveScore += 8;
                } else {
                    sensitiveScore += 3;
                }
            }
        }
        score += Math.min(40, sensitiveScore);

        // 2. 历史违规评分 (0-20)
        if (userId != null) {
            long violationCount = violationRecordService.countByUserId(userId);
            if (violationCount >= 5) {
                score += 20;
            } else if (violationCount >= 3) {
                score += 15;
            } else if (violationCount >= 1) {
                score += 8;
            }
        }

        // 3. 信用分评分 (0-20)
        if (userId != null) {
            int creditScore = userCreditService.getCreditScore(userId);
            if (creditScore < 30) {
                score += 20;
            } else if (creditScore < 60) {
                score += 12;
            } else if (creditScore < 80) {
                score += 5;
            }
        }

        // 4. 附件风险评分 (0-10)
        if (contentType != null) {
            if (contentType == 2) {
                score += 5; // 视频
            } else if (contentType == 1) {
                if (fileCount != null && fileCount >= 3) {
                    score += 4;
                } else if (fileCount != null && fileCount > 0) {
                    score += 2;
                }
            }
        }

        // 5. 分类风险评分 (0-10) - 可扩展
        // 保留扩展点，可根据分类配置动态加分

        return score;
    }
}
