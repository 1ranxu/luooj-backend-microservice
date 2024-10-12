package com.luoying.luoojbackendcommon.constant;

/**
 * @Description: 电子邮件常量
 */
public interface EmailConstant {

    /**
     * html电子邮件模板路径 resources目录下
     */
    String EMAIL_HTML_CONTENT_PATH = "email.html";

    /**
     * 电子邮件主题
     */
    String EMAIL_SUBJECT = "验证码邮件";

    /**
     * 电子邮件标题
     */
    String EMAIL_TITLE = "LUO-OJ 在线判题平台";

    /**
     * 电子邮件标题英语
     */
    String EMAIL_TITLE_ENGLISH = "LUO-OJ Online Judge Platform";

    /**
     * 平台负责人
     */
    String PLATFORM_RESPONSIBLE_PERSON = "落樱工作室";

    /**
     * 平台地址
     */
    String PLATFORM_ADDRESS = "<a href='http://luooj.icu/'>请联系我们</a>";

    /**
     * 注册操作
     */
    String REGISTER = "REGISTER";


    /**
     * 登录操作
     */
    String LOGIN = "LOGIN";

    /**
     * 更新密码操作
     */
    String UPDATE_PASSWORD = "UPDATE_PASSWORD";

    /**
     * 更新邮箱操作
     */
    String UPDATE_EMAIL = "UPDATE_EMAIL";

}
