package com.luoying.luoojbackendcommon.constant;

/**
 * @author 落樱的悔恨
 */
public class RedisKey {
    private static final String BASE_KEY = "luooj:backend:";

    /**
     * 邮箱验证码的key
     */
    public static final String EMAIL_CAPTCHA_KEY = "captcha:email_%s";


    public static String getKey(String key, Object... o) {
        return BASE_KEY + String.format(key, o);
    }
}
