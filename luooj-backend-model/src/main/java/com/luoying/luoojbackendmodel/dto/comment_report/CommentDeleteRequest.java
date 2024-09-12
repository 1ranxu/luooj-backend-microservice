package com.luoying.luoojbackendmodel.dto.comment_report;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/10 17:06
 */
@Data
public class CommentDeleteRequest implements Serializable {
    /**
     * 评论举报id
     */
    private Long id;

    /**
     * 评论类型（0-题解评论，1-题目评论）
     */
    private Integer commentType;

    /**
     * 被检举评论的id
     */
    private Long commentId;

    private static final long serialVersionUID = 1L;
}
