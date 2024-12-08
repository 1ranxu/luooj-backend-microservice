package com.luoying.luoojbackendmodel.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 题目评论表
 * @TableName question_comment
 */
@TableName(value ="question_comment")
@Data
public class QuestionComment implements Serializable {
    /**
     * 评论id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户昵称
     */
    @TableField(exist = false)
    private String userName;

    /**
     * 用户头像
     */
    @TableField(exist = false)
    private String userAvatar;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 本评论关联的1级评论id，如果本评论是一级评论，则值为0
     */
    private Long parentId;

    /**
     * 本评论回复的评论的发布人，如果本评论是一级评论，则值为0
     */
    private Long respondUserId;

    /**
     * 本评论回复的评论的发布人的昵称，如果本评论是一级评论，则值为空
     */
    @TableField(exist = false)
    private String respondUserName;

    /**
     * 回复内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Long likes;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 是否点赞过
     */
    @TableField(exist = false)
    private Boolean isLike;

    /**
     * 子评论列表
     */
    @TableField(exist = false)
    private List<QuestionComment> childList;

    /**
     * 子评论列表
     */
    @TableField(exist = false)
    private Boolean showReplyBox = false;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}