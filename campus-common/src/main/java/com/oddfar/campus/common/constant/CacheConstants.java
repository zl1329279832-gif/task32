package com.oddfar.campus.common.constant;

/**
 * 缓存的key 常量
 */
public class CacheConstants {
    /**
     * 登录用户 redis token key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 登录用户 redis userId key
     */
    public static final String LOGIN_USER_KEY = "login_user:";
    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";


    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys_dict:";

    /**
     * 防重提交 redis key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 限流 redis key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 登录账户密码错误次数 redis key
     */
    public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";

    /**
     * 内容详情缓存 redis key
     */
    public static final String CONTENT_DETAIL_KEY = "campus:content:detail:";

    /**
     * 内容热门列表缓存 redis key
     */
    public static final String CONTENT_HOT_KEY = "campus:content:hot";

    /**
     * 内容最新列表缓存 redis key
     */
    public static final String CONTENT_NEWEST_KEY = "campus:content:newest";

    /**
     * 内容评论缓存 redis key
     */
    public static final String CONTENT_COMMENTS_KEY = "campus:content:comments:";
}
