package com.luoying.luoojbackendmodel.dto.comment_report;

import com.luoying.luoojbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/10 19:39
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommentReportQueryRequest extends PageRequest implements Serializable {
    /**
     * 评论举报记录id
     */
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

    private static final long serialVersionUID = 1L;
}
