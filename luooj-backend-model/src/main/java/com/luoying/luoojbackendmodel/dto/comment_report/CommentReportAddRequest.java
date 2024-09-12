package com.luoying.luoojbackendmodel.dto.comment_report;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/10 17:06
 */
@Data
public class CommentReportAddRequest implements Serializable {
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

    private static final long serialVersionUID = 1L;
}
