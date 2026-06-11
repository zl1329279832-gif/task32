package com.oddfar.campus.business.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddfar.campus.business.domain.entity.SensitiveWordEntity;
import com.oddfar.campus.business.mapper.SensitiveWordMapper;
import com.oddfar.campus.business.service.SensitiveWordService;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import com.oddfar.campus.common.core.page.PageUtils;
import com.oddfar.campus.common.domain.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 敏感词服务实现 - 使用DFA字典树
 */
@Service
public class SensitiveWordServiceImpl extends ServiceImpl<SensitiveWordMapper, SensitiveWordEntity>
        implements SensitiveWordService {

    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;

    /** DFA根节点 */
    private volatile Map<Character, Map> dfaRoot = new HashMap<>();

    /** 存储词的信息: word -> {severity, category} */
    private volatile Map<String, SensitiveWordEntity> wordInfoMap = new HashMap<>();

    @PostConstruct
    public void init() {
        reloadCache();
    }

    @Override
    public void reloadCache() {
        List<SensitiveWordEntity> words = sensitiveWordMapper.selectEnabledWords();
        Map<Character, Map> newRoot = new HashMap<>();
        Map<String, SensitiveWordEntity> newWordMap = new HashMap<>();

        for (SensitiveWordEntity wordEntity : words) {
            String word = wordEntity.getWord().toLowerCase();
            newWordMap.put(word, wordEntity);
            addWordToDFA(newRoot, word);
        }

        this.dfaRoot = newRoot;
        this.wordInfoMap = newWordMap;
    }

    @SuppressWarnings("unchecked")
    private void addWordToDFA(Map<Character, Map> root, String word) {
        Map<Character, Map> current = root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            Map<Character, Map> next = current.get(c);
            if (next == null) {
                next = new HashMap<>();
                current.put(c, next);
            }
            if (i == word.length() - 1) {
                next.put((char) 0, null); // 结束标记
            }
            current = next;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SensitiveWordMatch> detectSensitiveWords(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        List<SensitiveWordMatch> matches = new ArrayList<>();
        String lowerText = text.toLowerCase();
        Map<Character, Map> root = this.dfaRoot;

        for (int i = 0; i < lowerText.length(); i++) {
            Map<Character, Map> current = root;
            for (int j = i; j < lowerText.length(); j++) {
                char c = lowerText.charAt(j);
                Map<Character, Map> next = current.get(c);
                if (next == null) {
                    break;
                }
                // 检查是否是词的结尾
                if (next.containsKey((char) 0)) {
                    String matchedWord = lowerText.substring(i, j + 1);
                    SensitiveWordEntity info = wordInfoMap.get(matchedWord);
                    if (info != null) {
                        matches.add(new SensitiveWordMatch(info.getWord(), info.getSeverity(), info.getCategory()));
                    }
                }
                current = next;
            }
        }

        // 去重
        Set<String> seen = new HashSet<>();
        List<SensitiveWordMatch> unique = new ArrayList<>();
        for (SensitiveWordMatch m : matches) {
            if (seen.add(m.getWord())) {
                unique.add(m);
            }
        }
        return unique;
    }

    @Override
    public PageResult<SensitiveWordEntity> page(SensitiveWordEntity entity) {
        LambdaQueryWrapperX<SensitiveWordEntity> wrapper = new LambdaQueryWrapperX<>();
        wrapper.likeIfPresent(SensitiveWordEntity::getWord, entity.getWord());
        wrapper.eqIfPresent(SensitiveWordEntity::getCategory, entity.getCategory());
        wrapper.eqIfPresent(SensitiveWordEntity::getStatus, entity.getStatus());
        wrapper.orderByDesc(SensitiveWordEntity::getCreateTime);
        return sensitiveWordMapper.selectPage(wrapper);
    }
}
