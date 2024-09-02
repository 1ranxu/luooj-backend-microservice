package com.luoying.luoojbackendmodel.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 落樱的悔恨
 * 用户VO（脱敏）
 */
@Data
public class UserVO implements Serializable {

    /**
     * 用户 id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 性别 0-男 1-女
     */
    private Integer gender;

    /**
     * 积分
     */
    private Long score;

    /**
     * 粉丝数
     */
    private Long fans;

    /**
     * 关注数
     */
    private Long followers;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}