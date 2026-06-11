package com.oddfar.campus.business.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.oddfar.campus.business.domain.entity.CommentEntity;
import com.oddfar.campus.business.domain.entity.ContentEntity;
import com.oddfar.campus.business.domain.entity.ContentLoveEntity;
import com.oddfar.campus.business.mapper.ContentLoveMapper;
import com.oddfar.campus.business.mapper.ContentMapper;
import com.oddfar.campus.business.service.impl.ContentServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 点赞统计回补测试
 */
@ExtendWith(MockitoExtension.class)
public class ContentLoveBackfillTest {

    @InjectMocks
    private ContentServiceImpl contentService;

    @Mock
    private ContentMapper contentMapper;

    @Mock
    private ContentLoveMapper contentLoveMapper;

    @Mock
    private com.oddfar.campus.business.mapper.CommentMapper commentMapper;

    @Mock
    private CampusFileService fileService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private TagService tagService;

    @Mock
    private ModerationService moderationService;

    @Mock
    private ModerationRecordService moderationRecordService;

    @Mock
    private ViolationRecordService violationRecordService;

    @BeforeEach
    void setUp() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, ""), ContentLoveEntity.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, ""), CommentEntity.class);
    }

    @Test
    @DisplayName("恢复内容后应回补正确的点赞数")
    void testLoveCountBackfillAfterRestore() {
        ContentEntity content = new ContentEntity();
        content.setContentId(100L);
        content.setStatus(3);
        content.setLoveCount(0L);
        content.setUserId(1L);

        when(contentMapper.selectById(100L)).thenReturn(content);
        when(contentLoveMapper.selectCount(any())).thenReturn(3L);
        when(contentMapper.updateById(any(ContentEntity.class))).thenReturn(1);
        when(commentMapper.update(any(), any())).thenReturn(0);

        contentService.restoreContentWithInteractions(100L);

        verify(contentMapper).updateById(argThat(c -> {
            ContentEntity updated = (ContentEntity) c;
            return updated.getStatus() == 1 && updated.getLoveCount() == 3L;
        }));
    }

    @Test
    @DisplayName("无点赞内容恢复后点赞数应为0")
    void testLoveCountZeroAfterRestore() {
        ContentEntity content = new ContentEntity();
        content.setContentId(200L);
        content.setStatus(2);
        content.setLoveCount(5L);
        content.setUserId(1L);

        when(contentMapper.selectById(200L)).thenReturn(content);
        when(contentLoveMapper.selectCount(any())).thenReturn(0L);
        when(contentMapper.updateById(any(ContentEntity.class))).thenReturn(1);
        when(commentMapper.update(any(), any())).thenReturn(0);

        contentService.restoreContentWithInteractions(200L);

        verify(contentMapper).updateById(argThat(c -> {
            ContentEntity updated = (ContentEntity) c;
            return updated.getStatus() == 1 && updated.getLoveCount() == 0L;
        }));
    }

    @Test
    @DisplayName("内容不存在时恢复操作应安全跳过")
    void testRestoreNonExistentContent() {
        when(contentMapper.selectById(999L)).thenReturn(null);

        assertDoesNotThrow(() -> contentService.restoreContentWithInteractions(999L));
        verify(contentMapper, never()).updateById(any());
    }
}
