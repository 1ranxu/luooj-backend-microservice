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
     * 邮箱验证码的key和过期时间
     */
    public static final String EMAIL_CAPTCHA_KEY = "user_service:email_captcha:%s";
    public static final long EMAIL_CAPTCHA_KEY_TTL = 5L;

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



    public static String getKey(String key, Object... o) {
        return BASE_KEY + String.format(key, o);
    }
}
