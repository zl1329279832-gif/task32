package com.oddfar.campus.business.service.impl;

import com.alibaba.fastjson2.JSON;
import com.oddfar.campus.business.domain.entity.*;
import com.oddfar.campus.business.enums.ModerationDecision;
import com.oddfar.campus.business.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自动审核服务实现（核心引擎）
 *
 * 评估优先级：BLOCK > PENDING > PASS
 */
@Service
public class AutoModerationServiceImpl implements AutoModerationService {

    @Autowired
    private SensitiveWordService sensitiveWordService;
    @Autowired
    private ModerationRuleService moderationRuleService;
    @Autowired
    private ModerationRecordService moderationRecordService;
    @Autowired
    private ViolationRecordService violationRecordService;
    @Autowired
    private UserCreditService userCreditService;

    @Override
    public ModerationDecision evaluate(ContentEntity content, List<Long> fileIds, List<String> tagNames) {
        ModerationDecision worst = ModerationDecision.PASS;
        List<String> matchedRuleNames = new ArrayList<>();
        StringBuilder reasonBuilder = new StringBuilder();

        // 1. 敏感词检测
        List<SensitiveWordService.SensitiveWordMatch> sensitiveMatches =
                sensitiveWordService.detectSensitiveWords(content.getContent());
        if (!sensitiveMatches.isEmpty()) {
            int maxSeverity = sensitiveMatches.stream()
                    .mapToInt(SensitiveWordService.SensitiveWordMatch::getSeverity)
                    .max().orElse(0);

            if (maxSeverity >= 3) {
                worst = ModerationDecision.worst(worst, ModerationDecision.BLOCK);
            } else {
                worst = ModerationDecision.worst(worst, ModerationDecision.PENDING);
            }
            String words = sensitiveMatches.stream()
                    .map(SensitiveWordService.SensitiveWordMatch::getWord)
                    .collect(Collectors.joining(","));
            matchedRuleNames.add("SENSITIVE_WORD[" + words + "]");
            reasonBuilder.append("敏感词: ").append(words).append("; ");
        }

        // 2. 分类规则匹配
        List<ModerationRuleEntity> rules = moderationRuleService.getEnabledRules();
        for (ModerationRuleEntity rule : rules) {
            if ("CATEGORY".equals(rule.getRuleType())) {
                if (rule.getRuleKey() != null && rule.getRuleKey().equals(String.valueOf(content.getCategoryId()))) {
                    ModerationDecision ruleDecision = parseAction(rule.getAction());
                    worst = ModerationDecision.worst(worst, ruleDecision);
                    matchedRuleNames.add("CATEGORY_RULE[" + rule.getRuleKey() + "]");
                    reasonBuilder.append("分类规则: ").append(rule.getDescription()).append("; ");
                }
            }
        }

        // 3. 标签规则匹配
        if (tagNames != null && !tagNames.isEmpty()) {
            for (ModerationRuleEntity rule : rules) {
                if ("TAG".equals(rule.getRuleType()) && tagNames.contains(rule.getRuleKey())) {
                    ModerationDecision ruleDecision = parseAction(rule.getAction());
                    worst = ModerationDecision.worst(worst, ruleDecision);
                    matchedRuleNames.add("TAG_RULE[" + rule.getRuleKey() + "]");
                    reasonBuilder.append("标签规则: ").append(rule.getDescription()).append("; ");
                }
            }
        }

        // 4. 用户信用分评估
        String creditAction = userCreditService.evaluateCreditAction(content.getUserId());
        ModerationDecision creditDecision = parseAction(creditAction);
        if (creditDecision.ordinal() > ModerationDecision.PASS.ordinal()) {
            worst = ModerationDecision.worst(worst, creditDecision);
            matchedRuleNames.add("CREDIT_SCORE[" + creditAction + "]");
            reasonBuilder.append("信用分评估: ").append(creditAction).append("; ");
        }

        // 5. 违规历史
        Long violationCount = violationRecordService.getViolationCount(content.getUserId());
        if (violationCount >= 3) {
            worst = ModerationDecision.worst(worst, ModerationDecision.BLOCK);
            matchedRuleNames.add("VIOLATION_HISTORY[" + violationCount + "]");
            reasonBuilder.append("违规次数过多: ").append(violationCount).append("次; ");
        } else if (violationCount >= 1) {
            worst = ModerationDecision.worst(worst, ModerationDecision.PENDING);
            matchedRuleNames.add("VIOLATION_HISTORY[" + violationCount + "]");
            reasonBuilder.append("有违规历史: ").append(violationCount).append("次; ");
        }

        // 6. 附件规则匹配
        if (content.getType() != null && content.getType() > 0) {
            String typeStr = content.getType() == 1 ? "image" : "video";
            for (ModerationRuleEntity rule : rules) {
                if ("ATTACHMENT".equals(rule.getRuleType()) && typeStr.equals(rule.getRuleKey())) {
                    ModerationDecision ruleDecision = parseAction(rule.getAction());
                    worst = ModerationDecision.worst(worst, ruleDecision);
                    matchedRuleNames.add("ATTACHMENT_RULE[" + typeStr + "]");
                    reasonBuilder.append("附件规则: ").append(rule.getDescription()).append("; ");
                }
            }
        }

        // 记录审核记录
        String matchedRulesJson = JSON.toJSONString(matchedRuleNames);
        ModerationRecordEntity record = moderationRecordService.recordAction(
                content.getContentId(), "CONTENT", null,
                "AUTO", worst.name(), reasonBuilder.toString(),
                matchedRulesJson, null, worst.toStatus());

        // 如果被拦截，记录违规并扣信用分
        if (worst == ModerationDecision.BLOCK) {
            String matchedWordsJson = sensitiveMatches.isEmpty() ? null :
                    JSON.toJSONString(sensitiveMatches.stream()
                            .map(SensitiveWordService.SensitiveWordMatch::getWord)
                            .collect(Collectors.toList()));
            violationRecordService.recordViolation(
                    content.getUserId(), content.getContentId(),
                    sensitiveMatches.isEmpty() ? "MANUAL" : "SENSITIVE_WORD",
                    reasonBuilder.toString(), matchedWordsJson, record.getRecordId());
            userCreditService.changeCredit(content.getUserId(), -10,
                    "内容被自动拦截", "content", content.getContentId());
        }

        return worst;
    }

    @Override
    public ModerationDecision evaluateComment(CommentEntity comment) {
        if (comment.getCoContent() == null || comment.getCoContent().isEmpty()) {
            return ModerationDecision.PASS;
        }

        List<SensitiveWordService.SensitiveWordMatch> matches =
                sensitiveWordService.detectSensitiveWords(comment.getCoContent());

        if (matches.isEmpty()) {
            return ModerationDecision.PASS;
        }

        int maxSeverity = matches.stream()
                .mapToInt(SensitiveWordService.SensitiveWordMatch::getSeverity)
                .max().orElse(0);

        if (maxSeverity >= 3) {
            // 记录审核记录
            String words = matches.stream().map(SensitiveWordService.SensitiveWordMatch::getWord)
                    .collect(Collectors.joining(","));
            moderationRecordService.recordAction(
                    comment.getContentId(), "COMMENT", comment.getCommentId(),
                    "AUTO", "BLOCK", "评论敏感词: " + words,
                    JSON.toJSONString(matches.stream()
                            .map(SensitiveWordService.SensitiveWordMatch::getWord)
                            .collect(Collectors.toList())),
                    null, null);
            return ModerationDecision.BLOCK;
        }

        return ModerationDecision.PENDING;
    }

    private ModerationDecision parseAction(String action) {
        if (action == null) {
            return ModerationDecision.PENDING;
        }
        try {
            return ModerationDecision.valueOf(action);
        } catch (IllegalArgumentException e) {
            return ModerationDecision.PENDING;
        }
    }
}
