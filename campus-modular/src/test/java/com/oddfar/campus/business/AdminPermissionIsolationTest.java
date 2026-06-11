package com.oddfar.campus.business;

import com.oddfar.campus.business.domain.entity.AdminModerationScopeEntity;
import com.oddfar.campus.business.domain.entity.ContentEntity;
import com.oddfar.campus.business.mapper.AdminModerationScopeMapper;
import com.oddfar.campus.business.mapper.ContentMapper;
import com.oddfar.campus.business.service.impl.AdminModerationScopeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 管理员权限隔离测试
 */
@ExtendWith(MockitoExtension.class)
public class AdminPermissionIsolationTest extends BaseTest {

    @InjectMocks
    private AdminModerationScopeServiceImpl scopeService;

    @Mock
    private AdminModerationScopeMapper adminModerationScopeMapper;
    @Mock
    private ContentMapper contentMapper;

    /**
     * 分类范围管理员不能审核其他分类的内容
     */
    @Test
    void categoryScopedAdminCannotModerateOther() {
        // 审核员200只能管理分类1
        AdminModerationScopeEntity scope = createScope(1L, 200L, "CATEGORY", 1L);
        when(adminModerationScopeMapper.selectByAdminUserId(200L))
                .thenReturn(Arrays.asList(scope));

        // 内容属于分类2
        ContentEntity content = createContent(1001L, 100L, 2L, "二手交易内容", 0, 0);
        when(contentMapper.selectById(1001L)).thenReturn(content);

        boolean canModerate = scopeService.canModerate(200L, 1001L);

        assertFalse(canModerate, "分类范围管理员不应能审核其他分类的内容");
    }

    /**
     * ALL范围管理员可以审核任何内容
     */
    @Test
    void allScopeAdminCanModerateAny() {
        AdminModerationScopeEntity scope = createScope(2L, 200L, "ALL", null);
        when(adminModerationScopeMapper.selectByAdminUserId(200L))
                .thenReturn(Arrays.asList(scope));

        boolean canModerate = scopeService.canModerate(200L, 1001L);

        assertTrue(canModerate, "ALL范围管理员应能审核任何内容");
    }

    /**
     * 超级管理员(userId=1)可以审核任何内容
     */
    @Test
    void superAdminCanModerateAny() {
        boolean canModerate = scopeService.canModerate(1L, 1001L);

        assertTrue(canModerate, "超级管理员应能审核任何内容");
        // 不应查询scope
        verify(adminModerationScopeMapper, never()).selectByAdminUserId(anyLong());
    }
}
