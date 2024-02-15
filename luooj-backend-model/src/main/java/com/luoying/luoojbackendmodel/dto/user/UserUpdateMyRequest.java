package com.luoying.luoojbackendmodel.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 落樱的悔恨
 * 用户个人信息更新请求
 */
@Data
public class UserUpdateMyRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    private static final long serialVersionUID = 1L;
}