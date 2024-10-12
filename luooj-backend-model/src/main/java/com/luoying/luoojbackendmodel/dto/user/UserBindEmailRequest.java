package com.luoying.luoojbackendmodel.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 绑定邮箱请求
 */
@Data
public class UserBindEmailRequest implements Serializable {
    /**
     * 邮箱
     */
    private String email;

    /**
     * 验证码
     */
    private String captcha;

    private static final long serialVersionUID = 1L;
}
