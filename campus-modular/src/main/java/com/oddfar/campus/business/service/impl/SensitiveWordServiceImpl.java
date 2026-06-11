package com.oddfar.campus.business.service.impl;

import cn.hutool.dfa.WordTree;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddfar.campus.business.domain.entity.SensitiveWordEntity;
import com.oddfar.campus.business.mapper.SensitiveWordMapper;
import com.oddfar.campus.business.service.SensitiveWordService;
import com.oddfar.campus.common.domain.PageResult;
import com.oddfar.campus.common.core.page.PageUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SensitiveWordServiceImpl extends ServiceImpl<SensitiveWordMapper, SensitiveWordEntity>
        implements SensitiveWordService {

    @Resource
    private SensitiveWordMapper sensitiveWordMapper;

    private WordTree wordTree = new WordTree();

    /** 敏感词 -> 等级 的映射 */
    private Map<String, Integer> wordLevelMap = new HashMap<>();

    @PostConstruct
    public void init() {
        refreshCache();
    }

    @Override
    public PageResult<SensitiveWordEntity> page(SensitiveWordEntity word) {
        PageUtils.startPage();
        LambdaQueryWrapper<SensitiveWordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(word.getWord() != null, SensitiveWordEntity::getWord, word.getWord())
               .eq(word.getCategory() != null, SensitiveWordEntity::getCategory, word.getCategory())
               .eq(word.getLevel() != null, SensitiveWordEntity::getLevel, word.getLevel())
               .eq(word.getStatus() != null, SensitiveWordEntity::getStatus, word.getStatus())
               .orderByDesc(SensitiveWordEntity::getCreateTime);
        List<SensitiveWordEntity> list = sensitiveWordMapper.selectList(wrapper);
        return PageUtils.getPageResult(list);
    }

    @Override
    public int insertWord(SensitiveWordEntity word) {
        int result = sensitiveWordMapper.insert(word);
        refreshCache();
        return result;
    }

    @Override
    public int updateWord(SensitiveWordEntity word) {
        int result = sensitiveWordMapper.updateById(word);
        refreshCache();
        return result;
    }

    @Override
    public int deleteWordByIds(Long[] wordIds) {
        int result = sensitiveWordMapper.deleteBatchIds(Arrays.asList(wordIds));
        refreshCache();
        return result;
    }

    @Override
    public List<String> checkText(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> matchAll = wordTree.matchAll(text, -1, false, false);
        return matchAll != null ? matchAll : Collections.emptyList();
    }

    @Override
    public int getMaxHitLevel(String text) {
        List<String> hits = checkText(text);
        int maxLevel = 0;
        for (String hit : hits) {
            Integer level = wordLevelMap.getOrDefault(hit, 1);
            if (level > maxLevel) {
                maxLevel = level;
            }
        }
        return maxLevel;
    }

    @Override
    public void refreshCache() {
        LambdaQueryWrapper<SensitiveWordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensitiveWordEntity::getStatus, "0");
        List<SensitiveWordEntity> words = sensitiveWordMapper.selectList(wrapper);

        WordTree newTree = new WordTree();
        Map<String, Integer> newMap = new HashMap<>();
        for (SensitiveWordEntity w : words) {
            newTree.addWord(w.getWord());
            newMap.put(w.getWord(), w.getLevel() != null ? w.getLevel() : 1);
        }
        this.wordTree = newTree;
        this.wordLevelMap = newMap;
    }
}
