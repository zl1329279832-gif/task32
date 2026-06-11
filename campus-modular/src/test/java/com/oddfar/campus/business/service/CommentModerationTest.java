package com.oddfar.campus.business.service;

import com.oddfar.campus.business.domain.entity.CommentEntity;
import com.oddfar.campus.business.domain.entity.ContentEntity;
import com.oddfar.campus.business.domain.vo.ModerationResultVo;
import com.oddfar.campus.business.enums.CampusBizCodeEnum;
import com.oddfar.campus.business.mapper.CommentMapper;
import com.oddfar.campus.business.mapper.ContentMapper;
import com.oddfar.campus.business.service.impl.CommentServiceImpl;
import com.oddfar.campus.common.exception.ServiceException;
import com.oddfar.campus.common.utils.SecurityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 评论审核与连带冻结测试
 */
@ExtendWith(MockitoExtension.class)
public class CommentModerationTest {

    @InjectMocks
    private CommentServiceImpl commentService;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private ContentMapper contentMapper;

    @Mock
    private ModerationService moderationService;

    @Test
    @DisplayName("正常内容下的评论应通过审核")
    void testCommentOnNormalContent() {
        ContentEntity content = new ContentEntity();
        content.setContentId(100L);
        content.setStatus(1);
        content.setUserId(10L);

        when(contentMapper.selectById(100L)).thenReturn(content);

        ModerationResultVo passResult = new ModerationResultVo();
        passResult.setDecision(1);
        passResult.setRiskScore(0);
        when(moderationService.moderateComment(any(CommentEntity.class))).thenReturn(passResult);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class);
             MockedStatic<com.oddfar.campus.common.utils.ServletUtils> servletUtils =
                     mockStatic(com.oddfar.campus.common.utils.ServletUtils.class);
             MockedStatic<com.oddfar.campus.common.utils.ip.IpUtils> ipUtils =
                     mockStatic(com.oddfar.campus.common.utils.ip.IpUtils.class);
             MockedStatic<com.oddfar.campus.common.utils.ip.AddressUtils> addressUtils =
                     mockStatic(com.oddfar.campus.common.utils.ip.AddressUtils.class)) {

            securityUtils.when(SecurityUtils::getUserId).thenReturn(1L);
            servletUtils.when(com.oddfar.campus.common.utils.ServletUtils::getRequest)
                    .thenReturn(null);
            ipUtils.when(() -> com.oddfar.campus.common.utils.ip.IpUtils.getIpAddr(any()))
                    .thenReturn("127.0.0.1");
            addressUtils.when(() -> com.oddfar.campus.common.utils.ip.AddressUtils.getRealAddressByIP(anyString()))
                    .thenReturn("内网IP");

            CommentEntity comment = new CommentEntity();
            comment.setContentId(100L);
            comment.setCoContent("好的评论内容");

            when(commentMapper.insert(any(CommentEntity.class))).thenReturn(1);

            Long commentId = commentService.insertComment(comment);

            assertNotNull(commentId, "评论应成功创建");
            verify(commentMapper).insert(any(CommentEntity.class));
        }
    }

    @Test
    @DisplayName("对被下架内容评论应被禁止")
    void testCommentOnBlockedContent() {
        ContentEntity content = new ContentEntity();
        content.setContentId(200L);
        content.setStatus(2); // 已下架

        when(contentMapper.selectById(200L)).thenReturn(content);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getUserId).thenReturn(1L);

            CommentEntity comment = new CommentEntity();
            comment.setContentId(200L);
            comment.setCoContent("试图评论已下架的内容");

            ServiceException exception = assertThrows(ServiceException.class,
                    () -> commentService.insertComment(comment));
            assertEquals(CampusBizCodeEnum.CONTENT_OPERATION_PROHIBITED.getCode(),
                    exception.getCode(), "应抛出禁止操作异常");
        }
    }

    @Test
    @DisplayName("含高风险敏感词的评论应被拦截")
    void testCommentWithSensitiveWord() {
        ContentEntity content = new ContentEntity();
        content.setContentId(300L);
        content.setStatus(1);
        content.setUserId(10L);

        when(contentMapper.selectById(300L)).thenReturn(content);

        ModerationResultVo blockResult = new ModerationResultVo();
        blockResult.setDecision(3);
        blockResult.setRiskScore(80);
        blockResult.setHitWords(Arrays.asList("违禁词"));
        when(moderationService.moderateComment(any(CommentEntity.class))).thenReturn(blockResult);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class);
             MockedStatic<com.oddfar.campus.common.utils.ServletUtils> servletUtils =
                     mockStatic(com.oddfar.campus.common.utils.ServletUtils.class);
             MockedStatic<com.oddfar.campus.common.utils.ip.IpUtils> ipUtils =
                     mockStatic(com.oddfar.campus.common.utils.ip.IpUtils.class);
             MockedStatic<com.oddfar.campus.common.utils.ip.AddressUtils> addressUtils =
                     mockStatic(com.oddfar.campus.common.utils.ip.AddressUtils.class)) {

            securityUtils.when(SecurityUtils::getUserId).thenReturn(1L);
            servletUtils.when(com.oddfar.campus.common.utils.ServletUtils::getRequest)
                    .thenReturn(null);
            ipUtils.when(() -> com.oddfar.campus.common.utils.ip.IpUtils.getIpAddr(any()))
                    .thenReturn("127.0.0.1");
            addressUtils.when(() -> com.oddfar.campus.common.utils.ip.AddressUtils.getRealAddressByIP(anyString()))
                    .thenReturn("内网IP");

            CommentEntity comment = new CommentEntity();
            comment.setContentId(300L);
            comment.setCoContent("这包含违禁词的评论");

            ServiceException exception = assertThrows(ServiceException.class,
                    () -> commentService.insertComment(comment));
            assertEquals(CampusBizCodeEnum.COMMENT_BLOCKED.getCode(),
                    exception.getCode(), "应抛出评论被拦截异常");
        }
    }

    @Test
    @DisplayName("评论连带冻结：对不存在的内容评论应被禁止")
    void testCommentCascadeFreeze() {
        when(contentMapper.selectById(999L)).thenReturn(null);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getUserId).thenReturn(1L);

            CommentEntity comment = new CommentEntity();
            comment.setContentId(999L);
            comment.setCoContent("评论不存在的内容");

            assertThrows(ServiceException.class,
                    () -> commentService.insertComment(comment),
                    "对不存在的内容评论应抛出异常");
        }
    }
}
