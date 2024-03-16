package com.luoying.luoojbackendmodel.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 邮箱账号注册请求
 *
 * @author 落樱的悔恨
 */
@Data
public class UserEmailRegisterRequest implements Serializable {
    /**
     * 用户昵称
     */
    private String userName;

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
