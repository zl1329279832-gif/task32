package com.oddfar.campus.business.enums;

/***
 * 错误码和错误信息定义类
 * 1. 错误码定义规则为5为数字
 * 2. 前两位表示业务场景，最后三位表示错误码。例如：100001。10:通用 001:系统未知异常
 * 3. 维护错误码后需要维护错误描述，将他们定义为枚举形式
 * 错误码列表：
 *  10: 通用
 *      001：参数格式校验
 *      002：短信验证码频率太高
 *  11: 用户
 *  12: 校园墙
 *  13: 评论
 *
 */
public enum CampusBizCodeEnum {

    /**
     * 系统通用
     */
    UNKNOWN_EXCEPTION(10000, "系统未知错误"),
    VALID_EXCEPTION(10001, "参数校验异常"),
    SMS_CODE_EXCEPTION(10002, "验证码获取频率太高，请稍后再试"),
    TOO_MANY_REQUEST(10003, "请求流量过大，请稍后再试"),
    TOO_MANY_File(10004, "上传文件数量超限，请明日上传"),
    CODE_ERROR(10005, "验证码错误"),

    NO_THREE_CATEGORY(10010, "禁止添加三级分类"),
    CATEGORY_NAME_REPEAT(10011, "分类名称重复"),
    CATEGORY_SLUG_REPEAT(10012, "分类缩写名重复"),
    CATEGORY_NOT_EXIST(10013, "分类不存在"),

    TAG_NAME_REPEAT(10021, "标签缩写名重复"),

    /**
     * 信息墙
     */
    CONTENT_IS_NULL(12001,"信息墙内容不存在"),
    CONTENT_OPERATION_PROHIBITED(12002,"信息墙禁止操作"),
    CONTENT_NOT_NULL(12003,"信息墙内容不能为空"),
    CONTENT_FILE_COUNT_TOO_MANY(12004,"信息墙文件数量过多"),
    CONTENT_FILE_COUNT_EXCEPTION(12005,"信息墙文件数量异常"),
    CONTENT_FILE_EXCEPTION(12006,"信息墙文件异常"),
    CONTENT_NOT_YOU(12007,"这不是你的信息墙"),

    /**
     * 用户
     */
    NOT_LOGGED_IN(11001,"请登录后操作"),
    EMAIL_NOT_EXIST(11002,"邮箱不存在"),

    /**
     * 评论
     */
    COMMENT_IS_NULL(13001,"评论不存在"),
    COMMENT_DEL_ERR(13002,"评论删除失败"),
    COMMENT_BLOCKED(13003,"评论包含违规内容"),
    COMMENT_STATUS_ABNORMAL(13004,"评论状态异常"),

    /**
     * 审核
     */
    MODERATION_CONTENT_NOT_EXIST(14001,"审核内容不存在"),
    MODERATION_INVALID_ACTION(14002,"无效的审核操作"),
    MODERATION_BATCH_EMPTY(14003,"批量操作列表不能为空"),

    /**
     * 申诉
     */
    APPEAL_NOT_EXIST(15101,"申诉记录不存在"),
    APPEAL_NOT_OWN(15102,"无权操作此申诉"),
    APPEAL_ALREADY_EXISTS(15103,"已存在进行中的申诉"),
    APPEAL_TARGET_NOT_ELIGIBLE(15104,"该内容当前状态不支持申诉"),
    APPEAL_ALREADY_REVIEWED(15105,"申诉已处理"),

    /**
     * 信用
     */
    CREDIT_SCORE_INSUFFICIENT(16001,"信用分不足"),

    /**
     * 敏感词
     */
    SENSITIVE_WORD_DUPLICATE(17001,"敏感词已存在"),

    SMS_SEND_CODE_EXCEPTION(10403, "短信发送失败"),
    PHONE_EXIST_EXCEPTION(15002, "手机号已经存在"),
    USER_EXIST_EXCEPTION(15001, "用户已经存在"),

    PHONE_EXISTS_EXCEPTION(15002, "手机号已存在"),

    LOGIN_ACCOUNT_PASSWORD_INVALID(15003, "用户名或密码错误");


    private Integer code;
    private String msg;

    private CampusBizCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
