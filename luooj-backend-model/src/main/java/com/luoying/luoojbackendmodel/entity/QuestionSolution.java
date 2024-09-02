package com.luoying.luoojbackendmodel.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 题解表
 * @TableName question_solution
 */
@TableName(value ="question_solution")
@Data
public class QuestionSolution implements Serializable {
    /**
     * 题解id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 题解标题
     */
    private String title;

    /**
     * 题解内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Long likes;

    /**
     * 评论数
     */
    private Long comments;

    /**
     * 收藏数
     */
    private Long favourNum;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除 0-删除 1-正常
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}