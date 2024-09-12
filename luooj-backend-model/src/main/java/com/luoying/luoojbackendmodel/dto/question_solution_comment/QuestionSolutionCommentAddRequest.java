package com.luoying.luoojbackendmodel.dto.question_solution_comment;

import lombok.Data;

import java.io.Serializable;

/**
 * 题解评论创建请求
 *
 * @Author 落樱的悔恨
 * @Date 2024/9/8 13:58
 */
@Data
public class QuestionSolutionCommentAddRequest implements Serializable {
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
