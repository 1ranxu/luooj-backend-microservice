package com.luoying.luoojbackendmodel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 题解评论表
 * @TableName question_solution_comment
 */
@TableName(value ="question_solution_comment")
@Data
public class QuestionSolutionComment implements Serializable {
    /**
     * 题解评论id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 题解id
     */
    private Long solutionId;

    /**
     * 本评论关联的1级评论id，如果本评论是一级评论，则值为0
     */
    private Long parentId;

    /**
     * 本评论回复的评论的发布人，如果本评论是一级评论，则值为0
     */
    private Long respondUserId;

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
    private List<QuestionSolutionComment> childList;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}