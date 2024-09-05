package com.luoying.luoojbackendmodel.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/6/26 11:46
 */
@Data
public class UserPasswordUpdateRequest implements Serializable {
    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认密码
     */
    private String checkPassword;


    /**
     * 验证码
     */
    private String captcha;

    private static final long serialVersionUID = 1L;
}
