package com.luoying.luoojbackendcommon.constant;

/**
 * @author 落樱的悔恨
 */
public class RedisKey {
    private static final String BASE_KEY = "luooj:backend:";

    /**
     * 空值的有效期
     */
    public static final long CACHE_NULL_TTL = 2L;

    /**
     * 互斥锁的key
     */
    public static final String LOCK_QUESTION_KEY = "question_service:question_lock:%s";
    public static final long LOCK_QUESTION_KEY_TTL = 20L;

    /**
     * 注册操作时邮箱验证码的key和过期时间
     */
    public static final String REGISTER_EMAIL_CAPTCHA_KEY = "user_service:email_captcha:register:%s";
    public static final long REGISTER_EMAIL_CAPTCHA_KEY_TTL = 5L;

    /**
     * 登录操作时邮箱验证码的key和过期时间
     */
    public static final String LOGIN_EMAIL_CAPTCHA_KEY = "user_service:email_captcha:login:%s";
    public static final long LOGIN_EMAIL_CAPTCHA_KEY_TTL = 5L;

    /**
     * 更新密码操作时邮箱验证码的key和过期时间
     */
    public static final String UPDATE_PASSWORD_EMAIL_CAPTCHA_KEY = "user_service:email_captcha:update_password:%s";
    public static final long UPDATE_PASSWORD_EMAIL_CAPTCHA_KEY_TTL = 5L;

    /**
     * 更新邮箱操作时邮箱验证码的key和过期时间
     */
    public static final String UPDATE_EMAIL_EMAIL_CAPTCHA_KEY = "user_service:email_captcha:update_email:%s";
    public static final long UPDATE_EMAIL_EMAIL_CAPTCHA_KEY_TTL = 5L;

    /**
     * 单个题目的key和过期时间
     */
    public static final String SINGLE_QUESTION_KEY = "question_service:question_cache:%s";
    public static final long SINGLE_QUESTION_KEY_TTL = 30L;

    /**
     * 单个用户提交题目的key和过期时间
     */
    public static final String SINGLE_USER_QUESTION_SUBMIT_KEY = "question_service:question_submit_user:%s";
    public static final long SINGLE_USER_QUESTION_SUBMIT_KEY_TTL = 5L;

    /**
     * 通过题目数排名的key和过期时间
     */
    public static final String ACCEPTED_QUESTION_RANK_KEY = "question_service:accepted_question_rank";
    public static final long ACCEPTED_QUESTION_RANK_KEY_TTL = 30L;

    /**
     * 各个用户的关注列表
     */
    public static final String FOLLOW_KEY = "user_service:follow:%s";
    public static final long FOLLOW_KEY_TTL = 30L;

    /**
     * 某业务的点赞列表（题目，题解，题目评论，题解评论）
     */
    public static final String LIKE_LIST_KEY = "question_service:%s:%s";
    public static final long LIKE_LIST_KEY_TTL = 30L;

    /**
     * 竞赛的参赛人员
     */
    public static final String CONTEST_APPLY_KEY = "question_service:contest_apply:%s";
    public static final long CONTEST_APPLY_KEY_TTL = 30L;

    /**
     * 竞赛排名统计
     */
    public static final String CONTEST_RANK_KEY = "question_service:contest_rank:%s";
    public static final long CONTEST_RANK_KEY_TTL = 30L;



    public static String getKey(String key, Object... o) {
        return BASE_KEY + String.format(key, o);
    }
}
