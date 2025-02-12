package com.luoying.luoojbackendmodel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论举报表
 * @TableName comment_report
 */
@TableName(value ="comment_report")
@Data
public class CommentReport implements Serializable {
    /**
     * 评论举报记录id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 检举人id
     */
    private Long userId;

    /**
     * 评论类型（0-题解评论，1-题目评论）
     */
    private Integer commentType;

    /**
     * 被检举评论的id
     */
    private Long commentId;

    /**
     * 被检举人id
     */
    private Long reportedUserId;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}