package com.luoying.luoojbackendmodel.dto.question_solution_comment;

import com.luoying.luoojbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/8 15:25
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionSolutionCommentQueryRequest extends PageRequest implements Serializable {
    /**
     * 题解评论id
     */
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

    private static final long serialVersionUID = 1L;
}
