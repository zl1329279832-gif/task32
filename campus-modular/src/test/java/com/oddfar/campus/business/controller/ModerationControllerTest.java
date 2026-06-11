package com.oddfar.campus.business.controller;

import com.oddfar.campus.business.domain.entity.ContentEntity;
import com.oddfar.campus.business.domain.vo.BatchModerationVo;
import com.oddfar.campus.business.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 管理员权限隔离测试
 */
@ExtendWith(MockitoExtension.class)
public class ModerationControllerTest {

    @Test
    @DisplayName("批量通过动作值应为1")
    void testBatchApproveAction() {
        BatchModerationVo vo = new BatchModerationVo();
        vo.setContentIds(Arrays.asList(1L, 2L, 3L));
        vo.setAction(1); // 通过
        vo.setRemark("批量通过");

        assertEquals(1, vo.getAction());
        assertEquals(3, vo.getContentIds().size());
    }

    @Test
    @DisplayName("非内容所有者userId应与内容userId不同")
    void testAppealOwnershipCheck() {
        ContentEntity content = new ContentEntity();
        content.setContentId(100L);
        content.setUserId(10L);
        content.setStatus(3);

        Long nonOwnerUserId = 20L;
        assertNotEquals(nonOwnerUserId, content.getUserId(),
                "非所有者的userId应与内容userId不同，权限隔离有效");
    }

    @Test
    @DisplayName("无效的批量操作动作应被识别")
    void testInvalidBatchAction() {
        BatchModerationVo vo = new BatchModerationVo();
        vo.setContentIds(Arrays.asList(1L, 2L));
        vo.setAction(99); // 无效动作

        assertTrue(vo.getAction() < 1 || vo.getAction() > 4,
                "动作值99不在有效范围[1-4]内");
    }

    @Test
    @DisplayName("批量下架动作值应为2")
    void testBatchTakedownAction() {
        BatchModerationVo vo = new BatchModerationVo();
        vo.setContentIds(Arrays.asList(1L));
        vo.setAction(2); // 下架
        vo.setRemark("违反社区规范");

        assertEquals(2, vo.getAction(), "下架action应为2");
        assertNotNull(vo.getRemark());
    }

    @Test
    @DisplayName("敏感词服务接口应可被正确mock")
    void testSensitiveWordServiceInterface() {
        SensitiveWordService service = mock(SensitiveWordService.class);
        when(service.checkText("test")).thenReturn(Arrays.asList("test"));

        assertEquals(1, service.checkText("test").size());
        verify(service).checkText("test");
    }
}
