package com.luoying.luoojbackendmodel.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 邮箱账号登录请求
 */
@Data
public class UserEmailLoginRequest implements Serializable {
    /**
     * 邮箱
     */
    private String emailAccount;

    /**
     * 验证码
     */
    private String captcha;

    private static final long serialVersionUID = 1L;
}
