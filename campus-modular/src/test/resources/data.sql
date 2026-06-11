-- ============================================================
-- 测试种子数据
-- ============================================================

-- 超级管理员
INSERT INTO sys_user (user_id, user_name, nick_name, password, status, del_flag)
VALUES (1, 'admin', '管理员', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', b'0');

-- 普通用户
INSERT INTO sys_user (user_id, user_name, nick_name, password, status, del_flag)
VALUES (100, 'user1', '用户一', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', b'0');

INSERT INTO sys_user (user_id, user_name, nick_name, password, status, del_flag)
VALUES (101, 'user2', '用户二', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', b'0');

-- 管理员角色用户
INSERT INTO sys_user (user_id, user_name, nick_name, password, status, del_flag)
VALUES (200, 'moderator1', '审核员一', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', b'0');

INSERT INTO sys_user (user_id, user_name, nick_name, password, status, del_flag)
VALUES (201, 'moderator2', '审核员二', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', b'0');

-- 分类
INSERT INTO campus_category (category_id, category_name, parent_id, order_num, status, del_flag)
VALUES (1, '校园生活', 0, 1, '0', b'0');
INSERT INTO campus_category (category_id, category_name, parent_id, order_num, status, del_flag)
VALUES (2, '二手交易', 0, 2, '0', b'0');

-- 敏感词
INSERT INTO campus_sensitive_word (word_id, word, category, severity, status, del_flag)
VALUES (1, '脏话', 'abuse', 2, '0', b'0');
INSERT INTO campus_sensitive_word (word_id, word, category, severity, status, del_flag)
VALUES (2, '违禁品', 'custom', 3, '0', b'0');
INSERT INTO campus_sensitive_word (word_id, word, category, severity, status, del_flag)
VALUES (3, '广告词', 'ad', 1, '0', b'0');

-- 用户信用分
INSERT INTO campus_user_credit (credit_id, user_id, credit_score, del_flag)
VALUES (1, 100, 100, b'0');
INSERT INTO campus_user_credit (credit_id, user_id, credit_score, del_flag)
VALUES (2, 101, 30, b'0');

-- 审核规则：二手交易分类需要审核
INSERT INTO campus_moderation_rule (rule_id, rule_type, rule_key, rule_value, action, priority, status, description, del_flag)
VALUES (1, 'CATEGORY', '2', '{}', 'PENDING', 10, '0', '二手交易分类需要审核', b'0');

-- 内容数据
INSERT INTO campus_content (content_id, user_id, category_id, content, status, type, love_count, del_flag)
VALUES (1001, 100, 1, '这是正常内容', 1, 0, 5, b'0');
INSERT INTO campus_content (content_id, user_id, category_id, content, status, type, love_count, del_flag)
VALUES (1002, 100, 1, '这是被拒绝的内容', 3, 0, 0, b'0');

-- 评论数据
INSERT INTO campus_comment (comment_id, parent_id, user_id, to_user_id, one_level_id, content_id, co_content, frozen_status, del_flag)
VALUES (2001, 0, 101, NULL, -1, 1001, '这是一级评论', 0, b'0');
INSERT INTO campus_comment (comment_id, parent_id, user_id, to_user_id, one_level_id, content_id, co_content, frozen_status, del_flag)
VALUES (2002, 2001, 100, 101, 2001, 1001, '这是二级评论', 0, b'0');

-- 点赞数据
INSERT INTO campus_content_love (user_id, content_id) VALUES (101, 1001);
INSERT INTO campus_content_love (user_id, content_id) VALUES (100, 1001);

-- 系统配置
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, del_flag)
VALUES (1, '系统日志开关', 'sys.log.global.flag', 'true', 'Y', b'0');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, del_flag)
VALUES (2, '默认头像', 'sys.user.defaultAvatar', '/profile/avatar/default.png', 'Y', b'0');
